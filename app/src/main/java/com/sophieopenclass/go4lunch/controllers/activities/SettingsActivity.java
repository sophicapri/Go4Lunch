package com.sophieopenclass.go4lunch.controllers.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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

public class SettingsActivity extends BaseActivity<MyViewModel> {
    private static final String TAG = "SettingsActivity";
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

        workManager.pruneWork();
        workManager.cancelAllWork();

        binding.activateReminder.setOnClickListener(v -> {
            activateReminder(workManager);
        });
        binding.cancelReminder.setOnClickListener(v -> cancelReminder(workManager));
    }

    private void activateReminder(WorkManager workManager) {
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        // Set Execution time
        dueDate.set(Calendar.HOUR_OF_DAY, 22);
        dueDate.set(Calendar.MINUTE, 56);
        dueDate.set(Calendar.SECOND, 0);
        dueDate.set(Calendar.MILLISECOND, 0);

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, 1,
                TimeUnit.DAYS)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag("REMINDER")
                .build();


        workManager.enqueueUniquePeriodicWork("REMINDERR", ExistingPeriodicWorkPolicy.REPLACE, workRequest);
        workManager.getWorkInfoByIdLiveData(workRequest.getId()).observe(this, workInfo -> {
            System.out.println("work info : " + workInfo.getState().toString());
            if (workInfo.getState().isFinished()) {
                    if (getCurrentUser() != null)
                        viewModel.getUser(getCurrentUser().getUid()).observe(this, user -> {
                                    String chosenPlaceId = user.getDatesAndPlaceIds().get(User.getTodaysDate());
                                    if (chosenPlaceId != null) {
                                        NotificationHelper notificationHelper = new NotificationHelper(this);
                                        getNotificationMessage(user, chosenPlaceId).observe(this, notificationMessage -> {
                                            NotificationCompat.Builder nb = notificationHelper
                                                    .getChannelNotification(notificationMessage);
                                            notificationHelper.getManager().notify(1, nb.build());
                                        });

                                    }
                                }
                        );
                }
        });
    }

    public LiveData<String> getNotificationMessage(User user, String chosenPlaceId) {
        MutableLiveData<String> notificationMessage = new MutableLiveData<>();
        StringBuilder stringBuilderWorkmates = new StringBuilder();
        viewModel.getUsersByPlaceIdAndDate(chosenPlaceId, User.getTodaysDate())
                .observe(this, users -> {
                    if (users.size() != 1) {
                        ArrayList<String> workmates = new ArrayList<>();
                        for (User workmate : users)
                            if (!workmate.getUid().equals(user.getUid()))
                                workmates.add(workmate.getUsername());

                        for (int i = 0; i < workmates.size(); i++) {
                            stringBuilderWorkmates.append(workmates.get(i));
                            if (i < workmates.size() - 2)
                                stringBuilderWorkmates.append(", ");
                            else if (i == workmates.size() - 2)
                                stringBuilderWorkmates.append(" et ");
                            if (i == workmates.size() - 1)
                                stringBuilderWorkmates.append(".");
                        }
                        notificationMessage.setValue(getString(R.string.notification_message,
                                user.getChosenRestaurantName(), stringBuilderWorkmates.toString()));
                    } else
                        notificationMessage.setValue(getString(R.string.notification_message_solo_lunch,
                                user.getChosenRestaurantName()));
                });
        return notificationMessage;
    }

    private void cancelReminder(WorkManager workManager) {
        workManager.cancelAllWork();
        binding.textView.setText("Rappel journalier désactivé");
    }
}