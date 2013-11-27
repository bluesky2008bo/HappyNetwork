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

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import me.xiaopan.easy.android.util.FileUtils;
import me.xiaopan.easy.java.util.StringUtils;

public class HttpRequestRunnable implements Runnable {
	private Context context;
	private EasyHttpClient easyHttpClient;
    private HttpUriRequest httpUriRequest;  //HttpUri请求
    private HttpResponseHandler httpResponseHandler;    //Http响应处理器
    private String name;
    private ResponseCache responseCache;    //响应缓存配置

    public HttpRequestRunnable(Context context, EasyHttpClient easyHttpClient, String name, HttpUriRequest request, ResponseCache responseCache, HttpResponseHandler httpResponseHandler) {
    	this.context = context;
    	this.easyHttpClient = easyHttpClient;
        this.httpUriRequest = request;
        this.responseCache = responseCache;
        this.httpResponseHandler = httpResponseHandler;
        this.name = name;
    }

    @Override
    public void run() {
    	if(!Thread.currentThread().isInterrupted()) {
    		if(httpResponseHandler != null){
    			httpResponseHandler.start();
    		}

            try {
    			if(!Thread.currentThread().isInterrupted()) {
    				String uri = httpUriRequest.getURI().toString();
    				
    				/* 判断是否需要从本地加载 */
    				boolean fromlocalLoad = false;
    				File cacheEntityFile = null, cacheHeadersFile = null;	//缓存相应实体的文件,缓存响应头的文件
    				if(responseCache != null){
    					String id = StringUtils.MD5(uri);
    					cacheEntityFile = new File(FileUtils.getDynamicCacheDir(context).getPath() + File.separator + "easy_http_client" + File.separator  + id + ".entity");
    					cacheHeadersFile = new File(FileUtils.getDynamicCacheDir(context).getPath() + File.separator + "easy_http_client" + File.separator  + id + ".headers");
    					fromlocalLoad = cacheEntityFile.exists() && cacheHeadersFile.exists();
    					if(fromlocalLoad && responseCache.getPeriodOfValidity() > 0){
                            Calendar calendar = new GregorianCalendar();
                            calendar.add(Calendar.MILLISECOND, -responseCache.getPeriodOfValidity());
                            fromlocalLoad = calendar.getTimeInMillis() < cacheEntityFile.lastModified();
                        }
    				}

    				/* 根据需要从本地或者网络加载数据 */
                    HttpResponse httpResponse = fromlocalLoad?fromLocalLoad(uri, cacheEntityFile, cacheHeadersFile):fromNetworkLoad(uri, cacheEntityFile, cacheHeadersFile);
                    if(!Thread.currentThread().isInterrupted() && httpResponseHandler != null) {
                        httpResponseHandler.handleResponse(httpResponse);
                    }
    			}
    		} catch (Throwable e) {
    			e.printStackTrace();
    			if(httpUriRequest != null){
    				httpUriRequest.abort();
    			}
    			if(!Thread.currentThread().isInterrupted() && httpResponseHandler != null) {
    				httpResponseHandler.exception(e);
    			}
    		}
    	}
    }
    
    /**
     * 从本地加载
     * @param uri
     * @param cacheEntityFile
     * @param cacheHeadersFile
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    private HttpResponse fromLocalLoad(String uri, File cacheEntityFile, File cacheHeadersFile) throws ClientProtocolException, IOException{
    	easyHttpClient.log(name + "（本地）请求地址："+uri);
		try{
			/* 读取响应头 */
			Header[] headers = null;
			String[] headerStrings = new Gson().fromJson(me.xiaopan.easy.java.util.FileUtils.readString(cacheHeadersFile), new TypeToken<String[]>(){}.getType());
			if(headerStrings != null && headerStrings.length > 0){
				headers = new Header[headerStrings.length];
				int w = 0;
				for(String string : headerStrings){
					CharArrayBuffer charArrayBuffer = new CharArrayBuffer(string.length());
					charArrayBuffer.append(string);
					headers[w++] = new BufferedHeader(charArrayBuffer);
				}
			}
			
