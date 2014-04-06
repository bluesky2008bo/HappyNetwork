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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import me.xiaopan.android.easynetwork.http.annotation.Method;
import me.xiaopan.android.easynetwork.http.enums.MethodType;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;
import android.util.Log;

/**
 * Http客户端，所有的Http操作都将由此类来异步完成，同时此类提供一个单例模式来方便直接使用
 */
public class EasyHttpClient {
	private static EasyHttpClient instance;
	private Configuration configuration;	//配置
    private Map<Context, List<RequestHandle>> requestMap;	//请求Map
	
    public EasyHttpClient(Context context){
    	configuration = new Configuration(context);
    	requestMap = new WeakHashMap<Context, List<RequestHandle>>();
    }
    
	/**
	 * 获取实例
	 * @return 实例
	 */
	public static EasyHttpClient getInstance(Context context){
		if(instance == null){
			instance = new EasyHttpClient(context);
		}
		return instance;
	}

    /**
     * 执行请求
     * @param httpRequest http请求对象
     * @param name 请求名称，在后台输出log的时候会输出此名称方便区分请求
     * @param responseCache 响应缓存配置
     * @param httpResponseHandler Http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @return 
     */
    public RequestHandle execute(HttpUriRequest httpRequest, String name, ResponseCache responseCache, HttpResponseHandler httpResponseHandler, Context context) {
    	HttpRequestExecuteRunnable httpRequestRunnable = new HttpRequestExecuteRunnable(this, name, httpRequest, responseCache, httpResponseHandler);
    	getConfiguration().getExecutorService().submit(httpRequestRunnable);
        RequestHandle requestHandle = new RequestHandle(httpRequestRunnable);
        if(context != null) {
            List<RequestHandle> requestList = requestMap.get(context);
            if(requestList == null) {
                requestList = new LinkedList<RequestHandle>();
                requestMap.put(context, requestList);
            }
            requestList.add(requestHandle);
        }
        return requestHandle;
    }

    /**
     * 执行请求
     * @param httpRequest http请求对象
     * @param name 请求名称，在后台输出log的时候会输出此名称方便区分请求
     * @param responseCache 响应缓存配置
     * @param httpResponseHandler Http响应处理器
     * @return 
     */
    public RequestHandle execute(HttpUriRequest httpRequest, String name, ResponseCache responseCache, HttpResponseHandler httpResponseHandler) {
        return execute(httpRequest, name, responseCache, httpResponseHandler, null);
    }

    /**
     * 执行请求
     * @param httpRequest http请求对象
     * @param httpResponseHandler Http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @return 
     */
    public RequestHandle execute(HttpUriRequest httpRequest, HttpResponseHandler httpResponseHandler, Context context) {
    	return execute(httpRequest, null, null, httpResponseHandler, context);
    }

    /**
     * 执行请求
     * @param httpRequest http请求对象
     * @param name 请求名称，在后台输出log的时候会输出此名称方便区分请求
     * @param responseCache 响应缓存配置
     * @param httpResponseHandler Http响应处理器
     * @return 
     */
    public RequestHandle execute(HttpUriRequest httpRequest, HttpResponseHandler httpResponseHandler) {
        return execute(httpRequest, null, null, httpResponseHandler, null);
    }

