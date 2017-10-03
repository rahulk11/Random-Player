package com.rahulk11.randomplayer.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;

import com.rahulk11.randomplayer.MainActivity;
import com.rahulk11.randomplayer.SongService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by rahul on 5/31/2017.
 */

public class PlaybackManager {
    public static final String songPref = "songPref";
    public static boolean isFirstLoad = true;
    private static ArrayList<SongData> songsList = new ArrayList<SongData>();
    public static ArrayList<Integer> shuffledPosList = new ArrayList<Integer>();
    private static int CURR_POS = 0;
    public static boolean isServiceRunning = false, isManuallyPaused = false;
    public static boolean goAhead = true;
    private static Context mContext;
    private static SharedPreferences sharedPref;
    private static PlaybackManager playbackManager;
    private static Handler handler = new Handler();
    private static Runnable runnable = new Runnable() {
        @Override
        public void run() {

        }
    };
    private Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private String[] projectionSongs = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION};
    private String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    private Listeners.LoadSongListener loadSongListener = new Listeners.LoadSongListener() {

        @Override
        public void onSongLoaded(ArrayList<SongData> songsList) {
            ((MainActivity) mContext).setAllSongs(songsList);
        }
    };

    private PlaybackManager(Context mContext) {
        this.mContext = mContext;
        isFirstLoad = true;
        if (songsList == null) {
            songsList = new ArrayList<SongData>();
        }
        sharedPref = mContext.getSharedPreferences(songPref, mContext.MODE_PRIVATE);
        new LoadSongsAsync().execute();
    }

    private PlaybackManager(Context mContext, Listeners.LoadSongListener loadSongListener, Uri allsongsuri) {
        this.mContext = mContext;
        sharedPref = mContext.getSharedPreferences(songPref, mContext.MODE_PRIVATE);
        if (loadSongListener != null)
            this.loadSongListener = loadSongListener;
        if (allsongsuri != null)
            this.allsongsuri = allsongsuri;
        new LoadSongsAsync().execute();
    }

    public static PlaybackManager getInstance(Context mContext) {
        playbackManager = null;
        goAhead = true;
        playbackManager = new PlaybackManager(mContext);
        return playbackManager;
    }

    public boolean ifInstanceNull() {
        if (playbackManager == null)
            return true;
        else return false;
    }

    public static void stopService() {
        mContext.startService(
                new Intent(mContext, SongService.class).setAction(SongService.ACTION_STOP));
    }

    public static void onStopService(int currProg) {
        ((MainActivity) mContext).setPlayPauseView(false);

        SongData songData = getPlayingSongPref();
        songData.setSongProg(currProg);
        setPlayingSongPref(songData);

        songsList.clear();
        shuffledPosList.clear();
        sharedPref = null;

        playbackManager = null;
        isServiceRunning = false;
        goAhead = true;
        isFirstLoad = true;
        isManuallyPaused = false;

        ((MainActivity) mContext).finish();
//        mContext = null;
    }

    public static boolean playPauseEvent(boolean headphone, boolean isPlaying, boolean isResume, int seekProgress) {
        SongData songData = getPlayingSongPref();
        if (headphone || isPlaying) {
            goAhead = false;
            ((MainActivity) mContext).setPlayPauseView(false);
            mContext.startService(
                    new Intent(mContext, SongService.class).setAction(SongService.ACTION_PAUSE));
            songData.setSongProg(SongService.getCurrPos());
            setPlayingSongPref(songData);
            return false;
        } else {
            isManuallyPaused = false;
            if (songData != null && !android.text.TextUtils.isEmpty(songData.getSongPath())) {
                if (seekProgress != -1)
                    seekTo(seekProgress, isResume, songData);
                else playSong(songData);
                return true;
            }
        }

        return false;
    }

    public static void playSong(final SongData songData) {
        goAhead = false;
        isManuallyPaused = false;
        setPlayingSongPref(songData);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(mContext, SongService.class);
                i.setAction(SongService.ACTION_PLAY);
                i.putExtra(SongData.SONG_PATH, songData.getSongPath());
                i.putExtra(SongData.SONG_TITLE, songData.getSongTitle());
                i.putExtra(SongData.ARTIST_NAME, songData.getSongArtist());
                i.putExtra(SongData.ALBUM_NAME, songData.getSongAlbum());
                mContext.startService(i);
//                int pos = Integer.parseInt(hashMap.get(MainActivity.SONG_POS));
//                if (!shufflePosList.contains(pos)) {
//                    shufflePosList.add(pos);
//                }
            }
        }).start();
    }

    private static void seekTo(final int progress, final boolean resume, final SongData songData) {
        if (goAhead) {
            goAhead = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(mContext, SongService.class);
                    if (songData != null) {
                        i.putExtra(SongData.SONG_PATH, songData.getSongPath());
                        i.putExtra(SongData.SONG_TITLE, songData.getSongTitle());
                        i.putExtra(SongData.ARTIST_NAME, songData.getSongArtist());
                        i.putExtra(SongData.ALBUM_NAME, songData.getSongAlbum());
                    }
                    i.setAction(SongService.ACTION_SEEK);
                    i.putExtra("seekTo", progress);
                    if (resume) i.putExtra("resume", true);
                    else i.putExtra("resume", false);
                    mContext.startService(i);
//                    int pos = Integer.parseInt(hashMap.get(MainActivity.SONG_POS));
//                    if (!shufflePosList.contains(pos)) {
//                        shufflePosList.add(pos);
//                    }
                }
            }).start();
        }
    }

    public void playOnClick(int pos) {
        SongData songData = songsList.get(pos);
        CURR_POS = shuffledPosList.indexOf(pos);
        playSong(songData);
    }

    public static void playNext(boolean isShuffle) {
        if (goAhead) {
            goAhead = false;
            MainActivity.shouldContinue = false;
            int i;
            if (isShuffle) {
                i = shuffledPosList.get(++CURR_POS);
                if (i < 0 || i > shuffledPosList.size() - 1) {
                    i = (shuffledPosList.size() - 1) / 2;
                    CURR_POS = i;
                }
            } else {
                i = ++CURR_POS;
            }
            SongData songData = songsList.get(i);
            playSong(songData);
        }
    }

    public static void playPrev(boolean isShuffle) {
        if (goAhead) {
            goAhead = false;
            MainActivity.shouldContinue = false;
            int i = shuffledPosList.get(--CURR_POS);
            if (i < 0 || i > shuffledPosList.size() - 1) {
                i = (shuffledPosList.size() - 1) / 2;
                CURR_POS = i;
            }
            SongData songData = songsList.get(i);
            playSong(songData);
        }
    }

    public static void mediaPlayerStarted(MediaPlayer mp) {
        ((MainActivity) mContext).setSeekProgress();
    }

    public void showNotif(boolean updateColors) {
        if (updateColors)
            ((MainActivity) mContext).setBitmapColors();
        if (!isFirstLoad)
            mContext.startService(new Intent(mContext, SongService.class).setAction(SongService.UPDATE_NOTIF));
    }

    public static SongData getPlayingSongPref() {
        SongData songData = new SongData();
        if (sharedPref == null) {
            sharedPref = mContext.getSharedPreferences(songPref, mContext.MODE_PRIVATE);
        }
        songData.setSongTitle(sharedPref.getString(SongData.SONG_TITLE, ""));
        songData.setSongName(sharedPref.getString(SongData.DISPLAY_NAME, ""));
        songData.setSongId(sharedPref.getInt(SongData.SONG_ID, 0));
        songData.setSongArtist(sharedPref.getString(SongData.ARTIST_NAME, ""));
        songData.setSongAlbum(sharedPref.getString(SongData.ALBUM_NAME, ""));
        songData.setSongDur(sharedPref.getInt(SongData.SONG_DURATION, 0));
        songData.setSongPath(sharedPref.getString(SongData.SONG_PATH, ""));
        songData.setSongProg(sharedPref.getInt(SongData.SONG_PROGRESS, 0));
//      songData.setSongPos(sharedPref.getInt(MainActivity.SONG_POS, 0));

        return songData;
    }

    private static void setPlayingSongPref(final SongData songData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (sharedPref == null) {
                    sharedPref = mContext.getSharedPreferences(songPref, mContext.MODE_PRIVATE);
                }
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putString(SongData.SONG_TITLE, songData.getSongTitle())
                        .putString(SongData.DISPLAY_NAME, songData.getSongName())
                        .putInt(SongData.SONG_ID, songData.getSongId())
                        .putString(SongData.ARTIST_NAME, songData.getSongArtist())
                        .putString(SongData.ALBUM_NAME, songData.getSongAlbum())
                        .putInt(SongData.SONG_DURATION, songData.getSongDur())
                        .putString(SongData.SONG_PATH, songData.getSongPath())
                        .putInt(SongData.SONG_PROGRESS, songData.getSongProg());
//                        .putInt(MainActivity.SONG_POS, songData.getSongPos());
                prefEditor.commit();
            }
        }).start();
    }

    private class LoadSongsAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Cursor cursor = mContext.getContentResolver().query(allsongsuri, projectionSongs,
                    selection, null, null);

            if (cursor != null && songsList.isEmpty()) {
                shuffledPosList.clear();
                if (cursor.moveToFirst()) {
                    SongData songData;
                    int i = 0;
                    do {
                        String song_title = cursor
                                .getString(cursor
                                        .getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String display_name = cursor
                                .getString(cursor
                                        .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        int song_id = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Audio.Media._ID));

                        String song_path = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.DATA));

                        String album_name = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ALBUM));

                        String artist_name = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ARTIST));

                        int song_duration = cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Audio.Media.DURATION));


                        songData = new SongData(song_id, display_name, song_title,
                                artist_name, album_name, song_duration, song_path);
                        songsList.add(songData);
                        shuffledPosList.add(i);
                        i += 1;
                    } while (cursor.moveToNext());

                }
                cursor.close();
                if (songsList != null && !songsList.isEmpty()) {
                    Collections.sort(songsList, new Comparator<SongData>() {
                        @Override
                        public int compare(SongData o1, SongData o2) {
                            return o1.getSongTitle()
                                    .compareToIgnoreCase(o2.getSongTitle());
                        }
                    });
                }
                if (shuffledPosList != null && !shuffledPosList.isEmpty()) {
                    Collections.shuffle(shuffledPosList);
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadSongListener.onSongLoaded(songsList);
        }
    }

}
