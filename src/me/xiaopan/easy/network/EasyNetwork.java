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

package me.xiaopan.easy.network;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class EasyNetwork {
	public static final String CHARSET_NAME_UTF8 = "UTF-8";
    private static ThreadPoolExecutor threadPool;	//线程池

    public static ThreadPoolExecutor getThreadPool() {
        if(threadPool == null){
            threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        }
        return threadPool;
    }

	public static void setThreadPool(ThreadPoolExecutor threadPool) {
		EasyNetwork.threadPool = threadPool;
	}
}