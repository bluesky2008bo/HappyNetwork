/*
 * Copyright 2013 Peng fei Pan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.xiaopan.easy.network.android;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.xiaopan.easy.network.android.http.EasyHttpClient;
import me.xiaopan.easy.network.android.http.False;
import me.xiaopan.easy.network.android.http.Host;
import me.xiaopan.easy.network.android.http.HttpHeaderUtils;
import me.xiaopan.easy.network.android.http.Path;
import me.xiaopan.easy.network.android.http.Request;
import me.xiaopan.easy.network.android.http.RequestParams;
import me.xiaopan.easy.network.android.http.True;
import me.xiaopan.easy.network.android.http.Url;
import me.xiaopan.easy.network.android.http.headers.ContentType;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EasyNetworkUtils {
	public static final String DEFAULT_CHARSET = "UTF-8";
	
	/**
	 * 把给定的字符串用给定的字符分割
	 * @param string 给定的字符串
	 * @param ch 给定的字符
	 * @return 分割后的字符串数组
	 */
	public static String[] split(String string, char ch) {
		ArrayList<String> stringList = new ArrayList<String>();
		char chars[] = string.toCharArray();
		int nextStart = 0;
		for (int w = 0; w < chars.length; w++){
			if (ch == chars[w]) {
				stringList.add(new String(chars, nextStart, w-nextStart));
				nextStart = w+1;
				if(nextStart == chars.length){	//当最后一位是分割符的话，就再添加一个空的字符串到分割数组中去
					stringList.add("");
				}
			}
		}
		if(nextStart < chars.length){	//如果最后一位不是分隔符的话，就将最后一个分割符到最后一个字符中间的左右字符串作为一个字符串添加到分割数组中去
			stringList.add(new String(chars,nextStart, chars.length-1-nextStart+1));
		}
		return stringList.toArray(new String[stringList.size()]);
	}
	
	/**
	 * 判断给定的字符串是否不为null且不为空
	 * @param string 给定的字符串
	 * @return 
	 */
	public static boolean isNotNullAndEmpty(String string){
		return string != null && !"".equals(string.trim());
	}
	
	/**
	 * 判断给定的字符串数组中是否全部都不为null且不为空
	 * @param strings 给定的字符串数组
	 * @return 是否全部都不为null且不为空
	 */
	public static boolean isNotNullAndEmpty(String... strings){
		boolean result = true;
		for(String string : strings){
			if(string == null || "".equals(string.trim())){
				result = false;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 获取一个枚举上的指定类型的注解
	 * @param enumObject 给定的枚举
	 * @param annoitaion 指定类型的注解
	 * @return
	 */
	public static final <T extends Annotation> T getAnnotationFromEnum(Enum<?> enumObject, Class<T> annoitaion){
		try {
			return (T) enumObject.getClass().getField(enumObject.name()).getAnnotation(annoitaion);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取响应编码，首先会尝试从响应体的Content-Type中获取，如果获取不到的话就返回默认的UTF-8
	 * @param httpResponse
	 * @return
	 */
	public static final String getResponseCharset(HttpResponse httpResponse){
		ContentType contentType = HttpHeaderUtils.getContentType(httpResponse);
		if(contentType != null){
			return contentType.getCharset(DEFAULT_CHARSET);
		}else{
			return DEFAULT_CHARSET;
		}
	}
	
	/**
	 * 获取给定的类所有的父类
	 * @param clas 给定的类
	 * @param isAddCurrentClass 是否将当年类放在最终返回的父类列表的首位
	 * @return 给定的类所有的父类
	 */
	public static List<Class<?>> getSuperClasss(Class<?> sourceClass, boolean isAddCurrentClass){
		List<Class<?>> classList = new ArrayList<Class<?>>();
		Class<?> classs;
		if(isAddCurrentClass){
			classs = sourceClass;
		}else{
			classs = sourceClass.getSuperclass();
		}
		while(classs != null){
			classList.add(classs);
			classs = classs.getSuperclass();
		}
		return classList;
	}
	
	/**
	 * 获取给定类的所有字段
	 * @param sourceClass 给定的类
	 * @param isGetDeclaredField 是否需要获取Declared字段
	 * @param isFromSuperClassGet 是否需要把其父类中的字段也取出
	 * @param isDESCGet 在最终获取的列表里，父类的字段是否需要排在子类的前面。只有需要把其父类中的字段也取出时此参数才有效
	 * @return 给定类的所有字段
	 */
	public static List<Field> getFields(Class<?> sourceClass, boolean isGetDeclaredField, boolean isFromSuperClassGet, boolean isDESCGet){
		List<Field> fieldList = new ArrayList<Field>();
		//如果需要从父类中获取
		if(isFromSuperClassGet){
			//获取当前类的所有父类
			List<Class<?>> classList = getSuperClasss(sourceClass, true);
			
			//如果是降序获取
			if(isDESCGet){
				for(int w = classList.size()-1; w > -1; w--){
					for(Field field : isGetDeclaredField ? classList.get(w).getDeclaredFields() : classList.get(w).getFields()){
						fieldList.add(field);
					}
				}
			}else{
				for(int w = 0; w < classList.size(); w++){
					for(Field field : isGetDeclaredField ? classList.get(w).getDeclaredFields() : classList.get(w).getFields()){
						fieldList.add(field);
					}
				}
			}
		}else{
			for(Field field : isGetDeclaredField ? sourceClass.getDeclaredFields() : sourceClass.getFields()){
				fieldList.add(field);
			}
		}
		return fieldList;
	}
	
	/**
	 * 将一个请求对象转换为RequestParams对象
	 * @param request 请求对象
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public static RequestParams requestToRequestParams(Request request){
		if(request != null){
			RequestParams requestParams = new RequestParams();
			
			//循环处理所有字段
			String paramValue;
			Object paramValueObject;
			for(Field field : getFields(request.getClass(), true, true, true)){
				if(field.getAnnotation(Expose.class) != null){	//如果当前字段被标记为需要序列化
					try {
						field.setAccessible(true);
						if((paramValueObject = field.get(request)) != null){
							if(paramValueObject instanceof Map){	//如果当前字段是一个MAP，就取出其中的每一项添加到请求参数集中
								Map<Object, Object> map = (Map<Object, Object>)paramValueObject;
								for(java.util.Map.Entry<Object, Object> entry : map.entrySet()){
									if(entry.getKey() != null && entry.getValue() != null && isNotNullAndEmpty(entry.getKey().toString(), entry.getValue().toString())){
										requestParams.put(entry.getKey().toString(), entry.getValue().toString());	
									}
								}
							}else if(paramValueObject instanceof File){	//如果当前字段是一个文件，就将其作为一个文件添加到请求参水集中
								requestParams.put(getParamKey(field), (File) paramValueObject);
							}else if(paramValueObject instanceof ArrayList){	//如果当前字段是ArrayList，就将其作为一个ArrayList添加到请求参水集中
								requestParams.put(getParamKey(field), (ArrayList<String>) paramValueObject);
							}else if(paramValueObject instanceof Boolean){	//如果当前字段是boolean
								if((Boolean) paramValueObject){
									True true1 = field.getAnnotation(True.class);
									if(true1 != null && isNotNullAndEmpty(true1.value())){
										requestParams.put(getParamKey(field), true1.value());
									}else{
										requestParams.put(getParamKey(field), paramValueObject.toString());
									}
								}else{
									False false1 = field.getAnnotation(False.class);
									if(false1 != null && isNotNullAndEmpty(false1.value())){
										requestParams.put(getParamKey(field), false1.value());
									}else{
										requestParams.put(getParamKey(field), paramValueObject.toString());
									}
								}
							}else if(paramValueObject instanceof Enum){	//如果当前字段是枚举
								Enum<?> enumObject = (Enum<?>) paramValueObject;
								SerializedName serializedName = EasyNetworkUtils.getAnnotationFromEnum(enumObject, SerializedName.class);
								if(serializedName != null && isNotNullAndEmpty(serializedName.value())){
									requestParams.put(getParamKey(field), serializedName.value());
								}else{
									requestParams.put(getParamKey(field), enumObject.name());
								}
							}else{	//如果以上几种情况都不是就直接转为字符串添加到请求参数集中
								paramValue = paramValueObject.toString();
								if(isNotNullAndEmpty(paramValue)){
									requestParams.put(getParamKey(field), paramValue);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			return requestParams;
		}else{
			return null;
		}
	}
	
	/**
	 * 获取参数名
	 * @param field
	 * @return
	 */
	public static final String getParamKey(Field field){
		SerializedName serializedName = field.getAnnotation(SerializedName.class);
		if(serializedName != null && isNotNullAndEmpty(serializedName.value())){
			return serializedName.value();
		}else{
			return field.getName();
		}
	}
	
	/**
	 * 判断给定的字符串是否以一个特定的字符串开头，忽略大小写
	 * @param sourceString 给定的字符串
	 * @param newString 一个特定的字符串
	 * @return 
	 */
	public static boolean startsWithIgnoreCase(String sourceString, String newString){
		int newLength = newString.length();
		int sourceLength = sourceString.length();
		if(newLength == sourceLength){
			return newString.equalsIgnoreCase(sourceString);
		}else if(newLength < sourceLength){
			char[] newChars = new char[newLength];
			sourceString.getChars(0, newLength, newChars, 0);
			return newString.equalsIgnoreCase(String.valueOf(newChars));
		}else{
			return false;
		}
	}
	
	/**
	 * 判断给定的字符串是否以一个特定的字符串结尾，忽略大小写
	 * @param sourceString 给定的字符串
	 * @param newString 一个特定的字符串
	 * @return 
	 */
	public static boolean endsWithIgnoreCase(String sourceString, String newString){
		int newLength = newString.length();
		int sourceLength = sourceString.length();
		if(newLength == sourceLength){
			return newString.equalsIgnoreCase(sourceString);
		}else if(newLength < sourceLength){
			char[] newChars = new char[newLength];
			sourceString.getChars(sourceLength - newLength, sourceLength, newChars, 0);
			return newString.equalsIgnoreCase(String.valueOf(newChars));
		}else{
			return false;
		}
	}
	
	/**
	 * 通过解析一个请求对象来获取请求地址
	 * @param priorUrl 默认的请求地址，如果priorUrl不为null也不为空将直接返回priorUrl
	 * @param requestObject 请求对象
	 * @return 请求地址
	 * @throws Exception 请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
	 */
	public static final String getUrlFromRequestObject(String priorUrl, Object requestObject) throws Exception{
		if(isNotNullAndEmpty(priorUrl)){
			return priorUrl;
		}else{
			Class<?> requestClass = requestObject.getClass();
			
			/* 优先使用Url注解的值作为请求地址，如果没有Url注解再去用Host和Path注解来组合请求地址 */
			Url url = requestClass.getAnnotation(Url.class);
			String urlValue = url != null ? url.value() : null;
			if(isNotNullAndEmpty(urlValue)){
				return urlValue;
			}else{
				/* 如果有Host注解就继续，否则抛异常 */
				Host host = requestClass.getAnnotation(Host.class);
				String hostValue = host != null ? host.value() : null;
				if(isNotNullAndEmpty(hostValue)){
					/* 如果有Path注解就用Host注解的值拼接上Path注解的值作为请求地址，否则就只使用Host注解的值来作为请求地址 */
					Path path = requestClass.getAnnotation(Path.class);
					String pathValue = path != null ? path.value() : null;
					if(isNotNullAndEmpty(pathValue)){
						return hostValue + "/" + pathValue;
					}else{
						return hostValue;
					}
				}else{
					throw new Exception(requestClass.getName()+"上既没有Url注解（或者值为空）也没有Host注解（或者值为空）");
				}
			}
		}
	}
	
	/**
	 * 通过解析一个请求对象来获取请求地址
	 * @param requestObject 请求对象
	 * @return 请求地址，例如：http://www.baidu.com/index.html
	 * @throws Exception 请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
	 */
	public static final String getUrlFromRequestObject(Object requestObject) throws Exception{
		return getUrlFromRequestObject(null, requestObject);
	}
	
	public static final String getUrlWithQueryString(String url, RequestParams params) {
        if(params != null) {
            String paramString = params.getParamString();
            if(isNotNullAndEmpty(paramString)){
            	if (url.indexOf("?") == -1) {
            		url += "?" + paramString;
            	} else {
            		url += "&" + paramString;
            	}
            }
        }
        return url;
    }
    
	public static final HttpEntity paramsToEntity(RequestParams params) {
		EasyHttpClient.log("请求参数："+params.getParamString());
		return params != null?params.getEntity():null;
    }

	public static final HttpEntityEnclosingRequestBase setEntity(HttpEntityEnclosingRequestBase requestBase, HttpEntity entity, Header[] headers) {
        if(entity != null){
            requestBase.setEntity(entity);
        }
        if(headers != null){
        	requestBase.setHeaders(headers);
        }
        return requestBase;
    }

	public static final HttpEntityEnclosingRequestBase setEntity(HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {
        if(entity != null){
            requestBase.setEntity(entity);
        }
        return requestBase;
    }
	
	public static final HttpRequestBase setHeaders(HttpRequestBase httpRequest, Header[] headers){
		if(headers != null){
			httpRequest.setHeaders(headers);
		}
		return httpRequest;
	}
}
