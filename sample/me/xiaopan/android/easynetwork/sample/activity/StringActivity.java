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

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import me.xiaopan.android.easynetwork.R;
import me.xiaopan.android.easynetwork.http.EasyHttpClient;
import me.xiaopan.android.easynetwork.http.HttpGetRequest;
import me.xiaopan.android.easynetwork.http.ResponseCache;
import me.xiaopan.android.easynetwork.http.StringHttpResponseHandler;
import me.xiaopan.android.easynetwork.sample.util.WebViewManager;
import android.annotation.SuppressLint;
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
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		webViewManager = new WebViewManager((WebView) findViewById(R.id.web1));
		
		EasyHttpClient.getInstance().get(getBaseContext(), new HttpGetRequest.Builder("http://www.miui.com/forum.php").setResponseCache(new ResponseCache.Builder(20 * 1000).create()).create(), new StringHttpResponseHandler(){
			@Override
			public void onStart() {
				findViewById(R.id.loading).setVisibility(View.VISIBLE);
			}

			@Override
			public void onSuccess(HttpResponse httpResponse, String responseContent, boolean isCache, boolean isRefreshCacheAndCallback) {
				Header contentTypeHeader = httpResponse.getEntity().getContentType();
				webViewManager.getWebView().loadData(responseContent, contentTypeHeader != null?contentTypeHeader.getValue():"text/html;charset=utf-8", null);
				findViewById(R.id.loading).setVisibility(View.GONE);
			}
			
			@Override
			public void onFailure(Throwable throwable) {
				Toast.makeText(getBaseContext(), "失败了，信息："+throwable.getMessage(), Toast.LENGTH_LONG).show();
				finish();
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