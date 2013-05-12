package me.xiaopan.networkeasy;

import org.apache.http.HttpResponse;

/**
 * Http响应处理器
 * @author xiaopan
 */
public interface HttpResponseHandler {
    /**
     * 当开始
     */
    public void onStart();

    /**
     * 当需要处理响应
     * @param httpResponse Http响应
     * @throws Throwable 当发生异常时会进入onException()方法
     */
    public void onHandleResponse(HttpResponse httpResponse) throws Throwable;
    
    /**
     * 当发生异常
     * @param e
     */
    public void onException(Throwable e);
}