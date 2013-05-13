package me.xiaopan.networkeasy;

import java.lang.reflect.Type;

import me.xiaopan.networkeasy.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 默认的JSON响应处理器
 * @author xiaopan
 */
public abstract class JsonHttpResponseHandler<T> extends HttpResponseHandler {
	private static Gson gson;
	private Class<?> responseClass;
	private Type responseType;
	
	public JsonHttpResponseHandler(Class<?> responseClass){
		this.responseClass = responseClass;
	}
	
	public JsonHttpResponseHandler(Context context, Type responseType){
		this.responseType = responseType;
	}
	
	@Override
	public void sendStartMessage() {
		sendEmptyMessage(MESSAGE_START);
	}

	@Override
	public void sendHandleResponseMessage(HttpResponse httpResponse) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() < 300 ){
			Object result = null;
			HttpEntity httpEntity = httpResponse.getEntity();
			if(httpEntity != null){
				/* 读取返回的JSON字符串并转换成对象 */
				String jsonString = EntityUtils.toString(new BufferedHttpEntity(httpEntity), Utils.getResponseCharset(httpResponse));
				if(jsonString != null && !"".equals(jsonString)){
					if(gson == null){
						gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
					}
					
					if(responseClass != null){	//如果是要转换成一个对象
						ResponseBodyKey responseBodyKey = responseClass.getAnnotation(ResponseBodyKey.class);
						if(responseBodyKey != null && responseBodyKey.value() != null && !"".equals(responseBodyKey.value())){
							result = gson.fromJson(new JSONObject(jsonString).getString(responseBodyKey.value()), responseClass);
						}else{
							result = gson.fromJson(jsonString, responseClass);
						}
					}else if(responseType != null){	//如果是要转换成一个集合
						result = gson.fromJson(jsonString, responseType);
					}
				}
			}
			sendMessage(obtainMessage(MESSAGE_SUCCESS, result));
		}else{
			sendMessage(obtainMessage(MESSAGE_FAILURE, httpResponse));
		}
	}
	
	@Override
	public void sendExceptionMessage(Throwable e) {
		sendMessage(obtainMessage(MESSAGE_EXCEPTION, e));
	}

	@Override
	public void sendEndMessage() {
		sendEmptyMessage(MESSAGE_END);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
			case MESSAGE_START: onStart(); break;
			case MESSAGE_SUCCESS: onSuccess((T) msg.obj); break;
			case MESSAGE_FAILURE: onFailure((HttpResponse) msg.obj); break;
			case MESSAGE_EXCEPTION: onException((Throwable) msg.obj); break;
			case MESSAGE_END: onEnd(); break;
		}
	}
	
	public abstract void onStart();
	public abstract void onSuccess(T responseObject);
	public abstract void onFailure(HttpResponse httpResponse);
	public abstract void onException(Throwable e);
	public abstract void onEnd();
}