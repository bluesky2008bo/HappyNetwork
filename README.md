#EasyNetworkForAndroid

这是一个Android网络访问库，旨在用最简单的方式（同时又极具扩展性）来访问网络！

EasyNetworkForAndroid目前包含Http网络访问框架（EasyHttpClient）和图片加载器（ImageLoader）两部分：

##EasyHttpClient

EasyHttpClient是在开源项目android-async-http的基础上扩展而来的

###特征

>* 通过注解加反射来解析对象获取请求地址以及请求参数；
>* 优化HttpResponseHandler的处理流程，具体的BinaryHttpResponseHandler、StringHttpResponseHandler以及JsonHttpResponseHandler的使用方式请参考演示程序中的StringActivity、BinaryActivity以及JsonActivity。

###请求对象配置以及使用详解：

####配置示例：

```java
/**
 * 百度搜索请求
 */
@Url("http://www.baidu.com/s")
public class BaiduSearchRequest extends BaseRequest {
    @Expose
    public String rsv_spt = "1";
	@Expose
	public String issp = "1";
	@Expose
	public String rsv_bp = "0";
	@Expose
	public String ie = "utf-8";
	@Expose
	public String tn = "98012088_3_dg";
	@Expose
	public String rsv_sug3 = "4";
	@Expose
	public String rsv_sug = "0";
	@Expose
	public String rsv_sug1 = "3";
	@Expose
	public String rsv_sug4 = "481";
	@Expose
	@SerializedName("wd")
	public String keyword;
	
	/**
	 * 创建一个百度搜索请求
	 * @param keyword 搜索关键字
	 */
	public BaiduSearchRequest(String keyword){
		this.keyword = keyword;
	}
}
```

####配置详解：

>* 请求地址的配置。通过``@Url``、``@Host``以及``@Path``三个注解来完成（都是加在类上的）：
    1. 使用``@Url``注解来指定完整的请求地址。例如：``@Url("http://www.baidu.com/s")``；
    2. 使用``@Host``注解来指定请求地址的主机部分，例如：``@Host("http://m.weather.com.cn")``；
    3. 使用``@Path``注解来指定请求地址的路径部分，例如：``@Path("data/101010100.html")``；
    4. ``@Host``+``"/"``+``@Path``==``@Url``;
    5. 以上``@Url``、``@Host``以及``@Path``注解都是可以继承的，因此你可以弄一个BaseRequest然后把请求地址的主机部分用``@Host``注解加在BaseRequest上，然后其他的请求都继承BaseRequest，这样一来其它的请求就只需添加``@Path``注解即可，同时也可以保证主机地址只会在一个地方定义；
    6. 在解析请求地址的时候会先检测``@Url``注解，如果有``@Url``注解并且值不为空就直接使用``@Url``注解的值作为请求地址，不会再考虑``@Host``和``@Path``注解；
    7. 在使用``@Host``和``@Path``注解来组织请求地址的时候会直接用“/”来拼接，所以请不要在``@Host``末尾或者``@Path``的开头加“/”。

>* 请求参数的配置。
    1. 加了``@Expose``注解的字段才会被转换成请求参数，因此请不要忘记给字段加上``@Expose``注解。另``外@Expose``注解是google gson开源项目中的因此需要依赖于gson jar包；
    2. 你还可以使用``@SerializedName``注解来指定参数名称，如果不加此注解的话，将会使用字段名称来作为参数名称。同样的``@SerializedName``注解也是google gson包中的；
    4. 对于``boolean``类型的字段你可以通过``@True``和``@False``注解来指定当字段值是true或false的时候其对应的转换成请求参数时的参数值；
    5. 对于Map类型的字段会将其每一对键值对都转换成请求参数。

>* 请求方式的配置。
    1. 如果要用get方式请求就在请求对象上加上``@Get``注解；
    2. 如果要用post方式请求就在请求对象上加上``@post``注解；
    3. 默认是get方式。

####使用示例：

```java
/**
 * 请求对象演示Demo
 */
public class RequestObjectActivity extends Activity {
    private WebViewManager webViewManager;
	private EditText keywordEdit;
	private Button searchButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request_object);
		keywordEdit = (EditText) findViewById(R.id.edit_requestObject_keyword);
		searchButton = (Button) findViewById(R.id.button_requestObject_search);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.closeSoftKeyboard(RequestObjectActivity.this);
				search(keywordEdit.getEditableText().toString().trim());
			}
		});
		webViewManager = new WebViewManager((WebView) findViewById(R.id.web1));
		
		keywordEdit.setText("王力宏");
		search(keywordEdit.getEditableText().toString().trim());
	}
	
	private void search(String keyword){
		EasyHttpClient.getInstance().get(new BaiduSearchRequest(keyword), new StringHttpResponseHandler(){
			@Override
			public void onStart() {
				searchButton.setEnabled(false);
				findViewById(R.id.loading).setVisibility(View.VISIBLE);
			}

			@Override
			public void onSuccess(String responseContent) {
				webViewManager.getWebView().loadData(responseContent, "text/html;charset=utf-8", null);
			}

			@Override
			public void onFailure(HttpResponse httpResponse) {
				Toast.makeText(getBaseContext(), "失败了，状态码："+httpResponse.getStatusLine().getStatusCode(), Toast.LENGTH_LONG).show();
				finish();
			}

			@Override
			public void onException(Throwable e) {
				e.printStackTrace();
				Toast.makeText(getBaseContext(), "异常了："+e.getMessage(), Toast.LENGTH_LONG).show();
				finish();
			}

			@Override
			public void onEnd() {
				searchButton.setEnabled(true);
				findViewById(R.id.loading).setVisibility(View.GONE);
			}
		});
	}

	@Override
	public void onBackPressed() {
		if(webViewManager.getWebView().canGoBack()){
			webViewManager.getWebView().goBack();
		}else{
			super.onBackPressed();
		}
	}
}
```

完整使用方式请参考演示程序中的RequestObjectActivity。

##ImageLoader

ImageLoader是一个图片加载器，主要功能是从本地或者网络加载图片显示在ImageView上

###特征

>* 支持ViewHolder。即使你在ListView中使用了ViewHolder也依然可以使用ImageLoader来加载图片，并且可以保证图片显示绝对不会混乱。

>* 支持缓存。ImageLoader可以在本地或内存中缓存图片数据，并且在内存中缓存使用的是软引用，可确保在高效的加载图片的同时，也不会发生内存溢出。

>* 异步加载。ImageLoader采用线程池来处理每一个请求，可确保每一个请求都在一个单独的线程中加载。另外线程数量是有限制的（默认是50），当线程池负荷已满的时候，新的加载请求会放到等待区域中，而等待区域也是有数量限制的（默认是30），新的等待请求会将最旧的等待请求挤出等待区域，保证最新的请求会被及时处理。

>* 强大的自定义功能。通过Options对象可以自定义动画、默认图片、加载失败图片、图片处理、超时重试、缓存目录、缓存判定。另外你可以给ImageLoder设置一个默认的Options，也可以针对每一个请求都使用不一样的Options。

###示例

####在Application中初始化ImagLoader

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

####在适配器中使用ImageLoader

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

1. 在使用ImageLoader之前你需要调用ImageLoader.init(Context, Options)方法来初始化ImageLoader。因为ImageLoader需要一个上下文来将下载好的图片缓存到Android/data/下面，另外也需要一个默认的Options来处理所有的加载请求。

2. ImageLoader提供了一个单例，所以没有特殊需求的话，你只须通过ImageLoader.getInstance()方法获取其实例即可。