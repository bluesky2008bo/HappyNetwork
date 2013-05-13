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
     * 发送开始消息
     */
    public abstract  void sendStartMessage();

    /**
     * 发送处理响应消息
     * @param httpResponse Http响应
     * @throws Throwable 当发生异常时会进入onException()方法
     */
    public abstract  void sendHandleResponseMessage(HttpResponse httpResponse) throws Throwable;
    
    /**
     * 发送异常消息
     * @param e
     */
    public abstract  void sendExceptionMessage(Throwable e);
    
    /**
     * 发送结束消息
     */
    public abstract  void sendEndMessage();
}