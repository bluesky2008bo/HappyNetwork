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
package me.xiaopan.easy.network.smaple.fragment;

import me.xiaopan.easy.network.R;
import me.xiaopan.easy.network.smaple.adapter.ImageAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ImageListFragment extends TitleFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ListView listView = (ListView) inflater.inflate(R.layout.list, null);
		listView.setAdapter(new ImageAdapter(getActivity(), getResources().getStringArray(R.array.imageurls)));
		return listView;
	}

	@Override
	public String getTitle() {
		return "ListView";
	}
}
