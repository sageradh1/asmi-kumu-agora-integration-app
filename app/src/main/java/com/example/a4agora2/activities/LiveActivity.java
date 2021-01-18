package com.example.a4agora2.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.example.a4agora2.R;
import com.example.a4agora2.stats.LocalStatsData;
import com.example.a4agora2.stats.RemoteStatsData;
import com.example.a4agora2.stats.StatsData;
import com.example.a4agora2.ui.VideoGridContainer;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.live.LiveInjectStreamConfig;
import io.agora.rtc.live.LiveTranscoding;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static com.example.a4agora2.Constants.USER_UID;
import static io.agora.rtc.live.LiveTranscoding.AudioSampleRateType.TYPE_44100;
import static io.agora.rtc.live.LiveTranscoding.VideoCodecProfileType.HIGH;

public class LiveActivity extends RtcBaseActivity {
    private static final String TAG = LiveActivity.class.getSimpleName();

    private VideoGridContainer mVideoGridContainer;
    private ImageView mMuteAudioBtn;
    private ImageView mMuteVideoBtn;

    private VideoEncoderConfiguration.VideoDimensions mVideoDimension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);
        initUI();
        initData();
    }

    private void initUI() {
        TextView roomName = findViewById(R.id.live_room_name);
        roomName.setText(config().getChannelName());
        roomName.setSelected(true);
        
        initUserIcon();
        int role = getIntent().getIntExtra(
                com.example.a4agora2.Constants.KEY_CLIENT_ROLE,
                Constants.CLIENT_ROLE_AUDIENCE);
        boolean isBroadcaster =  (role == Constants.CLIENT_ROLE_BROADCASTER);

        mMuteVideoBtn = findViewById(R.id.live_btn_mute_video);
        mMuteVideoBtn.setActivated(isBroadcaster);

        mMuteAudioBtn = findViewById(R.id.live_btn_mute_audio);
        mMuteAudioBtn.setActivated(isBroadcaster);

        ImageView beautyBtn = findViewById(R.id.live_btn_beautification);
        beautyBtn.setActivated(true);
        rtcEngine().setBeautyEffectOptions(beautyBtn.isActivated(),
                com.example.a4agora2.Constants.DEFAULT_BEAUTY_OPTIONS);

        mVideoGridContainer = findViewById(R.id.live_video_grid_layout);
        mVideoGridContainer.setStatsManager(statsManager());

        rtcEngine().setClientRole(role);
        if (isBroadcaster) startBroadcast();
//        if (isBroadcaster) startBroadcastToAsmi();


    }

    private void initUserIcon() {
        Bitmap origin = BitmapFactory.decodeResource(getResources(), R.drawable.fake_user_icon);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), origin);
        drawable.setCircular(true);
        ImageView iconView = findViewById(R.id.live_name_board_icon);
        iconView.setImageDrawable(drawable);
    }

    private void initData() {
        mVideoDimension = com.example.a4agora2.Constants.VIDEO_DIMENSIONS[
                config().getVideoDimenIndex()];
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        RelativeLayout topLayout = findViewById(R.id.live_room_top_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
        params.height = mStatusBarHeight + topLayout.getMeasuredHeight();
        topLayout.setLayoutParams(params);
        topLayout.setPadding(0, mStatusBarHeight, 0, 0);
    }

//    private void startBroadcast() {
//        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
////        SurfaceView surface = prepareRtcVideo(USER_UID, true);
////        mVideoGridContainer.addUserVideoSurface(USER_UID, surface, true);
//        SurfaceView surface = prepareRtcVideo(0, true);
//        mVideoGridContainer.addUserVideoSurface(0, surface, true);
//        mMuteAudioBtn.setActivated(true);
//    }

    private void stopBroadcast() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
//        removeRtcVideo(USER_UID, true);
//        mVideoGridContainer.removeUserVideo(USER_UID, true);
        removeRtcVideo(0, true);
        mVideoGridContainer.removeUserVideo(0, true);
        mMuteAudioBtn.setActivated(false);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onUserOffline(final int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeRemoteUser(uid);
            }
        });
    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                renderRemoteUser(uid);
            }
        });
    }

    private void renderRemoteUser(int uid) {
        SurfaceView surface = prepareRtcVideo(uid, false);
        mVideoGridContainer.addUserVideoSurface(uid, surface, false);
    }

    private void removeRemoteUser(int uid) {
        removeRtcVideo(uid, false);
        mVideoGridContainer.removeUserVideo(uid, false);
    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setWidth(mVideoDimension.width);
        data.setHeight(mVideoDimension.height);
        data.setFramerate(stats.sentFrameRate);
    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setLastMileDelay(stats.lastmileDelay);
        data.setVideoSendBitrate(stats.txVideoKBitRate);
        data.setVideoRecvBitrate(stats.rxVideoKBitRate);
        data.setAudioSendBitrate(stats.txAudioKBitRate);
        data.setAudioRecvBitrate(stats.rxAudioKBitRate);
        data.setCpuApp(stats.cpuAppUsage);
        data.setCpuTotal(stats.cpuAppUsage);
        data.setSendLoss(stats.txPacketLossRate);
        data.setRecvLoss(stats.rxPacketLossRate);
    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        if (!statsManager().isEnabled()) return;

        StatsData data = statsManager().getStatsData(uid);
        if (data == null) return;

        data.setSendQuality(statsManager().qualityToString(txQuality));
        data.setRecvQuality(statsManager().qualityToString(rxQuality));
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setAudioNetDelay(stats.networkTransportDelay);
        data.setAudioNetJitter(stats.jitterBufferDelay);
        data.setAudioLoss(stats.audioLossRate);
        data.setAudioQuality(statsManager().qualityToString(stats.quality));
    }

    @Override
    public void finish() {
        super.finish();
        statsManager().clearAllData();
    }

    public void onLeaveClicked(View view) {
        finish();
    }

    public void onSwitchCameraClicked(View view) {
        rtcEngine().switchCamera();
    }

    public void onBeautyClicked(View view) {
        view.setActivated(!view.isActivated());
        rtcEngine().setBeautyEffectOptions(view.isActivated(),
                com.example.a4agora2.Constants.DEFAULT_BEAUTY_OPTIONS);
    }

    public void onMoreClicked(View view) {
        // Do nothing at the moment
    }

    public void onPushStreamClicked(View view) {
        // Do nothing at the moment
    }

    public void onMuteAudioClicked(View view) {
        if (!mMuteVideoBtn.isActivated()) return;

        rtcEngine().muteLocalAudioStream(view.isActivated());
        view.setActivated(!view.isActivated());
    }

    public void onMuteVideoClicked(View view) {
        if (view.isActivated()) {
            stopBroadcast();
        } else {
            startBroadcast();
        }
        view.setActivated(!view.isActivated());
    }


    //////////////////
    private void startBroadcast() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        SurfaceView localSurface = prepareRtcVideo(USER_UID, true);
        mVideoGridContainer.addUserVideoSurface(USER_UID, localSurface, true);

