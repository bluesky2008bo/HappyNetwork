package me.xiaopan.android.easynetwork.sample.activity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import me.xiaopan.android.easynetwork.R;
import me.xiaopan.android.easynetwork.http.DownloadHttpResponseHandler;
import me.xiaopan.android.easynetwork.http.EasyHttpClient;
import me.xiaopan.android.easynetwork.sample.MyActivity;
import me.xiaopan.android.easynetwork.sample.net.Failure;

import java.io.File;

public class DownloadActivity extends MyActivity {
	private ImageView imageView;
    private Object requestTag = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		imageView = (ImageView) findViewById(R.id.image_download);
		load();
	}
	
	private void load(){
		File file = new File(getExternalCacheDir(), "800x600.jpg");

        EasyHttpClient.getInstance(getBaseContext()).get("http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1311/11/c0/28529113_1384156076013_800x600.jpg", new DownloadHttpResponseHandler(file, true) {
			@Override
			public void onStart() {
				getHintView().loading("");
			}
			
			@Override
			public void onUpdateProgress(long totalLength, long completedLength) {
				getHintView().setProgress((int)totalLength, (int)completedLength);
			}

			@Override
			public void onSuccess(File file) {
				imageView.setImageURI(Uri.fromFile(file));
				getHintView().hidden();
			}

			@Override
			public void onFailure(Throwable e) {
				getHintView().failure(Failure.buildByException(getBaseContext(), e), new OnClickListener() {
					@Override
					public void onClick(View v) {
						load();
					}
				});
			}

			@Override
			protected void onCancel() {
				Log.e("下载", "取消");
			}
		}, requestTag);

        EasyHttpClient.getInstance(getBaseContext()).get("http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1311/11/c0/28529113_1384156076013_800x600.jpg", new DownloadHttpResponseHandler(file, true) {
            @Override
            public void onStart() {
                getHintView().loading("");
            }

            @Override
            public void onUpdateProgress(long totalLength, long completedLength) {
                getHintView().setProgress((int)totalLength, (int)completedLength);
            }

            @Override
            public void onSuccess(File file) {
                imageView.setImageURI(Uri.fromFile(file));
                getHintView().hidden();
            }

            @Override
            public void onFailure(Throwable e) {
                getHintView().failure(Failure.buildByException(getBaseContext(), e), new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        load();
                    }
                });
            }

            @Override
            protected void onCancel() {
                Log.e("下载", "取消");
            }
        }, requestTag);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EasyHttpClient.getInstance(getBaseContext()).cancelRequests(requestTag, true);
	}
}