package com.griends.myapplication.picturescan.core;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.griends.myapplication.picturescan.ImagePopup;
import com.griends.myapplication.picturescan.animator.EmptyAnimator;
import com.griends.myapplication.picturescan.animator.PopupAnimator;
import com.griends.myapplication.picturescan.animator.ScaleAlphaAnimator;
import com.griends.myapplication.picturescan.animator.ScrollScaleAnimator;
import com.griends.myapplication.picturescan.animator.ShadowBgAnimator;
import com.griends.myapplication.picturescan.animator.TranslateAlphaAnimator;
import com.griends.myapplication.picturescan.animator.TranslateAnimator;
import com.griends.myapplication.picturescan.enums.PopupStatus;
import com.griends.myapplication.picturescan.util.ImagePopupUtils;

import java.util.ArrayList;
import java.util.Stack;

import static com.griends.myapplication.picturescan.enums.PopupAnimation.NoAnimation;


/**
 * Description: 弹窗基类
 * Create by lxj, at 2018/12/7
 */
public abstract class BasePopupView extends FrameLayout {
    //静态存储所有弹窗对象
    private static Stack<BasePopupView> stack = new Stack<>();
    public PopupInfo popupInfo;
    protected PopupAnimator popupContentAnimator;
    protected ShadowBgAnimator shadowBgAnimator;
    private int touchSlop;
    public PopupStatus popupStatus = PopupStatus.Dismiss;
    private boolean isCreated = false;
    
