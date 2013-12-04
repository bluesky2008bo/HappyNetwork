package me.xiaopan.easy.network.http;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.xiaopan.easy.network.http.annotation.False;
import me.xiaopan.easy.network.http.annotation.Host;
import me.xiaopan.easy.network.http.annotation.Path;
import me.xiaopan.easy.network.http.annotation.RequestHeader;
import me.xiaopan.easy.network.http.annotation.RequestName;
import me.xiaopan.easy.network.http.annotation.RequestParam;
import me.xiaopan.easy.network.http.annotation.ResponseCache;
import me.xiaopan.easy.network.http.annotation.True;
import me.xiaopan.easy.network.http.annotation.Url;

import org.apache.http.Header;

import com.google.gson.annotations.SerializedName;

/**
 * 请求解析器，用于解析继承于Request的请求对象
 * Created by XIAOPAN on 13-11-24.
 */
public class RequestParser {
    private  Request request;

    public RequestParser(Request request) {
        if(request == null){
        	throw new NullPointerException("request 不能为null");
        }
    	this.request = request;
    }

    public String getRequestName(){
        RequestName requestName = request.getClass().getAnnotation(RequestName.class);
        return (requestName != null && GeneralUtils.isNotEmpty(requestName.value()))?requestName.value():null;
    }

    /**
     * 获取URL
     * @return
     */
    public String getUrl(){
        return getUrl(request.getClass());
    }

    /**
     * 获取参数集
     * @param requestParams 请求参数集，如果为null，请自动创建
     * @return
     */
    @SuppressWarnings("unchecked")
	public RequestParams getRequestParams(RequestParams requestParams){
        if(requestParams == null){
        	requestParams = new RequestParams();
        }
        
        String paramValue;
        Object paramValueObject;
        for(Field field : GeneralUtils.getFields(request.getClass(), true, true, true)){
            if(field.getAnnotation(RequestParam.class) != null){	//如果当前字段被标记为需要序列化
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
                            requestParams.put(getRequestParamKey(field), (File) paramValueObject);
                        }else if(paramValueObject instanceof ArrayList){	//如果当前字段是ArrayList，就将其作为一个ArrayList添加到请求参水集中
                            requestParams.put(getRequestParamKey(field), (ArrayList<String>) paramValueObject);
                        }else if(paramValueObject instanceof Boolean){	//如果当前字段是boolean
                            if((Boolean) paramValueObject){
                                True true1 = field.getAnnotation(True.class);
                                if(true1 != null && GeneralUtils.isNotEmpty(true1.value())){
                                    requestParams.put(getRequestParamKey(field), true1.value());
                                }else{
                                    requestParams.put(getRequestParamKey(field), paramValueObject.toString());
                                }
                            }else{
                                False false1 = field.getAnnotation(False.class);
                                if(false1 != null && GeneralUtils.isNotEmpty(false1.value())){
                                    requestParams.put(getRequestParamKey(field), false1.value());
                                }else{
                                    requestParams.put(getRequestParamKey(field), paramValueObject.toString());
                                }
                            }
                        }else if(paramValueObject instanceof Enum){	//如果当前字段是枚举
                            Enum<?> enumObject = (Enum<?>) paramValueObject;
                            SerializedName serializedName = GeneralUtils.getAnnotationFromEnum(enumObject, SerializedName.class);
                            if(serializedName != null && GeneralUtils.isNotEmpty(serializedName.value())){
                                requestParams.put(getRequestParamKey(field), serializedName.value());
                            }else{
                                requestParams.put(getRequestParamKey(field), enumObject.name());
                            }
                        }else{	//如果以上几种情况都不是就直接转为字符串添加到请求参数集中
                            paramValue = paramValueObject.toString();
                            if(GeneralUtils.isNotEmpty(paramValue)){
                                requestParams.put(getRequestParamKey(field), paramValue);
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
     * 获取参数集
     * @return
     */
    public RequestParams getRequestParams(){
        return getRequestParams(null);
    }
    
    /**
     * 获取请求头
     * @return
     */
    @SuppressWarnings("unchecked")
	public Header[] getRequestHeaders(){
    	List<Header> finalHeaders = new LinkedList<Header>();
    	for(Field field : GeneralUtils.getFields(request.getClass(), true, true, true)){
    		field.setAccessible(true);
    		if(field.getAnnotation(RequestHeader.class) != null){	//如果当前字段被标记为需要序列化
            	if(Header.class.isAssignableFrom(field.getType())){	//如果是单个
            		try {
						finalHeaders.add((Header) field.get(request));
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
                }else if(GeneralUtils.isArrayByType(field, Header.class)){	//如果Header数组
            	   try {
						Header[] headers = (Header[]) field.get(request);
						for(Header header : headers){
							finalHeaders.add(header);
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
               }else if(GeneralUtils.isCollectionByType(field, Collection.class, Header.class)){	//如果是Header集合
            	   try {
						finalHeaders.addAll((Collection<Header>) field.get(request));
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
               }
            }
        }
    	
    	if(finalHeaders.size() > 0){
    		Header[] heades = new Header[finalHeaders.size()];
    		finalHeaders.toArray(heades);
    		return heades;
    	}else{
    		return null;
    	}
    }

    /**
     * 获取响应缓存配置信息
     * @return
     */
    public me.xiaopan.easy.network.http.ResponseCache getResponseCache(){
        ResponseCache responseCacheAnnotation = request.getClass().getAnnotation(ResponseCache.class);
        if(responseCacheAnnotation != null){
            return new me.xiaopan.easy.network.http.ResponseCache.Builder().setRefreshCache(responseCacheAnnotation.isRefreshCache()).setPeriodOfValidity(responseCacheAnnotation.periodOfValidity()).setRefreshCallback(responseCacheAnnotation.isRefreshCallback()).setCacheDirectory(responseCacheAnnotation.cacheDirectory()).create();
        }else{
            return null;
        }
    }

    /**
     * 获取参数名
     * @param field
     * @return
     */
    public static final String getRequestParamKey(Field field){
        RequestParam param = field.getAnnotation(RequestParam.class);
        if(param != null && GeneralUtils.isNotEmpty(param.value())){
            return param.value();
        }else{
            return field.getName();
        }
    }

    /**
     * 通过解析一个请求对象来获取请求地址
     * @param requestClass 请求对象
     * @return 请求地址，null：requestClass为null或请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
     */
    public static final String getUrl(Class<?> requestClass){
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
}
