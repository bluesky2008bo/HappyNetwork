package me.xiaopan.easynetwork.android.image;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * 默认的图片加载处理器，会根据ImageView的大小来自动读取宽高合适的Bitmap，因此ImageView的宽高必须大于0
 */
public class DefaultBitmapLoadHandler implements BitmapLoadHandler{
	private int displayWidth;
	private int displayHeight;
	
	public DefaultBitmapLoadHandler(Context conetxt){
		WindowManager windowManager = (WindowManager) conetxt.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		displayWidth = display.getWidth();
		displayHeight = display.getHeight();
	}
	
	@Override
	public Bitmap onFromByteArrayLoad(byte[] byteArray, ImageView showImageView) {
		/* 首先，计算最终的宽度，具体计算方法是，如果ImageView的宽度是固定的并且其宽度小于屏幕宽度，那么最终宽度就是ImageView的宽度，否则就是屏幕的宽度 */
		int finalWidth = displayWidth;	//最总宽度默认为屏幕的宽度
		int finalHeight = displayHeight;	//最总宽度默认为屏幕的宽度
		if(showImageView.getLayoutParams().width > 0 && showImageView.getLayoutParams().width < finalWidth){
			finalWidth = showImageView.getLayoutParams().width;
		}
		if(showImageView.getLayoutParams().height > 0 && showImageView.getLayoutParams().height < finalHeight){
			finalWidth = showImageView.getLayoutParams().height;
		}
		
		/* 然后，读取原图的宽度 */
		BitmapFactory.Options options = new BitmapFactory.Options(); 
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
		
		/* 最后，根据最终宽度和原图的宽度计算出合适的比例并按照新的比例读取图片 */
		options.inSampleSize = ImageLoaderUtils.computeSampleSize(options, -1, finalWidth * finalHeight);
		options.inPurgeable = true;  
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
		return bitmap;
	}
	
	@Override
	public Bitmap onFromLocalFileLoad(File localFile, ImageView showImageView) {
		/* 首先，计算最终的宽度，具体计算方法是，如果ImageView的宽度是固定的并且其宽度小于屏幕宽度，那么最终宽度就是ImageView的宽度，否则就是屏幕的宽度 */
		int finalWidth = displayWidth;	//最总宽度默认为屏幕的宽度
		int finalHeight = displayHeight;	//最总宽度默认为屏幕的宽度
		if(showImageView.getLayoutParams().width > 0 && showImageView.getLayoutParams().width < finalWidth){
			finalWidth = showImageView.getLayoutParams().width;
		}
		if(showImageView.getLayoutParams().height > 0 && showImageView.getLayoutParams().height < finalHeight){
			finalWidth = showImageView.getLayoutParams().height;
		}
		
		/* 然后，读取原图的宽度 */
		BitmapFactory.Options options = new BitmapFactory.Options(); 
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(localFile.getPath(), options);
		
		/* 最后，根据最终宽度和原图的宽度计算出合适的比例并按照新的比例读取图片 */
		options.inSampleSize = ImageLoaderUtils.computeSampleSize(options, -1, finalWidth * finalHeight);
		options.inPurgeable = true;  
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(localFile.getPath(), options);
		return bitmap;
	}
}