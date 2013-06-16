package me.xiaopan.easynetwork.android.http;

import me.xiaopan.easynetwork.android.http.headers.ContentLength;
import me.xiaopan.easynetwork.android.http.headers.ContentType;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

public class HttpHeaderUtils {
	public static ContentType getContentType(HttpResponse httpResponse){
		Header[] contentTypeString = httpResponse.getHeaders(ContentType.NAME);
		if(contentTypeString.length > 0){
			return new ContentType(contentTypeString[0].getValue());
		}else{
			return null;
		}
	}
	
	public static ContentLength getContentLength(HttpResponse httpResponse){
		Header[] contentTypeString = httpResponse.getHeaders(ContentLength.NAME);
		if(contentTypeString.length > 0){
			return new ContentLength(contentTypeString[0].getValue());
		}else{
			return null;
		}
	}
}