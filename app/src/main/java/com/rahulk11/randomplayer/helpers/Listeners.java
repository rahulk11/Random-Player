package com.rahulk11.randomplayer.helpers;

import android.graphics.Bitmap;
import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rahul Kumar on 6/29/2017.
 */

public class Listeners {

    public interface LoadSongListener {
        void onSongLoaded(ArrayList<SongData> songsList);
    }

    public interface LoadImageListener {
        void onImageLoaded(BitmapPalette bitmapPalette);
    }

    public interface MediaPlayerListener {
        void onMediaPlayerStarted(MediaPlayer mp);
    }
}
