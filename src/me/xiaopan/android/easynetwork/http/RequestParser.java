package me.xiaopan.android.easynetwork.http;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.xiaopan.android.easynetwork.http.annotation.False;
import me.xiaopan.android.easynetwork.http.annotation.Header;
import me.xiaopan.android.easynetwork.http.annotation.Host;
import me.xiaopan.android.easynetwork.http.annotation.Method;
import me.xiaopan.android.easynetwork.http.annotation.Name;
import me.xiaopan.android.easynetwork.http.annotation.Param;
import me.xiaopan.android.easynetwork.http.annotation.Path;
import me.xiaopan.android.easynetwork.http.annotation.ResponseCache;
import me.xiaopan.android.easynetwork.http.annotation.True;
import me.xiaopan.android.easynetwork.http.annotation.Url;
import me.xiaopan.android.easynetwork.http.enums.MethodType;

/**
 * 请求解析器，用于解析继承于Request的请求对象
 * Created by XIAOPAN on 13-11-24.
 */
public class RequestParser {
    /**
     * 解析请求名称
     * @param requestClass 请求对象
     * @return 请求名称
     */
    public static String parseName(Class<? extends Request> requestClass){
        Name name = requestClass.getAnnotation(Name.class);
        return (name != null && GeneralUtils.isNotEmpty(name.value()))? name.value():null;
    }
    
    /**
     * 解析基本的请求地址，值得注意的是此方法返回的请求地址不包含请求参数（即使请求方式是GET或者DELETE）
     * @param requestClass 请求对象
     * @return 请求地址，null：requestClass为null或请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
     */
    public static String parseBaseUrl(Class<? extends Request> requestClass){
        StringBuffer stringBuffer = new StringBuffer();
    	
        /* 优先解析Url注解 */
        Url url = requestClass.getAnnotation(Url.class);
        String urlValue = url != null ? url.value().trim() : null;
        if(GeneralUtils.isNotEmpty(urlValue)){
        	stringBuffer.append(urlValue);
        }else{
        	/* 其次解析Host注解 */
        	if(stringBuffer.length() == 0){
        		Host host = requestClass.getAnnotation(Host.class);
        		String hostValue = host != null ? host.value().trim() : null;
        		if(GeneralUtils.isNotEmpty(hostValue)){
        			stringBuffer.append(hostValue);
        		}
        	}
        	
        	/* 最后解析Path注解 */
        	if(stringBuffer.length() > 0){
        		Path path = requestClass.getAnnotation(Path.class);
        		String pathValue = path != null ? path.value().trim() : null;
        		if(GeneralUtils.isNotEmpty(pathValue)){
        			stringBuffer.append("/");
        			stringBuffer.append(pathValue);
        		}
        	}
        }
        
        if(stringBuffer.length() > 0){
        	return stringBuffer.toString();
    	}else{
    		return null;
    	}
    }

    /**
     * 解析请求地址，值得注意的是只有此请求对象中静态字段才会被处理
     * @param requestClass 请求对象
     * @param extraRequestParams 额外的请求参数，如果此请求对象的请求方式是GET或者DELETE的话，此参数集就会被处理
     * @return 请求地址，null：requestClass为null或请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
     */
    public static String parseUrl(Class<? extends Request> requestClass, RequestParams extraRequestParams){
        StringBuffer stringBuffer = new StringBuffer();
    	
        /* 优先解析Url注解 */
        Url url = requestClass.getAnnotation(Url.class);
        String urlValue = url != null ? url.value().trim() : null;
        if(GeneralUtils.isNotEmpty(urlValue)){
        	stringBuffer.append(urlValue);
        }else{
        	/* 其次解析Host注解 */
        	if(stringBuffer.length() == 0){
        		Host host = requestClass.getAnnotation(Host.class);
        		String hostValue = host != null ? host.value().trim() : null;
        		if(GeneralUtils.isNotEmpty(hostValue)){
        			stringBuffer.append(hostValue);
        		}
        	}
        	
        	/* 最后解析Path注解 */
        	if(stringBuffer.length() > 0){
        		Path path = requestClass.getAnnotation(Path.class);
        		String pathValue = path != null ? path.value().trim() : null;
        		if(GeneralUtils.isNotEmpty(pathValue)){
        			stringBuffer.append("/");
        			stringBuffer.append(pathValue);
        		}
        	}
        }
        
        if(stringBuffer.length() > 0){
        	Method method = requestClass.getAnnotation(Method.class);
        	if(method == null || method.value() == MethodType.GET || method.value() == MethodType.DELETE){
        		RequestParams newParams = parseRequestParams(requestClass, extraRequestParams);
        		String paramString = newParams.getParamString();
        		if(GeneralUtils.isNotEmpty(paramString)){
        			stringBuffer.append(stringBuffer.indexOf("?") == -1?"?":"&");
        			stringBuffer.append(paramString);
        		}
    		}
        	return stringBuffer.toString();
    	}else{
    		return null;
    	}
    }

