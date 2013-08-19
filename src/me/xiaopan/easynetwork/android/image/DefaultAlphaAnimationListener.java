package me.xiaopan.easynetwork.android.image;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class DefaultAlphaAnimationListener implements ShowAnimationListener {

	@Override
	public Animation onGetShowAnimation() {
		/* 创建一个从0.5到1.0透明度渐变动画 */
		AlphaAnimation alphaAnimation = new AlphaAnimation(0.5f, 1.0f);
		alphaAnimation.setDuration(400);
		return alphaAnimation;
	}
}