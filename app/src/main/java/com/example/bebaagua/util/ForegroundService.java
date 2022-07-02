package com.example.bebaagua.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.bebaagua.R;
import com.example.bebaagua.controller.MainActivity;
import com.example.bebaagua.model.Alarm;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ForegroundService extends Service {

    public static final String HOURS_FOREGROUND_START = "hours_notification";
    public static final String MINUTE_FOREGROUND_START = "minute_notification";

    public static final String LIST_ALARM = "list_hours_alarm";

    private static boolean activated = false;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            activated = true;

            List<Alarm> alarms = DatabaseWater.getInstance(getApplicationContext()).getListNotification();

            Bundle extras = intent.getExtras();
            Calendar calendarStart = setCalendar(extras.getInt(HOURS_FOREGROUND_START), extras.getInt(MINUTE_FOREGROUND_START));
            Date dateStart = calendarStart.getTime();

            Calendar calendarStop = setCalendar(23, 0);
            Date dateStop = calendarStop.getTime();

            Calendar calendarNow;
            Date dateNow;

            Calendar calendarAlarm;
            Date dateAlarm;

            while (activated) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                calendarNow = Calendar.getInstance();
                dateNow = calendarNow.getTime();

                if (dateNow.getHours() >= dateStart.getHours()
                        && dateNow.getMinutes() >= dateStart.getMinutes()
                        && dateNow.getHours() < dateStop.getHours()) {

                    for (int i = 0; i < alarms.size(); i++) {
                        calendarAlarm = setCalendar(alarms.get(i).getHour(), alarms.get(i).getMinute());
                        dateAlarm = calendarAlarm.getTime();

                        if (dateNow.getHours() == dateAlarm.getHours()
                                && dateNow.getMinutes() == dateAlarm.getMinutes()) {

                            callNotificationWater(dateAlarm.getTime());
                            try {
                                Thread.sleep(60000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }).start();

        startForeground(1002, callNotificationForeground());

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        activated = false;

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            super.onDestroy();
        }).start();

    }

    private Calendar setCalendar(int hours, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void callNotificationWater(long alarmHour) {
        Intent intentNotification = new Intent(getApplicationContext(), NotificationPublisher.class);
        intentNotification.putExtra(NotificationPublisher.KEY_NOTIFICATION_ID, 1001);
        intentNotification.putExtra(NotificationPublisher.KEY_NOTIFICATION, getApplicationContext().getString(R.string.hours_drink_water));


        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(
                    getBaseContext(),
                    0,
                    intentNotification,
                    PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(
                    getBaseContext(),
                    0,
                    intentNotification,
                    PendingIntent.FLAG_CANCEL_CURRENT);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmHour, pendingIntent);

    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private Notification callNotificationForeground() {
        String CHANNEL = "Foreground Service ID";
        NotificationChannel channel = null;
        Notification.Builder notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    CHANNEL,
                    CHANNEL,
                    NotificationManager.IMPORTANCE_LOW
            );
        }

        Intent intentNotification = new Intent(getApplicationContext(), MainActivity.class);
        intentNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(
                    getBaseContext(),
                    0,
                    intentNotification,
                    PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(
                    getBaseContext(),
                    0,
                    intentNotification,
                    PendingIntent.FLAG_CANCEL_CURRENT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager.class).createNotificationChannel(channel);

            notification = new Notification.Builder(this, CHANNEL)
                    .setContentText(getText(R.string.running_text_notification))
                    .setContentTitle(getText(R.string.app_name))
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_icon_round);
        }

        assert notification != null;
        return notification.build();
    }
}
