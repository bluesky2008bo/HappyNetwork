package me.xiaopan.android.easynetwork.sample;

import me.xiaopan.android.easynetwork.R;
import me.xiaopan.android.easynetwork.sample.widget.HintView;
import android.app.Activity;

public abstract class MyActivity extends Activity {
	/**
	 * 获取提示视图
	 * @return
	 */
	public HintView getHintView(){
		return (HintView) findViewById(R.id.hint);
	}
}
