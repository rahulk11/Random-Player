package com.rahulk11.audioplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Created by rahul on 6/12/2017.
 */

public class NotificationHandler extends Notification {

    private static Context ctx;
    private static NotificationManager mNotificationManager;
    private static final String ACTION_PLAY = "com.rahulk11.audioplayer.ACTION_PLAY";
    private static final String ACTION_NEXT = "com.rahulk11.audioplayer.ACTION_NEXT";
    private static final String ACTION_CLOSE = "com.rahulk11.audioplayer.ACTION_CLOSE";
    private static final int notifID = 54388;
    private static RemoteViews notificationView;

    public NotificationHandler(Context ctx1, String title, String artist, String album, boolean isPlay) {
        super();
        ctx = ctx1;
        String ns = Context.NOTIFICATION_SERVICE;

        mNotificationManager = (NotificationManager) ctx.getSystemService(ns);
        Notification notification = new Notification(R.drawable.notif, null, System.currentTimeMillis());
        notificationView = new RemoteViews(ctx.getPackageName(), R.layout.notification_layout);
        notificationView.setTextViewText(R.id.songTitle, title);
        notificationView.setTextViewText(R.id.songArtist, artist);
        notificationView.setTextViewText(R.id.songAlbum, album);
        setListeners();
        playPauseEvent(isPlay);
        Intent notificationIntent = new Intent(ctx, MainActivity.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);

        notification.contentView = notificationView;
        notification.contentIntent = pendingNotificationIntent;
        notification.flags |= Notification.FLAG_NO_CLEAR;

        mNotificationManager.notify(notifID, notification);
    }

    public void setListeners() {

        Intent playPauseIntent = new Intent(ACTION_PLAY);
        PendingIntent pendingPlayPauseIntent = PendingIntent.getBroadcast(ctx, 0, playPauseIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.playNotifBtn, pendingPlayPauseIntent);
        notificationView.setOnClickPendingIntent(R.id.pauseNotifBtn, pendingPlayPauseIntent);

        Intent nextIntent = new Intent(ACTION_NEXT);
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(ctx, 1, nextIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.nextNotifBtn, pendingNextIntent);

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
                    PlaybackManager.playPauseEvent();
                    break;
                case ACTION_NEXT:
                    PlaybackManager.playNext(true);
                    break;
                case ACTION_CLOSE:
                    mNotificationManager.cancel(notifID);
                    PlaybackManager.stopService();
                    break;
            }
        }
    }

    public void playPauseEvent(boolean isPlaying) {
        Bitmap bitmap = null;
        if (notificationView != null)
            if (isPlaying) {
                notificationView.setViewVisibility(R.id.pauseNotifBtn, View.VISIBLE);
                notificationView.setViewVisibility(R.id.playNotifBtn, View.GONE);
            } else {
                notificationView.setViewVisibility(R.id.playNotifBtn, View.VISIBLE);
                notificationView.setViewVisibility(R.id.pauseNotifBtn, View.GONE);
            }
    }

}
