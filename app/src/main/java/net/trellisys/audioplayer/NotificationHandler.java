package net.trellisys.audioplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import net.trellisys.audioplayer.slidinguppanelhelper.PlayPauseView;

/**
 * Created by rahul on 6/12/2017.
 */

public class NotificationHandler extends Notification{

        private Context ctx;
        private NotificationManager mNotificationManager;

        public NotificationHandler(Context ctx, String title, String artist){
            super();
            this.ctx=ctx;
            String ns = Context.NOTIFICATION_SERVICE;
            mNotificationManager = (NotificationManager) ctx.getSystemService(ns);
            Notification notification = new Notification(R.drawable.notif, null, System.currentTimeMillis());

            RemoteViews notificationView = new RemoteViews(ctx.getPackageName(), R.layout.notification_layout);
            notificationView.setTextViewText(R.id.songTitle, title);
            notificationView.setTextViewText(R.id.songArtist, artist);
            //the intent that is started when the notification is clicked (works)
            Intent notificationIntent = new Intent(ctx, MainActivity.class);
            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);

            notification.contentView = notificationView;
            notification.contentIntent = pendingNotificationIntent;
            notification.flags |= Notification.FLAG_NO_CLEAR;

            Intent switchIntent = new Intent("net.trellisys.audioplayer.ACTION_PLAY");
            PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(ctx, 0, switchIntent, 0);

            notificationView.setOnClickPendingIntent(R.id.playPauseNotif, pendingSwitchIntent);
            mNotificationManager.notify(1, notification);
        }

        public void setListeners(RemoteViews view){
            //radio listener
            Intent radio=new Intent(ctx,MainActivity.class);
            radio.putExtra("DO", "radio");
            PendingIntent pRadio = PendingIntent.getActivity(ctx, 0, radio, 0);
            view.setOnClickPendingIntent(R.id.radio, pRadio);

            //volume listener
            Intent volume=new Intent(ctx, MainActivity.class);
            volume.putExtra("DO", "volume");
            PendingIntent pVolume = PendingIntent.getActivity(ctx, 1, volume, 0);
//            view.setOnClickPendingIntent(R.id.volume, pVolume);

            //reboot listener
            Intent reboot=new Intent(ctx, MainActivity.class);
            reboot.putExtra("DO", "reboot");
            PendingIntent pReboot = PendingIntent.getActivity(ctx, 5, reboot, 0);
//            view.setOnClickPendingIntent(R.id.reboot, pReboot);

            //top listener
            Intent top=new Intent(ctx, MainActivity.class);
            top.putExtra("DO", "top");
            PendingIntent pTop = PendingIntent.getActivity(ctx, 3, top, 0);
            view.setOnClickPendingIntent(R.id.top, pTop);

            //app listener
            Intent app=new Intent(ctx, MainActivity.class);
            app.putExtra("DO", "app");
            PendingIntent pApp = PendingIntent.getActivity(ctx, 4, app, 0);
            view.setOnClickPendingIntent(R.id.playPauseNotif, pApp);
        }

}
