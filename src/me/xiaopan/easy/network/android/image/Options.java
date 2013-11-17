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
package me.xiaopan.easy.network.android.image;

/**
 * 加载选项
 */
public class Options {
	private int loadingDrawableResId = -1;	//正在加载时显示的图片的资源ID
	private int loadFailureDrawableResId = -1;	//加载失败时显示的图片的资源ID
	private int maxRetryCount = -1;	//最大重试次数
	private boolean isCachedInMemory;	//是否每次加载图片的时候先从内存中去找，并且加载完成后将图片缓存在内存中
	private boolean isCacheInLocal;	//是否需要将图片缓存到本地
	private String cacheDir;	//默认缓存目录
	private ShowAnimationListener showAnimationListener;	//显示动画
	private BitmapHandler bitmapHandler;	//位图加载处理器
	
	/**
	 * 获取正在加载时显示的图片的资源ID
	 * @return 正在加载时显示的图片的资源ID
	 */
	public int getLoadingDrawableResId() {
		return loadingDrawableResId;
	}
	
	/**
	 * 设置正在加载时显示的图片的资源ID
	 * @param loadingDrawableResId 正在加载时显示的图片的资源ID
	 */
	public void setLoadingDrawableResId(int loadingDrawableResId) {
		this.loadingDrawableResId = loadingDrawableResId;
	}
	
	/**
	 * 获取加载失败时显示的图片的资源ID
	 * @return 加载失败时显示的图片的资源ID
	 */
	public int getLoadFailureDrawableResId() {
		return loadFailureDrawableResId;
	}

	/**
	 * 设置加载失败时显示的图片的资源ID
	 * @param loadFailureDrawableResId 加载失败时显示的图片的资源ID
	 */
	public void setLoadFailureDrawableResId(int loadFailureDrawableResId) {
		this.loadFailureDrawableResId = loadFailureDrawableResId;
	}

	/**
	 * 判断是否每次加载图片的时候先从内存中去找，并且加载完成后将图片缓存在内存中
	 * @return 
	 */
	public boolean isCachedInMemory() {
		return isCachedInMemory;
	}
	
	/**
	 * 设置是否每次加载图片的时候先从内存中去找，并且加载完成后将图片缓存在内存中
	 * @param isCachedInMemory 是否每次加载图片的时候先从内存中去找，并且加载完成后将图片缓存在内存中
	 */
	public void setCachedInMemory(boolean isCachedInMemory) {
		this.isCachedInMemory = isCachedInMemory;
	}

	/**
	 * 判断是否缓存到本地
	 * @return 是否缓存到本地
	 */
	public boolean isCacheInLocal() {
		return isCacheInLocal;
	}

	/**
	 * 设置是否缓存到本地
	 * @param isCacheInLocal 是否缓存到本地
	 */
	public void setCacheInLocal(boolean isCacheInLocal) {
		this.isCacheInLocal = isCacheInLocal;
	}
	
	/**
	 * 获取最大重试次数
	 * @return 最大重试次数
	 */
	public int getMaxRetryCount() {
		return maxRetryCount;
	}
	
	/**
	 * 设置最大重试次数
	 * @param maxRetryCount 最大重试次数
	 */
	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	/**
	 * 获取显示动画监听器
	 * @return 显示动画监听器
	 */
	public ShowAnimationListener getShowAnimationListener() {
		return showAnimationListener;
	}

	/**
	 * 设置显示动画监听器
	 * @param showAnimationListener 显示动画监听器
	 */
	public void setShowAnimationListener(ShowAnimationListener showAnimationListener) {
		this.showAnimationListener = showAnimationListener;
	}

	/**
	 * 获取缓存目录
	 * @return 缓存目录
	 */
	public String getCacheDir() {
		return cacheDir;
	}

	/**
	 * 设置缓存目录
	 * @param cacheDir 缓存目录
	 */
	public void setCacheDir(String cacheDir) {
		this.cacheDir = cacheDir;
	}

	/**
	 * 获取位图加载处理器
	 * @return 位图加载处理器
	 */
	public BitmapHandler getBitmapHandler() {
		return bitmapHandler;
	}

	/**
	 * 设置位图加载处理器
	 * @param bitmapHandler 位图加载处理器
	 */
	public void setBitmapHandler(BitmapHandler bitmapHandler) {
		this.bitmapHandler = bitmapHandler;
	}
}