    /**
     * 执行请求
     * @param request 请求对象，将通过请求对象来解析出一个Http请求
     * @param httpResponseHandler http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @return
     */
    public RequestHandle execute(Request request, HttpResponseHandler httpResponseHandler, Context context){
        if(request != null){
            /* 解析请求方式 */
            MethodType methodType = MethodType.GET;
            Method method = request.getClass().getAnnotation(Method.class);
            if(method != null){
                methodType = method.value();
            }

            //根据不同的请求方式选择不同的方法执行
            if(methodType == MethodType.GET){
            	 return get(new HttpGetRequest.Builder(configuration.getContext(), request).create(), httpResponseHandler, context);
            }else if(methodType == MethodType.POST){
            	 return post(new HttpPostRequest.Builder(configuration.getContext(), request).create(), httpResponseHandler, context);
            }else if(methodType == MethodType.PUT){
            	 return put(new HttpPutRequest.Builder(configuration.getContext(), request).create(), httpResponseHandler, context);
            }else if(methodType == MethodType.DELETE){
            	 return delete(new HttpDeleteRequest.Builder(configuration.getContext(), request).create(), httpResponseHandler, context);
            }else{
            	 return null;
            }
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("request 不能为null");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
                httpResponseHandler.onException(getConfiguration().getHandler(), illegalArgumentException, false);
            }
            return null;
        }
    }

    /**
     * 执行请求
     * @param request 请求对象，将通过请求对象来解析出一个Http请求
     * @param httpResponseHandler http响应处理器
     * @return
     */
    public RequestHandle execute(Request request, HttpResponseHandler httpResponseHandler){
        return execute(request, httpResponseHandler, null);
    }
	
    /**
     * 执行一个Get请求
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @param httpRequest Http Get请求
     * @param httpResponseHandler Http响应处理器
     * @return
     */
    public RequestHandle get(HttpGetRequest httpRequest, HttpResponseHandler httpResponseHandler, Context context) {
        if(GeneralUtils.isNotEmpty(httpRequest.getBaseUrl())){
            HttpGet httGet = new HttpGet(HttpUtils.getUrlByParams(getConfiguration().isUrlEncodingEnabled(), httpRequest.getBaseUrl(), httpRequest.getParams()));
            HttpUtils.appendHeaders(httGet, httpRequest.getHeaders());
            if(httpRequest.getResponseCache() != null && GeneralUtils.isEmpty(httpRequest.getResponseCache().getId())){
            	httpRequest.getResponseCache().setId(GeneralUtils.createCacheId(httpRequest.getResponseCache(), httpRequest.getBaseUrl(), httpRequest.getParams(), httpRequest.getCacheIgnoreParams()));
            }
            return execute(httGet, httpRequest.getName(), httpRequest.getResponseCache(), httpResponseHandler, context);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("url不能为空");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
            	httpResponseHandler.onException(getConfiguration().getHandler(), illegalArgumentException, false);
            }
            return null;
        }
    }
	
    /**
     * 执行一个Get请求
     * @param httpRequest Http Get请求
     * @param httpResponseHandler Http响应处理器
     * @return
     */
    public RequestHandle get(HttpGetRequest httpRequest, HttpResponseHandler httpResponseHandler) {
        return get(httpRequest, httpResponseHandler, null);
    }

    /**
     * 执行一个Get请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @return
     */
    public RequestHandle get(String url, RequestParams params, HttpResponseHandler httpResponseHandler, Context context) {
    	 return get(new HttpGetRequest.Builder(url).setParams(params).create(), httpResponseHandler, context);
    }

    /**
     * 执行一个Get请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     * @return
     */
    public RequestHandle get(String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
    	 return get(new HttpGetRequest.Builder(url).setParams(params).create(), httpResponseHandler, null);
    }

    /**
     * 执行一个Get请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     */
    public RequestHandle get(String url, HttpResponseHandler httpResponseHandler, Context context) {
    	 return get(new HttpGetRequest.Builder(url).create(), httpResponseHandler, context);
    }

    /**
     * 执行一个Get请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     */
    public RequestHandle get(String url, HttpResponseHandler httpResponseHandler) {
    	 return get(new HttpGetRequest.Builder(url).create(), httpResponseHandler, null);
    }
    
    /**
     * 执行一个Post请求
     * @param httpRequest Http Post请求
     * @param httpResponseHandler Http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @return
     */
    public RequestHandle post(HttpPostRequest httpRequest, HttpResponseHandler httpResponseHandler, Context context){
        if(GeneralUtils.isNotEmpty(httpRequest.getBaseUrl())){
            HttpPost httPost = new HttpPost(httpRequest.getBaseUrl());
            HttpUtils.appendHeaders(httPost, httpRequest.getHeaders());

            HttpEntity httpEntity = httpRequest.getHttpEntity();
            if(httpEntity == null && httpRequest.getParams() != null){
                if(getConfiguration().isDebugMode()){
                    Log.d(getConfiguration().getLogTag(), httpRequest.getName() + " 请求实体：" + httpRequest.getParams().toString());
                }
                httpEntity = httpRequest.getParams().getEntity();
            }
            if(httpEntity != null){
                httPost.setEntity(httpEntity);
            }
            if(httpRequest.getResponseCache() != null && GeneralUtils.isEmpty(httpRequest.getResponseCache().getId())){
            	httpRequest.getResponseCache().setId(GeneralUtils.createCacheId(httpRequest.getResponseCache(), httpRequest.getBaseUrl(), httpRequest.getParams(), httpRequest.getCacheIgnoreParams()));
            }
            return execute(httPost, httpRequest.getName(), httpRequest.getResponseCache(), httpResponseHandler, context);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("url不能为空");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
            	httpResponseHandler.onException(getConfiguration().getHandler(), illegalArgumentException, false);
            }
            return null;
        }
    }
    
    /**
     * 执行一个Post请求
     * @param httpRequest Http Post请求
     * @param httpResponseHandler Http响应处理器
     * @return
     */
    public RequestHandle post(HttpPostRequest httpRequest, HttpResponseHandler httpResponseHandler){
        return post(httpRequest, httpResponseHandler, null);
    }

    /**
     * 执行一个Post请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @return
     */
    public RequestHandle post(String url, RequestParams params, HttpResponseHandler httpResponseHandler, Context context) {
    	 return post(new HttpPostRequest.Builder(url).setParams(params).create(), httpResponseHandler, context);
    }

    /**
     * 执行一个Post请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     * @return
     */
    public RequestHandle post(String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
    	 return post(new HttpPostRequest.Builder(url).setParams(params).create(), httpResponseHandler, null);
    }

    /**
     * 执行一个Post请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @return
     */
    public RequestHandle post(String url, HttpResponseHandler httpResponseHandler, Context context) {
    	 return post(new HttpPostRequest.Builder(url).create(), httpResponseHandler, context);
    }

    /**
     * 执行一个Post请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @return
     */
    public RequestHandle post(String url, HttpResponseHandler httpResponseHandler) {
    	 return post(new HttpPostRequest.Builder(url).create(), httpResponseHandler, null);
    }

    /**
     * 执行一个Put请求
     * @param httpRequest Http Put请求
     * @param httpResponseHandler Http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @return
     */
    public RequestHandle put(HttpPutRequest httpRequest, HttpResponseHandler httpResponseHandler, Context context){
        if(GeneralUtils.isNotEmpty(httpRequest.getBaseUrl())){
            HttpPut httPut = new HttpPut(httpRequest.getBaseUrl());
            HttpUtils.appendHeaders(httPut, httpRequest.getHeaders());

            HttpEntity httpEntity = httpRequest.getHttpEntity();
            if(httpEntity == null && httpRequest.getParams() != null){
                if(getConfiguration().isDebugMode()){
                    Log.d(getConfiguration().getLogTag(), httpRequest.getName() + " 请求实体：" + httpRequest.getParams().toString());
                }
                httpEntity = httpRequest.getParams().getEntity();
            }
            if(httpEntity != null){
                httPut.setEntity(httpEntity);
            }
            if(httpRequest.getResponseCache() != null && GeneralUtils.isEmpty(httpRequest.getResponseCache().getId())){
            	httpRequest.getResponseCache().setId(GeneralUtils.createCacheId(httpRequest.getResponseCache(), httpRequest.getBaseUrl(), httpRequest.getParams(), httpRequest.getCacheIgnoreParams()));
            }
            return execute(httPut, httpRequest.getName(), httpRequest.getResponseCache(), httpResponseHandler, context);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("url不能为空");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
            	httpResponseHandler.onException(getConfiguration().getHandler(), illegalArgumentException, false);
            }
            return null;
        }
    }

    /**
     * 执行一个Put请求
     * @param httpRequest Http Put请求
     * @param httpResponseHandler Http响应处理器
     * @return
     */
    public RequestHandle put(HttpPutRequest httpRequest, HttpResponseHandler httpResponseHandler){
        return put(httpRequest, httpResponseHandler, null);
    }

    /**
     * 执行一个Put请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @return
     */
    public RequestHandle put(String url, RequestParams params, HttpResponseHandler httpResponseHandler, Context context) {
    	 return put(new HttpPutRequest.Builder(url).setParams(params).create(), httpResponseHandler, context);
    }

    /**
     * 执行一个Put请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     * @return
     */
    public RequestHandle put(String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
    	 return put(new HttpPutRequest.Builder(url).setParams(params).create(), httpResponseHandler, null);
    }

    /**
     * 执行一个Put请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @return
     */
    public RequestHandle put(String url, HttpResponseHandler httpResponseHandler, Context context) {
    	 return put(new HttpPutRequest.Builder(url).create(), httpResponseHandler, context);
    }

    /**
     * 执行一个Put请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @return
     */
    public RequestHandle put(String url, HttpResponseHandler httpResponseHandler) {
    	 return put(new HttpPutRequest.Builder(url).create(), httpResponseHandler, null);
    }

    /**
     * 执行一个Delete请求
     * @param httpRequest Http Delete请求
     * @param httpResponseHandler Http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @return
     */
    public RequestHandle delete(HttpDeleteRequest httpRequest, HttpResponseHandler httpResponseHandler, Context context) {
        if(GeneralUtils.isNotEmpty(httpRequest.getBaseUrl())){
            HttpDelete httDelete = new HttpDelete(HttpUtils.getUrlByParams(getConfiguration().isUrlEncodingEnabled(), httpRequest.getBaseUrl(), httpRequest.getParams()));
            HttpUtils.appendHeaders(httDelete, httpRequest.getHeaders());
            return execute(httDelete, httpRequest.getName(), null, httpResponseHandler, context);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("你必须指定url。你有两种方式来指定url，一是使用HttpGetRequest.Builder.setUrl()，而是在Request上使有Url注解或者Host加Path注解");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
            	httpResponseHandler.onException(getConfiguration().getHandler(), illegalArgumentException, false);
            }
            return null;
        }
    }

    /**
     * 执行一个Delete请求
     * @param httpRequest Http Delete请求
     * @param httpResponseHandler Http响应处理器
     * @return
     */
    public RequestHandle delete(HttpDeleteRequest httpRequest, HttpResponseHandler httpResponseHandler) {
        return delete(httpRequest, httpResponseHandler, null);
    }

    /**
     * 执行一个Delete请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @param context Android上下文，此上下文唯一的作用就是稍后你可以通过cancelRequests()方法批量取消请求
     * @return
     */
    public RequestHandle delete(String url, HttpResponseHandler httpResponseHandler, Context context) {
        return delete(new HttpDeleteRequest.Builder(url).create(), httpResponseHandler, context);
    }

    /**
     * 执行一个Delete请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     * @return
     */
    public RequestHandle delete(String url, HttpResponseHandler httpResponseHandler) {
        return delete(new HttpDeleteRequest.Builder(url).create(), httpResponseHandler, null);
    }

    /**
     * 取消所有的请求，请求如果尚未开始就不再执行，如果已经开始就尝试中断
     * <br>你可以在Activity Destory的时候调用此方法来抛弃跟当前Activity相关的所有请求
     * @param context 上下文
     * @param mayInterruptIfRunning 如果有请求正在运行中的话是否尝试中断
     */
    public void cancelRequests(Context context, boolean mayInterruptIfRunning) {
        List<RequestHandle> requestList = requestMap.get(context);
        if(requestList != null) {
            for(RequestHandle requestHandle : requestList) {
                requestHandle.cancel(mayInterruptIfRunning);
            }
        }
        requestMap.remove(context);
    }

    /**
     * 获取配置
     * @return 配置
     */
	public Configuration getConfiguration() {
		return configuration;
	}
}