package com.griends.myapplication;

import android.app.Activity;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class AppUtil {

    private static Context mContext;
    private static Handler sMainHandler;

    private AppUtil() {
    }

    public static void init(Context context) {
        if (context == null) {
            throw new NullPointerException("Context is null");
        }
        mContext = context.getApplicationContext();
    }

    public static Handler getMainHandler() {
        if (sMainHandler == null) {
            synchronized (AppUtil.class) {
                if (sMainHandler == null) {
                    sMainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return sMainHandler;
    }

    public static Context getContext() {
        return mContext;
    }

    public static Object getSystemService(@NonNull String name) {
        return mContext.getSystemService(name);
    }

    public static Resources getResources() {
        return mContext.getResources();
    }

    public static String getPackageName() {
        return mContext.getPackageName();
    }

    /**
     * 获取版本名
     */
    public static String getVersionName() {
        return getVersionName(getPackageName());
    }

    /**
     * 获取版本名
     */
    public static String getVersionName(final String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            try {
                PackageManager pm = mContext.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(packageName, 0);
                return pi == null ? null : pi.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 获取版本号
     */
    public static int getVersionCode() {
        return getVersionCode(getPackageName());
    }

    /**
     * 获取版本号
     */
    public static int getVersionCode(final String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            try {
                PackageManager pm = mContext.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(packageName, 0);
                return pi == null ? -1 : pi.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static boolean isAndroidQ() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    /**
     * 是否为主进程
     *
     * @return
     */
    public static boolean isMainProcess() {
        android.app.ActivityManager am = ((android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    private static String channelName = null;

    /**
     * 获取线上电子市场渠道
     *
     * @return
     */
    public static String getMarketChannel() {
        if (!TextUtils.isEmpty(channelName)) {
            return channelName;
        }
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            channelName = applicationInfo.metaData.getString("umeng_channel");
        } catch (Exception e) {
            channelName = "official";
            e.printStackTrace();
        }
        return channelName;
    }

    public static void installApk(Uri uri) {
        if (uri == null || TextUtils.isEmpty(uri.getPath())) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        getContext().startActivity(intent);
    }

    /**
     * AppUtil.copyDb("database.db", "database.db");
     * 拷贝数据库
     *
     * @param dbName
     * @param newDBName
     */
    public static void copyDb(String dbName, String newDBName) {
        File f = AppUtil.getContext().getDatabasePath(dbName);
        copyFile(f.getAbsolutePath(), Environment.getExternalStorageDirectory().getPath() + File.separator + newDBName);
    }

    // 文件拷贝,将一个文件复制到另外一个目录下的临时文件
    public static int copyFile(String fromFile, String toFile) {
        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte[] bt = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    /**
     * 判断应用是否正在前台运行
     *
     * @param context
     * @return
     */
    public static boolean isFrontRunning(Context context) {
        boolean isFront = false;
        android.app.ActivityManager am = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (RunningAppProcessInfo processInfo : runningProcesses) {
                //前台程序
                if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isFront = true;
                            break;
                        }
                    }
                }
            }
        } else {
            List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isFront = true;
            }
        }
        return isFront;
    }

    /**
     * 设置振动
     * @param context
     */
    public static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200L);
    }

    public static void gotoAppDetail(Context context) {
        Intent detailIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", AppUtil.getPackageName(), null);
        detailIntent.setData(uri);
        context.startActivity(detailIntent);
    }

    // 显示或隐藏状态栏，导航栏，在setContentView方法之前调用
    public static void setSystemUIVisible(Activity context, boolean show) {
        if (show) {
            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            uiFlags |= 0x00001000;
            context.getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        } else {
            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            uiFlags |= 0x00001000;
            context.getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        }
    }
}
