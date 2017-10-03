package com.rahulk11.randomplayer.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;

import com.rahulk11.randomplayer.R;

import java.util.List;

/**
 * Created by rahul on 6/27/2017.
 */

public class BitmapPalette {
    private Bitmap normalBitmap, blurredBitmap;
    private int dominantRGBColor = Color.DKGRAY,
            dominantTitleTextColor = Color.WHITE,
            dominantBodyTextColor = Color.LTGRAY;

    private int vibrantRGBColor = Color.TRANSPARENT,
            vibrantTitleTextColor = Color.WHITE,
            vibrantBodyTextColor = Color.LTGRAY;

    private int darkVibrantRGBColor = Color.TRANSPARENT,
            darkVibrantTitleTextColor = Color.WHITE,
            darkVibrantBodyTextColor = Color.LTGRAY;

    private int darkMutedRGBColor = Color.DKGRAY,
            darkMutedTitleTextColor = Color.WHITE,
            darkMutedBodyTextColor = Color.LTGRAY;

    private Listeners.LoadImageListener loadImageListener;
    private Context context;
    private String songPath = "";
    private boolean isNotif = false;
    private BitmapPalette bitmapPalette;

    public BitmapPalette(Context context, Listeners.LoadImageListener loadImageListener) {

        this.context = context;
        this.loadImageListener = loadImageListener;
        bitmapPalette = this;
    }

    public void generateImageAndColors(String path, boolean isNotif) {
        this.songPath = path;
        this.isNotif = isNotif;
        new AsyncTask<Void, Void, Void>() {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();

            @Override
            protected Void doInBackground(Void... params) {
                if (songPath != null && !songPath.equals("")) {
                    try {
                        mmr.setDataSource(songPath);
                        byte[] byteData = mmr.getEmbeddedPicture();
                        if (byteData != null) {
                            normalBitmap = getBitmap(byteData);
                        } else {
                            normalBitmap = BitmapFactory.decodeResource(context.getResources(),
                                    R.drawable.random);
                        }
                        mmr.release();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        mmr.release();
                    }
                } else {
                    normalBitmap = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.random);
                }
                if (normalBitmap == null) {
                    normalBitmap = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.random);
                }
                if (normalBitmap != null && !normalBitmap.isRecycled()) {
                    Bitmap.Config config = normalBitmap.getConfig();

                    blurredBitmap = normalBitmap.copy((config != null) ?
                            config : Bitmap.Config.ARGB_8888, false);

                    generateBlurredBitmap(context, blurredBitmap);
                    Palette palette = createPaletteSync(normalBitmap);
                    Palette.Swatch dominantSwatch = checkDominantSwatch(palette);
//                    Palette.Swatch vibrantSwatch = checkVibrantSwatch(palette);
                    Palette.Swatch darkVibrantSwatch = checkDarkVibrantSwatch(palette);
//                    Palette.Swatch darkMutedSwatch = checkDarkMutedSwatch(palette);

                    dominantRGBColor = dominantSwatch.getRgb();
                    dominantTitleTextColor = dominantSwatch.getTitleTextColor();
                    dominantBodyTextColor = dominantSwatch.getBodyTextColor();

//                    vibrantRGBColor = vibrantSwatch.getRgb();
//                    vibrantTitleTextColor = vibrantSwatch.getTitleTextColor();
//                    vibrantBodyTextColor = vibrantSwatch.getBodyTextColor();
//
                    darkVibrantRGBColor = darkVibrantSwatch.getRgb();
                    darkVibrantTitleTextColor = darkVibrantSwatch.getTitleTextColor();
                    darkVibrantBodyTextColor = darkVibrantSwatch.getBodyTextColor();
//
//                    if (darkMutedSwatch != null) {
//                        darkMutedRGBColor = darkMutedSwatch.getRgb();
//                        darkMutedTitleTextColor = darkMutedSwatch.getTitleTextColor();
//                        darkMutedBodyTextColor = darkMutedSwatch.getBodyTextColor();
//                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                PlayerConstants.ALBUM_ART = normalBitmap;
                loadImageListener.onImageLoaded(bitmapPalette);
            }

        }.execute();

    }

    private void generateBlurredBitmap(Context context, Bitmap bitmap) {
        RenderScript rs = RenderScript.create(context);


        final Allocation input = Allocation.createFromBitmap(rs, bitmap); //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(25f);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(blurredBitmap);
    }

    private Palette createPaletteSync(Bitmap bitmap) {
        Palette p = Palette.from(bitmap).generate();
        return p;
    }

    private Palette.Swatch checkDominantSwatch(Palette p) {
        Palette.Swatch vibrant = p.getDominantSwatch();
        return vibrant;
    }

    private Palette.Swatch checkVibrantSwatch(Palette p) {
        Palette.Swatch vibrant = p.getVibrantSwatch();
        if (vibrant == null) {
            return checkDominantSwatch(p);
        } else return vibrant;
    }

    private Palette.Swatch checkDarkVibrantSwatch(Palette p) {
        Palette.Swatch darkVibrant = p.getDarkVibrantSwatch();
        if (darkVibrant == null) {
            return checkVibrantSwatch(p);
        } else return darkVibrant;
    }

    private Palette.Swatch checkDarkMutedSwatch(Palette p) {
        Palette.Swatch darkMuted = p.getDarkMutedSwatch();
        return darkMuted;
    }

    private List<Palette.Swatch> checkAllSwatches(Palette p) {
        List<Palette.Swatch> swatchList = p.getSwatches();
        return swatchList;
    }

    public void onDestroy() {
        resetColors();
        if (normalBitmap != null && !normalBitmap.isRecycled()) {
            normalBitmap = null;
            blurredBitmap = null;
        }
    }

    private void resetColors() {
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

    public Bitmap getBitmap(byte[] byteCoverArt) {

        int pixels = isNotif ? calculatePixels(10, context) : calculatePixels(25, context);
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

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

    public Bitmap getNormalBitmap() {
        return normalBitmap;
    }

    public Bitmap getBlurredBitmap() {
        return blurredBitmap;
    }

    public int getDominantRGBColor() {
        return dominantRGBColor;
    }

    public int getDominantTitleTextColor() {
        return dominantTitleTextColor;
    }

    public int getDominantBodyTextColor() {
        return dominantBodyTextColor;
    }

    public int getVibrantRGBColor() {
        return vibrantRGBColor;
    }

    public int getVibrantTitleTextColor() {
        return vibrantTitleTextColor;
    }

    public int getVibrantBodyTextColor() {
        return vibrantBodyTextColor;
    }

    public int getDarkVibrantRGBColor() {
        return darkVibrantRGBColor;
    }

    public int getDarkVibrantTitleTextColor() {
        return darkVibrantTitleTextColor;
    }

    public int getDarkVibrantBodyTextColor() {
        return darkVibrantBodyTextColor;
    }

    public int getDarkMutedRGBColor() {
        return darkMutedRGBColor;
    }

    public int getDarkMutedTitleTextColor() {
        return darkMutedTitleTextColor;
    }

    public int getDarkMutedBodyTextColor() {
        return darkMutedBodyTextColor;
    }
}
