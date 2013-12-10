# ![Logo](https://github.com/ixiaopan/EasyNetwork/raw/master/res/drawable-mdpi/ic_launcher.png) EasyNetwork

这是一个参考了android-async-http项目的Android网络访问库，旨在用最简单、最快捷的方式来访问网络！

###特征
>* 支持响应缓存功能，
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

>* 请求参数的配置：
    1. 加了``@Expose``注解的字段才会被转换成请求参数，因此请不要忘记给字段加上``@Expose``注解。另``外@Expose``注解是google gson开源项目中的因此需要依赖于gson jar包；
    2. 你还可以使用``@SerializedName``注解来指定参数名称，如果不加此注解的话，将会使用字段名称来作为参数名称。同样的``@SerializedName``注解也是google gson包中的；
    3. 默认使用请求字段的toString()方法来获取请求参数值，但以下几种类型的字段将会被特殊处理：
        * Map
            对于``Map``类型的字段``EasyHttpClient``会将其每一对键值对都转换成请求参数，而每一对键值对的键将作为参数名，键值对的值将作为参数值；
        * File
            对于``File``类型的字段EasyHttpClient将使用``RequestParams``的``put(String key, File file)``方法将其添加到``RequestParams``中；
        * ArrayList 
            对于``ArrayList``类型的字段EasyHttpClient将使用``RequestParams``的``put(String key, ArrayList<String> values)``方法将其添加到``RequestParams``中；
        * Boolean 
            对于``Boolean``类型的字段你可以通过``@True``和``@False``注解来指定当字段值是``true``或``false``的时候其对应的转换成请求参数时的参数值；
        * Enum
            对于``Enum``类型的参数你可以使用``@SerializedName``注解来指定其参数值，如果没有``@SerizlizedName``注解将使用Enum对象的name来作为参数值。

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

##License
```java
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
```