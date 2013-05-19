package me.xiaopan.easynetwork.android;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

/**
 * 图片加载器，可以从网络或者本地加载图片，并且无操作1分钟后自动清除缓存
 */
public class ImageLoader {
	private static final int DEFAULT_MAX_CONNECTIONS = 10;	//最大连接数
    private static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;	//连接超时时间
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;	//Socket缓存大小
	private static final ConcurrentHashMap<String, SoftReference<Bitmap>> softReferenceMap = new ConcurrentHashMap<String, SoftReference<Bitmap>>();//软引用图片Map
	private static ImageLoader imageLoaderInstance; //图片加载器的实例，用来实现单例模式
	private Set<ImageView> imageViewSet;	//图片视图集合，这个集合里的每个尚未加载完成的视图身上都会携带有他要显示的图片的地址，当每一个图片加载完成之后都会在这个列表中遍历找到所有携带有这个这个图片的地址的视图，并把图片显示到这个视图上
	private Set<String> loadingUrlSet;	//正在加载的Url列表，用来防止同一个URL被重复加载
	private Circle<ImageLoadRequest> waitingLoadCircle;	//等待处理的加载请求
	private AutoClearCacheRunnable autoClearCacheRunnable;	//自动清除缓存处理对象
	private Handler autoClearCacheHandler;	//自动清除缓存处理器
	private Options loadOptions;	//图片加载选项
	private boolean autoClearCache = false;	//自动清除缓存，默认关闭此功能
	private int defaultDrawableResId = -5;	//默认图片的资源ID
	private long autoClearCacheTimerTime = 60000;	//自动清除缓存倒计时时间，单位毫秒。当1分钟之内没有任何操作就会自动清空缓存
	private int maxThreadNumber = 30;	//最大线程数
	private int maxWaitingNumber = 30;	//最大等待数
	private OnGetShowAnimationListener showImageListener;	//获取显示图片动画的监听器
	private int maxRetryCount = 3;	//最大重试次数
	private DefaultHttpClient defaultHttpClient;
	
	/**
	 * 创建图片加载器
	 * @param defaultDrawableResId 默认显示的图片
	 */
	public ImageLoader(int defaultDrawableResId){
		this.defaultDrawableResId = defaultDrawableResId;//初始化默认图片的资源ID
		this.autoClearCacheHandler = new Handler();//初始化自动清除缓存处理器
		this.autoClearCacheRunnable = new AutoClearCacheRunnable();//初始化自动清除缓存处理对象
		this.imageViewSet = new HashSet<ImageView>();//初始化图片视图集合
		this.loadingUrlSet = new HashSet<String>();//初始化加载中URL集合
		this.waitingLoadCircle = new Circle<ImageLoadRequest>(getMaxWaitingNumber());//初始化等待处理的加载请求集合
		
		this.loadOptions = new Options();//初始化图片加载选项
//		this.loadOptions.inSampleSize = 2;
		this.loadOptions.inPreferredConfig = Bitmap.Config.RGB_565;   
		this.loadOptions.inPurgeable = true;  
		this.loadOptions.inInputShareable = true;
		
		//设置默认的图片显示监听器
		this.showImageListener = new OnGetShowAnimationListener() {
			@Override
			public Animation onGetShowAnimation() {
				AlphaAnimation alphaAnimation = new AlphaAnimation(0.6f, 1.0f);
				alphaAnimation.setDuration(500);
				return alphaAnimation;
			}
		};
	}
	
	/**
	 * 创建图片加载器
	 */
	public ImageLoader(){
		this(-5);
	}
	
	/**
	 * 获取图片加载器的实例，每执行一次此方法就会清除一次历史记录
	 * @return 图片加载器的实例
	 */
	public static final ImageLoader getInstance(){
		if(imageLoaderInstance == null){
			imageLoaderInstance = new ImageLoader();
		}else{
			imageLoaderInstance.clearHistory();
		}
		return imageLoaderInstance;
	}
	
