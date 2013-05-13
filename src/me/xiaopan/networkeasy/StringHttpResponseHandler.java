package me.xiaopan.networkeasy;

import me.xiaopan.networkeasy.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import android.os.Message;

public abstract class StringHttpResponseHandler extends HttpResponseHandler {
	@Override
	public void sendStartMessage() {
		sendEmptyMessage(MESSAGE_START);
	}

	@Override
	public void sendHandleResponseMessage(HttpResponse httpResponse) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() < 300 ){
			/* 读取内容并转换成字符串 */
			HttpEntity httpEntity = httpResponse.getEntity();
			if(httpEntity != null){
				sendMessage(obtainMessage(MESSAGE_SUCCESS, EntityUtils.toString(new BufferedHttpEntity(httpEntity), Utils.getResponseCharset(httpResponse))));
			}else{
				sendMessage(obtainMessage(MESSAGE_SUCCESS));
			}
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
	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
			case MESSAGE_START: onStart(); break;
			case MESSAGE_SUCCESS: onSuccess((String) msg.obj); break;
			case MESSAGE_FAILURE: onFailure((HttpResponse) msg.obj); break;
			case MESSAGE_EXCEPTION: onException((Throwable) msg.obj); break;
			case MESSAGE_END: onEnd(); break;
		}
	}

	public abstract void onStart();
	public abstract void onSuccess(String responseContent);
	public abstract void onFailure(HttpResponse httpResponse);
	public abstract void onException(Throwable e);
	public abstract void onEnd();
}