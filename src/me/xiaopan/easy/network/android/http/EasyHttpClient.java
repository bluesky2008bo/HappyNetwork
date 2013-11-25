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

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;

import me.xiaopan.easy.java.util.StringUtils;
import me.xiaopan.easy.network.android.EasyNetwork;
import me.xiaopan.easy.network.android.http.interceptor.AddRequestHeaderRequestInterceptor;
import me.xiaopan.easy.network.android.http.interceptor.GzipProcessRequestInterceptor;
import me.xiaopan.easy.network.android.http.interceptor.GzipProcessResponseInterceptor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

import android.content.Context;
import android.util.Log;

/**
 * Http客户端，所有的Http操作都将由此类来异步完成，同时此类提供一个单例模式来方便直接使用
 */
public class EasyHttpClient {
	private boolean debugMode;
	private Configuration configuration;	//配置
	private HttpContext httpContext;	//Http上下文
	private DefaultHttpClient httpClient;	//Http客户端
    private Map<Context, List<WeakReference<Future<?>>>> requestMap;	//请求Map
	
	public EasyHttpClient(){
		configuration = new Configuration(this);
		httpContext = new SyncBasicHttpContext(new BasicHttpContext());
		requestMap = new WeakHashMap<Context, List<WeakReference<Future<?>>>>();
		
		/* 初始化HttpClient */
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
        httpClient.addRequestInterceptor(new GzipProcessRequestInterceptor());
        httpClient.addRequestInterceptor(new AddRequestHeaderRequestInterceptor(configuration.getHeaderMap()));
        httpClient.addResponseInterceptor(new GzipProcessResponseInterceptor());
        configuration.setConnectionTimeout(20000);
        configuration.setMaxConnections(10);
        configuration.setSocketBufferSize(8192);
        configuration.setMaxRetries(5);
	}
	
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
	public static final EasyHttpClient getInstance(){
		return EasyHttpClientInstanceHolder.instance;
	}

