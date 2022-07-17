package com.example.bebaagua.util;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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
        onCreate(db);

    }

    public void deleteAll(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM alarm");
    }

    public void addNotification(int id, int hour, int minute, int checked) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("hour", hour);
            values.put("minute", minute);
            values.put("checked", checked);

            db.insertOrThrow("alarm", null, values);
            db.setTransactionSuccessful();
        } catch (SQLException sE) {
            Log.e("SQLite", sE.getMessage(), sE);
        } finally {
            if (db.isOpen()) db.endTransaction();
        }
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

    @SuppressLint("Range")
    public Alarm getNotification(String id) {
        Alarm alarm = new Alarm();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM alarm WHERE id = ?", new String[]{id});

        try {
            if (cursor.moveToFirst()) {
                alarm.setId(cursor.getInt(cursor.getColumnIndex("id")));
                alarm.setHour(cursor.getInt(cursor.getColumnIndex("hour")));
                alarm.setMinute(cursor.getInt(cursor.getColumnIndex("minute")));
                alarm.setChecked(cursor.getInt(cursor.getColumnIndex("checked")));
            }
        } catch (Exception e) {
            Log.e("SQLite", e.getMessage(), e);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }

        return alarm;
    }

    @SuppressLint("Range")
    public void setChecked(String id, int hour, int minute, int checked) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put("id", Integer.valueOf(id));
            values.put("hour", hour);
            values.put("minute", minute);
            values.put("checked", checked);

            db.update("alarm", values, "id = ?", new String[]{id});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("SQLite", e.getMessage(), e);
        } finally {
            if (db.isOpen()) db.endTransaction();
        }

    }

    @SuppressLint("Recycle")
    public void setCheckedFalse() {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();

            for (int i = 0; i < getListNotification().size(); i++) {
                db.execSQL("UPDATE alarm SET checked = 0 WHERE id = ?", new String[]{String.valueOf(i)});
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("SQLite", e.getMessage(), e);
        } finally {
            if (db.isOpen()) db.endTransaction();
        }

    }
}
