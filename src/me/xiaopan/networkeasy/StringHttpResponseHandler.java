package me.xiaopan.networkeasy;

import me.xiaopan.networkeasy.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.Message;

public class StringHttpResponseHandler extends HttpResponseHandler {
    private Context context;
	private StringHttpResponseHandleListener httpResponseHandleListener;
	
	public StringHttpResponseHandler(Context context, StringHttpResponseHandleListener stringHttpResponseHandleListener){
		this.context = context;
		this.httpResponseHandleListener = stringHttpResponseHandleListener;
	}
	
	@Override
	public void onStart() {
		sendEmptyMessage(MESSAGE_START);
	}

	@Override
	public void onHandleResponse(HttpResponse httpResponse) throws Throwable {
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
	public void onException(Throwable e) {
		sendMessage(obtainMessage(MESSAGE_EXCEPTION, e));
	}

	@Override
	public void onEnd() {
		sendEmptyMessage(MESSAGE_END);
	}
	
	@Override
	public void handleMessage(Message msg) {
		if(httpResponseHandleListener != null){
			switch(msg.what) {
				case MESSAGE_START: httpResponseHandleListener.onStart(); break;
				case MESSAGE_SUCCESS: httpResponseHandleListener.onSuccess((String) msg.obj); break;
				case MESSAGE_FAILURE: httpResponseHandleListener.onFailure((HttpResponse) msg.obj); break;
				case MESSAGE_EXCEPTION: httpResponseHandleListener.onException(context, (Throwable) msg.obj); break;
				case MESSAGE_END: httpResponseHandleListener.onEnd(); break;
			}
		}
	}

	public interface StringHttpResponseHandleListener{
		public void onStart();
		public void onSuccess(String responseContent);
		public void onFailure(HttpResponse httpResponse);
		public void onException(Context context, Throwable e);
		public void onEnd();
	}
}