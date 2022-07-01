package com.example.bebaagua.util;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.bebaagua.model.Alarm;

import java.util.ArrayList;
import java.util.List;

public class DatabaseWater extends SQLiteOpenHelper {

    private static final String DB_NAME = "database_water_alarm";
    private static final int DB_VERSION = 1;

    @SuppressLint("StaticFieldLeak")
    private static DatabaseWater INSTANCE;

    public static DatabaseWater getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseWater(context);
            return INSTANCE;
        }
        return INSTANCE;
    }

    public DatabaseWater(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE alarm(id INTEGER primary key, hour INTEGER, minute INTEGER, checked INTEGER)"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE alarm");
        db.execSQL(
                "CREATE TABLE alarm(id INTEGER primary key, hour INTEGER, minute INTEGER, checked INTEGER)"
        );

    }

    public void deleteAll(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM alarm");
    }

    public long addNotification(int id, int hour, int minute, int checked) {
        SQLiteDatabase db = getWritableDatabase();

        long idTask = 0;
        try {
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("hour", hour);
            values.put("minute", minute);
            values.put("checked", checked);

            idTask = db.insertOrThrow("alarm", null, values);
            db.setTransactionSuccessful();
        } finally {
            if (db.isOpen()) db.endTransaction();
        }

        return idTask;
    }

    @SuppressLint("Range")
    public List<Alarm> getListNotification() {
        List<Alarm> alarms = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM alarm", new String[0]);

        try {
            if (cursor.moveToFirst()) {
                do {

                    Alarm alarm = new Alarm();

                    alarm.id = cursor.getInt(cursor.getColumnIndex("id"));
                    alarm.hour = cursor.getInt(cursor.getColumnIndex("hour"));
                    alarm.minute = cursor.getInt(cursor.getColumnIndex("minute"));
                    alarm.checked = cursor.getInt(cursor.getColumnIndex("checked"));

                    alarms.add(alarm);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("SQLite", e.getMessage(), e);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }

        return alarms;
    }
}
