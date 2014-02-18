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
package me.xiaopan.android.easynetwork.http;


import me.xiaopan.android.easynetwork.http.enums.FailureType;
import me.xiaopan.android.easynetwork.http.enums.ResponseType;

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
	public void start(final Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onStart();
            }
        });
	}

	@Override
	public void handleResponse(final Handler handler, final ResponseType responseType, final HttpResponse httpResponse) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300 ){
			/* 读取内容并转换成字符串 */
			HttpEntity httpEntity = httpResponse.getEntity();
			if(httpEntity != null){
                final String responseContent = EntityUtils.toString(new BufferedHttpEntity(httpEntity), HttpUtils.getResponseCharset(httpResponse));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onSuccess(responseType, httpResponse, responseContent);
                    }
                });
			}else{
                throw new Exception("没有响应体");
			}
		}else{
            throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), "异常状态码："+httpResponse.getStatusLine().getStatusCode());
		}
	}

	@Override
	public void exception(final Handler handler, final FailureType failureType, final Throwable e) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onFailure(failureType, e);
            }
        });
	}

	public abstract void onStart();
	public abstract void onSuccess(ResponseType responseType, HttpResponse httpResponse, String responseContent);
	public abstract void onFailure(FailureType failureType, Throwable throwable);
}
