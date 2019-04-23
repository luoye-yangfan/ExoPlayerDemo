package com.example.shy.exoplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class VideoInfo {
    public  String playkey;
    public  HashMap<String,String> resolutions=new HashMap<>();
    private ArrayList<String> keys=new ArrayList<>();
    public  String title="";
    public String imgAD="";//图片广告
    public boolean IMAGE_AD=false;
    public boolean VIDEO_AD=false;//视频广告
    public String  video_AD="";
    public String imgCover="https://www.baidu.com";
    public int img_ad_time=6;
    public int SCREEN_TYPE= LYExoVideoView.SCREEN_NORMAL;//正常屏幕状态
    public void setImgAD(String imgAD) {
        this.imgAD = imgAD;
        IMAGE_AD=true;
    }
    public void setVideo_AD(String video_ad){
        this.video_AD=video_ad;
        VIDEO_AD=true;
    }

    public ArrayList<String> getKeys() {
        Set<String> strings = resolutions.keySet();
        keys.clear();
        for (String key:strings){
            keys.add(key);
        }
        return keys;
    }

    public void setImgAdTime(String imgadtime) {
       try {
           this.img_ad_time= Integer.valueOf(imgadtime);
       }catch (Exception e){

       }
    }
}
