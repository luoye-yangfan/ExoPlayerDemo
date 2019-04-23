package com.example.shy.exoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.spherical.SingleTapListener;
import com.google.android.exoplayer2.ui.spherical.SphericalSurfaceView;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.video.VideoListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;


/**
 * 落叶 基于vitamio 播放器
 */
public class LYExoVideoView extends FrameLayout implements LYLeftlCycle, AdapterView.OnItemClickListener, View.OnClickListener, View.OnTouchListener, RadioGroup.OnCheckedChangeListener, VideoListener {
    static final String TAG="LYVideoView";
    //todo  网络显示放大器
    public static int NET_MAGNIFICATION=20;
    // TODO full screen 全屏屏幕状态常亮
    public static final int SCREEN_FULL=1;//全屏
    public static final int SCREEN_NORMAL=2;//正常屏幕。
    public static final int SCREEN_LIST=3;//列表屏幕。
    public static final int SCREEN_SMALL=4;//小屏幕播放。暂不处理、
    //todo  重要马甲分割线-----------------------------变量
    public int SCREEN_TYPE=SCREEN_NORMAL;//设置屏幕状态为默认状态。
    public int START_SCREEN_TYPE=SCREEN_NORMAL;//初始屏幕状态
    private RelativeLayout re_video;
    private FrameLayout frame_video;//用于存储播放器相关的。
    private ImageView imgCover;
    private TextView t_ad_time;
    private LinearLayout line_top;
    private ProgressBar pr_loading;
    private TextView t_percentage;
    private TextView t_network;
    private ImageView img_back;
    private TextView t_title;
    private LinearLayout l_controller;
    private TextView t_dissmiss;
    private GridView grid_sharpness;
    private RadioGroup radio_type;
    private SeekBar seek_sound;
    private SeekBar seek_brightness;
    private ImageView img_start;
    private LinearLayout lin_center_pro;
    private TextView t_progress;
    private ProgressBar progress_bar;
    private LinearLayout l_bottom_controller;
    private ImageView mediacontroller_play_pause;
    private TextView mediacontroller_time_current;
    private SeekBar mediacontroller_seekbar;
    private TextView mediacontroller_time_total;
    private ImageView img_move;
    private ImageView img_full;
    private TextView t_speek_1;
    private TextView t_speek_2;
    private TextView t_speek_3;
    private TextView t_speek_4;
    Activity activity;
    Handler handler=new Handler();//主线程hander
    private float rawX;
    private float rawY;
    private boolean left;
    int horizontal=-1;
    float event_x=-1;
    private TextView t_config;
    long modify_time=0;
    boolean ismove=false;
    private float event_y;
    int magnification=15;
    private int interval_move=50;
    private int boottom_move=50;
    Window window;
    private AudioManager mAudioManager;
    float nowVolunme=-1;
    InterceptCallBack interceptCallBack;
    VideoInfo info=new VideoInfo();//视频信息存储对象
    private ResolutionAdapter adapter;
    boolean isVideo=false;
    private LinearLayout l_laoding;
    private TextureView surfaceView;
    private SimpleExoPlayer player;
    private ComponentListener componentListener;
    private int textureViewRotation;
    int videoWidth=0;
    int videoHeight=0;
    private KeyEvent event;

    public boolean isVideo() {
        return isVideo;
    }

    public void setInterceptCallBack(InterceptCallBack interceptCallBack) {
        this.interceptCallBack = interceptCallBack;
    }

    public LYExoVideoView(Context context) {
        super(context);
        initLayout();
    }

