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

import com.google.gson.annotations.Expose;

import me.xiaopan.easynetwork.android.http.Url;
import test.net.BaseRequest;

/**
 * 百度搜索请求
 */
@Url("http://www.baidu.com/s")
public class BaiduSearchRequest extends BaseRequest {
	@Expose
	private String rsv_spt = "1";
	@Expose
	private String issp = "1";
	@Expose
	private String rsv_bp = "0";
	@Expose
	private String ie = "utf-8";
	@Expose
	private String tn = "98012088_3_dg";
	@Expose
	private String rsv_sug3 = "4";
	@Expose
	private String rsv_sug = "0";
	@Expose
	private String rsv_sug1 = "3";
	@Expose
	private String rsv_sug4 = "481";
	@Expose
	private String wd;
	
	/**
	 * 创建一个百度搜索请求
	 * @param keyword 搜索关键字
	 */
	public BaiduSearchRequest(String keyword){
		wd = keyword;
	}
	
	public String getRsv_spt() {
		return rsv_spt;
	}
	public String getIssp() {
		return issp;
	}
	public String getRsv_bp() {
		return rsv_bp;
	}
	public String getIe() {
		return ie;
	}
	public String getTn() {
		return tn;
	}
	public String getRsv_sug3() {
		return rsv_sug3;
	}
	public String getRsv_sug() {
		return rsv_sug;
	}
	public String getRsv_sug1() {
		return rsv_sug1;
	}
	public String getRsv_sug4() {
		return rsv_sug4;
	}
	public String getWd() {
		return wd;
	}
	public void setRsv_spt(String rsv_spt) {
		this.rsv_spt = rsv_spt;
	}
	public void setIssp(String issp) {
		this.issp = issp;
	}
	public void setRsv_bp(String rsv_bp) {
		this.rsv_bp = rsv_bp;
	}
	public void setIe(String ie) {
		this.ie = ie;
	}
	public void setTn(String tn) {
		this.tn = tn;
	}
	public void setRsv_sug3(String rsv_sug3) {
		this.rsv_sug3 = rsv_sug3;
	}
	public void setRsv_sug(String rsv_sug) {
		this.rsv_sug = rsv_sug;
	}
	public void setRsv_sug1(String rsv_sug1) {
		this.rsv_sug1 = rsv_sug1;
	}
	public void setRsv_sug4(String rsv_sug4) {
		this.rsv_sug4 = rsv_sug4;
	}
	public void setWd(String wd) {
		this.wd = wd;
	}
}