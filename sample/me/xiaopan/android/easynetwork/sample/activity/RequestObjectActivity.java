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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import me.xiaopan.android.easynetwork.R;
import me.xiaopan.android.easynetwork.http.EasyHttpClient;
import me.xiaopan.android.easynetwork.http.StringHttpResponseHandler;
import me.xiaopan.android.easynetwork.sample.net.request.BaiduSearchRequest;
import me.xiaopan.android.easynetwork.sample.util.Utils;
import me.xiaopan.android.easynetwork.sample.util.WebViewManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
	
	@SuppressLint("HandlerLeak")
	private void search(String keyword){
		EasyHttpClient.getInstance().execute(getBaseContext(), new BaiduSearchRequest(keyword), new StringHttpResponseHandler(){
			@Override
			public void onStart() {
				searchButton.setEnabled(false);
				findViewById(R.id.loading).setVisibility(View.VISIBLE);
			}

			@Override
			public void onSuccess(HttpResponse httpResponse, String responseContent, boolean isCache, boolean isRefreshCacheAndCallback) {
				Header contentTypeHeader = httpResponse.getEntity().getContentType();
				webViewManager.getWebView().loadData(responseContent, contentTypeHeader != null?contentTypeHeader.getValue():"text/html;charset=utf-8", null);
				File file = new File(getExternalCacheDir().getPath() + File.separator + System.currentTimeMillis() +".txt");
				try {
					file.createNewFile();
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					fileOutputStream.write(responseContent.getBytes());
					fileOutputStream.flush();
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				searchButton.setEnabled(true);
				findViewById(R.id.loading).setVisibility(View.GONE);
			}
			
			@Override
			public void onFailure(Throwable throwable) {
				Toast.makeText(getBaseContext(), "失败了，信息："+(throwable.getMessage()!=null?throwable.getMessage():""), Toast.LENGTH_LONG).show();
//				finish();
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