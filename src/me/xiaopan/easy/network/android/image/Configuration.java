/*
 * Copyright 2013 Peng fei Pan
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

package me.xiaopan.easy.network.android.image;

import java.io.File;

import me.xiaopan.easy.android.util.FileUtils;
import me.xiaopan.easy.java.util.StringUtils;

import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.os.Handler;

/**
 * 配置
 */
public class Configuration {
	private int maxThreadNumber;	//最大线程数
	private int maxWaitingNumber;	//最大等待数
	private int connectionTimeout;	//连接超时时间
	private int maxConnections;	//最大连接数
	private int socketBufferSize;	//Socket缓存池大小
	private String logTag;	//LogTag
	private String defaultCacheDirectory;	//默认的缓存目录
	private Options defaultOptions;	//默认加载选项
	private Handler handler;	//任务结果处理器
	private ImageLoader imageLoader;
	private BitmapCacher bitmapCacher;	//位图缓存器
	
	public Configuration(ImageLoader imageLoader){
		this.imageLoader = imageLoader;
		maxThreadNumber = 20;
		maxWaitingNumber = 10;
		logTag = "ImageLoader";
		defaultOptions = new Options().setCacheInLocal(true).setShowAnimationListener(new AlphaShowAnimationListener()).setBitmapHandler(new PixelsBitmapHandler());
		bitmapCacher = new BitmapLruCacher();
		handler = new Handler();
	}
	
	/**
	 * 获取最大线程数
	 * @return
	 */
	public int getMaxThreadNumber() {
		return maxThreadNumber;
	}

	/**
	 * 设置最大线程数
	 * @param maxThreadNumber
	 */
	public void setMaxThreadNumber(int maxThreadNumber) {
		if(maxThreadNumber > 0){
			this.maxThreadNumber = maxThreadNumber;
		}
	}
	
	/**
	 * 获取最大等待数，即等待区的最大容量
	 * @return
	 */
	public int getMaxWaitingNumber() {
		return maxWaitingNumber;
	}

	/**
	 * 设置最大等待数
	 * @param maxWaitingNumber
	 */
	public void setMaxWaitingNumber(int maxWaitingNumber) {
		if(maxWaitingNumber > 0){
			this.maxWaitingNumber = maxWaitingNumber;
			imageLoader.getWaitingRequestCircle().setMaxSize(maxWaitingNumber);
		}
	}
	
	/**
	 * 获取默认加载选项，当没有单独指定加载选项时，将默认使用此加载选项
	 * @return
	 */
	public Options getDefaultOptions() {
		return defaultOptions;
	}
	
	/**
	 * 设置加载选项，当没有单独指定加载选项时，将默认使用此加载选项
	 * @param defaultOptions
	 */
	public void setDefaultOptions(Options defaultOptions) {
		if(defaultOptions != null){
			this.defaultOptions = defaultOptions;
		}
	}
	
	/**
	 * 获取位图缓存器
	 * @return
	 */
	public BitmapCacher getBitmapCacher() {
		return bitmapCacher;
	}
	
	/**
	 * 设置位图缓存器
	 * @param bitmapCacher
	 */
	public void setBitmapCacher(BitmapCacher bitmapCacher) {
		if(bitmapCacher != null){
			this.bitmapCacher = bitmapCacher;
		}
	}

	/**
	 * 获取默认的缓存目录，当没有指定单独的缓存目录时将使用此缓存目录
	 * @return
	 */
	public String getDefaultCacheDirectory() {
		return defaultCacheDirectory;
	}

	/**
	 * 设置默认的缓存目录，当没有指定单独的缓存目录时将使用此缓存目录
	 * @param defaultCacheDirectory
	 */
	public void setDefaultCacheDirectory(String defaultCacheDirectory) {
		if(StringUtils.isNotEmpty(defaultCacheDirectory)){
			this.defaultCacheDirectory = defaultCacheDirectory;
		}
	}
	
	/**
	 * 获取消息处理器
	 * @return
	 */
	public Handler getHandler() {
		return handler;
	}

	/**
	 * 设置消息处理器
	 * @param handler
	 */
	public void setHandler(Handler handler) {
		if(handler != null){
			this.handler = handler;
		}
	}

	/**
	 * 获取缓存文件，将优先考虑options指定的缓存目录，然后考虑当前configuration指定的缓存目录，然后考虑通过context获取默认的应用缓存目录，再然后就要返回null了
	 * @param context
	 * @param options
	 * @param fileName
	 * @return
	 */
	public File getCacheFile(Context context, Options options, String fileName){
		if(options != null && StringUtils.isNotEmpty(options.getCacheDir())){
			return new File(options.getCacheDir() + File.separator + fileName);
		}else if(StringUtils.isNotEmpty(getDefaultCacheDirectory())){
			return new File(getDefaultCacheDirectory() + File.separator + fileName);
		}else if(context != null){
			return new File(FileUtils.getDynamicCacheDir(context).getPath() + File.separator + "image_loader" + File.separator + fileName);
		}else{
			return null;
		}
	}
	
	/**
	 * 获取Log Tag
	 * @return
	 */
	public String getLogTag() {
		return logTag;
	}

	/**
	 * 设置Log Tag
	 * @param logTag
	 */
	public void setLogTag(String logTag) {
		this.logTag = logTag;
	}

	/**
	 * 获取连接超时时间，单位毫秒
	 * @return
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * 设置连接超时间，单位毫秒
	 * @param connectionTimeout
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		if(connectionTimeout > 0){
			this.connectionTimeout = connectionTimeout;
			HttpParams httpParams = imageLoader.getHttpClient().getParams();
			ConnManagerParams.setTimeout(httpParams, connectionTimeout);
			HttpConnectionParams.setSoTimeout(httpParams, connectionTimeout);
			HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
		}
	}

	/**
	 * 获取最大连接数
	 * @return
	 */
	public int getMaxConnections() {
		return maxConnections;
	}

	/**
	 * 设置最大连接数
	 * @param maxConnections
	 */
	public void setMaxConnections(int maxConnections) {
		if(maxConnections > 0){
			this.maxConnections = maxConnections;
			HttpParams httpParams = imageLoader.getHttpClient().getParams();
			ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
			ConnManagerParams.setMaxTotalConnections(httpParams, maxConnections);
		}
	}

	/**
	 * 获取Socket缓存池大小
	 * @return
	 */
	public int getSocketBufferSize() {
		return socketBufferSize;
	}

	/**
	 * 设置Socket缓存池大小
	 * @param socketBufferSize
	 */
	public void setSocketBufferSize(int socketBufferSize) {
		if(socketBufferSize > 0){
			this.socketBufferSize = socketBufferSize;
			HttpParams httpParams = imageLoader.getHttpClient().getParams();
			HttpConnectionParams.setSocketBufferSize(httpParams, socketBufferSize);
		}
	}
}