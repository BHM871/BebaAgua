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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bebaagua.R;
import com.example.bebaagua.model.Alarm;
import com.example.bebaagua.util.DatabaseWater;
import com.example.bebaagua.util.ForegroundService;
import com.example.bebaagua.util.NotificationPublisher;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

    @SuppressLint({"RestrictedApi", "UnspecifiedImmutableFlag"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        idListNotification = 0;

        setSupportActionBar(findViewById(R.id.toolbar_main));
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");

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

                new Thread(() -> DatabaseWater.getInstance(MainActivity.this).deleteAll()).start();

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
                        .setTitle(R.string.alert_water_title)
                        .setMessage(R.string.alert_water_message)
                        .setIcon(R.drawable.ic_launcher_foreground)
                        .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                        }))
                        .setIcon(R.drawable.ic_info)
                        .create();
                dialogConfirm.show();

                alert(R.string.save);
            }

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editAmountsOfWater.getWindowToken(), 0);
        }
    };

    private final View.OnClickListener seeHoursListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Adapter adapter;
            cont++;
            if (!seeOrNo) {
                seeOrNo = true;

                List<Alarm> alarms = getList();

                listSeeSchedules.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                adapter = new Adapter(alarms);
                listSeeSchedules.setAdapter(adapter);

                btnSeeSchedules.setText(R.string.back);
                linearSeeSchedules.setVisibility(View.VISIBLE);
            } else {
                seeOrNo = false;

                List<Alarm> alarms = new ArrayList<>();

                listSeeSchedules.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                adapter = new Adapter(alarms);
                listSeeSchedules.setAdapter(adapter);

                btnSeeSchedules.setText(R.string.see_schedules);
                linearSeeSchedules.setVisibility(View.GONE);
            }
        }
    };

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
        StringBuilder sControl = new StringBuilder();
        sControl.append(getString(R.string.drink_water_at, hours, minutes)).append("\n");
        int index = 0;

        int finalIndex1 = index;
        new Thread(() -> {
            idListNotification = DatabaseWater.getInstance(MainActivity.this).addNotification(finalIndex1, hours, minutes, 0);
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
                int finalHoursStart = hoursStart;
                int finalMinutesStart = minutesStart;
                int finalIndex = index + 1;

                new Thread(() -> {
                    idListNotification = DatabaseWater.getInstance(MainActivity.this).addNotification(finalIndex, finalHoursStart, finalMinutesStart, 0);
                    runOnUiThread(() -> {
                        if (idListNotification == 0) alert(R.string.error);
                    });
                }).start();
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
        new Thread(() -> {
            List<Alarm> temporaryAlarms = DatabaseWater.getInstance(MainActivity.this).getListNotification();

            for (int i = 0; i <= temporaryAlarms.size(); i++) {

                for (int j = 0; j < temporaryAlarms.size(); j++) {

                    if (temporaryAlarms.get(j).getId() == i) {
                        Alarm alarm = temporaryAlarms.get(j);
                        alarms.add(alarm);
                    }
                }
            }
        }).start();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intentForeground = new Intent(MainActivity.this, ForegroundService.class);
            intentForeground.putExtra(ForegroundService.HOURS_FOREGROUND_START, hour);
            intentForeground.putExtra(ForegroundService.MINUTE_FOREGROUND_START, minute);

            startForegroundService(intentForeground);
        }
    }

    private boolean foregroundServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (ForegroundService.class.getName().equals(service.service.getClassName()))
                return true;
        }
        return false;
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

    private void alert(int resId) {
        Toast.makeText(MainActivity.this, resId, Toast.LENGTH_SHORT).show();
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

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
            return new ViewHolder(getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false));
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

        private class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            public void bind(Alarm item) {
                TextView txtListSchedules = (TextView) itemView;
                txtListSchedules.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                txtListSchedules.setText(getString(R.string.drink_water_at, item.hour, item.minute));

                txtListSchedules.setOnClickListener(v -> listener.onClick(item.getId()));

            }
        }
    }

}