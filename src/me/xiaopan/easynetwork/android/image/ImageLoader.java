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
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import me.xiaopan.easynetwork.android.EasyNetwork;

import org.apache.http.HttpVersion;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

/**
 * 图片加载器，可以从网络或者本地加载图片，并且支持自动清除缓存
 */
public class ImageLoader{
    private static boolean enableOutputLogToConsole = true;	//输出Log到控制台
    private static Context context;	//上下文
	private static ConcurrentHashMap<String, SoftReference<Bitmap>> bitmapCacheMap;//软引用图片Map
	
	private String cacheDirName = "image_loader";	//缓存文件夹名称
	private String logTag = "ImageLoader";	//LogTag
	private Options defaultOptions;	//默认加载选项
	private DefaultHttpClient httpClient;	//Http客户端
	private int maxThreadNumber = 50;	//最大线程数
	private int maxWaitingNumber = 30;	//最大等待数
	private Set<ImageView> loadingImageViewSet;	//图片视图集合，这个集合里的每个尚未加载完成的视图身上都会携带有他要显示的图片的地址，当每一个图片加载完成之后都会在这个列表中遍历找到所有携带有这个这个图片的地址的视图，并把图片显示到这个视图上
	private Set<String> loadingRequestSet;	//正在加载的Url列表，用来防止同一个URL被重复加载
	private Circle<LoadRequest> waitingRequestCircle;	//等待处理的加载请求
	private ImageLoadHandler imageLoadHandler;	//加载处理器
	
	private Bitmap tempCacheBitmap;
	
	/**
	 * 创建图片加载器
	 * @param defaultDrawableResId 默认显示的图片
	 */
	public ImageLoader(){
		if(bitmapCacheMap == null){
			bitmapCacheMap = new ConcurrentHashMap<String, SoftReference<Bitmap>>();//软引用图片Map
		}
		
		imageLoadHandler = new ImageLoadHandler(this);
		loadingImageViewSet = new HashSet<ImageView>();//初始化图片视图集合
		loadingRequestSet = new HashSet<String>();//初始化加载中URL集合
		waitingRequestCircle = new Circle<LoadRequest>(maxWaitingNumber);//初始化等待处理的加载请求集合
	}
	
