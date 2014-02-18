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
package me.xiaopan.android.easynetwork.sample.activity;

import me.xiaopan.android.easynetwork.R;
import me.xiaopan.android.easynetwork.http.BinaryHttpResponseHandler;
import me.xiaopan.android.easynetwork.http.EasyHttpClient;

import org.apache.http.HttpResponse;

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
		EasyHttpClient.getInstance().get(getBaseContext(), "http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1311/11/c0/28529113_1384156076013_800x600.jpg", new BinaryHttpResponseHandler() {
			@Override
			public void onStart() {
				findViewById(R.id.loading).setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onSuccess(HttpResponse httpResponse, byte[] binaryData, boolean isOver) {
				((ImageView) findViewById(R.id.image1)).setImageBitmap(BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length));
				findViewById(R.id.loading).setVisibility(View.GONE);
			}
			
			@Override
			public void onFailure(Throwable throwable, boolean isNotRefresh) {
				Toast.makeText(getBaseContext(), "失败了，信息："+throwable.getMessage(), Toast.LENGTH_LONG).show();
				finish();
			}
		});
	}
}