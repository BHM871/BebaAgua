package com.example.bebaagua.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.bebaagua.model.Alarm;

public class CheckActivity extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = NotificationPublisher.id_alarm;
        if (id != -1) {
            Alarm notification = DatabaseWater.getInstance(context).getNotification(String.valueOf(id));
            DatabaseWater.getInstance(context).setChecked(
                    String.valueOf(notification.getId()),
                    notification.getHour(),
                    notification.getMinute(),
                    1);
        }
    }
}