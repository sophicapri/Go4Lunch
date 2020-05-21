package com.sophieopenclass.go4lunch.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.sophieopenclass.go4lunch.MyViewModel;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.injection.Injection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationReceiver  extends BroadcastReceiver {
    MyViewModel viewModel;
    public static final String NOTIFICATION_MESSAGE = "message";

    @Override
    public void onReceive(Context context, Intent intent) {
        configureViewModel(context);
        String uid = "";

        if (intent.hasExtra(Intent.EXTRA_UID))
            uid = intent.getStringExtra(Intent.EXTRA_UID);


        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(getNotificationMessage(context, uid));
        notificationHelper.getManager().notify(1, nb.build());
    }

    private void configureViewModel(Context context) {
            ViewModelFactory viewModelFactory = Injection.provideViewModelFactory();
            viewModel = new ViewModelProvider((ViewModelStoreOwner) context, viewModelFactory).get(MyViewModel.class);
    }


    public String getNotificationMessage(Context context, String uid) {
        viewModel.getUser(uid).observe((LifecycleOwner) context, user -> {

        });


        String message = "";
        String restaurant = "Yummy";
        StringBuilder workmates = new StringBuilder();
        List<String> workmatesList = new ArrayList<>(Arrays.asList("Zendaya", "Johnny", "Gabrielle"));


        for (int i = 0; i < workmatesList.size(); i++)
            if (i != workmatesList.size() - 1)
                workmates.append(workmatesList.get(i)).append(", ");
            else
                workmates.append(workmatesList.get(i)).append(".");


        message = context.getString(R.string.notification_message, restaurant, workmates.toString());
        return message;
    }
}
