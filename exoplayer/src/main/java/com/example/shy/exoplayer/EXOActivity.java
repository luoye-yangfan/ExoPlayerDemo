package com.example.shy.exoplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

public class EXOActivity extends AppCompatActivity {
    String TAG="EXOActivity";
    private LYExoVideoView ly_exo_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_exo);
        ly_exo_video = findViewById(R.id.ly_exo_video);
        ly_exo_video.requestFocus();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        VideoInfo info=new VideoInfo();
        info.playkey="标清";
        // info.keys.add("标清");
        // info.keys.add("高清");
        info.resolutions.put("标清","http://192.168.0.121:2100/video/20190405/8f4DyBLz/hls/index.m3u8");
        info.resolutions.put("高清","http://192.168.0.121:2100/video/20190405/8f4DyBLz/hls/index.m3u8");
        info.setImgAD("http://192.168.0.121:2100/pic/5.jpg");
        info.img_ad_time=6;
        info.imgCover="http://192.168.0.121:2100/pic/3.jpg";
        info.setVideo_AD("http://192.168.0.121:2100/short/4.mp4");
        ly_exo_video.setVideoUri(info);
        ly_exo_video.startADVideo();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "onKeyDown: "+keyCode );
        if (keyCode==KeyEvent.KEYCODE_BACK){
            if (LYEXOPlayerManger.init().onBack()){
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
