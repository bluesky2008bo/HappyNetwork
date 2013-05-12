package me.xiaopan.networkeasy;

import java.io.IOException;

import me.xiaopan.networkeasy.headers.ContentType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class StringHttpResponseHandler extends Handler implements HttpResponseHandler {
	protected static final int MESSAGE_START = 0;
    protected static final int MESSAGE_SUCCESS = 1;
    protected static final int MESSAGE_FAILURE = 2;
    protected static final int MESSAGE_EXCEPTION = 3;
    protected static final int MESSAGE_END = 4;
	
    private Context context;
	private StringHttpResponseHandleListener stringHttpResponseHandleListener;
	private Throwable throwable;
	private String responseContent;
	private HttpResponse httpResponse;
	
	public StringHttpResponseHandler(Context context, StringHttpResponseHandleListener stringHttpResponseHandleListener){
		this.context = context;
		this.stringHttpResponseHandleListener = stringHttpResponseHandleListener;
	}
	
	@Override
	public void onStart() {
		sendEmptyMessage(MESSAGE_START);
	}

	@Override
	public void onHandleResponse(HttpResponse httpResponse) throws ParseException, IOException {
		if(httpResponse.getStatusLine().getStatusCode() < 300 ){
			/* 初始化编码方式 */
			String charset = "UTF-8";
			ContentType contentType = HttpHeaderUtils.getContentType(httpResponse);
			if(contentType != null){
				charset = contentType.getCharset(charset);
			}
			
			/* 转换成字符串 */
			HttpEntity httpEntity = httpResponse.getEntity();
			if(httpEntity != null){
				responseContent = EntityUtils.toString(new BufferedHttpEntity(httpEntity), charset);
			}
			
			sendEmptyMessage(MESSAGE_SUCCESS);
		}else{
			this.httpResponse = httpResponse;
			sendEmptyMessage(MESSAGE_FAILURE);
		}
	}

	@Override
	public void onException(Throwable e) {
		throwable = e;
		sendEmptyMessage(MESSAGE_EXCEPTION);
	}

	@Override
	public void onEnd() {
		sendEmptyMessage(MESSAGE_END);
	}
	
	@Override
	public void handleMessage(Message msg) {
		if(stringHttpResponseHandleListener != null){
			switch(msg.what) {
				case MESSAGE_START: stringHttpResponseHandleListener.onStart(); break;
				case MESSAGE_SUCCESS: stringHttpResponseHandleListener.onSuccess(responseContent); break;
				case MESSAGE_FAILURE: stringHttpResponseHandleListener.onFailure(httpResponse); break;
				case MESSAGE_EXCEPTION: stringHttpResponseHandleListener.onException(context, throwable); break;
				case MESSAGE_END: stringHttpResponseHandleListener.onEnd(); break;
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