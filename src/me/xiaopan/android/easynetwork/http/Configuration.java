/*
 * Copyright 2013 Peng fei Pan
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import me.xiaopan.android.easynetwork.http.interceptor.AddRequestHeaderRequestInterceptor;

import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
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
import android.os.Handler;
import android.os.Looper;

/**
 * 配置
 */
public class Configuration {
	private Context context;
	private int maxRetries = 5;	//最大重试次数
	private int connectionTimeout = 20000;	//连接超时时间
	private int maxConnections = 10;	//最大连接数
	private int socketBufferSize = 8192;	//Socket缓存池大小
	private boolean debugMode;	//调试模式
	private String logTag;	//Log Tag
	private String userAgent;	//代理
	private String defaultCacheDirerctory;	//默认缓存目录
	private Handler handler;	//异步处理器
	private CookieStore cookieStore;	//Cookie池
	private HttpContext httpContext;	//Http上下文
	private SSLSocketFactory sslSocketFactory;	//SSL加密Socket工厂
	private DefaultHttpClient defaultHttpClient;
    private Map<String, String> headerMap;	//请求头Map
	private ThreadPoolExecutor threadPool;	//线程池
    private AuthScope authScope;
    private Credentials credentials;
	
    public Configuration(Context context){
    	if(Looper.myLooper() != Looper.getMainLooper()){
			throw new IllegalStateException("你不能在异步线程中创建此对象");
		}
    	this.logTag = EasyHttpClient.class.getSimpleName();
    	this.context = context;
    	handler = new Handler();
    	headerMap = new HashMap<String, String>();
	}
    
	/**
	 * 获取上下文
	 * @return
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * 设置上下文
	 * @param context
	 */
	public void setContext(Context context) {
		this.context = context;
	}
	
	/**
	 * 获取Http上下文
	 * @return the httpContext
	 */
	public HttpContext getHttpContext() {
		if(httpContext == null){
			httpContext = new SyncBasicHttpContext(new BasicHttpContext());
			if(cookieStore != null){
				httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
			}
		}
		return httpContext;
	}

