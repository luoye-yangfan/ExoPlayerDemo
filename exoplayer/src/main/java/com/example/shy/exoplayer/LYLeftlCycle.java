package com.example.shy.exoplayer;

/**
 * 播放器生命周期
 */
public interface LYLeftlCycle {
    void onError(int what, int extra);
    void onStart();//onstart 在oninfo 的what 里面
    void onInfo(int what, int extra);
    void onBuffer(int percent);//百分比
    void onCompletion();//播放完成
    void onPrepared();//播放器准备成功
    void onSeekComplete();//拖动成功还是什么鬼。
    void onRestore();//恢复播放器到未播放状态、
    void onActivityBack();
    void onFragmentBack();
}
