package me.xiaopan.easy.network.http;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.xiaopan.easy.network.http.annotation.False;
import me.xiaopan.easy.network.http.annotation.Header;
import me.xiaopan.easy.network.http.annotation.Host;
import me.xiaopan.easy.network.http.annotation.Name;
import me.xiaopan.easy.network.http.annotation.Param;
import me.xiaopan.easy.network.http.annotation.Path;
import me.xiaopan.easy.network.http.annotation.ResponseCache;
import me.xiaopan.easy.network.http.annotation.True;
import me.xiaopan.easy.network.http.annotation.Url;

import com.google.gson.annotations.SerializedName;

/**
 * 请求解析器，用于解析继承于Request的请求对象
 * Created by XIAOPAN on 13-11-24.
 */
public class RequestParser {
    /**
     * 请求对象
     */
    private  Request request;

    /**
     * 创建一个请求解析器
     * @param request 请求对象
     */
    public RequestParser(Request request) {
        if(request == null){
        	throw new NullPointerException("request 不能为null");
        }
    	this.request = request;
    }

    /**
     * 获取请求名称
     * @return 请求名称
     */
    public String getName(){
        return RequestParser.parseName(request);
    }

    /**
     * 获取请求地址
     * @return 请求地址
     */
    public String getUrl(){
        return RequestParser.parseUrl(request);
    }

    /**
     * 获取请求参数集
     * @param requestParams 请求参数集，如果不为null的话，会将解析到的请求参数放入此请求参数集中，然后再返回否则就会创建新的请求参数集对象
     * @return 请求参数集
     */
    public RequestParams getRequestParams(RequestParams requestParams){
        return RequestParser.parseRequestParams(request, requestParams);
    }

    /**
     * 获取请求头
     * @return 请求头
     */
    public org.apache.http.Header[] getRequestHeaders(){
        return RequestParser.parseRequestHeaders(request);
    }

    /**
     * 获取响应缓存配置信息
     * @return 响应缓存配置信息
     */
    public me.xiaopan.easy.network.http.ResponseCache getResponseCache(){
        return RequestParser.parseResponseCache(request);
    }

    /**
     * 解析请求名称
     * @param request 请求对象
     * @return 请求名称
     */
    public static String parseName(Request request){
        Name name = request.getClass().getAnnotation(Name.class);
        return (name != null && GeneralUtils.isNotEmpty(name.value()))? name.value():null;
    }

    /**
     * 解析请求地址
     * @param request 请求对象
     * @return 请求地址，null：requestClass为null或请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
     */
    public static String parseUrl(Request request){
        Class<?> requestClass = request.getClass();
        /* 优先使用Url注解的值作为请求地址，如果没有Url注解再去用Host和Path注解来组合请求地址 */
        Url url = requestClass.getAnnotation(Url.class);
        String urlValue = url != null ? url.value().trim() : null;
        if(GeneralUtils.isNotEmpty(urlValue)){
            return urlValue;
        }else{
            /* 如果有Host注解就继续，否则抛异常 */
            Host host = requestClass.getAnnotation(Host.class);
            String hostValue = host != null ? host.value().trim() : null;
            if(GeneralUtils.isNotEmpty(hostValue)){
                /* 如果有Path注解就用Host注解的值拼接上Path注解的值作为请求地址，否则就只使用Host注解的值来作为请求地址 */
                Path path = requestClass.getAnnotation(Path.class);
                String pathValue = path != null ? path.value().trim() : null;
                if(GeneralUtils.isNotEmpty(pathValue)){
                    return hostValue + "/" + pathValue;
                }else{
                    return hostValue;
                }
            }else{
                return null;
            }
        }
    }

    /**
     * 解析请求参数名
     * @param field 字段
     * @return 请求参数名
     */
    public static String parseRequestParamKey(Field field){
        Param param = field.getAnnotation(Param.class);
        if(param != null && GeneralUtils.isNotEmpty(param.value())){
            return param.value();
        }else{
            return field.getName();
        }
    }

