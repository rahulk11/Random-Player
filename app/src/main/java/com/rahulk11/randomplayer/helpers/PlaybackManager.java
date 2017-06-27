package com.rahulk11.randomplayer.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.rahulk11.randomplayer.MainActivity;
import com.rahulk11.randomplayer.SongService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rahul on 5/31/2017.
 */

public class PlaybackManager {

    private Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private String[] projectionSongs = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION};
    private String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    public static ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    public static ArrayList<Integer> shufflePosList = new ArrayList<>();
    public static final String songPref = "songPref";
    private static Context mContext;
    private static SharedPreferences sharedPref;

    private static PlaybackManager playbackManager;

    private PlaybackManager(Context mContext) {
        this.mContext = mContext;
        sharedPref = mContext.getSharedPreferences(songPref, mContext.MODE_PRIVATE);
        createPlayList();
    }

    private PlaybackManager(Context mContext, LoadSongListener loadSongListener, Uri allsongsuri) {
        this.mContext = mContext;
        sharedPref = mContext.getSharedPreferences(songPref, mContext.MODE_PRIVATE);
        if (loadSongListener != null)
            this.loadSongListener = loadSongListener;
        if (allsongsuri != null)
            this.allsongsuri = allsongsuri;
        createPlayList();
    }

    public static PlaybackManager getInstance(Context mContext) {
        playbackManager = null;
        songsList.clear();
        shufflePosList.clear();
        playbackManager = new PlaybackManager(mContext);
        return playbackManager;
    }

    private LoadSongListener loadSongListener = new LoadSongListener() {
        @Override
        public void onDoneLoading() {
//            if(mContext.getClass().getSimpleName().equals("MainActivity"))
            ((MainActivity) mContext).setAllSongs();
        }
    };

    private void createPlayList() {
        new LoadSongsAsync().execute();
    }

    private class LoadSongsAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Cursor cursor = mContext.getContentResolver().query(allsongsuri, projectionSongs,
                    selection, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int i = 0;
                    do {
                        HashMap<String, String> song = new HashMap<String, String>();
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

                        song.put(MainActivity.SONG_TITLE, song_title);
                        song.put(MainActivity.DISPLAY_NAME, display_name);
                        song.put(MainActivity.SONG_ID, "" + song_id);
                        song.put(MainActivity.SONG_PATH, song_path);
                        song.put(MainActivity.ALBUM_NAME, album_name);
                        song.put(MainActivity.ARTIST_NAME, artist_name);
                        song.put(MainActivity.SONG_DURATION, "" + song_duration);
                        song.put(MainActivity.SONG_POS, "" + i);
                        songsList.add(song);
                        ++i;
                    } while (cursor.moveToNext());

                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadSongListener.onDoneLoading();
        }
    }

    public static void stopService() {
        mContext.startService(
                new Intent(mContext, SongService.class).setAction(SongService.ACTION_STOP));
    }

    public static void onStopService(){
        ((MainActivity) mContext).setPlayPauseView(false);
    }

    public static boolean playPauseEvent(boolean headphone, boolean isPlaying, int seekProgress) {
        if (headphone || isPlaying) {
            goAhead = false;
            ((MainActivity) mContext).setPlayPauseView(false);
            mContext.startService(
                    new Intent(mContext, SongService.class).setAction(SongService.ACTION_PAUSE));
            return false;
        } else {
            HashMap<String, String> hashMap = getPlayingSongPref();
            if (hashMap!=null && !hashMap.get(MainActivity.SONG_ID).equals("")) {
                ((MainActivity) mContext).setPlayPauseView(true);
                if (seekProgress != -1)
                    seekTo(seekProgress, hashMap);
                else playSong(hashMap);
                return true;
            }
        }
        return false;
    }

    public static boolean goAhead = true;

    public static void playSong(final HashMap<String, String> hashMap) {
        goAhead = false;
        setPlayingSongPref(hashMap);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(mContext, SongService.class);
                i.setAction(SongService.ACTION_PLAY);
                i.putExtra(MainActivity.SONG_PATH, hashMap.get(MainActivity.SONG_PATH));
                i.putExtra(MainActivity.SONG_TITLE, hashMap.get(MainActivity.SONG_TITLE));
                i.putExtra(MainActivity.ARTIST_NAME, hashMap.get(MainActivity.ARTIST_NAME));
                i.putExtra(MainActivity.ALBUM_NAME, hashMap.get(MainActivity.ALBUM_NAME));
                mContext.startService(i);
            }
        }).start();
        ((MainActivity) mContext).setPlayPauseView(true);
    }

    private static void seekTo(final int progress, final HashMap<String, String> hashMap) {
        if (goAhead) {
            goAhead = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(mContext, SongService.class);
                    if (hashMap != null) {
                        i.putExtra(MainActivity.SONG_PATH, hashMap.get(MainActivity.SONG_PATH));
                        i.putExtra(MainActivity.SONG_TITLE, hashMap.get(MainActivity.SONG_TITLE));
                        i.putExtra(MainActivity.ARTIST_NAME, hashMap.get(MainActivity.ARTIST_NAME));
                        i.putExtra(MainActivity.ALBUM_NAME, hashMap.get(MainActivity.ALBUM_NAME));
                    }
                    i.setAction(SongService.ACTION_SEEK);
                    i.putExtra("seekTo", progress);
                    if(progress==0) i.putExtra("resume", true);
                    else i.putExtra("resume", false);
                    mContext.startService(i);
                }
            }).start();
        }
    }

    public static void playNext(boolean isShuffle) {
        if (goAhead) {
            goAhead = false;
            MainActivity.shouldContinue = false;
            int pos = Integer.parseInt(getPlayingSongPref().get(MainActivity.SONG_POS));
            if (isShuffle) {
                if (shufflePosList.contains(pos)) {
                    int index = shufflePosList.indexOf(pos);
                    if (index < (shufflePosList.size() - 1))
                        pos = shufflePosList.get(++index);
                    else pos = shufflePos();
                } else pos = shufflePos();
            } else pos += 1;

            if (pos > -1 && pos < songsList.size()) {
                HashMap<String, String> hashMap = songsList.get(pos);
                ((MainActivity) mContext).loadSongInfo(hashMap, true);
                playSong(hashMap);
            }
        }
    }

    public static void playPrev(boolean isShuffle) {
        if (goAhead) {
            goAhead = false;
            MainActivity.shouldContinue = false;
            int pos = Integer.parseInt(getPlayingSongPref().get(MainActivity.SONG_POS));
            if (isShuffle && shufflePosList.contains(pos)) {
                int index = shufflePosList.indexOf(pos);
                if (index != 0) pos = shufflePosList.get(--index);
                else {
                    pos = shufflePos();
                }
            } else pos -= 1;
            if (pos > -1 && pos < songsList.size()) {
                HashMap<String, String> hashMap = songsList.get(pos);
                ((MainActivity) mContext).loadSongInfo(hashMap, true);
                playSong(hashMap);
            }
        }
    }

    private static void setPlayingSongPref(final HashMap<String, String> songDetail) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putString(MainActivity.SONG_TITLE, songDetail.get(MainActivity.SONG_TITLE))
                        .putString(MainActivity.SONG_ID, songDetail.get(MainActivity.SONG_ID))
                        .putString(MainActivity.ARTIST_NAME, songDetail.get(MainActivity.ARTIST_NAME))
                        .putString(MainActivity.ALBUM_NAME, songDetail.get(MainActivity.ALBUM_NAME))
                        .putString(MainActivity.SONG_DURATION, songDetail.get(MainActivity.SONG_DURATION))
                        .putString(MainActivity.SONG_PATH, songDetail.get(MainActivity.SONG_PATH))
                        .putString(MainActivity.SONG_POS, songDetail.get(MainActivity.SONG_POS));

                prefEditor.commit();
            }
        }).start();
    }

    public static HashMap<String, String> getPlayingSongPref() {
        HashMap<String, String> hashMap = new HashMap<>();
        if (sharedPref != null) {
            hashMap.put(MainActivity.SONG_TITLE, sharedPref.getString(MainActivity.SONG_TITLE, ""));
            hashMap.put(MainActivity.SONG_ID, sharedPref.getString(MainActivity.SONG_ID, ""));
            hashMap.put(MainActivity.ARTIST_NAME, sharedPref.getString(MainActivity.ARTIST_NAME, ""));
            hashMap.put(MainActivity.ALBUM_NAME, sharedPref.getString(MainActivity.ALBUM_NAME, ""));
            hashMap.put(MainActivity.SONG_DURATION, sharedPref.getString(MainActivity.SONG_DURATION, "" + 0));
            hashMap.put(MainActivity.SONG_PATH, sharedPref.getString(MainActivity.SONG_PATH, ""));
            hashMap.put(MainActivity.SONG_POS, sharedPref.getString(MainActivity.SONG_POS, -1 + ""));
        }
        return hashMap;
    }

    private static int shufflePos() {

        int min = 0, max = (songsList.size() - 1);
        int range = (max - min) + 1;
        int shuffledPos = (int) (Math.random() * range) + min;
        if (!shufflePosList.contains(shuffledPos)) {
            shufflePosList.add(shuffledPos);
        }
        if (shuffledPos != Integer.parseInt(getPlayingSongPref().get(MainActivity.SONG_POS)))
            return shuffledPos;
        else return shufflePos();
    }

    public interface LoadSongListener {
        public void onDoneLoading();
    }

}
