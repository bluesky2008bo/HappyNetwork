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
	protected static final int MESSAGE_START = 2;
    protected static final int MESSAGE_SUCCESS = 0;
    protected static final int MESSAGE_EXCEPTION = 1;
	
    private Context context;
	private StringHttpResponseHandleListener stringHttpResponseHandleListener;
	private Throwable throwable;
	private String responseContent;
	
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
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		sendEmptyMessage(MESSAGE_SUCCESS);
	}

	@Override
	public void onException(Throwable e) {
		throwable = e;
		sendEmptyMessage(MESSAGE_EXCEPTION);
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
			case MESSAGE_START:
				if(stringHttpResponseHandleListener != null){
					stringHttpResponseHandleListener.onStart();
				}
				break;
	        case MESSAGE_SUCCESS:
	        	if(stringHttpResponseHandleListener != null){
	        		stringHttpResponseHandleListener.onSuccess(responseContent);
	        	}
	            break;
	        case MESSAGE_EXCEPTION:
	        	if(stringHttpResponseHandleListener != null){
	        		stringHttpResponseHandleListener.onException(context, throwable);
	        	}
	            break;
		}
	}

	public interface StringHttpResponseHandleListener{
		public void onStart();
		public void onSuccess(String responseContent);
		public void onException(Context context, Throwable e);
	}
}