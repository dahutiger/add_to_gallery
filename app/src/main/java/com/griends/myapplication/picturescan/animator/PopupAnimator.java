package com.griends.myapplication.picturescan.animator;

import android.view.View;

import com.griends.myapplication.picturescan.ImagePopup;
import com.griends.myapplication.picturescan.enums.PopupAnimation;

/**
 * Description: 弹窗动画执行器
 * Create by dance, at 2018/12/9
 */
public abstract class PopupAnimator {
    public View targetView;
    public PopupAnimation popupAnimation; // 内置的动画
    public PopupAnimator(){}
    public PopupAnimator(View target){
        this(target, null);
    }

    public PopupAnimator(View target, PopupAnimation popupAnimation){
        this.targetView = target;
        this.popupAnimation = popupAnimation;
    }

    public abstract void initAnimator();
    public abstract void animateShow();
    public abstract void animateDismiss();
    public int getDuration(){
        return ImagePopup.getAnimationDuration();
    }
}
