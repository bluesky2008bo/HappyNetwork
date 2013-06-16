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

import java.util.Iterator;

import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.widget.ImageView;

public class LoadMessageHandler extends Handler {
	public static final int WHAT_LOAD_FINISH = 12313;
	private ImageLoader imageLoader;
	
	public LoadMessageHandler(ImageLoader imageLoader){
		this.imageLoader = imageLoader;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what){
			case WHAT_LOAD_FINISH : onLoadFinish(msg); break; 
		}
	}
	
	/**
	 * 当加载完成
	 * @param message
	 */
	private void onLoadFinish(Message message){
		LoadRequest loadRequest = (LoadRequest) message.obj;
		
		/* 尝试缓存到内存中 */
		if(loadRequest.getResultBitmap() != null && ImageLoaderUtils.isCachedInMemor(loadRequest.getOptions())){
			CacheDetermineListener determineCache = ImageLoaderUtils.getCacheDetermineListener(loadRequest.getOptions());
			if(determineCache == null || determineCache.isCache(loadRequest.getResultBitmap())){
				ImageLoader.putBitmapToCache(loadRequest.getId(), loadRequest.getResultBitmap());
			}
		}
			
		/* 遍历图片视图，找到其绑定的地址同当前下载的地址一样的图片视图，并将结果显示到图片视图上 */
		Iterator<ImageView> iterator = imageLoader.getLoadingImageViewSet().iterator();
		ImageView imageView;
		Object tagObject;
		while(iterator.hasNext()){
			imageView = iterator.next();
			tagObject = imageView.getTag();
			if(tagObject != null && loadRequest.getId().equals(tagObject.toString())){
				imageView.clearAnimation();//先清除之前所有的动画
				if(loadRequest.getResultBitmap() != null){
					Animation animation = ImageLoaderUtils.getShowAnimationListener(loadRequest.getOptions());
					if(animation != null){
						imageView.setAnimation(animation);
					}
					imageView.setImageBitmap(loadRequest.getResultBitmap());
				}else{
					int loadFailedDrawableResId = ImageLoaderUtils.getLoadFailedDrawableResId(loadRequest.getOptions());
					if(loadFailedDrawableResId > 0){
						imageView.setImageResource(loadFailedDrawableResId);
					}else{
						imageView.setImageBitmap(null);
					}
				}
				iterator.remove();
			}
		}
		
		/* 将当前下载对象从正在下载集合中删除 */
		imageLoader.getLoadingRequestSet().remove(loadRequest.getId());
		
		/* 从等待队列中取出等待下载的对象并执行 */
		LoadRequest waitImageLoadRequest;
		synchronized (imageLoader.getWaitingRequestCircle()) {
			waitImageLoadRequest = imageLoader.getWaitingRequestCircle().remove();
		}
		if(waitImageLoadRequest != null){
			imageLoader.tryLoad(waitImageLoadRequest.getId(), null, null, null, null, waitImageLoadRequest);
		}
	}
}
