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

import me.xiaopan.easynetwork.android.R;
import me.xiaopan.easynetwork.android.http.EasyHttpClient;
import me.xiaopan.easynetwork.android.http.StringHttpResponseHandler;
import test.net.request.BaiduSearchRequest;
import test.util.Utils;
import test.util.WebViewManager;
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
			public void onFailure(Throwable throwable) {
				Toast.makeText(getBaseContext(), "失败了，信息："+throwable.getMessage(), Toast.LENGTH_LONG).show();
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