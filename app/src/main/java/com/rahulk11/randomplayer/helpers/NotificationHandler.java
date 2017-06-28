package com.rahulk11.randomplayer.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.RemoteViews;
import android.support.v7.graphics.Palette;

import com.rahulk11.randomplayer.MainActivity;
import com.rahulk11.randomplayer.R;
import com.rahulk11.randomplayer.SongService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by rahul on 6/12/2017.
 */

public class NotificationHandler extends Notification {

    private Context ctx;
    private NotificationManager mNotificationManager;
    private static final String ACTION_PLAY = "com.rahulk11.randomplayer.ACTION_PLAY";
    private static final String ACTION_NEXT = "com.rahulk11.randomplayer.ACTION_NEXT";
    private static final String ACTION_PREV = "com.rahulk11.randomplayer.ACTION_PREV";
    private static final String ACTION_CLOSE = "com.rahulk11.randomplayer.ACTION_CLOSE";

    private static final int notifID = 54388;
    private RemoteViews notificationView, bigNotificationView;
    private Notification notification;
    private static boolean isOldLayout = false, staticBool = true;
    ;
    private boolean isFirstTime = true;
    private PendingIntent pendingNotificationIntent;
    private String title = "";

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private NotificationHandler(Context ctx1) {
        super();
        ctx = ctx1;
        staticBool = this.isFirstTime;
        mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(ctx, MainActivity.class);
        pendingNotificationIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
    }

    private static NotificationHandler notificationHandler = null;

    public static NotificationHandler getInstance(Context ctx1) {
        if (notificationHandler != null)
            return notificationHandler;
        else {
            notificationHandler = new NotificationHandler(ctx1);
            return notificationHandler;
        }
    }