//        SurfaceView remoteSurface = prepareRtcVideo(com.example.a4agora2.Constants.USER_UID, false);
//        mVideoGridContainer.addUserVideoSurface(com.example.a4agora2.Constants.USER_UID, remoteSurface, false);


        mMuteAudioBtn.setActivated(true);
        // Java
        // CDN transcoding settings.
        LiveTranscoding config = new LiveTranscoding();
        config.audioSampleRate = TYPE_44100;
        config.audioChannels = 2;
        config.audioBitrate = 48;
        // Width of the video (px). The default value is 360.
//        config.width = 360;
//        // Height of the video (px). The default value is 640.
//        config.height = 640;

        config.width = 360;
        config.height = 720;
        // Video bitrate of the video (Kbps). The default value is 400.
        config.videoBitrate = 400;
        // Video framerate of the video (fps). The default value is 15. Agora adjusts all values over 30 to 30.
        config.videoFramerate = 15;
        // If userCount > 1，set the layout for each user with transcodingUser.
        config.userCount = 1;
        // Video codec profile. Choose to set as Baseline (66), Main (77) or High (100). If you set this parameter to other values, Agora adjusts it to the default value 100.
        config.videoCodecProfile = HIGH;

        // Sets the output layout for each user.
        LiveTranscoding transcoding = new LiveTranscoding();
        LiveTranscoding.TranscodingUser user = new LiveTranscoding.TranscodingUser();
        // The uid must be identical to the uid used in joinChannel().
        user.uid = USER_UID;
        transcoding.addUser(user);
        user.x = 0;
        user.audioChannel = 0;
        user.y = 0;
//        user.width = 640;
//        user.height = 720;
        user.width = 360;
        user.height = 720;

        // CDN transcoding settings when using transcoding.
        rtcEngine().setLiveTranscoding(transcoding);
        String url=getString(R.string.push_stream_url);
        // Adds a URL to which the host pushes a stream. Set the transcodingEnabled parameter as true to enable the transcoding service. Once transcoding is enabled, you need to set the live transcoding configurations by calling the setLiveTranscoding method. We do not recommend transcoding in the case of a single host.
        rtcEngine().addPublishStreamUrl(url, true);

        // Removes a URL to which the host pushes a stream.
        //        rtcEngine().removePublishStreamUrl(url);
        String published_url = getString(R.string.receive_stream_url);
        injectAsmiProcessedStreamToChannel(published_url);
    }

    private void stopBroadcastToAsmi() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        removeRtcVideo(USER_UID, true);
        mVideoGridContainer.removeUserVideo(USER_UID, true);
        mMuteAudioBtn.setActivated(false);
    }

    private void injectAsmiProcessedStreamToChannel(String rtmpUrl){
        LiveInjectStreamConfig config = new LiveInjectStreamConfig();
//        config.width = 640;
//        config.height = 720;

        config.videoGop = 25;
        config.videoFramerate = 15;
        config.videoBitrate = 400;
        config.audioSampleRate = LiveInjectStreamConfig.AudioSampleRateType.TYPE_44100;
        config.audioBitrate = 48;
        config.audioChannels = 1;

        rtcEngine().addInjectStreamUrl(rtmpUrl, config);

        // Remove an online media stream.
//        rtcEngine().removeInjectStreamUrl(rtmpUrl);
    }
}
