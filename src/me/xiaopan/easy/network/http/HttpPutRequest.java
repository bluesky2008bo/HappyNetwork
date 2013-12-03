package me.xiaopan.easy.network.http;

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
    private String name;    //本次请求的名称，默认为当前时间
    private String url; //请求地址
    private List<Header> headers;   //请求头信息
    private RequestParams params;   //请求参数
    private ResponseCache responseCache;    //响应缓存配置
    private HttpEntity httpEntity;  //Http请求体

    private HttpPutRequest(){
        setName(GeneralUtils.getCurrentDateTimeByDefultFormat() + " PUT ");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void addHeader(Header header){
        if(header != null){
            if(this.headers == null){
                this.headers = new LinkedList<Header>();
            }
            this.headers.add(header);
        }
    }

    public void addHeaders(Header... headers){
        if(headers != null && headers.length > 0){
            if(this.headers == null){
                this.headers = new LinkedList<Header>();
            }
            for(Header header : headers){
                this.headers.add(header);
            }
        }
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public RequestParams getParams() {
        return params;
    }

    public void addParam(String key, String value){
        if(GeneralUtils.isNotEmpty(key, value)){
            if(params == null){
                params = new RequestParams();
            }
            params.put(key, value);
        }
    }

    public void addParam(String key, ArrayList<String> values){
        if(GeneralUtils.isNotEmpty(key) && values != null && values.size() > 0){
            if(params == null){
                params = new RequestParams();
            }
            params.put(key, values);
        }
    }

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

    public void addParam(String key, InputStream value){
        if(GeneralUtils.isNotEmpty(key) && value != null){
            if(params == null){
                params = new RequestParams();
            }
            params.put(key, value);
        }
    }

    public void addParam(String key, InputStream value, String fileName){
        if(GeneralUtils.isNotEmpty(key, fileName) && value != null){
            if(params == null){
                params = new RequestParams();
            }
            params.put(key, value, fileName);
        }
    }

    public void addParam(String key, InputStream value, String fileName, String contentType){
        if(GeneralUtils.isNotEmpty(key, fileName, contentType) && value != null){
            if(params == null){
                params = new RequestParams();
            }
            params.put(key, value, fileName, contentType);
        }
    }

    public void setParams(RequestParams params) {
        this.params = params;
    }

    public ResponseCache getResponseCache() {
        return responseCache;
    }

    public void setResponseCache(ResponseCache responseCache) {
        this.responseCache = responseCache;
    }

    public HttpEntity getHttpEntity() {
        return httpEntity;
    }

    public void setHttpEntity(HttpEntity httpEntity) {
        this.httpEntity = httpEntity;
    }

    /**
     * Http Put请求构建器
     */
    public static class Builder{
        private HttpPutRequest httpRequest;

        /**
         * 创建一个Http Put请求构建器，你必须指定请求地址
         * @param url
         */
        public Builder(String url) {
            httpRequest = new HttpPutRequest();
            setUrl(url);
        }

        /**
         * 创建一个Http Put请求构建器，你必须指定请求对象
         */
        public Builder(Request request){
            httpRequest = new HttpPutRequest();
            setRequest(request);
        }

        public Builder setName(String name) {
            httpRequest.setName(name);
            return this;
        }

        public Builder setUrl(String url) {
            httpRequest.setUrl(url);
            return this;
        }

        public Builder addHeader(Header header){
            httpRequest.addHeader(header);
            return this;
        }

        public Builder addHeaders(Header... headers){
            httpRequest.addHeaders(headers);
            return this;
        }

        public Builder setHeaders(List<Header> headers) {
            httpRequest.setHeaders(headers);
            return this;
        }

        public Builder addParam(String key, String value){
            httpRequest.addParam(key, value);
            return this;
        }

        public Builder addParam(String key, ArrayList<String> values){
            httpRequest.addParam(key, values);
            return this;
        }

        public Builder addParam(String key, File value){
            httpRequest.addParam(key, value);
            return this;
        }

        public Builder addParam(String key, InputStream value){
            httpRequest.addParam(key, value);
            return this;
        }

        public Builder addParam(String key, InputStream value, String fileName){
            httpRequest.addParam(key, value, fileName);
            return this;
        }

        public Builder addParam(String key, InputStream value, String fileName, String contentType){
            httpRequest.addParam(key, value, fileName, contentType);
            return this;
        }

        public Builder setParams(RequestParams params) {
            httpRequest.setParams(params);
            return this;
        }

        public Builder setResponseCache(ResponseCache responseCache) {
            httpRequest.setResponseCache(responseCache);
            return this;
        }

        public Builder setRequest(Request request){
        	RequestParser requestParser = new RequestParser(request);
            String requestName = requestParser.getName();
            if(GeneralUtils.isNotEmpty(requestName)){
                httpRequest.setName(httpRequest.getName() + " "+requestName+" ");
            }
        	String url = requestParser.getUrl();
            if(GeneralUtils.isEmpty(url)){
                throw new IllegalArgumentException("你必须在Request上使有Url注解或者Host加Path注解指定请求地址");
            }
            httpRequest.setUrl(url);
            httpRequest.setParams(requestParser.getParams(httpRequest.getParams()));
            httpRequest.addHeaders(requestParser.getHeaders());
            ResponseCache responseCache = requestParser.getResponseCache();
            if(responseCache != null){
                httpRequest.setResponseCache(responseCache);
            }
            return this;
        }

        public Builder setHttpEntity(HttpEntity httpEntity) {
            httpRequest.setHttpEntity(httpEntity);
            return this;
        }

        public HttpPutRequest create(){
            return httpRequest;
        }
    }
}
