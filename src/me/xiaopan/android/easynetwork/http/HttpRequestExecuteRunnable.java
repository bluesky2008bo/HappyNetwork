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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BufferedHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

class HttpRequestExecuteRunnable implements Runnable {
    private String name;    //请求名称，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
	private EasyHttpClient easyHttpClient;
    private HttpUriRequest httpUriRequest;  //HttpUri请求
    private HttpResponseHandler httpResponseHandler;    //Http响应处理器
    private ResponseCache responseCache;    //响应缓存配置
    private File statusLineCacheFile;
    private File responseEntityCacheFile;
    private File responseHeadersCacheFile;
    private String uri;
    private boolean isCancelled = false;
    private boolean cancelIsNotified = false;
    private boolean isFinished = false;

    public HttpRequestExecuteRunnable(EasyHttpClient easyHttpClient, String name, HttpUriRequest request, ResponseCache responseCache, HttpResponseHandler httpResponseHandler) {
    	this.easyHttpClient = easyHttpClient;
        this.httpUriRequest = request;
        this.responseCache = responseCache;
        this.httpResponseHandler = httpResponseHandler;
        this.name = name;
    }

    @Override
    public void run() {
    	if(httpUriRequest != null && httpResponseHandler != null) {
            httpResponseHandler.onStart(easyHttpClient.getConfiguration().getHandler());
            uri = httpUriRequest.getURI().toString();
            if(isAvailableByCache()){
                fromCacheLoad();
            }else{
                fromNetworkLoad(false);
            }
    	}
    }
    
