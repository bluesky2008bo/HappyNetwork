package me.xiaopan.easy.network.http;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;

/**
 * Http Delete请求
 * Created by XIAOPAN on 13-11-23.
 */
public class HttpDeleteRequest {
    private String name;    //本次请求的名称，默认为当前时间
    private String url; //请求地址
    private List<Header> headers;   //请求头信息
    private ResponseCache responseCache;    //响应缓存配置

    private HttpDeleteRequest(){
        setName(GeneralUtils.getCurrentDateTimeByDefultFormat() + " DELETE ");
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

    public ResponseCache getResponseCache() {
        return responseCache;
    }

    public void setResponseCache(ResponseCache responseCache) {
        this.responseCache = responseCache;
    }

    /**
     * Http Delete 请求构建器
     */
    public static class Builder{
        private HttpDeleteRequest httpRequest;

        /**
         * 创建一个Http Delete请求构建器，你必须指定请求地址
         * @param url
         */
        public Builder(String url) {
            httpRequest = new HttpDeleteRequest();
            setUrl(url);
        }

        /**
         * 创建一个Http Delete请求构建器，你必须指定继承于Request的class
         * @param request
         */
        public Builder(Request request) {
            httpRequest = new HttpDeleteRequest();
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

        public Builder setResponseCache(ResponseCache responseCache) {
            httpRequest.setResponseCache(responseCache);
            return this;
        }

        public Builder setRequest(Request request){
        	RequestParser requestParser = new RequestParser(request);
            String requestName = requestParser.getRequestName();
            if(GeneralUtils.isNotEmpty(requestName)){
                httpRequest.setName(httpRequest.getName() + " "+requestName+" ");
            }
        	String url = requestParser.getUrl();
            if(GeneralUtils.isEmpty(url)){
                throw new IllegalArgumentException("你必须在Request上使有Url注解或者Host加Path注解指定请求地址");
            }
            httpRequest.setUrl(url);
            httpRequest.addHeaders(requestParser.getRequestHeaders());
            ResponseCache responseCache = requestParser.getResponseCache();
            if(responseCache != null){
                httpRequest.setResponseCache(responseCache);
            }
            return this;
        }

        public HttpDeleteRequest create(){
            return httpRequest;
        }
    }
}
