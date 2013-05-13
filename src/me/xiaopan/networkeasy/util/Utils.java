package me.xiaopan.networkeasy.util;

import java.util.ArrayList;

import me.xiaopan.networkeasy.HttpHeaderUtils;
import me.xiaopan.networkeasy.headers.ContentType;

import org.apache.http.HttpResponse;

public class Utils {
	public static final String DEFAULT_CHARSET = "UTF-8";
	
	/**
	 * 把给定的字符串用给定的字符分割
	 * @param string 给定的字符串
	 * @param ch 给定的字符
	 * @return 分割后的字符串数组
	 */
	public static String[] partition(String string, char ch) {
		ArrayList<String> stringList = new ArrayList<String>();
		char chars[] = string.toCharArray();
		int nextStart = 0;
		for (int w = 0; w < chars.length; w++){
			if (ch == chars[w]) {
				if(w != nextStart){					
					stringList.add(new String(chars,nextStart, w-nextStart));
					nextStart = w+1;
				}else{
					nextStart++;
				}
			}
		}
		if(nextStart < chars.length-1){
			stringList.add(new String(chars,nextStart, chars.length-1-nextStart+1));
		}
		return stringList.toArray(new String[stringList.size()]);
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
}