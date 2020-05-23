package com.sophieopenclass.go4lunch.controllers.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.base.BaseActivity;
import com.sophieopenclass.go4lunch.databinding.ActivitySettingsBinding;
import com.sophieopenclass.go4lunch.utils.NotificationWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.EXTRA_UID;

public class SettingsActivity extends BaseActivity<MyViewModel> {
    private static final String TAG = "SettingsActivity";
    public static final String WORK_REQUEST_NAME = "Lunch reminder";
    public final WorkManager workManager = WorkManager.getInstance(this);
    ActivitySettingsBinding binding;
    public static PeriodicWorkRequest workRequest;

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
        binding.notificationToggle.setOnClickListener(v -> {
            if (binding.notificationToggle.isChecked())
                activateReminder(workManager);
            else
                workManager.cancelAllWork();
        });
    }

    private void activateReminder(WorkManager workManager) {
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        // Set Execution time of the reminder
        dueDate.set(Calendar.HOUR_OF_DAY, 19);
        dueDate.set(Calendar.MINUTE, 56);
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

        workRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, 1,
                TimeUnit.DAYS)
                .setInputData(userId)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .build();

        workManager.enqueueUniquePeriodicWork(WORK_REQUEST_NAME, ExistingPeriodicWorkPolicy.REPLACE, workRequest);
    }
}