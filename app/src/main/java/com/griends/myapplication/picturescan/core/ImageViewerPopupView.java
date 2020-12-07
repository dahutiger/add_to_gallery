package com.griends.myapplication.picturescan.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.transition.ChangeBounds;
import androidx.transition.ChangeImageTransform;
import androidx.transition.ChangeTransform;
import androidx.transition.Transition;
import androidx.transition.TransitionListenerAdapter;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.griends.myapplication.PermissionUtil;
import com.griends.myapplication.R;
import com.griends.myapplication.picturescan.ImagePopup;
import com.griends.myapplication.picturescan.enums.PopupStatus;
import com.griends.myapplication.picturescan.interfaces.OnDragChangeListener;
import com.griends.myapplication.picturescan.interfaces.OnSrcViewUpdateListener;
import com.griends.myapplication.picturescan.interfaces.PopupImageLoaderListener;
import com.griends.myapplication.picturescan.permission.PermissionConstants;
import com.griends.myapplication.picturescan.permission.PermissionUtils;
import com.griends.myapplication.picturescan.photoview.PhotoView;
import com.griends.myapplication.picturescan.util.ImagePopupUtils;
import com.griends.myapplication.picturescan.widget.BlankView;
import com.griends.myapplication.picturescan.widget.HackyViewPager;
import com.griends.myapplication.picturescan.widget.PhotoViewContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 大图预览的弹窗，使用Transition实现
 * Create by lxj, at 2019/1/22
 */
public class ImageViewerPopupView extends BasePopupView implements OnDragChangeListener, View.OnClickListener {
    
    protected FrameLayout container;
    protected PhotoViewContainer photoViewContainer;
    protected BlankView placeholderView;
    protected TextView tvPagerIndicator;
    private ImageView imgSave;
    protected RelativeLayout saveLayout;
    protected HackyViewPager pager;
    
    protected ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    protected List<String> urls = new ArrayList<>();
    protected PopupImageLoaderListener imageLoader;
    protected OnSrcViewUpdateListener srcViewUpdateListener;
    protected int position;
    protected Rect rect = null;
    protected ImageView srcView; //动画起始的View，如果为null，移动和过渡动画效果会没有，只有弹窗的缩放功能
    protected PhotoView snapshotView;
    protected boolean isShowPlaceholder = true; //是否显示占位白色，当图片切换为大图时，原来的地方会有一个白色块
    protected int placeholderColor = -1; //占位View的颜色
    protected int placeholderStrokeColor = -1; // 占位View的边框色
    protected int placeholderRadius = -1; // 占位View的圆角
    protected boolean isShowSaveBtn = true; //是否显示保存按钮
    protected boolean isShowIndicator = true; //是否页码指示器
    protected boolean isInfinite = false;//是否需要无限滚动
    protected View customView;
    protected int bgColor = Color.rgb(32, 36, 46);//弹窗的背景颜色，可以自定义
    
    private Context context;
    
    public ImageViewerPopupView(@NonNull Context context) {
        super(context);
        this.context = context;
        container = findViewById(R.id.container);
        if (getImplLayoutId() > 0) {
            customView = LayoutInflater.from(context).inflate(getImplLayoutId(), container, false);
            customView.setVisibility(INVISIBLE);
            customView.setAlpha(0);
            container.addView(customView);
        }
    }
    
    @Override
    protected int getPopupLayoutId() {
        return R.layout.popup_image_viewer;
    }
    
