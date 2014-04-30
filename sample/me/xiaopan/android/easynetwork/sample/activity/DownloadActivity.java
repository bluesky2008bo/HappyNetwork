package me.xiaopan.android.easynetwork.sample.activity;

import java.io.File;

import me.xiaopan.android.easynetwork.R;
import me.xiaopan.android.easynetwork.http.DownloadHttpResponseHandler;
import me.xiaopan.android.easynetwork.http.EasyHttpClient;
import me.xiaopan.android.easynetwork.sample.MyActivity;
import me.xiaopan.android.easynetwork.sample.net.Failure;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class DownloadActivity extends MyActivity {
	private ImageView imageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		imageView = (ImageView) findViewById(R.id.image_download);
		load();
	}
	
	private void load(){
		EasyHttpClient.getInstance(getBaseContext()).get("http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1311/11/c0/28529113_1384156076013_800x600.jpg", new DownloadHttpResponseHandler(null) {
			@Override
			public void onStart() {
				getHintView().loading("");
			}
			
			@Override
			public void onUpdateProgress(long totalLength, long completedLength) {
				getHintView().setProgress((int)totalLength, (int)completedLength);
				Log.e("下载", "更新进度");
			}

			@Override
			public void onSuccess(byte[] data) {
				imageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
				getHintView().hidden();
				Log.e("下载", "成功");
			}

			@Override
			public void onSuccess(File file) {
				
			}

			@Override
			public void onFailure(Throwable e) {
				getHintView().failure(Failure.buildByException(getBaseContext(), e), new OnClickListener() {
					@Override
					public void onClick(View v) {
						load();
					}
				});
				Log.e("下载", "失败");
			}

			@Override
			protected void onCancel() {
				Log.e("下载", "取消");
			}
		}, this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EasyHttpClient.getInstance(getBaseContext()).cancelRequests(this, true);
	}
}