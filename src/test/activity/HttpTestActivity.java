package test.activity;

import me.xiaopan.easynetwork.android.R;
import me.xiaopan.easynetwork.android.http.EasyHttpClient;
import me.xiaopan.easynetwork.android.http.StringResponseHandler;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

/**
 * Http测试
 */
public class HttpTestActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_http);
		
		EasyHttpClient.getInstance().get("http://www.miui.com/forum.php", new StringResponseHandler(){
			@Override
			public void onStart() {
				findViewById(R.id.loading).setVisibility(View.VISIBLE);
			}

			@Override
			public void onSuccess(String responseContent) {
				((TextView) findViewById(R.id.text_main_content)).setText(Html.fromHtml("成功了："+responseContent));
			}

			@Override
			public void onFailure(HttpResponse httpResponse) {
				((TextView) findViewById(R.id.text_main_content)).setText("失败了："+httpResponse.getStatusLine().getStatusCode());
			}

			@Override
			public void onException(Throwable e) {
				e.printStackTrace();
				((TextView) findViewById(R.id.text_main_content)).setText("异常了："+e.getMessage());
			}

			@Override
			public void onEnd() {
				findViewById(R.id.loading).setVisibility(View.GONE);
			}
		});
		
//		EasyHttpClient.getInstance().get("http://e.hiphotos.baidu.com/album/w%3D2048/sign=4a605579d1160924dc25a51be03f34fa/1f178a82b9014a900a9e7492a8773912b31bee79.jpg", new BinaryHttpResponseHandler() {
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
//			public void onException(Throwable e) {
//				
//			}
//			
//			@Override
//			public void onEnd() {
//				
//			}
//		});
		
//		EasyHttpClient.getInstance().get("http://www.weather.com.cn/data/cityinfo/101010100.html", new JsonHttpResponseHandler<WeatherResponse>(WeatherResponse.class){
//			@Override
//			public void onStart() {
//				findViewById(R.id.loading).setVisibility(View.VISIBLE);
//			}
//
//			@Override
//			public void onSuccess(WeatherResponse t) {
//				((TextView) findViewById(R.id.text_main_content)).setText(t.toString());
//			}
//
//			@Override
//			public void onFailure(HttpResponse httpResponse) {
//				((TextView) findViewById(R.id.text_main_content)).setText("失败了："+httpResponse.getStatusLine().getStatusCode());
//			}
//
//			@Override
//			public void onException(Throwable e) {
//				e.printStackTrace();
//				((TextView) findViewById(R.id.text_main_content)).setText("异常了："+e.getMessage());
//			}
//
//			@Override
//			public void onEnd() {
//				findViewById(R.id.loading).setVisibility(View.GONE);
//			}
//		});
	}
}