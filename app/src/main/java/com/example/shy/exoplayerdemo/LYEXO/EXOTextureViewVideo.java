package com.example.shy.exoplayerdemo.LYEXO;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * 基于 textture 的exo 的播放器
 * 现在主要处理M3U8
 */
public class EXOTextureViewVideo extends TextureView {
    public EXOTextureViewVideo(Context context) {
        super(context);
    }

    public EXOTextureViewVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EXOTextureViewVideo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
