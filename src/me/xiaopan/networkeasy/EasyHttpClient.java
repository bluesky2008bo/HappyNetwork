package me.xiaopan.networkeasy;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import me.xiaopan.networkeasy.interceptor.AddRequestHeaderRequestInterceptor;
import me.xiaopan.networkeasy.interceptor.GzipProcessRequestInterceptor;
import me.xiaopan.networkeasy.interceptor.GzipProcessResponseInterceptor;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
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
 * @author xiaopan
 */
public class EasyHttpClient {
    private static final String VERSION = "1.4.3";
	private static final int DEFAULT_MAX_CONNECTIONS = 10;	//最大连接数
    private static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;	//连接超时时间
    private static final int DEFAULT_MAX_RETRIES = 5;	//最大重试次数
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;	//Socket缓存大小
    
	private HttpContext httpContext;	//Http上下文
	private DefaultHttpClient httpClient;	//Http客户端
	private ThreadPoolExecutor threadPool;	//线程池
    private Map<Context, List<WeakReference<Future<?>>>> requestMap;
    private Map<String, String> clientHeaderMap;
    
    private static EasyHttpClient easyHttpClient = null;
	
	public EasyHttpClient(){
		httpContext = new SyncBasicHttpContext(new BasicHttpContext());
		requestMap = new WeakHashMap<Context, List<WeakReference<Future<?>>>>();
        clientHeaderMap = new HashMap<String, String>();
		threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		
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
     * Sets basic authentication for the request. Uses AuthScope.ANY. This is the same as
     * setBasicAuth('username','password',AuthScope.ANY) 
     * @param username
     * @param password
     */
    public void setBasicAuth(String user, String pass){
        setBasicAuth(user, pass, AuthScope.ANY);
    }
    
   /**
     * Sets basic authentication for the request. You should pass in your AuthScope for security. It should be like this
     * setBasicAuth("username","password", new AuthScope("host",port,AuthScope.ANY_REALM))
     * @param username
     * @param password
     * @param scope - an AuthScope object
     *
     */
    public void setBasicAuth( String user, String pass, AuthScope scope){
        this.httpClient.getCredentialsProvider().setCredentials(scope, new UsernamePasswordCredentials(user,pass));
    }
    
    /**
     * Perform a HTTP GET request, without any parameters.
     * @param url the URL to send the request to.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void get(String url, HttpResponseHandler responseHandler) {
        get(null, url, null, responseHandler);
    }

    /**
     * Perform a HTTP GET request with parameters.
     * @param url the URL to send the request to.
     * @param params additional GET parameters to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void get(String url, RequestParams params, HttpResponseHandler responseHandler) {
        get(null, url, params, responseHandler);
    }

    /**
     * Perform a HTTP GET request without any parameters and track the Android Context which initiated the request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void get(Context context, String url, HttpResponseHandler responseHandler) {
        get(context, url, null, responseHandler);
    }

    /**
     * Perform a HTTP GET request and track the Android Context which initiated the request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param params additional GET parameters to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void get(Context context, String url, RequestParams params, HttpResponseHandler responseHandler) {
        sendRequest(new HttpGet(getUrlWithQueryString(url, params)), null, responseHandler, context);
    }
    
    /**
     * Perform a HTTP GET request and track the Android Context which initiated
     * the request with customized headers
     * 
     * @param url the URL to send the request to.
     * @param headers set headers only for this request
     * @param params additional GET parameters to send with the request.
     * @param responseHandler the response handler instance that should handle
     *        the response.
     */
    public void get(Context context, String url, Header[] headers, RequestParams params, HttpResponseHandler responseHandler) {
        HttpUriRequest request = new HttpGet(getUrlWithQueryString(url, params));
        if(headers != null){
        	request.setHeaders(headers);
        }
        sendRequest(request, null, responseHandler, context);
    }


    
    /**
     * Perform a HTTP POST request, without any parameters.
     * @param url the URL to send the request to.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void post(String url, HttpResponseHandler responseHandler) {
        post(null, url, null, responseHandler);
    }

    /**
     * Perform a HTTP POST request with parameters.
     * @param url the URL to send the request to.
     * @param params additional POST parameters or files to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void post(String url, RequestParams params, HttpResponseHandler responseHandler) {
        post(null, url, params, responseHandler);
    }

    /**
     * Perform a HTTP POST request and track the Android Context which initiated the request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param params additional POST parameters or files to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void post(Context context, String url, RequestParams params, HttpResponseHandler responseHandler) {
        post(context, url, paramsToEntity(params), null, responseHandler);
    }

    /**
     * Perform a HTTP POST request and track the Android Context which initiated the request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param entity a raw {@link HttpEntity} to send with the request, for example, use this to send string/json/xml payloads to a server by passing a {@link org.apache.http.entity.StringEntity}.
     * @param contentType the content type of the payload you are sending, for example application/json if sending a json payload.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void post(Context context, String url, HttpEntity entity, String contentType, HttpResponseHandler responseHandler) {
        sendRequest(addEntityToRequestBase(new HttpPost(url), entity), contentType, responseHandler, context);
    }

    /**
     * Perform a HTTP POST request and track the Android Context which initiated
     * the request. Set headers only for this request
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param headers set headers only for this request
     * @param params additional POST parameters to send with the request.
     * @param contentType the content type of the payload you are sending, for example application/json if sending a json payload.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void post(Context context, String url, Header[] headers, RequestParams params, String contentType, HttpResponseHandler responseHandler) {
        HttpEntityEnclosingRequestBase request = new HttpPost(url);
        if(params != null) request.setEntity(paramsToEntity(params));
        if(headers != null) request.setHeaders(headers);
        sendRequest(request, contentType, responseHandler, context);
    }

    /**
     * Perform a HTTP POST request and track the Android Context which initiated
     * the request. Set headers only for this request
     *
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param headers set headers only for this request
     * @param entity a raw {@link HttpEntity} to send with the request, for
     *        example, use this to send string/json/xml payloads to a server by
     *        passing a {@link org.apache.http.entity.StringEntity}.
     * @param contentType the content type of the payload you are sending, for
     *        example application/json if sending a json payload.
     * @param responseHandler the response handler instance that should handle
     *        the response.
     */
    public void post(Context context, String url, Header[] headers, HttpEntity entity, String contentType,
            HttpResponseHandler responseHandler) {
        HttpEntityEnclosingRequestBase request = addEntityToRequestBase(new HttpPost(url), entity);
        if(headers != null) request.setHeaders(headers);
        sendRequest(request, contentType, responseHandler, context);
    }

    //
    // HTTP PUT Requests
    //

    /**
     * Perform a HTTP PUT request, without any parameters.
     * @param url the URL to send the request to.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void put(String url, HttpResponseHandler responseHandler) {
        put(null, url, null, responseHandler);
    }

    /**
     * Perform a HTTP PUT request with parameters.
     * @param url the URL to send the request to.
     * @param params additional PUT parameters or files to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void put(String url, RequestParams params, HttpResponseHandler responseHandler) {
        put(null, url, params, responseHandler);
    }

    /**
     * Perform a HTTP PUT request and track the Android Context which initiated the request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param params additional PUT parameters or files to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void put(Context context, String url, RequestParams params, HttpResponseHandler responseHandler) {
        put(context, url, paramsToEntity(params), null, responseHandler);
    }

    /**
     * Perform a HTTP PUT request and track the Android Context which initiated the request.
     * And set one-time headers for the request
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param entity a raw {@link HttpEntity} to send with the request, for example, use this to send string/json/xml payloads to a server by passing a {@link org.apache.http.entity.StringEntity}.
     * @param contentType the content type of the payload you are sending, for example application/json if sending a json payload.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void put(Context context, String url, HttpEntity entity, String contentType, HttpResponseHandler responseHandler) {
        sendRequest(addEntityToRequestBase(new HttpPut(url), entity), contentType, responseHandler, context);
    }
    
    /**
     * Perform a HTTP PUT request and track the Android Context which initiated the request.
     * And set one-time headers for the request
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param headers set one-time headers for this request
     * @param entity a raw {@link HttpEntity} to send with the request, for example, use this to send string/json/xml payloads to a server by passing a {@link org.apache.http.entity.StringEntity}.
     * @param contentType the content type of the payload you are sending, for example application/json if sending a json payload.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void put(Context context, String url,Header[] headers, HttpEntity entity, String contentType, HttpResponseHandler responseHandler) {
        HttpEntityEnclosingRequestBase request = addEntityToRequestBase(new HttpPut(url), entity);
        if(headers != null) request.setHeaders(headers);
        sendRequest(request, contentType, responseHandler, context);
    }

    //
    // HTTP DELETE Requests
    //

    /**
     * Perform a HTTP DELETE request.
     * @param url the URL to send the request to.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void delete(String url, HttpResponseHandler responseHandler) {
        delete(null, url, responseHandler);
    }

    /**
     * Perform a HTTP DELETE request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void delete(Context context, String url, HttpResponseHandler responseHandler) {
        final HttpDelete delete = new HttpDelete(url);
        sendRequest(delete, null, responseHandler, context);
    }
    
    /**
     * Perform a HTTP DELETE request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param headers set one-time headers for this request
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void delete(Context context, String url, Header[] headers, HttpResponseHandler responseHandler) {
        final HttpDelete delete = new HttpDelete(url);
        if(headers != null) delete.setHeaders(headers);
        sendRequest(delete, null, responseHandler, context);
    }
    
    /**
     * 发送请求
     * @param client
     * @param httpContext
     * @param uriRequest
     * @param contentType
     * @param responseHandler
     * @param context
     */
    public void sendRequest(HttpUriRequest uriRequest, String contentType, HttpResponseHandler responseHandler, Context context) {
        if(contentType != null) {
            uriRequest.addHeader("Content-Type", contentType);
        }

        Future<?> request = threadPool.submit(new AsyncHttpRequest(httpClient, httpContext, uriRequest, responseHandler));

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
     * 取消所有的请求，请求如果尚未开始就不再执行，如果已经开始就尝试中断
     * <p>
     * <b>Note:</b> This will only affect requests which were created with a non-null
     * android Context. This method is intended to be used in the onDestroy
     * method of your android activities to destroy all requests which are no
     * longer required.
     *
     * @param context the android Context instance associated to the request.
     * @param mayInterruptIfRunning specifies if active requests should be cancelled along with pending requests.
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
	 * 获取默认的Http参数集
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
        HttpProtocolParams.setUserAgent(httpParams, String.format("android-async-http/%s (http://loopj.com/android-async-http)", VERSION));
        
        return httpParams;
	}

	private String getUrlWithQueryString(String url, RequestParams params) {
        if(params != null) {
            String paramString = params.getParamString();
            if (url.indexOf("?") == -1) {
                url += "?" + paramString;
            } else {
                url += "&" + paramString;
            }
        }

        return url;
    }
    
    private HttpEntity paramsToEntity(RequestParams params) {
        return params != null?params.getEntity():null;
    }

    private HttpEntityEnclosingRequestBase addEntityToRequestBase(HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {
        if(entity != null){
            requestBase.setEntity(entity);
        }
        return requestBase;
    }
}