    public void showNotif(String title1, String artist1, String album1, final boolean isPlay) {
        if (title1.equalsIgnoreCase(title) && !android.text.TextUtils.isEmpty(title1) && BitmapPalette.vibrantTitleTextColor != 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    playPauseEvent(isPlay);
                    notification.contentView = notificationView;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        notification.bigContentView = bigNotificationView;
                    }
                    mNotificationManager.notify(notifID, notification);
                }
            }).start();
        } else {
            songChange(title1, artist1, album1, isPlay);
        }
    }

    public void updateNotif(String title1, String artist1, String album1) {
        if (notificationView != null)
            setSongDetail(title1, artist1, album1);
    }

    private void initNotificationView() {
        notification = new Notification(R.drawable.music, null, System.currentTimeMillis());
        notification.contentIntent = pendingNotificationIntent;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            notificationView = new RemoteViews(ctx.getPackageName(), R.layout.notif_old_layout);
            isOldLayout = true;
        } else notificationView = new RemoteViews(ctx.getPackageName(), R.layout.notif_new_layout);
        bigNotificationView = new RemoteViews(ctx.getPackageName(), R.layout.big_notif_layout);
        setListeners();
    }

    private void setListeners() {
        Intent playPauseIntent = new Intent(ACTION_PLAY);
        PendingIntent pendingPlayPauseIntent = PendingIntent.getBroadcast(ctx, 0, playPauseIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.playNotifBtn, pendingPlayPauseIntent);
        notificationView.setOnClickPendingIntent(R.id.pauseNotifBtn, pendingPlayPauseIntent);
        bigNotificationView.setOnClickPendingIntent(R.id.playNotifBtn, pendingPlayPauseIntent);
        bigNotificationView.setOnClickPendingIntent(R.id.pauseNotifBtn, pendingPlayPauseIntent);

        Intent nextIntent = new Intent(ACTION_NEXT);
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(ctx, 1, nextIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.nextNotifBtn, pendingNextIntent);
        bigNotificationView.setOnClickPendingIntent(R.id.nextNotifBtn, pendingNextIntent);

        Intent prevIntent = new Intent(ACTION_PREV);
        PendingIntent pendingPrevIntent = PendingIntent.getBroadcast(ctx, 1, prevIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.prevNotifBtn, pendingPrevIntent);
        bigNotificationView.setOnClickPendingIntent(R.id.prevNotifBtn, pendingPrevIntent);

        Intent closeIntent = new Intent(ACTION_CLOSE);
        PendingIntent pendingCloseIntent = PendingIntent.getBroadcast(ctx, 2, closeIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.closeNotifBtn, pendingCloseIntent);
        bigNotificationView.setOnClickPendingIntent(R.id.closeNotifBtn, pendingCloseIntent);
    }

    private void songChange(String title1, final String artist, final String album, final boolean isPlay) {
        this.title = title1;
        initNotificationView();
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                setSongDetail(title, artist, album);
                playPauseEvent(isPlay);

                notification.contentView = notificationView;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification.bigContentView = bigNotificationView;
                }
                mNotificationManager.notify(notifID, notification);
            }
        }).start();
    }

    private void playPauseEvent(boolean isPlaying) {
        Method setDrawableParameters = null;
        if (notificationView != null && bigNotificationView != null) {
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
                bigNotificationView.setViewVisibility(R.id.pauseNotifBtn, View.VISIBLE);
                bigNotificationView.setViewVisibility(R.id.playNotifBtn, View.GONE);
            } else {
                notificationView.setViewVisibility(R.id.playNotifBtn, View.VISIBLE);
                notificationView.setViewVisibility(R.id.pauseNotifBtn, View.GONE);
                bigNotificationView.setViewVisibility(R.id.playNotifBtn, View.VISIBLE);
                bigNotificationView.setViewVisibility(R.id.pauseNotifBtn, View.GONE);
            }

            try {
                if (BitmapPalette.vibrantTitleTextColor != 0 && setDrawableParameters != null) {
                    setDrawableParameters.invoke(notificationView, new Object[]{R.id.playNotifBtn, false,
                            -1, BitmapPalette.vibrantTitleTextColor, PorterDuff.Mode.MULTIPLY, -1});
                    setDrawableParameters.invoke(notificationView, new Object[]{R.id.pauseNotifBtn, false,
                            -1, BitmapPalette.vibrantTitleTextColor, PorterDuff.Mode.MULTIPLY, -1});
                    setDrawableParameters.invoke(notificationView, new Object[]{R.id.nextNotifBtn, false,
                            -1, BitmapPalette.vibrantTitleTextColor, PorterDuff.Mode.MULTIPLY, -1});
                    setDrawableParameters.invoke(notificationView, new Object[]{R.id.closeNotifBtn, false,
                            -1, BitmapPalette.vibrantTitleTextColor, PorterDuff.Mode.MULTIPLY, -1});

                    setDrawableParameters.invoke(bigNotificationView, new Object[]{R.id.playNotifBtn, false,
                            -1, BitmapPalette.vibrantTitleTextColor, PorterDuff.Mode.MULTIPLY, -1});
                    setDrawableParameters.invoke(bigNotificationView, new Object[]{R.id.pauseNotifBtn, false,
                            -1, BitmapPalette.vibrantTitleTextColor, PorterDuff.Mode.MULTIPLY, -1});
                    setDrawableParameters.invoke(bigNotificationView, new Object[]{R.id.nextNotifBtn, false,
                            -1, BitmapPalette.vibrantTitleTextColor, PorterDuff.Mode.MULTIPLY, -1});
                    setDrawableParameters.invoke(bigNotificationView, new Object[]{R.id.closeNotifBtn, false,
                            -1, BitmapPalette.vibrantTitleTextColor, PorterDuff.Mode.MULTIPLY, -1});

                    setDrawableParameters.invoke(notificationView, new Object[]{R.id.prevNotifBtn, false,
                            -1, BitmapPalette.vibrantTitleTextColor, PorterDuff.Mode.MULTIPLY, -1});
                    setDrawableParameters.invoke(bigNotificationView, new Object[]{R.id.prevNotifBtn, false,
                            -1, BitmapPalette.vibrantTitleTextColor, PorterDuff.Mode.MULTIPLY, -1});

                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void setSongDetail(final String title1, final String artist, final String album) {
        if (BitmapPalette.bitmap != null) {
            notificationView.setImageViewBitmap(R.id.albumNotifArt, BitmapPalette.bitmap);
            bigNotificationView.setImageViewBitmap(R.id.albumNotifArt, BitmapPalette.bitmap);

            int[] colors, colorsOverlay;
            if (isOldLayout) {
                colors = new int[]{BitmapPalette.vibrantRGBColor, BitmapPalette.darkVibrantRGBColor};
                colorsOverlay = new int[]{ctx.getResources().getColor(R.color.colorTransparent), BitmapPalette.vibrantRGBColor};
            } else {
                colors = new int[]{BitmapPalette.vibrantRGBColor, BitmapPalette.darkVibrantRGBColor};
                colorsOverlay = new int[]{BitmapPalette.darkVibrantRGBColor, ctx.getColor(R.color.colorTransparent)};
            }
            notificationView.setInt(R.id.songTitle, "setTextColor", BitmapPalette.vibrantTitleTextColor);
            notificationView.setInt(R.id.songArtist, "setTextColor", BitmapPalette.vibrantTitleTextColor);
            notificationView.setInt(R.id.songAlbum, "setTextColor", BitmapPalette.vibrantTitleTextColor);

            bigNotificationView.setInt(R.id.songTitle, "setTextColor", BitmapPalette.vibrantTitleTextColor);
            bigNotificationView.setInt(R.id.songArtist, "setTextColor", BitmapPalette.vibrantTitleTextColor);
            bigNotificationView.setInt(R.id.songAlbum, "setTextColor", BitmapPalette.vibrantTitleTextColor);

            int width = BitmapPalette.calculatePixels(60, ctx);
            int height = BitmapPalette.calculatePixels(30, ctx);
            setGradientBitmap(width, height, colors, false);
            setGradientBitmap(height, height, colorsOverlay, true);
        } else {
            notificationView.setImageViewResource(R.id.albumNotifArt, R.drawable.random);
            bigNotificationView.setImageViewResource(R.id.albumNotifArt, R.drawable.random);
        }
        notificationView.setTextViewText(R.id.songTitle, title1);
        notificationView.setTextViewText(R.id.songArtist, artist);
        notificationView.setTextViewText(R.id.songAlbum, album);

        bigNotificationView.setTextViewText(R.id.songTitle, title1);
        bigNotificationView.setTextViewText(R.id.songArtist, artist);
        bigNotificationView.setTextViewText(R.id.songAlbum, album);
    }

    private void setGradientBitmap(int width, int height, int[] colors, boolean isOverlay) {
        Bitmap gradientBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(gradientBitmap);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, colors);

        gradientDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        gradientDrawable.draw(canvas);
        if (isOverlay) {
//            gradientDrawable.setGradientCenter(canvasOverlay.getWidth()/3, canvasOverlay.getHeight()/2);
            notificationView.setImageViewBitmap(R.id.fadeOverlay, gradientBitmap);
            if (isOldLayout) {
                int[] colorsRev = new int[]{BitmapPalette.darkVibrantRGBColor, colors[0]};
                Bitmap gradientBigBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas bigCanvas = new Canvas(gradientBigBitmap);
                GradientDrawable gradientBigDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT, colorsRev);

                gradientBigDrawable.setBounds(0, 0, bigCanvas.getWidth(), bigCanvas.getHeight());
                gradientBigDrawable.draw(bigCanvas);
                bigNotificationView.setImageViewBitmap(R.id.fadeOverlay, gradientBigBitmap);
            } else
                bigNotificationView.setImageViewBitmap(R.id.fadeOverlay, gradientBitmap);
        } else {
            notificationView.setImageViewBitmap(R.id.ivBackground, gradientBitmap);
            bigNotificationView.setImageViewBitmap(R.id.ivBackground, gradientBitmap);
        }
    }

    public void onServiceDestroy() {
        title = "";
        mNotificationManager.cancel(notifID);
        notificationHandler = null;
    }

//    private int[] hslToRgb(float h, float s, float l){
//        float r, g, b;
//
//        if(s == 0){
//            r = g = b = l; // achromatic
//        }else{
//
//            float q = l < 0.5 ? l * (1 + s) : l + s - l * s;
//            float p = 2 * l - q;
//            r = hue2rgb(p, q, h + 1/3);
//            g = hue2rgb(p, q, h);
//            b = hue2rgb(p, q, h - 1/3);
//        }
//
//        return new int[] {Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)};
//    }
//
//    private float hue2rgb(float p, float q, float t){
//        if(t < 0) t += 1;
//        if(t > 1) t -= 1;
//        if(t < 1/6) return p + (q - p) * 6 * t;
//        if(t < 1/2) return q;
//        if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
//        return p;
//    }

    public static class NotifBtnClickReceiver extends BroadcastReceiver {

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
                    //noinspection deprecaRandom Playervition
                    if (context != null && !staticBool && SongService.isPlaying())
                        if (!((AudioManager) context.getSystemService(AUDIO_SERVICE)).isWiredHeadsetOn()) {
                            PlaybackManager.playPauseEvent(true, true, SongService.getCurrPos());
                        }
                    staticBool = false;
                    break;
            }
        }
    }
}