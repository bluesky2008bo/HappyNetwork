/*
 * Copyright 2013 Peng fei Pan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.xiaopan.easy.network.android.http;

import org.apache.http.HttpResponse;

/**
 * 响应处理器
 */
public abstract class HttpResponseHandler{
	/**
     * 开始
     */
    public abstract void start();
    
    /**
     * 判断是否可以缓存，当需要缓存HttpResponse的时候会先调用此方法判断是否可以缓存，例如：实现者可以再次方法里过滤掉状态码不是200的响应。默认为主要是状态码大于等于200并且 小于300就返回true
     * @param httpResponse
     * @return
     */
    public boolean isCanCache(HttpResponse httpResponse){
    	return httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300;
    }

    /**
     * 处理响应
     * @param httpResponse Http响应
     * @param isCache 是否是缓存数据
     * @param isRefreshCacheAndCallback 是否还要刷新本地缓存并回调
     * @throws Throwable 当发生异常时会进入exception()方法
     */
    public abstract void handleResponse(HttpResponse httpResponse, boolean isCache, boolean isRefreshCacheAndCallback) throws Throwable;

    /**
     * 异常
     * @param e
     */
    public abstract void exception(Throwable e);
}