	/**
	 * 初始化
	 * @param context 上下文
	 * @param defaultOptions 默认加载选项
	 */
	public final void init(Context context, Options defaultOptions){
		ImageLoader.context = context;
		setDefaultOptions(defaultOptions);
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
		if(ImageLoaderUtils.isNotNullAndEmpty(url) && showImageView != null){
			try {
				String id = URLEncoder.encode(url, ImageLoaderUtils.CHARSET_NAME_UTF8);
				if(!tryShowImage(url, id, showImageView, options)){	//尝试显示图片，如果显示失败了就尝试加载
					tryLoad(id, url, ImageLoaderUtils.getCacheFile(this, context, options, id), showImageView, options, null);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
			if(showImageView != null){
				showImageView.setTag(null);
				if(options != null && options.getLoadingDrawableResId() > 0){
					showImageView.setImageResource(options.getLoadingDrawableResId());
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
		load(url, showImageView, getDefaultOptions());
	}
	
	/**
	 * 加载图片
	 * @param localFile 本地缓存文件，始终会优先加载本地缓存文件，只有在本地缓存文件不存在的话才会从网络获取
	 * @param showImageView 显示图片的视图
	 * @param imageUrl 图片下载地址，如果本地缓存文件不存在将从网络获取
	 * @param options 加载选项
	 */
	public final void load(File localFile, ImageView showImageView, String url, Options options){
		if((localFile != null || ImageLoaderUtils.isNotNullAndEmpty(url)) && showImageView != null){
			try{
				String id = URLEncoder.encode(localFile.getPath(), ImageLoaderUtils.CHARSET_NAME_UTF8);
				if(!tryShowImage(localFile.getPath(), id, showImageView, options)){	//尝试显示图片，如果显示失败了就尝试加载
					tryLoad(id, url, localFile, showImageView, options, null);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
			if(showImageView != null){
				showImageView.setTag(null);
				if(options != null && options.getLoadingDrawableResId() > 0){
					showImageView.setImageResource(options.getLoadingDrawableResId());
				}else{
					showImageView.setImageDrawable(null);
				}
			}
		}
	}
	
	/**
	 * 加载图片
	 * @param localFile 本地缓存文件，始终会优先加载本地缓存文件，只有在本地缓存文件不存在的话才会从网络获取
	 * @param showImageView 显示图片的视图
	 * @param imageUrl 图片下载地址，如果本地缓存文件不存在将从网络获取
	 */
	public final void load(File localFile, ImageView showImageView, String url){
		load(localFile, showImageView, url, getDefaultOptions());
	}
	
	/**
	 * 加载图片
	 * @param localFile 本地缓存文件，始终会优先加载本地缓存文件，只有在本地缓存文件不存在的话才会从网络获取
	 * @param showImageView 显示图片的视图
	 */
	public final void load(File localFile, ImageView showImageView){
		load(localFile, showImageView, null, getDefaultOptions());
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
		if(options != null && options.isCachedInMemory() && (tempCacheBitmap = getBitmapFromCache(id)) != null){
			showImageView.setTag(null);	//清空绑定关系
			log("从缓存加载图片："+url);
			loadingImageViewSet.remove(showImageView);
			showImageView.clearAnimation();
			showImageView.setImageBitmap(tempCacheBitmap);
			return true;
		}else{
			showImageView.setTag(id);	//将ImageView和当前图片绑定，以便在下载完成后通过此ID来找到此ImageView
			if(options != null && options.getLoadingDrawableResId() > 0){
				showImageView.setImageResource(options.getLoadingDrawableResId());
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
			if(loadingRequestSet.size() < maxThreadNumber){	//如果尚未达到最大负荷，就开启线程加载
				loadingRequestSet.add(id);
				EasyNetwork.getThreadPool().submit(new ImageLoadTask(this, loadRequest));
			}else{
				//否则，加到等待队列中
				synchronized (waitingRequestCircle) {
					waitingRequestCircle.put(loadRequest);
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
	 * 获取最大线程数
	 * @return 最大线程数
	 */
	public final int getMaxThreadNumber() {
		return maxThreadNumber;
	}

	/**
	 * 设置最大线程数 
	 * @param maxThreadNumber 最大线程数
	 */
	public final void setMaxThreadNumber(int maxThreadNumber) {
		this.maxThreadNumber = maxThreadNumber;
	}

	/**
	 * 获取最大等待数
	 * @return 最大等待数
	 */
	public final int getMaxWaitingNumber() {
		return maxWaitingNumber;
	}

	/**
	 * 设置最大等待数
	 * @param maxWaitingNumber 最大等待数
	 */
	public final void setMaxWaitingNumber(int maxWaitingNumber) {
		this.maxWaitingNumber = maxWaitingNumber;
	}

	/**
	 * 获取加载中显示视图集合
	 * @return
	 */
	public final Set<ImageView> getLoadingImageViewSet() {
		return loadingImageViewSet;
	}

	/**
	 * 获取加载中请求ID集合
	 * @return
	 */
	public final Set<String> getLoadingRequestSet() {
		return loadingRequestSet;
	}

	/**
	 * 获取等待请求集合
	 * @return 等待请求集合
	 */
	public final Circle<LoadRequest> getWaitingRequestCircle() {
		return waitingRequestCircle;
	}

	/**
	 * 获取加载处理器
	 * @return 加载处理器
	 */
	public final ImageLoadHandler getImageLoadHandler() {
		return imageLoadHandler;
	}

	/**
	 * 设置加载处理器
	 * @param loadHandler 加载处理器
	 */
	public final void setImageLoadHandler(ImageLoadHandler loadHandler) {
		this.imageLoadHandler = loadHandler;
	}
	
	/**
	 * 往缓存中添加图片
	 * @param id 地址
	 * @param bitmap 图片
	 * @return 
	 */
	public static final SoftReference<Bitmap> putBitmapToCache(String id, Bitmap bitmap){
		return bitmapCacheMap.put(id, new SoftReference<Bitmap>(bitmap));
	}
	
	/**
	 * 从缓存中获取图片
	 * @param id 地址
	 * @return 图片
	 */
	public static final Bitmap getBitmapFromCache(String id){
		SoftReference<Bitmap> softReferenceBitmap = bitmapCacheMap.get(id);
		if(softReferenceBitmap != null){
			Bitmap bitmap = softReferenceBitmap.get();
			if(bitmap == null){
				bitmapCacheMap.remove(id);//将当前地址从Map中删除
			}
			return bitmap;
		}else{
			return null;
		}
	}
	
	/**
	 * 从缓存中删除图片
	 * @param id 地址
	 * @return 图片
	 */
	public static final Bitmap removeBitmapFromCache(String id){
		SoftReference<Bitmap> softReferenceBitmap = bitmapCacheMap.remove(id);
		if(softReferenceBitmap != null){
			return softReferenceBitmap.get();
		}else{
			return null;
		}
	}
	
	/**
	 * 清除缓存
	 */
	public static final void clearCache(){
		if(bitmapCacheMap != null){
			bitmapCacheMap.clear();
		}
	}

    /**
     * 设置请求超时时间，默认是10秒
     * @param timeout 请求超时时间，单位毫秒
     */
    public final void setTimeout(int timeout){
        final HttpParams httpParams = getHttpClient().getParams();
        ConnManagerParams.setTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
    }

	/**
	 * 获取Http客户端用来发送请求
	 * @return
	 */
	public final DefaultHttpClient getHttpClient() {
		if(httpClient == null){
			BasicHttpParams httpParams = new BasicHttpParams();
			
			int defaultMaxConnections = 10;		//最大连接数
		    int defaultSocketTimeout = 10 * 1000;		//连接超时时间
		    int defaultSocketBufferSize = 8192;		//Socket缓存大小
			
	        ConnManagerParams.setTimeout(httpParams, defaultSocketTimeout);
	        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(defaultMaxConnections));
	        ConnManagerParams.setMaxTotalConnections(httpParams, defaultMaxConnections);

	        HttpConnectionParams.setSoTimeout(httpParams, defaultSocketTimeout);
	        HttpConnectionParams.setConnectionTimeout(httpParams, defaultSocketTimeout);
	        HttpConnectionParams.setTcpNoDelay(httpParams, true);
	        HttpConnectionParams.setSocketBufferSize(httpParams, defaultSocketBufferSize);

	        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams); 
		}
		return httpClient;
	}

	/**
	 * 设置Http客户端
	 * @param httpClient Http客户端
	 */
	public final void setHttpClient(DefaultHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * 获取缓存目录名称
	 * @return 缓存目录名称，默认为“image_loader”
	 */
	public final String getCacheDirName() {
		return cacheDirName;
	}

	/**
	 * 设置缓存目录名称
	 * @param cacheDirName 缓存目录名称，默认为“image_loader”
	 */
	public final void setCacheDirName(String cacheDirName) {
		this.cacheDirName = cacheDirName;
	}

	/**
	 * 获取Log Tag
	 * @return Log Tag，默认为“ImageLoader”
	 */
	public final String getLogTag() {
		return logTag;
	}

	/**
	 * 设置Log Tag
	 * @param logTag，默认为“ImageLoader”
	 */
	public final void setLogTag(String logTag) {
		this.logTag = logTag;
	}

	/**
	 * 判断是否输出Log到控制台
	 * @return 是否输出Log到控制台
	 */
	public static boolean isEnableOutputLogToConsole() {
		return enableOutputLogToConsole;
	}

	/**
	 * 设置是否输出Log到控制台
	 * @param enableOutputLogToConsole 是否输出Log到控制台
	 */
	public static void setEnableOutputLogToConsole(boolean enableOutputLogToConsole) {
		ImageLoader.enableOutputLogToConsole = enableOutputLogToConsole;
	}

	/**
	 * 获取默认的加载选项
	 * @return 默认的加载选项
	 */
	public final Options getDefaultOptions() {
		return defaultOptions;
	}

	/**
	 * 设置默认的加载选项
	 * @param defaultOptions 默认的加载选项
	 */
	public final void setDefaultOptions(Options defaultOptions) {
		this.defaultOptions = defaultOptions;
	}
	
	/**
	 * 输出LOG
	 * @param logContent LOG内容
	 */
	public void log(String logContent, boolean error){
		if(isEnableOutputLogToConsole()){
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