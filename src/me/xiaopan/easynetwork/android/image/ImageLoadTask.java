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
package me.xiaopan.easynetwork.android.image;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageLoadTask implements Runnable {
	private ImageLoader imageLoader;	//图片加载器
	private LoadRequest loadRequest;	//加载请求
	private int numberOfLoaded;	//已加载次数
	
	/**
	 * 创建一个加载图片任务
	 * @param loadRequest 加载请求
	 */
	public ImageLoadTask(ImageLoader imageLoader, LoadRequest loadRequest){
		this.imageLoader = imageLoader;
		this.loadRequest = loadRequest;
	}
	
	@Override
	public void run() {
		if(loadRequest.getLocalCacheFile() !=null && loadRequest.getLocalCacheFile().exists()){
			loadRequest.setResultBitmap(fromLocalLoadBitmap(loadRequest.getLocalCacheFile()));
		}else if(ImageLoaderUtils.isNotNullAndEmpty(loadRequest.getImageUrl())){
			loadRequest.setResultBitmap(fromNetworkDownload(loadRequest.getLocalCacheFile()));
		}else{
			loadRequest.setResultBitmap(null);
		}
		imageLoader.getLoadHandler().obtainMessage(LoadHandler.WHAT_LOAD_FINISH, loadRequest).sendToTarget();
	}
	
	/**
	 * 从本地加载位图
	 * @param localFile
	 * @return
	 */
	private Bitmap fromLocalLoadBitmap(File localFile){
		ImageLoader.log("从本地加载图片："+localFile.getPath());
		BitmapLoadHandler bitmapHandler = ImageLoaderUtils.getBitmapLoadListener(loadRequest.getOptions());
		if(bitmapHandler != null){
			return bitmapHandler.onFromByteArrayLoad(localFile, loadRequest.getShowImageView());
		}else{
			return BitmapFactory.decodeFile(localFile.getPath());
		}
	}
	
	/**
	 * 从字节数据总加载位图
	 * @param byteArray
	 * @return
	 */
	private Bitmap fromByteArrayLoadBitmap(byte[] byteArray){
		BitmapLoadHandler bitmapHandler = ImageLoaderUtils.getBitmapLoadListener(loadRequest.getOptions());
		if(bitmapHandler != null){
			return bitmapHandler.onFromLocalFileLoad(byteArray, loadRequest.getShowImageView());
		}else{
			return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		}
	}
	
	/**
	 * 下载图片
	 * @param localCacheFile 本地缓存文件，如果需要缓存到本地文件的话，会先将图片数据下载并存储到本地文件中再读取，否则将直接存到内存中
	 * @return
	 */
	private Bitmap fromNetworkDownload(File localCacheFile){
		ImageLoader.log("从网络加载图片："+loadRequest.getImageUrl());
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
			try {
				httpResponse = ImageLoader.getHttpClient().execute(new HttpGet(loadRequest.getImageUrl()));//请求数据
				
				//读取文件长度
				long fileLength;
				Header[] contentTypeString = httpResponse.getHeaders("Content-Length");
				if(contentTypeString.length > 0){
					fileLength = Long.valueOf(contentTypeString[0].getValue());
				}else{
					throw new Exception("文件长度为0");
				}
				
				//如果需要缓存并且缓存文件不null，就尝试先创建文件在下载数据存到缓存文件中再读取
				if(localCacheFile != null && ImageLoaderUtils.isCacheToLocal(loadRequest.getOptions())){
					/* 尝试创建父目录并创建新的缓存文件 */
					localCacheParentDir = localCacheFile.getParentFile();	//获取其父目录
					if(!localCacheParentDir.exists()){	//如果父目录同样不存在
						createNewDir = localCacheParentDir.mkdirs();	//创建父目录
					}
					
					//如果文件创建成功了
					if(createNewFile = localCacheFile.createNewFile()){
						/* 设置文件长度 */
						RandomAccessFile raf = new RandomAccessFile(localCacheFile, "rwd");
						raf.setLength(fileLength);
						raf.close();
						
						/* 读取数据并写入本地文件 */
						bufferedfInputStream = new BufferedInputStream(httpResponse.getEntity().getContent());
						bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localCacheFile, false));
						while((readNumber = bufferedfInputStream.read(cacheBytes)) != -1){
							bufferedOutputStream.write(cacheBytes, 0, readNumber);
						}
						bufferedfInputStream.close();
						bufferedOutputStream.flush();
						bufferedOutputStream.close();
						
						/* 再从本地读取图片 */
						resultBitmap = fromLocalLoadBitmap(localCacheFile);
					}else{
						throw new Exception("文件"+localCacheFile.getPath()+"创建失败");
					}
				}else{
					resultBitmap = fromByteArrayLoadBitmap(EntityUtils.toByteArray(new BufferedHttpEntity(httpResponse.getEntity())));
				}
				running = false;
			} catch (Throwable e2) {
				ImageLoader.log(loadRequest.getImageUrl()+"加载失败，异常信息："+e2.getClass().getName()+":"+e2.getMessage());
				
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
				if((e2 instanceof ConnectTimeoutException || e2 instanceof SocketTimeoutException  || e2 instanceof  ConnectionPoolTimeoutException) && ImageLoaderUtils.getMaxRetryCount(loadRequest.getOptions()) > 0){
					running = numberOfLoaded < ImageLoaderUtils.getMaxRetryCount(loadRequest.getOptions());	//如果尚未达到最大重试次数，那么就再尝试一次
				}else{
					running = false;
				}
			}
		}
		return resultBitmap;
	}
}
