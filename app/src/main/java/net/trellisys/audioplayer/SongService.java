package net.trellisys.audioplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

import static net.trellisys.audioplayer.MainActivity.ALBUM_NAME;
import static net.trellisys.audioplayer.MainActivity.ARTIST_NAME;
import static net.trellisys.audioplayer.MainActivity.SONG_DURATION;
import static net.trellisys.audioplayer.MainActivity.SONG_ID;
import static net.trellisys.audioplayer.MainActivity.SONG_PATH;
import static net.trellisys.audioplayer.MainActivity.SONG_POS;
import static net.trellisys.audioplayer.MainActivity.SONG_TITLE;
import static net.trellisys.audioplayer.PlaybackManager.songsList;

/**
 * Created by rahul on 6/9/2017.
 */

public class SongService extends Service {
    public final static String ACTION_PLAY = "PLAY";
    public final static String ACTION_PAUSE = "PAUSE";
    public final static String ACTION_RESUME = "RESUME";
    private AudioManager audioManager;
    private PhoneStateListener phoneStateListener;
    private static MediaPlayer player;
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                PlaybackManager.playNext(false);
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

                    } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {

                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            };
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
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
        if (action.equals(ACTION_PLAY)){
            String data = intent.getStringExtra("path");
            try {
                player.reset();
                player.setDataSource(data);
                player.prepare();
                player.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (action.equals(ACTION_PAUSE)){
            if(player.isPlaying()){
                player.pause();
            }
        } else if (action.equals(ACTION_RESUME)){
            if(player!=null){
                player.start();
            }
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
        PlaybackManager.songsList.clear();
        stopSelf();
    }

    @Override
    public void onLowMemory() {

    }


    public static boolean isPlaying(){
        if(player!=null)
            return player.isPlaying();
        return false;
    }

}
