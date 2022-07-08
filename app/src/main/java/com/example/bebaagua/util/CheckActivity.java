package com.example.bebaagua.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.bebaagua.R;
import com.example.bebaagua.model.Alarm;

public class CheckActivity extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = NotificationPublisher.id_alarm;
        if (id != -1) {
            Alarm notification = DatabaseWater.getInstance(context).getNotification(String.valueOf(id));
            new Thread(() -> DatabaseWater.getInstance(context).setChecked(
                    String.valueOf(notification.getId()),
                    notification.getHour(),
                    notification.getMinute(),
                    1)).start();
            Toast.makeText(context, R.string.check, Toast.LENGTH_SHORT).show();
        } else Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
    }
}