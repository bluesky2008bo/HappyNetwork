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
package me.xiaopan.easynetwork.android.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

/**
 * 下载图片异步任务
 */
public class DownloadImageAsyncTask extends AsyncTask<Integer, Integer, Integer> {
	public static int defaulrTimeout = 10000;
	public static int defaultMaxRetries = 3;
	private static final int RESULT_SUCCESS = 101;//结果标记 - 成功了
	private static final int RESULT_EXCEPTION = 102;//结果标记 - 异常了
	private Context context;//上下文
	private Activity activity;
	private int resultFlag = RESULT_EXCEPTION;//结果标记，默认异常
	private Throwable throwable;//访问网络过程中发生的异常
	private String imageUrl;
	private DownloadImageListener downloadImageListener;
	private long totalLength;
	private byte[] resultObject;
	
	public DownloadImageAsyncTask(Context context, String imageUrl, DownloadImageListener downloadImageListener){
		this.context = context;
		if(this.context instanceof Activity){
			activity = (Activity) this.context;
		}
		this.imageUrl = imageUrl;
		this.downloadImageListener = downloadImageListener;
	}
	
	@Override
	protected void onPreExecute() {
		if((activity == null || !activity.isFinishing()) && downloadImageListener != null){
			downloadImageListener.onStart();
		}
	}

	@Override
	protected Integer doInBackground(Integer... params) {
		if(downloadImageListener != null){
			boolean running = true;
			int requestNumber = 0;
			while(running){
				requestNumber++;
				try {
					request();
					running = false;
				} catch (Throwable e) {
					if(requestNumber < defaultMaxRetries){
						running = true;
					}else{
						running = false;
						e.printStackTrace();
						resultFlag = RESULT_EXCEPTION;
						throwable = e;
					}
				}
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if(downloadImageListener != null){
			downloadImageListener.onProgressUpdate(totalLength, values[0]);
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		if((activity == null || !activity.isFinishing()) && downloadImageListener != null){
			switch(resultFlag){
				case RESULT_SUCCESS : downloadImageListener.onSuccess(resultObject); break;
				case RESULT_EXCEPTION : downloadImageListener.onException(throwable, context); break;
			}
			downloadImageListener.onEnd();
		}
		context = null;
		activity = null;
		throwable = null;
		imageUrl = null;
		downloadImageListener = null;
		resultObject = null;
	}
	
	private void request() throws Throwable{
		BufferedInputStream bufferedInputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		try {
			/* 初始化HttpURL连接并发起连接 */
			HttpURLConnection httpURLConn = (HttpURLConnection) new URL(imageUrl).openConnection();
			httpURLConn.setConnectTimeout(defaulrTimeout);	//设置请求超时时间为15秒
			httpURLConn.setReadTimeout(defaulrTimeout);	//设置读取超时时间为15秒
			httpURLConn.setRequestMethod("GET");	//设置请求方式为get
			HttpURLConnection.setFollowRedirects(true);	//设置允许自动重定向
			httpURLConn.setDoInput(true);	//设置允许输入
			httpURLConn.connect();// 尝试连接
			
			/* 处理响应 */
			totalLength = Long.valueOf(httpURLConn.getHeaderField("Content-Length"));//获取内容长度
			bufferedInputStream = new BufferedInputStream(httpURLConn.getInputStream());
			byteArrayOutputStream = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			int readLength;
			while((readLength = bufferedInputStream.read(data)) != -1){
				byteArrayOutputStream.write(data, 0, readLength);
				onProgressUpdate(byteArrayOutputStream.size());
			}
			resultObject = byteArrayOutputStream.toByteArray();
			resultFlag = RESULT_SUCCESS;
		} catch (Throwable e) {
			throw e;
		}finally{
			if(bufferedInputStream != null){
				try {
					bufferedInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(byteArrayOutputStream != null){
				try {
					byteArrayOutputStream.flush();
					byteArrayOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public interface DownloadImageListener{
		public void onStart();
		public void onProgressUpdate(long totalLength, int finishLength);
		public void onSuccess(byte[] bytes);
		public void onException(Throwable throwable, Context context);
		public void onEnd();
	}
}