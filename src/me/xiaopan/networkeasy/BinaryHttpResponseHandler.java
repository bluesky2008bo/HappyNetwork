package me.xiaopan.networkeasy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import android.os.Message;

public abstract class BinaryHttpResponseHandler extends HttpResponseHandler {
	@Override
	public void sendStartMessage() {
		sendEmptyMessage(MESSAGE_START);
	}

	@Override
	public void sendHandleResponseMessage(HttpResponse httpResponse) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() < 300 ){
			HttpEntity httpEntity = httpResponse.getEntity();
			sendMessage(obtainMessage(MESSAGE_SUCCESS, httpEntity != null?EntityUtils.toByteArray(new BufferedHttpEntity(httpEntity)):null));
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
			case MESSAGE_START: sendStartMessage(); break;
			case MESSAGE_SUCCESS: onSuccess((byte[]) msg.obj); break;
			case MESSAGE_FAILURE: onFailure((HttpResponse) msg.obj); break;
			case MESSAGE_EXCEPTION: onException((Throwable) msg.obj); break;
			case MESSAGE_END: sendEndMessage(); break;
		}
	}
	
	public abstract void onStart();
	public abstract void onSuccess(byte[] binaryData);
	public abstract void onFailure(HttpResponse httpResponse);
	public abstract void onException(Throwable e);
	public abstract void onEnd();
}