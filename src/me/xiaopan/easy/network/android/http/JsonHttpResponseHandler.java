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

import java.lang.reflect.Type;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.google.gson.GsonBuilder;

/**
 * 默认的JsonHttp响应处理器
 */
public abstract class JsonHttpResponseHandler<T> extends Handler implements HttpResponseHandler {
	private static final int MESSAGE_START = 0;
	private static final int MESSAGE_SUCCESS = 1;
	private static final int MESSAGE_FAILURE = 2;
	private Class<?> responseClass;
	private Type responseType;
	
	public JsonHttpResponseHandler(Class<?> responseClass){
		this.responseClass = responseClass;
	}
	
	public JsonHttpResponseHandler(Type responseType){
		this.responseType = responseType;
	}
	
	@Override
	public void start() {
		sendEmptyMessage(MESSAGE_START);
	}

	@Override
	public void handleResponse(HttpResponse httpResponse) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300 ){
			HttpEntity httpEntity = httpResponse.getEntity();
			if(httpEntity != null){
				/* 读取返回的JSON字符串并转换成对象 */
				String jsonString = EntityUtils.toString(new BufferedHttpEntity(httpEntity), HttpUtils.getResponseCharset(httpResponse));
				if(jsonString != null && !"".equals(jsonString)){
					if(responseClass != null){	//如果是要转换成一个对象
						ResponseBodyKey responseBodyKey = responseClass.getAnnotation(ResponseBodyKey.class);
						if(responseBodyKey != null && responseBodyKey.value() != null && !"".equals(responseBodyKey.value())){
							sendMessage(obtainMessage(MESSAGE_SUCCESS, new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(new JSONObject(jsonString).getString(responseBodyKey.value()), responseClass)));
						}else{
							sendMessage(obtainMessage(MESSAGE_SUCCESS, new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(jsonString, responseClass)));
						}
					}else if(responseType != null){	//如果是要转换成一个集合
						sendMessage(obtainMessage(MESSAGE_SUCCESS, new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(jsonString, responseType)));
					}else{
						sendMessage(obtainMessage(MESSAGE_FAILURE, new Exception("responseClass和responseType至少有一个不能为null")));
					}
				}else{
					sendMessage(obtainMessage(MESSAGE_FAILURE, new Exception("响应内容为空")));
				}
			}else{
				sendMessage(obtainMessage(MESSAGE_FAILURE, new Exception("没有响应实体")));
			}
		}else{
			sendMessage(obtainMessage(MESSAGE_FAILURE, new HttpStatusCodeException(httpResponse.getStatusLine().getStatusCode())));
		}
	}
	
	@Override
	public void exception(Throwable e) {
		sendMessage(obtainMessage(MESSAGE_FAILURE, e));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
			case MESSAGE_START: onStart(); break;
			case MESSAGE_SUCCESS: onSuccess((T) msg.obj); break;
			case MESSAGE_FAILURE: onFailure((Throwable) msg.obj); break;
		}
	}
	
	public abstract void onStart();
	public abstract void onSuccess(T responseObject);
	public abstract void onFailure(Throwable throwable);
}