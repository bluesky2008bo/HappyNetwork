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

package me.xiaopan.easy.network.image;

import android.graphics.Bitmap;

/**
 * 位图缓存适配器
 */
public interface BitmapCacher {
	/**
	 * 放进去一个位图
	 * @param key
	 * @param bitmap
	 * @return
	 */
	public void put(String key, Bitmap bitmap);
	
	/**
	 * 根据给定的key获取位图
	 * @param key
	 * @return
	 */
	public Bitmap get(String key);
	
	/**
	 * 根据给定的key删除位图
	 * @param key
	 * @return
	 */
	public Bitmap remove(String key);
	
	/**
	 * 清除所有的位图
	 */
	public void clear();
}