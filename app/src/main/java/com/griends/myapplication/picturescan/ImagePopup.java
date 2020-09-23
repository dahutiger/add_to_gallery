package com.griends.myapplication.picturescan;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.griends.myapplication.picturescan.animator.PopupAnimator;
import com.griends.myapplication.picturescan.core.ImageViewerPopupView;
import com.griends.myapplication.picturescan.core.PopupInfo;
import com.griends.myapplication.picturescan.enums.PopupAnimation;
import com.griends.myapplication.picturescan.enums.PopupType;
import com.griends.myapplication.picturescan.interfaces.ImagePopupCallback;
import com.griends.myapplication.picturescan.interfaces.OnSrcViewUpdateListener;
import com.griends.myapplication.picturescan.interfaces.PopupImageLoaderListener;

import java.util.List;

public class ImagePopup {
    private ImagePopup() {
    }

    /**
     * 全局弹窗的设置
     **/
    private static int animationDuration = 360;
    private static int shadowBgColor = Color.parseColor("#9F000000");

    public static void setShadowBgColor(int color) {
        shadowBgColor = color;
    }

    public static int getShadowBgColor() {
        return shadowBgColor;
    }

    public static void setAnimationDuration(int duration) {
        if (duration >= 0) {
            animationDuration = duration;
        }
    }

    public static int getAnimationDuration() {
        return animationDuration;
    }

    public static class Builder {
        private final PopupInfo popupInfo = new PopupInfo();
        private Context context;

        public Builder(Context context) {
            this.context = context;
        }

        Builder popupType(PopupType popupType) {
            this.popupInfo.popupType = popupType;
            return this;
        }

        /**
         * 设置按下返回键是否关闭弹窗，默认为true
         *
         * @param isDismissOnBackPressed
         * @return
         */
        public Builder dismissOnBackPressed(Boolean isDismissOnBackPressed) {
            this.popupInfo.isDismissOnBackPressed = isDismissOnBackPressed;
            return this;
        }

        /**
         * 设置点击弹窗外面是否关闭弹窗，默认为true
         *
         * @param isDismissOnTouchOutside
         * @return
         */
        public Builder dismissOnTouchOutside(Boolean isDismissOnTouchOutside) {
            this.popupInfo.isDismissOnTouchOutside = isDismissOnTouchOutside;
            return this;
        }

        /**
         * 设置当操作完毕后是否自动关闭弹窗，默认为true。比如：点击Confirm弹窗的确认按钮默认是关闭弹窗，如果为false，则不关闭
         *
         * @param autoDismiss
         * @return
         */
        public Builder autoDismiss(Boolean autoDismiss) {
            this.popupInfo.autoDismiss = autoDismiss;
            return this;
        }

        /**
         * 弹窗是否有半透明背景遮罩，默认是true
         *
         * @param hasShadowBg
         * @return
         */
        public Builder hasShadowBg(Boolean hasShadowBg) {
            this.popupInfo.hasShadowBg = hasShadowBg;
            return this;
        }

        /**
         * 设置弹窗依附的View
         *
         * @param atView
         * @return
         */
        public Builder atView(View atView) {
            this.popupInfo.atView = atView;
            return this;
        }

