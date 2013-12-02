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
package me.xiaopan.easy.network.http;

import java.util.List;

import me.xiaopan.easy.java.util.StringUtils;
import me.xiaopan.easy.network.EasyNetwork;
import me.xiaopan.easy.network.http.headers.ContentType;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

public class HttpUtils {
	/**
	 * 获取响应编码，首先会尝试从响应体的Content-Type中获取，如果获取不到的话就返回默认的UTF-8
	 * @param httpResponse
	 * @return
	 */
	public static final String getResponseCharset(HttpResponse httpResponse){
		ContentType contentType = ContentType.getContentType(httpResponse);
		if(contentType != null){
			return contentType.getCharset(EasyNetwork.CHARSET_NAME_UTF8);
		}else{
			return EasyNetwork.CHARSET_NAME_UTF8;
		}
	}
	
	/**
	 * 拼接Url和参数
	 * @param url
	 * @param params
	 * @return
	 */
	public static final String getUrlByParams(String url, RequestParams params) {
        if(params != null) {
            String paramString = params.getParamString();
            if(StringUtils.isNotEmpty(paramString)){
            	if (url.indexOf("?") == -1) {
            		url += "?" + paramString;
            	} else {
            		url += "&" + paramString;
            	}
            }
        }
        return url;
    }
    
    /**
     * 追加Http头信息
     * @param httpRequest
     * @param headers
     * @return
     */
    public static final HttpRequestBase appendHeaders(HttpRequestBase httpRequest, List<Header> headers){
        if(httpRequest != null && headers != null && headers.size() > 0){
            for(Header header : headers){
                httpRequest.addHeader(header);
            }
        }
        return httpRequest;
    }
}
