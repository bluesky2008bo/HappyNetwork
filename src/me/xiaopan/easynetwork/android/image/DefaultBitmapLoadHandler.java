package me.xiaopan.easynetwork.android.image;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

/**
 * 默认的图片加载处理器，会根据ImageView的大小来自动读取宽高合适的Bitmap，因此ImageView的宽高必须大于0
 */
public class DefaultBitmapLoadHandler implements BitmapLoadHandler{
	@Override
	public Bitmap onFromLocalFileLoad(byte[] byteArray, ImageView showImageView) {
		if(showImageView.getWidth() > 0){
			//先读取图片的原图的宽高
			BitmapFactory.Options options = new BitmapFactory.Options(); 
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
			int bitmapWidth = options.outWidth;
			
			//计算合适的比例并按照新的比例读取图片
			int bili = bitmapWidth/showImageView.getWidth();
			options.inSampleSize = bili;
			options.inPurgeable = true;  
			options.inInputShareable = true;
			return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
		}else{
			return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		}
	}
	
	@Override
	public Bitmap onFromByteArrayLoad(File localFile, ImageView showImageView) {
		if(showImageView.getWidth() > 0){
			//先读取图片的原图的宽高
			BitmapFactory.Options options = new BitmapFactory.Options(); 
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(localFile.getPath(), options);
			int bitmapWidth = options.outWidth;
			
			//计算合适的比例并按照新的比例读取图片
			int bili = bitmapWidth/showImageView.getWidth();
			options.inSampleSize = bili;
			options.inJustDecodeBounds = false;
			options.inPurgeable = true;  
			options.inInputShareable = true;
			Bitmap bitmap = BitmapFactory.decodeFile(localFile.getPath(), options);
			return bitmap;
		}else{
			return BitmapFactory.decodeFile(localFile.getPath());
		}
	}
}