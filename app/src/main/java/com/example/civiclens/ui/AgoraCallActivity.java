package com.example.civiclens.ui;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.civiclens.BuildConfig;
import com.example.civiclens.R;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

/**
 * Agora RTC Real-Time Voice & Video Calling Activity
 */
public class AgoraCallActivity extends AppCompatActivity {

    public static final String EXTRA_CHANNEL_NAME = "channel_name";
    public static final String EXTRA_IS_VIDEO = "is_video";

    private RtcEngine mRtcEngine;
    private FrameLayout localVideoContainer;
    private FrameLayout remoteVideoContainer;
    private TextView callStatusText;
    private boolean isMuted = false;
    private boolean isVideoCall = true;
    private String channelName = "CivicLens_Assembly_Group";

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> {
                callStatusText.setVisibility(View.GONE);
                if (isVideoCall) {
                    setupRemoteVideo(uid);
                }
                com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(
                        AgoraCallActivity.this, "🟢 Citizen joined the assembly call!", false);
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> {
                remoteVideoContainer.removeAllViews();
                callStatusText.setText("Citizen left assembly call.");
                callStatusText.setVisibility(View.VISIBLE);
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agora_call);

        channelName = getIntent().getStringExtra(EXTRA_CHANNEL_NAME);
        if (channelName == null || channelName.isEmpty()) {
            channelName = "CivicLens_Assembly_Group";
        }
        isVideoCall = getIntent().getBooleanExtra(EXTRA_IS_VIDEO, true);

        localVideoContainer = findViewById(R.id.localVideoContainer);
        remoteVideoContainer = findViewById(R.id.remoteVideoContainer);
        callStatusText = findViewById(R.id.callStatusText);

        findViewById(R.id.btnMuteAudio).setOnClickListener(v -> onMuteClicked());
        findViewById(R.id.btnEndCall).setOnClickListener(v -> finish());
        findViewById(R.id.btnSwitchCamera).setOnClickListener(v -> {
            if (mRtcEngine != null) {
                mRtcEngine.switchCamera();
            }
        });

        initAgoraEngineAndJoin();
    }

    private void initAgoraEngineAndJoin() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getApplicationContext();
            config.mAppId = "e3d48911f9814c33a21644781742a781";
            config.mEventHandler = mRtcEventHandler;

            mRtcEngine = RtcEngine.create(config);

            if (isVideoCall) {
                mRtcEngine.enableVideo();
                setupLocalVideo();
            } else {
                mRtcEngine.enableAudio();
                localVideoContainer.setVisibility(View.GONE);
                callStatusText.setText("🎙️ Voice Assembly Call Active: " + channelName);
            }

            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
            mRtcEngine.joinChannel(null, channelName, "CivicLensCitizen", 0);

            com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(this, "Connecting to Agora RTC Call Room...", false);
        } catch (Exception e) {
            Toast.makeText(this, "Agora Engine Init Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupLocalVideo() {
        SurfaceView surfaceView = new SurfaceView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        localVideoContainer.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void setupRemoteVideo(int uid) {
        SurfaceView surfaceView = new SurfaceView(getBaseContext());
        remoteVideoContainer.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    private void onMuteClicked() {
        isMuted = !isMuted;
        if (mRtcEngine != null) {
            mRtcEngine.muteLocalAudioStream(isMuted);
        }
        com.example.civiclens.accessibility.AccessibilityHelper.notifyWithHaptic(
                this, isMuted ? "Microphone Muted" : "Microphone Active", false);
    }

    @Override
    protected void onDestroy() {
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
        com.example.civiclens.accessibility.AccessibilityHelper.stop();
        super.onDestroy();
    }
}
