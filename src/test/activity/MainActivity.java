package test.activity;

import java.io.UnsupportedEncodingException;

import me.xiaopan.networkeasy.BinaryHttpResponseHandler;
import me.xiaopan.networkeasy.EasyHttpClient;
import me.xiaopan.networkeasy.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		EasyHttpClient.getInstance().get("http://m.weather.com.cn/data/101010100.html", new BinaryHttpResponseHandler(){
			@Override
			public void onSuccess(byte[] binaryData) {
				try {
					((TextView) findViewById(R.id.text_main_content)).setText(Html.fromHtml(new String(binaryData, "UTF-8")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}