package me.xiaopan.easynetwork.android.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import android.os.Handler;
import android.os.Message;

/**
 * 将Http响应转换为二进制数组的处理器
 */
public abstract class BinaryResponseHandler extends Handler implements HttpResponseHandler {
	private static final int MESSAGE_START = 0;
	private static final int MESSAGE_SUCCESS = 1;
	private static final int MESSAGE_FAILURE = 2;
	private static final int MESSAGE_EXCEPTION = 3;
	private static final int MESSAGE_END = 4;
	
	@Override
	public void start() {
		sendEmptyMessage(MESSAGE_START);
	}

	@Override
	public void handleResponse(HttpResponse httpResponse) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() < 300 ){
			HttpEntity httpEntity = httpResponse.getEntity();
			sendMessage(obtainMessage(MESSAGE_SUCCESS, httpEntity != null?EntityUtils.toByteArray(new BufferedHttpEntity(httpEntity)):null));
		}else{
			sendMessage(obtainMessage(MESSAGE_FAILURE, httpResponse));
		}
	}

	@Override
	public void exception(Throwable e) {
		sendMessage(obtainMessage(MESSAGE_EXCEPTION, e));
	}

	@Override
	public void end() {
		sendEmptyMessage(MESSAGE_END);
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
			case MESSAGE_START: start(); break;
			case MESSAGE_SUCCESS: onSuccess((byte[]) msg.obj); break;
			case MESSAGE_FAILURE: onFailure((HttpResponse) msg.obj); break;
			case MESSAGE_EXCEPTION: onException((Throwable) msg.obj); break;
			case MESSAGE_END: end(); break;
		}
	}
	
	public abstract void onStart();
	public abstract void onSuccess(byte[] binaryData);
	public abstract void onFailure(HttpResponse httpResponse);
	public abstract void onException(Throwable e);
	public abstract void onEnd();
}