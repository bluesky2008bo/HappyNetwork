package test.activity;

import me.xiaopan.networkeasy.EasyHttpClient;
import me.xiaopan.networkeasy.JsonHttpResponseHandler;
import me.xiaopan.networkeasy.JsonHttpResponseHandler.JsonHttpResponseHandleListener;
import me.xiaopan.networkeasy.R;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		EasyHttpClient.getInstance().get("http://www.miui.com/forum.php", new StringHttpResponseHandler(getBaseContext(), new StringHttpResponseHandleListener(){
//			@Override
//			public void onStart() {
//				findViewById(R.id.loading).setVisibility(View.VISIBLE);
//				Log.w("测试", "开始");
//			}
//
//			@Override
//			public void onSuccess(String responseContent) {
//				((TextView) findViewById(R.id.text_main_content)).setText(Html.fromHtml("成功了："+responseContent));
//				Log.w("测试", "成功");
//			}
//
//			@Override
//			public void onFailure(HttpResponse httpResponse) {
//				((TextView) findViewById(R.id.text_main_content)).setText("失败了："+httpResponse.getStatusLine().getStatusCode());
//				Log.w("测试", "失败");
//			}
//
//			@Override
//			public void onException(Context context, Throwable e) {
//				e.printStackTrace();
//				((TextView) findViewById(R.id.text_main_content)).setText("异常了："+e.getMessage());
//				Log.w("测试", "异常");
//			}
//
//			@Override
//			public void onEnd() {
//				findViewById(R.id.loading).setVisibility(View.GONE);
//				Log.w("测试", "结束");
//			}
//		}));
		
//		EasyHttpClient.getInstance().get("http://e.hiphotos.baidu.com/album/w%3D2048/sign=4a605579d1160924dc25a51be03f34fa/1f178a82b9014a900a9e7492a8773912b31bee79.jpg", new BinaryHttpResponseHandler(getBaseContext(), new BinaryHttpResponseHandleListener() {
//			@Override
//			public void onStart() {
//				
//			}
//			
//			@Override
//			public void onSuccess(byte[] binaryData) {
//				((ImageView) findViewById(R.id.image_main)).setImageBitmap(BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length));
//				findViewById(R.id.image_main).setVisibility(View.VISIBLE);
//			}
//			
//			@Override
//			public void onFailure(HttpResponse httpResponse) {
//				
//			}
//			
//			@Override
//			public void onException(Context context, Throwable e) {
//				
//			}
//			
//			@Override
//			public void onEnd() {
//				
//			}
//		}));
		
		EasyHttpClient.getInstance().get("http://www.weather.com.cn/data/cityinfo/101010100.html", 
				new JsonHttpResponseHandler(getBaseContext(), WeatherResponse.class, new JsonHttpResponseHandleListener<WeatherResponse>() {
			@Override
			public void onStart() {
				findViewById(R.id.loading).setVisibility(View.VISIBLE);
			}

			@Override
			public void onSuccess(WeatherResponse t) {
				((TextView) findViewById(R.id.text_main_content)).setText(t.toString());
			}

			@Override
			public void onFailure(HttpResponse httpResponse) {
				((TextView) findViewById(R.id.text_main_content)).setText("失败了："+httpResponse.getStatusLine().getStatusCode());
			}

			@Override
			public void onException(Context context, Throwable e) {
				e.printStackTrace();
				((TextView) findViewById(R.id.text_main_content)).setText("异常了："+e.getMessage());
			}

			@Override
			public void onEnd() {
				findViewById(R.id.loading).setVisibility(View.GONE);
			}
		}));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}