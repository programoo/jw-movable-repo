package com.jwplayer.demo.movableplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.core.PlayerState;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;

public class MainActivity extends AppCompatActivity implements VideoPlayerEvents.OnFullscreenListener, VideoPlayerEvents.OnPlayListener, VideoPlayerEvents.OnPauseListener, VideoPlayerEvents.OnCompleteListener {

    private static final double SCALING_FACTOR = 1.75;

    private JWPlayerView mPlayerView;
    private MovableFrameLayout mPlayerContainer;
    private CoordinatorLayout mCoordinatorLayout;
    private RelativeLayout mContentContainer;

    private ViewGroup.LayoutParams mInitialLayoutParams;

    private PlayerState mPlayerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a reference to the CoordinatorLayout
        mContentContainer = (RelativeLayout) findViewById(R.id.main_container);

        // Get a reference to the FrameLayout that we will use to contain the player in.
        mPlayerContainer = (MovableFrameLayout) findViewById(R.id.player_container);

        // Initialize a new JW Player.
        mPlayerView = new JWPlayerView(this, new PlayerConfig.Builder()
                .file("https://tungsten.aaplimg.com/VOD/bipbop_adv_example_v2/master.m3u8")
                .build());
        mPlayerView.addOnFullscreenListener(this);
        mPlayerView.addOnPlayListener(this);
        mPlayerView.addOnPauseListener(this);
        mPlayerView.addOnCompleteListener(this);

        // Add the View to the View Hierarchy.
        mPlayerContainer.addView(mPlayerView);
        setInitialLayoutParams();
    }

    /**
     * Sets the initial layout parameters for the {@link JWPlayerView}.
     */
    private void setInitialLayoutParams() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mPlayerContainer.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (displayMetrics.widthPixels / 16) * 9)); // 16:9
        } else {
            // We need to use height to calculate a 16:9 ratio since we're in landscape mode.
            mPlayerContainer.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (displayMetrics.heightPixels / 16) * 9)); // 16:9
            // Toggle fullscreen, since we're in landscape mode.
            mPlayerView.setFullscreen(true, true);
        }
    }

    /**
     * Positions the movable player to the right bottom corner.
     *
     * @param layoutParams
     * @return
     */
    private RelativeLayout.LayoutParams getInitialMovablePlayerLayoutParams(RelativeLayout.LayoutParams layoutParams) {
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        layoutParams.setMargins(0, 0, Math.round(displayMetrics.density * 16), Math.round(displayMetrics.density * 16));
        return layoutParams;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (!mPlayerContainer.isMovable()) {
            // Set fullscreen when the device is rotated to landscape, and not in movable player mode.
            mPlayerView.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE, true);
        } else {
            // When we rotate and the player is in movable mode, reset it's position.
            mPlayerContainer.setLayoutParams(getInitialMovablePlayerLayoutParams(
                    (RelativeLayout.LayoutParams) mPlayerContainer.getLayoutParams()));
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        // Let JW Player know that the app has returned from the background
        mPlayerView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Let JW Player know that the app is going to the background
        mPlayerView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Let JW Player know that the app is being destroyed
        mPlayerView.onDestroy();
        super.onDestroy();
    }

    /**
     * Handles JW Player going to and returning from fullscreen by hiding the ActionBar
     *
     * @param fullscreen true if the player is fullscreen
     */
    @Override
    public void onFullscreen(boolean fullscreen) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (fullscreen) {
                mCoordinatorLayout.setFitsSystemWindows(false);
                actionBar.hide();
            } else {
                mCoordinatorLayout.setFitsSystemWindows(true);
                actionBar.show();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // If we are in fullscreen mode, exit fullscreen mode when the user uses the back button.
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPlayerView.getFullscreen()) {
                mPlayerView.setFullscreen(false, true);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPlay(PlayerState playerState) {
        mPlayerState = playerState;
    }

    @Override
    public void onComplete() {
        mPlayerState = PlayerState.IDLE;
    }

    @Override
    public void onPause(PlayerState playerState) {
        mPlayerState = playerState;
    }
}
