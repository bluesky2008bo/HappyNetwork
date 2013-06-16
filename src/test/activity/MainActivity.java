package test.activity;

import me.xiaopan.easynetwork.android.R;
import test.adapter.ActivityAdapter;
import test.beans.ActivityItem;
import android.app.ListActivity;
import android.os.Bundle;

public class MainActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		getListView().setAdapter(new ActivityAdapter(getBaseContext(), getListView()
			, new ActivityItem(getString(R.string.activityTitle_http), HttpTestActivity.class)
			, new ActivityItem(getString(R.string.activityTitle_imageLoader), ImageLoaderTestActivity.class)
		));
	}
}