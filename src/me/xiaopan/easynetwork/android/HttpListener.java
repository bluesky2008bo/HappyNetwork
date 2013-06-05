package me.xiaopan.easynetwork.android;

import org.apache.http.HttpResponse;

/**
 * Http访问监听器
 */
public interface HttpListener{
	/**
     * 开始
     */
    public  void start();

    /**
     * 处理响应
     * @param httpResponse Http响应
     * @throws Throwable 当发生异常时会进入exception()方法
     */
    public  void handleResponse(HttpResponse httpResponse) throws Throwable;
    
    /**
     * 异常
     * @param e
     */
    public  void exception(Throwable e);
    
    /**
     * 结束
     */
    public  void end();
}