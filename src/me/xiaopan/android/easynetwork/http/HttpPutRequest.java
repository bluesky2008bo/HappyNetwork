package me.xiaopan.android.easynetwork.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

/**
 * Http Put请求
 * Created by XIAOPAN on 13-11-24.
 */
public class HttpPutRequest {
    private String name;    //本次请求的名称，默认为当前时间，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
    private String baseUrl; //请求地址
    private List<Header> headers;   //请求头信息
    private RequestParams params;   //请求参数
    private ResponseCache responseCache;    //响应缓存配置
    private HttpEntity httpEntity;  //Http请求体
    private List<String> cacheIgnoreParams;	//计算缓存ID时候忽略的参数集

    private HttpPutRequest(){
        setName(GeneralUtils.getCurrentDateTimeBy24Hour() + " PUT ");
    }

    /**
     * 获取请求名称，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 设置请求名称，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取请求地址
     * @return
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 设置请求地址
     * @param baseUrl
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 获取所有的请求头
     * @return
     */
    public List<Header> getHeaders() {
        return headers;
    }

    /**
     * 添加请求头
     * @param headers
     */
    public void addHeader(Header... headers){
        if(headers != null && headers.length > 0){
            if(this.headers == null){
                this.headers = new LinkedList<Header>();
            }
            for(Header header : headers){
                if(header != null){
                    this.headers.add(header);
                }
            }
        }
    }

    /**
     * 设置请求头
     * @param headers
     */
    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    /**
     * 获取请求参数
     * @return
     */
    public RequestParams getParams() {
        return params;
    }

    /**
     * 添加请求参数
     * @param key
     * @param value
     */
    public void addParam(String key, String value){
        if(GeneralUtils.isNotEmpty(key, value)){
            if(params == null){
                params = new RequestParams();
            }
            params.put(key, value);
        }
    }

    /**
     * 添加请求参数
     * @param key
     * @param values
     */
    public void addParam(String key, ArrayList<String> values){
        if(GeneralUtils.isNotEmpty(key) && values != null && values.size() > 0){
            if(params == null){
                params = new RequestParams();
            }
            params.put(key, values);
        }
    }

