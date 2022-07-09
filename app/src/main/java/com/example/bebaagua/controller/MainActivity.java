package com.example.bebaagua.controller;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bebaagua.API.ClickItem;
import com.example.bebaagua.R;
import com.example.bebaagua.model.Alarm;
import com.example.bebaagua.util.DatabaseWater;
import com.example.bebaagua.util.ForegroundService;
import com.example.bebaagua.util.NotificationPublisher;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static DatabaseWater db;

    private LinearLayout linearSeeSchedules;
    private RecyclerView listSeeSchedules;
    private long idListNotification;

    private TimePicker timerStartAlarms;
    private EditText editAmountsOfWater;
    private SharedPreferences preferences;

    private Button btnCalc, btnSeeSchedules;

    private int hours, minutes, amountsWater, cont = 0;

    private Intent notificationIntent;
    private AlarmManager alarmManager;
    private PendingIntent broadcast = null;
    private Intent intentForeground;

    private boolean activator = false, seeOrNo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        idListNotification = 0;
        db = new DatabaseWater(MainActivity.this);

        setSupportActionBar(findViewById(R.id.toolbar_main));
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");

        MobileAds.initialize(this, initializationStatus -> {
        });
        AdView mAdView = findViewById(R.id.ad_view_main);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        linearSeeSchedules = findViewById(R.id.linear_hours);

        listSeeSchedules = findViewById(R.id.recycler_view_list_schedules);

        timerStartAlarms = findViewById(R.id.timer);
        timerStartAlarms.setIs24HourView(true);
        editAmountsOfWater = findViewById(R.id.edit_amounts_of_water);

        btnCalc = findViewById(R.id.btn_calcu);
        btnSeeSchedules = findViewById(R.id.btn_see_schedules);

        preferences = getSharedPreferences("db", Context.MODE_PRIVATE);
        boolean activated = preferences.getBoolean("activated", false);

        setupUI(activated, preferences);

        btnCalc.setOnClickListener(calcuListener);

        btnSeeSchedules.setOnClickListener(seeHoursListener);

    }

    private final View.OnClickListener calcuListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (linearSeeSchedules.getVisibility() == View.VISIBLE) return;

            short contClick = 0;

            if (activator) {
                ++contClick;
                activator = false;

                btnCalc.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.bg_button_initial));
                btnCalc.setText(R.string.calcu);

                btnSeeSchedules.setVisibility(View.GONE);

                updatePreferences(preferences.getBoolean("activated", false));

                cancelNotification();
                intentForeground = new Intent(MainActivity.this, ForegroundService.class);
                stopService(intentForeground);

                new Thread(() -> db.deleteAll()).start();

                alert(R.string.delete);
            }

            if (!valorIsValid()) return;

            hours = timerStartAlarms.getCurrentHour();
            minutes = timerStartAlarms.getCurrentMinute();
            amountsWater = Integer.parseInt(editAmountsOfWater.getText().toString());

            if (!activator && contClick == 0) {
                activator = true;

                btnCalc.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.bg_button_cancel));
                btnCalc.setText(R.string.cancel);

                new Thread(() -> {
                    int interval = intervalInMinutes();
                    hoursOfAlarms(hours, minutes, interval);

                    updatePreferences(preferences.getBoolean("activated", true));

                    runOnUiThread(() -> btnSeeSchedules.setVisibility(View.VISIBLE));

                }).start();

                AlertDialog dialogConfirm = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.save)
                        .setMessage(R.string.alert_water_message)
                        .setIcon(R.mipmap.ic_icon_round)
                        .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                        }))
                        .setIcon(R.drawable.ic_info)
                        .create();
                dialogConfirm.show();
            }

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editAmountsOfWater.getWindowToken(), 0);
        }
    };

    private final View.OnClickListener seeHoursListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Adapter adapterMain;
            cont++;
            if (!seeOrNo) {
                seeOrNo = true;

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<Alarm> alarms = getList();
                listSeeSchedules.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                adapterMain = new Adapter(alarms);
                listSeeSchedules.setAdapter(adapterMain);

                adapterMain.setListener(id -> {
                    adapterMain.setList(getList());
                    setCheckedList(adapterMain, adapterMain.getItemList(id));
                });

                btnSeeSchedules.setText(R.string.back);
                linearSeeSchedules.setVisibility(View.VISIBLE);
            } else {
                seeOrNo = false;

                List<Alarm> alarms = new ArrayList<>();

                listSeeSchedules.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                adapterMain = new Adapter(alarms);
                listSeeSchedules.setAdapter(adapterMain);

                btnSeeSchedules.setText(R.string.see_schedules);
                linearSeeSchedules.setVisibility(View.GONE);
            }
        }
    };

    private void setupUI(boolean activated, SharedPreferences preferences) {
        if (activated) {
            activator = true;

            hours = preferences.getInt("hours", timerStartAlarms.getCurrentHour());
            minutes = preferences.getInt("minutes", timerStartAlarms.getCurrentMinute());
            amountsWater = preferences.getInt("amounts", 0);

            timerStartAlarms.setCurrentHour(hours);
            timerStartAlarms.setCurrentMinute(minutes);
            editAmountsOfWater.setText(String.valueOf(amountsWater));

            btnCalc.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_cancel));
            btnCalc.setText(R.string.cancel);

            btnSeeSchedules.setVisibility(View.VISIBLE);

        }
    }

    private boolean valorIsValid() {
        String sAmountsWater = editAmountsOfWater.getText().toString();

        if (sAmountsWater.isEmpty() || Integer.parseInt(sAmountsWater) < 500) {
            alert(R.string.input);
            return false;
        }
        return true;
    }

    private int intervalInMinutes() {
        int hoursToAlarm = 23 - hours;
        hoursToAlarm *= 60;
        hoursToAlarm -= minutes;

        double quantitiesOfCups = amountsWater / 250;

        double intervalInMinutes = hoursToAlarm / quantitiesOfCups;
        return (int) intervalInMinutes;
    }

    @SuppressLint("DefaultLocale")
    private void hoursOfAlarms(int hoursStart, int minutesStart, int interval) {
        int index = 0;

        int finalIndex1 = index;
        new Thread(() -> {
            idListNotification = db.addNotification(finalIndex1, hours, minutes, 0);
            runOnUiThread(() -> {
                if (idListNotification == 0) alert(R.string.error);
            });
        }).start();
        ++index;
        int waterMax = 250;

        while (hoursStart < 23 && waterMax < amountsWater) {
            minutesStart += interval;

            while (minutesStart >= 60) {
                hoursStart++;
                minutesStart -= 60;
            }

            if (hoursStart >= 23 && waterMax >= amountsWater) break;
            else {
                idListNotification = db.addNotification(index, hoursStart, minutesStart, 0);
                        if (idListNotification == 0) alert(R.string.error);

                ++index;

                waterMax += 250;
            }
        }

        if (!foregroundServiceRunning()) {
            updateForegroundService(hours, minutes);
        }
    }

    private List<Alarm> getList() {
        List<Alarm> alarms = new ArrayList<>();
        List<Alarm> temporaryAlarms = db.getListNotification();

            for (int i = 0; i <= temporaryAlarms.size(); i++) {

                for (int j = 0; j < temporaryAlarms.size(); j++) {

                    if (temporaryAlarms.get(j).getId() == i) {
                        Alarm alarm = temporaryAlarms.get(j);
                        alarms.add(alarm);
                    }
                }
            }
        return alarms;
    }

    private void updatePreferences(boolean activated) {
        SharedPreferences.Editor editor = preferences.edit();
        if (activated) {
            editor.putBoolean("activated", false);
            editor.remove("hours");
            editor.remove("minutes");
            editor.remove("amounts");
        } else {
            editor.putBoolean("activated", true);
            editor.putInt("hours", hours);
            editor.putInt("minutes", minutes);
            editor.putInt("amounts", amountsWater);
        }
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuInfo) {
            openInfo();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openInfo() {
        Intent intent = new Intent(MainActivity.this, OpenInfoActivity.class);
        startActivity(intent);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void updateForegroundService(int hour, int minute) {
            intentForeground = new Intent(MainActivity.this, ForegroundService.class);
            intentForeground.putExtra(ForegroundService.HOURS_FOREGROUND_START, hour);
            intentForeground.putExtra(ForegroundService.MINUTE_FOREGROUND_START, minute);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intentForeground);
        }

    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void cancelNotification() {
        if (notificationIntent == null) {

            notificationIntent = new Intent(MainActivity.this, NotificationPublisher.class);
            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION, getString(R.string.hours_drink_water));
            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION_ID, 1);
        }

        if (broadcast == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                broadcast = PendingIntent.getBroadcast(
                        MainActivity.this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);
            } else {

                broadcast = PendingIntent.getBroadcast(
                        MainActivity.this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
            }
        }

        if (alarmManager == null) {
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        }

        alarmManager.cancel(broadcast);
    }

    public void setCheckedList(Adapter adapterMain, Alarm alarm) {
        new Thread(() -> {
            if (alarm.getChecked() == 0) {
                db.setChecked(
                        String.valueOf(alarm.getId()),
                        alarm.getHour(),
                        alarm.getMinute(),
                        1);
            } else {
                db.setChecked(
                        String.valueOf(alarm.getId()),
                        alarm.getHour(),
                        alarm.getMinute(),
                        0);
            }
            adapterMain.setList(getList());
        }).start();
    }

    private boolean foregroundServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (ForegroundService.class.getName().equals(service.service.getClassName()))
                return true;
        }
        return false;
    }

    private void alert(int resId) {
        Toast.makeText(MainActivity.this, resId, Toast.LENGTH_SHORT).show();
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private final List<Alarm> list;
        private ClickItem listener;

        public Adapter(List<Alarm> list) {
            this.list = list;
        }

        public void setListener(ClickItem listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.layout_list_notification, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Alarm alarmCurrent = list.get(position);
            holder.bind(alarmCurrent);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public Alarm getItemList(int id) {
            while (list.size() == 0) {
                if (list.size() != 0) return list.get(id);
            }
            return list.get(id);
        }

        public void setList(List<Alarm> list) {
            this.list.clear();
            this.list.addAll(list);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            public void bind(Alarm item) {
                CheckBox checkBox = itemView.findViewById(R.id.checkbox_list);

                if (item.getChecked() == 1) {
                    checkBox.setChecked(true);
                    checkBox.setButtonDrawable(R.drawable.ic_checked_true);
                } else {
                    checkBox.setChecked(false);
                    checkBox.setButtonDrawable(R.drawable.ic_checked_false);
                }
                checkBox.setText(getString(R.string.drink_water_at, item.hour, item.minute));

                checkBox.setOnClickListener(v -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkBox.isChecked()) {
                            checkBox.setChecked(true);
                            checkBox.setButtonDrawable(R.drawable.ic_checked_true);
                        } else {
                            checkBox.setChecked(false);
                            checkBox.setButtonDrawable(R.drawable.ic_checked_false);
                        }
                    }
                    listener.onClick(item.getId());
                });
            }
        }
    }

}