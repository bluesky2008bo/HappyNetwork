package me.xiaopan.easy.network.android.image;

import java.io.File;

import me.xiaopan.easy.android.util.BitmapDecoder;
import me.xiaopan.easy.android.util.DeviceUtils;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 根据最大像素数来读取位图的位图处理器，默认最大像素数为屏幕的宽乘以屏幕的高
 * <br>如果通过ImageView.getLayoutParams().width乘以ImageView.getLayoutParams().height得到的值大于0并且小于默认最大像素数的话就会临时使用得到的值作为最大像素数来读取图片
 */
public class PixelsBitmapHandler implements BitmapHandler{
	private int defaultMaxNumOfPixels;	//默认最大像素数
	
	/**
	 * 创建根据像素数来读取位图的位图处理器
	 * @param defaultMaxNumOfPixels 默认最大像素数，可以以此来限制读取的位图的尺寸
	 */
	public PixelsBitmapHandler(int defaultMaxNumOfPixels){
		setDefaultMaxNumOfPixels(defaultMaxNumOfPixels);
	}
	
	/**
	 * 创建位图处理器，默认的最大像素数是当前屏幕的宽乘以高
	 */
	public PixelsBitmapHandler(){}
	
	@Override
	public Bitmap onFromByteArrayLoad(byte[] byteArray, ImageView showImageView) {
		int currentNumOfPixels = showImageView.getLayoutParams().width * showImageView.getLayoutParams().height;
		if(defaultMaxNumOfPixels == 0){
			int[] screenSize = DeviceUtils.getScreenSize(showImageView.getContext());
			defaultMaxNumOfPixels = screenSize[0] * screenSize[1];
		}
		if(currentNumOfPixels > defaultMaxNumOfPixels){
			currentNumOfPixels = defaultMaxNumOfPixels;
		}
		return new BitmapDecoder(currentNumOfPixels).decodeByteArray(byteArray);
	}
	
	@Override
	public Bitmap onFromLocalFileLoad(File localFile, ImageView showImageView) {
		int currentNumOfPixels = showImageView.getLayoutParams().width * showImageView.getLayoutParams().height;
		if(defaultMaxNumOfPixels == 0){
			int[] screenSize = DeviceUtils.getScreenSize(showImageView.getContext());
			defaultMaxNumOfPixels = screenSize[0] * screenSize[1];
		}
		if(currentNumOfPixels > defaultMaxNumOfPixels){
			currentNumOfPixels = defaultMaxNumOfPixels;
		}
		return new BitmapDecoder(currentNumOfPixels).decodeFile(localFile.getPath());
	}

	/**
	 * 获取默认的最大像素数
	 * @return
	 */
	public int getDefaultMaxNumOfPixels() {
		return defaultMaxNumOfPixels;
	}

	/**
	 * 设置默认的最大像素数
	 * @param defaultMaxNumOfPixels，当小于等于0时会抛出IllegalArgumentException异常
	 */
	public void setDefaultMaxNumOfPixels(int defaultMaxNumOfPixels) {
		if(defaultMaxNumOfPixels <= 0){
			throw new IllegalArgumentException("defaultMaxNumOfPixels 不能小于等于0");
		}
		this.defaultMaxNumOfPixels = defaultMaxNumOfPixels;
	}
}