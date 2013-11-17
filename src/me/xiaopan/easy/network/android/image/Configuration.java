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

/**
 * 配置
 */
public class Configuration {
	private int maxThreadNumber = 20;	//最大线程数
	private int maxWaitingNumber = 10;	//最大等待数
	private boolean enableOutputLogToConsole = true;	//输出Log到控制台
	private String cacheDirName = "image_loader";	//缓存文件夹名称
	private String logTag = "ImageLoader";	//LogTag
	private Options defaultOptions;	//默认加载选项
	private DefaultBitmapLoadHandler defaultBitmapLoadHandler;	//默认的图片加载处理器
	private BitmapCacher bitmapCacher;	//位图缓存器
	
	public int getMaxThreadNumber() {
		return maxThreadNumber;
	}
	
	public void setMaxThreadNumber(int maxThreadNumber) {
		this.maxThreadNumber = maxThreadNumber;
	}
	
	public int getMaxWaitingNumber() {
		return maxWaitingNumber;
	}
	
	public void setMaxWaitingNumber(int maxWaitingNumber) {
		this.maxWaitingNumber = maxWaitingNumber;
	}
	
	public boolean isEnableOutputLogToConsole() {
		return enableOutputLogToConsole;
	}
	
	public void setEnableOutputLogToConsole(boolean enableOutputLogToConsole) {
		this.enableOutputLogToConsole = enableOutputLogToConsole;
	}
	
	public String getCacheDirName() {
		return cacheDirName;
	}
	
	public void setCacheDirName(String cacheDirName) {
		this.cacheDirName = cacheDirName;
	}
	
	public String getLogTag() {
		return logTag;
	}
	
	public void setLogTag(String logTag) {
		this.logTag = logTag;
	}
	
	public Options getDefaultOptions() {
		return defaultOptions;
	}
	
	public void setDefaultOptions(Options defaultOptions) {
		this.defaultOptions = defaultOptions;
	}
	
	public DefaultBitmapLoadHandler getDefaultBitmapLoadHandler() {
		return defaultBitmapLoadHandler;
	}
	
	public void setDefaultBitmapLoadHandler(DefaultBitmapLoadHandler defaultBitmapLoadHandler) {
		this.defaultBitmapLoadHandler = defaultBitmapLoadHandler;
	}
	
	public BitmapCacher getBitmapCacher() {
		return bitmapCacher;
	}
	
	public void setBitmapCacher(BitmapCacher bitmapCacher) {
		this.bitmapCacher = bitmapCacher;
	}
}