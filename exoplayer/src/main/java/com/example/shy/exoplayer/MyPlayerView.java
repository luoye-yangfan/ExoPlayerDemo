/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.shy.exoplayer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.DiscontinuityReason;
import com.google.android.exoplayer2.Player.VideoComponent;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.id3.ApicFrame;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.ui.spherical.SingleTapListener;
import com.google.android.exoplayer2.ui.spherical.SphericalSurfaceView;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * 复制PlayerView
 */
public class MyPlayerView extends FrameLayout implements AdsLoader.AdViewProvider {
  String TAG="MyPlayerView";
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({SHOW_BUFFERING_NEVER, SHOW_BUFFERING_WHEN_PLAYING, SHOW_BUFFERING_ALWAYS})
  public @interface ShowBuffering {}
  /** The buffering view is never shown. */
  public static final int SHOW_BUFFERING_NEVER = 0;
  /**
   * The buffering view is shown when the player is in the {@link Player#STATE_BUFFERING buffering}
   * state and {@link Player#getPlayWhenReady() playWhenReady} is {@code true}.
   */
  public static final int SHOW_BUFFERING_WHEN_PLAYING = 1;
  /**
   * The buffering view is always shown when the player is in the {@link Player#STATE_BUFFERING
   * buffering} state.
   */
  public static final int SHOW_BUFFERING_ALWAYS = 2;

  @Nullable private final AspectRatioFrameLayout contentFrame;
  private final View shutterView;
  @Nullable private final View surfaceView;
  private final ImageView artworkView;
  private final SubtitleView subtitleView;
  @Nullable private final View bufferingView;
  @Nullable private final TextView errorMessageView;
  private final ComponentListener componentListener;
  @Nullable private final FrameLayout adOverlayFrameLayout;
  @Nullable private final FrameLayout overlayFrameLayout;

  private Player player;

  private boolean useArtwork;
  @Nullable private Drawable defaultArtwork;
  private @ShowBuffering int showBuffering;
  private boolean keepContentOnPlayerReset;
  @Nullable private ErrorMessageProvider<? super ExoPlaybackException> errorMessageProvider;
  @Nullable private CharSequence customErrorMessage;
  private boolean controllerHideDuringAds;

  private int textureViewRotation;

  public MyPlayerView(Context context) {
    this(context, null);
  }

  public MyPlayerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MyPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    if (isInEditMode()) {
      contentFrame = null;
      shutterView = null;
      surfaceView = null;
      artworkView = null;
      subtitleView = null;
      bufferingView = null;
      errorMessageView = null;
      componentListener = null;
      adOverlayFrameLayout = null;
      overlayFrameLayout = null;
      ImageView logo = new ImageView(context);
      if (Util.SDK_INT >= 23) {
        configureEditModeLogoV23(getResources(), logo);
      } else {
        configureEditModeLogo(getResources(), logo);
      }
      addView(logo);
      return;
    }

    int playerLayoutId = com.google.android.exoplayer2.ui.R.layout.exo_player_view;

    LayoutInflater.from(context).inflate(playerLayoutId, this);
    componentListener = new ComponentListener();
    setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
    // Content frame.
    contentFrame = findViewById(com.google.android.exoplayer2.ui.R.id.exo_content_frame);
    // Shutter view.
    shutterView = findViewById(com.google.android.exoplayer2.ui.R.id.exo_shutter);
    // Create a surface view and insert it into the content frame, if there is one.

      ViewGroup.LayoutParams params =
          new ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

      surfaceView = new TextureView(context);
      surfaceView.setLayoutParams(params);
      contentFrame.addView(surfaceView, 0);
    // Ad overlay frame layout.
    adOverlayFrameLayout = findViewById(com.google.android.exoplayer2.ui.R.id.exo_ad_overlay);

    // Overlay frame layout.
    overlayFrameLayout = findViewById(com.google.android.exoplayer2.ui.R.id.exo_overlay);

    // Artwork view.
    artworkView = findViewById(com.google.android.exoplayer2.ui.R.id.exo_artwork);
    this.useArtwork = useArtwork && artworkView != null;
    // Subtitle view.
    subtitleView = findViewById(com.google.android.exoplayer2.ui.R.id.exo_subtitles);
    if (subtitleView != null) {
      subtitleView.setUserDefaultStyle();
      subtitleView.setUserDefaultTextSize();
    }
    // Buffering view.
    bufferingView = findViewById(com.google.android.exoplayer2.ui.R.id.exo_buffering);
    if (bufferingView != null) {
      bufferingView.setVisibility(View.GONE);
    }
    this.showBuffering = showBuffering;

