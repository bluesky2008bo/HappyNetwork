package me.xiaopan.networkeasy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.Message;

public class BinaryHttpResponseHandler extends HttpResponseHandler {
    private Context context;
	private BinaryHttpResponseHandleListener binaryHttpResponseHandleListener;
	
	public BinaryHttpResponseHandler(Context context, BinaryHttpResponseHandleListener binaryHttpResponseHandleListener){
		this.context = context;
		this.binaryHttpResponseHandleListener = binaryHttpResponseHandleListener;
	}
	
	@Override
	public void onStart() {
		sendEmptyMessage(MESSAGE_START);
	}

	@Override
	public void onHandleResponse(HttpResponse httpResponse) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() < 300 ){
			HttpEntity httpEntity = httpResponse.getEntity();
			sendMessage(obtainMessage(MESSAGE_SUCCESS, httpEntity != null?EntityUtils.toByteArray(new BufferedHttpEntity(httpEntity)):null));
		}else{
			sendMessage(obtainMessage(MESSAGE_FAILURE, httpResponse));
		}
	}

	@Override
	public void onException(Throwable e) {
		sendMessage(obtainMessage(MESSAGE_EXCEPTION, e));
	}

	@Override
	public void onEnd() {
		sendEmptyMessage(MESSAGE_END);
	}
	
	@Override
	public void handleMessage(Message msg) {
		if(binaryHttpResponseHandleListener != null){
			switch(msg.what) {
				case MESSAGE_START: binaryHttpResponseHandleListener.onStart(); break;
				case MESSAGE_SUCCESS: binaryHttpResponseHandleListener.onSuccess((byte[]) msg.obj); break;
				case MESSAGE_FAILURE: binaryHttpResponseHandleListener.onFailure((HttpResponse) msg.obj); break;
				case MESSAGE_EXCEPTION: binaryHttpResponseHandleListener.onException(context, (Throwable) msg.obj); break;
				case MESSAGE_END: binaryHttpResponseHandleListener.onEnd(); break;
			}
		}
	}
	
	public interface BinaryHttpResponseHandleListener{
		public void onStart();
		public void onSuccess(byte[] binaryData);
		public void onFailure(HttpResponse httpResponse);
		public void onException(Context context, Throwable e);
		public void onEnd();
	}
}