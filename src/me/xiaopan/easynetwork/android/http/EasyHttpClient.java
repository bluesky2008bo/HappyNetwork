package me.xiaopan.easynetwork.android.http;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;

import me.xiaopan.easynetwork.android.EasyNetwork;
import me.xiaopan.easynetwork.android.http.interceptor.AddRequestHeaderRequestInterceptor;
import me.xiaopan.easynetwork.android.http.interceptor.GzipProcessRequestInterceptor;
import me.xiaopan.easynetwork.android.http.interceptor.GzipProcessResponseInterceptor;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

import android.content.Context;

/**
 * Http客户端，所有的Http操作都将由此类来异步完成，同时此类提供一个单例模式来方便直接使用
 */
public class EasyHttpClient {
	private static final int DEFAULT_MAX_CONNECTIONS = 10;	//最大连接数
    private static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;	//连接超时时间
    private static final int DEFAULT_MAX_RETRIES = 5;	//最大重试次数
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;	//Socket缓存大小
    private static boolean enableOutputLogToConsole = true;
    private static EasyHttpClient easyHttpClient;	//实例
    private DefaultHttpClient httpClient;	//Http客户端
	private HttpContext httpContext;	//Http上下文
    private Map<Context, List<WeakReference<Future<?>>>> requestMap;	//请求Map
    private Map<String, String> clientHeaderMap;	//请求头Map
	
