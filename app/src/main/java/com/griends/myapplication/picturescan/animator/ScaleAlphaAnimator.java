package com.griends.myapplication.picturescan.animator;

import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.griends.myapplication.picturescan.ImagePopup;
import com.griends.myapplication.picturescan.enums.PopupAnimation;

/**
 * Description: 缩放透明
 * Create by dance, at 2018/12/9
 */
public class ScaleAlphaAnimator extends PopupAnimator {
    public ScaleAlphaAnimator(View target, PopupAnimation popupAnimation) {
        super(target, popupAnimation);
    }

    @Override
    public void initAnimator() {
        targetView.setScaleX(0f);
        targetView.setScaleY(0f);
        targetView.setAlpha(0);

        // 设置动画参考点
        targetView.post(new Runnable() {
            @Override
            public void run() {
                applyPivot();
            }
        });
    }

    /**
     * 根据不同的PopupAnimation来设定对应的pivot
     */
    private void applyPivot() {
        switch (popupAnimation) {
            case ScaleAlphaFromCenter:
                targetView.setPivotX(targetView.getMeasuredWidth() / 2F);
                targetView.setPivotY(targetView.getMeasuredHeight() / 2F);
                break;
            case ScaleAlphaFromLeftTop:
                targetView.setPivotX(0);
                targetView.setPivotY(0);
                break;
            case ScaleAlphaFromRightTop:
                targetView.setPivotX(targetView.getMeasuredWidth());
                targetView.setPivotY(0f);
                break;
            case ScaleAlphaFromLeftBottom:
                targetView.setPivotX(0f);
                targetView.setPivotY(targetView.getMeasuredHeight());
                break;
            case ScaleAlphaFromRightBottom:
                targetView.setPivotX(targetView.getMeasuredWidth());
                targetView.setPivotY(targetView.getMeasuredHeight());
                break;
        }

    }

    @Override
    public void animateShow() {
        targetView.animate().scaleX(1F).scaleY(1F).alpha(1F)
                .setDuration(ImagePopup.getAnimationDuration())
                .setInterpolator(new OvershootInterpolator(1F))
                .start();
    }

    @Override
    public void animateDismiss() {
        targetView.animate().scaleX(0F).scaleY(0F).alpha(0F).setDuration(ImagePopup.getAnimationDuration())
                .setInterpolator(new FastOutSlowInInterpolator()).start();
    }

}
