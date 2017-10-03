package com.rahulk11.randomplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.rahulk11.randomplayer.helpers.Listeners;
import com.rahulk11.randomplayer.helpers.NotificationHandler;
import com.rahulk11.randomplayer.helpers.PlaybackManager;
import com.rahulk11.randomplayer.helpers.SongData;
import com.rahulk11.randomplayer.helpers.UpdateReceiver;

import java.io.IOException;

/**
 * Created by rahul on 6/9/2017.
 */

public class SongService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    public final static String ACTION_PLAY = "PLAY";
    public final static String ACTION_PAUSE = "PAUSE";
    public final static String ACTION_RESUME = "RESUME";
    public final static String ACTION_STOP = "STOP";
    public final static String ACTION_SEEK = "SEEK_TO";
    public final static String UPDATE_NOTIF = "updateNotif";
    private AudioManager audioManager;
    private static MediaPlayer player;
    private static Context mContext;
    String data = "", title = "", artist = "", album = "";
    int seekTo = -1;
    private static UpdateReceiver receiver;
    private NotificationHandler notificationHandler;
    private static int result = 11;
    private boolean isMediaPlayerReset = false;

    private Listeners.MediaPlayerListener mediaPlayerListener = new Listeners.MediaPlayerListener() {
        @Override
        public void onMediaPlayerStarted(MediaPlayer mp) {
            PlaybackManager.mediaPlayerStarted(mp);
        }
    };

    private static AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {

                case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                    player.setVolume(0.2f, 0.2f);
                    break;
                case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                    PlaybackManager.playPauseEvent(false, player.isPlaying(), false, player.getCurrentPosition());
                    break;
                case (AudioManager.AUDIOFOCUS_LOSS):
                    if (PlaybackManager.goAhead)
                        PlaybackManager.playPauseEvent(false, true, false, player.getCurrentPosition());
                    break;
                case (AudioManager.AUDIOFOCUS_GAIN):
                    player.setVolume(1f, 1f);
                    if (!player.isPlaying() && !PlaybackManager.isManuallyPaused)
                        PlaybackManager.playPauseEvent(false, false, true, player.getCurrentPosition());
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
                    PlaybackManager.playPauseEvent(false, true, false, -1);
                }
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (player != null && !player.isPlaying()) {
                    PlaybackManager.playPauseEvent(false, false, false, player.getCurrentPosition());
                }
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                if (player.isPlaying()) {
                    PlaybackManager.playPauseEvent(false, true, false, -1);
                }
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        PlaybackManager.isFirstLoad = false;
        PlaybackManager.isServiceRunning = true;
        notificationHandler = NotificationHandler.getInstance(mContext);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnPreparedListener(this);

        try {
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mContext.getSystemService(Context.TELEPHONY_SERVICE);
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
            IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            receiver = new UpdateReceiver();
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
        stopSelf();
    }

    @Override
    public void onDestroy() {
        PlaybackManager.isServiceRunning = false;
        int currProg = 0;
        if(player!=null){
            currProg = player.getCurrentPosition();
            player.stop();
            player.release();
            player = null;
        }
        if(receiver!=null)
            unregisterReceiver(receiver);
        if(notificationHandler!=null)
            notificationHandler.onServiceDestroy();
        if(audioManager!=null)
            audioManager.abandonAudioFocus(focusChangeListener);
        PlaybackManager.onStopService(currProg);
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
                        data = intent.getStringExtra(SongData.SONG_PATH);
                        title = intent.getStringExtra(SongData.SONG_TITLE);
                        artist = intent.getStringExtra(SongData.ARTIST_NAME);
                        album = intent.getStringExtra(SongData.ALBUM_NAME);
                        try {
                            isMediaPlayerReset = true;
                            player.reset();
                            player.setDataSource(data);
                            result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC,
                                    AudioManager.AUDIOFOCUS_GAIN);
                            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                player.prepareAsync();
                            }
                        } catch (IOException e) {
                            PlaybackManager.goAhead = true;
                            e.printStackTrace();
                        } catch (IllegalStateException e) {
                            PlaybackManager.goAhead = true;
                            e.printStackTrace();
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
//                        PlaybackManager.goAhead = true;
//                        mediaPlayerListener.onMediaPlayerStarted(player);
                        break;
                    case ACTION_PAUSE:
                        if (player.isPlaying()) {
                            player.pause();
                        }
                        notificationHandler.showNotif(title, artist, album, false);
                        PlaybackManager.goAhead = true;
                        break;
                    case ACTION_RESUME:
                        if (player != null) {
                            result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC,
                                    AudioManager.AUDIOFOCUS_GAIN);
                            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                player.start();
                                notificationHandler.showNotif(title, artist, album, true);
                                mediaPlayerListener.onMediaPlayerStarted(player);
                            }
                        }
//                        PlaybackManager.goAhead = true;
                        break;
                    case ACTION_STOP:
                        Log.d("AudioFocus", "State: " + result);
                        if (player != null) {
                            stopSelf();
                        }
                        break;
                    case ACTION_SEEK:
                        seekTo = intent.getIntExtra("seekTo", -1);
                        data = intent.getStringExtra(SongData.SONG_PATH);
                        title = intent.getStringExtra(SongData.SONG_TITLE);
                        artist = intent.getStringExtra(SongData.ARTIST_NAME);
                        album = intent.getStringExtra(SongData.ALBUM_NAME);
                        boolean resume = intent.getBooleanExtra("resume", false);
                        if (player != null) {
                            try {
                                result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC,
                                        AudioManager.AUDIOFOCUS_GAIN);
                                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                    if (!android.text.TextUtils.isEmpty(data) && resume) {
                                        isMediaPlayerReset = true;
                                        player.reset();
                                        player.setDataSource(data);
                                        player.prepareAsync();
                                    } else if(seekTo!=-1){
                                        player.start();
                                        player.seekTo(seekTo);
                                        seekTo = -1;
//                                        notificationHandler.showNotif(title, artist, album, true);
                                        mediaPlayerListener.onMediaPlayerStarted(player);
                                    } else {
                                        player.start();
                                        notificationHandler.showNotif(title, artist, album, true);
                                        mediaPlayerListener.onMediaPlayerStarted(player);
                                    }
//                                    notificationHandler.showNotif(title, artist, album, true);
                                }
                            } catch (IOException e) {
                                PlaybackManager.goAhead = true;
                                e.printStackTrace();
                            } catch (IllegalStateException e) {
                                PlaybackManager.goAhead = true;
                                e.printStackTrace();
                            } catch (RuntimeException e) {
                                PlaybackManager.goAhead = true;
                                e.printStackTrace();
                            }
                        }
                        break;
                    case UPDATE_NOTIF:
                        notificationHandler.updateNotif(title, artist, album, true);
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isMediaPlayerReset) {
            isMediaPlayerReset = false;
            return;
        } else if (mp != null && !mp.isPlaying() && PlaybackManager.goAhead) {
            PlaybackManager.playNext(true);
        } else {
            PlaybackManager.playPauseEvent(false, false, false, mp.getCurrentPosition());
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isMediaPlayerReset = false;
        player.start();
        if(seekTo!=-1)
            player.seekTo(seekTo);
        seekTo = -1;
        notificationHandler.showNotif(title, artist, album, true);
        mediaPlayerListener.onMediaPlayerStarted(player);
    }
}
