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

import java.util.Set;

import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.widget.ImageView;

/**
 * 加载处理器
 */
public class LoadHandler extends Handler {
	public static final int WHAT_LOAD_FINISH = 12313;
	private ImageLoader imageLoader;
	
	public LoadHandler(ImageLoader imageLoader){
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
			
		/* 根据当前加载ID从加载中集合中取出其显示视图集合，并遍历其显示视图集合，一一显示 */
		Set<ImageView> imageViews =  imageLoader.getLoadingMap().remove(loadRequest.getId());
		if(imageViews != null &&imageViews.size() > 0){
			for(ImageView imageView : imageViews){
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
			}
			imageViews.clear();
		}
		
		/* 从等待队列中取出等待加载的请求并尝试加载 */
		LoadRequest waitImageLoadRequest;
		synchronized (imageLoader.getWaitingRequestCircle()) {
			waitImageLoadRequest = imageLoader.getWaitingRequestCircle().remove();
		}
		if(waitImageLoadRequest != null){
			imageLoader.tryLoad(waitImageLoadRequest.getId(), null, null, null, null, waitImageLoadRequest);
		}
	}
}