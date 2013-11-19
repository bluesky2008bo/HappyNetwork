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
import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * 配置
 */
public class Configuration {
	public static String logTag = "ImageLoader";	//LogTag
	private int maxThreadNumber;	//最大线程数
	private int maxWaitingNumber;	//最大等待数
	private boolean debugMode;	//调试模式，在控制台输出日志
	private String defaultCacheDirectory;	//默认的缓存目录
	private Options defaultOptions;	//默认加载选项
	private Handler handler;	//任务结果处理器
	private BitmapCacher bitmapCacher;	//位图缓存器
	
	public Configuration(){
		maxThreadNumber = 20;
		maxWaitingNumber = 10;
		logTag = "ImageLoader";
		debugMode = true;
		defaultOptions = new Options();
		defaultOptions.setCacheInLocal(true);	//将图片缓存到本地
		defaultOptions.setShowAnimationListener(new AlphaShowAnimationListener());	//设置一个透明度由50%渐变到100%的显示动画
		defaultOptions.setBitmapHandler(new PixelsBitmapHandler());	//设置一个图片处理器，保证读取到大小合适的Bitmap，避免内存溢出
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
	 * 获取最大等待数，即等待区的最大容量
	 * @return
	 */
	public int getMaxWaitingNumber() {
		return maxWaitingNumber;
	}
	
	/**
	 * 判断是否开启调试模式
	 * @return
	 */
	public boolean isDebugMode() {
		return debugMode;
	}
	
	/**
	 * 设置是否开启调试模式，开启调试模式后会在控制台输出LOG
	 * @param debugMode
	 */
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
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
		this.defaultOptions = defaultOptions;
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
		this.bitmapCacher = bitmapCacher;
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
		this.defaultCacheDirectory = defaultCacheDirectory;
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
		this.handler = handler;
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
	 * 输出LOG
	 * @param logContent LOG内容
	 */
	public void log(String logContent, boolean error){
		if(isDebugMode()){
			if(error){
				Log.e(logTag, logContent);
			}else{
				Log.d(logTag, logContent);
			}
		}
	}
	
	/**
	 * 输出LOG
	 * @param logContent LOG内容
	 */
	public void log(String logContent){
		log(logContent, false);
	}
}