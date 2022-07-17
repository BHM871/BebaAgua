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

    public static final String ID_NOTIFICATION_CHECK = "id_notification_check";
    public static final String CHECK = "check";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent ii = new Intent(context.getApplicationContext(), MainActivity.class);
        ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Intent ii2 = new Intent(context.getApplicationContext(), CheckActivity.class);
        ii2.setAction(CHECK);
        ii2.putExtra(ID_NOTIFICATION_CHECK, 1);

        PendingIntent intentCheck = null;
        PendingIntent pIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            intentCheck = PendingIntent.getBroadcast(context, 0, ii2, PendingIntent.FLAG_MUTABLE);
            pIntent = PendingIntent.getActivity(context, 0, ii, PendingIntent.FLAG_MUTABLE);
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String message = intent.getStringExtra(KEY_NOTIFICATION);
        int id = intent.getIntExtra(KEY_NOTIFICATION_ID, 0);

        Notification notification = getNotification(message, context, notificationManager, pIntent, intentCheck);

        notificationManager.notify(id, notification);
    }

    public Notification getNotification(String content, Context context, NotificationManager manager, PendingIntent intentMain, PendingIntent intentCheck) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context.getApplicationContext())
                        .setContentText(content)
                        .setContentTitle(context.getString(R.string.alert))
                        .setContentIntent(intentMain)
                        .addAction(R.drawable.ic_checked_true, context.getString(R.string.i_drank_water), intentCheck)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_drink)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        String channelId = "YOUR_CHANNEL_ID";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel", NotificationManager.IMPORTANCE_HIGH);

            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        return builder.build();
    }
}
