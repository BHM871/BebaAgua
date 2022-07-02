package com.example.bebaagua.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.bebaagua.R;
import com.example.bebaagua.controller.MainActivity;

public class NotificationPublisher extends BroadcastReceiver {

    public static final String KEY_NOTIFICATION = "key_notification";
    public static final String KEY_NOTIFICATION_ID = "key_notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent ii = new Intent(context.getApplicationContext(), MainActivity.class);
        ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pIntent = PendingIntent.getActivity(context, 0, ii, PendingIntent.FLAG_IMMUTABLE);
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String message = intent.getStringExtra(KEY_NOTIFICATION);
        int id = intent.getIntExtra(KEY_NOTIFICATION_ID, 0);

        Notification notification = getNotification(message, context, notificationManager, pIntent);

        notificationManager.notify(id, notification);
    }

    public Notification getNotification(String content, Context context, NotificationManager manager, PendingIntent intent) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context.getApplicationContext())
                        .setContentText(content)
                        .setContentTitle(context.getString(R.string.alert))
                        .setContentIntent(intent)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setSmallIcon(R.mipmap.ic_icon_round)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        String channelId = "YOUR_CHANNEL_ID";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel", NotificationManager.IMPORTANCE_HIGH);

            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        return builder.build();
    }
}