    public LYExoVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public LYExoVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout();
    }
    //todo 重要马甲分割线， 这是初始化区域。-----------------------------------------------------------------------------------------------
    private void initLayout() {
        removeAllViews();
        //设置 子视图获取焦点的能力
        // FOCUS_BEFORE_DESCENDANTS
        //参数该表明ViewGroup自身先处理焦点，如果没有处理则分发给子视图进行处理。
        //FOCUS_AFTER_DESCENDANTS
        //该参数表明子查看优先处理焦点，如果所有的子视图都没有处理，则ViewGroup自身再处理。
        //FOCUS_BLOCK_DESCENDANTS
        //参数该表明   ViewGroup自身处理焦点，不会分发给子视图处理。
       setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        //设置背景颜色。
        setBackgroundColor(Color.parseColor("#ff0099"));
        View.inflate(getContext(),getLayoutId(),this);
        initControllerViews();
        componentListener = new ComponentListener();
    }



    /**
     * 初始化控制的所有view
     */
    private void initControllerViews() {
        //将view  从xml 中转成对象
        interval_move = dp2px( 35);
        boottom_move = dp2px( 55);
        re_video = findViewById(R.id.re_video);
        frame_video = findViewById(R.id.frame_video);
        addVideoView();
        imgCover = findViewById(R.id.imgCover);
        t_ad_time = findViewById(R.id.t_ad_time);
        line_top = findViewById(R.id.line_top);
        pr_loading = findViewById(R.id.pr_loading);
        t_percentage = findViewById(R.id.t_percentage);
        t_network = findViewById(R.id.t_network);
        img_back = findViewById(R.id.img_back);
        t_title = findViewById(R.id.t_title);
        l_controller = findViewById(R.id.l_controller);
        t_dissmiss = findViewById(R.id.t_dissmiss);
        grid_sharpness = findViewById(R.id.grid_sharpness);
        radio_type = findViewById(R.id.radio_type);//屏幕模式
        seek_sound = findViewById(R.id.seek_sound);
        seek_brightness = findViewById(R.id.seek_brightness);
        img_start = findViewById(R.id.img_start);
        lin_center_pro = findViewById(R.id.lin_center_pro);
        t_progress = findViewById(R.id.t_progress);
        progress_bar = findViewById(R.id.progress_bar);
        l_bottom_controller = findViewById(R.id.l_bottom_controller);
        mediacontroller_play_pause = findViewById(R.id.mediacontroller_play_pause);
        mediacontroller_time_current = findViewById(R.id.mediacontroller_time_current);
        mediacontroller_seekbar = findViewById(R.id.mediacontroller_seekbar);
        mediacontroller_time_total = findViewById(R.id.mediacontroller_time_total);
        l_laoding = findViewById(R.id.l_laoding);
        img_move = findViewById(R.id.img_move);
        img_full = findViewById(R.id.img_full);
        t_config = findViewById(R.id.t_config);
        t_speek_1 = findViewById(R.id.t_speek_1);
        t_speek_2 = findViewById(R.id.t_speek_2);
        t_speek_3 = findViewById(R.id.t_speek_3);
        t_speek_4 = findViewById(R.id.t_speek_4);
        //todo  在这个地方判断
        //设置view的点击事件和触摸事件
        re_video.setOnClickListener(this);
        img_back.setOnClickListener(this);
        t_dissmiss.setOnClickListener(this);
        //倍数播放的点击事件
        t_speek_1.setOnClickListener(this);
        t_speek_2.setOnClickListener(this);
        t_speek_3.setOnClickListener(this);
        t_speek_4.setOnClickListener(this);
        img_start.setOnClickListener(this);
        mediacontroller_play_pause.setOnClickListener(this);
        img_move.setOnClickListener(this);
        img_full.setOnClickListener(this);
        radio_type.setOnCheckedChangeListener(this);
        grid_sharpness.setOnItemClickListener(this);
        re_video.setOnTouchListener(this);
        mediacontroller_seekbar.setOnSeekBarChangeListener(mediacontrollerSeekbar);
        seek_brightness.setOnSeekBarChangeListener(seekBrightness);
        seek_sound.setOnSeekBarChangeListener(seekSound);
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        imgCover.setOnClickListener(this);

    }

    SeekBar.OnSeekBarChangeListener seekSound=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateVolumeProgress(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    /**
     * 亮度拖动条相关事件
     */
    SeekBar.OnSeekBarChangeListener seekBrightness=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateBrightProgress(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    /**
     * 底部拖动条的拖动事件
     */
    SeekBar.OnSeekBarChangeListener mediacontrollerSeekbar=new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            LYEXOPlayerManger.init().pause();
            handler.removeCallbacks(updataController);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser)
                return;
            long newposition = (LYEXOPlayerManger.init().getDuration() * progress) / 1000;
            String time = StringUtils.generateTime(newposition);
            LYEXOPlayerManger.init().seekTo(newposition);
            if (mediacontroller_time_current != null)
                mediacontroller_time_current.setText(time);
        }

        public void onStopTrackingTouch(SeekBar bar) {
            pr_loading.setVisibility(View.VISIBLE);
            t_network.setVisibility(View.VISIBLE);
            t_percentage.setVisibility(View.VISIBLE);
            handler.postDelayed(updataController,UpdataController);
            LYEXOPlayerManger.init().start();
        }
    };

    /**
     * 全局点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.img_start) {
        //开始播放的点击事件
            Log.e(TAG, "onClick: img_start" );
            if (interceptCallBack!=null){
                interceptCallBack.onStartClick();
            }else {
                startVideo();
            }

        }else if (i==R.id.re_video){
            //TODO 定时5秒 关闭控制层
           showAndDissMissController();
            Log.e(TAG, "onClick: re_video" );
        }else if (i==R.id.img_full){
            //控制屏幕横竖屏切换。
            if (SCREEN_TYPE==SCREEN_FULL){
                Log.e(TAG, "现在是全屏改成小屏" );
                onBack();

            }else {
                Log.e(TAG, "现在是小屏改成全屏" );
                onFull();
            }
        }else if (i==R.id.img_move){
            Log.e(TAG, "onClick: img_move" );
            //控制操作层显示
            if (info!=null&&info.VIDEO_AD==false&&info.IMAGE_AD==false){
                l_controller.setVisibility(VISIBLE);
            }

        }else if (i==R.id.t_dissmiss){
            Log.e(TAG, "onClick: t_dissmiss" );
            l_controller.setVisibility(GONE);
        }else if (i==R.id.mediacontroller_play_pause){
            Log.e(TAG, "onClick: mediacontroller_play_pause" );
            //暂停或者播放
            if (LYEXOPlayerManger.init().getPlayWhenReady()){
                LYEXOPlayerManger.init().pause();
                mediacontroller_play_pause.setImageResource(getResources().getIdentifier("mediacontroller_play", "drawable", getContext().getPackageName()));
            }else {
                LYEXOPlayerManger.init().start();
                mediacontroller_play_pause.setImageResource(getResources().getIdentifier("mediacontroller_pause", "drawable", getContext().getPackageName()));

            }
        }else if (i==R.id.t_speek_1){
            t_speek_1.setTextColor(Color.parseColor("#ea8b3c"));
            t_speek_2.setTextColor(Color.parseColor("#ffffff"));
            t_speek_3.setTextColor(Color.parseColor("#ffffff"));
            t_speek_4.setTextColor(Color.parseColor("#ffffff"));
            LYEXOPlayerManger.init().setPlaybackSpeed(1.0f);
        }else if (i==R.id.t_speek_2){
            t_speek_2.setTextColor(Color.parseColor("#ea8b3c"));
            t_speek_1.setTextColor(Color.parseColor("#ffffff"));
            t_speek_3.setTextColor(Color.parseColor("#ffffff"));
            t_speek_4.setTextColor(Color.parseColor("#ffffff"));
            LYEXOPlayerManger.init().setPlaybackSpeed(1.25f);
        }else if (i==R.id.t_speek_3){
            t_speek_3.setTextColor(Color.parseColor("#ea8b3c"));
            t_speek_1.setTextColor(Color.parseColor("#ffffff"));
            t_speek_2.setTextColor(Color.parseColor("#ffffff"));
            t_speek_4.setTextColor(Color.parseColor("#ffffff"));
            LYEXOPlayerManger.init().setPlaybackSpeed(1.5f);
        }else if (i==R.id.t_speek_4){
            t_speek_4.setTextColor(Color.parseColor("#ea8b3c"));
            t_speek_2.setTextColor(Color.parseColor("#ffffff"));
            t_speek_3.setTextColor(Color.parseColor("#ffffff"));
            t_speek_1.setTextColor(Color.parseColor("#ffffff"));
            LYEXOPlayerManger.init().setPlaybackSpeed(2f);
        }else if (i==R.id.img_back){
            Log.e(TAG, "onClick:img_back " );
            if (START_SCREEN_TYPE==SCREEN_FULL){
                handler.removeCallbacks(imgAd);
                LYEXOPlayerManger.init().clearPlayer();
                activity= (Activity) getContext();
                activity.finish();
                return;
            }
            if (SCREEN_TYPE==SCREEN_FULL){
                onBack();
            }else {
                LYEXOPlayerManger.init().clearPlayer();
                activity= (Activity) getContext();
                activity.finish();
            }
        }else if (i==R.id.imgCover){
            Log.e(TAG, "onClick: img_back" );
            if (info!=null&&info.IMAGE_AD){
                if (adCallBack!=null){
                    adCallBack.onADCallBack();
                }
            }
        }
    }



    /**
     * gridview 的item的点击事件
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (info.resolutions.containsKey(info.getKeys().get(position))){
            //如果包含
            adapter.setSelect_key(position);
            info.playkey=info.getKeys().get(position);
            startVideo();
        }
    }

    /**
     * 全局触摸事件
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId()!=R.id.re_video){
            return false;
        }
        switch (event.getAction()){
            //单指
            case MotionEvent.ACTION_DOWN:
                rawX = event.getRawX();
                rawY = event.getRawY();
                //触碰区域。
                left = false;
                if (rawX <=getWidth()/2){
                    left =true;
                }
                LYEXOPlayerManger.init().pause();
                handler.removeCallbacks(updataController);
                Log.e(TAG, "setFullProbar 手指按下"+ rawX +"--------------------"+ rawY);
                //手指 初次接触到屏幕 时触发
                if (info!=null&&info.IMAGE_AD==false&&info.VIDEO_AD==false&&isVideo){
                    l_bottom_controller.setVisibility(VISIBLE);
                    if (SCREEN_TYPE!=SCREEN_LIST){
                        line_top.setVisibility(VISIBLE);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "手指移动: " );
                setFullProbar(event);
                //手指 在屏幕上滑动 时触发，会多次触发。
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onTouchEvent: 手指抬起" );
                t_config.setVisibility(GONE);
                t_progress.setVisibility(GONE);
                progress_bar.setVisibility(GONE);
                //2019年4月1日18:35:06  重写 滑动改变事件
                if (horizontal==0){
                    LYEXOPlayerManger.init().seekTo(modify_time);
                }
                LYEXOPlayerManger.init().start();
                ismove=false;
                handler.postDelayed(updataController,UpdataController);
                showAndDissMissController();
                //手指 离开屏幕 时触发。
                event_x=-1;
                event_y = -1;
                horizontal=-1;
                break;
            case MotionEvent.ACTION_CANCEL:
                //事件 被上层拦截 时触发。
                break;
            case MotionEvent.ACTION_OUTSIDE:
                //手指 不在控件区域 时触发。
                break;
            //多指
            case MotionEvent.ACTION_POINTER_DOWN:
                //有非主要的手指按下(即按下之前已经有手指在屏幕上)。
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //有非主要的手指抬起(即抬起之后仍然有手指在屏幕上)。
                break;
        }
        return true;
    }


    /**
     * 单选点击事件
     * todo  暂未实现
     * @param group
     * @param checkedId
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId==R.id.radio_1){
           // LYVPMannger.getInstance().setVideoLayout(0,0);
        }else if (checkedId==R.id.radio_2){
           // LYVPMannger.getInstance().setVideoLayout(1,0);
        }else if (checkedId==R.id.radio_3){
           // LYVPMannger.getInstance().setVideoLayout(2,0);
        }else {
           // LYVPMannger.getInstance().setVideoLayout(3,0);
        }
    }






    /**
     *
     * @return
     */
    private int getLayoutId() {
        return R.layout.ly_video_exo_floor;
    }
    //todo 重要马甲分割线， 这是生命周期 回调处理。-----------------------------------------------------------------------------------------------
    @Override
    public void onError(int what, int extra) {
        startVideo();
        showDialog("哎呀，播放发生错误了，要不刷新一下或者重新打开APP试试？");
    }

    @Override
    public void onStart() {
        imgCover.setVisibility(GONE);
        onPrepared();
    }

    @Override
    public void onInfo(int what, int extra) {
        //lyexo 没得这个调调
    }

    @Override
    public void onBuffer(int percent) {
        //缓冲进度
      //  Log.e(TAG, "onBuffer: "+percent );
        t_percentage.setText(percent + "%");
    }

    @Override
    public void onCompletion() {
        //播放完成
        if (info.VIDEO_AD){
            info.VIDEO_AD=false;
        }
        startVideo();
    }

    @Override
    public void onPrepared() {
        //准备完成
       updataBottomInfo();
       isVideo=true;
       setTag(TAG);
        img_start.setVisibility(GONE);
    }

    @Override
    public void onSeekComplete() {
        //todo 拖动完成
        Log.e(TAG, "onSeekComplete: " );
    }


    @Override
    public void onRestore() {
        //todo 恢复
        isVideo=false;
        if (getLocalVisibleRect(new Rect())){
            imgCover.setVisibility(VISIBLE);
        }
        img_start.setVisibility(VISIBLE);
        l_bottom_controller.setVisibility(GONE);
        clearALlPost();
    }

    @Override
    public void onActivityBack() {
        if (SCREEN_TYPE==SCREEN_FULL){
            onBack();
        }else {
            handler.removeCallbacks(imgAd);
            LYEXOPlayerManger.init().clearPlayer();
             activity= (Activity) getContext();
             activity.finish();
        }
    }

    @Override
    public void onFragmentBack() {
        if (SCREEN_TYPE==SCREEN_FULL){
            onBack();
        }
    }


    //todo 重要马甲分割线， 这是其他方法区域。-----------------------------------------------------------------------------------------------

    /**
     * 开始播放
     */
    public void startVideo() {
        if (info==null){
            showDialog("播放地址无效，请刷新后重试");
            return;
        }
        info.IMAGE_AD=false;
        info.VIDEO_AD=false;
        Log.e(TAG, "startVideo: " );
        img_start.setVisibility(GONE);
        imgCover.setVisibility(GONE);
        t_ad_time.setVisibility(GONE);
        setPlayer(LYEXOPlayerManger.init().getPlayer(getContext(),info.resolutions.get(info.playkey),this,true));
        isVideo=true;
    }

    private void addVideoView() {
        frame_video.removeAllViews();
        surfaceView = new TextureView(getContext());
        frame_video.addView(surfaceView,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * 播放视频广告
     */
    private void startVideoADVideo(){
        if (info==null){
            showDialog("播放地址无效，请刷新后重试");
            return;
        }
        img_start.setVisibility(GONE);
        imgCover.setVisibility(GONE);
        setPlayer(LYEXOPlayerManger.init().getPlayer(getContext(),info.video_AD,this,false));
        //frame_video.addView(LYVPMannger.getInstance().getVideoPlayer(getContext(),this,info.video_AD),new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }
    /**
     * 播放带有广告的视频、
     */
    public void startADVideo(){
        img_start.setVisibility(GONE);
        imgCover.setVisibility(VISIBLE);
        //LYVPMannger.getInstance().clearVideo();
        if (info==null){
            showDialog("播放地址无效，请刷新后重试");
            return;
        }
        if (info.IMAGE_AD){
            showImageAd();
            //表示这个地方需要处理 图片广告
            return;
        }
        if (info.VIDEO_AD){
            //表示这个地方需要处理视频广告。
            startVideoADVideo();
            return;
        }
        //播放正文视频
        startVideo();
    }

    /**
     * 设置 视频信息
     * @param info
     */
    public void setVideoUri(VideoInfo info){
        this.info=info;
        bindInfo();
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    public void setPlayer(@Nullable SimpleExoPlayer player) {
        Assertions.checkState(Looper.myLooper() == Looper.getMainLooper());
        Assertions.checkArgument(
                player == null || player.getApplicationLooper() == Looper.getMainLooper());
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(componentListener);
            this.player.removeAnalyticsListener(analyticsListener);
            Player.VideoComponent oldVideoComponent = this.player.getVideoComponent();
            if (oldVideoComponent != null) {
                oldVideoComponent.removeVideoListener(componentListener);
                oldVideoComponent.clearVideoTextureView(surfaceView);
            }
            Player.TextComponent oldTextComponent = this.player.getTextComponent();
            if (oldTextComponent != null) {
                oldTextComponent.removeTextOutput(componentListener);
            }
        }
        this.player = player;
        if (player != null) {
            Player.VideoComponent newVideoComponent = player.getVideoComponent();
            if (newVideoComponent != null) {
                newVideoComponent.setVideoTextureView( surfaceView);
                newVideoComponent.addVideoListener(componentListener);
            }
            Player.TextComponent newTextComponent = player.getTextComponent();
            if (newTextComponent != null) {
                newTextComponent.addTextOutput(componentListener);
            }
            player.addListener(componentListener);
            player.addAnalyticsListener(analyticsListener);//添加分析记录器
        } else {

        }
        isVideo=true;
    }

    /**
     * 移除所有 hander  postion 相关的线程
     */
    private void clearALlPost() {
        handler.removeCallbacks(updataController);
    }
    /**
     * 更新底部的控制层。
     */
    private void updataBottomInfo() {
        upController();
        handler.removeCallbacks(updataController);//移除之前未完成的run。
        handler.post(updataController);
        showAndDissMissController();
    }
    //todo  重要马甲分割线。通过hander post  刷新UI.---------------------------------------------------------------------------------------------------------


    //TODO  5秒后关闭底部控制栏
    long DissMIssBootomController=5000;
    Runnable dissMIssBootomController=new Runnable() {
        @Override
        public void run() {
            line_top.setVisibility(GONE);
            if (l_bottom_controller.getVisibility()==VISIBLE){
                l_bottom_controller.setVisibility(GONE);
            }
        }
    };

    //todo  每隔 0.5秒刷新一次控制栏数据
    long UpdataController=500;
    Runnable updataController=new Runnable() {
        @Override
        public void run() {
            //逻辑，只有底部控制层处于显示状态的时候才会更新信息。同时处于播放 时候才更新数据。
          if (l_bottom_controller.getVisibility()==VISIBLE){
              if (LYEXOPlayerManger.init().getPlayWhenReady()){
                  upController();
              }
          }
          if (LYEXOPlayerManger.init().getPlaybackState()==Player.STATE_ENDED){
              //播放结束
              startVideo();
          }
          handler.postDelayed(updataController,UpdataController);
        }
    };

    //todo  UP 更新区域--------------------------------------------------------------------------------------------------------------------------

    /**
     * 绑定 info 传递过来的数据。
     */
    private void bindInfo() {
        Picasso.get().load(info.imgCover).into(imgCover);
        START_SCREEN_TYPE=info.SCREEN_TYPE;
        SCREEN_TYPE=info.SCREEN_TYPE;
        if (SCREEN_TYPE==SCREEN_LIST){
            line_top.setVisibility(GONE);
        }else {
            line_top.setVisibility(VISIBLE);
            img_back.setVisibility(VISIBLE);
            t_title.setText(info.title);
        }
        //设置清晰度
        adapter = new ResolutionAdapter(getContext(), info.getKeys(), info.playkey);
        grid_sharpness.setAdapter(adapter);
    }

    /**
     *更新UI
     */
    private void upController() {
        long position = LYEXOPlayerManger.init().getCurrentPosition();
        long duration = LYEXOPlayerManger.init().getDuration();
        Log.e(TAG, "upController: "+position+"-------------------"+duration );
        if (mediacontroller_seekbar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mediacontroller_seekbar.setProgress((int) pos);
            }
        }

        if (mediacontroller_time_total != null){
                mediacontroller_time_total.setText(StringUtils.generateTime(duration));}
        if (mediacontroller_time_current != null){
            mediacontroller_time_current.setText(StringUtils.generateTime(position));}
        if (mediacontroller_play_pause != null) {
            if (LYEXOPlayerManger.init().getPlayWhenReady()) {
                mediacontroller_play_pause.setImageResource(getResources().getIdentifier("mediacontroller_pause", "drawable", getContext().getPackageName()));
            } else {
                mediacontroller_play_pause.setImageResource(getResources().getIdentifier("mediacontroller_play", "drawable", getContext().getPackageName()));
            }

        }
    }
    /**
     * 先显示 后隐藏控制层
     */
    private void showAndDissMissController() {
        if (info!=null&&info.VIDEO_AD==false&&info.IMAGE_AD==false&&isVideo){
            handler.removeCallbacks(dissMIssBootomController);
            l_bottom_controller.setVisibility(VISIBLE);
            handler.postDelayed(dissMIssBootomController,DissMIssBootomController);
            if (SCREEN_TYPE!=SCREEN_LIST){
                line_top.setVisibility(VISIBLE);
            }
        }

    }

    /**
     * 全屏改成小屏
     */
    private void onBack() {
        LYEXOPlayerManger.init().setFull(false);
        //LYVPMannger.getInstance().setFull(false);
        Activity activity= (Activity) getContext();
        SCREEN_TYPE=START_SCREEN_TYPE;
        //ActivityUtlis.showNavigationBar(activity);
        img_full.setImageResource(R.drawable.ic_full);
        //videoView.pause();
        img_start.setVisibility(GONE);
        imgCover.setVisibility(GONE);
        l_controller.setVisibility(GONE);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (START_SCREEN_TYPE==SCREEN_LIST){
            line_top.setVisibility(GONE);

        }else {
            img_back.setVisibility(VISIBLE);
        }
        ViewGroup view = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        view.removeView(re_video);
        this.removeAllViews();
        this.addView(re_video,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        //LYVPMannger.getInstance().setVideoLayout(3,0);
       // LYVPMannger.getInstance().start();
        img_move.setVisibility(GONE);
    }

    /**
     * 小屏转大屏
     */
    private void onFull(){
        LYEXOPlayerManger.init().setFull(true);
        //LYVPMannger.getInstance().setFull(true);
        Activity activity= (Activity) getContext();
      //  ActivityUtlis.hideSupportActionBar(activity);
        SCREEN_TYPE=SCREEN_FULL;
        img_back.setVisibility(VISIBLE);
        l_controller.setVisibility(GONE);
        line_top.setVisibility(VISIBLE);
        if (START_SCREEN_TYPE==SCREEN_LIST){
            t_title.setVisibility(GONE);
        }
        img_start.setVisibility(GONE);
        imgCover.setVisibility(GONE);
        img_full.setImageResource(R.drawable.ic_dissmiss_full);
       // LYVPMannger.getInstance().setVideoLayout(3,0);
        img_move.setVisibility(VISIBLE);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ViewGroup view = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        this.removeAllViews();
        view.removeView(re_video);
        view.addView(re_video,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
      // LYVPMannger.getInstance().start();
    }

    /**
     * 格式化 播放器样式
     */
    private void layoutFormart() {
        if (surfaceView==null||videoHeight==0||videoWidth==0){
            return;
        }
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        int windowWidth = dm.widthPixels;
        int windowHeight = dm.heightPixels;
        float x=(float) videoWidth/(float) videoHeight;
        //todo  以高为基准，绘制屏幕画布
        if (windowWidth<windowHeight){
            //竖屏
            //android.util.Log.e(TAG, "setVideoLayout: 竖屏"+windowWidth+"-------"+windowHeight+"------" );

            FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(windowWidth, (int) (windowWidth/x));
            params.gravity= Gravity.CENTER;
            surfaceView.setLayoutParams(params);
        }else {
            //横屏
            //  android.util.Log.e(TAG, "setVideoLayout: 横屏"+windowWidth+"-------"+windowHeight+"-------");
            FrameLayout.LayoutParams params=new FrameLayout.LayoutParams((int) (windowHeight*x),windowHeight);
            params.gravity= Gravity.CENTER;
            surfaceView.setLayoutParams(params);

        }
    }

    /**
     * 显示loading层
     */
    public void showLoading(){
        l_laoding.setVisibility(VISIBLE);
        pr_loading.setVisibility(View.VISIBLE);
        // t_network.setText("正在缓冲中，请稍等···");
        t_network.setVisibility(View.VISIBLE);
        t_percentage.setText("");
        t_percentage.setVisibility(GONE);
    }

    /**
     * 显示缓冲
     */
    public void dissmissLoading(){
        pr_loading.setVisibility(View.GONE);
        // t_network.setText("正在缓冲中，请稍等···");
        t_network.setVisibility(View.GONE);
        t_percentage.setText("");
        t_percentage.setVisibility(GONE);
    }

    /**
     * 对全屏状态下 触摸事件进行分发
     * @param event
     */
    private void setFullProbar(MotionEvent event) {
        if (SCREEN_TYPE!=1){
            //非全屏状态
            return;
        }
        if (horizontal==-1){
            //todo  凼且仅当 未移动坐标的时候 赋值为-1.
            if (Math.abs(rawX-event.getRawX())==0&&Math.abs(rawY-event.getRawY())==0){
                Log.e(TAG, "setFullProbar: -1" );
                horizontal=-1;
            }else if (Math.abs(rawX-event.getRawX())>Math.abs(rawY-event.getRawY())){
                horizontal=0;
                Log.e(TAG, "setFullProbar: 0" );
                lin_center_pro.setVisibility(VISIBLE);
                t_progress.setVisibility(VISIBLE);
                progress_bar.setVisibility(VISIBLE);
              /*  t_progress.setText(StringUtils.generateTime(LYVPMannger.getInstance().getCurrentPosition())+"");
                progress_bar.setProgress((int) (LYVPMannger.getInstance().getCurrentPosition()*100/LYVPMannger.getInstance().getDuration()));
            */}else {
                Log.e(TAG, "setFullProbar: 1" );
                // Log.e(TAG, "setFullProbar: ==1" );
                horizontal=1;
            }
        }
        if (horizontal==0){
            //表示是左右滑动
                float n_x= event.getRawX()-rawX;
                float width = getWidth()*magnification;
                float v_p= (n_x/width)*LYEXOPlayerManger.init().getDuration();
                  Log.e(TAG, "setFullProbar: "+n_x+"////////////////////////"+v_p );
                if (n_x<0.0){
                    ismove=true;
                    modify_time = (long) (LYEXOPlayerManger.init().getCurrentPosition()+v_p);
                    if (modify_time<=0){
                        modify_time=0;
                    }
                    t_progress.setText(StringUtils.generateTime(modify_time)+"");
                    progress_bar.setProgress((int) (modify_time *100/LYEXOPlayerManger.init().getDuration()));
                }else if (n_x>0.0){
                    ismove=true;
                    if (modify_time>=LYEXOPlayerManger.init().getDuration()){
                        modify_time=LYEXOPlayerManger.init().getDuration();
                    }
                    modify_time = (long) (LYEXOPlayerManger.init().getCurrentPosition()+v_p);
                    t_progress.setText(StringUtils.generateTime(modify_time)+"");
                    progress_bar.setProgress((int) (modify_time *100/LYEXOPlayerManger.init().getDuration()));
                }else {
                    ismove=false;
                }
        }else if (horizontal==1){
            if (event_y==-1){
                event_y=event.getRawY();
            }
            if (Math.abs(event_y-event.getRawY())<interval_move){
                return;
            }
            //表示是上下滑动
            t_config.setVisibility(VISIBLE);
            if (left){
                //TODO 调整亮度

                if (activity==null){
                    activity = (Activity) getContext();
                }

                if (window==null){
                    window = activity.getWindow();
                }
                WindowManager.LayoutParams params = window.getAttributes();
                if (event_y-event.getRawY()>0){
                    params.screenBrightness=params.screenBrightness+0.05f;
                }else {
                    params.screenBrightness=params.screenBrightness-0.05f;
                }

                if (params.screenBrightness > 1.0f) {
                    params.screenBrightness = 1.0f;
                }
                if (params.screenBrightness <= 0.01f) {
                    params.screenBrightness = 0.01f;
                }
                window.setAttributes(params);
                int pro= (int) (params.screenBrightness*100);
                t_config.setText("亮度："+pro+"%");
            }else {
                //todo 调整声音 如果移动区域小于 35就不那个啥了。
                if (Math.abs(event_y-event.getRawY())<interval_move){
                    return;
                }
                if (mAudioManager != null) {
                    float maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                    if (nowVolunme==-1){
                        nowVolunme= mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    }
                    if (event_y-event.getRawY()>0){
                        nowVolunme=nowVolunme+0.5f;
                    }else {
                        nowVolunme=nowVolunme-0.5f;
                    }
                    if (nowVolunme>maxVolume){
                        nowVolunme=maxVolume;
                    }
                    if (nowVolunme<0){
                        nowVolunme=0;
                    }
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) nowVolunme, 0);
                    t_config.setText("声音："+nowVolunme);
                }
            }
        }
    }

    /**
     * 显示图片广告
     */
    private void showImageAd() {
        t_ad_time.setVisibility(VISIBLE);
        t_ad_time.setText(info.img_ad_time+" 秒");
        Picasso.get().load(info.imgAD).into(imgCover);
        handler.postDelayed(imgAd,ADIN);
    }
    int ADIN=1000;
    Runnable imgAd=new Runnable() {
        @Override
        public void run() {
            info.img_ad_time--;
            t_ad_time.setText(info.img_ad_time+" 秒");
            if (info.img_ad_time>0){
                handler.postDelayed(imgAd,ADIN);
            }else {
                t_ad_time.setVisibility(GONE);
                imgCover.setVisibility(GONE);
                info.IMAGE_AD=false;
                startADVideo();
            }
        }
    };

    /**
     * 调整屏幕亮度
     * @param progress
     */
    private void updateBrightProgress(int progress) {
        Activity activity = (Activity) getContext();
        Window window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = progress * 1.0f / 100;
        if (params.screenBrightness > 1.0f) {
            params.screenBrightness = 1.0f;
        }
        if (params.screenBrightness <= 0.01f) {
            params.screenBrightness = 0.01f;
        }

        window.setAttributes(params);
        seek_brightness.setProgress(progress);
    }

    /**
     * 显示错误弹窗
     * @param errContent
     */
    private void showDialog(String errContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setMessage(errContent).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //ToDo: 你想做的事情
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }

    /**
     * 调整声音
     * @param progress
     */
    private void updateVolumeProgress(int progress) {
        float percentage = (float) progress / seek_sound.getMax();

        if (percentage < 0 || percentage > 1)
            return;

        if (mAudioManager != null) {
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int newVolume = (int) (percentage * maxVolume);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        }
    }
    LYVideoADCallBack adCallBack;

    public void setAdCallBack(LYVideoADCallBack adCallBack) {
        this.adCallBack = adCallBack;
    }

    public int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }

    /** Applies a texture rotation to a {@link TextureView}. */
    private static void applyTextureViewRotation(TextureView textureView, int textureViewRotation) {
        float textureViewWidth = textureView.getWidth();
        float textureViewHeight = textureView.getHeight();
        if (textureViewWidth == 0 || textureViewHeight == 0 || textureViewRotation == 0) {
            textureView.setTransform(null);
        } else {
            Matrix transformMatrix = new Matrix();
            float pivotX = textureViewWidth / 2;
            float pivotY = textureViewHeight / 2;
            transformMatrix.postRotate(textureViewRotation, pivotX, pivotY);

            // After rotation, scale the rotated texture to fit the TextureView size.
            RectF originalTextureRect = new RectF(0, 0, textureViewWidth, textureViewHeight);
            RectF rotatedTextureRect = new RectF();
            transformMatrix.mapRect(rotatedTextureRect, originalTextureRect);
            transformMatrix.postScale(
                    textureViewWidth / rotatedTextureRect.width(),
                    textureViewHeight / rotatedTextureRect.height(),
                    pivotX,
                    pivotY);
            textureView.setTransform(transformMatrix);
        }
    }


    private final class ComponentListener implements Player.EventListener, TextOutput, VideoListener, OnLayoutChangeListener, SphericalSurfaceView.SurfaceListener, SingleTapListener {

        // TextOutput implementation

        @Override
        public void onCues(List<Cue> cues) {
            Log.e(TAG, "onCues: 当发出指令" );
        }

        // VideoListener implementation

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            Log.e(TAG, "onVideoSizeChanged: 当视频大小发生改变"+width+"-------------------"+height );
            videoWidth=width;
            videoHeight=height;
            float videoAspectRatio =
                    (height == 0 || width == 0) ? 1 : (width * pixelWidthHeightRatio) / height;

            if (surfaceView instanceof TextureView) {
                // Try to apply rotation transformation when our surface is a TextureView.
                if (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270) {
                    // We will apply a rotation 90/270 degree to the output texture of the TextureView.
                    // In this case, the output video's width and height will be swapped.
                    videoAspectRatio = 1 / videoAspectRatio;
                }
                if (textureViewRotation != 0) {
                    surfaceView.removeOnLayoutChangeListener(this);
                }
                textureViewRotation = unappliedRotationDegrees;
                if (textureViewRotation != 0) {
                    // The texture view's dimensions might be changed after layout step.
                    // So add an OnLayoutChangeListener to apply rotation after layout step.
                    surfaceView.addOnLayoutChangeListener(this);
                }
                applyTextureViewRotation( surfaceView, textureViewRotation);
            }
            layoutFormart();
           // onContentAspectRatioChanged(videoAspectRatio, frame_video, surfaceView);
        }

        @Override
        public void onRenderedFirstFrame() {
            Log.e(TAG, "onRenderedFirstFrame: 渲染第一帧" );
        }

        @Override
        public void onTracksChanged(TrackGroupArray tracks, TrackSelectionArray selections) {
            Log.e(TAG, "onTracksChanged: 轨迹改变" );
        }

        // Player.EventListener implementation

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.e(TAG, "onPlayerStateChanged: 已更改的播放机状态");
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            Log.e(TAG, "onPositionDiscontinuity:非定位连续性 " );

        }

        // OnLayoutChangeListener implementation

        @Override
        public void onLayoutChange(
                View view,
                int left,
                int top,
                int right,
                int bottom,
                int oldLeft,
                int oldTop,
                int oldRight,
                int oldBottom) {
            Log.e(TAG, "onLayoutChange: 当layout 改变" );
            applyTextureViewRotation((TextureView) view, textureViewRotation);
        }

        // SphericalSurfaceView.SurfaceTextureListener implementation

        @Override
        public void surfaceChanged(@Nullable Surface surface) {
            Log.e(TAG, "surfaceChanged: " );
            if (player != null) {
                Player.VideoComponent videoComponent = player.getVideoComponent();
                if (videoComponent != null) {
                    videoComponent.setVideoSurface(surface);
                }
            }
        }

        // SingleTapListener implementation

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.e(TAG, "onSingleTapUp: " );
            return true;
        }
    }




    /**
     * 事件分析回调
     */
    AnalyticsListener analyticsListener=new AnalyticsListener() {
        @Override
        public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {
            Log.e(TAG, "onPlayerStateChanged: 已更改的播放机状态" );
        }

        @Override
        public void onTimelineChanged(EventTime eventTime, int reason) {
            Log.e(TAG, "onTimelineChanged: 已更改的时间" );
        }

        @Override
        public void onPositionDiscontinuity(EventTime eventTime, int reason) {
            Log.e(TAG, "onPositionDiscontinuity:非定位连续性 " );
        }

        @Override
        public void onSeekStarted(EventTime eventTime) {
            Log.e(TAG, "onSeekStarted:开始seek " );
        }

        @Override
        public void onSeekProcessed(EventTime eventTime) {
            Log.e(TAG, "onSeekProcessed: seek 处理" );
        }

        @Override
        public void onPlaybackParametersChanged(EventTime eventTime, PlaybackParameters playbackParameters) {
            Log.e(TAG, "onPlaybackParametersChanged: 播放参数更改时" );
        }

        @Override
        public void onRepeatModeChanged(EventTime eventTime, int repeatMode) {
            Log.e(TAG, "onRepeatModeChanged: 重复模式更改时" );
        }

        @Override
        public void onShuffleModeChanged(EventTime eventTime, boolean shuffleModeEnabled) {
            Log.e(TAG, "onShuffleModeChanged:论分流模式的改变 " );
        }

        @Override
        public void onLoadingChanged(EventTime eventTime, boolean isLoading) {
            Log.e(TAG, "onLoadingChanged: 加载时更改" );
        }

        @Override
        public void onPlayerError(EventTime eventTime, ExoPlaybackException error) {
            Log.e(TAG, "onPlayerError: " );
        }

        @Override
        public void onTracksChanged(EventTime eventTime, TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.e(TAG, "onTracksChanged: 在更改的轨道上" );
        }

        @Override
        public void onLoadStarted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {
            Log.e(TAG, "onLoadStarted: 加载启动" );
            showLoading();
        }

        @Override
        public void onLoadCompleted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {
            Log.e(TAG, "onLoadCompleted: 加载完成" );
            dissmissLoading();
        }

        @Override
        public void onLoadCanceled(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {
            Log.e(TAG, "onLoadCanceled:已取消加载 " );
            dissmissLoading();
        }

        @Override
        public void onLoadError(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
            Log.e(TAG, "onLoadError: " );
            dissmissLoading();
        }

        @Override
        public void onDownstreamFormatChanged(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {
            Log.e(TAG, "onDownstreamFormatChanged: 下游格式更改时" );
        }

        @Override
        public void onUpstreamDiscarded(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {
            Log.e(TAG, "onUpstreamDiscarded: \n" +
                    "上游废弃" );
        }

        @Override
        public void onMediaPeriodCreated(EventTime eventTime) {
            Log.e(TAG, "onMediaPeriodCreated: 创建的媒体时段" );
        }

        @Override
        public void onMediaPeriodReleased(EventTime eventTime) {
            Log.e(TAG, "onMediaPeriodReleased: 关于媒体发布期" );
        }

        @Override
        public void onReadingStarted(EventTime eventTime) {
            Log.e(TAG, "onReadingStarted: 开始阅读" );
            onStart();
        }

        @Override
        public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {
          //  t_percentage.setText(LYEXOPlayerManger.init().getBufferedPosition() + "%");
            t_network.setText("正在缓冲中，请稍等···\n" + StringUtils.netWorkForamt(bitrateEstimate/(1024*1024),NET_MAGNIFICATION));
            Log.e(TAG, "onBandwidthEstimate: 带宽估计 总加载时间"+totalLoadTimeMs+"   已加载的字节总数"+totalBytesLoaded +"应该是网速"+bitrateEstimate/(1024*1024));
        }

        @Override
        public void onSurfaceSizeChanged(EventTime eventTime, int width, int height) {

            Log.e(TAG, "onSurfaceSizeChanged: "+width+"------"+height );
        }

        @Override
        public void onMetadata(EventTime eventTime, Metadata metadata) {
            Log.e(TAG, "onMetadata: 元数据" );
        }

        @Override
        public void onDecoderEnabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {
            Log.e(TAG, "onDecoderEnabled:启用解码器时 " );
        }

        @Override
        public void onDecoderInitialized(EventTime eventTime, int trackType, String decoderName, long initializationDurationMs) {
            Log.e(TAG, "onDecoderInitialized: 解码器初始化时" );
        }

        @Override
        public void onDecoderInputFormatChanged(EventTime eventTime, int trackType, Format format) {
            Log.e(TAG, "onDecoderInputFormatChanged:解码器输入格式更改时 " );
        }

        @Override
        public void onDecoderDisabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {
            Log.e(TAG, "onDecoderDisabled:已禁用解码器 " );
        }

        @Override
        public void onAudioSessionId(EventTime eventTime, int audioSessionId) {
            Log.e(TAG, "onAudioSessionId: 在音频会话ID上" );
        }

        @Override
        public void onAudioAttributesChanged(EventTime eventTime, AudioAttributes audioAttributes) {
            Log.e(TAG, "onAudioAttributesChanged: 音频属性改变" );
        }

        @Override
        public void onVolumeChanged(EventTime eventTime, float volume) {
            Log.e(TAG, "onVolumeChanged: 音量改变时" );
        }

        @Override
        public void onAudioUnderrun(EventTime eventTime, int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
            Log.e(TAG, "onAudioUnderrun: 在音频欠载运行时" );
        }

        @Override
        public void onDroppedVideoFrames(EventTime eventTime, int droppedFrames, long elapsedMs) {
            Log.e(TAG, "onDroppedVideoFrames:在丢弃的视频帧上 " );
        }

        @Override
        public void onVideoSizeChanged(EventTime eventTime, int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            Log.e(TAG, "onVideoSizeChanged: " );
        }

        @Override
        public void onRenderedFirstFrame(EventTime eventTime, @Nullable Surface surface) {
            Log.e(TAG, "onRenderedFirstFrame: 在渲染的第一帧上" );
        }

        @Override
        public void onDrmSessionAcquired(EventTime eventTime) {
            Log.e(TAG, "onDrmSessionAcquired:在获得DRM会话时 " );
        }

        @Override
        public void onDrmKeysLoaded(EventTime eventTime) {
            Log.e(TAG, "onDrmKeysLoaded: 在已加载的DRM密钥上" );
        }

        @Override
        public void onDrmSessionManagerError(EventTime eventTime, Exception error) {
            Log.e(TAG, "onDrmSessionManagerError: 在DRM会话管理器错误时" );
        }

        @Override
        public void onDrmKeysRestored(EventTime eventTime) {
            Log.e(TAG, "onDrmKeysRestored:" +
                    "在DRM密钥恢复时" );
        }

        @Override
        public void onDrmKeysRemoved(EventTime eventTime) {
            Log.e(TAG, "onDrmKeysRemoved:在已删除的DRM密钥上 " );
        }

        @Override
        public void onDrmSessionReleased(EventTime eventTime) {
            Log.e(TAG, "onDrmSessionReleased: 会话已释放" );
        }
    };
}
