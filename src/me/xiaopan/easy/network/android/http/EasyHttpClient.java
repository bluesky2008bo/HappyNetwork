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

import org.apache.http.Header;
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
	
    
    
    /** **************************************************************************************** HTTP GET 请求 **************************************************************************************** */
    /**
     * 执行一个HTTP GET请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void get(Context context, String url, Header[] headers, RequestParams params, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setHeaders(new HttpGet(HttpUtils.getUrlWithQueryString(url, params)), headers), httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP GET请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void get(Context context, String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, new HttpGet(HttpUtils.getUrlWithQueryString(url, params)), httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP GET请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     */
    public void get(Context context, String url, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, new HttpGet(url), httpResponseHandler);
    }

    /**
     * 执行一个HTTP GET请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param httpResponseHandler Http响应处理器
     */
    public void get(Context context, String url, Header[] headers, Request request, HttpResponseHandler httpResponseHandler){
		try {
			sendRequest(context, HttpUtils.setHeaders(new HttpGet(HttpUtils.getUrlWithQueryString(HttpUtils.getUrlFromRequestObject(url, request), HttpUtils.requestToRequestParams(request))), headers), httpResponseHandler);
		} catch (Exception e) {
			if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
		}
    }

    /**
     * 执行一个HTTP GET请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param httpResponseHandler Http响应处理器
     */
    public void get(Context context, String url, Request request, HttpResponseHandler httpResponseHandler){
		try {
			sendRequest(context, new HttpGet(HttpUtils.getUrlWithQueryString(HttpUtils.getUrlFromRequestObject(url, request), HttpUtils.requestToRequestParams(request))), httpResponseHandler);
		} catch (Exception e) {
			if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
		}
    }

    /**
     * 执行一个HTTP GET请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param httpResponseHandler Http响应处理器
     */
    public void get(Context context, Request request, HttpResponseHandler httpResponseHandler){
		try {
			sendRequest(context, new HttpGet(HttpUtils.getUrlWithQueryString(HttpUtils.getUrlFromRequestObject(null, request), HttpUtils.requestToRequestParams(request))), httpResponseHandler);
		} catch (Exception e) {
			if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
		}
    }

    /** **************************************************************************************** HTTP POST 请求 **************************************************************************************** */
    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param entity 请求实体
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, Header[] headers, HttpEntity entity, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setEntity(new HttpPost(url), entity, headers), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param entity 请求实体
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, HttpEntity entity, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setEntity(new HttpPost(url), entity), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param entity 请求实体
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, HttpEntity entity, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setEntity(new HttpPost(url), entity), httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, new HttpPost(url), httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param params 请求参数
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, Header[] headers, RequestParams params, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setEntity(new HttpPost(url), HttpUtils.paramsToEntity(this, params), headers), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param params 请求参数
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, RequestParams params, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setEntity(new HttpPost(url), HttpUtils.paramsToEntity(this, params)), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setEntity(new HttpPost(url), HttpUtils.paramsToEntity(this, params)), httpResponseHandler);
    }

    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, Header[] headers, Request request, String contentType, HttpResponseHandler httpResponseHandler){
	    try {
	        sendRequest(context, HttpUtils.setEntity(new HttpPost(HttpUtils.getUrlFromRequestObject(url, request)), HttpUtils.paramsToEntity(this, HttpUtils.requestToRequestParams(request)), headers), contentType, httpResponseHandler);
	    } catch (Exception e) {
	    	if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
	    }
    }

    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, Request request, String contentType, HttpResponseHandler httpResponseHandler){
	    try {
	        sendRequest(context, HttpUtils.setEntity(new HttpPost(HttpUtils.getUrlFromRequestObject(url, request)), HttpUtils.paramsToEntity(this, HttpUtils.requestToRequestParams(request))), contentType, httpResponseHandler);
	    } catch (Exception e) {
	    	if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
	    }
    }

    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, Request request, HttpResponseHandler httpResponseHandler){
	    try {
	        sendRequest(context, HttpUtils.setEntity(new HttpPost(HttpUtils.getUrlFromRequestObject(url, request)), HttpUtils.paramsToEntity(this, HttpUtils.requestToRequestParams(request))), httpResponseHandler);
	    } catch (Exception e) {
	    	if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
	    }
    }

    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, Request request, HttpResponseHandler httpResponseHandler){
	    try {
	        sendRequest(context, HttpUtils.setEntity(new HttpPost(HttpUtils.getUrlFromRequestObject(null, request)), HttpUtils.paramsToEntity(this, HttpUtils.requestToRequestParams(request))), httpResponseHandler);
	    } catch (Exception e) {
	    	if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
	    }
    }
    
    /** **************************************************************************************** HTTP PUT 请求 **************************************************************************************** */
    /**
     * 执行一个HTTP PUT请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url the 请求地址
     * @param headers 请求头信息
     * @param entity 请求实体
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void put(Context context, String url, Header[] headers, HttpEntity entity, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setEntity(new HttpPut(url), entity, headers), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP PUT请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url the 请求地址
     * @param entity 请求实体
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void put(Context context, String url, HttpEntity entity, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setEntity(new HttpPut(url), entity), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP PUT请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url the 请求地址
     * @param entity 请求实体
     * @param httpResponseHandler Http响应处理器
     */
    public void put(Context context, String url, HttpEntity entity, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setEntity(new HttpPut(url), entity), httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP PUT请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url the 请求地址
     * @param entity 请求实体
     * @param httpResponseHandler Http响应处理器
     */
    public void put(Context context, String url, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, new HttpPut(url), httpResponseHandler);
    }

    /**
     * 执行一个HTTP PUT请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param params 请求参数
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void put(Context context, String url, Header[] headers, RequestParams params, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setEntity(new HttpPut(url), HttpUtils.paramsToEntity(this, params), headers), contentType, httpResponseHandler);
    }

    /**
     * 执行一个HTTP PUT请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param params 请求参数
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void put(Context context, String url, RequestParams params, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setEntity(new HttpPut(url), HttpUtils.paramsToEntity(this, params)), contentType, httpResponseHandler);
    }

    /**
     * 执行一个HTTP PUT请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void put(Context context, String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setEntity(new HttpPut(url), HttpUtils.paramsToEntity(this, params)), httpResponseHandler);
    }
    
    /** **************************************************************************************** HTTP DELETE 请求 **************************************************************************************** */   
    /**
     * 执行一个HTTP DELETE请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param httpResponseHandler Http响应处理器
     */
    public void delete(Context context, String url, Header[] headers, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, HttpUtils.setHeaders(new HttpDelete(url), headers), null, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP DELETE请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     */
    public void delete(Context context, String url, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, new HttpDelete(url), null, httpResponseHandler);
    }
    
    /** **************************************************************************************** 发送 请求 **************************************************************************************** */   
    /**
     * 发送请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param uriRequest http请求对象
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(Context context, HttpUriRequest uriRequest, String contentType, HttpResponseHandler httpResponseHandler) {
        if(StringUtils.isNotEmpty(contentType)) {
            uriRequest.addHeader("Content-Type", contentType);
        }
        Future<?> request = EasyNetwork.getThreadPool().submit(new HttpRequestRunnable(context, this, uriRequest, httpResponseHandler));
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
     * 发送请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param uriRequest http请求对象
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(Context context, HttpUriRequest uriRequest, HttpResponseHandler httpResponseHandler) {
       sendRequest(context, uriRequest, null, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP 请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams，如果请求对象有Post注解就会以Post的方式来发送请求，否则一律采用Get的方式来发送请求
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(Context context, String url, Header[] headers, Request request, String contentType, HttpResponseHandler httpResponseHandler){
    	if(request.getClass().getAnnotation(Post.class) != null){
    		post(context, url, headers, request, contentType, httpResponseHandler);
    	}else{
    		get(context, url, headers, request, httpResponseHandler);
    	}
    }
    
    /**
     * 执行一个HTTP 请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams，如果请求对象有Post注解就会以Post的方式来发送请求，否则一律采用Get的方式来发送请求
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(Context context, String url, Request request, String contentType, HttpResponseHandler httpResponseHandler){
    	if(request.getClass().getAnnotation(Post.class) != null){
    		post(context, url, request, contentType, httpResponseHandler);
    	}else{
    		get(context, url, request, httpResponseHandler);
    	}
    }
    
    /**
     * 执行一个HTTP 请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams，如果请求对象有Post注解就会以Post的方式来发送请求，否则一律采用Get的方式来发送请求
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(Context context, String url, Request request, HttpResponseHandler httpResponseHandler){
    	if(request.getClass().getAnnotation(Post.class) != null){
    		post(context, url, request, httpResponseHandler);
    	}else{
    		get(context, url, request, httpResponseHandler);
    	}
    }
    
    /**
     * 执行一个HTTP 请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams，如果请求对象有Post注解就会以Post的方式来发送请求，否则一律采用Get的方式来发送请求
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(Context context, Request request, HttpResponseHandler httpResponseHandler){
    	if(request.getClass().getAnnotation(Post.class) != null){
    		post(context, request, httpResponseHandler);
    	}else{
    		get(context, request, httpResponseHandler);
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