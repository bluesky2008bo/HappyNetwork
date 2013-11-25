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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

/**
 * 默认的二进制Http响应处理器
 */
public abstract class BinaryHttpResponseHandler extends HttpResponseHandler {
	private static final int MESSAGE_START = 0;
	private static final int MESSAGE_SUCCESS = 1;
	private static final int MESSAGE_FAILURE = 2;
	private Handler handler;
	
	@SuppressLint("HandlerLeak")
	public BinaryHttpResponseHandler(){
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
					case MESSAGE_START: onStart(); break;
					case MESSAGE_SUCCESS: onSuccess((byte[]) msg.obj); break;
					case MESSAGE_FAILURE: onFailure((Throwable) msg.obj); break;
				}
			}
		};
	}
	
	@Override
	public void start() {
		handler.sendEmptyMessage(MESSAGE_START);
	}

	@Override
	public void handleResponse(HttpResponse httpResponse) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300 ){
			HttpEntity httpEntity = httpResponse.getEntity();
			handler.sendMessage(handler.obtainMessage(MESSAGE_SUCCESS, httpEntity != null?EntityUtils.toByteArray(new BufferedHttpEntity(httpEntity)):null));
		}else{
			handler.sendMessage(handler.obtainMessage(MESSAGE_FAILURE, new HttpStatusCodeException(httpResponse.getStatusLine().getStatusCode())));
		}
	}

	@Override
	public void exception(Throwable e) {
		handler.sendMessage(handler.obtainMessage(MESSAGE_FAILURE, e));
	}
	
	public abstract void onStart();
	public abstract void onSuccess(byte[] binaryData);
	public abstract void onFailure(Throwable throwable);
}
