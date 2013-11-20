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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import me.xiaopan.easy.java.util.CircleList;
import me.xiaopan.easy.java.util.StringUtils;
import me.xiaopan.easy.network.android.EasyNetwork;

import org.apache.http.HttpVersion;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

/**
 * 图片加载器，可以从网络或者本地加载图片，并且支持自动清除缓存
 */
public class ImageLoader{
	private Bitmap tempCacheBitmap;	//临时存储缓存的图片
	private Set<String> loadingRequestSet;	//正在加载的Url列表，用来防止同一个URL被重复加载
	private Configuration configuration;	//配置
	private Set<ImageView> loadingImageViewSet;	//图片视图集合，这个集合里的每个尚未加载完成的视图身上都会携带有他要显示的图片的地址，当每一个图片加载完成之后都会在这个列表中遍历找到所有携带有这个这个图片的地址的视图，并把图片显示到这个视图上
	private DefaultHttpClient httpClient;	//Http客户端
	private CircleList<LoadRequest> waitingRequestCircle;	//等待处理的加载请求
	
	/**
	 * 创建图片加载器
	 * @param defaultDrawableResId 默认显示的图片
	 */
	public ImageLoader(){
		configuration = new Configuration(this);
		loadingImageViewSet = new HashSet<ImageView>();//初始化图片视图集合
		loadingRequestSet = new HashSet<String>();//初始化加载中URL集合
		waitingRequestCircle = new CircleList<LoadRequest>(configuration.getMaxWaitingNumber());//初始化等待处理的加载请求集合
	
		BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams); 
		configuration.setConnectionTimeout(10000);
		configuration.setMaxConnections(1);
		configuration.setSocketBufferSize(8192);
	}
	
	/**
	 * 实例持有器
	 */
	private static class ImageLoaderInstanceHolder{
		private static ImageLoader instance = new ImageLoader();
	}
	
	/**
	 * 获取图片加载器的实例，每执行一次此方法就会清除一次历史记录
	 * @return 图片加载器的实例
	 */
	public static final ImageLoader getInstance(){
		return ImageLoaderInstanceHolder.instance;
	}
	
	/**
	 * 加载图片
	 * @param imageUrl 图片下载地址，如果本地缓存文件不存在将从网络获取
	 * @param showImageView 显示图片的视图
	 * @param options 加载选项
	 */
	public final void load(String url, ImageView showImageView, Options options){
		if(StringUtils.isNotEmpty(url) && showImageView != null){
			try {
				String id = URLEncoder.encode(url, EasyNetwork.CHARSET_NAME_UTF8);
				if(!tryShowImage(url, id, showImageView, options)){	//尝试显示图片，如果显示失败了就尝试加载
					tryLoad(id, url, configuration.getCacheFile(showImageView.getContext(), options, id), showImageView, options, null);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
			if(showImageView != null){
				showImageView.setTag(null);
				if(options != null && options.getLoadingImageResource() > 0){
					showImageView.setImageResource(options.getLoadingImageResource());
				}else{
					showImageView.setImageDrawable(null);
				}
			}
		}
	}
	
	/**
	 * 加载图片
	 * @param imageUrl 图片下载地址
	 * @param showImageView 显示图片的视图
	 */
	public final void load(String url, ImageView showImageView){
		load(url, showImageView, configuration.getDefaultOptions());
	}
	
	/**
	 * 加载图片
	 * @param localFile 本地图片文件，如果本地文件不存在会尝试从imageUrl下载图片并创建localFile
	 * @param showImageView 显示图片的视图
	 * @param imageUrl 图片下载地址，如果本地图片文件不存在将从网络获取
	 * @param options 加载选项
	 */
	public final void load(File localFile, ImageView showImageView, String url, Options options){
		if((localFile != null || StringUtils.isNotEmpty(url)) && showImageView != null){
			try{
				String id = URLEncoder.encode(localFile.getPath(), EasyNetwork.CHARSET_NAME_UTF8);
				if(!tryShowImage(localFile.getPath(), id, showImageView, options)){	//尝试显示图片，如果显示失败了就尝试加载
					tryLoad(id, url, localFile, showImageView, options, null);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
			if(showImageView != null){
				showImageView.setTag(null);
				if(options != null && options.getLoadingImageResource() > 0){
					showImageView.setImageResource(options.getLoadingImageResource());
				}else{
					showImageView.setImageDrawable(null);
				}
			}
		}
	}
	
	/**
	 * 加载图片
	 * @param localFile 本地图片文件，如果本地文件不存在会尝试从imageUrl下载图片并创建localFile
	 * @param showImageView 显示图片的视图
	 * @param imageUrl 图片下载地址，如果本地图片文件不存在将从网络获取
	 */
	public final void load(File localFile, ImageView showImageView, String url){
		load(localFile, showImageView, url, configuration.getDefaultOptions());
	}
	
	/**
	 * 加载图片
	 * @param localFile 本地图片文件
	 * @param showImageView 显示图片的视图
	 * @param options 加载选项
	 */
	public final void load(File localFile, ImageView showImageView, Options options){
		load(localFile, showImageView, null, options);
	}
	
	/**
	 * 加载图片
	 * @param localFile 本地图片文件
	 * @param showImageView 显示图片的视图
	 */
	public final void load(File localFile, ImageView showImageView){
		load(localFile, showImageView, null, configuration.getDefaultOptions());
	}
	
	/**
	 * 尝试显示图片
	 * @param id ID，根据此ID从缓存中获取图片
	 * @param showImageView 显示视图
	 * @param options 加载选项
	 * @return true：图片缓存中有图片并且已经显示了；false：缓存中没有对应的图片，需要开启新线程从网络或本地加载
	 */
	private final boolean tryShowImage(String url, String id, ImageView showImageView, Options options){
		//如果需要从缓存中读取，就根据地址从缓存中获取图片，如果缓存中存在相对的图片就显示，否则显示默认图片或者显示空
		if(options != null && options.isCachedInMemory() && (tempCacheBitmap = configuration.getBitmapCacher().get(id)) != null){
			showImageView.setTag(null);	//清空绑定关系
			log("从缓存加载图片："+url);
			loadingImageViewSet.remove(showImageView);
			showImageView.clearAnimation();
			showImageView.setImageBitmap(tempCacheBitmap);
			tempCacheBitmap = null;
			return true;
		}else{
			showImageView.setTag(id);	//将ImageView和当前图片绑定，以便在下载完成后通过此ID来找到此ImageView
			if(options != null && options.getLoadingImageResource() > 0){
				showImageView.setImageResource(options.getLoadingImageResource());
			}else{
				showImageView.setImageDrawable(null);
			}
			return false;
		}
	}
	
	/**
	 * 尝试加载
	 * @param id
	 * @param url
	 * @param localCacheFile
	 * @param showImageView
	 * @param options
	 */
	final void tryLoad(String id, String url, File localCacheFile, ImageView showImageView, Options options, LoadRequest loadRequest){
		loadingImageViewSet.add(showImageView);	//先将当前ImageView存起来
		if(!loadingRequestSet.contains(id)){		//如果当前图片没有正在加载
			if(loadRequest == null){
				loadRequest = new LoadRequest(id, url, localCacheFile, showImageView, options);
			}
			if(loadingRequestSet.size() < configuration.getMaxThreadNumber()){	//如果尚未达到最大负荷，就开启线程加载
				loadingRequestSet.add(id);
				EasyNetwork.getThreadPool().submit(new LoadRunable(this, loadRequest));
			}else{
				synchronized (waitingRequestCircle) {	//否则，加到等待队列中
					waitingRequestCircle.add(loadRequest);
				}
			}
		}
	}
	
	/**
	 * 清除历史
	 */
	public final void clearHistory(){
		synchronized (loadingImageViewSet) {
			loadingImageViewSet.clear();
		}
		synchronized (loadingRequestSet) {
			loadingRequestSet.clear();
		}
		synchronized (waitingRequestCircle) {
			waitingRequestCircle.clear();
		}
	}
	
	/**
	 * 获取加载中显示视图集合
	 * @return
	 */
	final Set<ImageView> getLoadingImageViewSet() {
		return loadingImageViewSet;
	}

	/**
	 * 获取加载中请求ID集合
	 * @return
	 */
	final Set<String> getLoadingRequestSet() {
		return loadingRequestSet;
	}

	/**
	 * 获取等待请求集合
	 * @return 等待请求集合
	 */
	final CircleList<LoadRequest> getWaitingRequestCircle() {
		return waitingRequestCircle;
	}

	/**
	 * 获取Http客户端
	 * @return
	 */
	public final DefaultHttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * 获取配置
	 * @return
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * 输出LOG
	 * @param logContent LOG内容
	 */
	public void log(String logContent, boolean error){
		if(configuration.isDebugMode()){
			if(error){
				Log.e(configuration.getLogTag(), logContent);
			}else{
				Log.d(configuration.getLogTag(), logContent);
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