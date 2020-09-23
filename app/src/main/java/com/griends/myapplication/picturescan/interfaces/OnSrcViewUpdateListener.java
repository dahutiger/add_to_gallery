package com.griends.myapplication.picturescan.interfaces;

import androidx.annotation.NonNull;

import com.griends.myapplication.picturescan.core.ImageViewerPopupView;

/**
 * Description:
 * Create by dance, at 2019/1/29
 */
public interface OnSrcViewUpdateListener {
    void onSrcViewUpdate(@NonNull ImageViewerPopupView popupView, int position);
}
