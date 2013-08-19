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
import android.graphics.BitmapFactory;
import android.os.Environment;


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
	
	public static File getCacheFile(ImageLoader imageLoader, Context context, ImageLoadOptions imageLoadOptions, String fileName){
		if(imageLoadOptions != null && isNotNullAndEmpty(imageLoadOptions.getCacheDir())){
			return new File(imageLoadOptions.getCacheDir() + File.separator + fileName);
		}else if(context != null){
			File dir = context.getCacheDir();
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				File externalDir = context.getExternalCacheDir();
				if(externalDir != null){
					dir = externalDir;
				}
			}
			return new File(dir.getPath() + File.separator + imageLoader.getCacheDirName() + File.separator + fileName);
		}else{
			return null;
		}
	}
	
	/**
	 * 计算BitmapFactory.Options合适的缩放比例
	 * @param options
	 * @param minSideLength 用于指定最小宽度或最小高度
	 * @param maxNumOfPixels 最大尺寸，由最大宽高相乘得出
	 * @return
	 */
	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
	    int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
	    int roundedSize;
	    if (initialSize <= 8) {
	        roundedSize = 1;
	        while (roundedSize < initialSize) {
	            roundedSize <<= 1;
	        }
	    } else {
	        roundedSize = (initialSize + 7) / 8 * 8;
	    }
	    return roundedSize;
	}
	
	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
	    double w = options.outWidth;
	    double h = options.outHeight;
	    int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
	    int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
	    if (upperBound < lowerBound) {
	        return lowerBound;
	    }

	    if ((maxNumOfPixels == -1) &&
	            (minSideLength == -1)) {
	        return 1;
	    } else if (minSideLength == -1) {
	        return lowerBound;
	    } else {
	        return upperBound;
	    }
	}
}