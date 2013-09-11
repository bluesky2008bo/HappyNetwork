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

package test.net.request;

import me.xiaopan.easy.network.android.http.Url;
import test.net.BaseRequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 百度搜索请求
 */
@Url("http://www.baidu.com/s")
public class BaiduSearchRequest extends BaseRequest {
	@Expose
	public String rsv_spt = "1";
	@Expose
	public String issp = "1";
	@Expose
	public String rsv_bp = "0";
	@Expose
	public String ie = "utf-8";
	@Expose
	public String tn = "98012088_3_dg";
	@Expose
	public String rsv_sug3 = "4";
	@Expose
	public String rsv_sug = "0";
	@Expose
	public String rsv_sug1 = "3";
	@Expose
	public String rsv_sug4 = "481";
	@Expose
	@SerializedName("wd")
	public String keyword;
	
	/**
	 * 创建一个百度搜索请求
	 * @param keyword 搜索关键字
	 */
	public BaiduSearchRequest(String keyword){
		this.keyword = keyword;
	}
}