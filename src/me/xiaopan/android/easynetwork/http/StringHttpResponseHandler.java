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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import android.os.Handler;

/**
 * 默认的字符串Http响应处理器
 */
public abstract class StringHttpResponseHandler extends HttpResponseHandler {

	@Override
	protected void onStart(final Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onStart();
            }
        });
	}

	@Override
	protected void onHandleResponse(final Handler handler, final HttpResponse httpResponse, final boolean isNotRefresh, final boolean isOver) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300 ){
			/* 读取内容并转换成字符串 */
			HttpEntity httpEntity = httpResponse.getEntity();
			if(httpEntity != null){
                final String responseContent = EntityUtils.toString(new BufferedHttpEntity(httpEntity), HttpUtils.getResponseCharset(httpResponse));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onSuccess(httpResponse, responseContent, isNotRefresh, isOver);
                    }
                });
			}else{
                throw new Exception("没有响应体");
			}
		}else{
			if(httpResponse.getStatusLine().getStatusCode() == 404){
				throw new FileNotFoundException("请求地址错误");
			}else{
				throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), "异常状态码："+httpResponse.getStatusLine().getStatusCode());
			}
		}
	}

	@Override
	protected void onException(final Handler handler, final Throwable e, final boolean isNotRefresh) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onFailure(e, isNotRefresh);
            }
        });
	}
	
	@Override
	protected void onCancel(Handler handler) {
		
	}

	/**
	 * 请求开始
	 */
	protected abstract void onStart();
	
	/**
	 * 请求成功
	 * @param httpResponse Http响应
	 * @param responseContent 响应内容
     * @param isNotRefresh 本次响应不是刷新
	 * @param isOver 本次执行是否是最后一次
	 */
	protected abstract void onSuccess(HttpResponse httpResponse, String responseContent, boolean isNotRefresh, boolean isOver);
	
	/**
	 * 请求失败
	 * @param throwable 异常
	 * @param isNotRefresh 本次异常不是在刷新缓存数据的时候发生的
	 */
	protected abstract void onFailure(Throwable throwable, boolean isNotRefresh);
}
