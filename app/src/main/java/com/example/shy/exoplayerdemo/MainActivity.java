package com.example.shy.exoplayerdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.example.shy.exoplayer.EXOActivity;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity implements PlayerControlView.VisibilityListener, Player.EventListener {
    String TAG="MainActivity";
    private PlayerView videoPlayer;
    HlsMediaSource mediaSource;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoPlayer = findViewById(R.id.video);
        videoPlayer.setControllerVisibilityListener(this);//控制层
        videoPlayer.setErrorMessageProvider(new PlayerErrorMessageProvider());//设置错误回调
        videoPlayer.requestFocus();
        //STEREO_MODE_MONO 立体声模式单声道
        //STEREO_MODE_TOP_BOTTOM _顶底_立体模式
        //STEREO_MODE_LEFT_RIGHT  右立体声模式_
         //videoPlayer.getVideoSurfaceView().setDefaultStereoMode(C.STEREO_MODE_MONO);//设置声音模式。
        DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
        String userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        DefaultDataSourceFactory mediaDataSourceFactory = new DefaultDataSourceFactory(this, BANDWIDTH_METER,
                new DefaultHttpDataSourceFactory(userAgent, BANDWIDTH_METER));
        String fileName = "http://192.168.0.121:2100/video/20190405/8f4DyBLz/hls/index.m3u8";
        Uri uri = Uri.parse(fileName);

        if (fileName.endsWith(".m3u8")){
            mediaSource = new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
        }

//        TrackSelection.Factory trackSelectionFactory  = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
       /* DefaultTrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        DefaultRenderersFactory renderersFactory =
                new DefaultRenderersFactory(this, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);*/
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this);
        videoPlayer.setPlayer(player);
        player.addListener(this);
        player.setPlayWhenReady(true);
        if(mediaSource!=null) {
            player.prepare(mediaSource);
        }
        findViewById(R.id.btn_my).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), EXOActivity.class));
            }
        });
        //SHOW_BUFFERING_WHEN_PLAYING播放时显示缓冲
        //SHOW_BUFFERING_NEVER显示缓冲从不
        //SHOW_BUFFERING_ALWAYS 始终显示缓冲
    }


    @Override
    public void onVisibilityChange(int visibility) {
        Log.e(TAG, "onVisibilityChange: " );
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        Log.e(TAG, "onPointerCaptureChanged: " );
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    /**
     * 播放错误管理
     */
    private class PlayerErrorMessageProvider implements ErrorMessageProvider<ExoPlaybackException> {

        @Override
        public Pair<Integer, String> getErrorMessage(ExoPlaybackException e) {
            String errorString = getString(R.string.error_generic);
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = e.getRendererException();
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                            (MediaCodecRenderer.DecoderInitializationException) cause;
                    if (decoderInitializationException.decoderName == null) {
                        if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                            errorString = getString(R.string.error_querying_decoders);
                        } else if (decoderInitializationException.secureDecoderRequired) {
                            errorString =
                                    getString(
                                            R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                        } else {
                            errorString =
                                    getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                        }
                    } else {
                        errorString =
                                getString(
                                        R.string.error_instantiating_decoder,
                                        decoderInitializationException.decoderName);
                    }
                }
            }
            return Pair.create(0, errorString);
        }
    }
}
