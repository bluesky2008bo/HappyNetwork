#EasyNetworkForAndroid

这是一个Android网络访问库，旨在用最简单的方式（同时又极具扩展性）来访问网络！

EasyNetworkForAndroid目前包含Http网络访问框架（EasyHttpClient）和图片加载器（ImageLoader）两部分：

##EasyHttpClient

EasyHttpClient是在开源项目android-async-http的基础上扩展而来的

###特征

>*	通过注解加反射来解析对象获取请求地址以及请求参数

##ImageLoader

ImageLoader是一个图片加载器，主要功能是从本地或者网络加载图片显示在ImageView上

###特征

>*  特征1：支持ViewHolder。即使你在ListView中使用了ViewHolder也依然可以使用ImageLoader来加载图片，并且可以保证图片显示绝对不会混乱。

>*  特征2：支持缓存。ImageLoader可以在本地或内存中缓存图片数据，并且在内存中缓存使用的是软引用，可确保在高效的加载图片的同时，也不会发生内存溢出。

>*  特征3：异步加载。ImageLoader采用线程池来处理每一个请求，可确保每一个请求都在一个单独的线程中加载。另外线程数量是有限制的（默认是50），当线程池负荷已满的时候，新的加载请求会放到等待区域中，而等待区域也是有数量限制的（默认是30），新的等待请求会将最旧的等待请求挤出等待区域，保证最新的请求会被及时处理。

>*  特征4：强大的自定义功能。通过Options对象可以自定义动画、默认图片、加载失败图片、图片处理、超时重试、缓存目录、缓存判定。另外你可以给ImageLoder设置一个默认的Options，也可以针对每一个请求都使用不一样的Options。

###示例

>在Application中初始化ImagLoader

```java
public class MyApplication extends Application {
    @Override
	public void onCreate() {
		super.onCreate();
		
		/* 初始化图片加载器 */
		Options options = new Options();
		options.setLoadingDrawableResId(R.drawable.image_loading);	//设置加载中显示的图片
		options.setLoadFailedDrawableResId(R.drawable.image_load_failed);	//设置加载失败时显示的图片
		options.setShowAnimationListener(new ShowAnimationListener() {	//设置显示动画监听器，用来获取显示图片的动画
			@Override
			public Animation onGetShowAnimation() {
				/* 创建一个从50%放大到100%并且持续0.5秒的缩放动画 */
				ScaleAnimation scaleAnimation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				scaleAnimation.setDuration(500);
				return scaleAnimation;
			}
		});
		options.setBitmapLoadListener(new RoundedBitmapLoadListener());	//设置图片加载监听器，这里传入的是一个可以将图片都处理为圆角的图片加载监听器
		ImageLoader.init(getBaseContext(), options);
	}
}
```

>在适配器中使用ImageLoader

```java
public class ImageAdapter extends BaseAdapter {
	private Context context;
	private String[] imageUrls;
	
	public ImageAdapter(Context context, String[] imageUrls){
		this.context = context;
		this.imageUrls = imageUrls;
	}

	@Override
	public Object getItem(int position) {
		return imageUrls[position];
	}

	@Override
	public int getCount() {
		return imageUrls.length;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int realPosition, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if(convertView == null){
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_image_loader, null);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.listItem_image);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		ImageLoader.getInstance().load(imageUrls[realPosition], viewHolder.image);
		return convertView;
	}
	
	class ViewHolder{
		ImageView image;
	}
}
```

###注意事项

>*  1：在使用ImageLoader之前你需要调用ImageLoader.init(Context, Options)方法来初始化ImageLoader。因为ImageLoader需要一个上下文来将下载好的图片缓存到Android/data/下面，另外也需要一个默认的Options来处理所有的加载请求。

>*  2：ImageLoader提供了一个单例，所以没有特殊需求的话，你只须通过ImageLoader.getInstance()方法获取其实例即可。