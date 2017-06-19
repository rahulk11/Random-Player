package com.rahulk11.audioplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.support.v7.graphics.Palette;

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
    private static RemoteViews notificationView;
    private Notification notification;
    private Canvas canvas;
    private Bitmap gradientBitmap;
    private float dp, dpi;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public NotificationHandler(Context ctx1) {
        super();
        ctx = ctx1;
        mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new Notification(R.drawable.play_button, null, System.currentTimeMillis());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            notificationView = new RemoteViews(ctx.getPackageName(), R.layout.notif_old_layout);
        else notificationView = new RemoteViews(ctx.getPackageName(), R.layout.notif_new_layout);

        setListeners();


        Intent notificationIntent = new Intent(ctx, MainActivity.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);

        notification.contentIntent = pendingNotificationIntent;
        notification.flags |= Notification.FLAG_NO_CLEAR;

        dpi = ctx.getResources().getDisplayMetrics().xdpi;
        dp = ctx.getResources().getDisplayMetrics().density;

        gradientBitmap = Bitmap.createBitmap(Math.round(288 * dp), Math.round(72 * dp), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(gradientBitmap);
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
        private boolean firstClick = true;

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
                    onServiceDestroy();
                    PlaybackManager.stopService();
                    break;
                case Intent.ACTION_HEADSET_PLUG:
                    //noinspection deprecation
                    if (ctx != null)
                        if (!((AudioManager) ctx.getSystemService(AUDIO_SERVICE)).isWiredHeadsetOn() && !firstClick) {
                            PlaybackManager.playPauseEvent(true, SongService.isPlaying(), SongService.getCurrPos());
                            firstClick = false;
                        }
                    break;

            }
        }
    }

    public void playPauseEvent(boolean isPlaying) {
        if (notificationView != null)
            if (isPlaying) {
                notificationView.setViewVisibility(R.id.pauseNotifBtn, View.VISIBLE);
                notificationView.setViewVisibility(R.id.playNotifBtn, View.GONE);
            } else {
                notificationView.setViewVisibility(R.id.playNotifBtn, View.VISIBLE);
                notificationView.setViewVisibility(R.id.pauseNotifBtn, View.GONE);
            }
    }

    public static void onServiceDestroy() {
        mNotificationManager.cancel(notifID);
    }

    public void updateNotification(final byte[] byteCoverArt, final String title, final String artist, final String album, final boolean isPlay) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                playPauseEvent(isPlay);
                if (byteCoverArt != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(byteCoverArt, 0, byteCoverArt.length);
                    notificationView.setImageViewBitmap(R.id.albumNotifArt, bitmap);
                    if (bitmap != null) {
                        Palette palette = Palette.from(bitmap).generate();
                        int dominantColor = palette.getDominantColor(Color.WHITE);
                        int vibrantColor = palette.getVibrantColor(Color.GRAY);
                        int darkVibrantColor = palette.getDarkVibrantColor(Color.DKGRAY);
//                        int lightVibrantColor = palette.getLightVibrantColor(Color.LTGRAY);
                        int[] colors = new int[]{vibrantColor, dominantColor};
                        notificationView.setInt(R.id.songTitle, "setTextColor", darkVibrantColor);
                        notificationView.setInt(R.id.songArtist, "setTextColor", darkVibrantColor);
                        notificationView.setInt(R.id.songAlbum, "setTextColor", darkVibrantColor);

                        GradientDrawable gradientDrawable = new GradientDrawable(
                                GradientDrawable.Orientation.LEFT_RIGHT, colors);

                        gradientDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        gradientDrawable.setCornerRadius(5 * (dpi / 160));
                        gradientDrawable.draw(canvas);

                        notificationView.setImageViewBitmap(R.id.ivBackground, gradientBitmap);
                    }
                }
                notificationView.setTextViewText(R.id.songTitle, title);
                notificationView.setTextViewText(R.id.songArtist, artist);
                notificationView.setTextViewText(R.id.songAlbum, album);

                notification.contentView = notificationView;
                mNotificationManager.notify(notifID, notification);
            }
        }).start();

    }

}
