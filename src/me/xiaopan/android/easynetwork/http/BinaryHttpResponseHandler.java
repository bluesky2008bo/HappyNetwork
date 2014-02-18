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
package me.xiaopan.android.easynetwork.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import android.os.Handler;

/**
 * 默认的二进制Http响应处理器
 */
public abstract class BinaryHttpResponseHandler extends HttpResponseHandler {

	@Override
	public void start(final Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onStart();
            }
        });
	}

	@Override
	public void handleResponse(final Handler handler, final HttpResponse httpResponse, final boolean isOver) throws Throwable {
		if(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300 ){
			HttpEntity httpEntity = httpResponse.getEntity();
            if(httpEntity != null){
                final byte[] data = EntityUtils.toByteArray(new BufferedHttpEntity(httpEntity));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onSuccess(httpResponse, data, isOver);
                    }
                });
            }else{
                throw new Exception("没有响应体");
            }
		}else{
            throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), "异常状态码："+httpResponse.getStatusLine().getStatusCode());
        }
	}

	@Override
	public void exception(final Handler handler, final Throwable e, final boolean isRefresh) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onFailure(e, isRefresh);
            }
        });
	}
	
	/**
	 * 请求开始
	 */
	public abstract void onStart();
	
	/**
	 * 请求成功
	 * @param httpResponse  Http响应
	 * @param binaryData 响应内容
	 * @param isOver 本次执行是否是最后一次
	 */
	public abstract void onSuccess(HttpResponse httpResponse, byte[] binaryData, boolean isOver);
	
	/**
	 * 请求失败
	 * @param throwable 异常
	 * @param isRefresh 本次异常是否是在刷新缓存数据的时候发生的
	 */
	public abstract void onFailure(Throwable throwable, boolean isRefresh);
}