        /**
         * 设置弹窗监视的View
         *
         * @param watchView
         * @return
         */
        public Builder watchView(View watchView) {
            this.popupInfo.watchView = watchView;
            this.popupInfo.watchView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (popupInfo.touchPoint == null || event.getAction() == MotionEvent.ACTION_DOWN)
                        popupInfo.touchPoint = new PointF(event.getRawX(), event.getRawY());
                    return false;
                }
            });
            return this;
        }

        /**
         * 为弹窗设置内置的动画器，默认情况下，已经为每种弹窗设置了效果最佳的动画器；如果你不喜欢，仍然可以修改。
         *
         * @param popupAnimation
         * @return
         */
        public Builder popupAnimation(PopupAnimation popupAnimation) {
            this.popupInfo.popupAnimation = popupAnimation;
            return this;
        }

        /**
         * 自定义弹窗动画器
         *
         * @param customAnimator
         * @return
         */
        public Builder customAnimator(PopupAnimator customAnimator) {
            this.popupInfo.customAnimator = customAnimator;
            return this;
        }

        /**
         * 设置最大宽度，如果重写了弹窗的getMaxWidth，则以重写的为准
         *
         * @param maxWidth
         * @return
         */
        public Builder maxWidth(int maxWidth) {
            this.popupInfo.maxWidth = maxWidth;
            return this;
        }

        /**
         * 设置最大高度，如果重写了弹窗的getMaxHeight，则以重写的为准
         *
         * @param maxHeight
         * @return
         */
        public Builder maxHeight(int maxHeight) {
            this.popupInfo.maxHeight = maxHeight;
            return this;
        }

        /**
         * 设置是否给StatusBar添加阴影，目前对Drawer弹窗生效。如果你的Drawer的背景是白色，建议设置为true，因为状态栏文字的颜色也往往
         * 是白色，会导致状态栏文字看不清；如果Drawer的背景色不是白色，则忽略即可
         *
         * @param hasStatusBarShadow
         * @return
         */
        public Builder hasStatusBarShadow(boolean hasStatusBarShadow) {
            this.popupInfo.hasStatusBarShadow = hasStatusBarShadow;
            return this;
        }

        /**
         * 弹窗在x方向的偏移量，对所有弹窗生效，单位是px
         *
         * @param offsetX
         * @return
         */
        public Builder offsetX(int offsetX) {
            this.popupInfo.offsetX = offsetX;
            return this;
        }

        /**
         * 弹窗在y方向的偏移量，对所有弹窗生效，单位是px
         *
         * @param offsetY
         * @return
         */
        public Builder offsetY(int offsetY) {
            this.popupInfo.offsetY = offsetY;
            return this;
        }

        /**
         * 是否启用拖拽，比如：Bottom弹窗默认是带手势拖拽效果的，如果禁用则不能拖拽
         *
         * @param enableDrag
         * @return
         */
        public Builder enableDrag(boolean enableDrag) {
            this.popupInfo.enableDrag = enableDrag;
            return this;
        }

        /**
         * /**
         * 设置弹窗显示和隐藏的回调监听
         *
         * @param imagePopupCallback
         * @return
         */
        public Builder setPopupCallback(ImagePopupCallback imagePopupCallback) {
            this.popupInfo.imagePopupCallback = imagePopupCallback;
            return this;
        }

        /**
         * 大图浏览类型弹窗，单张图片使用场景
         *
         * @param srcView 源View，就是你点击的那个ImageView，弹窗消失的时候需回到该位置。如果实在没有这个View，可以传空，但是动画变的非常僵硬，适用于在Webview点击场景
         * @return
         */
        public ImageViewerPopupView asImageViewer(ImageView srcView, String url, PopupImageLoaderListener imageLoader) {
            popupType(PopupType.ImageViewer);
            ImageViewerPopupView popupView = new ImageViewerPopupView(this.context)
                    .setSingleSrcView(srcView, url)
                    .setXPopupImageLoader(imageLoader);
            popupView.popupInfo = this.popupInfo;
            return popupView;
        }

        /**
         * 大图浏览类型弹窗，单张图片使用场景
         *
         * @param srcView           源View，就是你点击的那个ImageView，弹窗消失的时候需回到该位置。如果实在没有这个View，可以传空，但是动画变的非常僵硬，适用于在Webview点击场景
         * @param url               资源id，url或者文件路径
         * @param isInfinite        是否需要无限滚动，默认为false
         * @param placeholderColor  占位View的填充色，默认为-1
         * @param placeholderStroke 占位View的边框色，默认为-1
         * @param placeholderRadius 占位View的圆角大小，默认为-1
         * @param isShowSaveBtn     是否显示保存按钮，默认显示
         * @return
         */
        public ImageViewerPopupView asImageViewer(ImageView srcView, String url, boolean isInfinite, int placeholderColor, int placeholderStroke, int placeholderRadius,
                                                  boolean isShowSaveBtn, PopupImageLoaderListener imageLoader) {
            popupType(PopupType.ImageViewer);
            ImageViewerPopupView popupView = new ImageViewerPopupView(this.context)
                    .setSingleSrcView(srcView, url)
                    .isInfinite(isInfinite)
                    .setPlaceholderColor(placeholderColor)
                    .setPlaceholderStrokeColor(placeholderStroke)
                    .setPlaceholderRadius(placeholderRadius)
                    .isShowSaveButton(isShowSaveBtn)
                    .setXPopupImageLoader(imageLoader);
            popupView.popupInfo = this.popupInfo;
            return popupView;
        }

        /**
         * 大图浏览类型弹窗，多张图片使用场景
         *
         * @param srcView               源View，就是你点击的那个ImageView，弹窗消失的时候需回到该位置。如果实在没有这个View，可以传空，但是动画变的非常僵硬，适用于在Webview点击场景
         * @param currentPosition       指定显示图片的位置
         * @param urls                  图片url集合
         * @param srcViewUpdateListener 当滑动ViewPager切换图片后，需要更新srcView，此时会执行该回调，你需要调用updateSrcView方法。
         * @return
         */
        public ImageViewerPopupView asImageViewer(ImageView srcView, int currentPosition, List<String> urls,
                                                  OnSrcViewUpdateListener srcViewUpdateListener, PopupImageLoaderListener imageLoader) {
            return asImageViewer(srcView, currentPosition, urls, false, -1, -1, -1, true, srcViewUpdateListener, imageLoader);
        }

        /**
         * 大图浏览类型弹窗，多张图片使用场景
         *
         * @param srcView               源View，就是你点击的那个ImageView，弹窗消失的时候需回到该位置。如果实在没有这个View，可以传空，但是动画变的非常僵硬，适用于在Webview点击场景
         * @param currentPosition       指定显示图片的位置
         * @param urls                  图片url集合
         * @param isInfinite            是否需要无限滚动，默认为false
         * @param placeholderColor      占位View的填充色，默认为-1
         * @param placeholderStroke     占位View的边框色，默认为-1
         * @param placeholderRadius     占位View的圆角大小，默认为-1
         * @param isShowSaveBtn         是否显示保存按钮，默认显示
         * @param srcViewUpdateListener 当滑动ViewPager切换图片后，需要更新srcView，此时会执行该回调，你需要调用updateSrcView方法。
         */
        public ImageViewerPopupView asImageViewer(ImageView srcView, int currentPosition, List<String> urls,
                                                  boolean isInfinite,
                                                  int placeholderColor, int placeholderStroke, int placeholderRadius, boolean isShowSaveBtn,
                                                  OnSrcViewUpdateListener srcViewUpdateListener, PopupImageLoaderListener imageLoader) {
            popupType(PopupType.ImageViewer);
            ImageViewerPopupView popupView = new ImageViewerPopupView(this.context)
                    .setSrcView(srcView, currentPosition)
                    .setImageUrls(urls)
                    .isInfinite(isInfinite)
                    .setPlaceholderColor(placeholderColor)
                    .setPlaceholderStrokeColor(placeholderStroke)
                    .setPlaceholderRadius(placeholderRadius)
                    .isShowSaveButton(isShowSaveBtn)
                    .setSrcViewUpdateListener(srcViewUpdateListener)
                    .setXPopupImageLoader(imageLoader);
            popupView.popupInfo = this.popupInfo;
            return popupView;
        }
    }
}
