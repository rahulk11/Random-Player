package com.rahulk11.audioplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

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

    public PlaybackManager(Context mContext) {
        this.mContext = mContext;
        sharedPref = mContext.getSharedPreferences(songPref, mContext.MODE_PRIVATE);
        createPlayList();
    }

    public PlaybackManager(Context mContext, LoadSongListener loadSongListener, Uri allsongsuri) {
        this.mContext = mContext;
        sharedPref = mContext.getSharedPreferences(songPref, mContext.MODE_PRIVATE);
        if (loadSongListener != null)
            this.loadSongListener = loadSongListener;
        if (allsongsuri != null)
            this.allsongsuri = allsongsuri;
        createPlayList();
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
        ((MainActivity) mContext).setPlayPauseView(false);
    }

    public static boolean playPauseEvent(boolean headphone) {
        if (headphone || SongService.isPlaying()) {
            ((MainActivity) mContext).setPlayPauseView(false);
            mContext.startService(
                    new Intent(mContext, SongService.class).setAction(SongService.ACTION_PAUSE));
            return false;
        } else {
            if (!PlaybackManager.getLastPlayingSongPref().get(MainActivity.SONG_ID).equals("")) {
                ((MainActivity) mContext).setPlayPauseView(true);
                mContext.startService(
                        new Intent(mContext, SongService.class).setAction(SongService.ACTION_RESUME));
                return true;
            }
        }
        return false;
    }

    public static void playSong(String path, String title, String artist, String album) {
        Intent i = new Intent(mContext, SongService.class);
        i.setAction(SongService.ACTION_PLAY);
        i.putExtra("path", path);
        i.putExtra("songTitle", title);
        i.putExtra("songArtist", artist);
        i.putExtra("songAlbum", album);
        mContext.startService(i);

    }

    public static void seekTo(int pos) {
        Intent i = new Intent(mContext, SongService.class);
        i.setAction(SongService.ACTION_SEEK);
        i.putExtra("seekTo", pos);
        mContext.startService(i);
    }

    public static void playNext(boolean isShuffle) {
        MainActivity.shouldContinue = false;
        int pos = Integer.parseInt(getLastPlayingSongPref().get(MainActivity.SONG_POS));
        if (isShuffle) {
            pos = shufflePos(0);
        } else pos += 1;
        if (pos > -1 && pos < songsList.size()) {
            HashMap<String, String> hashMap = songsList.get(pos);
            ((MainActivity) mContext).loadSongInfo(hashMap, true);
            playSong(hashMap.get(MainActivity.SONG_PATH),
                    hashMap.get(MainActivity.SONG_TITLE),
                    hashMap.get(MainActivity.ARTIST_NAME),
                    hashMap.get(MainActivity.ALBUM_NAME));

        }


    }

    public static void playPrev(boolean isShuffle) {
        MainActivity.shouldContinue = false;
        int pos = Integer.parseInt(getLastPlayingSongPref().get(MainActivity.SONG_POS));
        if (isShuffle && shufflePosList.contains(pos)) {
            int index = shufflePosList.indexOf(pos);
            if (index != 0)
                pos = shufflePosList.get(index - 1);
        } else pos -= 1;
        if (pos > -1 && pos < songsList.size()) {
            HashMap<String, String> hashMap = songsList.get(pos);
            ((MainActivity) mContext).loadSongInfo(hashMap, true);
            playSong(hashMap.get(MainActivity.SONG_PATH),
                    hashMap.get(MainActivity.SONG_TITLE),
                    hashMap.get(MainActivity.ARTIST_NAME),
                    hashMap.get(MainActivity.ALBUM_NAME));
        }
    }

    public void setLastPlayingSongPref(HashMap<String, String> songDetail) {
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

    public static HashMap<String, String> getLastPlayingSongPref() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(MainActivity.SONG_TITLE, sharedPref.getString(MainActivity.SONG_TITLE, ""));
        hashMap.put(MainActivity.SONG_ID, sharedPref.getString(MainActivity.SONG_ID, ""));
        hashMap.put(MainActivity.ARTIST_NAME, sharedPref.getString(MainActivity.ARTIST_NAME, ""));
        hashMap.put(MainActivity.ALBUM_NAME, sharedPref.getString(MainActivity.ALBUM_NAME, ""));
        hashMap.put(MainActivity.SONG_DURATION, sharedPref.getString(MainActivity.SONG_DURATION, "" + 0));
        hashMap.put(MainActivity.SONG_PATH, sharedPref.getString(MainActivity.SONG_PATH, ""));
        hashMap.put(MainActivity.SONG_POS, sharedPref.getString(MainActivity.SONG_POS, -1 + ""));
        return hashMap;
    }

    private static int shufflePos(int i) {
        int min = 0, max = songsList.size();
        int range = (max - min) + 1;
        int shuffledPos = (int) (Math.random() * range) + min;
        if (shufflePosList.contains(shuffledPos) && i < 3) {
            shuffledPos = shufflePos(++i);
        } else if (i >= 3) {
//            shufflePosList = new ArrayList<>();
//            shufflePosList.add(shuffledPos);
            return shuffledPos;
        }
        shufflePosList.add(shuffledPos);
        return shuffledPos;
    }

    public interface LoadSongListener {
        public void onDoneLoading();
    }

}
