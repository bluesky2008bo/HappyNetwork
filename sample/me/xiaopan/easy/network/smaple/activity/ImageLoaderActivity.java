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
package me.xiaopan.easy.network.smaple.activity;

import java.util.ArrayList;
import java.util.List;

import me.xiaopan.easy.network.R;
import me.xiaopan.easy.network.smaple.adapter.TitleFragmentPagerAdapter;
import me.xiaopan.easy.network.smaple.fragment.ImageFragment;
import me.xiaopan.easy.network.smaple.fragment.ImageGalleryFragment;
import me.xiaopan.easy.network.smaple.fragment.ImageGridFragment;
import me.xiaopan.easy.network.smaple.fragment.ImageListFragment;
import me.xiaopan.easy.network.smaple.fragment.TitleFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class ImageLoaderActivity extends FragmentActivity {
	ViewPager viewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_loader);
		viewPager = (ViewPager) findViewById(R.id.viewPager_main);
		
		List<TitleFragment> fragments = new ArrayList<TitleFragment>();
		fragments.add(new ImageListFragment());
		fragments.add(new ImageGridFragment());
		fragments.add(new ImageFragment());
		fragments.add(new ImageGalleryFragment());
		
		viewPager.setAdapter(new TitleFragmentPagerAdapter(getSupportFragmentManager(), fragments));
	}
}