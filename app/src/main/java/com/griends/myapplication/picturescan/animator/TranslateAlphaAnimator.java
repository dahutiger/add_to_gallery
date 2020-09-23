package com.griends.myapplication.picturescan.animator;

import android.view.View;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.griends.myapplication.picturescan.ImagePopup;
import com.griends.myapplication.picturescan.enums.PopupAnimation;
import com.griends.myapplication.picturescan.util.ImagePopupUtils;

/**
 * Description: 平移动画
 * Create by dance, at 2018/12/9
 */
public class TranslateAlphaAnimator extends PopupAnimator {
    //动画起始坐标
    private float startTranslationX, startTranslationY;
    private float defTranslationX, defTranslationY;
    
    public TranslateAlphaAnimator(View target, PopupAnimation popupAnimation) {
        super(target, popupAnimation);
    }
    
    @Override
    public void initAnimator() {
        defTranslationX = targetView.getTranslationX();
        defTranslationY = targetView.getTranslationY();
        
        targetView.setAlpha(0);
        // 设置移动坐标
        applyTranslation();
        startTranslationX = targetView.getTranslationX();
        startTranslationY = targetView.getTranslationY();
    }
    
    private void applyTranslation() {
        int halfWidthOffset = ImagePopupUtils.getWindowWidth(targetView.getContext()) / 2 - targetView.getMeasuredWidth() / 2;
        int halfHeightOffset = ImagePopupUtils.getWindowHeight(targetView.getContext()) / 2 - targetView.getMeasuredHeight() / 2;
        switch (popupAnimation) {
            case TranslateAlphaFromLeft:
                targetView.setTranslationX(-(targetView.getMeasuredWidth()/* + halfWidthOffset*/));
                break;
            case TranslateAlphaFromTop:
                targetView.setTranslationY(-(targetView.getMeasuredHeight() /*+ halfHeightOffset*/));
                break;
            case TranslateAlphaFromRight:
                targetView.setTranslationX(targetView.getMeasuredWidth() /*+ halfWidthOffset*/);
                break;
            case TranslateAlphaFromBottom:
                targetView.setTranslationY(targetView.getMeasuredHeight() /*+ halfHeightOffset*/);
                break;
        }
    }
    
    @Override
    public void animateShow() {
        targetView.animate().translationX(defTranslationX).translationY(defTranslationY).alpha(1F)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(ImagePopup.getAnimationDuration()).start();
    }
    
    @Override
    public void animateDismiss() {
        targetView.animate().translationX(startTranslationX).translationY(startTranslationY).alpha(0F)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(ImagePopup.getAnimationDuration()).start();
    }
}
