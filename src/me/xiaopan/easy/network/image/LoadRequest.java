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
package me.xiaopan.easy.network.image;

import java.io.File;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 加载请求
 */
public class LoadRequest {
	private String id;	//ID
	private ImageView showImageView;	//显示图片的视图
	private String imageUrl;	//要下载的图片的URL
	private File localCacheFile;	//本地缓存文件
	private Options options;	//加载选项
	private Bitmap resultBitmap;
	
	public LoadRequest(String id, String imageUrl, File localCacheFile, ImageView imageView, Options options){
		this.id = id;
		this.imageUrl = imageUrl;
		this.localCacheFile = localCacheFile;
		this.showImageView = imageView;
		this.options = options;
	}
	
	/**
	 * 图片加载请求
	 * @param id
	 * @param imageView
	 */
	public LoadRequest(String id, ImageView imageView){
		this.id = id;
		this.showImageView = imageView;
	}
	
	/**
	 * 获取ID
	 * @return ID
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * 设置ID
	 * @param id ID
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * 获取图片视图
	 * @return 图片视图
	 */
	public ImageView getShowImageView() {
		return showImageView;
	}
	
	/**
	 * 设置图片视图
	 * @param showImageView 图片视图
	 */
	public void setShowImageView(ImageView showImageView) {
		this.showImageView = showImageView;
	}

	/**
	 * 获取图片地址
	 * @return 图片地址
	 */
	public String getImageUrl() {
		return imageUrl;
	}

	/**
	 * 设置图片地址
	 * @param imageUrl 图片地址
	 */
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	/**
	 * 获取本地缓存文件
	 * @return 本地缓存文件
	 */
	public File getLocalCacheFile() {
		return localCacheFile;
	}

	/**
	 * 设置本地缓存文件
	 * @param localCacheFile 本地缓存文件
	 */
	public void setLocalCacheFile(File localCacheFile) {
		this.localCacheFile = localCacheFile;
	}
	
	/**
	 * 获取加载选项
	 * @return
	 */
	public Options getOptions() {
		return options;
	}

	/**
	 * 设置加载选项
	 * @param options
	 */
	public void setOptions(Options options) {
		this.options = options;
	}

	public Bitmap getResultBitmap() {
		return resultBitmap;
	}

	public void setResultBitmap(Bitmap resultBitmap) {
		this.resultBitmap = resultBitmap;
	}
}
