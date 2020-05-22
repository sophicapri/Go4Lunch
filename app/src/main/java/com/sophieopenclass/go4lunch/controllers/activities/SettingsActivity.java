package com.sophieopenclass.go4lunch.controllers.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivitySettingsBinding;
import com.sophieopenclass.go4lunch.models.User;
import com.sophieopenclass.go4lunch.utils.NotificationHelper;
import com.sophieopenclass.go4lunch.utils.NotificationWorker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.EXTRA_UID;

public class SettingsActivity extends BaseActivity<MyViewModel> {
    private static final String TAG = "SettingsActivity";
    public static final String WORK_REQUEST_NAME = "Lunch reminder";
    final WorkManager workManager = WorkManager.getInstance(this);
    ActivitySettingsBinding binding;


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


        binding.activateReminder.setOnClickListener(v -> {
            activateReminder(workManager);
        });
        binding.cancelReminder.setOnClickListener(v -> cancelReminder(workManager));
    }

    private void activateReminder(WorkManager workManager) {
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        // Set Execution time of the reminder
        dueDate.set(Calendar.HOUR_OF_DAY, 11);
        dueDate.set(Calendar.MINUTE, 50);
        dueDate.set(Calendar.SECOND, 0);
        dueDate.set(Calendar.MILLISECOND, 0);

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

        Data userId = new Data.Builder().build();
        if (getCurrentUser() != null)
            userId = new Data.Builder()
                    .putString(EXTRA_UID, getCurrentUser().getUid())
                    .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, 1,
                TimeUnit.DAYS)
                .setInputData(userId)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .build();

        workManager.enqueueUniquePeriodicWork(WORK_REQUEST_NAME, ExistingPeriodicWorkPolicy.REPLACE, workRequest);
    }

    private void cancelReminder(WorkManager workManager) {
        workManager.cancelAllWork();
        binding.textView.setText(R.string.reminder_deactivated);
    }
}