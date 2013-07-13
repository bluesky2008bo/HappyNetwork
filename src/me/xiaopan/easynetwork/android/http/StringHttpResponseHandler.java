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
package me.xiaopan.easynetwork.android.http;

import me.xiaopan.easynetwork.android.EasyNetworkUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import android.os.Handler;
import android.os.Message;

/**
 * 默认的字符串Http响应处理器
 */
public abstract class StringHttpResponseHandler extends Handler implements HttpResponseHandler {
	private static final int MESSAGE_START = 0;
	private static final int MESSAGE_SUCCESS = 1;
	private static final int MESSAGE_FAILURE = 2;
	private static final int MESSAGE_END = 3;
	
	@Override
	public void start() {
		sendEmptyMessage(MESSAGE_START);
	}

	@Override
	public void handleResponse(HttpResponse httpResponse) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() < 300 ){
			/* 读取内容并转换成字符串 */
			HttpEntity httpEntity = httpResponse.getEntity();
			if(httpEntity != null){
				sendMessage(obtainMessage(MESSAGE_SUCCESS, EntityUtils.toString(new BufferedHttpEntity(httpEntity), EasyNetworkUtils.DEFAULT_CHARSET)));
			}else{
				sendMessage(obtainMessage(MESSAGE_SUCCESS));
			}
		}else{
			sendMessage(obtainMessage(MESSAGE_FAILURE, new HttpStatusCodeException(httpResponse.getStatusLine().getStatusCode())));
		}
	}

	@Override
	public void exception(Throwable e) {
		sendMessage(obtainMessage(MESSAGE_FAILURE, e));
	}

	@Override
	public void end() {
		sendEmptyMessage(MESSAGE_END);
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
			case MESSAGE_START: onStart(); break;
			case MESSAGE_SUCCESS: onSuccess((String) msg.obj); break;
			case MESSAGE_FAILURE: onFailure((Throwable) msg.obj); break;
			case MESSAGE_END: onEnd(); break;
		}
	}

	public abstract void onStart();
	public abstract void onSuccess(String responseContent);
	public abstract void onFailure(Throwable throwable);
	public abstract void onEnd();
}
