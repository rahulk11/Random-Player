package com.rahulk11.randomplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.rahulk11.randomplayer.helpers.NotificationHandler;
import com.rahulk11.randomplayer.helpers.PlaybackManager;

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
    private static AudioManager audioManager;
    private static MediaPlayer player;
    private static Context mContext;
    String data = "", title = "", artist = "", album = "";
    private static NotificationHandler.NotifBtnClickReceiver receiver;
    private NotificationHandler notificationHandler;
    private static byte[] byteData = null;
    private static int result = 11;


    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mp != null && !mp.isPlaying() && PlaybackManager.goAhead) {
                PlaybackManager.playNext(true);
            } else {
                PlaybackManager.playPauseEvent(false, false, mp.getCurrentPosition());
            }
        }
    };

    private AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {

                case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                    player.setVolume(0.2f, 0.2f);
                    break;
                case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                    PlaybackManager.playPauseEvent(false, player.isPlaying(), player.getCurrentPosition());
                    break;
                case (AudioManager.AUDIOFOCUS_LOSS):
                    PlaybackManager.playPauseEvent(false, player.isPlaying(), player.getCurrentPosition());
                    break;
                case (AudioManager.AUDIOFOCUS_GAIN):
                    player.setVolume(1f, 1f);
                    PlaybackManager.playPauseEvent(false, false, player.getCurrentPosition());
                    break;
                default:
                    break;

            }

        }
    };

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                if (player.isPlaying()) {
                    PlaybackManager.playPauseEvent(false, true, -1);
                }
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (player != null && !player.isPlaying()) {
                    PlaybackManager.playPauseEvent(false, false, player.getCurrentPosition());
                }
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                if (player.isPlaying()) {
                    PlaybackManager.playPauseEvent(false, true, -1);
                }
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        notificationHandler = NotificationHandler.getInstance(mContext);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        player = new MediaPlayer();
        player.setOnCompletionListener(onCompletionListener);

        try {
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mContext.getSystemService(Context.TELEPHONY_SERVICE);
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
            IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            receiver = new NotificationHandler.NotifBtnClickReceiver();
            registerReceiver(receiver, receiverFilter);
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
        performAction(intent);
        return Service.START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        onDestroy();
    }

    @Override
    public void onDestroy() {
        PlaybackManager.goAhead = true;
        player.stop();
        player.release();
        player = null;
        unregisterReceiver(receiver);
        notificationHandler.onServiceDestroy();
        PlaybackManager.onStopService();
    }

    @Override
    public void onLowMemory() {

    }

    public void performAction(final Intent intent) {
        final String action = intent.getAction();
        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (action) {
                    case ACTION_PLAY:
                        data = intent.getStringExtra(MainActivity.SONG_PATH);
                        title = intent.getStringExtra(MainActivity.SONG_TITLE);
                        artist = intent.getStringExtra(MainActivity.ARTIST_NAME);
                        album = intent.getStringExtra(MainActivity.ALBUM_NAME);
                        try {
                            player.reset();
                            player.setDataSource(data);
                            player.prepare();
                            result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC,
                                    AudioManager.AUDIOFOCUS_GAIN);
                            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                player.start();
                                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                mmr.setDataSource(data);
                                byteData = mmr.getEmbeddedPicture();
                                notificationHandler.showNotif(byteData, title, artist, album, true);
                                PlaybackManager.goAhead = true;
                            }
                        } catch (IOException e) {
                            PlaybackManager.goAhead = true;
                            e.printStackTrace();
                        }
                        break;
                    case ACTION_PAUSE:
                        if (player.isPlaying()) {
                            player.pause();
                            notificationHandler.showNotif(byteData, title, artist, album, false);
                            PlaybackManager.goAhead = true;
                        }
                        break;
                    case ACTION_RESUME:
                        if (player != null) {
                            result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC,
                                    AudioManager.AUDIOFOCUS_GAIN);
                            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                player.start();
                                notificationHandler.showNotif(byteData, title, artist, album, true);
                                PlaybackManager.goAhead = true;
                            }
                        }
                        break;
                    case ACTION_STOP:
                        if (player != null) {
                            PlaybackManager.goAhead = true;
                            stopSelf();
                        }
                        break;
                    case ACTION_SEEK:
                        final int seekTo = intent.getIntExtra("seekTo", 0);
                        data = intent.getStringExtra(MainActivity.SONG_PATH);
                        title = intent.getStringExtra(MainActivity.SONG_TITLE);
                        artist = intent.getStringExtra(MainActivity.ARTIST_NAME);
                        album = intent.getStringExtra(MainActivity.ALBUM_NAME);
                        boolean resume = intent.getBooleanExtra("resume", false);
                        if (player != null) {
                            try {
                                result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC,
                                        AudioManager.AUDIOFOCUS_GAIN);
                                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                    if (!android.text.TextUtils.isEmpty(data) && resume) {
                                        player.reset();
                                        player.setDataSource(data);
                                        player.prepare();
                                        if (byteData == null) {
                                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                            mmr.setDataSource(data);
                                            byteData = mmr.getEmbeddedPicture();
                                        }
                                    }
                                    player.seekTo(seekTo);
                                    player.start();
                                    notificationHandler.showNotif(byteData, title, artist, album, true);
                                }
                                PlaybackManager.goAhead = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        }).start();
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
