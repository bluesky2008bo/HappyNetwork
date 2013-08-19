package me.xiaopan.easynetwork.android.image;

import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

public class DefaultScaleAnimationListener implements ShowAnimationListener {

	@Override
	public Animation onGetShowAnimation() {
		/* 创建一个从50%放大到100%并且持续0.5秒的缩放动画 */
		ScaleAnimation scaleAnimation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setDuration(500);
		return scaleAnimation;
	}
}