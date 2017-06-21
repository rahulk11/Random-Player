package com.rahulk11.audioplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RemoteViews;
import android.support.v7.graphics.Palette;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by rahul on 6/12/2017.
 */

public class NotificationHandler extends Notification {

    private static Context ctx;
    private static NotificationManager mNotificationManager;
    private static final String ACTION_PLAY = "com.rahulk11.audioplayer.ACTION_PLAY";
    private static final String ACTION_NEXT = "com.rahulk11.audioplayer.ACTION_NEXT";
    private static final String ACTION_PREV = "com.rahulk11.audioplayer.ACTION_PREV";
    private static final String ACTION_CLOSE = "com.rahulk11.audioplayer.ACTION_CLOSE";

    private static final int notifID = 54388;
    private RemoteViews notificationView;
    private Notification notification;
    private Canvas canvas, canvasOverlay;
    private Bitmap gradientBitmap, gradientBitmapOverlay;
    private float dp;
    private static boolean isOldLayout = false, staticBool = true;;
    private boolean isFirstTime = true;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private NotificationHandler(Context ctx1) {
        super();
        ctx = ctx1;
        staticBool = this.isFirstTime;
        mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new Notification(R.drawable.play_button, null, System.currentTimeMillis());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            notificationView = new RemoteViews(ctx.getPackageName(), R.layout.notif_old_layout);
            isOldLayout = true;
        } else notificationView = new RemoteViews(ctx.getPackageName(), R.layout.notif_new_layout);
        setListeners();

        Intent notificationIntent = new Intent(ctx, MainActivity.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);

        notification.contentIntent = pendingNotificationIntent;
        notification.flags |= Notification.FLAG_NO_CLEAR;

