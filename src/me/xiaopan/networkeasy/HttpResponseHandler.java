package me.xiaopan.networkeasy;

import org.apache.http.HttpResponse;

/**
 * Http响应处理器
 * @author xiaopan
 */
public interface HttpResponseHandler{
	/**
     * 发送开始消息
     */
    public  void sendStartMessage();

    /**
     * 发送处理响应消息
     * @param httpResponse Http响应
     * @throws Throwable 当发生异常时会进入onException()方法
     */
    public  void sendHandleResponseMessage(HttpResponse httpResponse) throws Throwable;
    
    /**
     * 发送异常消息
     * @param e
     */
    public  void sendExceptionMessage(Throwable e);
    
    /**
     * 发送结束消息
     */
    public  void sendEndMessage();
}