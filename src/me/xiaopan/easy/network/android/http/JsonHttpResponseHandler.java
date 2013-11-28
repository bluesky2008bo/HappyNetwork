/*
 * Copyright 2013 Peng fei Pan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.xiaopan.easy.network.android.http;

import android.os.Handler;

import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.lang.reflect.Type;

import me.xiaopan.easy.network.android.http.annotation.ResponseBodyKey;

/**
 * 默认的JsonHttp响应处理器
 */
public abstract class JsonHttpResponseHandler<T> extends HttpResponseHandler {
	private Class<?> responseClass;
	private Type responseType;

	public JsonHttpResponseHandler(Class<?> responseClass){
		this.responseClass = responseClass;
	}
	
	public JsonHttpResponseHandler(Type responseType){
		this.responseType = responseType;
	}
	
	@Override
	public void start(final Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onStart();
            }
        });
	}

	@Override
	public void handleResponse(final Handler handler, final HttpResponse httpResponse, final boolean isCache, final boolean isRefreshCacheAndCallback) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300 ){
			HttpEntity httpEntity = httpResponse.getEntity();
			if(httpEntity != null){
				/* 读取返回的JSON字符串并转换成对象 */
				String jsonString = EntityUtils.toString(new BufferedHttpEntity(httpEntity), HttpUtils.getResponseCharset(httpResponse));
				if(jsonString != null && !"".equals(jsonString)){
					if(responseClass != null){	//如果是要转换成一个对象
                        ResponseBodyKey responseBodyKey = responseClass.getAnnotation(ResponseBodyKey.class);
						if(responseBodyKey != null && responseBodyKey.value() != null && !"".equals(responseBodyKey.value())){
                            final Object object = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(new JSONObject(jsonString).getString(responseBodyKey.value()), responseClass);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onSuccess((T) object, isCache, isRefreshCacheAndCallback);
                                }
                            });
						}else{
                            final Object object = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(jsonString, responseClass);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onSuccess((T) object, isCache, isRefreshCacheAndCallback);
                                }
                            });
						}
					}else if(responseType != null){	//如果是要转换成一个集合
                        final Object object = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(jsonString, responseType);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onSuccess((T) object, isCache, isRefreshCacheAndCallback);
                            }
                        });
					}else{
                        throw new Exception("responseClass和responseType至少有一个不能为null");
					}
				}else{
                    throw new Exception("响应内容为空");
				}
			}else{
                throw new Exception("没有响应体");
			}
		}else{
            throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), "异常状态码："+httpResponse.getStatusLine().getStatusCode());
		}
	}
	
	@Override
	public void exception(final Handler handler, final Throwable e) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onFailure(e);
            }
        });
	}
	
	public abstract void onStart();
	public abstract void onSuccess(T responseObject, boolean isCache, boolean isRefreshCacheAndCallback);
	public abstract void onFailure(Throwable throwable);
}