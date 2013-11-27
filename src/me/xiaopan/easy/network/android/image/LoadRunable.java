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
package me.xiaopan.easy.network.android.image;

import android.graphics.Bitmap;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

import me.xiaopan.easy.java.util.StringUtils;

/**
 * 加载任务
 */
public class LoadRunable implements Runnable {
	private ImageLoader imageLoader;	//图片加载器
	private LoadRequest loadRequest;	//加载请求
	private int numberOfLoaded;	//已加载次数
	
	/**
	 * 创建一个加载图片任务
	 * @param loadRequest 加载请求
	 */
	public LoadRunable(ImageLoader imageLoader, LoadRequest loadRequest){
		this.imageLoader = imageLoader;
		this.loadRequest = loadRequest;
	}
	
	@Override
	public void run() {
		if(loadRequest.getLocalCacheFile() !=null && loadRequest.getLocalCacheFile().exists()){
			loadRequest.setResultBitmap(fromLocalFileLoadBitmap(loadRequest.getLocalCacheFile()));
		}else if(StringUtils.isNotEmpty(loadRequest.getImageUrl())){
			loadRequest.setResultBitmap(fromNetworkDownload(loadRequest.getLocalCacheFile()));
		}else{
			loadRequest.setResultBitmap(null);
		}

		/* 尝试缓存到内存中 */
		if(loadRequest.getResultBitmap() != null && loadRequest.getOptions() != null && loadRequest.getOptions().isCachedInMemory()){
			imageLoader.getConfiguration().getBitmapCacher().put(loadRequest.getId(), loadRequest.getResultBitmap());
		}
		
		imageLoader.getConfiguration().getHandler().post(new ResultHandleRunnable(imageLoader, loadRequest));
	}
	
	/**
	 * 从本地加载位图
	 * @param localFile
	 * @return
	 */
	private Bitmap fromLocalFileLoadBitmap(File localFile){
		imageLoader.log("从本地加载图片："+localFile.getPath());
		if(loadRequest.getOptions() != null && loadRequest.getOptions().getBitmapHandler() != null){
			return loadRequest.getOptions().getBitmapHandler().onFromLocalFileLoad(localFile, loadRequest.getShowImageView());
		}else{
			return new PixelsBitmapHandler().onFromLocalFileLoad(localFile, loadRequest.getShowImageView());
		}
	}
	
	/**
	 * 从字节数据总加载位图
	 * @param byteArray
	 * @return
	 */
	private Bitmap fromByteArrayLoadBitmap(byte[] byteArray){
		if(loadRequest.getOptions() != null && loadRequest.getOptions().getBitmapHandler() != null){
			return loadRequest.getOptions().getBitmapHandler().onFromByteArrayLoad(byteArray, loadRequest.getShowImageView());
		}else{
			return new PixelsBitmapHandler().onFromByteArrayLoad(byteArray, loadRequest.getShowImageView());
		}
	}
	
	/**
	 * 下载图片
	 * @param localCacheFile 本地缓存文件，如果需要缓存到本地文件的话，会先将图片数据下载并存储到本地文件中再读取，否则将直接存到内存中
	 * @return
	 */
	private Bitmap fromNetworkDownload(File localCacheFile){
		imageLoader.log("从网络加载图片："+loadRequest.getImageUrl());
		boolean running = true;
		boolean createNewDir = false;	//true：父目录之前不存在是现在才创建的，当发生异常时需要删除
		boolean createNewFile = false;	//true：保存图片的文件之前不存在是现在才创建的，当发生异常时需要删除
		File localCacheParentDir = null;	//本地缓存文件的父目录
		BufferedInputStream bufferedfInputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		Bitmap resultBitmap = null;	//结果图标
		HttpResponse httpResponse;
		int readNumber;	//读取到的字节的数量
		byte[] cacheBytes = new byte[1024];//数据缓存区
		
		while(running){
			numberOfLoaded++;//加载次数加1
			HttpGet httpGet = null;
			try {
				httpGet = new HttpGet(loadRequest.getImageUrl());
				httpResponse = imageLoader.getHttpClient().execute(httpGet);//请求数据
				
				//读取响应体长度，如果没有响应体长度字段或者长度为0就抛出异常
				long fileLength;
				Header[] contentTypeString = httpResponse.getHeaders("Content-Length");
				if(contentTypeString.length > 0){
					fileLength = Long.valueOf(contentTypeString[0].getValue());
					if(fileLength <= 0){
						throw new Exception("文件长度为0");
					}
				}else{
					throw new Exception("文件长度为0");
				}
				
				//如果需要缓存并且缓存文件不null，就尝试先创建文件在下载数据存到缓存文件中再读取
				if((loadRequest.getOptions() == null || loadRequest.getOptions().isCacheInLocal()) && localCacheFile != null){
					/* 尝试创建父目录并创建新的缓存文件 */
					localCacheParentDir = localCacheFile.getParentFile();	//获取其父目录
					if(!localCacheParentDir.exists()){	//如果父目录同样不存在
						createNewDir = localCacheParentDir.mkdirs();	//创建父目录
					}
					
					//如果文件创建成功了，就读取数据并写入本地文件，然后再从本地读取图片
					if(createNewFile = localCacheFile.createNewFile()){
						bufferedfInputStream = new BufferedInputStream(httpResponse.getEntity().getContent());
						bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localCacheFile, false));
						while((readNumber = bufferedfInputStream.read(cacheBytes)) != -1){
							bufferedOutputStream.write(cacheBytes, 0, readNumber);
						}
						bufferedfInputStream.close();
						bufferedOutputStream.flush();
						bufferedOutputStream.close();
						resultBitmap = fromLocalFileLoadBitmap(localCacheFile);
					}else{
						throw new Exception("文件"+localCacheFile.getPath()+"创建失败");
					}
				}else{
					resultBitmap = fromByteArrayLoadBitmap(EntityUtils.toByteArray(new BufferedHttpEntity(httpResponse.getEntity())));
				}
				running = false;
			} catch (Throwable e2) {
				imageLoader.log(loadRequest.getImageUrl()+"加载失败，异常信息："+e2.getClass().getName()+":"+e2.getMessage(), true);
				
				if(httpGet != null){
					httpGet.abort();
				}
				
				//尝试关闭输入流
				if(bufferedfInputStream != null){
					try {
						bufferedfInputStream.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
				//尝试关闭输出流
				if(bufferedOutputStream != null){
					try {
						bufferedOutputStream.flush();
						bufferedOutputStream.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
				//如果创建了新文件就删除
				if(createNewFile && localCacheFile != null && localCacheFile.exists()){
					localCacheFile.delete();
				}
				
				//如果创建了新目录就删除
				if(createNewDir && localCacheParentDir != null && localCacheParentDir.exists()){
					localCacheParentDir.delete();
				}
				
				//如果是请求超时异常，就尝试再请求一次
				if((e2 instanceof ConnectTimeoutException || e2 instanceof SocketTimeoutException  || e2 instanceof  ConnectionPoolTimeoutException) && loadRequest.getOptions() != null && loadRequest.getOptions().getMaxRetryCount() > 0){
					running = numberOfLoaded < loadRequest.getOptions().getMaxRetryCount();	//如果尚未达到最大重试次数，那么就再尝试一次
				}else{
					running = false;
				}
			}
		}
		return resultBitmap;
	}
}
