package com.griends.myapplication.picturescan.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.griends.myapplication.AppUtil;
import com.griends.myapplication.picturescan.core.BasePopupView;
import com.griends.myapplication.picturescan.enums.ImageType;
import com.griends.myapplication.picturescan.interfaces.PopupImageLoaderListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description:
 * Create by lxj, at 2018/12/7
 */
public class ImagePopupUtils {
    public static int getWindowWidth(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }
    
    public static int getWindowHeight(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
    }
    
    public static int getStatusBarHeight() {
        Resources resources = Resources.getSystem();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }
    
    /**
     * Return the navigation bar's height.
     *
     * @return the navigation bar's height
     */
    public static int getNavBarHeight() {
        Resources res = Resources.getSystem();
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId != 0) {
            return res.getDimensionPixelSize(resourceId);
        }
        return 0;
    }
    
    public static void setWidthHeight(View target, int width, int height) {
        if (width <= 0 && height <= 0) {
            return;
        }
        ViewGroup.LayoutParams params = target.getLayoutParams();
        if (width > 0) {
            params.width = width;
        }
        if (height > 0) {
            params.height = height;
        }
        target.setLayoutParams(params);
    }
    
    public static void applyPopupSize(ViewGroup content, int maxWidth, int maxHeight) {
        applyPopupSize(content, maxWidth, maxHeight, null);
    }
    
    public static void applyPopupSize(final ViewGroup content, final int maxWidth, final int maxHeight, final Runnable afterApplySize) {
        content.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = content.getLayoutParams();
                View implView = content.getChildAt(0);
                ViewGroup.LayoutParams implParams = implView.getLayoutParams();
                // 假设默认Content宽是match，高是wrap
                int w = content.getMeasuredWidth();
                // response impl view wrap_content params.
                if (implParams.width == FrameLayout.LayoutParams.WRAP_CONTENT) {
                    w = Math.min(w, implView.getMeasuredWidth());
                }
                if (maxWidth != 0) {
                    params.width = Math.min(w, maxWidth);
                }
                
                int h = content.getMeasuredHeight();
                // response impl view match_parent params.
                if (implParams.height == FrameLayout.LayoutParams.MATCH_PARENT) {
                    h = ((ViewGroup) content.getParent()).getMeasuredHeight();
                    params.height = h;
                }
                if (maxHeight != 0) {
                    // 如果content的高为match，则maxHeight限制impl
                    if (params.height == FrameLayout.LayoutParams.MATCH_PARENT ||
                            params.height == (getWindowHeight(content.getContext()) + getStatusBarHeight())) {
                        implParams.height = Math.min(implView.getMeasuredHeight(), maxHeight);
                        implView.setLayoutParams(implParams);
                    } else {
                        params.height = Math.min(h, maxHeight);
                    }
                }
                content.setLayoutParams(params);
                
                if (afterApplySize != null) {
                    afterApplySize.run();
                }
            }
        });
    }
    
    public static void setCursorDrawableColor(EditText et, int color) {
        //暂时没有找到有效的方法来动态设置cursor的颜色
    }
    
    public static BitmapDrawable createBitmapDrawable(Resources resources, int width, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, 20, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0, 0, bitmap.getWidth(), 4, paint);
        paint.setColor(Color.TRANSPARENT);
        canvas.drawRect(0, 4, bitmap.getWidth(), 20, paint);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(resources, bitmap);
        bitmapDrawable.setGravity(Gravity.BOTTOM);
        return bitmapDrawable;
    }
    
    public static StateListDrawable createSelector(Drawable defaultDrawable, Drawable focusDrawable) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_focused}, focusDrawable);
        stateListDrawable.addState(new int[]{}, defaultDrawable);
        return stateListDrawable;
    }
    
    public static boolean isInRect(float x, float y, Rect rect) {
        return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom;
    }
    
    /**
     * Return whether soft input is visible.
     *
     * @param activity The activity.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isSoftInputVisible(final Activity activity) {
        return getDecorViewInvisibleHeight(activity) > 0;
    }
    
    private static int sDecorViewDelta = 0;
    
    public static int getDecorViewInvisibleHeight(final Activity activity) {
        final View decorView = activity.getWindow().getDecorView();
        final Rect outRect = new Rect();
        decorView.getWindowVisibleDisplayFrame(outRect);
        int delta = Math.abs(decorView.getBottom() - outRect.bottom);
        if (delta <= getNavBarHeight()) {
            sDecorViewDelta = delta;
            return 0;
        }
        return delta - sDecorViewDelta;
    }
    
    public static void moveUpToKeyboard(int keyboardHeight, BasePopupView pv) {
        if (!pv.popupInfo.isMoveUpToKeyboard) return;
        //判断是否盖住输入框
        ArrayList<EditText> allEts = new ArrayList<>();
        findAllEditText(allEts, pv);
        EditText focusEt = null;
        for (EditText et : allEts) {
            if (et.isFocused()) {
                focusEt = et;
                break;
            }
        }
        
        int popupHeight = pv.getPopupContentView().getHeight();
        int popupWidth = pv.getPopupContentView().getWidth();
        if (pv.getPopupImplView() != null) {
            popupHeight = Math.min(popupHeight, pv.getPopupImplView().getMeasuredHeight());
            popupWidth = Math.min(popupWidth, pv.getPopupImplView().getMeasuredWidth());
        }
        int windowHeight = getWindowHeight(pv.getContext());
        int focusEtTop = 0;
        int focusBottom = 0;
        if (focusEt != null) {
            int[] locations = new int[2];
            focusEt.getLocationInWindow(locations);
            focusEtTop = locations[1];
            focusBottom = focusEtTop + focusEt.getMeasuredHeight();
        }
        
        //执行上移
        if ((popupWidth == ImagePopupUtils.getWindowWidth(pv.getContext()) &&
                popupHeight == (ImagePopupUtils.getWindowHeight(pv.getContext()) + ImagePopupUtils.getStatusBarHeight()))) {
            // 如果是全屏弹窗，特殊处理，只要输入框没被盖住，就不移动。
            if (focusBottom + keyboardHeight < windowHeight) {
                return;
            }
        }
        //dy=0说明没有触发移动，有些弹窗有translationY，不能影响它们
        if (pv.getPopupContentView().getTranslationY() != 0) return;
        pv.getPopupContentView().animate().translationY(0)
                .setDuration(200)
                .setInterpolator(new OvershootInterpolator(0))
                .start();
    }
    
    /**
     * Return whether the navigation bar visible.
     * <p>Call it in onWindowFocusChanged will get right result.</p>
     *
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isNavBarVisible(Context context) {
        boolean isVisible = false;
        ViewGroup decorView = (ViewGroup) ((Activity) context).getWindow().getDecorView();
        for (int i = 0, count = decorView.getChildCount(); i < count; i++) {
            final View child = decorView.getChildAt(i);
            final int id = child.getId();
            if (id != View.NO_ID) {
                String resourceEntryName = context
                        .getResources()
                        .getResourceEntryName(id);
                if ("navigationBarBackground".equals(resourceEntryName)
                        && child.getVisibility() == View.VISIBLE) {
                    isVisible = true;
                    break;
                }
            }
        }
        if (isVisible) {
            int visibility = decorView.getSystemUiVisibility();
            isVisible = (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
        }
        return isVisible;
    }
    
    public static void findAllEditText(ArrayList<EditText> list, ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View v = group.getChildAt(i);
            if (v instanceof EditText && v.getVisibility() == View.VISIBLE) {
                list.add((EditText) v);
            } else if (v instanceof ViewGroup) {
                findAllEditText(list, (ViewGroup) v);
            }
        }
    }
    
    
    public static void saveBmpToAlbum(final Context context, final PopupImageLoaderListener imageLoader, final Object uri) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                File source = imageLoader.getImageFile(context, uri);
                if (source == null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "图片不存在！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                //1. create path
                String dirPath = context.getExternalFilesDir("").getPath();
                File dirFile = new File(dirPath);
                if (!dirFile.exists()) {
                    dirFile.mkdirs();
                }
                try {
                    ImageType type = ImageHeaderParser.getImageType(new FileInputStream(source));
                    String ext = getFileExt(type);
                    final File target = new File(dirPath, System.currentTimeMillis() + "." + ext);
                    if (target.exists()) {
                        target.delete();
                    }
                    target.createNewFile();
                    //2. save
                    final boolean save = writeFileFromIS(target, new FileInputStream(source));
                    Log.d("aaaaa", "save ==== " + save + "：" + target.getAbsolutePath());
                    //3. 其次把文件插入到系统图库
                    if (AppUtil.isAndroidQ()) {
                        Uri uri1 = Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                        ContentValues values = new ContentValues();
                        long dateToken = System.currentTimeMillis();
                        values.put(Media.DATE_TAKEN, dateToken);
                        values.put(Media.DESCRIPTION, "this is an image");
                        values.put(Media.IS_PRIVATE, 1);
                        values.put(Media.DISPLAY_NAME, target.getName());
                        values.put(Media.MIME_TYPE, "image/" + ext);
                        values.put(Media.TITLE, "image");
                        values.put(Media.RELATIVE_PATH, "Pictures");
                        long dateAdded = System.currentTimeMillis() / 1000;
                        values.put(Media.DATE_ADDED, dateAdded);
                        values.put(Media.DATE_MODIFIED, dateAdded);
                        
                        Uri insertUri = context.getContentResolver().insert(uri1, values);
                        OutputStream os = context.getContentResolver().openOutputStream(insertUri);
                        final Bitmap bitmap = BitmapFactory.decodeFile(target.getAbsolutePath());
                        bitmap.compress(CompressFormat.JPEG, 90, os);
                        os.close();
                    } else {
                        try {
                            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                                    target.getAbsolutePath(), target.getName(), null);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(target.getAbsolutePath())));
                    }
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "已保存到图库！", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "没有保存权限，保存功能无法使用！", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
    
    private static String getFileExt(ImageType type) {
        switch (type) {
            case GIF:
                return "gif";
            case PNG:
            case PNG_A:
                return "png";
            case WEBP:
            case WEBP_A:
                return "webp";
            case JPEG:
                return "jpeg";
        }
        return "jpeg";
    }
    
    public static boolean writeFileFromIS(final File file, final InputStream is) {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            byte[] data = new byte[8192];
            int len;
            while ((len = is.read(data, 0, 8192)) != -1) {
                os.write(data, 0, len);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static int getWidth(Drawable drawable) {
        Bitmap bitmap = null;
        int width = 0;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ?
                                    Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
        }
        if (bitmap != null) {
            width = bitmap.getWidth();
            bitmap.recycle();
            bitmap = null;
        }
        return width;
    }
    
}
