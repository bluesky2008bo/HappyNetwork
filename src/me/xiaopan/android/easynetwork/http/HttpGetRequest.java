package me.xiaopan.android.easynetwork.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;

/**
 * Http Get请求
 * Created by XIAOPAN on 13-11-23.
 */
public class HttpGetRequest {
    private String name;    //本次请求的名称，默认为当前时间，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
    private String url; //请求地址
    private List<Header> headers;   //请求头信息
    private RequestParams params;   //请求参数
    private ResponseCache responseCache;    //响应缓存配置

    private HttpGetRequest(){
        setName(GeneralUtils.getCurrentDateTimeByDefultFormat() + " GET ");
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
    public String getUrl() {
        return url;
    }

    /**
     * 设置请求地址
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
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
     * Http Get 请求构建器
     */
    public static class Builder{
        private HttpGetRequest httpRequest;

        /**
         * 创建一个Http Get请求构建器，同时你必须指定请求地址
         * @param url
         */
        public Builder(String url) {
            httpRequest = new HttpGetRequest();
            setUrl(url);
        }

        /**
         * 创建一个Http Get请求构建器，同时你必须指定请求对象
         */
        public Builder(Request request){
            httpRequest = new HttpGetRequest();
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
         * @param url
         * @return
         */
        public Builder setUrl(String url) {
            httpRequest.setUrl(url);
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
         * 设置请求对象，会从此请求对象身上解析所需的信息
         * @param request
         * @return
         */
        public Builder setRequest(Request request){
        	String requestName = RequestParser.parseName(request.getClass());
            if(GeneralUtils.isNotEmpty(requestName)){
                httpRequest.setName(httpRequest.getName() + " "+requestName+" ");
            }
            
            String baseUrl = RequestParser.parseBaseUrl(request.getClass());
            if(GeneralUtils.isEmpty(baseUrl)){
                throw new IllegalArgumentException("你必须在Request上使用Url注解或者Host加Path注解指定请求地址");
            }
            httpRequest.setUrl(baseUrl);
            
            httpRequest.setParams(RequestParser.parseRequestParams(request, httpRequest.getParams()));
            httpRequest.addHeader(RequestParser.parseRequestHeaders(request));
            ResponseCache responseCache = RequestParser.parseResponseCache(request.getClass());
            if(responseCache != null){
                httpRequest.setResponseCache(responseCache);
            }
            return this;
        }

        /**
         * 创建并返回Http Get请求
         * @return
         */
        public HttpGetRequest create(){
            return httpRequest;
        }
    }
}
