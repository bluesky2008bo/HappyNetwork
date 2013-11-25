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
package me.xiaopan.easy.network.android.examples.activity;

import me.xiaopan.easy.network.android.R;
import me.xiaopan.easy.network.android.http.BinaryHttpResponseHandler;
import me.xiaopan.easy.network.android.http.EasyHttpClient;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 使用BinaryResponseHandler下载图片
 */
public class BinaryActivity extends Activity {
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		EasyHttpClient.getInstance().get(getBaseContext(), "http://e.hiphotos.baidu.com/album/w%3D2048/sign=4a605579d1160924dc25a51be03f34fa/1f178a82b9014a900a9e7492a8773912b31bee79.jpg", new BinaryHttpResponseHandler() {
			@Override
			public void onStart() {
				findViewById(R.id.loading).setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onSuccess(byte[] binaryData) {
				((ImageView) findViewById(R.id.image1)).setImageBitmap(BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length));
				findViewById(R.id.loading).setVisibility(View.GONE);
			}
			
			@Override
			public void onFailure(Throwable throwable) {
				Toast.makeText(getBaseContext(), "失败了，信息："+throwable.getMessage(), Toast.LENGTH_LONG).show();
				finish();
			}
		});
		
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