package com.example.bebaagua.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.bebaagua.R;
import com.example.bebaagua.model.Alarm;

public class CheckActivity extends BroadcastReceiver {

    private static DatabaseWater db;

    @Override
    public void onReceive(Context context, Intent intent) {
        db = new DatabaseWater(context);

        int id = ForegroundService.id_alarm;
        if (id == -1) Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
        else {
            new Thread(() -> {
                Alarm notification = db.getNotification(String.valueOf(id));

                db.setChecked(
                        String.valueOf(notification.getId()),
                        notification.getHour(),
                        notification.getMinute(),
                        1);
            }).start();
            Toast.makeText(context, R.string.check, Toast.LENGTH_SHORT).show();
        }
    }
}