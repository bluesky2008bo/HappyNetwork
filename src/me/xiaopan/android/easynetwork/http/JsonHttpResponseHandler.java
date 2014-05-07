/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.android.easynetwork.http;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BufferedHttpEntity;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

import com.google.gson.GsonBuilder;

/**
 * 默认的JsonHttp响应处理器
 */
public abstract class JsonHttpResponseHandler<T> extends HttpResponseHandler {
	private Context context;
	private Class<?> responseClass;
	private Type responseType;

	public JsonHttpResponseHandler(Context context, Class<?> responseClass){
		this.context = context;
		this.responseClass = responseClass;
	}
	
	public JsonHttpResponseHandler(Context context, Type responseType){
		this.context = context;
		this.responseType = responseType;
	}
	
	@Override
	protected final void onStart(final Handler handler) {
		if(isCancelled()) return;
        handler.post(new Runnable() {
            @Override
            public void run() {
        		if(isCancelled()) return;
                onStart();
            }
        });
	}

	@Override
	protected final void onHandleResponse(final Handler handler, HttpUriRequest request, final HttpResponse httpResponse, final boolean isNotRefresh, final boolean isOver) throws Throwable {
		if(!(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300)){
			if(httpResponse.getStatusLine().getStatusCode() == 404){
				throw new FileNotFoundException("请求地址错误："+request.getURI().toString());
			}else{
				throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), "异常状态码："+httpResponse.getStatusLine().getStatusCode()+"："+request.getURI().toString());
			}
		}
		
		HttpEntity httpEntity = httpResponse.getEntity();
		if(httpEntity == null){
            throw new Exception("没有响应体："+request.getURI().toString());
		}
		
		String jsonString = toString(new BufferedHttpEntity(httpEntity), this, handler, "UTF-8");
		if(isCancelled()) return;
		
		if(jsonString == null || "".equals(jsonString)){
			throw new Exception("响应内容为空："+request.getURI().toString());
		}
		
		if(responseClass != null){	//如果是要转换成一个对象
			String responseBodyKey = RequestParser.parseResponseBodyAnnotation(context, responseClass);
			if(responseBodyKey != null && !"".equals(responseBodyKey)){
				final Object object = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(new JSONObject(jsonString).getString(responseBodyKey), responseClass);
				handler.post(new Runnable() {
					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						if(isCancelled()) return;
						onSuccess(httpResponse, (T) object, isNotRefresh, isOver);
					}
				});
			}else{
				final Object object = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(jsonString, responseClass);
				handler.post(new Runnable() {
					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						if(isCancelled()) return;
						onSuccess(httpResponse, (T) object, isNotRefresh, isOver);
					}
				});
			}
		}else if(responseType != null){	//如果是要转换成一个集合
			final Object object = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(jsonString, responseType);
			handler.post(new Runnable() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					if(isCancelled()) return;
					onSuccess(httpResponse, (T) object, isNotRefresh, isOver);
				}
			});
		}else{
			throw new Exception("responseClass和responseType至少有一个不能为null");
		}
	}
    
    @Override
    protected final void onUpdateProgress(Handler handler, final long totalLength, final long completedLength){
    	if(isCancelled()) return;
    	handler.post(new Runnable() {
    		@Override
    		public void run() {
    			if(isCancelled()) return;
    			onUpdateProgress(totalLength, completedLength);
    		}
    	});
    }
	
	@Override
	protected final void onException(final Handler handler, final Throwable e, final boolean isNotRefresh) {
		if(isCancelled()) return;
        handler.post(new Runnable() {
            @Override
            public void run() {
        		if(isCancelled()) return;
                onFailure(e, isNotRefresh);
            }
        });
	}
	
	@Override
	protected final void onCancel(Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onCancel();
            }
        });
	}

	/**
	 * 请求开始
	 */
	protected abstract void onStart();

	/**
	 * 更新进度
	 * @param totalLength
	 * @param completedLength
	 */
    public void onUpdateProgress(long totalLength, long completedLength){};
	
	/**
	 * 请求成功
	 * @param httpResponse  Http响应
	 * @param responseObject 响应内容
     * @param isNotRefresh 本次响应不是刷新
	 * @param isOver 本次执行是否是最后一次
	 */
	protected abstract void onSuccess(HttpResponse httpResponse, T responseObject, boolean isNotRefresh, boolean isOver);
	
	/**
	 * 请求失败
	 * @param throwable 异常
	 * @param isNotRefresh 本次异常不是在刷新缓存数据的时候发生的
	 */
	protected abstract void onFailure(Throwable throwable, boolean isNotRefresh);

    /**
     * 请求取消
     */
    protected void onCancel(){

    }
}