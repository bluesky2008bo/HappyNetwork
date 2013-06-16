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
package test.fragment;

import me.xiaopan.easynetwork.android.R;
import test.adapter.ImageAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;

public class ImageGalleryFragment extends TitleFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.gallery_simple, null);
		Gallery gallery = (Gallery) view.findViewById(R.id.gallery);
		gallery.setAdapter(new ImageAdapter(getActivity(), getResources().getStringArray(R.array.imageurls)));
		return view;
	}

	@Override
	public String getTitle() {
		return "Gallery";
	}
}
