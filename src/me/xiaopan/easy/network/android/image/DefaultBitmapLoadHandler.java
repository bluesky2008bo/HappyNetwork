package me.xiaopan.easy.network.android.image;

import java.io.File;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * 默认的图片加载处理器，默认会限制图片的宽高不超过当前设备屏幕宽高的两倍，此举会在一定程度上防止内存溢出。
 * <br>如果通过ImageView.getLayoutParams().width获取到的宽度大于0的话则会根据ImageView的大小来读取宽高合适的Bitmap，另外由于缩放比例值是整型的所以会有误差，但可以保证不会超过ImageView的两倍
 */
public class DefaultBitmapLoadHandler implements BitmapLoadHandler{
	private int displayWidth;
	private int displayHeight;
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public DefaultBitmapLoadHandler(Context conetxt){
		WindowManager windowManager = (WindowManager) conetxt.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2){
			displayWidth = display.getWidth();
			displayHeight = display.getHeight();
		}else{
			Point point = new Point();
			display.getSize(point);
			displayWidth = point.x;
			displayHeight = point.y;
		}
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