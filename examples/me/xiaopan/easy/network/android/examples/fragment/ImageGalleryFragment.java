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
package me.xiaopan.easy.network.android.examples.fragment;

import me.xiaopan.easy.network.android.R;
import me.xiaopan.easy.network.android.examples.adapter.ImageAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;

@SuppressWarnings("deprecation")
public class ImageGalleryFragment extends TitleFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Gallery gallery = (Gallery) inflater.inflate(R.layout.gallery, null);
		gallery.setAdapter(new ImageAdapter(getActivity(), getResources().getStringArray(R.array.imageurls)));
		return gallery;
	}

	@Override
	public String getTitle() {
		return "Gallery";
	}
}
