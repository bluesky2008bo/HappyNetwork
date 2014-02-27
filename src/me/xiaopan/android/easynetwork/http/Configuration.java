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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * 配置
 */
public class Configuration {
	private boolean debugMode;	//调试模式
    private boolean isUrlEncodingEnabled = true;
	private String logTag;	//Log Tag
	private String defaultCacheDirerctory;	//默认缓存目录
	private Context context;	//上下文
	private Handler handler;	//异步处理器
	private ExecutorService executorService;	//线程池
    private HttpClientManager httpClientManager;	//Http客户端管理器
	
    public Configuration(Context context){
    	if(Looper.myLooper() != Looper.getMainLooper()){
			throw new IllegalStateException("你不能在异步线程中创建此对象");
		}
    	this.context = context;
    	this.logTag = EasyHttpClient.class.getSimpleName();
    	this.handler = new Handler();
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public boolean isUrlEncodingEnabled() {
		return isUrlEncodingEnabled;
	}

	public void setUrlEncodingEnabled(boolean isUrlEncodingEnabled) {
		this.isUrlEncodingEnabled = isUrlEncodingEnabled;
	}

	public String getLogTag() {
		return logTag;
	}

	public void setLogTag(String logTag) {
		this.logTag = logTag;
	}

	public String getDefaultCacheDirerctory() {
		return defaultCacheDirerctory;
	}

	public void setDefaultCacheDirerctory(String defaultCacheDirerctory) {
		this.defaultCacheDirerctory = defaultCacheDirerctory;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Handler getHandler() {
		return handler;
	}

	public ExecutorService getExecutorService() {
		if(executorService == null){
			executorService = Executors.newCachedThreadPool();
		}
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public HttpClientManager getHttpClientManager() {
		if(httpClientManager == null){
			httpClientManager = new HttpClientManager();
		}
		return httpClientManager;
	}

	public void setHttpClientManager(HttpClientManager httpClientManager) {
		this.httpClientManager = httpClientManager;
	}
}