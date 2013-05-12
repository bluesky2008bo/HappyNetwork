package me.xiaopan.networkeasy;

import org.apache.http.HttpResponse;

import android.os.Handler;

/**
 * Http响应处理器
 * @author xiaopan
 */
public abstract class HttpResponseHandler extends Handler{
	protected static final int MESSAGE_START = 0;
    protected static final int MESSAGE_SUCCESS = 1;
    protected static final int MESSAGE_FAILURE = 2;
    protected static final int MESSAGE_EXCEPTION = 3;
    protected static final int MESSAGE_END = 4;
	
	/**
     * 当开始
     */
    public abstract  void onStart();

    /**
     * 当需要处理响应
     * @param httpResponse Http响应
     * @throws Throwable 当发生异常时会进入onException()方法
     */
    public abstract  void onHandleResponse(HttpResponse httpResponse) throws Throwable;
    
    /**
     * 当发生异常
     * @param e
     */
    public abstract  void onException(Throwable e);
    
    /**
     * 当结束
     */
    public abstract  void onEnd();
}