    @Override
    protected void initPopupContent() {
        super.initPopupContent();
        
        tvPagerIndicator = findViewById(R.id.tv_pager_indicator);
        imgSave = findViewById(R.id.img_save);
        imgSave.setOnClickListener(this);
        
        saveLayout = findViewById(R.id.save_layout);
        placeholderView = findViewById(R.id.placeholderView);
        photoViewContainer = findViewById(R.id.photoViewContainer);
        photoViewContainer.setOnDragChangeListener(this);
        pager = findViewById(R.id.pager);
        pager.setAdapter(new PhotoViewAdapter());
        pager.setOffscreenPageLimit(urls.size());
        pager.setVisibility(INVISIBLE);
        addOrUpdateSnapshot();
        if (isInfinite) {
            pager.setOffscreenPageLimit(urls.size() / 2);
        }
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int i) {
                position = i;
                showPagerIndicator();
                //更新srcView
                if (srcViewUpdateListener != null) {
                    srcViewUpdateListener.onSrcViewUpdate(ImageViewerPopupView.this, i);
                }
            }
        });
        pager.setCurrentItem(position);
        if (!isShowIndicator) tvPagerIndicator.setVisibility(GONE);
        if (!isShowSaveBtn) {
            saveLayout.setVisibility(GONE);
        }
        
    }
    
    private void setupPlaceholder() {
        placeholderView.setVisibility(isShowPlaceholder ? VISIBLE : INVISIBLE);
        if (isShowPlaceholder) {
            if (placeholderColor != -1) {
                placeholderView.color = placeholderColor;
            }
            if (placeholderRadius != -1) {
                placeholderView.radius = placeholderRadius;
            }
            if (placeholderStrokeColor != -1) {
                placeholderView.strokeColor = placeholderStrokeColor;
            }
            ImagePopupUtils.setWidthHeight(placeholderView, rect.width(), rect.height());
            placeholderView.setTranslationX(rect.left);
            placeholderView.setTranslationY(rect.top);
            placeholderView.invalidate();
        }
    }
    
    private void showPagerIndicator() {
        if (urls.size() > 1) {
            int posi = isInfinite ? position % urls.size() : position;
            tvPagerIndicator.setText((posi + 1) + "/" + urls.size());
        }
        if (isShowSaveBtn) {
            saveLayout.setVisibility(VISIBLE);
        }
    }
    
    private void addOrUpdateSnapshot() {
        if (srcView == null) return;
        if (snapshotView == null) {
            snapshotView = new PhotoView(context);
            photoViewContainer.addView(snapshotView);
            snapshotView.setScaleType(srcView.getScaleType());
            snapshotView.setTranslationX(rect.left);
            snapshotView.setTranslationY(rect.top);
            ImagePopupUtils.setWidthHeight(snapshotView, rect.width(), rect.height());
        }
        setupPlaceholder();
        snapshotView.setImageDrawable(srcView.getDrawable());
    }
    
    @Override
    protected void doAfterShow() {
        //do nothing self.
    }
    
    @Override
    public void doShowAnimation() {
        if (srcView == null) {
            photoViewContainer.setBackgroundColor(bgColor);
            pager.setVisibility(VISIBLE);
            showPagerIndicator();
            photoViewContainer.isReleasing = false;
            ImageViewerPopupView.super.doAfterShow();
            return;
        }
        photoViewContainer.isReleasing = true;
        snapshotView.setVisibility(VISIBLE);
        if (customView != null) customView.setVisibility(VISIBLE);
        snapshotView.post(new Runnable() {
            @Override
            public void run() {
                TransitionManager.beginDelayedTransition((ViewGroup) snapshotView.getParent(), new TransitionSet()
                        .setDuration(ImagePopup.getAnimationDuration())
                        .addTransition(new ChangeBounds())
                        .addTransition(new ChangeTransform())
                        .addTransition(new ChangeImageTransform())
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .addListener(new TransitionListenerAdapter() {
                            @Override
                            public void onTransitionEnd(@NonNull Transition transition) {
                                pager.setVisibility(VISIBLE);
                                snapshotView.setVisibility(INVISIBLE);
                                showPagerIndicator();
                                photoViewContainer.isReleasing = false;
                                ImageViewerPopupView.super.doAfterShow();
                            }
                        }));
                snapshotView.setTranslationY(0);
                snapshotView.setTranslationX(0);
                snapshotView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ImagePopupUtils.setWidthHeight(snapshotView, photoViewContainer.getWidth(), photoViewContainer.getHeight());
                
                // do shadow anim.
                animateShadowBg(bgColor);
                if (customView != null) {
                    customView.animate().alpha(1f).setDuration(ImagePopup.getAnimationDuration()).start();
                }
            }
        });
        
    }
    
    private void animateShadowBg(final int endColor) {
        final int start = ((ColorDrawable) photoViewContainer.getBackground()).getColor();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                photoViewContainer.setBackgroundColor((Integer) argbEvaluator.evaluate(animation.getAnimatedFraction(),
                        start, endColor));
            }
        });
        animator.setDuration(ImagePopup.getAnimationDuration())
                .setInterpolator(new LinearInterpolator());
        animator.start();
    }
    
    @Override
    public void doDismissAnimation() {
        if (srcView == null) {
            photoViewContainer.setBackgroundColor(Color.TRANSPARENT);
            doAfterDismiss();
            pager.setVisibility(INVISIBLE);
            placeholderView.setVisibility(INVISIBLE);
            return;
        }
        tvPagerIndicator.setVisibility(INVISIBLE);
        saveLayout.setVisibility(INVISIBLE);
        pager.setVisibility(INVISIBLE);
        snapshotView.setVisibility(VISIBLE);
        photoViewContainer.isReleasing = true;
        TransitionManager.beginDelayedTransition((ViewGroup) snapshotView.getParent(), new TransitionSet()
                .setDuration(ImagePopup.getAnimationDuration())
                .addTransition(new ChangeBounds())
                .addTransition(new ChangeTransform())
                .addTransition(new ChangeImageTransform())
                .setInterpolator(new FastOutSlowInInterpolator())
                .addListener(new TransitionListenerAdapter() {
                    @Override
                    public void onTransitionEnd(@NonNull Transition transition) {
                        doAfterDismiss();
                        pager.setVisibility(INVISIBLE);
                        snapshotView.setVisibility(VISIBLE);
                        pager.setScaleX(1f);
                        pager.setScaleY(1f);
                        snapshotView.setScaleX(1f);
                        snapshotView.setScaleY(1f);
                        placeholderView.setVisibility(INVISIBLE);
                    }
                }));
        
        snapshotView.setTranslationY(rect.top);
        snapshotView.setTranslationX(rect.left);
        snapshotView.setScaleX(1f);
        snapshotView.setScaleY(1f);
        snapshotView.setScaleType(srcView.getScaleType());
        ImagePopupUtils.setWidthHeight(snapshotView, rect.width(), rect.height());
        
        // do shadow anim.
        animateShadowBg(Color.TRANSPARENT);
        if (customView != null) {
            customView.animate().alpha(0f).setDuration(ImagePopup.getAnimationDuration())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            if (customView != null) {
                                customView.setVisibility(INVISIBLE);
                            }
                        }
                    })
                    .start();
        }
    }
    
    @Override
    public int getAnimationDuration() {
        return 0;
    }
    
    @Override
    public void dismiss() {
        if (popupStatus != PopupStatus.Show) {
            return;
        }
        popupStatus = PopupStatus.Dismissing;
        if (srcView != null) {
            //snapshotView拥有当前pager中photoView的样子(matrix)
            PhotoView current = (PhotoView) pager.getChildAt(pager.getCurrentItem());
            if (current != null) {
                Matrix matrix = new Matrix();
                current.getSuppMatrix(matrix);
                snapshotView.setSuppMatrix(matrix);
            }
        }
        doDismissAnimation();
    }
    
    public ImageViewerPopupView setImageUrls(List<String> urls) {
        this.urls = urls;
        return this;
    }
    
    public ImageViewerPopupView setSrcViewUpdateListener(OnSrcViewUpdateListener srcViewUpdateListener) {
        this.srcViewUpdateListener = srcViewUpdateListener;
        return this;
    }
    
    public ImageViewerPopupView setXPopupImageLoader(PopupImageLoaderListener imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }
    
    /**
     * 是否显示白色占位区块
     *
     * @param isShow
     * @return
     */
    public ImageViewerPopupView isShowPlaceholder(boolean isShow) {
        this.isShowPlaceholder = isShow;
        return this;
    }
    
    /**
     * 是否显示页码指示器
     *
     * @param isShow
     * @return
     */
    public ImageViewerPopupView isShowIndicator(boolean isShow) {
        this.isShowIndicator = isShow;
        return this;
    }
    
    /**
     * 是否显示保存按钮
     *
     * @param isShowSaveBtn
     * @return
     */
    public ImageViewerPopupView isShowSaveButton(boolean isShowSaveBtn) {
        this.isShowSaveBtn = isShowSaveBtn;
        return this;
    }
    
    public ImageViewerPopupView isInfinite(boolean isInfinite) {
        this.isInfinite = isInfinite;
        return this;
    }
    
    public ImageViewerPopupView setPlaceholderColor(int color) {
        this.placeholderColor = color;
        return this;
    }
    
    public ImageViewerPopupView setPlaceholderRadius(int radius) {
        this.placeholderRadius = radius;
        return this;
    }
    
    public ImageViewerPopupView setPlaceholderStrokeColor(int strokeColor) {
        this.placeholderStrokeColor = strokeColor;
        return this;
    }
    
    /**
     * 设置单个使用的源View。单个使用的情况下，无需设置url集合和SrcViewUpdateListener
     *
     * @param srcView
     * @return
     */
    public ImageViewerPopupView setSingleSrcView(ImageView srcView, String url) {
        if (this.urls == null) {
            urls = new ArrayList<>();
        }
        urls.clear();
        urls.add(url);
        setSrcView(srcView, 0);
        return this;
    }
    
    public ImageViewerPopupView setSrcView(ImageView srcView, int position) {
        this.srcView = srcView;
        this.position = position;
        if (srcView != null) {
            int[] locations = new int[2];
            this.srcView.getLocationInWindow(locations);
            rect = new Rect(locations[0], locations[1], locations[0] + srcView.getWidth(), locations[1] + srcView.getHeight());
        }
        return this;
    }
    
    public void updateSrcView(ImageView srcView) {
        setSrcView(srcView, position);
        addOrUpdateSnapshot();
    }
    
    @Override
    public void onRelease() {
        dismiss();
    }
    
    @Override
    public void onDragChange(int dy, float scale, float fraction) {
        tvPagerIndicator.setAlpha(1 - fraction);
        if (customView != null) {
            customView.setAlpha(1 - fraction);
        }
        if (isShowSaveBtn) {
            imgSave.setAlpha(1 - fraction);
        }
        photoViewContainer.setBackgroundColor((Integer) argbEvaluator.evaluate(fraction * .8f, bgColor, Color.TRANSPARENT));
    }
    
    @Override
    protected void onDismiss() {
        super.onDismiss();
        srcView = null;
    }
    
    @Override
    public void onClick(View v) {
        if (v == imgSave) {
            save();
        }
    }
    
    
    /**
     * 保存图片到相册，会自动检查是否有保存权限
     */
    protected void save() {
        if (!PermissionUtil.checkStoragePermission((Activity) context)) {
            return;
        }
        //check permission
        PermissionUtils.create(context, PermissionConstants.STORAGE)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        //save bitmap to album.
//                        ImagePopupUtils.saveBmpToAlbum(context, imageLoader, urls.get(isInfinite ? position % urls.size() : position).getDataPath());
                        View view = pager.getChildAt(isInfinite ? position % urls.size() : position);
                        if (view instanceof PhotoView) {
                            ImagePopupUtils.saveBmpToAlbum(context, imageLoader, view.getContentDescription().toString());
                        }
                    }
                    
                    @Override
                    public void onDenied() {
                        Toast.makeText(context, "没有保存权限，保存功能无法使用！", Toast.LENGTH_SHORT).show();
                    }
                }).request();
    }
    
    public class PhotoViewAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return isInfinite ? Integer.MAX_VALUE / 2 : urls.size();
        }
        
        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return o == view;
        }
        
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            final PhotoView photoView = new PhotoView(context);
            // call LoadImageListener
            if (imageLoader != null) {
                imageLoader.loadImage(position, urls.get(isInfinite ? position % urls.size() : position), photoView);
            }

//            showImg(position, urls.get(isInfinite ? position % urls.size() : position), photoView);
            container.addView(photoView);
            photoView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            return photoView;
        }
        
        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
    
}