    /**
     * 解析请求地址，值得注意的是只有此请求对象中静态字段才会被处理
     * @param requestClass 请求对象
     * @return 请求地址，null：requestClass为null或请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
     */
    public static String parseUrl(Class<? extends Request> requestClass){
    	return parseUrl(requestClass, null);
    }

    /**
     * 解析请求地址
     * @param request 请求对象
     * @param extraRequestParams 额外的请求参数，如果此请求对象的请求方式是GET或者DELETE的话，此参数集就会被处理
     * @return 请求地址，null：requestClass为null或请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
     */
    public static String parseUrl(Request request, RequestParams extraRequestParams){
    	StringBuffer stringBuffer = new StringBuffer();
    	
        /* 优先解析Url注解 */
        Url url = request.getClass().getAnnotation(Url.class);
        String urlValue = url != null ? url.value().trim() : null;
        if(GeneralUtils.isNotEmpty(urlValue)){
        	stringBuffer.append(urlValue);
        }else{
        	/* 其次解析Host注解 */
    		Host host = request.getClass().getAnnotation(Host.class);
    		String hostValue = host != null ? host.value().trim() : null;
    		if(GeneralUtils.isNotEmpty(hostValue)){
    			stringBuffer.append(hostValue);
    		}
        	
        	/* 最后解析Path注解 */
        	if(stringBuffer.length() > 0){
        		Path path = request.getClass().getAnnotation(Path.class);
        		String pathValue = path != null ? path.value().trim() : null;
        		if(GeneralUtils.isNotEmpty(pathValue)){
        			stringBuffer.append("/");
        			stringBuffer.append(pathValue);
        		}
        	}
        }
        
        if(stringBuffer.length() > 0){
        	Method method = request.getClass().getAnnotation(Method.class);
        	if(method == null || method.value() == MethodType.GET || method.value() == MethodType.DELETE){
        		RequestParams newParams = parseRequestParams(request, extraRequestParams);
        		String paramString = newParams.getParamString();
        		if(GeneralUtils.isNotEmpty(paramString)){
        			stringBuffer.append(stringBuffer.indexOf("?") == -1?"?":"&");
        			stringBuffer.append(paramString);
        		}
    		}
        	return stringBuffer.toString();
    	}else{
    		return null;
    	}
    }

    /**
     * 解析请求地址
     * @param request 请求对象
     * @return 请求地址，null：requestClass为null或请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
     */
    public static String parseUrl(Request request){
    	return parseUrl(request, null);
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
     * @param requestClass 请求对象
     * @param requestParams 请求参数集，如果不为null的话，会将解析到的请求参数放入此请求参数集中，然后再返回否则就会创建新的请求参数集对象
     * @return 请求参数集
     */
    @SuppressWarnings("unchecked")
    public static RequestParams parseRequestParams(Class<? extends Request> requestClass, RequestParams requestParams){
        if(requestParams == null){
            requestParams = new RequestParams();
        }

        String paramValue;
        Object paramValueObject;
        for(Field field : GeneralUtils.getFields(requestClass, true, true, true)){
            if(field.getAnnotation(Param.class) != null && Modifier.isStatic(field.getModifiers())){	//如果当前字段被标记为需要序列化并且是静态的
                try {
                    field.setAccessible(true);
                    paramValueObject = field.get(null);
                    if(paramValueObject != null){
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
                            Param paramName = GeneralUtils.getAnnotationFromEnum(enumObject, Param.class);
                            if(paramName != null && GeneralUtils.isNotEmpty(paramName.value())){
                                requestParams.put(parseRequestParamKey(field), paramName.value());
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
     * 解析请求参数集
     * @param requestClass
     * @return 请求参数集
     */
    public static RequestParams parseRequestParams(Class<? extends Request> requestClass){
        return parseRequestParams(requestClass, null);
    }

    /**
     * 解析请求参数集
     * @param request 请求对象
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
                    paramValueObject = field.get(request);
                    if(paramValueObject != null){
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
                            Param paramName = GeneralUtils.getAnnotationFromEnum(enumObject, Param.class);
                            if(paramName != null && GeneralUtils.isNotEmpty(paramName.value())){
                                requestParams.put(parseRequestParamKey(field), paramName.value());
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
     * 解析请求参数集
     * @param request 请求对象
     * @return 请求参数集
     */
    public static RequestParams parseRequestParams(Request request){
        return parseRequestParams(request, null);
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
     * @param requestClass 请求对象
     * @return
     */
    public static me.xiaopan.android.easynetwork.http.ResponseCache parseResponseCache(Class<? extends Request> requestClass){
        ResponseCache responseCacheAnnotation = requestClass.getAnnotation(ResponseCache.class);
        if(responseCacheAnnotation != null){
            return new me.xiaopan.android.easynetwork.http.ResponseCache.Builder().setRefreshCache(responseCacheAnnotation.isRefreshCache()).setPeriodOfValidity(responseCacheAnnotation.periodOfValidity()).setRefreshCallback(responseCacheAnnotation.isRefreshCallback()).setCacheDirectory(responseCacheAnnotation.cacheDirectory()).create();
        }else{
            return null;
        }
    }
}
