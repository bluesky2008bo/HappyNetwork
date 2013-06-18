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
package me.xiaopan.easynetwork.android.image;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.view.animation.Animation;

/**
 * 工具箱
 */
public class ImageLoaderUtils {
	public static final String CHARSET_NAME_UTF8 = "UTF-8";
	
	/**
	 * 判断给定的字符串是否不为null且不为空
	 * @param string 给定的字符串
	 * @return 
	 */
	public static boolean isNotNullAndEmpty(String string){
		return string != null && !"".equals(string.trim());
	}
	
	/**
	 * 判断给定的字符串数组中是否全部都不为null且不为空
	 * @param strings 给定的字符串数组
	 * @return 是否全部都不为null且不为空
	 */
	public static boolean isNotNullAndEmpty(String... strings){
		boolean result = true;
		for(String string : strings){
			if(string == null || "".equals(string.trim())){
				result = false;
				break;
			}
		}
		return result;
	}
	
	public static Animation getShowAnimationListener(Options options){
		if(options != null && options.getShowAnimationListener() != null){
			return options.getShowAnimationListener().onGetShowAnimation();
		}else if(ImageLoader.getDefaultOptions() != null && ImageLoader.getDefaultOptions().getShowAnimationListener() != null){
			return ImageLoader.getDefaultOptions().getShowAnimationListener().onGetShowAnimation();
		}else{
			return null;
		}
	}
	
	public static int getMaxRetryCount(Options options){
		if(options != null && options.getMaxRetryCount() > 0){
			return options.getMaxRetryCount();
		}else if(ImageLoader.getDefaultOptions() != null && ImageLoader.getDefaultOptions().getMaxRetryCount() > 0){
			return ImageLoader.getDefaultOptions().getMaxRetryCount();
		}else{
			return -1;
		}
	}
	
	public static int getLoadingDrawbleResId(Options options){
		if(options != null && options.getLoadingDrawableResId() > 0){
			return options.getLoadingDrawableResId();
		}else if(ImageLoader.getDefaultOptions() != null && ImageLoader.getDefaultOptions().getLoadingDrawableResId() > 0){
			return ImageLoader.getDefaultOptions().getLoadingDrawableResId();
		}else{
			return -1;
		}
	}
	
	public static int getLoadFailedDrawableResId(Options options){
		if(options != null && options.getLoadFailedDrawableResId() > 0){
			return options.getLoadFailedDrawableResId();
		}else if(ImageLoader.getDefaultOptions() != null && ImageLoader.getDefaultOptions().getLoadFailedDrawableResId() > 0){
			return ImageLoader.getDefaultOptions().getLoadFailedDrawableResId();
		}else{
			return -1;
		}
	}
	
	public static boolean isCacheToLocal(Options options){
		if(options != null){
			return options.isCacheToLocal();
		}else if(ImageLoader.getDefaultOptions() != null){
			return ImageLoader.getDefaultOptions().isCacheToLocal();
		}else{
			return false;
		}
	}
	
	public static boolean isCachedInMemor(Options options){
		if(options != null){
			return options.isCachedInMemory();
		}else if(ImageLoader.getDefaultOptions() != null){
			return ImageLoader.getDefaultOptions().isCachedInMemory();
		}else{
			return false;
		}
	}
	
	public static File getCacheFile(Context context, Options options, String fileName){
		if(options != null && isNotNullAndEmpty(options.getCacheDir())){
			return new File(options.getCacheDir() + File.separator + fileName);
		}else if(ImageLoader.getDefaultOptions() != null && isNotNullAndEmpty(ImageLoader.getDefaultOptions().getCacheDir())){
			return new File(ImageLoader.getDefaultOptions().getCacheDir() + File.separator + fileName);
		}else if(context != null){
			return new File((Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? context.getExternalCacheDir() : context.getCacheDir()).getPath() + File.separator + ImageLoader.getCacheDirName() + File.separator + fileName);
		}else{
			return null;
		}
	}
	
	public static BitmapLoadHandler getBitmapLoadListener(Options options){
		if(options != null && options.getBitmapLoadHandler() != null){
			return options.getBitmapLoadHandler();
		}else if(ImageLoader.getDefaultOptions() != null && ImageLoader.getDefaultOptions().getBitmapLoadHandler() != null){
			return ImageLoader.getDefaultOptions().getBitmapLoadHandler();
		}else{
			return null;
		}
	}
	
	public static CacheDetermineListener getCacheDetermineListener(Options options){
		if(options != null && options.getCacheDetermineListener() != null){
			return options.getCacheDetermineListener();
		}else if(ImageLoader.getDefaultOptions() != null && ImageLoader.getDefaultOptions().getCacheDetermineListener() != null){
			return ImageLoader.getDefaultOptions().getCacheDetermineListener();
		}else{
			return null;
		}
	}
}