    public BasePopupView(@NonNull Context context) {
        super(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        shadowBgAnimator = new ShadowBgAnimator(this);
        //  添加Popup窗体内容View
        View contentView = LayoutInflater.from(context).inflate(getPopupLayoutId(), this, false);
        // 事先隐藏，等测量完毕恢复，避免View影子跳动现象。
        contentView.setAlpha(0);
        addView(contentView);
    }
    
    public BasePopupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    public BasePopupView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    /**
     * 执行初始化
     */
    public void init() {
        if (popupStatus == PopupStatus.Showing) {
            return;
        }
        popupStatus = PopupStatus.Showing;
        
        //1. 初始化Popup
        if (!isCreated) {
            initPopupContent();
        }
        //apply size dynamic
        if (!(this instanceof ImageViewerPopupView)) {
            ImagePopupUtils.setWidthHeight(getTargetSizeView(),
                    (getMaxWidth() != 0 && getPopupWidth() > getMaxWidth()) ? getMaxWidth() : getPopupWidth(),
                    (getMaxHeight() != 0 && getPopupHeight() > getMaxHeight()) ? getMaxHeight() : getPopupHeight()
            );
        }
        if (!isCreated) {
            isCreated = true;
            onCreate();
            if (popupInfo.imagePopupCallback != null) {
                popupInfo.imagePopupCallback.onCreated();
            }
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                // 如果有导航栏，则不能覆盖导航栏，判断各种屏幕方向
                applySize(false);
                getPopupContentView().setAlpha(1f);
                
                //2. 收集动画执行器
                collectAnimator();
                
                //3. 执行动画
                doShowAnimation();
                
                doAfterShow();
                
            }
        }, 50);
        
    }
    
    private boolean hasMoveUp = false;
    
    private void collectAnimator() {
        if (popupContentAnimator == null) {
            // 优先使用自定义的动画器
            if (popupInfo.customAnimator != null) {
                popupContentAnimator = popupInfo.customAnimator;
                popupContentAnimator.targetView = getPopupContentView();
            } else {
                // 根据PopupInfo的popupAnimation字段来生成对应的动画执行器，如果popupAnimation字段为null，则返回null
                popupContentAnimator = genAnimatorByPopupType();
                if (popupContentAnimator == null) {
                    popupContentAnimator = getPopupAnimator();
                }
            }
            
            //3. 初始化动画执行器
            shadowBgAnimator.initAnimator();
            if (popupContentAnimator != null) {
                popupContentAnimator.initAnimator();
            }
        }
    }
    
    protected void applySize(boolean isShowNavBar) {
        LayoutParams params = (LayoutParams) getLayoutParams();
        int rotation = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        boolean isNavBarShown = isShowNavBar || ImagePopupUtils.isNavBarVisible(getContext());
        if (rotation == 0) {
            params.leftMargin = 0;
            params.rightMargin = 0;
            params.bottomMargin = isNavBarShown ? ImagePopupUtils.getNavBarHeight() : 0;
        } else if (rotation == 1) {
            params.bottomMargin = 0;
            params.rightMargin = isNavBarShown ? ImagePopupUtils.getNavBarHeight() : 0;
            params.leftMargin = 0;
        } else if (rotation == 3) {
            params.bottomMargin = 0;
            params.leftMargin = 0;
            params.rightMargin = isNavBarShown ? ImagePopupUtils.getNavBarHeight() : 0;
        }
        setLayoutParams(params);
    }
    
    public BasePopupView show() {
        if (getParent() != null) {
            return this;
        }
        final Activity activity = (Activity) getContext();
        popupInfo.decorView = (ViewGroup) activity.getWindow().getDecorView();
        // 1. add PopupView to its decorView after measured.
        popupInfo.decorView.post(new Runnable() {
            @Override
            public void run() {
                if (getParent() != null) {
                    ((ViewGroup) getParent()).removeView(BasePopupView.this);
                }
                popupInfo.decorView.addView(BasePopupView.this, new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT));
                
                //2. do init，game start.
                init();
            }
        });
        return this;
    }
    
    protected void doAfterShow() {
        removeCallbacks(doAfterShowTask);
        postDelayed(doAfterShowTask, getAnimationDuration());
    }
    
    private Runnable doAfterShowTask = new Runnable() {
        @Override
        public void run() {
            popupStatus = PopupStatus.Show;
            onShow();
            if (popupInfo != null && popupInfo.imagePopupCallback != null)
                popupInfo.imagePopupCallback.onShow();
            if (ImagePopupUtils.getDecorViewInvisibleHeight((Activity) getContext()) > 0 && !hasMoveUp) {
                ImagePopupUtils.moveUpToKeyboard(ImagePopupUtils.getDecorViewInvisibleHeight((Activity) getContext()), BasePopupView.this);
            }
        }
    };
    
    public void focusAndProcessBackPress() {
        if (popupInfo.isRequestFocus) {
            setFocusableInTouchMode(true);
            requestFocus();
            if (!stack.contains(this)) stack.push(this);
        }
        
        //let all EditText can process back pressed.
        ArrayList<EditText> list = new ArrayList<>();
        ImagePopupUtils.findAllEditText(list, (ViewGroup) getPopupContentView());
        for (int i = 0; i < list.size(); i++) {
            final EditText et = list.get(i);
            if (i == 0 && popupInfo.autoFocusEditText) {
                et.setFocusable(true);
                et.setFocusableInTouchMode(true);
                et.requestFocus();
            }
        }
    }
    
    /**
     * 根据PopupInfo的popupAnimation字段来生成对应的内置的动画执行器
     */
    protected PopupAnimator genAnimatorByPopupType() {
        if (popupInfo == null || popupInfo.popupAnimation == null) return null;
        switch (popupInfo.popupAnimation) {
            case ScaleAlphaFromCenter:
            case ScaleAlphaFromLeftTop:
            case ScaleAlphaFromRightTop:
            case ScaleAlphaFromLeftBottom:
            case ScaleAlphaFromRightBottom:
                return new ScaleAlphaAnimator(getPopupContentView(), popupInfo.popupAnimation);
            
            case TranslateAlphaFromLeft:
            case TranslateAlphaFromTop:
            case TranslateAlphaFromRight:
            case TranslateAlphaFromBottom:
                return new TranslateAlphaAnimator(getPopupContentView(), popupInfo.popupAnimation);
            
            case TranslateFromLeft:
            case TranslateFromTop:
            case TranslateFromRight:
            case TranslateFromBottom:
                return new TranslateAnimator(getPopupContentView(), popupInfo.popupAnimation);
            
            case ScrollAlphaFromLeft:
            case ScrollAlphaFromLeftTop:
            case ScrollAlphaFromTop:
            case ScrollAlphaFromRightTop:
            case ScrollAlphaFromRight:
            case ScrollAlphaFromRightBottom:
            case ScrollAlphaFromBottom:
            case ScrollAlphaFromLeftBottom:
                return new ScrollScaleAnimator(getPopupContentView(), popupInfo.popupAnimation);
            
            case NoAnimation:
                return new EmptyAnimator();
        }
        return null;
    }
    
    protected abstract int getPopupLayoutId();
    
    /**
     * 如果你自己继承BasePopupView来做，这个不用实现
     */
    protected int getImplLayoutId() {
        return -1;
    }
    
    /**
     * 获取PopupAnimator，用于每种类型的PopupView自定义自己的动画器
     */
    protected PopupAnimator getPopupAnimator() {
        return null;
    }
    
    /**
     * 请使用onCreate，主要给弹窗内部用，不要去重写。
     */
    protected void initPopupContent() {
    }
    
    /**
     * do init.
     */
    protected void onCreate() {
    }
    
    /**
     * 执行显示动画：动画由2部分组成，一个是背景渐变动画，一个是Content的动画；
     * 背景动画由父类实现，Content由子类实现
     */
    public void doShowAnimation() {
        if (popupInfo.hasShadowBg) {
            shadowBgAnimator.isZeroDuration = (popupInfo.popupAnimation == NoAnimation);
            shadowBgAnimator.animateShow();
        }
        if (popupContentAnimator != null)
            popupContentAnimator.animateShow();
    }
    
    /**
     * 执行消失动画：动画由2部分组成，一个是背景渐变动画，一个是Content的动画；
     * 背景动画由父类实现，Content由子类实现
     */
    public void doDismissAnimation() {
        if (popupInfo.hasShadowBg) {
            shadowBgAnimator.animateDismiss();
        }
        if (popupContentAnimator != null)
            popupContentAnimator.animateDismiss();
    }
    
    /**
     * 获取内容View，本质上PopupView显示的内容都在这个View内部。
     * 而且我们对PopupView执行的动画，也是对它执行的动画
     */
    public View getPopupContentView() {
        return getChildAt(0);
    }
    
    public View getPopupImplView() {
        return ((ViewGroup) getPopupContentView()).getChildAt(0);
    }
    
    public int getAnimationDuration() {
        return popupInfo.popupAnimation == NoAnimation ? 10 : ImagePopup.getAnimationDuration();
    }
    
    /**
     * 弹窗的最大宽度，一般用来限制布局宽度为wrap或者match时的最大宽度
     */
    protected int getMaxWidth() {
        return popupInfo.maxWidth;
    }
    
    /**
     * 弹窗的最大高度，一般用来限制布局高度为wrap或者match时的最大宽度
     */
    protected int getMaxHeight() {
        return popupInfo.maxHeight;
    }
    
    /**
     * 弹窗的宽度，用来动态设定当前弹窗的宽度，受getMaxWidth()限制
     */
    protected int getPopupWidth() {
        return popupInfo.maxWidth;
    }
    
    /**
     * 弹窗的高度，用来动态设定当前弹窗的高度，受getMaxHeight()限制
     */
    protected int getPopupHeight() {
        return 0;
    }
    
    protected View getTargetSizeView() {
        return getPopupContentView();
    }
    
    /**
     * 消失
     */
    public void dismiss() {
        if (popupStatus == PopupStatus.Dismissing) {
            return;
        }
        popupStatus = PopupStatus.Dismissing;
        clearFocus();
        doDismissAnimation();
        doAfterDismiss();
    }
    
    public void delayDismiss(long delay) {
        if (delay < 0) delay = 0;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, delay);
    }
    
    public void delayDismissWith(long delay, Runnable runnable) {
        this.dismissWithRunnable = runnable;
        delayDismiss(delay);
    }
    
    protected void doAfterDismiss() {
        removeCallbacks(doAfterDismissTask);
        postDelayed(doAfterDismissTask, getAnimationDuration());
    }
    
    private Runnable doAfterDismissTask = new Runnable() {
        @Override
        public void run() {
            onDismiss();
            if (popupInfo == null) {
                return;
            }
            if (popupInfo.imagePopupCallback != null) {
                popupInfo.imagePopupCallback.onDismiss();
            }
            if (dismissWithRunnable != null) {
                dismissWithRunnable.run();
                dismissWithRunnable = null;//no cache, avoid some bad edge effect.
            }
            popupStatus = PopupStatus.Dismiss;
            
            if (!stack.isEmpty()) stack.pop();
            if (popupInfo.isRequestFocus) {
                if (!stack.isEmpty()) {
                    stack.get(stack.size() - 1).focusAndProcessBackPress();
                } else {
                    // 让根布局拿焦点，避免布局内RecyclerView类似布局获取焦点导致布局滚动
                    View needFocusView = ((Activity) getContext()).findViewById(android.R.id.content);
                    needFocusView.setFocusable(true);
                    needFocusView.setFocusableInTouchMode(true);
                }
            }
            
            // 移除弹窗，GameOver
            if (popupInfo.decorView != null) {
                popupInfo.decorView.removeView(BasePopupView.this);
            }
        }
    };
    
    Runnable dismissWithRunnable;
    
    public void dismissWith(Runnable runnable) {
        this.dismissWithRunnable = runnable;
        dismiss();
    }
    
    public boolean isShowing() {
        return popupStatus != PopupStatus.Dismiss;
    }
    
    public boolean isDismiss() {
        return popupStatus == PopupStatus.Dismiss;
    }
    
    public void toggle() {
        if (isShowing()) {
            dismiss();
        } else {
            show();
        }
    }
    
    /**
     * 消失动画执行完毕后执行
     */
    protected void onDismiss() {
    }
    
    /**
     * 显示动画执行完毕后执行
     */
    protected void onShow() {
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stack.clear();
        removeCallbacks(doAfterShowTask);
        removeCallbacks(doAfterDismissTask);
        popupStatus = PopupStatus.Dismiss;
        hasMoveUp = false;
    }
    
    private float x, y;
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 如果自己接触到了点击，并且不在PopupContentView范围内点击，则进行判断是否是点击事件,如果是，则dismiss
        Rect rect = new Rect();
        getPopupContentView().getGlobalVisibleRect(rect);
        if (!ImagePopupUtils.isInRect(event.getX(), event.getY(), rect)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = event.getX();
                    y = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    float dx = event.getX() - x;
                    float dy = event.getY() - y;
                    float distance = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                    if (distance < touchSlop && popupInfo.isDismissOnTouchOutside) {
                        dismiss();
                    }
                    x = 0;
                    y = 0;
                    break;
            }
        }
        return true;
    }
    
}
