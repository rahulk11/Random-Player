package net.trellisys.audioplayer;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import static net.trellisys.audioplayer.MainActivity.ALBUM_NAME;
import static net.trellisys.audioplayer.MainActivity.ARTIST_NAME;
import static net.trellisys.audioplayer.MainActivity.SONG_DURATION;
import static net.trellisys.audioplayer.MainActivity.SONG_ID;
import static net.trellisys.audioplayer.MainActivity.SONG_PATH;
import static net.trellisys.audioplayer.MainActivity.SONG_POS;
import static net.trellisys.audioplayer.MainActivity.SONG_TITLE;

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
        if(loadSongListener!=null)
            this.loadSongListener = loadSongListener;
        if(allsongsuri!=null)
            this.allsongsuri = allsongsuri;
        createPlayList();
    }

//
//    private FilenameFilter filenameFilter = new FilenameFilter() {
//        @Override
//        public boolean accept(File dir, String name) {
//            return (name.endsWith(".mp3") || name.endsWith(".MP3"));
//        }
//    };

    private LoadSongListener loadSongListener = new LoadSongListener() {

        @Override
        public void onDoneLoading() {
//            if(mContext.getClass().getSimpleName().equals("MainActivity"))
                ((MainActivity)mContext).setAllSongs();
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

                        song.put("songTitle", song_title);
                        song.put("displayName", display_name);
                        song.put("songID", "" + song_id);
                        song.put("songPath", song_path);
                        song.put("albumName", album_name);
                        song.put("artistName", artist_name);
                        song.put("songDuration", "" + song_duration);

                        songsList.add(song);

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

    public static void playSong(String path, String title, String artist){
        Intent i = new Intent(mContext, SongService.class);
        i.setAction(SongService.ACTION_PLAY);
        i.putExtra("path", path);
        i.putExtra("songTitle", title);
        i.putExtra("songArtist", artist);
        mContext.startService(i);
    }

    public static void playNext(boolean isShuffle){

        int pos = Integer.parseInt(getLastPlayingSongPref().get(SONG_POS));
        pos += 1;
        if(pos>-1 && pos<songsList.size()){
            HashMap<String, String> hashMap = songsList.get(pos);
            ((MainActivity)mContext).loadSongInfo(hashMap);
            playSong(hashMap.get(SONG_PATH),
                    hashMap.get(SONG_TITLE),
                    hashMap.get(ARTIST_NAME));
        }
    }

    public static void playPrev(){
        int pos = Integer.parseInt(getLastPlayingSongPref().get(SONG_POS));
        pos -= 1;
        if(pos>-1 && pos<songsList.size()){
            HashMap<String, String> hashMap = songsList.get(pos);
            ((MainActivity)mContext).loadSongInfo(hashMap);
            playSong(hashMap.get(SONG_PATH),
                    hashMap.get(SONG_TITLE),
                    hashMap.get(ARTIST_NAME));
        }
    }

    public void setLastPlayingSongPref(HashMap<String, String> songDetail) {
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString(SONG_TITLE, songDetail.get(SONG_TITLE))
                .putString(SONG_ID, songDetail.get(SONG_ID))
                .putString(ARTIST_NAME, songDetail.get(ARTIST_NAME))
                .putString(ALBUM_NAME, songDetail.get(ALBUM_NAME))
                .putString(SONG_DURATION, songDetail.get(SONG_DURATION))
                .putString(SONG_PATH, songDetail.get(SONG_PATH))
                .putString(SONG_POS, songDetail.get(SONG_POS));

        prefEditor.commit();
    }

    public static HashMap<String, String>  getLastPlayingSongPref() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(SONG_TITLE, sharedPref.getString(SONG_TITLE, ""));
        hashMap.put(SONG_ID, sharedPref.getString(SONG_ID, ""));
        hashMap.put(ARTIST_NAME, sharedPref.getString(ARTIST_NAME, ""));
        hashMap.put(ALBUM_NAME, sharedPref.getString(ALBUM_NAME, ""));
        hashMap.put(SONG_DURATION, sharedPref.getString(SONG_DURATION, ""+0));
        hashMap.put(SONG_PATH, sharedPref.getString(SONG_PATH, ""));
        hashMap.put(SONG_POS, sharedPref.getString(SONG_POS, ""+(-1)));
        return hashMap;
    }

    private static int shufflePos(){
        return 0;
    }

    public interface LoadSongListener {
        public void onDoneLoading();
    }

}
