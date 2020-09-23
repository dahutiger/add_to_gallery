package com.griends.myapplication.picturescan.util;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.griends.myapplication.picturescan.interfaces.PopupImageLoaderListener;

import java.io.File;

public class PopupImageLoader implements PopupImageLoaderListener {
    @Override
    public void loadImage(int position, @NonNull Object uri, @NonNull ImageView imageView) {
        //必须指定Target.SIZE_ORIGINAL，否则无法拿到原图
//        Glide.with(imageView).load(uri).apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)).into(imageView);

        Glide.with(imageView)
                .asBitmap()
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .format(DecodeFormat.PREFER_ARGB_8888)//设置图片解码格式
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
        imageView.setContentDescription(uri.toString());
    }

    @Override
    public File getImageFile(@NonNull Context context, @NonNull Object uri) {
        try {
            return Glide.with(context).downloadOnly().load(uri).submit().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