	/**
	 * 获取图片加载器的实例，每执行一次此方法就会清除一次历史记录
	 * @param defaultDrawableResId 默认的图片的资源ID
	 * @return 图片加载器的实例
	 */
	public static final ImageLoader getInstance(int defaultDrawableResId){
		if(imageLoaderInstance == null){
			imageLoaderInstance = new ImageLoader(defaultDrawableResId);
		}else{
			imageLoaderInstance.setDefaultDrawableResId(defaultDrawableResId);
			imageLoaderInstance.clearHistory();
		}
		return imageLoaderInstance;
	}
	
	/**
	 * 从网络加载
	 * @param imageUrl 图片地址
	 * @param imageView 显示图片的视图
	 * @param isCache 是否缓存到内存中
	 */
	public final void fromNetwork(String imageUrl, ImageView imageView, boolean isCache){
		if(imageUrl != null && imageView != null){
			ImageLoadRequest imageLoad = new ImageLoadRequest();
			imageLoad.setUrl(imageUrl);
			imageLoad.setAddress(imageUrl);
			imageLoad.setImageView(imageView);
			imageLoad.setCache(isCache);
			imageLoad.setLoadWay(LoadWay.FROM_NET);
			load(imageLoad);
		}
	}
	
	/**
	 * 从网络加载，默认缓存到内存中
	 * @param imageUrl 图片地址
	 * @param imageView 显示图片的视图
	 */
	public final void fromNetwork(String imageUrl, ImageView imageView){
		fromNetwork(imageUrl, imageView, true);
	}

	/**
	 * 从本地加载
	 * @param localSourceFile 本地源文件
	 * @param imageView 显示视图
	 * @param isCache 是否缓存到内存中
	 */
	public final void fromLocal(File localSourceFile, ImageView imageView, boolean isCache){
		if(localSourceFile != null && imageView != null){
			ImageLoadRequest imageLoad = new ImageLoadRequest();
			imageLoad.setLocalSourceFile(localSourceFile);
			imageLoad.setAddress(localSourceFile.getPath());
			imageLoad.setImageView(imageView);
			imageLoad.setCache(isCache);
			imageLoad.setLoadWay(LoadWay.FROM_LOCAL);
			load(imageLoad);
		}
	}

	/**
	 * 从本地加载，默认缓存到内存中
	 * @param localSourceFile 本地源文件
	 * @param imageView 显示视图
	 */
	public final void fromLocal(File localSourceFile, ImageView imageView){
		fromLocal(localSourceFile, imageView, true);
	}
	
	/**
	 * 如果源文件存在就从源文件中读取数据，否则就从网络地址上下载
	 * @param localSourceFile 本地源文件
	 * @param imageView 显示视图
	 * @param imageUrl 网络地址
	 * @param isSave 当需要从网络上下载图片时，是否需要将图片保存到源文件中
	 * @param isCache 是否缓存到内存中
	 */
	public final void fromLocalByPriority(File localSourceFile, ImageView imageView, String imageUrl, boolean isSave, boolean isCache){
		if(imageView != null && (localSourceFile != null || imageUrl != null)){
			ImageLoadRequest imageLoad = new ImageLoadRequest();
			imageLoad.setLocalSourceFile(localSourceFile);
			imageLoad.setImageView(imageView);
			imageLoad.setUrl(imageUrl);
			imageLoad.setAddress(localSourceFile.getPath());
			imageLoad.setSave(isSave);
			imageLoad.setCache(isCache);
			imageLoad.setLoadWay(LoadWay.FROM_LOCAL_BY_PRIORITY);
			load(imageLoad);
		}
	}
	
	/**
	 * 如果源文件存在就从源文件中读取数据，否则就从网络地址上下载，并默认缓存到内存中
	 * @param localSourceFile 源文件
	 * @param imageView 显示视图
	 * @param imageUrl 网络地址
	 * @param isSave 当需要从网络上下载图片时，是否需要将图片保存到源文件中
	 */
	public final void fromLocalByPriority(File localSourceFile, ImageView imageView, String imageUrl, boolean isSave){
		fromLocalByPriority(localSourceFile, imageView, imageUrl, isSave, true);
	}
	
