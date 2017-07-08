package com.rahulk11.randomplayer.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.service.notification.NotificationListenerService;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;

import com.rahulk11.randomplayer.MainActivity;
import com.rahulk11.randomplayer.SongService;

import java.util.List;

/**
 * Created by rahul on 6/27/2017.
 */

public class BitmapPalette {
    public static Bitmap bitmap;
    public static int dominantRGBColor = Color.DKGRAY,
            dominantTitleTextColor = Color.WHITE,
            dominantBodyTextColor = Color.LTGRAY;

    public static int vibrantRGBColor = Color.TRANSPARENT,
            vibrantTitleTextColor = Color.WHITE,
            vibrantBodyTextColor = Color.LTGRAY;

    public static int darkVibrantRGBColor = Color.TRANSPARENT,
            darkVibrantTitleTextColor = Color.WHITE,
            darkVibrantBodyTextColor = Color.LTGRAY;

    public static int darkMutedRGBColor = Color.DKGRAY,
            darkMutedTitleTextColor = Color.WHITE,
            darkMutedBodyTextColor = Color.LTGRAY;


    public static Palette createPaletteSync(Bitmap bitmap) {
        Palette p = Palette.from(bitmap).generate();
        return p;
    }

    public static Palette.Swatch checkDominantSwatch(Palette p) {
        Palette.Swatch vibrant = p.getDominantSwatch();
        return vibrant;
    }

    public static Palette.Swatch checkVibrantSwatch(Palette p) {
        Palette.Swatch vibrant = p.getVibrantSwatch();
        if (vibrant == null) {
            return checkDominantSwatch(p);
        } else return vibrant;
    }

    public static Palette.Swatch checkDarkVibrantSwatch(Palette p) {
        Palette.Swatch darkVibrant = p.getDarkVibrantSwatch();
        if (darkVibrant == null) {
            return checkVibrantSwatch(p);
        } else return darkVibrant;
    }

    public static Palette.Swatch checkDarkMutedSwatch(Palette p) {
        Palette.Swatch darkMuted = p.getDarkMutedSwatch();
        return darkMuted;
    }

    public static List<Palette.Swatch> checkAllSwatches(Palette p) {
        List<Palette.Swatch> swatchList = p.getSwatches();
        return swatchList;
    }

    private static Listeners.LoadImageListener loadImageListener = new Listeners.LoadImageListener() {
        @Override
        public void onImageLoaded() {
            PlaybackManager.showNotif();
            PlaybackManager.isFirstLoad = false;
        }
    };

    public static void getColorsFromBitmap(final Context context, final String path, final boolean isNotif) {
        new AsyncTask<Void, Void, Void>(){
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            @Override
            protected Void doInBackground(Void... params) {
                if (!path.equals("") && path != null) {
                    try {
                        mmr.setDataSource(path);
                        byte[] byteData = mmr.getEmbeddedPicture();
                        if (byteData != null) {
                            bitmap = BitmapPalette.getBitmap(context, byteData, isNotif);
                        } else bitmap = null;
                        mmr.release();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        mmr.release();
                    }
                }
                if(bitmap!=null){
                    Palette palette = createPaletteSync(bitmap);
                    Palette.Swatch dominantSwatch = checkDominantSwatch(palette);
                    Palette.Swatch vibrantSwatch = checkVibrantSwatch(palette);
                    Palette.Swatch darkVibrantSwatch = checkDarkVibrantSwatch(palette);
                    Palette.Swatch darkMutedSwatch = checkDarkMutedSwatch(palette);

                    dominantRGBColor = dominantSwatch.getRgb();
                    dominantTitleTextColor = dominantSwatch.getTitleTextColor();
                    dominantBodyTextColor = dominantSwatch.getBodyTextColor();

                    vibrantRGBColor = vibrantSwatch.getRgb();
                    vibrantTitleTextColor = vibrantSwatch.getTitleTextColor();
                    vibrantBodyTextColor = vibrantSwatch.getBodyTextColor();

                    darkVibrantRGBColor = darkVibrantSwatch.getRgb();
                    darkVibrantTitleTextColor = darkVibrantSwatch.getTitleTextColor();
                    darkVibrantBodyTextColor = darkVibrantSwatch.getBodyTextColor();

                    if (darkMutedSwatch != null) {
                        darkMutedRGBColor = darkMutedSwatch.getRgb();
                        darkMutedTitleTextColor = darkMutedSwatch.getTitleTextColor();
                        darkMutedBodyTextColor = darkMutedSwatch.getBodyTextColor();
                    }
                } else{
                    resetColors();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                loadImageListener.onImageLoaded();
            }

        }.execute();

    }

    private static void resetColors(){
        dominantRGBColor = Color.DKGRAY;
        dominantTitleTextColor = Color.WHITE;
        dominantBodyTextColor = Color.LTGRAY;

        vibrantRGBColor = Color.TRANSPARENT;
        vibrantTitleTextColor = Color.WHITE;
        vibrantBodyTextColor = Color.LTGRAY;

        darkVibrantRGBColor = Color.TRANSPARENT;
        darkVibrantTitleTextColor = Color.WHITE;
        darkVibrantBodyTextColor = Color.LTGRAY;

        darkMutedRGBColor = Color.DKGRAY;
        darkMutedTitleTextColor = Color.WHITE;
        darkMutedBodyTextColor = Color.LTGRAY;
    }

    public static Bitmap getBitmap(Context context, byte[] byteCoverArt, boolean isNotif) {

        int pixels = isNotif ? calculatePixels(10, context) : calculatePixels(35, context);
//        int pixels = calculatePixels(30, context);

        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeByteArray(byteCoverArt, 0, byteCoverArt.length, options);

        options.inSampleSize = calculateInSampleSize(options, pixels, pixels);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(byteCoverArt, 0, byteCoverArt.length, options);
    }

    public static int calculatePixels(int dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = dp * ((Integer) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
