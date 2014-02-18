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
import me.xiaopan.android.easynetwork.http.EasyHttpClient;
import me.xiaopan.android.easynetwork.http.JsonHttpResponseHandler;
import me.xiaopan.android.easynetwork.http.enums.FailureType;
import me.xiaopan.android.easynetwork.http.enums.ResponseType;
import me.xiaopan.android.easynetwork.sample.beans.Weather;
import me.xiaopan.android.easynetwork.sample.net.request.BeijingWeatherRequest;

import org.apache.http.HttpResponse;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

/**
 * 获取天气信息
 */
public class JsonActivity extends Activity {
	private TextView text;
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text);
		text = (TextView) findViewById(R.id.text1);
		
		EasyHttpClient.getInstance().execute(getBaseContext(), new BeijingWeatherRequest(), new JsonHttpResponseHandler<Weather>(Weather.class) {
            @Override
            public void onStart() {
                findViewById(R.id.loading).setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(ResponseType responseType, HttpResponse httpResponse, Weather responseObject) {
                text.setText(Html.fromHtml("<h2>" + responseObject.getCity() + "</h2>"
                        + "<br>" + responseObject.getDate_y() + " " + responseObject.getWeek()
                        + "<br>" + responseObject.getTemp1() + " " + responseObject.getWeather1()
                        + "<p><br>风力：" + responseObject.getWind1()
                        + "<br>紫外线：" + responseObject.getIndex_uv()
                        + "<br>紫外线（48小时）：" + responseObject.getIndex48_uv()
                        + "<br>穿衣指数：" + responseObject.getIndex() + "，" + responseObject.getIndex_d()
                        + "<br>穿衣指数（48小时）：" + responseObject.getIndex48() + "，" + responseObject.getIndex48_d()
                        + "<br>舒适指数：" + responseObject.getIndex_co()
                        + "<br>洗车指数：" + responseObject.getIndex_xc()
                        + "<br>旅游指数：" + responseObject.getIndex_tr()
                        + "<br>晨练指数：" + responseObject.getIndex_cl()
                        + "<br>晾晒指数：" + responseObject.getIndex_ls()
                        + "<br>过敏指数：" + responseObject.getIndex_ag() + "</p>"
                ));
                findViewById(R.id.loading).setVisibility(View.GONE);
            }

            @Override
            public void onFailure(FailureType failureType, Throwable throwable) {
                text.setText(throwable.getMessage());
                findViewById(R.id.loading).setVisibility(View.GONE);
            }
        });
	}
}