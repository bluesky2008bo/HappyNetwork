package me.xiaopan.networkeasy;

import java.lang.reflect.Type;

import me.xiaopan.networkeasy.headers.ContentType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonHttpResponseHandler extends HttpResponseHandler {
	private static final String DEFAULT_RESPONSE_BODY_KEY = "weatherinfo";
    private static Gson gson;
	private Context context;
	private JsonHttpResponseHandleListener<?> jsonHttpResponseHandleListener;
	private Class<?> responseClass;
	private Type responseType;
	
	public JsonHttpResponseHandler(Context context, Class<?> responseClass, JsonHttpResponseHandleListener<?> jsonHttpResponseHandleListener){
		this.context = context;
		this.responseClass = responseClass;
		this.jsonHttpResponseHandleListener = jsonHttpResponseHandleListener;
	}
	
	public JsonHttpResponseHandler(Context context, Type responseType, JsonHttpResponseHandleListener<?> jsonHttpResponseHandleListener){
		this.context = context;
		this.responseType = responseType;
		this.jsonHttpResponseHandleListener = jsonHttpResponseHandleListener;
	}
	
	@Override
	public void onStart() {
		sendEmptyMessage(MESSAGE_START);
	}

	@Override
	public void onHandleResponse(HttpResponse httpResponse) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() < 300 ){
			/* 初始化编码方式 */
			String charset = "UTF-8";
			ContentType contentType = HttpHeaderUtils.getContentType(httpResponse);
			if(contentType != null){
				charset = contentType.getCharset(charset);
			}
			
			/* 转换成字符串 */
			String jsonString = null;
			HttpEntity httpEntity = httpResponse.getEntity();
			if(httpEntity != null){
				jsonString = EntityUtils.toString(new BufferedHttpEntity(httpEntity), charset);
			}
			
			//将JSON字符串转换成对象
			Object result = null;
			if(jsonString != null && !"".equals(jsonString)){
				JSONObject responseJsonObject = new JSONObject(jsonString);
				if(gson == null){
					gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				}
				if(responseClass != null){
					result = gson.fromJson(responseJsonObject.getString(responseClass.getAnnotation(ResponseKey.class) != null?responseClass.getAnnotation(ResponseKey.class).value():DEFAULT_RESPONSE_BODY_KEY), responseClass);
				}else if(responseType != null){
					result = gson.fromJson(responseJsonObject.getString(DEFAULT_RESPONSE_BODY_KEY), responseType);
				}
			}
			sendMessage(obtainMessage(MESSAGE_SUCCESS, result));
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
		if(jsonHttpResponseHandleListener != null){
			switch(msg.what) {
				case MESSAGE_START: jsonHttpResponseHandleListener.onStart(); break;
				case MESSAGE_SUCCESS: jsonHttpResponseHandleListener.success(msg.obj); break;
				case MESSAGE_FAILURE: jsonHttpResponseHandleListener.onFailure((HttpResponse) msg.obj); break;
				case MESSAGE_EXCEPTION: jsonHttpResponseHandleListener.onException(context, (Throwable) msg.obj); break;
				case MESSAGE_END: jsonHttpResponseHandleListener.onEnd(); break;
			}
		}
	}
	
	public static abstract class JsonHttpResponseHandleListener<T>{
		public T ob;
		public abstract void onStart();
		public abstract void onSuccess(T responseObject);
		public abstract void onFailure(HttpResponse httpResponse);
		public abstract void onException(Context context, Throwable e);
		public abstract void onEnd();
		
		@SuppressWarnings("unchecked")
		public void success(Object object){
			onSuccess((T)object);
		}
	}
}