	/**
	 * 如果本地文件存在就从本地文件中读取数据，否则就从网络地址上下载，并默认缓存到内存中且保存图片到本地文件中
	 * @param localSourceFile 本地文件
	 * @param imageView 显示视图
	 * @param imageUrl 网络地址
	 */
	public final void fromLocalByPriority(File localSourceFile, ImageView imageView, String imageUrl){
		fromLocalByPriority(localSourceFile, imageView, imageUrl, true, true);
	}
	
	/**
	 * 加载
	 * @param imageLoad 图片加载对象
	 */
	public final void load(ImageLoadRequest imageLoad){
		//尝试显示图片，如果显示失败（说明缓存中没有相应的图片）并且当前url尚未开始下载
		if(!tryShowImage(imageLoad) && putLoadingImageLoad(imageLoad)){
			//创建图片加载任务并启动
			new ImageLoadTask(this).execute(imageLoad);
		}
	}
	
	/**
	 * 尝试显示图片
	 * @param imageLoad
	 */
	private final boolean tryShowImage(ImageLoadRequest imageLoad){
		boolean result = false;

		//锁定当前显示视图
		synchronized (imageLoad.getImageView()) {
			//绑定
			imageLoad.getImageView().setTag(imageLoad.getAddress());
			
			//将当前视图存起来
			synchronized (imageViewSet) {
				imageViewSet.add(imageLoad.getImageView());
			}
			
			//重置自动清除缓存计时器
			if(isAutoClearCache()){
				resetAutoClearCacheTimer();
			}
	
			//根据地址从缓存中获取图片
			Bitmap bitmap = getBitmapFromCache(imageLoad.getAddress());
		
			//如果缓存中存在相对的图片就显示，否则显示默认图片或者显示空
			if(bitmap != null){
				showImage(imageLoad.getImageView(), bitmap, false);
				result = true;
			}else{
				if(getDefaultDrawableResId() != -5){
					imageLoad.getImageView().setImageResource(getDefaultDrawableResId());
				}else{
					imageLoad.getImageView().setImageDrawable(null);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 显示图片
	 * @param imageView 显示图片的视图
	 * @param bitmap 要显示的图片
	 * @param useAnimation 显示图片的时候是否使用动画
	 */
	private void showImage(final ImageView imageView, final Bitmap bitmap, boolean useAnimation){
		//如果使用动画，就视图设置动画，否则清除动画
		if(useAnimation){
			//如果现实图片监听器不为null，就视图设置显示动画
			if(getShowImageListener() != null){
				Animation animation = getShowImageListener().onGetShowAnimation();
				if(animation != null){
					imageView.setAnimation(animation);
				}                          
			}
		}else{
			imageView.setAnimation(null);
		}
		imageView.setImageBitmap(bitmap);
	}
	
	/**
	 * 放入一个图片加载对象，放入之后这个图片加载对象的状态就变成正在加载，直到这个图片加载对象加载完成从Url列表中删除才可以再次放入
	 * @param url 地址
	 * @return true：放入成功，说明当前图片加载对象尚未开始加载；false：放入失败，说明当前图片加载对象正在加载，不可以重复加载
	 */
	public final boolean putLoadingImageLoad(ImageLoadRequest imageLoad){
		boolean result = false;
		synchronized (loadingUrlSet) {
			//如果尚未达到最大负荷，就将当前下载对象的地址添加到正在下载集合中，否则将当前下载对象添加到等待队列中
			if(loadingUrlSet.size() < getMaxThreadNumber()){
				result = loadingUrlSet.add(imageLoad.getAddress());//如果放入失败，说明当前图片正在下载不必重复下载
			}else{
				synchronized (waitingLoadCircle) {
					waitingLoadCircle.put(imageLoad);
				}
			}
		}
		return result;
	}
	
	/**
	 * 删除一个图片加载对象，删除之后，这个图片加载对象的状态就变为加载完成了
	 * @param imageLoad 图片加载对象
	 * @return true：删除成功；false：删除失败，也许之前列表中不存在此图片加载对象。
	 */
	public final boolean removeLoadingImageLoad(ImageLoadRequest imageLoad){
		synchronized (loadingUrlSet) {
			return loadingUrlSet.remove(imageLoad.getAddress());
		}
	}
	
	/**
	 * 往缓存中添加图片
	 * @param address 地址
	 * @param bitmap 图片
	 */
	public static final void putBitmapToCache(String address, Bitmap bitmap){
		synchronized (softReferenceMap) {
			softReferenceMap.put(address, new SoftReference<Bitmap>(bitmap));
		}
	}
	
	/**
	 * 从缓存中获取图片
	 * @param address 地址
	 * @return 图片
	 */
	public static final Bitmap getBitmapFromCache(String address){
		//根据地址从缓存中获取图片
		Bitmap bitmap = null;
		//锁定缓存Map
		synchronized (softReferenceMap) {
			//从Map中根据地址获取软引用图片
			SoftReference<Bitmap> softReferenceBitmap = softReferenceMap.get(address);
			//如果存在
			if(softReferenceBitmap != null){
				//从软引用中获取图片
				bitmap = softReferenceBitmap.get();
				//如果图片已经被回收了
				if(bitmap == null){
					//将当前地址从Map中删除
					softReferenceMap.remove(address);
				}
			}
		}
		return bitmap;
	}
	
	/**
	 * 从缓存中删除图片
	 * @param address 地址
	 * @return 图片
	 */
	public static final Bitmap removeBitmapFromCache(String address){
		//根据地址从缓存中获取图片
		Bitmap bitmap = null;
		//锁定缓存Map
		synchronized (softReferenceMap) {
			//从Map中根据地址获取软引用图片
			SoftReference<Bitmap> softReferenceBitmap = softReferenceMap.remove(address);
			//如果存在
			if(softReferenceBitmap != null){
				//从软引用中获取图片
				bitmap = softReferenceBitmap.get();
			}
		}
		return bitmap;
	}
	
	/**
	 * 清除缓存
	 */
	public static final void clearCache(){
		synchronized (softReferenceMap) {
			softReferenceMap.clear();
		}
	}
	
	/**
	 * 清除历史
	 */
	public final void clearHistory(){
		synchronized (loadingUrlSet) {
			loadingUrlSet.clear();
		}
		synchronized (imageViewSet) {
			imageViewSet.clear();
		}
		synchronized (waitingLoadCircle) {
			waitingLoadCircle.clear();
		}
	}
	
	/**
	 * 重置自动清除缓存计时器
	 */
	public final void resetAutoClearCacheTimer() {
		synchronized (autoClearCacheHandler) {
			autoClearCacheHandler.removeCallbacks(autoClearCacheRunnable);
			autoClearCacheHandler.postDelayed(autoClearCacheRunnable, getAutoClearCacheTimerTime());
		}
	}
	
	/**
	 * 判断是否自动清除缓存
	 * @return 是否自动清除缓存
	 */
	public final boolean isAutoClearCache() {
		return autoClearCache;
	}

	/**
	 * 设置是否自动清除缓存
	 * @param autoClearCache 是否自动清除缓存
	 */
	public final void setAutoClearCache(boolean autoClearCache) {
		if(this.autoClearCache != autoClearCache){
			this.autoClearCache = autoClearCache;
			synchronized (autoClearCacheHandler) {
				autoClearCacheHandler.removeCallbacks(autoClearCacheRunnable);
				if(isAutoClearCache()){
					autoClearCacheHandler.postDelayed(autoClearCacheRunnable, getAutoClearCacheTimerTime());
				}
			}
		}
	}

	/**
	 * 获取默认图片的资源ID
	 * @return 默认图片的资源ID
	 */
	public final int getDefaultDrawableResId() {
		return defaultDrawableResId;
	}

	/**
	 * 设置默认图片的资源ID
	 * @param defaultDrawableResId 默认图片的资源ID
	 */
	public final void setDefaultDrawableResId(int defaultDrawableResId) {
		this.defaultDrawableResId = defaultDrawableResId;
	}

	/**
	 * 获取自动清除缓存计时时间
	 * @return 自动清除缓存计时时间
	 */
	public final long getAutoClearCacheTimerTime() {
		return autoClearCacheTimerTime;
	}

	/**
	 * 设置自动清除缓存计时时间
	 * @param autoClearCacheTimerTime 自动清除缓存计时时间
	 */
	public final void setAutoClearCacheTimerTime(long autoClearCacheTimerTime) {
		this.autoClearCacheTimerTime = autoClearCacheTimerTime;
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
	 * 获取加载选项
	 * @return 加载选项
	 */
	public final Options getLoadOptions() {
		return loadOptions;
	}

	/**
	 * 设置加载选项
	 * @param loadOptions 加载选项
	 */
	public final void setLoadOptions(Options loadOptions) {
		this.loadOptions = loadOptions;
	}

	/**
	 * 获取图片视图集合
	 * @return 图片视图集合
	 */
	public final Set<ImageView> getImageViewSet() {
		return imageViewSet;
	}

	/**
	 * 设置图片视图集合
	 * @param imageViewSet 图片视图集合
	 */
	public final void setImageViewSet(Set<ImageView> imageViewSet) {
		this.imageViewSet = imageViewSet;
	}

	/**
	 * 获取等待加载队列
	 * @return 等待加载队列
	 */
	public final Circle<ImageLoadRequest> getWaitingLoadCircle() {
		return waitingLoadCircle;
	}

	/**
	 * 设置等待加载队列
	 * @param waitingLoadCircle 等待加载队列
	 */
	public final void setWaitingLoadCircle(Circle<ImageLoadRequest> waitingLoadCircle) {
		this.waitingLoadCircle = waitingLoadCircle;
	}
	
	/**
	 * 获取显示图片动画的监听器
	 * @return 获取显示图片动画的监听器
	 */
	public final OnGetShowAnimationListener getShowImageListener() {
		return showImageListener;
	}

	/**
	 * 设置获取显示图片动画的监听器
	 * @param showImageListener 获取显示图片动画的监听器
	 */
	public final void setShowImageListener(OnGetShowAnimationListener showImageListener) {
		this.showImageListener = showImageListener;
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
     * 设置请求超时时间，默认是10秒
     * @param timeout 请求超时时间，单位毫秒
     */
    public void setTimeout(int timeout){
        final HttpParams httpParams = defaultHttpClient.getParams();
        ConnManagerParams.setTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
    }

	/**
	 * 自动清除缓存
	 */
	private class AutoClearCacheRunnable implements Runnable{
		@Override
		public void run() {
			ImageLoader.clearCache();
		}
	}
	
	/**
	 * 图片加载对象
	 */
	private class ImageLoadRequest {
		private String url;
		private File localSourceFile;
		private String address;
		private Bitmap loadResult;
		private LoadWay loadWay;
		private ImageView imageView;
		private boolean isCache;
		private boolean isSave;
		private int loads;//加载次数
		
		public String getUrl() {
			return url;
		}
		
		public void setUrl(String url) {
			this.url = url;
		}
		
		public File getLocalSourceFile() {
			return localSourceFile;
		}
		
		public void setLocalSourceFile(File localSourceFile) {
			this.localSourceFile = localSourceFile;
		}
		
		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public Bitmap getLoadResult() {
			return loadResult;
		}

		public void setLoadResult(Bitmap loadResult) {
			this.loadResult = loadResult;
		}

		public LoadWay getLoadWay() {
			return loadWay;
		}
		
		public void setLoadWay(LoadWay loadWay) {
			this.loadWay = loadWay;
		}

		public ImageView getImageView() {
			return imageView;
		}

		public void setImageView(ImageView imageView) {
			this.imageView = imageView;
		}

		public boolean isCache() {
			return isCache;
		}

		public void setCache(boolean isCache) {
			this.isCache = isCache;
		}

		public boolean isSave() {
			return isSave;
		}

		public void setSave(boolean isSave) {
			this.isSave = isSave;
		}

		public int getLoads() {
			return loads;
		}

		public void setLoads(int loads) {
			this.loads = loads;
		}
	}
	
	/**
	 * 图片加载任务
	 */
	private class ImageLoadTask extends AsyncTask<ImageLoadRequest, Integer, ImageLoadRequest> {
		/**
		 * 图片加载器
		 */
		private ImageLoader imageLoader;
		
		/**
		 * 创建图片加载任务
		 * @param imageLoad 图片加载对象
		 */
		public ImageLoadTask(ImageLoader imageLoader){
			this.imageLoader = imageLoader;
		}
		
		@Override
		protected ImageLoadRequest doInBackground(ImageLoadRequest... params) {
			ImageLoadRequest imageLoad = params[0];
			
			//如果是从网络加载
			if(imageLoad.getLoadWay() == LoadWay.FROM_NET){
				if(imageLoad.getUrl() != null){
					downloadImage(imageLoad, imageLoader.getLoadOptions());
				}
			//如果是从本地加载
			}else if(imageLoad.getLoadWay() == LoadWay.FROM_LOCAL){
				if(imageLoad.getLocalSourceFile() != null && imageLoad.getLocalSourceFile().exists()){
					imageLoad.setLoadResult(BitmapFactory.decodeFile(imageLoad.getLocalSourceFile().getPath(), imageLoader.getLoadOptions()));
				}
			//如果是先从本地加载
			}else if(imageLoad.getLoadWay() == LoadWay.FROM_LOCAL_BY_PRIORITY){
				if(imageLoad.getLocalSourceFile() != null && imageLoad.getLocalSourceFile().exists()){
					imageLoad.setLoadResult(BitmapFactory.decodeFile(imageLoad.getLocalSourceFile().getPath(), imageLoader.getLoadOptions()));
				}else{
					if(imageLoad.getUrl() != null){
						downloadImage(imageLoad, imageLoader.getLoadOptions());
					}
				}
			}
			
			return imageLoad;
		}

		@Override
		protected void onPostExecute(ImageLoadRequest imageLoad) {
			//结果不为null
			if(imageLoad.getLoadResult() != null){
				//如果需要缓存到内存中就将结果放到缓存Map中
				if(imageLoad.isCache()){
					ImageLoader.putBitmapToCache(imageLoad.getAddress(), imageLoad.getLoadResult());
				}
				
				//遍历图片视图，找到其绑定的地址同当前下载的地址一样的图片视图，并将结果显示到图片视图上
				synchronized (imageLoader.getImageViewSet()) {
					for(ImageView imageView : imageLoader.getImageViewSet()){
						synchronized (imageView) {
							Object object = imageView.getTag();
							if(object != null && imageLoad.getAddress().equals(object.toString())){
								imageLoader.showImage(imageView, imageLoad.getLoadResult(), true);
							}
						}
					}
				}
			}
			
			//将当前下载对象从正在下载集合中删除
			imageLoader.removeLoadingImageLoad(imageLoad);
			
			//从等待队列中取出等待下载的对象并执行
			synchronized (imageLoader.getWaitingLoadCircle()) {
				ImageLoadRequest waitImageLoad = imageLoader.getWaitingLoadCircle().remove();
				if(waitImageLoad != null){
					if(imageLoader.putLoadingImageLoad(waitImageLoad)){
						new ImageLoadTask(imageLoader).execute(waitImageLoad);
					}
				}
			}
		}
		
		/**
		 * 下载图片
		 * @param url 图片地址
		 * @return 图片
		 * @param saveFile 将下载好的图片保存到此文件中
		 */
		public final void downloadImage(final ImageLoadRequest imageLoad, final Options loadOptions) {
			if(defaultHttpClient == null){
				BasicHttpParams httpParams = new BasicHttpParams();

		        ConnManagerParams.setTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);
		        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(DEFAULT_MAX_CONNECTIONS));
		        ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);

		        HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);
		        HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);
		        HttpConnectionParams.setTcpNoDelay(httpParams, true);
		        HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);

		        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
				schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
				defaultHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams); 
			}
			
			File parentDir = null;//保存图片的文件的父目录
			boolean createNewDir = false;//true：父目录之前不存在是现在才创建的，当发生异常时需要删除
			boolean createNewFile = false;//true：保存图片的文件之前不存在是现在才创建的，当发生异常时需要删除
			BufferedInputStream bufferedfInputStream = null;
			BufferedOutputStream bufferedOutputStream = null;
			try {
				HttpResponse httpResponse = defaultHttpClient.execute(new HttpGet(imageLoad.getUrl()));
				//如果需要将数据保存到本地，并且保存文件不为null
				if(imageLoad.isSave() && imageLoad.getLocalSourceFile() != null){
					boolean isContinue = true;
					//如果保存文件不存在
					if(!imageLoad.getLocalSourceFile().exists()){
						//获取其父目录
						parentDir = imageLoad.getLocalSourceFile().getParentFile();
						//如果父目录同样不存在
						if(!parentDir.exists()){
							//创建父目录
							createNewDir = parentDir.mkdirs();
						}
						createNewFile = imageLoad.getLocalSourceFile().createNewFile();
						isContinue = createNewFile;
					}
					
					//如果继续
					if(isContinue){
						bufferedfInputStream = new BufferedInputStream(httpResponse.getEntity().getContent());
						bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(imageLoad.getLocalSourceFile(), false));
						byte[] bytes = new byte[1024];
						int number;
						while((number = bufferedfInputStream.read(bytes)) != -1){
							bufferedOutputStream.write(bytes, 0, number);
						}
						bufferedfInputStream.close();
						bufferedOutputStream.flush();
						bufferedOutputStream.close();
						imageLoad.setLoadResult(BitmapFactory.decodeFile(imageLoad.getLocalSourceFile().getPath(), loadOptions));
					}
				}else{
					byte[] bytes = EntityUtils.toByteArray(new BufferedHttpEntity(httpResponse.getEntity()));
					imageLoad.setLoadResult(BitmapFactory.decodeByteArray(bytes, 0, bytes.length, loadOptions));
				}
			} catch (Throwable e2) {
				e2.printStackTrace();
				if(bufferedfInputStream != null){
					try {
						bufferedfInputStream.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
				if(bufferedOutputStream != null){
					try {
						bufferedOutputStream.flush();
						bufferedOutputStream.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				if(createNewDir && parentDir != null){
					parentDir.delete();
				}
				if(createNewFile){
					imageLoad.getLocalSourceFile().delete();
				}
				
				//加载次数加1
				imageLoad.setLoads(imageLoad.getLoads()+1);
				
				//如果尚未达到最大重试次数，那么就再尝试一次
				if(imageLoad.getLoads() < getMaxRetryCount()){
					downloadImage(imageLoad, loadOptions);
				}
			}
		}
	}
	
	/**
	 * 加载方式
	 */
	private enum LoadWay {
		/**
		 * 从网络加载
		 */
		FROM_NET, 
		/**
		 * 从本地加载
		 */
		FROM_LOCAL, 
		/**
		 * 优先从本地读取（如果从本地读取失败，再从网络上下载）
		 */
		FROM_LOCAL_BY_PRIORITY
	}
	
	/**
	 * 当获取显示动画监听器
	 */
	public interface OnGetShowAnimationListener{
		public Animation onGetShowAnimation();
	}
}