    /**
     * 解析请求参数集
     * @param request 请求对象集
     * @param requestParams 请求参数集，如果不为null的话，会将解析到的请求参数放入此请求参数集中，然后再返回否则就会创建新的请求参数集对象
     * @return 请求参数集
     */
    @SuppressWarnings("unchecked")
    public static RequestParams parseRequestParams(Request request, RequestParams requestParams){
        if(requestParams == null){
            requestParams = new RequestParams();
        }

        String paramValue;
        Object paramValueObject;
        for(Field field : GeneralUtils.getFields(request.getClass(), true, true, true)){
            if(field.getAnnotation(Param.class) != null){	//如果当前字段被标记为需要序列化
                try {
                    field.setAccessible(true);
                    if((paramValueObject = field.get(request)) != null){
                        if(paramValueObject instanceof Map){	//如果当前字段是一个MAP，就取出其中的每一项添加到请求参数集中
                            Map<Object, Object> map = (Map<Object, Object>)paramValueObject;
                            for(java.util.Map.Entry<Object, Object> entry : map.entrySet()){
                                if(entry.getKey() != null && entry.getValue() != null && GeneralUtils.isNotEmpty(entry.getKey().toString(), entry.getValue().toString())){
                                    requestParams.put(entry.getKey().toString(), entry.getValue().toString());
                                }
                            }
                        }else if(paramValueObject instanceof File){	//如果当前字段是一个文件，就将其作为一个文件添加到请求参水集中
                            requestParams.put(parseRequestParamKey(field), (File) paramValueObject);
                        }else if(paramValueObject instanceof ArrayList){	//如果当前字段是ArrayList，就将其作为一个ArrayList添加到请求参水集中
                            requestParams.put(parseRequestParamKey(field), (ArrayList<String>) paramValueObject);
                        }else if(paramValueObject instanceof Boolean){	//如果当前字段是boolean
                            if((Boolean) paramValueObject){
                                True trueAnnotation = field.getAnnotation(True.class);
                                if(trueAnnotation != null && GeneralUtils.isNotEmpty(trueAnnotation.value())){
                                    requestParams.put(parseRequestParamKey(field), trueAnnotation.value());
                                }else{
                                    requestParams.put(parseRequestParamKey(field), "true");
                                }
                            }else{
                                False falseAnnotation = field.getAnnotation(False.class);
                                if(falseAnnotation != null && GeneralUtils.isNotEmpty(falseAnnotation.value())){
                                    requestParams.put(parseRequestParamKey(field), falseAnnotation.value());
                                }else{
                                    requestParams.put(parseRequestParamKey(field), "false");
                                }
                            }
                        }else if(paramValueObject instanceof Enum){	//如果当前字段是枚举
                            Enum<?> enumObject = (Enum<?>) paramValueObject;
                            SerializedName serializedName = GeneralUtils.getAnnotationFromEnum(enumObject, SerializedName.class);
                            if(serializedName != null && GeneralUtils.isNotEmpty(serializedName.value())){
                                requestParams.put(parseRequestParamKey(field), serializedName.value());
                            }else{
                                requestParams.put(parseRequestParamKey(field), enumObject.name());
                            }
                        }else{	//如果以上几种情况都不是就直接转为字符串添加到请求参数集中
                            paramValue = paramValueObject.toString();
                            if(GeneralUtils.isNotEmpty(paramValue)){
                                requestParams.put(parseRequestParamKey(field), paramValue);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return requestParams;
    }

    /**
     * 解析请求头
     * @param request 请求对象
     * @return 请求头
     */
    @SuppressWarnings("unchecked")
    public static org.apache.http.Header[] parseRequestHeaders(Request request){
        List<org.apache.http.Header> finalHeaders = new LinkedList<org.apache.http.Header>();
        for(Field field : GeneralUtils.getFields(request.getClass(), true, true, true)){
            field.setAccessible(true);
            if(field.getAnnotation(Header.class) != null){	//如果当前字段被标记为需要序列化
                if(org.apache.http.Header.class.isAssignableFrom(field.getType())){	//如果是单个
                    try {
                        finalHeaders.add((org.apache.http.Header) field.get(request));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }else if(GeneralUtils.isArrayByType(field, org.apache.http.Header.class)){	//如果Header数组
                    try {
                        org.apache.http.Header[] headers = (org.apache.http.Header[]) field.get(request);
                        for(org.apache.http.Header header : headers){
                            finalHeaders.add(header);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }else if(GeneralUtils.isCollectionByType(field, Collection.class, org.apache.http.Header.class)){	//如果是Header集合
                    try {
                        finalHeaders.addAll((Collection<org.apache.http.Header>) field.get(request));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if(finalHeaders.size() > 0){
            org.apache.http.Header[] heades = new org.apache.http.Header[finalHeaders.size()];
            finalHeaders.toArray(heades);
            return heades;
        }else{
            return null;
        }
    }

    /**
     * 解析响应缓存配置信息
     * @param request 请求对象
     * @return
     */
    public static me.xiaopan.easy.network.http.ResponseCache parseResponseCache(Request request){
        ResponseCache responseCacheAnnotation = request.getClass().getAnnotation(ResponseCache.class);
        if(responseCacheAnnotation != null){
            return new me.xiaopan.easy.network.http.ResponseCache.Builder().setRefreshCache(responseCacheAnnotation.isRefreshCache()).setPeriodOfValidity(responseCacheAnnotation.periodOfValidity()).setRefreshCallback(responseCacheAnnotation.isRefreshCallback()).setCacheDirectory(responseCacheAnnotation.cacheDirectory()).create();
        }else{
            return null;
        }
    }
}