    // Error message view.
    errorMessageView = findViewById(com.google.android.exoplayer2.ui.R.id.exo_error_message);
    if (errorMessageView != null) {
      errorMessageView.setVisibility(View.GONE);
    }
    // Playback control view.
    hideController();
  }


  public Player getPlayer() {
    return player;
  }

  public void setPlayer(@Nullable Player player) {
    Assertions.checkState(Looper.myLooper() == Looper.getMainLooper());
    Assertions.checkArgument(
        player == null || player.getApplicationLooper() == Looper.getMainLooper());
    if (this.player == player) {
      return;
    }
    if (this.player != null) {
      this.player.removeListener(componentListener);
      VideoComponent oldVideoComponent = this.player.getVideoComponent();
      if (oldVideoComponent != null) {
        oldVideoComponent.removeVideoListener(componentListener);
        oldVideoComponent.clearVideoTextureView((TextureView) surfaceView);
      }
      Player.TextComponent oldTextComponent = this.player.getTextComponent();
      if (oldTextComponent != null) {
        oldTextComponent.removeTextOutput(componentListener);
      }
    }
    this.player = player;
    if (subtitleView != null) {
      subtitleView.setCues(null);
    }
    updateBuffering();
    updateErrorMessage();
    updateForCurrentTrackSelections(/* isNewPlayer= */ true);
    if (player != null) {
      VideoComponent newVideoComponent = player.getVideoComponent();
      if (newVideoComponent != null) {
        if (surfaceView instanceof TextureView) {
          newVideoComponent.setVideoTextureView((TextureView) surfaceView);
        } else if (surfaceView instanceof SphericalSurfaceView) {
          ((SphericalSurfaceView) surfaceView).setVideoComponent(newVideoComponent);
        } else if (surfaceView instanceof SurfaceView) {
          newVideoComponent.setVideoSurfaceView((SurfaceView) surfaceView);
        }
        newVideoComponent.addVideoListener(componentListener);
      }
      Player.TextComponent newTextComponent = player.getTextComponent();
      if (newTextComponent != null) {
        newTextComponent.addTextOutput(componentListener);
      }
      player.addListener(componentListener);
      maybeShowController(false);
    } else {
      hideController();
    }
  }

  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);
    if (surfaceView instanceof SurfaceView) {
      // Work around https://github.com/google/ExoPlayer/issues/3160.
      surfaceView.setVisibility(visibility);
    }
  }

  /**
   * Sets the default artwork to display if {@code useArtwork} is {@code true} and no artwork is
   * present in the media.
   *
   * @param defaultArtwork the default artwork to display.
   * @deprecated use (@link {@link #setDefaultArtwork(Drawable)} instead.
   */
  @Deprecated
  public void setDefaultArtwork(@Nullable Bitmap defaultArtwork) {
    setDefaultArtwork(
        defaultArtwork == null ? null : new BitmapDrawable(getResources(), defaultArtwork));
  }

  /**
   * Sets the default artwork to display if {@code useArtwork} is {@code true} and no artwork is
   * present in the media.
   *
   * @param defaultArtwork the default artwork to display
   */
  public void setDefaultArtwork(@Nullable Drawable defaultArtwork) {
    if (this.defaultArtwork != defaultArtwork) {
      this.defaultArtwork = defaultArtwork;
      updateForCurrentTrackSelections(/* isNewPlayer= */ false);
    }
  }






  /**
   * Sets whether a buffering spinner is displayed when the player is in the buffering state. The
   * buffering spinner is not displayed by default.
   *
   * @deprecated Use {@link #setShowBuffering(int)}
   * @param showBuffering Whether the buffering icon is displayed
   */
  @Deprecated
  public void setShowBuffering(boolean showBuffering) {
    setShowBuffering(showBuffering ? SHOW_BUFFERING_WHEN_PLAYING : SHOW_BUFFERING_NEVER);
  }

  /**
   * Sets whether a buffering spinner is displayed when the player is in the buffering state. The
   * buffering spinner is not displayed by default.
   *
   * @param showBuffering The mode that defines when the buffering spinner is displayed. One of
   *     {@link #SHOW_BUFFERING_NEVER}, {@link #SHOW_BUFFERING_WHEN_PLAYING} and
   *     {@link #SHOW_BUFFERING_ALWAYS}.
   */
  public void setShowBuffering(@ShowBuffering int showBuffering) {
    if (this.showBuffering != showBuffering) {
      this.showBuffering = showBuffering;
      updateBuffering();
    }
  }





  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (player != null && player.isPlayingAd()) {
      return super.dispatchKeyEvent(event);
    }
    return false;
  }

  /** Hides the playback controls. Does nothing if playback controls are disabled. */
  public void hideController() {

  }



  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (ev.getActionMasked() != MotionEvent.ACTION_DOWN) {
      return false;
    }
    return performClick();
  }

  @Override
  public boolean performClick() {
    super.performClick();
    return toggleControllerVisibility();
  }

  @Override
  public boolean onTrackballEvent(MotionEvent ev) {
    maybeShowController(true);
    return true;
  }

  /**
   * Called when there's a change in the aspect ratio of the content being displayed. The default
   * implementation sets the aspect ratio of the content frame to that of the content, unless the
   * content view is a {@link SphericalSurfaceView} in which case the frame's aspect ratio is
   * cleared.
   *
   * @param contentAspectRatio The aspect ratio of the content.
   * @param contentFrame The content frame, or {@code null}.
   * @param contentView The view that holds the content being displayed, or {@code null}.
   */
  protected void onContentAspectRatioChanged(
      float contentAspectRatio,
      @Nullable AspectRatioFrameLayout contentFrame,
      @Nullable View contentView) {
    if (contentFrame != null) {
      contentFrame.setAspectRatio(
          contentView instanceof SphericalSurfaceView ? 0 : contentAspectRatio);
    }
  }

  // AdsLoader.AdViewProvider implementation.

  @Override
  public ViewGroup getAdViewGroup() {
    return Assertions.checkNotNull(
        adOverlayFrameLayout, "exo_ad_overlay must be present for ad playback");
  }

  @Override
  public View[] getAdOverlayViews() {
    ArrayList<View> overlayViews = new ArrayList<>();
    if (overlayFrameLayout != null) {
      overlayViews.add(overlayFrameLayout);
    }

    return overlayViews.toArray(new View[0]);
  }

  // Internal methods.

  private boolean toggleControllerVisibility() {

    return true;
  }

  /** Shows the playback controls, but only if forced or shown indefinitely. */
  private void maybeShowController(boolean isForced) {
    if (isPlayingAd() && controllerHideDuringAds) {
      return;
    }
  }


  private boolean isPlayingAd() {
    return player != null && player.isPlayingAd() && player.getPlayWhenReady();
  }

  private void updateForCurrentTrackSelections(boolean isNewPlayer) {
    if (player == null || player.getCurrentTrackGroups().isEmpty()) {
      if (!keepContentOnPlayerReset) {
        hideArtwork();
        closeShutter();
      }
      return;
    }

    if (isNewPlayer && !keepContentOnPlayerReset) {
      // Hide any video from the previous player.
      closeShutter();
    }

    TrackSelectionArray selections = player.getCurrentTrackSelections();
    for (int i = 0; i < selections.length; i++) {
      if (player.getRendererType(i) == C.TRACK_TYPE_VIDEO && selections.get(i) != null) {
        // Video enabled so artwork must be hidden. If the shutter is closed, it will be opened in
        // onRenderedFirstFrame().
        hideArtwork();
        return;
      }
    }

    // Video disabled so the shutter must be closed.
    closeShutter();
    // Display artwork if enabled and available, else hide it.
    if (useArtwork) {
      for (int i = 0; i < selections.length; i++) {
        TrackSelection selection = selections.get(i);
        if (selection != null) {
          for (int j = 0; j < selection.length(); j++) {
            Metadata metadata = selection.getFormat(j).metadata;
            if (metadata != null && setArtworkFromMetadata(metadata)) {
              return;
            }
          }
        }
      }
      if (setDrawableArtwork(defaultArtwork)) {
        return;
      }
    }
    // Artwork disabled or unavailable.
    hideArtwork();
  }

  private boolean setArtworkFromMetadata(Metadata metadata) {
    for (int i = 0; i < metadata.length(); i++) {
      Metadata.Entry metadataEntry = metadata.get(i);
      if (metadataEntry instanceof ApicFrame) {
        byte[] bitmapData = ((ApicFrame) metadataEntry).pictureData;
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
        return setDrawableArtwork(new BitmapDrawable(getResources(), bitmap));
      }
    }
    return false;
  }

  private boolean setDrawableArtwork(@Nullable Drawable drawable) {
    if (drawable != null) {
      int drawableWidth = drawable.getIntrinsicWidth();
      int drawableHeight = drawable.getIntrinsicHeight();
      if (drawableWidth > 0 && drawableHeight > 0) {
        float artworkAspectRatio = (float) drawableWidth / drawableHeight;
        onContentAspectRatioChanged(artworkAspectRatio, contentFrame, artworkView);
        artworkView.setImageDrawable(drawable);
        artworkView.setVisibility(VISIBLE);
        return true;
      }
    }
    return false;
  }

  private void hideArtwork() {
    if (artworkView != null) {
      artworkView.setImageResource(android.R.color.transparent); // Clears any bitmap reference.
      artworkView.setVisibility(INVISIBLE);
    }
  }

  private void closeShutter() {
    if (shutterView != null) {
      shutterView.setVisibility(View.VISIBLE);
    }
  }

  private void updateBuffering() {
    if (bufferingView != null) {
      boolean showBufferingSpinner =
          player != null
              && player.getPlaybackState() == Player.STATE_BUFFERING
              && (showBuffering == SHOW_BUFFERING_ALWAYS
                  || (showBuffering == SHOW_BUFFERING_WHEN_PLAYING && player.getPlayWhenReady()));
      bufferingView.setVisibility(showBufferingSpinner ? View.VISIBLE : View.GONE);
    }
  }

  private void updateErrorMessage() {
    if (errorMessageView != null) {
      if (customErrorMessage != null) {
        errorMessageView.setText(customErrorMessage);
        errorMessageView.setVisibility(View.VISIBLE);
        return;
      }
      ExoPlaybackException error = null;
      if (player != null
          && player.getPlaybackState() == Player.STATE_IDLE
          && errorMessageProvider != null) {
        error = player.getPlaybackError();
      }
      if (error != null) {
        CharSequence errorMessage = errorMessageProvider.getErrorMessage(error).second;
        errorMessageView.setText(errorMessage);
        errorMessageView.setVisibility(View.VISIBLE);
      } else {
        errorMessageView.setVisibility(View.GONE);
      }
    }
  }

  @TargetApi(23)
  private static void configureEditModeLogoV23(Resources resources, ImageView logo) {
    logo.setImageDrawable(resources.getDrawable(com.google.android.exoplayer2.ui.R.drawable.exo_edit_mode_logo, null));
    logo.setBackgroundColor(resources.getColor(com.google.android.exoplayer2.ui.R.color.exo_edit_mode_background_color, null));
  }

  @SuppressWarnings("deprecation")
  private static void configureEditModeLogo(Resources resources, ImageView logo) {
    logo.setImageDrawable(resources.getDrawable(com.google.android.exoplayer2.ui.R.drawable.exo_edit_mode_logo));
    logo.setBackgroundColor(resources.getColor(com.google.android.exoplayer2.ui.R.color.exo_edit_mode_background_color));
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

  @SuppressLint("InlinedApi")
  private boolean isDpadKey(int keyCode) {
    return keyCode == KeyEvent.KEYCODE_DPAD_UP
        || keyCode == KeyEvent.KEYCODE_DPAD_UP_RIGHT
        || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
        || keyCode == KeyEvent.KEYCODE_DPAD_DOWN_RIGHT
        || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
        || keyCode == KeyEvent.KEYCODE_DPAD_DOWN_LEFT
        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT
        || keyCode == KeyEvent.KEYCODE_DPAD_UP_LEFT
        || keyCode == KeyEvent.KEYCODE_DPAD_CENTER;
  }

  private final class ComponentListener implements Player.EventListener, TextOutput, VideoListener, OnLayoutChangeListener, SphericalSurfaceView.SurfaceListener, SingleTapListener {

    // TextOutput implementation

    @Override
    public void onCues(List<Cue> cues) {
      Log.e(TAG, "onCues: 当发出指令" );
      if (subtitleView != null) {
        subtitleView.onCues(cues);
      }
    }

    // VideoListener implementation

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
      Log.e(TAG, "onVideoSizeChanged: 当视频大小发生改变" );
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
        applyTextureViewRotation((TextureView) surfaceView, textureViewRotation);
      }

      onContentAspectRatioChanged(videoAspectRatio, contentFrame, surfaceView);
    }

    @Override
    public void onRenderedFirstFrame() {
      Log.e(TAG, "onRenderedFirstFrame: 渲染第一帧" );
      if (shutterView != null) {
        shutterView.setVisibility(INVISIBLE);
      }
    }

    @Override
    public void onTracksChanged(TrackGroupArray tracks, TrackSelectionArray selections) {
      Log.e(TAG, "onTracksChanged: 轨迹改变" );
      updateForCurrentTrackSelections(/* isNewPlayer= */ false);
    }

    // Player.EventListener implementation

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      Log.e(TAG, "onPlayerStateChanged: 已更改的播放机状态");
      updateBuffering();
      updateErrorMessage();
      if (isPlayingAd() && controllerHideDuringAds) {
        hideController();
      } else {
        maybeShowController(false);
      }
    }

    @Override
    public void onPositionDiscontinuity(@DiscontinuityReason int reason) {
      Log.e(TAG, "onPositionDiscontinuity:非定位连续性 " );
      if (isPlayingAd() && controllerHideDuringAds) {
        hideController();
      }
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
        VideoComponent videoComponent = player.getVideoComponent();
        if (videoComponent != null) {
          videoComponent.setVideoSurface(surface);
        }
      }
    }

    // SingleTapListener implementation

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      Log.e(TAG, "onSingleTapUp: " );
      return toggleControllerVisibility();
    }
  }
}
