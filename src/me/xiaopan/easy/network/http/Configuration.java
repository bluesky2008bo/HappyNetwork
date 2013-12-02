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

package me.xiaopan.easy.network.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.os.Handler;

/**
 * 配置
 */
public class Configuration {
	private int maxRetries;	//最大重试次数
	private int connectionTimeout;	//连接超时时间
	private int maxConnections;	//最大连接数
	private int socketBufferSize;	//Socket缓存池大小
	private String logTag;	//Log Tag
	private EasyHttpClient easyHhttpClient;
    private Map<String, String> headerMap;	//请求头Map
    private Handler handler;
	
	public Configuration(EasyHttpClient httpClient){
		this.easyHhttpClient = httpClient;
        headerMap = new HashMap<String, String>();
        logTag = "EasyHttpClient";
        handler = new Handler();
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
		if(maxRetries > 0){
			this.maxRetries = maxRetries;
			easyHhttpClient.getHttpClient().setHttpRequestRetryHandler(new RetryHandler(maxRetries));
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
		if(connectionTimeout > 0){
			this.connectionTimeout = connectionTimeout;
			HttpParams httpParams = easyHhttpClient.getHttpClient().getParams();
			ConnManagerParams.setTimeout(httpParams, connectionTimeout);
			HttpConnectionParams.setSoTimeout(httpParams, connectionTimeout);
			HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
		}
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
		if(maxConnections > 0){
			this.maxConnections = maxConnections;
			HttpParams httpParams = easyHhttpClient.getHttpClient().getParams();
			ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
			ConnManagerParams.setMaxTotalConnections(httpParams, maxConnections);
		}
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
		if(socketBufferSize > 0){
			this.socketBufferSize = socketBufferSize;
			HttpParams httpParams = easyHhttpClient.getHttpClient().getParams();
			HttpConnectionParams.setSocketBufferSize(httpParams, socketBufferSize);
		}
	}
	
	/**
     * 设置Cookie仓库，将在发送请求时使用此Cookie仓库
     * @param cookieStore 另请参见 {@link PersistentCookieStore}
     */
    public void setCookieStore(CookieStore cookieStore) {
    	easyHhttpClient.getHttpContext().setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }
    
    /**
     * 设置代理，在之后的每一次请求都将使用此代理
     * @param userAgent 用户代理的信息将会添加在“User-Agent”请求头中
     */
    public void setUserAgent(String userAgent) {
        HttpProtocolParams.setUserAgent(easyHhttpClient.getHttpClient().getParams(), userAgent);
    }
    
    /**
     * Sets the SSLSocketFactory to user when making requests. By default,
     * a new, default SSLSocketFactory is used.
     * @param sslSocketFactory the socket factory to use for https requests.
     */
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
    	easyHhttpClient.getHttpClient().getConnectionManager().getSchemeRegistry().register(new Scheme("https", sslSocketFactory, 443));
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
     * 设置Http Auth认证
     * @param user 用户名
     * @param pass 密码
     * @param scope 
     */
    public void setBasicAuth( String user, String pass, AuthScope scope){
    	easyHhttpClient.getHttpClient().getCredentialsProvider().setCredentials(scope, new UsernamePasswordCredentials(user,pass));
    }

    /**
     * 获取Hander
     * @return
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * 设置Hande
     * @param handler
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}