	public EasyHttpClient(){
		httpContext = new SyncBasicHttpContext(new BasicHttpContext());
		requestMap = new WeakHashMap<Context, List<WeakReference<Future<?>>>>();
        clientHeaderMap = new HashMap<String, String>();
		
		/* 初始化HttpClient */
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		HttpParams httpParams = getDefaultHttpParams();
        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
        httpClient.addRequestInterceptor(new GzipProcessRequestInterceptor());
        httpClient.addRequestInterceptor(new AddRequestHeaderRequestInterceptor(clientHeaderMap));
        httpClient.addResponseInterceptor(new GzipProcessResponseInterceptor());
        httpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_MAX_RETRIES));
	}
	
	/**
	 * 获取实例
	 * @return 实例
	 */
	public static final EasyHttpClient getInstance(){
		if(easyHttpClient == null){
			easyHttpClient = new EasyHttpClient();
		}
		return easyHttpClient;
	}
	
	/**
     * 设置Cookie仓库，将在发送请求时使用此Cookie仓库
     * @param cookieStore 另请参见 {@link PersistentCookieStore}
     */
    public void setCookieStore(CookieStore cookieStore) {
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }
    
    /**
     * 设置代理，在之后的每一次请求都将使用此代理
     * @param userAgent 用户代理的信息将会添加在“User-Agent”请求头中
     */
    public void setUserAgent(String userAgent) {
        HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
    }
    
    /**
     * 设置请求超时时间，默认是10秒
     * @param timeout 请求超时时间，单位毫秒
     */
    public void setTimeout(int timeout){
        final HttpParams httpParams = this.httpClient.getParams();
        ConnManagerParams.setTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
    }
    
    /**
     * Sets the SSLSocketFactory to user when making requests. By default,
     * a new, default SSLSocketFactory is used.
     * @param sslSocketFactory the socket factory to use for https requests.
     */
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sslSocketFactory, 443));
    }
    
    /**
     * 添加一个请求头参数，这些参数都会在发送请求之前添加到请求体中
     * @param header 参数名
     * @param value 参数值
     */
    public void addHeader(String header, String value) {
    	clientHeaderMap.put(header, value);
    }

    /**
     * 设置Http Auth认证
     * @param username 用户名
     * @param password 密码
     */
    public void setBasicAuth(String user, String pass){
        setBasicAuth(user, pass, AuthScope.ANY);
    }
    
   /**
     * 设置Http Auth认证
     * @param username 用户名
     * @param password 密码
     * @param scope 
     */
    public void setBasicAuth( String user, String pass, AuthScope scope){
        this.httpClient.getCredentialsProvider().setCredentials(scope, new UsernamePasswordCredentials(user,pass));
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
        sendRequest(context, EasyNetworkUtils.setHeaders(new HttpGet(EasyNetworkUtils.getUrlWithQueryString(url, params)), headers), httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP GET请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void get(Context context, String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, new HttpGet(EasyNetworkUtils.getUrlWithQueryString(url, params)), httpResponseHandler);
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
			sendRequest(context, EasyNetworkUtils.setHeaders(new HttpGet(EasyNetworkUtils.getUrlWithQueryString(EasyNetworkUtils.getUrlFromRequestObject(url, request), EasyNetworkUtils.requestToRequestParams(request))), headers), httpResponseHandler);
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
			sendRequest(context, new HttpGet(EasyNetworkUtils.getUrlWithQueryString(EasyNetworkUtils.getUrlFromRequestObject(url, request), EasyNetworkUtils.requestToRequestParams(request))), httpResponseHandler);
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
    public void get(Request request, HttpResponseHandler httpResponseHandler){
		try {
			sendRequest(new HttpGet(EasyNetworkUtils.getUrlWithQueryString(EasyNetworkUtils.getUrlFromRequestObject(null, request), EasyNetworkUtils.requestToRequestParams(request))), httpResponseHandler);
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
     * @param headers 请求头信息
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void get(String url, Header[] headers, RequestParams params, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setHeaders(new HttpGet(EasyNetworkUtils.getUrlWithQueryString(url, params)), headers), httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP GET请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void get(String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
        sendRequest(new HttpGet(EasyNetworkUtils.getUrlWithQueryString(url, params)), httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP GET请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     */
    public void get(String url, HttpResponseHandler httpResponseHandler) {
        sendRequest(new HttpGet(url), httpResponseHandler);
    }

    /**
     * 执行一个HTTP GET请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param httpResponseHandler Http响应处理器
     */
    public void get(String url, Header[] headers, Request request, HttpResponseHandler httpResponseHandler){
		try {
			sendRequest(EasyNetworkUtils.setHeaders(new HttpGet(EasyNetworkUtils.getUrlWithQueryString(EasyNetworkUtils.getUrlFromRequestObject(url, request), EasyNetworkUtils.requestToRequestParams(request))), headers), httpResponseHandler);
		} catch (Exception e) {
			if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
		}
    }

    /**
     * 执行一个HTTP GET请求
     * @param url 请求地址
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param httpResponseHandler Http响应处理器
     */
    public void get(String url, Request request, HttpResponseHandler httpResponseHandler){
		try {
			sendRequest(new HttpGet(EasyNetworkUtils.getUrlWithQueryString(EasyNetworkUtils.getUrlFromRequestObject(url, request), EasyNetworkUtils.requestToRequestParams(request))), httpResponseHandler);
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
        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPost(url), entity, headers), contentType, httpResponseHandler);
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
        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPost(url), entity), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param entity 请求实体
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, HttpEntity entity, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPost(url), entity), httpResponseHandler);
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
        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPost(url), EasyNetworkUtils.paramsToEntity(params), headers), contentType, httpResponseHandler);
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
        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPost(url), EasyNetworkUtils.paramsToEntity(params)), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Context context, String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPost(url), EasyNetworkUtils.paramsToEntity(params)), httpResponseHandler);
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
	        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPost(EasyNetworkUtils.getUrlFromRequestObject(url, request)), EasyNetworkUtils.paramsToEntity(EasyNetworkUtils.requestToRequestParams(request)), headers), contentType, httpResponseHandler);
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
	        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPost(EasyNetworkUtils.getUrlFromRequestObject(url, request)), EasyNetworkUtils.paramsToEntity(EasyNetworkUtils.requestToRequestParams(request))), contentType, httpResponseHandler);
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
	        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPost(EasyNetworkUtils.getUrlFromRequestObject(url, request)), EasyNetworkUtils.paramsToEntity(EasyNetworkUtils.requestToRequestParams(request))), httpResponseHandler);
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
	        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPost(EasyNetworkUtils.getUrlFromRequestObject(null, request)), EasyNetworkUtils.paramsToEntity(EasyNetworkUtils.requestToRequestParams(request))), httpResponseHandler);
	    } catch (Exception e) {
	    	if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
	    }
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param entity 请求实体
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(String url, Header[] headers, HttpEntity entity, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setEntity(new HttpPost(url), entity, headers), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param url 请求地址
     * @param entity 请求实体
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(String url, HttpEntity entity, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setEntity(new HttpPost(url), entity), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param url 请求地址
     * @param entity 请求实体
     * @param httpResponseHandler Http响应处理器
     */
    public void post(String url, HttpEntity entity, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setEntity(new HttpPost(url), entity), httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     */
    public void post(String url, HttpResponseHandler httpResponseHandler) {
        sendRequest(new HttpPost(url), httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param params 请求参数
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(String url, Header[] headers, RequestParams params, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setEntity(new HttpPost(url), EasyNetworkUtils.paramsToEntity(params), headers), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param url 请求地址
     * @param params 请求参数
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(String url, RequestParams params, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setEntity(new HttpPost(url), EasyNetworkUtils.paramsToEntity(params)), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP POST请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void post(String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setEntity(new HttpPost(url), EasyNetworkUtils.paramsToEntity(params)), httpResponseHandler);
    }

    /**
     * 执行一个HTTP POST请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(String url, Header[] headers, Request request, String contentType, HttpResponseHandler httpResponseHandler){
	    try {
	        sendRequest(EasyNetworkUtils.setEntity(new HttpPost(EasyNetworkUtils.getUrlFromRequestObject(url, request)), EasyNetworkUtils.paramsToEntity(EasyNetworkUtils.requestToRequestParams(request)), headers), contentType, httpResponseHandler);
	    } catch (Exception e) {
	    	if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
	    }
    }

    /**
     * 执行一个HTTP POST请求
     * @param url 请求地址
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(String url, Request request, String contentType, HttpResponseHandler httpResponseHandler){
	    try {
	        sendRequest(EasyNetworkUtils.setEntity(new HttpPost(EasyNetworkUtils.getUrlFromRequestObject(url, request)), EasyNetworkUtils.paramsToEntity(EasyNetworkUtils.requestToRequestParams(request))), contentType, httpResponseHandler);
	    } catch (Exception e) {
	    	if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
	    }
    }

    /**
     * 执行一个HTTP POST请求
     * @param url 请求地址
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param httpResponseHandler Http响应处理器
     */
    public void post(String url, Request request, HttpResponseHandler httpResponseHandler){
	    try {
	        sendRequest(EasyNetworkUtils.setEntity(new HttpPost(EasyNetworkUtils.getUrlFromRequestObject(url, request)), EasyNetworkUtils.paramsToEntity(EasyNetworkUtils.requestToRequestParams(request))), httpResponseHandler);
	    } catch (Exception e) {
	    	if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
	    }
    }

    /**
     * 执行一个HTTP POST请求
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Request request, String contentType, HttpResponseHandler httpResponseHandler){
	    try {
	        sendRequest(EasyNetworkUtils.setEntity(new HttpPost(EasyNetworkUtils.getUrlFromRequestObject(null, request)), EasyNetworkUtils.paramsToEntity(EasyNetworkUtils.requestToRequestParams(request))), contentType, httpResponseHandler);
	    } catch (Exception e) {
	    	if(httpResponseHandler != null){
				httpResponseHandler.exception(e);
			}
	    }
    }

    /**
     * 执行一个HTTP POST请求
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams
     * @param httpResponseHandler Http响应处理器
     */
    public void post(Request request, HttpResponseHandler httpResponseHandler){
	    try {
	        sendRequest(EasyNetworkUtils.setEntity(new HttpPost(EasyNetworkUtils.getUrlFromRequestObject(null, request)), EasyNetworkUtils.paramsToEntity(EasyNetworkUtils.requestToRequestParams(request))), httpResponseHandler);
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
        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPut(url), entity, headers), contentType, httpResponseHandler);
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
        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPut(url), entity), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP PUT请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url the 请求地址
     * @param entity 请求实体
     * @param httpResponseHandler Http响应处理器
     */
    public void put(Context context, String url, HttpEntity entity, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPut(url), entity), httpResponseHandler);
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
        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPut(url), EasyNetworkUtils.paramsToEntity(params), headers), contentType, httpResponseHandler);
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
        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPut(url), EasyNetworkUtils.paramsToEntity(params)), contentType, httpResponseHandler);
    }

    /**
     * 执行一个HTTP PUT请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void put(Context context, String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
        sendRequest(context, EasyNetworkUtils.setEntity(new HttpPut(url), EasyNetworkUtils.paramsToEntity(params)), httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP PUT请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url the 请求地址
     * @param headers 请求头信息
     * @param entity 请求实体
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void put(String url, Header[] headers, HttpEntity entity, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setEntity(new HttpPut(url), entity, headers), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP PUT请求
     * @param context Android上下文，稍后你可以通过此上下文来取消此次请求
     * @param url the 请求地址
     * @param entity 请求实体
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void put(String url, HttpEntity entity, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setEntity(new HttpPut(url), entity), contentType, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP PUT请求
     * @param url the 请求地址
     * @param entity 请求实体
     * @param httpResponseHandler Http响应处理器
     */
    public void put(String url, HttpEntity entity, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setEntity(new HttpPut(url), entity), httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP PUT请求
     * @param url the 请求地址
     * @param entity 请求实体
     * @param httpResponseHandler Http响应处理器
     */
    public void put(String url, HttpResponseHandler httpResponseHandler) {
        sendRequest(new HttpPut(url), httpResponseHandler);
    }

    /**
     * 执行一个HTTP PUT请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param params 请求参数
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void put(String url, Header[] headers, RequestParams params, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setEntity(new HttpPut(url), EasyNetworkUtils.paramsToEntity(params), headers), contentType, httpResponseHandler);
    }

    /**
     * 执行一个HTTP PUT请求
     * @param url 请求地址
     * @param params 请求参数
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void put(String url, RequestParams params, String contentType, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setEntity(new HttpPut(url), EasyNetworkUtils.paramsToEntity(params)), contentType, httpResponseHandler);
    }

    /**
     * 执行一个HTTP PUT请求
     * @param url 请求地址
     * @param params 请求参数
     * @param httpResponseHandler Http响应处理器
     */
    public void put(String url, RequestParams params, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setEntity(new HttpPut(url), EasyNetworkUtils.paramsToEntity(params)), httpResponseHandler);
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
        sendRequest(context, EasyNetworkUtils.setHeaders(new HttpDelete(url), headers), null, httpResponseHandler);
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
    
    /**
     * 执行一个HTTP DELETE请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param httpResponseHandler Http响应处理器
     */
    public void delete(String url, Header[] headers, HttpResponseHandler httpResponseHandler) {
        sendRequest(EasyNetworkUtils.setHeaders(new HttpDelete(url), headers), null, httpResponseHandler);
    }
    
    /**
     * 执行一个HTTP DELETE请求
     * @param url 请求地址
     * @param httpResponseHandler Http响应处理器
     */
    public void delete(String url, HttpResponseHandler httpResponseHandler) {
        sendRequest(new HttpDelete(url), null, httpResponseHandler);
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
        if(contentType != null) {
            uriRequest.addHeader("Content-Type", contentType);
        }

        Future<?> request = EasyNetwork.getThreadPool().submit(new HttpRequestRunnable(httpClient, httpContext, uriRequest, httpResponseHandler));

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
     * 发送请求
     * @param uriRequest http请求对象
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(HttpUriRequest uriRequest, String contentType, HttpResponseHandler httpResponseHandler) {
       sendRequest(null, uriRequest, contentType, httpResponseHandler);
    }
    
    /**
     * 发送请求
     * @param uriRequest http请求对象
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(HttpUriRequest uriRequest, HttpResponseHandler httpResponseHandler) {
       sendRequest(null, uriRequest, null, httpResponseHandler);
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
     * @param url 请求地址
     * @param headers 请求头信息
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams，如果请求对象有Post注解就会以Post的方式来发送请求，否则一律采用Get的方式来发送请求
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(String url, Header[] headers, Request request, String contentType, HttpResponseHandler httpResponseHandler){
    	if(request.getClass().getAnnotation(Post.class) != null){
    		post(url, headers, request, contentType, httpResponseHandler);
    	}else{
    		get(url, headers, request, httpResponseHandler);
    	}
    }
    
    /**
     * 执行一个HTTP 请求
     * @param url 请求地址
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams，如果请求对象有Post注解就会以Post的方式来发送请求，否则一律采用Get的方式来发送请求
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(String url, Request request, String contentType, HttpResponseHandler httpResponseHandler){
    	if(request.getClass().getAnnotation(Post.class) != null){
    		post(url, request, contentType, httpResponseHandler);
    	}else{
    		get(url, request, httpResponseHandler);
    	}
    }
    
    /**
     * 执行一个HTTP 请求
     * @param url 请求地址
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams，如果请求对象有Post注解就会以Post的方式来发送请求，否则一律采用Get的方式来发送请求
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(String url, Request request, HttpResponseHandler httpResponseHandler){
    	if(request.getClass().getAnnotation(Post.class) != null){
    		post(url, request, httpResponseHandler);
    	}else{
    		get(url, request, httpResponseHandler);
    	}
    }
    
    /**
     * 执行一个HTTP 请求
     * @param url 请求地址
     * @param headers 请求头信息
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams，如果请求对象有Post注解就会以Post的方式来发送请求，否则一律采用Get的方式来发送请求
     * @param contentType 内容类型
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(Request request, String contentType, HttpResponseHandler httpResponseHandler){
    	if(request.getClass().getAnnotation(Post.class) != null){
    		post(request, contentType, httpResponseHandler);
    	}else{
    		get(request, httpResponseHandler);
    	}
    }
    
    /**
     * 执行一个HTTP 请求
     * @param request 请求对象，EasyHttpClient会采用反射的方式将请求对象里所有加了Expose注解的字段封装成一个RequestParams，如果请求对象有Post注解就会以Post的方式来发送请求，否则一律采用Get的方式来发送请求
     * @param httpResponseHandler Http响应处理器
     */
    public void sendRequest(Request request, HttpResponseHandler httpResponseHandler){
    	if(request.getClass().getAnnotation(Post.class) != null){
    		post(request, httpResponseHandler);
    	}else{
    		get(request, httpResponseHandler);
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
	 * 获取默认的Http参数
	 * @return
	 */
	private HttpParams getDefaultHttpParams(){
		BasicHttpParams httpParams = new BasicHttpParams();

        ConnManagerParams.setTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(DEFAULT_MAX_CONNECTIONS));
        ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);

        HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);
        HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);

        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        
        return httpParams;
	}

	/**
	 * 判断是否输出Log到控制台
	 * @return 是否输出Log到控制台
	 */
	public static boolean isEnableOutputLogToConsole() {
		return enableOutputLogToConsole;
	}

	/**
	 * 设置是否输出Log到控制台
	 * @param enableOutputLogToConsole 是否输出Log到控制台
	 */
	public static void setEnableOutputLogToConsole(boolean enableOutputLogToConsole) {
		EasyHttpClient.enableOutputLogToConsole = enableOutputLogToConsole;
	}
}