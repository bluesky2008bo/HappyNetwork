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
package me.xiaopan.easy.network.android.examples.activity;

import me.xiaopan.easy.network.android.R;
import me.xiaopan.easy.network.android.examples.adapter.ActivityAdapter;
import me.xiaopan.easy.network.android.examples.beans.ActivityItem;
import android.app.ListActivity;
import android.os.Bundle;

public class MainActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		getListView().setAdapter(new ActivityAdapter(getBaseContext(), getListView()
			, new ActivityItem(getString(R.string.activityTitle_binary), BinaryActivity.class)
			, new ActivityItem(getString(R.string.activityTitle_string), StringActivity.class)
			, new ActivityItem(getString(R.string.activityTitle_json), JsonActivity.class)
			, new ActivityItem(getString(R.string.activityTitle_requestObject), RequestObjectActivity.class)
			, new ActivityItem(getString(R.string.activityTitle_imageLoader), ImageLoaderActivity.class)
		));
	}
}