package com.example.shy.exoplayerdemo.LYEXO;

import android.content.Context;

import com.google.android.exoplayer2.SimpleExoPlayer;

/**
 * 控制全局只有一个播放器的那个啥
 */
public class LYEXOManger {
    private LYEXOManger() {
    }
    static LYEXOManger manger;
    SimpleExoPlayer player;
    public static LYEXOManger init(){
        if (manger==null){
            manger=new LYEXOManger();
        }
        return manger;
    }

    public SimpleExoPlayer getPlayer(Context context,String url){
        return player;
    }

    /**
     * 释放播放器资源
     */
    public void release(){
        if (player!=null){
            player.release();
        }
    }
}