    /**
     * 添加请求参数
     * @param key
     * @param value
     */
    public void addParam(String key, File value){
        if(GeneralUtils.isNotEmpty(key) && value != null && value.exists()){
            if(params == null){
                params = new RequestParams();
            }
            try{
                params.put(key, value);
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加请求参数
     * @param key
     * @param value
     */
    public void addParam(String key, InputStream value){
        if(GeneralUtils.isNotEmpty(key) && value != null){
            if(params == null){
                params = new RequestParams();
            }
            params.put(key, value);
        }
    }

    /**
     * 添加请求参数
     * @param key
     * @param value
     * @param fileName
     */
    public void addParam(String key, InputStream value, String fileName){
        if(GeneralUtils.isNotEmpty(key, fileName) && value != null){
            if(params == null){
                params = new RequestParams();
            }
            params.put(key, value, fileName);
        }
    }

    /**
     * 添加请求参数
     * @param key
     * @param value
     * @param fileName
     * @param contentType
     */
    public void addParam(String key, InputStream value, String fileName, String contentType){
        if(GeneralUtils.isNotEmpty(key, fileName, contentType) && value != null){
            if(params == null){
                params = new RequestParams();
            }
            params.put(key, value, fileName, contentType);
        }
    }

    /**
     * 设置请求参数
     * @param params
     */
    public void setParams(RequestParams params) {
        this.params = params;
    }

    /**
     * 获取响应缓存配置
     * @return
     */
    public ResponseCache getResponseCache() {
        return responseCache;
    }

    /**
     * 设置响应缓存配置
     * @param responseCache
     */
    public void setResponseCache(ResponseCache responseCache) {
        this.responseCache = responseCache;
    }

    /**
     * 获取请求实体
     * @return
     */
    public HttpEntity getHttpEntity() {
        return httpEntity;
    }

    /**
     * 设置请求实体
     * @param httpEntity
     */
    public void setHttpEntity(HttpEntity httpEntity) {
        this.httpEntity = httpEntity;
    }
    
    /**
     * 获取计算缓存ID时候忽略的参数集
     * @return
     */
    public List<String> getCacheIgnoreParams() {
		return cacheIgnoreParams;
	}

	/**
	 * 设置计算缓存ID时候忽略的参数集
	 * @param cacheIgnoreParams
	 */
	public void setCacheIgnoreParams(List<String> cacheIgnoreParams) {
		this.cacheIgnoreParams = cacheIgnoreParams;
	}

    /**
     * Http Put请求构建器
     */
    public static class Builder{
        private HttpPutRequest httpRequest;

        /**
         * 创建一个Http Put请求构建器，同时你必须指定请求地址
         * @param baseUrl
         */
        public Builder(String baseUrl) {
            httpRequest = new HttpPutRequest();
            setBaseUrl(baseUrl);
        }

        /**
         * 创建一个Http Put请求构建器，同时你必须指定请求对象
         */
        public Builder(Request request){
            httpRequest = new HttpPutRequest();
            setRequest(request);
        }

        /**
         * 设置请求名称，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
         * @param name
         * @return
         */
        public Builder setName(String name) {
            httpRequest.setName(name);
            return this;
        }

        /**
         * 设置请求地址
         * @param baseUrl
         * @return
         */
        public Builder setBaseUrl(String baseUrl) {
            httpRequest.setBaseUrl(baseUrl);
            return this;
        }

        /**
         * 添加请求头
         * @param headers
         * @return
         */
        public Builder addHeader(Header... headers){
            httpRequest.addHeader(headers);
            return this;
        }

        /**
         * 设置请求头
         * @param headers
         * @return
         */
        public Builder setHeaders(List<Header> headers) {
            httpRequest.setHeaders(headers);
            return this;
        }

        /**
         * 添加请求参数
         * @param key
         * @param value
         * @return
         */
        public Builder addParam(String key, String value){
            httpRequest.addParam(key, value);
            return this;
        }

        /**
         * 添加请求参数
         * @param key
         * @param values
         * @return
         */
        public Builder addParam(String key, ArrayList<String> values){
            httpRequest.addParam(key, values);
            return this;
        }

        /**
         * 添加请求参数
         * @param key
         * @param value
         * @return
         */
        public Builder addParam(String key, File value){
            httpRequest.addParam(key, value);
            return this;
        }

        /**
         * 添加请求参数
         * @param key
         * @param value
         * @return
         */
        public Builder addParam(String key, InputStream value){
            httpRequest.addParam(key, value);
            return this;
        }

        /**
         * 添加请求参数
         * @param key
         * @param value
         * @param fileName
         * @return
         */
        public Builder addParam(String key, InputStream value, String fileName){
            httpRequest.addParam(key, value, fileName);
            return this;
        }

        /**
         * 添加请求参数
         * @param key
         * @param value
         * @param fileName
         * @param contentType
         * @return
         */
        public Builder addParam(String key, InputStream value, String fileName, String contentType){
            httpRequest.addParam(key, value, fileName, contentType);
            return this;
        }

        /**
         * 设置请求参数
         * @param params
         * @return
         */
        public Builder setParams(RequestParams params) {
            httpRequest.setParams(params);
            return this;
        }

        /**
         * 设置响应缓存配置
         * @param responseCache
         * @return
         */
        public Builder setResponseCache(ResponseCache responseCache) {
            httpRequest.setResponseCache(responseCache);
            return this;
        }
        
    	/**
    	 * 设置缓存忽略参数集
    	 * @param cacheIgnoreParams
    	 * @return
    	 */
    	public Builder setCacheIgnoreParams(List<String> cacheIgnoreParams) {
    		httpRequest.setCacheIgnoreParams(cacheIgnoreParams);
    		return this;
    	}

        /**
         * 设置请求对象，会从此请求对象身上解析所需的信息
         * @param request
         * @return
         */
        public Builder setRequest(Request request){
        	Class<? extends Request> requestClass = request.getClass();
            String requestName = RequestParser.parseName(requestClass);
            if(GeneralUtils.isNotEmpty(requestName)){
                httpRequest.setName(httpRequest.getName() + " "+requestName+" ");
            }
        	String url = RequestParser.parseBaseUrl(requestClass);
            if(GeneralUtils.isEmpty(url)){
                throw new IllegalArgumentException("你必须在Request上使有Url注解或者Host加Path注解指定请求地址");
            }
            httpRequest.setBaseUrl(url);
            
            httpRequest.setParams(RequestParser.parseRequestParams(request, httpRequest.getParams()));
            httpRequest.setCacheIgnoreParams(RequestParser.parseCacheIgnoreParams(requestClass));
            httpRequest.addHeader(RequestParser.parseRequestHeaders(request));
            ResponseCache responseCache = RequestParser.parseResponseCache(requestClass);
            if(responseCache != null){
                httpRequest.setResponseCache(responseCache);
            }
            return this;
        }

        /**
         * 设置请求实体
         * @param httpEntity
         * @return
         */
        public Builder setHttpEntity(HttpEntity httpEntity) {
            httpRequest.setHttpEntity(httpEntity);
            return this;
        }

        /**
         * 创建并返回Http Put请求
         * @return
         */
        public HttpPutRequest create(){
            return httpRequest;
        }
    }
}
