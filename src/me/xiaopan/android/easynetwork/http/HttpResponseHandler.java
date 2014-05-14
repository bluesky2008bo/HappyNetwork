/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.android.easynetwork.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import android.os.Handler;

/**
 * 响应处理器
 */
public abstract class HttpResponseHandler{
	private boolean cancelled;	// 是否已经取消
	private boolean mayInterruptIfRunning;	// 当取消请求的时候是否中断正在进行的请求
	private boolean enableUpdateProgress;	// 开启更新进度功能
	private boolean synchronizationCallback;	// 同步回调
	
    public HttpResponseHandler(boolean enableUpdateProgress) {
		this.enableUpdateProgress = enableUpdateProgress;
	}
	
    public HttpResponseHandler() {
	}

    /**
     * 是否已经取消了
     */
	public boolean isCancelled() {
		return cancelled;
	}
    
	/**
	 * 如果正在运行是否中断
	 */
	public boolean isMayInterruptIfRunning() {
		return mayInterruptIfRunning;
	}

	/**
     * 取消
     * @param handler
     * @param mayInterruptIfRunning 如果正在运行是否尝试终止
     * @return
     */
    public void cancel(Handler handler, boolean mayInterruptIfRunning) {
    	this.cancelled = true;
    	this.mayInterruptIfRunning = mayInterruptIfRunning;
    	onCancel(handler);
    }
    
    /**
     * 是否开启进度更新功能
     * @return 是否开启进度更新功能
     */
	public boolean isEnableUpdateProgress() {
		return enableUpdateProgress;
	}

	/**
	 * 设置是否开启进度更新功能
	 * @param enableUpdateProgress 是否开启进度更新功能
	 */
	public void setEnableUpdateProgress(boolean enableUpdateProgress) {
		this.enableUpdateProgress = enableUpdateProgress;
	}

	/**
	 * 是否同步回调
	 * @return 是否同步回调
	 */
	public boolean isSynchronizationCallback() {
		return synchronizationCallback;
	}

	/**
	 * 设置是否同步回调
	 * @param synchronizationCallback 是否同步回调
	 */
	public void setSynchronizationCallback(boolean synchronizationCallback) {
		this.synchronizationCallback = synchronizationCallback;
	}

	/**
     * 当请求开始
     * @param handler 消息处理器
     */
    protected abstract void onStart(final Handler handler);
    
    /**
     * 判断是否可以缓存，当需要缓存HttpResponse的时候会先调用此方法判断是否可以缓存，例如：实现者可以在此方法里过滤掉状态码不是200的响应。默认为状态码大于等于200并且 小于300就返回true
     * @param handler 消息处理器
     * @param httpResponse Http响应
     * @return 是否可以缓存
     */
    protected boolean isCanCache(Handler handler, HttpResponse httpResponse){
    	return httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300;
    }

    /**
     * 处理响应，值得注意的是此方法当在开启缓存模式并且开启刷新缓存以及开启了刷新后再次回调的时候总共会回调两次，你可以通过isCache、isRefreshCacheAndCallback参数来区分并做不同的处理
     * @param handler 消息处理器
     * @param request 请求
     * @param httpResponse Http响应
     * @param isNotRefresh 本次响应不是刷新
     * @param isOver 本次执行是否是最后一次
     * @throws Throwable 当发生异常时会进入exception()方法
     */
    protected abstract void onHandleResponse(Handler handler, HttpUriRequest request, HttpResponse httpResponse, boolean isNotRefresh, boolean isOver) throws Throwable;
    
    /**
     * 更新进度
     * @param handler
     * @param totalLength
     * @param completedLength
     */
    protected abstract void onUpdateProgress(Handler handler, long totalLength, long completedLength);

    /**
     * 在请求的过程中发生异常，值得注意的是在读取本地缓存发生异常的话不会回调此方法，因为会自动改为从网络获取数据，所以一旦回调此方法就意味着整个请求已经结束了
     * @param handler 消息处理器
     * @param isNotRefresh 本次异常不是在刷新缓存数据的时候发生的
     * @param e 异常
     */
    protected abstract void onException(Handler handler, Throwable e, boolean isNotRefresh);
    
    /**
     * 当请求取消
     * @param handler 消息处理器
     */
    protected abstract void onCancel(final Handler handler);
}
