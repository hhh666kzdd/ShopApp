package com.example.shopapp.ui.base;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.shopapp.R;

/**
 * 图片加载工具：统一占位图与错误图，URL 为空时显示占位图。
 */
public class ImageLoader {

    private ImageLoader() {
    }

    public static void load(Context context, String url, ImageView target) {
        if (context == null || target == null) return;
        Glide.with(context)
                .load(url == null || url.trim().isEmpty() ? null : url.trim())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .fallback(R.drawable.ic_placeholder)
                .centerCrop()
                .into(target);
    }

    /** 圆形头像 */
    public static void loadCircle(Context context, String url, ImageView target) {
        if (context == null || target == null) return;
        Glide.with(context)
                .load(url == null || url.trim().isEmpty() ? null : url.trim())
                .placeholder(R.drawable.ic_avatar_default)
                .error(R.drawable.ic_avatar_default)
                .fallback(R.drawable.ic_avatar_default)
                .circleCrop()
                .into(target);
    }
}
