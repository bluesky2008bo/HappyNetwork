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
package me.xiaopan.android.easynetwork.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HttpRequestRunnable implements Runnable {
    private String name;    //请求名称，在输出log的时候会用此参数来作为标识，方便在log中区分具体的请求
	private Context context;    //上下文
	private EasyHttpClient easyHttpClient;
    private HttpUriRequest httpUriRequest;  //HttpUri请求
    private HttpResponseHandler httpResponseHandler;    //Http响应处理器
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
    	if(httpUriRequest != null && httpResponseHandler != null) {
            try{
                httpResponseHandler.start(easyHttpClient.getConfiguration().getHandler());

                /* 判断是否需要从本地加载 */
                boolean fromlocalLoad = false;
                File responseEntityCacheFile = null, responseHeadersCacheFile = null;	//缓存相应实体的文件,缓存响应头的文件
                String uri = httpUriRequest.getURI().toString();
                if(responseCache != null){
                    String id = GeneralUtils.MD5(uri);
                    responseEntityCacheFile = new File(GeneralUtils.getDynamicCacheDir(context).getPath() + File.separator + "easy_http_client" + File.separator  + id + ".entity");
                    responseHeadersCacheFile = new File(GeneralUtils.getDynamicCacheDir(context).getPath() + File.separator + "easy_http_client" + File.separator  + id + ".headers");
                    fromlocalLoad = responseEntityCacheFile.exists() && responseHeadersCacheFile.exists();
                    if(fromlocalLoad && responseCache.getPeriodOfValidity() > 0){
                        Calendar calendar = new GregorianCalendar();
                        calendar.add(Calendar.MILLISECOND, -responseCache.getPeriodOfValidity());
                        fromlocalLoad = calendar.getTimeInMillis() < responseEntityCacheFile.lastModified();
                        if(!fromlocalLoad){
                            if(responseEntityCacheFile.delete() || responseHeadersCacheFile.delete()){
                                if(easyHttpClient.getConfiguration().isDebugMode()){
                                    Log.w(easyHttpClient.getConfiguration().getLogTag(), name + "緩存過期，已刪除");
                                }
                            }else{
                                if(easyHttpClient.getConfiguration().isDebugMode()){
                                    Log.e(easyHttpClient.getConfiguration().getLogTag(), name + "緩存過期，刪除失敗");
                                }
                            }
                        }
                    }
                }

                /* 根据需要从本地或者网络加载数据 */
                if(fromlocalLoad){
                    fromLocalLoad(uri, responseEntityCacheFile, responseHeadersCacheFile);
                }else{
                    fromNetworkLoad(uri, responseEntityCacheFile, responseHeadersCacheFile, false);
                }
            }catch(Throwable throwable){
                throwable.printStackTrace();
                if(easyHttpClient.getConfiguration().isDebugMode()){
                    Log.e(easyHttpClient.getConfiguration().getLogTag(), name + "（总的）加载失败："+throwable.toString());
                }
                httpResponseHandler.exception(easyHttpClient.getConfiguration().getHandler(), throwable);
            }
    	}
    }

    /**
     * 从本地加载
     * @param uri 请求地址
     * @param responseEntityCacheFile 响应实体缓存文件
     * @param responseHeadersCacheFile 响应头缓存文件
     */
    private void fromLocalLoad(String uri, File responseEntityCacheFile, File responseHeadersCacheFile){
        if(easyHttpClient.getConfiguration().isDebugMode()){
            Log.d(easyHttpClient.getConfiguration().getLogTag(), name + "（本地）请求地址："+uri);
        }
		try{
			HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "success"));

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
			httpResponse.setEntity(new InputStreamEntity(new FileInputStream(responseEntityCacheFile), responseEntityCacheFile.length()));

            /* 回调处理响应 */
            httpResponseHandler.handleResponse(easyHttpClient.getConfiguration().getHandler(), httpResponse, true, responseCache.isRefreshCache() && responseCache.isRefreshCallback());

            /* 如果需要刷新本地缓存 */
            if(responseCache.isRefreshCache()){
                if(easyHttpClient.getConfiguration().isDebugMode()){
                    Log.w(easyHttpClient.getConfiguration().getLogTag(), name + "（本地）加载成功，重新从网络加载，刷新本地缓存");
                }
                fromNetworkLoad(uri, responseEntityCacheFile, responseHeadersCacheFile, true);
            }
		}catch(Throwable throwable){
			throwable.printStackTrace();
            if(easyHttpClient.getConfiguration().isDebugMode()){
                Log.e(easyHttpClient.getConfiguration().getLogTag(), name + "（本地）加载失败，重新从网络加载："+throwable.toString());
            }
            fromNetworkLoad(uri, responseEntityCacheFile, responseHeadersCacheFile, false);
		}
    }
    
    /**
     * 从网络加载
     * @param uri 请求地址
     * @param responseEntityCacheFile 响应实体缓存文件
     * @param responseHeadersCacheFile 响应头缓存文件
     * @param refreshCache 是否刷新缓存
     */
    private void fromNetworkLoad(String uri, File responseEntityCacheFile, File responseHeadersCacheFile, boolean refreshCache){
        if(easyHttpClient.getConfiguration().isDebugMode()){
            Log.d(easyHttpClient.getConfiguration().getLogTag(), name + "（网络）请求地址："+uri);
        }
        try{
            HttpResponse httpResponse = easyHttpClient.getConfiguration().getDefaultHttpClient().execute(httpUriRequest, easyHttpClient.getConfiguration().getHttpContext());

            /* 如果需要缓存 */
            if(responseCache != null && httpResponseHandler.isCanCache(easyHttpClient.getConfiguration().getHandler(), httpResponse)){
                if(GeneralUtils.createFile(responseHeadersCacheFile) != null && GeneralUtils.createFile(responseEntityCacheFile) != null){
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
                        GeneralUtils.writeString(responseHeadersCacheFile, new Gson().toJson(heaerStrings), false);

					    /* 保存响应体 */
                        inputStream = httpResponse.getEntity().getContent();
                        fileOutputStream = new FileOutputStream(responseEntityCacheFile);
                        GeneralUtils.outputFromInput(inputStream, fileOutputStream);
                        inputStream.close();
                        fileOutputStream.flush();
                        fileOutputStream.close();

                        //将响应实体替换为本地文件
                        httpResponse.setEntity(new FileEntity(responseEntityCacheFile, "text/plan"));
                    }catch(IOException exception){
                        if(inputStream != null){ try{inputStream.close();}catch (Exception exception2){exception2.printStackTrace();}}
                        if(fileOutputStream != null){try{fileOutputStream.flush();fileOutputStream.close();}catch (Exception exception2){exception2.printStackTrace();}}
                        if(responseEntityCacheFile.delete() || responseHeadersCacheFile.delete()){
                            if(easyHttpClient.getConfiguration().isDebugMode()){
                                Log.w(easyHttpClient.getConfiguration().getLogTag(), name + "保存响应失败，缓存文件已刪除");
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
            }

            /* 回调处理响应 */
            if(!refreshCache || (responseCache != null && responseCache.isRefreshCallback())){
                httpResponseHandler.handleResponse(easyHttpClient.getConfiguration().getHandler(), httpResponse, false, false);
            }
        }catch(Throwable throwable){
            if(easyHttpClient.getConfiguration().isDebugMode()){
                Log.e(easyHttpClient.getConfiguration().getLogTag(), name + "（网络）加载失败："+throwable.toString());
            }
            httpUriRequest.abort();
            httpResponseHandler.exception(easyHttpClient.getConfiguration().getHandler(), throwable);
        }
    }
}
