/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.android.easynetwork.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.CharArrayBuffer;

import android.os.Handler;

/**
 * 响应处理器
 */
public abstract class HttpResponseHandler{
	private boolean cancelled;
	private boolean mayInterruptIfRunning;
	
    public boolean isCancelled() {
		return cancelled;
	}
    
	public boolean isMayInterruptIfRunning() {
		return mayInterruptIfRunning;
	}

    /**
     * 取消
     * @param handler
     * @param mayInterruptIfRunning 如果正在运行是否尝试终止
     * @return
     */
    public void cancel(Handler handler, boolean mayInterruptIfRunning) {
    	this.cancelled = true;
    	this.mayInterruptIfRunning = mayInterruptIfRunning;
    	onCancel(handler);
    }

	/**
     * 当请求开始
     * @param handler 消息处理器
     */
    protected abstract void onStart(final Handler handler);
    
    /**
     * 判断是否可以缓存，当需要缓存HttpResponse的时候会先调用此方法判断是否可以缓存，例如：实现者可以在此方法里过滤掉状态码不是200的响应。默认为状态码大于等于200并且 小于300就返回true
     * @param handler 消息处理器
     * @param httpResponse Http响应
     * @return 是否可以缓存
     */
    protected boolean isCanCache(Handler handler, HttpResponse httpResponse){
    	return httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300;
    }

    /**
     * 处理响应，值得注意的是此方法当在开启缓存模式并且开启刷新缓存以及开启了刷新后再次回调的时候总共会回调两次，你可以通过isCache、isRefreshCacheAndCallback参数来区分并做不同的处理
     * @param handler 消息处理器
     * @param request 请求
     * @param httpResponse Http响应
     * @param isNotRefresh 本次响应不是刷新
     * @param isOver 本次执行是否是最后一次
     * @throws Throwable 当发生异常时会进入exception()方法
     */
    protected abstract void onHandleResponse(Handler handler, HttpUriRequest request, HttpResponse httpResponse, boolean isNotRefresh, boolean isOver) throws Throwable;
    
    /**
     * 更新进度
     * @param handler
     * @param totalLength
     * @param completedLength
     */
    protected abstract void onUpdateProgress(Handler handler, long totalLength, long completedLength);

    /**
     * 在请求的过程中发生异常，值得注意的是在读取本地缓存发生异常的话不会回调此方法，因为会自动改为从网络获取数据，所以一旦回调此方法就意味着整个请求已经结束了
     * @param handler 消息处理器
     * @param isNotRefresh 本次异常不是在刷新缓存数据的时候发生的
     * @param e 异常
     */
    protected abstract void onException(Handler handler, Throwable e, boolean isNotRefresh);
    
    /**
     * 当请求取消
     * @param handler 消息处理器
     */
    protected abstract void onCancel(final Handler handler);
    
    public static byte[] toByteArray(final HttpEntity entity, HttpResponseHandler httpResponseHandler, Handler handler) throws IOException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = null;
		ByteArrayBuffer buffer = null;
		try {
			instream = entity.getContent();
			if (instream == null) {
				return new byte[] {};
			}
			if (entity.getContentLength() > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
			}
			int contentLength = (int) entity.getContentLength();
			if (contentLength < 0) {
				throw new IllegalArgumentException("HTTP entity contentLength is 0");
			}
			buffer = new ByteArrayBuffer(contentLength);
			int readLength;
			long completedLength = 0;
			byte[] tmp = new byte[4096];
			while (!httpResponseHandler.isMayInterruptIfRunning() && (readLength = instream.read(tmp)) != -1) {
				buffer.append(tmp, 0, readLength);
				completedLength+=readLength;
				if(!httpResponseHandler.isCancelled()){
					httpResponseHandler.onUpdateProgress(handler, contentLength, completedLength);
				}
			}
		} finally {
			GeneralUtils.close(instream);
		}
		return httpResponseHandler.isCancelled() || buffer==null?null:buffer.toByteArray();
	}

	public static String getContentCharSet(final HttpEntity entity) throws ParseException {

		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		String charset = null;
		if (entity.getContentType() != null) {
			HeaderElement values[] = entity.getContentType().getElements();
			if (values.length > 0) {
				NameValuePair param = values[0].getParameterByName("charset");
				if (param != null) {
					charset = param.getValue();
				}
			}
		}
		return charset;
	}

	public static String toString(final HttpEntity entity, HttpResponseHandler httpResponseHandler, Handler handler, final String defaultCharset) throws IOException, ParseException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = null;
		CharArrayBuffer buffer = null;
		try {
			instream = entity.getContent();
			if (instream == null) {
				return "";
			}
			if (entity.getContentLength() > Integer.MAX_VALUE) {
				throw new IllegalArgumentException(
						"HTTP entity too large to be buffered in memory");
			}
			int contentLength = (int) entity.getContentLength();
			if (contentLength < 0) {
				throw new IllegalArgumentException("HTTP entity contentLength is 0");
			}
			String charset = getContentCharSet(entity);
			if (charset == null) {
				charset = defaultCharset;
			}
			if (charset == null) {
				charset = HTTP.DEFAULT_CONTENT_CHARSET;
			}
			Reader reader = new InputStreamReader(instream, charset);
			buffer = new CharArrayBuffer(contentLength);
			int readLength;
			long completedLength = 0;
			char[] tmp = new char[1024];
			while (!httpResponseHandler.isMayInterruptIfRunning() && (readLength = reader.read(tmp)) != -1) {
				buffer.append(tmp, 0, readLength);
				completedLength+=readLength;
				if(!httpResponseHandler.isCancelled()){
					httpResponseHandler.onUpdateProgress(handler, contentLength, completedLength);
				}
			}
		} finally {
			GeneralUtils.close(instream);
		}
		return httpResponseHandler.isCancelled() || buffer==null?null:buffer.toString();
	}

	public static String toString(final HttpEntity entity, HttpResponseHandler httpResponseHandler, Handler handler) throws IOException, ParseException {
		return toString(entity, httpResponseHandler, handler, null);
	}
	
	public static void read(final HttpEntity entity, OutputStream outputStream, HttpResponseHandler httpResponseHandler, Handler handler) throws IOException{
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = null;
		try {
			instream = entity.getContent();
			if (instream == null) {
				return;
			}
			if (entity.getContentLength() > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
			}
			int contentLength = (int) entity.getContentLength();
			if (contentLength < 0) {
				throw new IllegalArgumentException("HTTP entity contentLength is 0");
			}
			int readLength;
			long completedLength = 0;
			byte[] tmp = new byte[4096];
			while (!httpResponseHandler.isMayInterruptIfRunning() && (readLength = instream.read(tmp)) != -1) {
				outputStream.write(tmp, 0, readLength);
				completedLength+=readLength;
				if(!httpResponseHandler.isCancelled()){
					httpResponseHandler.onUpdateProgress(handler, contentLength, completedLength);
				}
			}
		} finally {
			GeneralUtils.close(instream);
		}
	}
}