	/**
	 * 设置Http上下文
	 * @param httpContext the httpContext to set
	 */
	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = httpContext;
	}

	/**
	 * 获取Http客户端
	 * @return
	 */
	public final DefaultHttpClient getDefaultHttpClient() {
		if(defaultHttpClient == null){
	        BasicHttpParams httpParams = new BasicHttpParams();
	        GeneralUtils.setConnectionTimeout(httpParams, connectionTimeout);
			GeneralUtils.setMaxConnections(httpParams, maxConnections);
			GeneralUtils.setSocketBufferSize(httpParams, socketBufferSize);
	        HttpConnectionParams.setTcpNoDelay(httpParams, true);
	        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
	        if(userAgent != null){
             	HttpProtocolParams.setUserAgent(httpParams, userAgent);
	        }
	        SchemeRegistry schemeRegistry = new SchemeRegistry();
	        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
	        defaultHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
//	        defaultHttpClient.addRequestInterceptor(new GzipProcessRequestInterceptor());
	        defaultHttpClient.addRequestInterceptor(new AddRequestHeaderRequestInterceptor(getHeaderMap()));
//	        defaultHttpClient.addResponseInterceptor(new GzipProcessResponseInterceptor());
	        defaultHttpClient.setHttpRequestRetryHandler(new RetryHandler(maxRetries));
	        if(sslSocketFactory != null){
	    		defaultHttpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sslSocketFactory, 443));
	    	}
	        if(authScope != null && credentials != null){
	    		defaultHttpClient.getCredentialsProvider().setCredentials(authScope, credentials);
	    	}
		}
		return defaultHttpClient;
	}

	/**
	 * 设置Http客户端
	 * @param httpClient
	 */
	public void setDefaultHttpClient(DefaultHttpClient httpClient) {
		this.defaultHttpClient = httpClient;
	}
    
    /**
     * 添加一个请求头参数，这些参数都会在发送请求之前添加到请求体中
     * @param header 参数名
     * @param value 参数值
     */
    public void addHeader(String header, String value) {
    	headerMap.put(header, value);
    }

	/**
	 * 获取通用请求头集合
	 * @return
	 */
	public Map<String, String> getHeaderMap() {
		return headerMap;
	}

	/**
	 * 设置通用请求头集合
	 * @param headerMap
	 */
	public void setHeaderMap(Map<String, String> headerMap) {
		this.headerMap = headerMap;
	}
	
	/**
	 * 获取Log Tag
	 * @return
	 */
	public String getLogTag() {
		return logTag;
	}

	/**
	 * 设置Log Tag
	 * @param logTag
	 */
	public void setLogTag(String logTag) {
		this.logTag = logTag;
	}
	
	/**
	 * 获取默认缓存目录
	 * @return
	 */
	public String getDefaultCacheDirerctory() {
		return defaultCacheDirerctory;
	}

	/**
	 * 设置默认缓存目录
	 * @param defaultCacheDirerctory
	 */
	public void setDefaultCacheDirerctory(String defaultCacheDirerctory) {
		this.defaultCacheDirerctory = defaultCacheDirerctory;
	}

	/**
	 * 获取最大重置次数
	 * @return
	 */
	public int getMaxRetries() {
		return maxRetries;
	}

	/**
	 * 设置最大重置次数
	 * @param maxRetries
	 */
	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
		if(defaultHttpClient != null){
			defaultHttpClient.setHttpRequestRetryHandler(new RetryHandler(this.maxRetries));
		}
	}

	/**
	 * 获取连接超时时间，单位毫秒
	 * @return
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * 设置连接超时间，单位毫秒
	 * @param connectionTimeout
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
		GeneralUtils.setConnectionTimeout(defaultHttpClient, this.connectionTimeout);
	}

	/**
	 * 获取最大连接数
	 * @return
	 */
	public int getMaxConnections() {
		return maxConnections;
	}

	/**
	 * 设置最大连接数
	 * @param maxConnections
	 */
	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
		GeneralUtils.setMaxConnections(defaultHttpClient, this.maxConnections);
	}

	/**
	 * 获取Socket缓存池大小
	 * @return
	 */
	public int getSocketBufferSize() {
		return socketBufferSize;
	}

	/**
	 * 设置Socket缓存池大小
	 * @param socketBufferSize
	 */
	public void setSocketBufferSize(int socketBufferSize) {
		this.socketBufferSize = socketBufferSize;
		GeneralUtils.setSocketBufferSize(defaultHttpClient, this.socketBufferSize);
	}
	
	/**
	 * @return the cookieStore
	 */
	public CookieStore getCookieStore() {
		return cookieStore;
	}

	/**
     * 设置Cookie仓库，将在发送请求时使用此Cookie仓库
     * @param cookieStore 另请参见 {@link PersistentCookieStore}
     */
    public void setCookieStore(CookieStore cookieStore) {
    	this.cookieStore = cookieStore;
    	if(httpContext != null){
    		httpContext.setAttribute(ClientContext.COOKIE_STORE, this.cookieStore);
    	}
    }
    
    /**
     * 获取代理信息
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
     * 设置代理，在之后的每一次请求都将使用此代理
     * @param userAgent 用户代理的信息将会添加在“User-Agent”请求头中
     */
    public void setUserAgent(String userAgent) {
    	this.userAgent = userAgent;
        if(defaultHttpClient != null){
        	HttpProtocolParams.setUserAgent(defaultHttpClient.getParams(), this.userAgent);
        }
    }
    
    /**
	 * @return the sslSocketFactory
	 */
	public SSLSocketFactory getSSLSocketFactory() {
		return sslSocketFactory;
	}

	/**
     * Sets the SSLSocketFactory to user when making requests. By default,
     * a new, default SSLSocketFactory is used.
     * @param sslSocketFactory the socket factory to use for https requests.
     */
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
    	this.sslSocketFactory = sslSocketFactory;
    	if(defaultHttpClient != null){
    		defaultHttpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", this.sslSocketFactory, 443));
    	}
    }
    
   /**
     * 设置Http Auth认证
     * @param user 用户名
     * @param pass 密码
     * @param scope 
     */
    public void setBasicAuth(String user, String pass, AuthScope scope){
    	authScope = scope;
    	credentials = new UsernamePasswordCredentials(user,pass);
    	if(defaultHttpClient != null){
    		defaultHttpClient.getCredentialsProvider().setCredentials(authScope, credentials);
    	}
    }

    /**
     * 设置Http Auth认证
     * @param user 用户名
     * @param pass 密码
     */
    public void setBasicAuth(String user, String pass){
        setBasicAuth(user, pass, AuthScope.ANY);
    }

    /**
     * 获取Hander
     * @return
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * 设置Handler
     * @param handler
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * 获取线程池
     * @return
     */
    public ThreadPoolExecutor getThreadPool() {
        if(threadPool == null){
            threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        }
        return threadPool;
    }
    
	/**
	 * 设置线程池
	 * @param threadPool
	 */
	public void setThreadPool(ThreadPoolExecutor threadPool) {
		this.threadPool = threadPool;
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