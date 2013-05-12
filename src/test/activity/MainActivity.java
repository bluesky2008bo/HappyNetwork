package test.activity;

import me.xiaopan.networkeasy.EasyHttpClient;
import me.xiaopan.networkeasy.R;
import me.xiaopan.networkeasy.StringHttpResponseHandler;
import me.xiaopan.networkeasy.StringHttpResponseHandler.StringHttpResponseHandleListener;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		EasyHttpClient.getInstance().get("http://www.miui.com/forum.php", new StringHttpResponseHandler(getBaseContext(), new StringHttpResponseHandleListener(){
			@Override
			public void onStart() {
				findViewById(R.id.loading).setVisibility(View.VISIBLE);
				Log.w("测试", "开始");
			}

			@Override
			public void onSuccess(String responseContent) {
				((TextView) findViewById(R.id.text_main_content)).setText(Html.fromHtml("成功了："+responseContent));
				Log.w("测试", "成功");
			}

			@Override
			public void onFailure(HttpResponse httpResponse) {
				((TextView) findViewById(R.id.text_main_content)).setText("失败了："+httpResponse.getStatusLine().getStatusCode());
				Log.w("测试", "失败");
			}

			@Override
			public void onException(Context context, Throwable e) {
				e.printStackTrace();
				((TextView) findViewById(R.id.text_main_content)).setText("异常了："+e.getMessage());
				Log.w("测试", "异常");
			}

			@Override
			public void onEnd() {
				findViewById(R.id.loading).setVisibility(View.GONE);
				Log.w("测试", "结束");
			}
		}));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}