    /**
     * 判断缓存是否可用
     * @return
     */
    private boolean isAvailableByCache(){
    	try{
    		boolean isAvailable = false;
    		if(responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId())){
    			statusLineCacheFile = getCacheFile(easyHttpClient.getConfiguration(), responseCache.getId() + ".status_line");
    			responseHeadersCacheFile = getCacheFile(easyHttpClient.getConfiguration(), responseCache.getId() + ".headers");
    			responseEntityCacheFile = getCacheFile(easyHttpClient.getConfiguration(), responseCache.getId() + ".entity");
    			isAvailable = statusLineCacheFile.exists() && responseHeadersCacheFile.exists() && responseEntityCacheFile.exists();
    			if(isAvailable){
    				if(responseCache.getPeriodOfValidity() > 0){
    					Calendar calendar = new GregorianCalendar();
    					calendar.add(Calendar.MILLISECOND, -responseCache.getPeriodOfValidity());
    					isAvailable = calendar.getTimeInMillis() < responseEntityCacheFile.lastModified();
    					if(!isAvailable){
    						if(easyHttpClient.getConfiguration().isDebugMode()){
								Log.w(easyHttpClient.getConfiguration().getLogTag(), name + "（缓存）已過期（"+uri+"）");
							}
    					}else{
    						if(easyHttpClient.getConfiguration().isDebugMode()){
    							Log.i(easyHttpClient.getConfiguration().getLogTag(), name + "（缓存）有效（"+uri+"）");
    						}
    					}
    				}else{
    					if(easyHttpClient.getConfiguration().isDebugMode()){
    						Log.i(easyHttpClient.getConfiguration().getLogTag(), name + "（缓存）永久有效（"+uri+"）");
    					}
    				}
    			}else{
    				if(easyHttpClient.getConfiguration().isDebugMode()){
    					Log.w(easyHttpClient.getConfiguration().getLogTag(), name + "（缓存）文件不存在（"+uri+"）");
    				}
    				statusLineCacheFile.delete();
    				responseHeadersCacheFile.delete();
    				responseEntityCacheFile.delete();
    			}
    		}
    		return isAvailable;
    	}catch(Throwable throwable){
    		Log.e(easyHttpClient.getConfiguration().getLogTag(), name + "（缓存）异常（"+uri+"）"+throwable.toString());
    		throwable.printStackTrace();
    		return false;
    	}
    }

    /**
     * 从缓存加载
     */
    private void fromCacheLoad(){
        if(easyHttpClient.getConfiguration().isDebugMode()){
            Log.d(easyHttpClient.getConfiguration().getLogTag(), name + "（本地）开始加载（"+uri+"）");
        }
		try{
            /* 回调处理响应 */
            httpResponseHandler.onHandleResponse(easyHttpClient.getConfiguration().getHandler(), readHttpResponseFromCacheFile(statusLineCacheFile, responseHeadersCacheFile, responseEntityCacheFile), true, !(responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId()) && responseCache.isRefreshCache() && responseCache.isRefreshCallback()));
            if(easyHttpClient.getConfiguration().isDebugMode()){
            	Log.i(easyHttpClient.getConfiguration().getLogTag(), name + "（本地）加载成功（"+uri+"）");
            }

            /* 如果需要刷新本地缓存 */
            if(responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId()) && responseCache.isRefreshCache()){
                fromNetworkLoad(true);
            }
		}catch(Throwable throwable){
			throwable.printStackTrace();
            if(easyHttpClient.getConfiguration().isDebugMode()){
                Log.e(easyHttpClient.getConfiguration().getLogTag(), name + "（本地）加载失败（"+uri+"）"+throwable.toString());
            }
            fromNetworkLoad(false);
		}
    }
    
    /**
     * 从网络加载
     * @param isRefreshCache 本次从网络加载数据是否是为了刷新缓存
     */
    private void fromNetworkLoad(boolean isRefreshCache){
        if(easyHttpClient.getConfiguration().isDebugMode()){
            if(isRefreshCache){
            	Log.d(easyHttpClient.getConfiguration().getLogTag(), name + "（网络）开始刷新缓存（"+uri+"）");
            }else{
            	Log.d(easyHttpClient.getConfiguration().getLogTag(), name + "（网络）开始加载（"+uri+"）");
            }
        }
        try{
            HttpResponse httpResponse = easyHttpClient.getConfiguration().getHttpClientManager().getHttpClient().execute(httpUriRequest, easyHttpClient.getConfiguration().getHttpClientManager().getHttpContext());
            //尝试缓存
            if(responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId()) && httpResponseHandler.isCanCache(easyHttpClient.getConfiguration().getHandler(), httpResponse)){
            	saveHttpResponseToCacheFile(httpResponse, statusLineCacheFile, responseHeadersCacheFile, responseEntityCacheFile);
            }
            //回调处理响应
            if(!isRefreshCache || (responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId()) && responseCache.isRefreshCallback())){
                httpResponseHandler.onHandleResponse(easyHttpClient.getConfiguration().getHandler(), httpResponse, !isRefreshCache, true);
            }
            if(easyHttpClient.getConfiguration().isDebugMode()){
            	if(isRefreshCache){
            		Log.i(easyHttpClient.getConfiguration().getLogTag(), name + "（网络）刷新缓存成功（"+uri+"）");
            	}else{
            		Log.i(easyHttpClient.getConfiguration().getLogTag(), name + "（网络）加载成功（"+uri+"）");
            	}
            }
        }catch(Throwable throwable){
        	httpUriRequest.abort();
            if(easyHttpClient.getConfiguration().isDebugMode()){
            	if(isRefreshCache){
            		Log.e(easyHttpClient.getConfiguration().getLogTag(), name + "（网络）刷新缓存失败（"+uri+"）"+throwable.toString());
            	}else{
            		Log.e(easyHttpClient.getConfiguration().getLogTag(), name + "（网络）加载失败（"+uri+"）"+throwable.toString());
            	}
            }
            throwable.printStackTrace();
            if(!isRefreshCache || (responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId()) && responseCache.isRefreshCallback())){
            	httpResponseHandler.onException(easyHttpClient.getConfiguration().getHandler(), throwable, !isRefreshCache);
            }
        }
    }
    
    /**
     * 保存Http响应
     * @param httpResponse
     * @throws IOException
     */
    private void saveHttpResponseToCacheFile(HttpResponse httpResponse, File statusLineCacheFile, File responseHeadersCacheFile, File responseEntityCacheFile) throws IOException{
    	HttpEntity httpEntity = httpResponse.getEntity();
        if(httpEntity != null){
        	if(GeneralUtils.createFile(statusLineCacheFile) != null && GeneralUtils.createFile(responseHeadersCacheFile) != null && GeneralUtils.createFile(responseEntityCacheFile) != null){
        		InputStream inputStream = null;
        		FileOutputStream fileOutputStream = null;
        		try{
        			/* 保存状态行 */
        			SaveStatusLine saveStatusLine = new SaveStatusLine(httpResponse.getStatusLine());
        			GeneralUtils.writeString(statusLineCacheFile, new Gson().toJson(saveStatusLine), false);
        			
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
        			GeneralUtils.writeString(responseHeadersCacheFile, new Gson().toJson(heaerStrings), false);
        			
        			/* 保存响应体 */
        			inputStream = httpEntity.getContent();
        			fileOutputStream = new FileOutputStream(responseEntityCacheFile);
        			GeneralUtils.outputFromInput(inputStream, fileOutputStream);
        			inputStream.close();
        			fileOutputStream.flush();
        			fileOutputStream.close();
        			
        			//将响应实体替换为本地文件
        			Header contentTypeHeader = httpEntity.getContentType();
        			httpResponse.setEntity(new FileEntity(responseEntityCacheFile,contentTypeHeader != null?contentTypeHeader.getValue():null));
        		}catch(IOException exception){
        			if(inputStream != null){ try{inputStream.close();}catch (Exception exception2){exception2.printStackTrace();}}
        			if(fileOutputStream != null){try{fileOutputStream.flush();fileOutputStream.close();}catch (Exception exception2){exception2.printStackTrace();}}
        			if(responseEntityCacheFile.delete() || responseHeadersCacheFile.delete()){
        				if(easyHttpClient.getConfiguration().isDebugMode()){
        					Log.e(easyHttpClient.getConfiguration().getLogTag(), name + "保存响应失败，缓存文件已刪除");
        				}
        			}else{
        				if(easyHttpClient.getConfiguration().isDebugMode()){
        					Log.e(easyHttpClient.getConfiguration().getLogTag(), name + "保存响应失败，缓存文件刪除失敗");
        				}
        			}
        			throw exception;
        		}
        	}else{
        		if(easyHttpClient.getConfiguration().isDebugMode()){
        			Log.e(easyHttpClient.getConfiguration().getLogTag(), name + "创建文件 "+responseHeadersCacheFile.getPath() + " 或 " + responseEntityCacheFile.getPath()+" 失败");
        		}
        	}
        }else{
        	if(easyHttpClient.getConfiguration().isDebugMode()){
    			Log.e(easyHttpClient.getConfiguration().getLogTag(), name + "缓存失败，原因：Http实体是null");
    		}
        }
    }
    
    /**
     * 读取Http响应
     * @param statusLineCacheFile
     * @param responseHeadersCacheFile
     * @param responseEntityCacheFile
     * @return
     * @throws JsonSyntaxException
     * @throws IOException
     */
    private HttpResponse readHttpResponseFromCacheFile(File statusLineCacheFile, File responseHeadersCacheFile, File responseEntityCacheFile) throws JsonSyntaxException, IOException{
    	/* 读取状态行 */
		HttpResponse httpResponse = new BasicHttpResponse(new Gson().fromJson(GeneralUtils.readString(statusLineCacheFile), SaveStatusLine.class).toStatusLine());

		/* 读取响应头 */
        String[] headerStrings = new Gson().fromJson(GeneralUtils.readString(responseHeadersCacheFile), new TypeToken<String[]>(){}.getType());
        if(headerStrings != null && headerStrings.length > 0){
            Header[] headers = new Header[headerStrings.length];
            int w = 0;
            for(String string : headerStrings){
                CharArrayBuffer charArrayBuffer = new CharArrayBuffer(string.length());
                charArrayBuffer.append(string);
                headers[w++] = new BufferedHeader(charArrayBuffer);
            }
            httpResponse.setHeaders(headers);
        }

		/* 设置响应体 */
        Header contentTypeHeader = null;
        Header[] contentTypes = httpResponse.getHeaders(HTTP.CONTENT_TYPE);
        if(contentTypes != null && contentTypes.length > 0){
        	contentTypeHeader = contentTypes[0];
        }
        httpResponse.setEntity(new FileEntity(responseEntityCacheFile, contentTypeHeader != null?contentTypeHeader.getValue():null));
        
        return httpResponse;
    }

    public boolean isCancelled() {
        if (isCancelled) {
            sendCancelNotification();
        }
        return isCancelled;
    }

    private synchronized void sendCancelNotification() {
        if (!isFinished && isCancelled && !cancelIsNotified) {
            cancelIsNotified = true;
            if (httpResponseHandler != null){
            	httpResponseHandler.onCancel(easyHttpClient.getConfiguration().getHandler());
            }
        }
    }

    public boolean isDone() {
        return isCancelled() || isFinished;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        isCancelled = true;
        if (mayInterruptIfRunning && httpUriRequest != null && !httpUriRequest.isAborted()) {
        	httpUriRequest.abort();
        }
        return isCancelled();
    }
    
    private static final File getCacheFile(Configuration configuration, String fileName){
    	if(GeneralUtils.isNotEmpty(configuration.getDefaultCacheDirectory())){
    		return new File(configuration.getDefaultCacheDirectory() + File.separator + "easy_http_client" + File.separator  + fileName);
    	}else{
    		return new File(GeneralUtils.getDynamicCacheDir(configuration.getContext()).getPath() + File.separator + "easy_http_client" + File.separator  + fileName);
    	}
    }
}
