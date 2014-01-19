package me.xiaopan.android.easynetwork.http;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;

/**
 * Http Delete请求
 * Created by XIAOPAN on 13-11-23.
 */
public class HttpDeleteRequest {
    private String name;    //本次请求的名称，默认为当前时间，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
    private String url; //请求地址
    private List<Header> headers;   //请求头信息
    private ResponseCache responseCache;    //响应缓存配置

    private HttpDeleteRequest(){
        setName(GeneralUtils.getCurrentDateTimeByDefultFormat() + " DELETE ");
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
     * Http Delete 请求构建器
     */
    public static class Builder{
        private HttpDeleteRequest httpRequest;

        /**
         * 创建一个Http Delete请求构建器，同时你必须指定请求地址
         * @param url
         */
        public Builder(String url) {
            httpRequest = new HttpDeleteRequest();
            setUrl(url);
        }

        /**
         * 创建一个Http Delete请求构建器，同时你必须指定请求对象
         * @param request
         */
        public Builder(Request request) {
            httpRequest = new HttpDeleteRequest();
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
            httpRequest.addHeader(requestParser.getRequestHeaders());
            ResponseCache responseCache = requestParser.getResponseCache();
            if(responseCache != null){
                httpRequest.setResponseCache(responseCache);
            }
            return this;
        }

        /**
         * 创建并返回Http Delete请求
         * @return
         */
        public HttpDeleteRequest create(){
            return httpRequest;
        }
    }
}
