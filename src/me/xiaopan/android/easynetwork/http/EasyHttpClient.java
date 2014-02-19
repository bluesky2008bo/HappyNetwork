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
package me.xiaopan.android.easynetwork.http;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;

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
	private Configuration configuration;	//配置
    private Map<Context, List<WeakReference<Future<?>>>> requestMap;	//请求Map
	
	/**
	 * 实例持有器
	 */
	private static class EasyHttpClientInstanceHolder{
		private static EasyHttpClient instance = new EasyHttpClient();
	}
	
	/**
	 * 获取实例
	 * @return 实例
	 */
	public static EasyHttpClient getInstance(){
		return EasyHttpClientInstanceHolder.instance;
	}

    /**
     * 执行请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param name 请求名称，在后台输出log的时候会输出此名称方便区分请求
     * @param httpRequest http请求对象
     * @param cacheId 缓存ID
     * @param responseCache 响应缓存配置
     * @param httpResponseHandler Http响应处理器
     */
    public void execute(Context context, String name, HttpUriRequest httpRequest, String cacheId, ResponseCache responseCache, HttpResponseHandler httpResponseHandler) {
        Future<?> request = getConfiguration().getThreadPool().submit(new HttpRequestRunnable(context, this, name, httpRequest, cacheId, responseCache, httpResponseHandler));
        if(context != null) {
            List<WeakReference<Future<?>>> requestList = getRequestMap().get(context);
            if(requestList == null) {
                requestList = new LinkedList<WeakReference<Future<?>>>();
                getRequestMap().put(context, requestList);
            }
            requestList.add(new WeakReference<Future<?>>(request));
        }
    }

    /**
     * 执行请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param httpRequest http请求对象
     * @param httpResponseHandler Http响应处理器
     */
    public void execute(Context context, HttpUriRequest httpRequest, HttpResponseHandler httpResponseHandler) {
        execute(context, null, httpRequest, null, null, httpResponseHandler);
    }

    /**
     * 执行请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param request 请求对象，将通过请求对象来解析出一个Http请求
     * @param httpResponseHandler http响应处理器
     */
    public void execute(Context context, Request request, HttpResponseHandler httpResponseHandler){
        if(request != null){
            /* 解析请求方式 */
            MethodType methodType = MethodType.GET;
            Method method = request.getClass().getAnnotation(Method.class);
            if(method != null){
                methodType = method.value();
            }

            //根据不同的请求方式选择不同的方法执行
            if(methodType == MethodType.GET){
                get(context, new HttpGetRequest.Builder(request).create(), httpResponseHandler);
            }else if(methodType == MethodType.POST){
                post(context, new HttpPostRequest.Builder(request).create(), httpResponseHandler);
            }else if(methodType == MethodType.PUT){
                put(context, new HttpPutRequest.Builder(request).create(), httpResponseHandler);
            }else if(methodType == MethodType.DELETE){
                delete(context, new HttpDeleteRequest.Builder(request).create(), httpResponseHandler);
            }
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("request 不能为null");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
                httpResponseHandler.exception(getConfiguration().getHandler(), illegalArgumentException, false);
            }
        }
    }
	
    /**
     * 执行一个Get请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param httpRequest Http Get请求
     * @param httpResponseHandler Http响应处理器
     */
    public void get(Context context, HttpGetRequest httpRequest, HttpResponseHandler httpResponseHandler) {
        if(GeneralUtils.isNotEmpty(httpRequest.getBaseUrl())){
            HttpGet httGet = new HttpGet(HttpUtils.getUrlByParams(httpRequest.getBaseUrl(), httpRequest.getParams()));
            HttpUtils.appendHeaders(httGet, httpRequest.getHeaders());
            execute(context, httpRequest.getName(), httGet, GeneralUtils.getCacheId(httpRequest.getResponseCache(), httpRequest.getBaseUrl(), httpRequest.getParams(), httpRequest.getCacheIgnoreParams()), httpRequest.getResponseCache(), httpResponseHandler);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("url不能为空");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
            	httpResponseHandler.exception(getConfiguration().getHandler(), illegalArgumentException, false);
            }
        }
    }

    /**
     * 执行一个Get请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void get(Context context, String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
        get(context, new HttpGetRequest.Builder(url).setParams(params).create(), httpResponseHandler);
    }

    /**
     * 执行一个Get请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     */
    public void get(Context context, String url, HttpResponseHandler httpResponseHandler) {
        get(context, new HttpGetRequest.Builder(url).create(), httpResponseHandler);
    }
    
    /**
     * 执行一个Post请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param httpRequest Http Post请求
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, HttpPostRequest httpRequest, HttpResponseHandler httpResponseHandler){
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

            execute(context, httpRequest.getName(), httPost, GeneralUtils.getCacheId(httpRequest.getResponseCache(), httpRequest.getBaseUrl(), httpRequest.getParams(), httpRequest.getCacheIgnoreParams()), httpRequest.getResponseCache(), httpResponseHandler);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("url不能为空");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
            	httpResponseHandler.exception(getConfiguration().getHandler(), illegalArgumentException, false);
            }
        }
    }

    /**
     * 执行一个Post请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
    	post(context, new HttpPostRequest.Builder(url).setParams(params).create(), httpResponseHandler);
    }

    /**
     * 执行一个Post请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, HttpResponseHandler httpResponseHandler) {
    	post(context, new HttpPostRequest.Builder(url).create(), httpResponseHandler);
    }

    /**
     * 执行一个Put请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param httpRequest Http Put请求
     * @param httpResponseHandler Http响应处理器
     */
    public void put(Context context, HttpPutRequest httpRequest, HttpResponseHandler httpResponseHandler){
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

            execute(context, httpRequest.getName(), httPut, GeneralUtils.getCacheId(httpRequest.getResponseCache(), httpRequest.getBaseUrl(), httpRequest.getParams(), httpRequest.getCacheIgnoreParams()), httpRequest.getResponseCache(), httpResponseHandler);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("url不能为空");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
            	httpResponseHandler.exception(getConfiguration().getHandler(), illegalArgumentException, false);
            }
        }
    }

    /**
     * 执行一个Put请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void put(Context context, String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
    	put(context, new HttpPutRequest.Builder(url).setParams(params).create(), httpResponseHandler);
    }

    /**
     * 执行一个Put请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     */
    public void put(Context context, String url, HttpResponseHandler httpResponseHandler) {
    	put(context, new HttpPutRequest.Builder(url).create(), httpResponseHandler);
    }

    /**
     * 执行一个Delete请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param httpRequest Http Delete请求
     * @param httpResponseHandler Http响应处理器
     */
    public void delete(Context context, HttpDeleteRequest httpRequest, HttpResponseHandler httpResponseHandler) {
        if(GeneralUtils.isNotEmpty(httpRequest.getBaseUrl())){
            HttpDelete httDelete = new HttpDelete(httpRequest.getBaseUrl());
            HttpUtils.appendHeaders(httDelete, httpRequest.getHeaders());
            execute(context, httpRequest.getName(), httDelete, null, null, httpResponseHandler);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("你必须指定url。你有两种方式来指定url，一是使用HttpGetRequest.Builder.setUrl()，而是在Request上使有Url注解或者Host加Path注解");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
            	httpResponseHandler.exception(getConfiguration().getHandler(), illegalArgumentException, false);
            }
        }
    }

    /**
     * 执行一个Delete请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     */
    public void delete(Context context, String url, HttpResponseHandler httpResponseHandler) {
        delete(context, new HttpDeleteRequest.Builder(url).create(), httpResponseHandler);
    }

    /**
     * 取消所有的请求，请求如果尚未开始就不再执行，如果已经开始就尝试中断
     * <br>你可以在Activity Destory的时候调用此方法来抛弃跟当前Activity相关的所有请求
     * @param context 上下文
     * @param mayInterruptIfRunning 如果有请求正在运行中的话是否尝试中断
     */
    public void cancelRequests(Context context, boolean mayInterruptIfRunning) {
        List<WeakReference<Future<?>>> requestList = getRequestMap().get(context);
        if(requestList != null) {
            for(WeakReference<Future<?>> requestRef : requestList) {
                Future<?> requestFuture = requestRef.get();
                if(requestFuture != null) {
                    requestFuture.cancel(mayInterruptIfRunning);
                }
            }
        }
        getRequestMap().remove(context);
    }

    /**
     * 获取配置
     * @return 配置
     */
	public Configuration getConfiguration() {
		if(configuration == null){
            configuration = new Configuration.Builder().create();
        }
        return configuration;
	}

	/**
	 * 设置配置
	 * @param configuration 配置
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	private Map<Context, List<WeakReference<Future<?>>>> getRequestMap() {
		if(requestMap == null){
			requestMap = new WeakHashMap<Context, List<WeakReference<Future<?>>>>();
		}
		return requestMap;
	}
}