package com.sophieopenclass.go4lunch.controllers.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivitySettingsBinding;
import com.sophieopenclass.go4lunch.utils.NotificationReceiver;
import com.sophieopenclass.go4lunch.utils.TimePickerFragment;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SettingsActivity extends BaseActivity <MyViewModel> implements TimePickerDialog.OnTimeSetListener {
    private static final String TAG = "SettingsActivity";
    ActivitySettingsBinding binding;
    public static String chosenRestaurant;
    public static ArrayList<String> workmates;

    public static Activity newInstance() {
        return new SettingsActivity();
    }

    @Override
    public Class getViewModelClass() {
        return MyViewModel.class;
    }

    @Override
    public View getFragmentLayout() {
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding.buttonTimepicker.setOnClickListener(v -> {
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "time picker");
        });

        binding.buttonCancel.setOnClickListener(v -> cancelAlarm());
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        updateTimeText(c);
        startAlarm(c);
    }


    private void updateTimeText(Calendar c) {
        String timeText = "Alarm set for: ";
        timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        binding.textView.setText(timeText);
    }
    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra(Intent.EXTRA_UID , getNotificationMessage());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        }
    }


    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        binding.textView.setText("Notification canceled");
    }


    public String getNotificationMessage() {
        AtomicReference<String> restaurant = new AtomicReference<>("");

        viewModel.getUser(getCurrentUser().getUid()).observe(this, user -> {
            restaurant.set(user.getChosenRestaurantName());
        });
        String message = "";
        StringBuilder workmates = new StringBuilder();
        List<String> workmatesList = new ArrayList<>(Arrays.asList("Zendaya", "Johnny", "Gabrielle"));


        for (int i = 0; i < workmatesList.size(); i++)
            if (i != workmatesList.size() - 1)
                workmates.append(workmatesList.get(i)).append(", ");
            else
                workmates.append(workmatesList.get(i)).append(".");


        message = getString(R.string.notification_message, restaurant, workmates.toString());
        return message;
    }
}