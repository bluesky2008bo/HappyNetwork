package me.xiaopan.easynetwork.android;

import me.xiaopan.easynetwork.android.headers.ContentType;

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
}