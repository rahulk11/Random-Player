package com.rahulk11.randomplayer.helpers;

import android.graphics.Bitmap;

/**
 * Created by absolutezero on 3/9/17.
 */

public class PlayerConstants {

    public static int TITLE_COLOR;
    public static int BODY_COLOR;
    public static int RGB_COLOR;

    public static Bitmap ALBUM_ART;

    public static SongData CURRENT_SONG;

    public static void setPlayerConstants(SongData songData, BitmapPalette bitmapPalette) {
        if (songData != null)
            CURRENT_SONG = songData;

        if (bitmapPalette != null) {
            TITLE_COLOR = bitmapPalette.getDarkVibrantTitleTextColor();
            BODY_COLOR = bitmapPalette.getDarkVibrantBodyTextColor();
            RGB_COLOR = bitmapPalette.getDarkVibrantRGBColor();
        }

    }

}