    /**
     * 执行请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param httpRequest http请求对象
     * @param responseCache 响应缓存配置
     * @param httpResponseHandler Http响应处理器
     */
    public void execute(Context context, HttpUriRequest httpRequest, ResponseCache responseCache, HttpResponseHandler httpResponseHandler) {
        Future<?> request = EasyNetwork.getThreadPool().submit(new HttpRequestRunnable(context, this, httpRequest, responseCache, httpResponseHandler));
        if(context != null) {
            List<WeakReference<Future<?>>> requestList = requestMap.get(context);
            if(requestList == null) {
                requestList = new LinkedList<WeakReference<Future<?>>>();
                requestMap.put(context, requestList);
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
        execute(context, httpRequest, null, httpResponseHandler);
    }

    /**
     * 执行请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param request 请求对象，将通过请求对象来解析出一个Http请求
     * @param httpResponseHandler
     */
    public void execute(Context context, Request request, HttpResponseHandler httpResponseHandler){
        if(request != null){
            /* 解析请求方式 */
            MethodType methodType = null;
            Method method = request.getClass().getAnnotation(Method.class);
            if(method != null){
                methodType = method.value();
            }else{
                methodType = MethodType.GET;
            }

            //根据不同的请求方式选择不同的方法执行
            if(methodType == MethodType.GET){
                get(context, new HttpGetRequest.Builder(request).create(), httpResponseHandler);
            }else if(methodType == MethodType.POST){
                post(context, new HttpPostRequest.Builder(request).create(), httpResponseHandler);
            }else if(methodType == MethodType.PUT){
                put(context, new HttpPutRequest.Builder(request).create(), httpResponseHandler);
            }else if(methodType == MethodType.DELETE){
                delete(context, new HttpDeleteRequest.Builder(request.getClass()).create(), httpResponseHandler);
            }
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("request 不能为null");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
                httpResponseHandler.exception(illegalArgumentException);
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
        if(StringUtils.isNotEmpty(httpRequest.getUrl())){
            HttpGet httGet = new HttpGet(HttpUtils.getUrlByParams(httpRequest.getUrl(), httpRequest.getParams()));
            HttpUtils.appendHeaders(httGet, httpRequest.getHeaders());
            execute(context, httGet, httpRequest.getResponseCache(), httpResponseHandler);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("url不能为空");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
                httpResponseHandler.exception(illegalArgumentException);
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
        if(StringUtils.isNotEmpty(httpRequest.getUrl())){
            HttpPost httPost = new HttpPost(httpRequest.getUrl());
            HttpUtils.appendHeaders(httPost, httpRequest.getHeaders());

            HttpEntity httpEntity = httpRequest.getHttpEntity();
            if(httpEntity == null && httpRequest.getParams() != null){
                httpEntity = httpRequest.getParams().getEntity();
            }
            if(httpEntity != null){
                httPost.setEntity(httpEntity);
            }

            execute(context, httPost, httpRequest.getResponseCache(), httpResponseHandler);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("url不能为空");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
                httpResponseHandler.exception(illegalArgumentException);
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
        if(StringUtils.isNotEmpty(httpRequest.getUrl())){
            HttpPut httPost = new HttpPut(httpRequest.getUrl());
            HttpUtils.appendHeaders(httPost, httpRequest.getHeaders());

            HttpEntity httpEntity = httpRequest.getHttpEntity();
            if(httpEntity == null && httpRequest.getParams() != null){
                httpEntity = httpRequest.getParams().getEntity();
            }
            if(httpEntity != null){
                httPost.setEntity(httpEntity);
            }

            execute(context, httPost, httpRequest.getResponseCache(), httpResponseHandler);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("url不能为空");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
                httpResponseHandler.exception(illegalArgumentException);
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
        if(StringUtils.isNotEmpty(httpRequest.getUrl())){
            HttpDelete httGet = new HttpDelete(httpRequest.getUrl());
            HttpUtils.appendHeaders(httGet, httpRequest.getHeaders());
            execute(context, httGet, httpRequest.getResponseCache(), httpResponseHandler);
        }else{
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("你必须指定url。你有两种方式来指定url，一是使用HttpGetRequest.Builder.setUrl()，而是在Request上使有Url注解或者Host加Path注解");
            illegalArgumentException.printStackTrace();
            if(httpResponseHandler != null){
                httpResponseHandler.exception(illegalArgumentException);
            }
        }
    }

    /**
     * 取消所有的请求，请求如果尚未开始就不再执行，如果已经开始就尝试中断
     * <br>你可以在Activity Destory的时候调用此方法来抛弃跟当前Activity相关的所有请求
     * @param context 上下文
     * @param mayInterruptIfRunning 如果有请求正在运行中的话是否尝试中断
     */
    public void cancelRequests(Context context, boolean mayInterruptIfRunning) {
        List<WeakReference<Future<?>>> requestList = requestMap.get(context);
        if(requestList != null) {
            for(WeakReference<Future<?>> requestRef : requestList) {
                Future<?> requestFuture = requestRef.get();
                if(requestFuture != null) {
                    requestFuture.cancel(mayInterruptIfRunning);
                }
            }
        }
        requestMap.remove(context);
    }
    
    /**
     * 获取Http客户端
     * @return
     */
    public DefaultHttpClient getHttpClient() {
		return httpClient;
	}
	
	/**
	 * 获取Http上下文
	 * @return
	 */
	public HttpContext getHttpContext() {
		return httpContext;
	}

	/**
	 * 输出LOG
	 * @param logContent LOG内容
	 */
	public void log(String logContent){
		if(debugMode){
			Log.d(configuration.getLogTag(), logContent);
		}
	}

    /**
     * 获取配置
     * @return
     */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * 判断是否开启调试模式
	 * @return 
	 */
	public boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * 设置是否开启调试模式，开启调试模式后会在控制台输出LOG
	 * @param debugMode 
	 */
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
}