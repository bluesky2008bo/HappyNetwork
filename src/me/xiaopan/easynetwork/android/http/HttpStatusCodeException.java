/*
 * Copyright 2013 Peng fei Pan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.easynetwork.android.http;

/**
 * Http状态码异常
 */
public class HttpStatusCodeException extends Exception {
    private static final long serialVersionUID = -33875147648L;
    private int httpStatusCode;	//Http状态码
    
    public HttpStatusCodeException(int httpStatusCode) {
        super("异常状态码："+httpStatusCode);
        this.httpStatusCode = httpStatusCode;
    }

	/**
	 * 获取Http状态码
	 * @return Http状态码
	 */
	public int getHttpStatusCode() {
		return httpStatusCode;
	}
}