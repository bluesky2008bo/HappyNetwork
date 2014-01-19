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

package me.xiaopan.android.easynetwork.sample.net.request;

import me.xiaopan.android.easynetwork.http.annotation.Name;
import me.xiaopan.android.easynetwork.http.annotation.Param;
import me.xiaopan.android.easynetwork.http.annotation.Url;
import me.xiaopan.android.easynetwork.sample.net.BaseRequest;

/**
 * 百度搜索请求
 */
@Url("http://www.baidu.com/s")
@Name("百度搜索")
public class BaiduSearchRequest extends BaseRequest {
	@Param
	public String rsv_spt = "1";

	@Param
	public String issp = "1";

	@Param
	public String rsv_bp = "0";

	@Param
	public String ie = "utf-8";

	@Param
	public String tn = "98012088_3_dg";

	@Param
	public String rsv_sug3 = "4";

	@Param
	public String rsv_sug = "0";

	@Param
	public String rsv_sug1 = "3";

	@Param
	public String rsv_sug4 = "481";

	@Param("wd")
	public String keyword;
	
	/**
	 * 创建一个百度搜索请求
	 * @param keyword 搜索关键字
	 */
	public BaiduSearchRequest(String keyword){
		this.keyword = keyword;
	}
}