			/* 设置响应头和响应体 */
			HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "success"));
			if(headers != null && headers.length > 0){
				httpResponse.setHeaders(headers);
			}
			httpResponse.setEntity(new InputStreamEntity(new FileInputStream(cacheEntityFile), cacheEntityFile.length()));
			return httpResponse;
		}catch(Throwable throwable){
			throwable.printStackTrace();
			return fromNetworkLoad(uri, cacheEntityFile, cacheHeadersFile);
		}
    }
    
    /**
     * 从网络加载
     * @param uri
     * @param cacheEntityFile
     * @param cacheHeadersFile
     * @throws ClientProtocolException
     * @throws IOException
     */
    private HttpResponse fromNetworkLoad(String uri, File cacheEntityFile, File cacheHeadersFile) throws ClientProtocolException, IOException{
    	easyHttpClient.log(name + "（网络）请求地址："+uri);
    	HttpResponse httpResponse = easyHttpClient.getHttpClient().execute(httpUriRequest, easyHttpClient.getHttpContext());
		if(responseCache != null && httpResponseHandler.isCanCache(httpResponse)){	//如果需要缓存
			if(me.xiaopan.easy.java.util.FileUtils.createFile(cacheHeadersFile) != null && me.xiaopan.easy.java.util.FileUtils.createFile(cacheEntityFile) != null){
				InputStream inputStream = null;
				FileOutputStream fileOutputStream = null;
				try{
					/* 保存响应头 */
					Header[] headers = httpResponse.getAllHeaders();
					String[] heaerStrings = new String[headers.length];
					for(int w = 0; w < headers.length; w++){
						Header header = headers[w];
						if(header instanceof BufferedHeader){
							heaerStrings[w] = header.toString();
						}else{
							headers[w] = null;
						}
					}
					me.xiaopan.easy.java.util.FileUtils.writeString(cacheHeadersFile, new Gson().toJson(heaerStrings), false);
					
					/* 保存响应体 */
					inputStream = httpResponse.getEntity().getContent();
					fileOutputStream = new FileOutputStream(cacheEntityFile);
					me.xiaopan.easy.java.util.IOUtils.outputFromInput(inputStream, fileOutputStream);
					inputStream.close();
					fileOutputStream.flush();
					fileOutputStream.close();

					//将响应实体替换为本地文件
					httpResponse.setEntity(new FileEntity(cacheEntityFile, "text/plan"));
				}catch(IOException exception){
					exception.printStackTrace();
					if(inputStream != null){ try{inputStream.close();}catch (Exception exception2){exception2.printStackTrace();}}
					if(fileOutputStream != null){try{fileOutputStream.flush();fileOutputStream.close();}catch (Exception exception2){exception2.printStackTrace();}}
                    cacheEntityFile.delete();
                    cacheHeadersFile.delete();
                    throw exception;
				}
			}else{
				easyHttpClient.log("创建文件 "+cacheHeadersFile.getPath() + " 或 " + cacheEntityFile.getPath()+" 失败");
			}
		}
		return httpResponse;
    }

//    private void makeRequest() throws IOException {
//        if(!Thread.currentThread().isInterrupted()) {
//        	try {
//        		HttpResponse response = httpClient.execute(httpUriRequest, httpContext);
//        		if(!Thread.currentThread().isInterrupted()) {
//        			if(httpResponseHandler != null) {
//        				httpResponseHandler.sendResponseMessage(response);
//        			}
//        		} else{
//        		}
//        	} catch (IOException e) {
//        		if(!Thread.currentThread().isInterrupted()) {
//        			throw e;
//        		}
//        	}
//        }
//    }
//
//    private int executionCount;
//    private void makeRequestWithRetries() throws ConnectException {
//        // This is an additional layer of retry logic lifted from droid-fu
//        // See: https://github.com/kaeppler/droid-fu/blob/master/src/main/java/com/github/droidfu/http/BetterHttpRequestBase.java
//        boolean retry = true;
//        IOException cause = null;
//        HttpRequestRetryHandler retryHandler = httpClient.getHttpRequestRetryHandler();
//        while (retry) {
//            try {
//                makeRequest();
//                return;
//            } catch (UnknownHostException e) {
//		        if(httpResponseHandler != null) {
//		            httpResponseHandler.sendFailureMessage(e, "can't resolve host");
//		        }
//	        	return;
//            }catch (SocketException e){
//                // Added to detect host unreachable
//                if(httpResponseHandler != null) {
//                    httpResponseHandler.sendFailureMessage(e, "can't resolve host");
//                }
//                return;
//            }catch (SocketTimeoutException e){
//                if(httpResponseHandler != null) {
//                    httpResponseHandler.sendFailureMessage(e, "socket time out");
//                }
//                return;
//            } catch (IOException e) {
//                cause = e;
//                retry = retryHandler.retryRequest(cause, ++executionCount, httpContext);
//            } catch (NullPointerException e) {
//                // there's a bug in HttpClient 4.0.x that on some occasions causes
//                // DefaultRequestExecutor to throw an NPE, see
//                // http://code.google.com/p/android/issues/detail?id=5255
//                cause = new IOException("NPE in HttpClient" + e.getMessage());
//                retry = retryHandler.retryRequest(cause, ++executionCount, httpContext);
//            }
//        }
//
//        // no retries left, crap out with exception
//        ConnectException ex = new ConnectException();
//        ex.initCause(cause);
//        throw ex;
//    }
}