        dp = ctx.getResources().getDisplayMetrics().density;
//        initGradientBitmap();
    }

    private static NotificationHandler notificationHandler = null;
    public static NotificationHandler getInstance(Context ctx1){
       if(notificationHandler!=null)
           return  notificationHandler;
       else{
           notificationHandler = new NotificationHandler(ctx1);
           return notificationHandler;
       }
    }



    public void setListeners() {

        Intent playPauseIntent = new Intent(ACTION_PLAY);
        PendingIntent pendingPlayPauseIntent = PendingIntent.getBroadcast(ctx, 0, playPauseIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.playNotifBtn, pendingPlayPauseIntent);
        notificationView.setOnClickPendingIntent(R.id.pauseNotifBtn, pendingPlayPauseIntent);

        Intent nextIntent = new Intent(ACTION_NEXT);
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(ctx, 1, nextIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.nextNotifBtn, pendingNextIntent);

        Intent prevIntent = new Intent(ACTION_PREV);
        PendingIntent pendingPrevIntent = PendingIntent.getBroadcast(ctx, 1, prevIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.prevNotifBtn, pendingPrevIntent);

        Intent closeIntent = new Intent(ACTION_CLOSE);
        PendingIntent pendingCloseIntent = PendingIntent.getBroadcast(ctx, 2, closeIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.closeNotifBtn, pendingCloseIntent);
    }

    public static class NotifBtnClickReceiver extends BroadcastReceiver {
        public void NotifBtnClickReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_PLAY:
                    PlaybackManager.playPauseEvent(false, SongService.isPlaying(), SongService.getCurrPos());
                    break;
                case ACTION_NEXT:
                    PlaybackManager.playNext(true);
                    break;
                case ACTION_PREV:
                    PlaybackManager.playPrev(true);
                    break;
                case ACTION_CLOSE:
                    notificationHandler.onServiceDestroy();
                    PlaybackManager.stopService();
                    break;
                case Intent.ACTION_HEADSET_PLUG:
                    //noinspection deprecation
                    if (ctx != null && !staticBool && SongService.isPlaying())
                        if (!((AudioManager) ctx.getSystemService(AUDIO_SERVICE)).isWiredHeadsetOn()) {
                            PlaybackManager.playPauseEvent(true, true, SongService.getCurrPos());
                        }
                    staticBool = false;
                    break;

            }
        }
    }

    public void playPauseEvent(boolean isPlaying) {
        Method setDrawableParameters = null;
        if (notificationView != null) {
            try {
                Class RemoteViews = Class.forName("android.widget.RemoteViews");
                setDrawableParameters = RemoteViews.getMethod("setDrawableParameters",
                        new Class[]{int.class, boolean.class, int.class, int.class, PorterDuff.Mode.class, int.class});
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (isPlaying) {
                notificationView.setViewVisibility(R.id.pauseNotifBtn, View.VISIBLE);
                notificationView.setViewVisibility(R.id.playNotifBtn, View.GONE);
                try {
                    if (vibrantTitleColor != 0 && setDrawableParameters != null) {
                        setDrawableParameters.invoke(notificationView, new Object[]{R.id.pauseNotifBtn, false,
                                -1, vibrantTitleColor, PorterDuff.Mode.MULTIPLY, -1});
                        setDrawableParameters.invoke(notificationView, new Object[]{R.id.nextNotifBtn, false,
                                -1, vibrantTitleColor, PorterDuff.Mode.MULTIPLY, -1});
                        setDrawableParameters.invoke(notificationView, new Object[]{R.id.closeNotifBtn, false,
                                -1, vibrantTitleColor, PorterDuff.Mode.MULTIPLY, -1});
                        if(!isOldLayout){
                            setDrawableParameters.invoke(notificationView, new Object[]{R.id.prevNotifBtn, false,
                                    -1, vibrantTitleColor, PorterDuff.Mode.MULTIPLY, -1});
                        }
                    }
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                notificationView.setViewVisibility(R.id.playNotifBtn, View.VISIBLE);
                notificationView.setViewVisibility(R.id.pauseNotifBtn, View.GONE);
                try {
                    if (vibrantTitleColor != 0 && setDrawableParameters != null) {
                        setDrawableParameters.invoke(notificationView, new Object[]{R.id.playNotifBtn, false,
                                -1, vibrantTitleColor, PorterDuff.Mode.MULTIPLY, -1});
                        setDrawableParameters.invoke(notificationView, new Object[]{R.id.nextNotifBtn, false,
                                -1, vibrantTitleColor, PorterDuff.Mode.MULTIPLY, -1});
                        setDrawableParameters.invoke(notificationView, new Object[]{R.id.closeNotifBtn, false,
                                -1, vibrantTitleColor, PorterDuff.Mode.MULTIPLY, -1});
                        if(!isOldLayout){
                            setDrawableParameters.invoke(notificationView, new Object[]{R.id.prevNotifBtn, false,
                                    -1, vibrantTitleColor, PorterDuff.Mode.MULTIPLY, -1});
                        }
                    }
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String title = "";
    private int vibrantTitleColor = 0, vibrantBodyColor = 0;

    public void onServiceDestroy() {
        mNotificationManager.cancel(notifID);
        gradientBitmap=null;
        gradientBitmapOverlay=null;
        System.gc();
    }

    private void songChange(final byte[] byteCoverArt, String title1, final String artist, final String album, final boolean isPlay) {
        this.title = title1;
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                setSongDetail(byteCoverArt, title, artist, album);
                playPauseEvent(isPlay);

                notification.contentView = notificationView;
                mNotificationManager.notify(notifID, notification);
            }
        }).start();

    }

    public void showNotif(final byte[] byteCoverArt, final String title1, final String artist, final String album, final boolean isPlay) {
        if (title1.equalsIgnoreCase(title) && !android.text.TextUtils.isEmpty(title1) && vibrantTitleColor != 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    playPauseEvent(isPlay);
                    notification.contentView = notificationView;
                    mNotificationManager.notify(notifID, notification);
                }
            }).start();
            ;
        } else {
            songChange(byteCoverArt, title1, artist, album, isPlay);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setSongDetail(final byte[] byteCoverArt, final String title1, final String artist, final String album) {
        if (byteCoverArt != null) {
            Bitmap bitmap = AllSongListAdapter.getBitmap(ctx, byteCoverArt, true);
            if (bitmap != null) {
                notificationView.setImageViewBitmap(R.id.albumNotifArt, bitmap);
                Palette palette = createPaletteSync(bitmap);
                Palette.Swatch vibrantSwatch = checkVibrantSwatch(palette);
                Palette.Swatch dominantSwatch = checkDominantSwatch(palette);
                if (vibrantSwatch != null && dominantSwatch != null) {
                    vibrantBodyColor = vibrantSwatch.getRgb();
                    vibrantTitleColor = vibrantSwatch.getTitleTextColor();
                    int dominantBodyColor = dominantSwatch.getRgb();
//                    int dominantTitleColor = dominantSwatch.getTitleTextColor();
                    int[] colors, colorsOverlay;
                    if (isOldLayout) {
                        colors = new int[]{vibrantBodyColor, dominantBodyColor};
                        colorsOverlay = new int[]{ctx.getResources().getColor(R.color.colorTransparent), vibrantBodyColor};
                    } else {
                        colors = new int[]{dominantBodyColor, vibrantBodyColor};
                        colorsOverlay = new int[]{vibrantBodyColor, ctx.getColor(R.color.colorTransparent)};
                    }
                    notificationView.setInt(R.id.songTitle, "setTextColor", vibrantTitleColor);
                    notificationView.setInt(R.id.songArtist, "setTextColor", vibrantTitleColor);
                    notificationView.setInt(R.id.songAlbum, "setTextColor", vibrantTitleColor);

                    initGradientBitmap();

                    GradientDrawable gradientDrawable = new GradientDrawable(
                            GradientDrawable.Orientation.LEFT_RIGHT, colors);
                    GradientDrawable gradientDrawableOverlay = new GradientDrawable(
                            GradientDrawable.Orientation.LEFT_RIGHT, colorsOverlay);

                    gradientDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    gradientDrawable.draw(canvas);

                    gradientDrawableOverlay.setBounds(0, 0, canvasOverlay.getWidth(), canvasOverlay.getHeight());
                    gradientDrawableOverlay.setGradientCenter(canvasOverlay.getWidth()/3, canvasOverlay.getHeight()/2);
                    gradientDrawableOverlay.draw(canvasOverlay);
                    notificationView.setImageViewBitmap(R.id.fadeOverlay, gradientBitmapOverlay);
                    notificationView.setImageViewBitmap(R.id.ivBackground, gradientBitmap);
                }
            } else{
                notificationView.setImageViewResource(R.id.albumNotifArt, R.drawable.play_button);
            }
        } else{
            notificationView.setImageViewResource(R.id.albumNotifArt, R.drawable.play_button);
        }
        notificationView.setTextViewText(R.id.songTitle, title1);
        notificationView.setTextViewText(R.id.songArtist, artist);
        notificationView.setTextViewText(R.id.songAlbum, album);
    }

    public Palette createPaletteSync(Bitmap bitmap) {
        Palette p = Palette.from(bitmap).generate();
        return p;
    }

    private Palette.Swatch checkVibrantSwatch(Palette p) {
        Palette.Swatch vibrant = p.getVibrantSwatch();
        if (vibrant != null) {
            return vibrant;
        }
        return null;
    }

    private Palette.Swatch checkDominantSwatch(Palette p) {
        Palette.Swatch vibrant = p.getDominantSwatch();
        if (vibrant != null) {
            return vibrant;
        }
        return null;
    }
    private void initGradientBitmap(){
        gradientBitmap = Bitmap.createBitmap(Math.round(288 * dp), Math.round(72 * dp), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(gradientBitmap);
        int pixels = AllSongListAdapter.calculatePixels(40, ctx);
        gradientBitmapOverlay = Bitmap.createBitmap(pixels, pixels, Bitmap.Config.ARGB_8888);
        canvasOverlay = new Canvas(gradientBitmapOverlay);
    }
}
