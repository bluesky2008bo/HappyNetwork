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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import me.xiaopan.easy.android.util.FileUtils;
import me.xiaopan.easy.java.util.StringUtils;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import android.content.Context;

public class HttpRequestRunnable implements Runnable {
	private Context context;
	private EasyHttpClient easyHttpClient;
    private HttpUriRequest httpUriRequest;
    private HttpResponseHandler httpResponseHandler;
    private boolean isCache = true;

    public HttpRequestRunnable(Context context, EasyHttpClient easyHttpClient, HttpUriRequest request, HttpResponseHandler httpResponseHandler) {
    	this.context = context;
    	this.easyHttpClient = easyHttpClient;
        this.httpUriRequest = request;
        this.httpResponseHandler = httpResponseHandler;
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
    				
    				/* 判断是否从本地加载 */
    				boolean fromlocalLoad = false;
    				File cacheFile = null;
    				if(isCache){
    					String id = StringUtils.MD5(uri);
    					cacheFile = new File(FileUtils.getDynamicCacheDir(context).getPath() + File.separator + "EasyHttpClient" + File.separator +id);
    					fromlocalLoad = cacheFile.exists();
    				}
    				
    				HttpResponse httpResponse = null;
    				if(fromlocalLoad){
    					easyHttpClient.log("（本地）请求地址："+uri);
    					httpResponse = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "success"));
    					httpResponse.setEntity(new InputStreamEntity(new FileInputStream(cacheFile), cacheFile.length()));
    				}else{
    					easyHttpClient.log("（网络）请求地址："+uri);
    					httpResponse = easyHttpClient.getHttpClient().execute(httpUriRequest, easyHttpClient.getHttpContext());
    					if(isCache){
    						if(me.xiaopan.easy.java.util.FileUtils.createFile(cacheFile) != null){
    							InputStream inputStream = null;
    							FileOutputStream fileOutputStream = null;
    							try{
    								inputStream = httpResponse.getEntity().getContent();
    								fileOutputStream = new FileOutputStream(cacheFile);
    								me.xiaopan.easy.java.util.IOUtils.outputFromInput(inputStream, fileOutputStream);
    								inputStream.close();
    								fileOutputStream.flush();
    								fileOutputStream.close();
    								httpResponse.setEntity(new InputStreamEntity(new FileInputStream(cacheFile), cacheFile.length()));
    							}catch(IOException exception){
    								exception.printStackTrace();
    								if(inputStream != null){
    									try{
    										inputStream.close();
    									}catch (Exception exception2){
    										exception2.printStackTrace();
    									}
    								}
    								if(fileOutputStream != null){
    									try{
    										fileOutputStream.flush();
    										fileOutputStream.close();
    									}catch (Exception exception2){
    										exception2.printStackTrace();
    									}
    								}
    								throw exception;
    							}
    						}else{
    							easyHttpClient.log("创建文件 "+cacheFile.getPath()+" 失败");
    						}
    					}
    				}
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

//    private void makeRequest() throws IOException {
//        if(!Thread.currentThread().isInterrupted()) {
//        	try {
//        		HttpResponse response = httpClient.execute(httpUriRequest, httpContext);
//        		if(!Thread.currentThread().isInterrupted()) {
//        			if(httpResponseHandler != null) {
//        				httpResponseHandler.sendResponseMessage(response);
//        			}
//        		} else{
//        			//TODO: should raise InterruptedException? this block is reached whenever the request is cancelled before its response is received
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
