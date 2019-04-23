package com.example.shy.exoplayer;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/**
 * https://google.github.io/ExoPlayer/guide.html
 * exoplayer  生成唯一 的单例模式，全局只有一个播放器。
 */
public class LYEXOPlayerManger  {
    int postion=-1;

    public void setPostion(int postion) {
        this.postion = postion;
    }

    LYLeftlCycle leftlCycle;
    private SimpleExoPlayer player;
    boolean full=false;

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    private LYEXOPlayerManger() {
    }
  private   static LYEXOPlayerManger manger;
    public static LYEXOPlayerManger init(){
        if (manger==null){
            manger=new LYEXOPlayerManger();
        }
        return manger;
    }



    /**
     *
     * @param context
     * @param url
     * @return
     */
    public SimpleExoPlayer getPlayer(Context context,String url,LYLeftlCycle leftlCycle,boolean looper){
        clearPlayer();
        this.leftlCycle=leftlCycle;
        DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
        String userAgent = Util.getUserAgent(context, "ExoPlayerDemo");
        DefaultDataSourceFactory mediaDataSourceFactory = new DefaultDataSourceFactory(context, BANDWIDTH_METER,
                new DefaultHttpDataSourceFactory(userAgent, BANDWIDTH_METER));

        Uri uri = Uri.parse(url);
        MediaSource mediaSource ;
        if (url.endsWith(".m3u8")){
            mediaSource = new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
        }else {
            //这个处理MP4
            mediaSource=new ExtractorMediaSource .Factory(mediaDataSourceFactory).createMediaSource(uri);
        }
        player = ExoPlayerFactory.newSimpleInstance(context);
        // player.addListener(this);
        player.setPlayWhenReady(true);
        if(mediaSource!=null) {
            //循环播放设置
            if (looper){
                //Player.REPEAT_MODE_ALL 重复模式
                //Player.REPEAT_MODE_OFF 正常播放，无重复
                //Player.REPEAT_MODE_ONE 只是当前窗口无限播放
                player.setRepeatMode(Player.REPEAT_MODE_ONE);//设置为重复模式
            }else {
                player.setRepeatMode(Player.REPEAT_MODE_OFF);//正常播放
            }
            player.prepare(mediaSource);


        }
        return player;
    }

    /**
     * 清空播放器
     */
    public void clearPlayer() {
        full=false;
        postion=-1;
        if (player!=null){
            player.release();
        }
        if (leftlCycle!=null){
            leftlCycle.onRestore();
            leftlCycle=null;
        }
    }

    /**
     * 获取当前视频总时长
     * @return
     */
    public long getDuration(){
        if (player!=null){
            return player.getDuration();
        }
        return 0;
    }

    /**
     * 获取播放的时间长度
     * @return
     */
    public long getCurrentPosition(){
        if (player!=null){
            return player.getCurrentPosition();
        }
        return 0;
    }

    /**
     *
     * @return
     */
    public long getContentPosition(){
        if (player!=null){
            return player.getContentPosition();
        }
        return 0;
    }

    /**
     * 获取播放状态
     * @return
     */
    public int getPlaybackState(){
        if (player!=null){
            return player.getPlaybackState();
        }
        return -1;
    }

    /**
     * 获取是否处于播放状态
     * @return
     */
    public boolean getPlayWhenReady(){
        if (player!=null){
              return player != null
                    && player.getPlaybackState() != Player.STATE_ENDED
                    && player.getPlaybackState() != Player.STATE_IDLE
                    && player.getPlayWhenReady();
        }
        return false;
    }

    /**
     * 暂停
     */
    public void  pause(){
        if (player!=null){
            player.setPlayWhenReady(false);
        }
    }

    /**
     * 播放
     */
    public void start(){
        if (player!=null){
            player.setPlayWhenReady(true);
        }
    }
    public void setPlaybackSpeed(float speed){
        if (player!=null){
            PlaybackParameters param = new PlaybackParameters(speed);
            player.setPlaybackParameters(param);
        }
    }
    public void seekTo(long seek){
        if (player!=null){
            player.seekTo(seek);
        }
    }
    public long getTotalBufferedDuration(){
        if (player!=null){
           return player.getTotalBufferedDuration();
        }
        return 1;
    }
    public long getBufferedPosition(){
        if (player!=null){
            return player.getBufferedPosition();
        }
        return 1;
    }

    /**
     * activity 的返回键监听
     */
    public boolean onBack(){
        if (player!=null&&leftlCycle!=null){
            leftlCycle.onActivityBack();
            return true;
        }
        return false;
    }
    public boolean onFragmentBack(){
        if (player!=null&&leftlCycle!=null){
            leftlCycle.onFragmentBack();
            return true;
        }
        return false;
    }

    public void setRecyleView(RecyclerView recyleView){
        LinearLayoutManager manager= (LinearLayoutManager) recyleView.getLayoutManager();
        recyleView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                int first_postion = manager.findFirstVisibleItemPosition();
                int last_postion = manager.findLastVisibleItemPosition();

                if (full){

                }else {
                    if (postion<first_postion||postion>last_postion){
                        clearPlayer();
                    }
                }
            }
        });
    }
}
