package com.rahulk11.audioplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;

/**
 * Created by rahul on 6/9/2017.
 */

public class SongService extends Service {
    public final static String ACTION_PLAY = "PLAY";
    public final static String ACTION_PAUSE = "PAUSE";
    public final static String ACTION_RESUME = "RESUME";
    public final static String ACTION_STOP = "STOP";
    public final static String ACTION_SEEK = "SEEK_TO";
    private AudioManager audioManager;
    private PhoneStateListener phoneStateListener;
    private static MediaPlayer player;
    private static Context mContext;
    String title = "", artist = "", album = "";
    private static NotificationHandler.NotifBtnClickReceiver receiver;
    private static byte[] byteData;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                int duration = mp.getDuration(), currPos=mp.getCurrentPosition();
                if ((currPos>=(duration)||currPos==0)  && duration!=0){
                    PlaybackManager.playNext(true);
                } else{
                    PlaybackManager.playPauseEvent(false, false, currPos);
                }
            }
        });

        try {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        if (player.isPlaying()) {
                            player.pause();
                        }
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                        player.start();
                    } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        if (player.isPlaying()) {
                            player.pause();
                        }
                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            };
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mContext.getSystemService(Context.TELEPHONY_SERVICE);
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
            IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            receiver = new NotificationHandler.NotifBtnClickReceiver();
            registerReceiver( receiver, receiverFilter );
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_PLAY:
                String data = intent.getStringExtra(MainActivity.SONG_PATH);
                title = intent.getStringExtra(MainActivity.SONG_TITLE);
                artist = intent.getStringExtra(MainActivity.ARTIST_NAME);
                album = intent.getStringExtra(MainActivity.ALBUM_NAME);
                try {
                    player.reset();
                    player.setDataSource(data);
                    player.prepare();
                    player.start();
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(data);
                    byteData = mmr.getEmbeddedPicture();
                    new NotificationHandler(mContext, title, artist, album, byteData, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ACTION_PAUSE:
                if (player.isPlaying()) {
                    player.pause();
                    new NotificationHandler(mContext, title, artist, album, byteData, false);
                }
                break;
            case ACTION_RESUME:
                if (player != null) {
                    player.start();
                    new NotificationHandler(mContext, title, artist, album, byteData, true);
                }
                break;
            case ACTION_STOP:
                if (player != null) {
                    stopSelf();
                }
                break;
            case ACTION_SEEK:
                int seekTo = intent.getIntExtra("seekTo", 0);
                String seekData = intent.getStringExtra(MainActivity.SONG_PATH);
                title = intent.getStringExtra(MainActivity.SONG_TITLE);
                artist = intent.getStringExtra(MainActivity.ARTIST_NAME);
                album = intent.getStringExtra(MainActivity.ALBUM_NAME);
                if (player != null){
                    try {
                        if(!android.text.TextUtils.isEmpty(seekData)){
                            player.reset();
                            player.setDataSource(seekData);
                            player.prepare();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    player.seekTo(seekTo);
                    player.start();
                }
                new NotificationHandler(mContext, title, artist, album, byteData, true);
                break;
        }
        return Service.START_NOT_STICKY;
    }

    public IBinder onUnBind(Intent arg0) {
        // TO DO Auto-generated method
        return null;
    }

    public void onStop() {

    }

    public void onPause() {

    }

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
        player = null;
        unregisterReceiver(receiver);
        NotificationHandler.onServiceDestroy();
        stopSelf();
    }

    @Override
    public void onLowMemory() {

    }


    public static boolean isPlaying() {
        if (player != null)
            return player.isPlaying();
        return false;
    }

    public static int getCurrPos() {
        if (player != null) {
            return player.getCurrentPosition();
        }
        return 0;
    }
    public static int getDuration() {
        if (player != null) {
            return player.getDuration();
        }
        return 0;
    }

}
