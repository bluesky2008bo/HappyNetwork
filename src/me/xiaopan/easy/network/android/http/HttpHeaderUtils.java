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
package me.xiaopan.easy.network.android.http;

import me.xiaopan.easy.network.android.http.headers.ContentLength;
import me.xiaopan.easy.network.android.http.headers.ContentType;

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
