package me.xiaopan.android.net.sample;

import me.xiaopan.android.happynetwork.R;
import me.xiaopan.android.net.sample.widget.HintView;
import android.app.ListActivity;

public class MyListActivity extends ListActivity {

	/**
	 * 获取提示视图
	 * @return
	 */
	public HintView getHintView(){
		return (HintView) findViewById(R.id.hint);
	}
}
