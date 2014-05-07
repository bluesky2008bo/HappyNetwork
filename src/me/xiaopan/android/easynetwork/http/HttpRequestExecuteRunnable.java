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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
	private boolean isCancelled;
	private boolean isFinished;
	private boolean isSuccessed;
	private File statusLineCacheFile;
	private File responseEntityCacheFile;
	private File responseHeadersCacheFile;
	private String uri;
    private String name;    //请求名称，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
    private Configuration configuration;	// 配置
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
    	isFinished = false;
		
    	// 输出开始日志和回调
    	uri = httpUriRequest.getURI().toString();
		if(configuration.isDebugMode()) Log.i(configuration.getLogTag(), name + "开始（"+uri+"）");
		httpResponseHandler.onStart(configuration.getHandler());
		
		boolean isContinue = true;
		boolean isRefreshCache = false;
		HttpResponse httpResponse = null;
		
		// 如果本地缓存可以使用
		if(isAvailableByCache()){
			// 从本地缓存中读取Http响应
			httpResponse = getHttpResponseFromCacheFile();
			if(!isCancelled() && httpResponse != null){
				try {
					httpResponseHandler.onHandleResponse(configuration.getHandler(), httpUriRequest, httpResponse, true, !(isRefershCache()&&responseCache.isRefreshCallback()));
					isContinue = !isCancelled() && isRefershCache();// 如果尚未取消并且需要刷新缓存
					isRefreshCache = isRefershCache();
					isSuccessed = true;
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
		
		// 如果还要继续从网络读取数据
		if(!isCancelled() && isContinue){
			try {
				httpResponse = getHttpResponseFromNetwork(isRefreshCache);
				if(!isCancelled() && (!isRefreshCache || (isRefershCache() && responseCache.isRefreshCallback()))){
	        		httpResponseHandler.onHandleResponse(configuration.getHandler(), httpUriRequest, httpResponse, !isRefreshCache, true);
	        	}
				isSuccessed = true;
			} catch (Throwable e) {
				e.printStackTrace();
	        	httpUriRequest.abort();
	        	isSuccessed = false;
	        	if(!isCancelled() && (!isRefreshCache || (isRefershCache() && responseCache.isRefreshCallback()))){
	            	httpResponseHandler.onException(configuration.getHandler(), e, !isRefreshCache);
	            }
			}
		}
		
        isFinished = true;
       
        if(configuration.isDebugMode()){
        	if(isCancelled()){
        		Log.w(configuration.getLogTag(), name + "取消（"+uri+"）");
        	}else if(isSuccessed){
        		Log.i(configuration.getLogTag(), name + "成功（"+uri+"）");
        	}else{
        		Log.e(configuration.getLogTag(), name + "失败（"+uri+"）");
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
    					long cacheTime = responseEntityCacheFile.lastModified();	// 缓存时间
    					Calendar calendar = new GregorianCalendar();
    					calendar.setTime(new Date(cacheTime));
    					calendar.add(Calendar.MILLISECOND, responseCache.getPeriodOfValidity());
    					long outOfDateTime = calendar.getTimeInMillis();	// 过期时间
    					long currentTime = System.currentTimeMillis();	//当前时间
    					isAvailable = outOfDateTime > currentTime;
    					if(configuration.isDebugMode()){
    						if(isAvailable){
    							Log.d(configuration.getLogTag(), name + "缓存 - 有效（"+uri+"）");
    						}else{
    							SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
    							String lastModifiedTimeString = simpleDateFormat.format(new Date(cacheTime));
    							String currentTimeString = simpleDateFormat.format(new Date());
    							String outOfDateTimeString = simpleDateFormat.format(new Date(outOfDateTime));
    							Log.w(configuration.getLogTag(), name + "缓存 - 已過期，缓存时间："+lastModifiedTimeString+"；过期时间："+outOfDateTimeString+"；当前时间："+currentTimeString+"；缓存有效期："+responseCache.getPeriodOfValidity()+"毫秒（"+uri+"）");
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
     * 从缓存文件中读取Http响应
     * @return
     * @throws JsonSyntaxException
     * @throws IOException
     */
    private HttpResponse getHttpResponseFromCacheFile(){
    	try {
	    	if(configuration.isDebugMode()) Log.d(configuration.getLogTag(), name + "本地（"+uri+"）");
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
    	} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}
    }
    
    /**
     * 从网络加载
     */
    private HttpResponse getHttpResponseFromNetwork(boolean isRefreshCache) throws ClientProtocolException, IOException{
    	if(configuration.isDebugMode()) Log.d(configuration.getLogTag(), name + "网络"+(isRefreshCache?" - 刷新缓存":"")+"（"+uri+"）");
        HttpResponse httpResponse = configuration.getHttpClientManager().getHttpClient().execute(httpUriRequest, configuration.getHttpClientManager().getHttpContext());
        
        if(isCancelled()){
        	return null;
        }

        //尝试缓存
        if(isCache() && httpResponseHandler.isCanCache(configuration.getHandler(), httpResponse)){
        	saveHttpResponseToCacheFile(httpResponse);
        }
        
        return httpResponse;
    }
    
    /**
     * 保存Http响应
     * @param httpResponse
     * @throws IOException
     */
    private void saveHttpResponseToCacheFile(HttpResponse httpResponse) throws IOException{
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
        			if(configuration.isDebugMode()) Log.d(configuration.getLogTag(), name + "网络 - 响应已缓存（"+uri+"）");
        		}catch(IOException exception){
        			if(inputStream != null){ try{inputStream.close();}catch (Exception exception2){exception2.printStackTrace();}}
        			if(fileOutputStream != null){try{fileOutputStream.flush();fileOutputStream.close();}catch (Exception exception2){exception2.printStackTrace();}}
        			if(responseEntityCacheFile.delete() || responseHeadersCacheFile.delete()){
        				if(configuration.isDebugMode()) Log.w(configuration.getLogTag(), name + "网络 - 缓存响应失败，缓存文件已刪除（"+uri+"）");
        			}else{
        				if(configuration.isDebugMode()) Log.w(configuration.getLogTag(), name + "网络 - 缓存响应失败，缓存文件刪除失敗（"+uri+"）");
        			}
        			throw exception;
        		}
        	}else{
        		if(configuration.isDebugMode()) Log.w(configuration.getLogTag(), name + "网络 - 创建文件 "+responseHeadersCacheFile.getPath() + " 或 " + responseEntityCacheFile.getPath()+" 失败（"+uri+"）");
        	}
        }else{
        	if(configuration.isDebugMode()) Log.w(configuration.getLogTag(), name + "网络 - 缓存失败，原因：Http实体是null（"+uri+"）");
        }
    }
    
    /**
     * 是否缓存
     * @return
     */
    public boolean isCache(){
    	return responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId());
    }
    
    /**
     * 是否刷新缓存
     * @return
     */
    public boolean isRefershCache(){
    	return responseCache != null && GeneralUtils.isNotEmpty(responseCache.getId()) && responseCache.isRefreshCache();
    }

    /**
     * 是否已经取消
     * @return
     */
    public boolean isCancelled() {
        return isCancelled;
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
        	isCancelled = true;
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
}
