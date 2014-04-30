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
	private boolean cancelled = false;
	private boolean isFinished = false;
	private File statusLineCacheFile;
	private File responseEntityCacheFile;
	private File responseHeadersCacheFile;
	private String uri;
    private String name;    //请求名称，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
    private Configuration configuration;
    private HttpUriRequest httpUriRequest;  //HttpUri请求
    private ResponseCache responseCache;    //响应缓存配置
    private HttpResponseHandler httpResponseHandler;    //Http响应处理器

    public HttpRequestExecuteRunnable(Configuration configuration, String name, HttpUriRequest request, ResponseCache responseCache, HttpResponseHandler httpResponseHandler) {
        this.httpUriRequest = request;
        this.responseCache = responseCache;
        this.httpResponseHandler = httpResponseHandler;
        this.name = name;
        this.configuration = configuration;
    }

    @Override
    public void run() {
    	if(!cancelled){
    		uri = httpUriRequest.getURI().toString();
    		
    		if(configuration.isDebugMode()){
    			Log.i(configuration.getLogTag(), name + "开始（"+uri+"）");
    		}
    		
    		httpResponseHandler.onStart(configuration.getHandler());
    		
    		if(isAvailableByCache()){
    			fromCacheLoad();
    		}else{
    			fromNetworkLoad(false);
    		}
    	}
        isFinished = true;
       
        if(configuration.isDebugMode()){
        	if(cancelled){
        		Log.w(configuration.getLogTag(), name + "取消（"+uri+"）");
        	}else{
        		Log.i(configuration.getLogTag(), name + "完成（"+uri+"）");
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
    			statusLineCacheFile = getCacheFile(configuration, responseCache.getId() + ".status_line");
    			responseHeadersCacheFile = getCacheFile(configuration, responseCache.getId() + ".headers");
    			responseEntityCacheFile = getCacheFile(configuration, responseCache.getId() + ".entity");
    			isAvailable = statusLineCacheFile.exists() && responseHeadersCacheFile.exists() && responseEntityCacheFile.exists();
    			if(isAvailable){
    				if(responseCache.getPeriodOfValidity() > 0){
    					Calendar calendar = new GregorianCalendar();
    					calendar.add(Calendar.MILLISECOND, -responseCache.getPeriodOfValidity());
    					isAvailable = calendar.getTimeInMillis() < responseEntityCacheFile.lastModified();
    					if(configuration.isDebugMode()){
    						if(isAvailable){
    							Log.d(configuration.getLogTag(), name + "缓存 - 有效（"+uri+"）");
    						}else{
    							Log.w(configuration.getLogTag(), name + "缓存 - 已過期（"+uri+"）");
    						}
    					}
    				}else{
    					if(configuration.isDebugMode()) Log.d(configuration.getLogTag(), name + "缓存 - 永久有效（"+uri+"）");
    				}
    			}else{
    				if(configuration.isDebugMode()) Log.w(configuration.getLogTag(), name + "缓存 - 文件不存在（"+uri+"）");
    				statusLineCacheFile.delete();
    				responseHeadersCacheFile.delete();
    				responseEntityCacheFile.delete();
    			}
    		}
    		return isAvailable;
    	}catch(Throwable throwable){
    		Log.e(configuration.getLogTag(), name + "缓存 - 异常（"+uri+"）"+throwable.toString());
    		throwable.printStackTrace();
    		return false;
    	}
    }

    /**
     * 从缓存加载
     */
    private void fromCacheLoad(){
    	if(isCancelled()) return;
    	if(configuration.isDebugMode()) Log.d(configuration.getLogTag(), name + "本地（"+uri+"）");
		
    	try{
			HttpResponse cacheHttpResponse = readHttpResponseFromCacheFile(statusLineCacheFile, responseHeadersCacheFile, responseEntityCacheFile);
			boolean isOver = !(responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId()) && responseCache.isRefreshCache() && responseCache.isRefreshCallback());
			httpResponseHandler.onHandleResponse(configuration.getHandler(), cacheHttpResponse, true, isOver);
			
			if(isCancelled()){
				return;
			}

			// 如果需要刷新本地缓存
			if(responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId()) && responseCache.isRefreshCache()){
				fromNetworkLoad(true);
			}
		}catch(Throwable throwable){
			throwable.printStackTrace();
            if(!isCancelled()){
            	fromNetworkLoad(false);
            }
		}
    }
    
    /**
     * 从网络加载
     * @param isRefreshCache 本次从网络加载数据是否是为了刷新缓存
     */
    private void fromNetworkLoad(boolean isRefreshCache){
    	if(isCancelled()) return;
    	if(configuration.isDebugMode()) Log.d(configuration.getLogTag(), name + "网络"+(isRefreshCache?" - 刷新缓存":"")+"（"+uri+"）");
        
    	try{
            HttpResponse httpResponse = configuration.getHttpClientManager().getHttpClient().execute(httpUriRequest, configuration.getHttpClientManager().getHttpContext());
            
            if(isCancelled()) return;

            //尝试缓存
            if(responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId()) && httpResponseHandler.isCanCache(configuration.getHandler(), httpResponse)){
            	saveHttpResponseToCacheFile(httpResponse, statusLineCacheFile, responseHeadersCacheFile, responseEntityCacheFile, configuration.isDebugMode(), configuration.getLogTag(), name);
            }
            
            if(isCancelled()) return;
            
            //回调处理响应
        	if(!isRefreshCache || (responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId()) && responseCache.isRefreshCallback())){
        		httpResponseHandler.onHandleResponse(configuration.getHandler(), httpResponse, !isRefreshCache, true);
        	}
        }catch(Throwable throwable){
        	throwable.printStackTrace();
        	httpUriRequest.abort();
            if(!isCancelled() && !isRefreshCache || (responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId()) && responseCache.isRefreshCallback())){
            	httpResponseHandler.onException(configuration.getHandler(), throwable, !isRefreshCache);
            }
        }
    }
    
    /**
     * 保存Http响应
     * @param httpResponse
     * @throws IOException
     */
    private static void saveHttpResponseToCacheFile(HttpResponse httpResponse, File statusLineCacheFile, File responseHeadersCacheFile, File responseEntityCacheFile, boolean debugMode, String logTag, String name) throws IOException{
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
        				if(debugMode) Log.w(logTag, name + "网络 - 保存响应失败，缓存文件已刪除");
        			}else{
        				if(debugMode) Log.w(logTag, name + "网络 - 保存响应失败，缓存文件刪除失敗");
        			}
        			throw exception;
        		}
        	}else{
        		if(debugMode) Log.w(logTag, name + "网络 - 创建文件 "+responseHeadersCacheFile.getPath() + " 或 " + responseEntityCacheFile.getPath()+" 失败");
        	}
        }else{
        	if(debugMode) Log.w(logTag, name + "网络 - 缓存失败，原因：Http实体是null");
        }
    }

    /**
     * 是否已经取消
     * @return
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * 是否完成
     * @return
     */
    public boolean isDone() {
        return isFinished;
    }

    /**
     * 取消
     * @param mayInterruptIfRunning 如果正在运行是否 尝试终止
     * @return
     */
    public void cancel(boolean mayInterruptIfRunning) {
        if(!isFinished){
        	cancelled = true;
        	if (mayInterruptIfRunning && httpUriRequest != null && !httpUriRequest.isAborted()) {
        		httpUriRequest.abort();
        	}
        	httpResponseHandler.cancel(configuration.getHandler(), mayInterruptIfRunning);
        }
    }
    
    /**
     * 获取缓存文件
     */
    private static final File getCacheFile(Configuration configuration, String fileName){
    	if(GeneralUtils.isNotEmpty(configuration.getDefaultCacheDirectory())){
    		return new File(configuration.getDefaultCacheDirectory() + File.separator + "easy_http_client" + File.separator  + fileName);
    	}else{
    		return new File(GeneralUtils.getDynamicCacheDir(configuration.getContext()).getPath() + File.separator + "easy_http_client" + File.separator  + fileName);
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
    private static HttpResponse readHttpResponseFromCacheFile(File statusLineCacheFile, File responseHeadersCacheFile, File responseEntityCacheFile) throws JsonSyntaxException, IOException{
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
}
