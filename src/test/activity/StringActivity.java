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
package test.activity;

import me.xiaopan.easy.network.android.R;
import me.xiaopan.easy.network.android.http.EasyHttpClient;
import me.xiaopan.easy.network.android.http.StringHttpResponseHandler;
import test.util.WebViewManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * 字符串
 */
public class StringActivity extends Activity {
	private WebViewManager webViewManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		webViewManager = new WebViewManager((WebView) findViewById(R.id.web1));
		
		EasyHttpClient.getInstance().get("http://www.miui.com/forum.php", new StringHttpResponseHandler(){
			@Override
			public void onStart() {
				findViewById(R.id.loading).setVisibility(View.VISIBLE);
			}

			@Override
			public void onSuccess(String responseContent) {
				webViewManager.getWebView().loadData(responseContent, "text/html;charset=utf-8", null);
			}
			
			@Override
			public void onFailure(Throwable throwable) {
				Toast.makeText(getBaseContext(), "失败了，信息："+throwable.getMessage(), Toast.LENGTH_LONG).show();
				finish();
			}

			@Override
			public void onEnd() {
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