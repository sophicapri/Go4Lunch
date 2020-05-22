package com.sophieopenclass.go4lunch.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.models.User;

import java.util.ArrayList;

import static android.content.Intent.EXTRA_UID;
import static com.sophieopenclass.go4lunch.injection.Injection.USER_COLLECTION_NAME;

public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";
    private static final int NOTIFICATION_ID = 1;
    private CollectionReference userCollectionRef;
    private Context context;

    public NotificationWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "doWork: ");
        userCollectionRef = FirebaseFirestore.getInstance().collection(USER_COLLECTION_NAME);
        String userId = getInputData().getString(EXTRA_UID);
        getUserAndShowNotification(userId);
        return Result.success();
    }


    private void getUserAndShowNotification(String userId) {
        final User[] user = {new User()};
        userCollectionRef.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                if (task.getResult() != null) {
                    user[0] = task.getResult().toObject(User.class);
                    if (user[0] != null) {
                        checkIfUserHasChosenARestaurant(user[0]);
                    }
                } else if (task.getException() != null)
                    Log.e(TAG, "getUser " + (task.getException().getMessage()));
        });
    }

    private void checkIfUserHasChosenARestaurant(User user) {
        String chosenRestaurantId = user.getDatesAndPlaceIds().get(User.getTodaysDate());
        if (chosenRestaurantId != null) {
            initNotificationMessage(user, chosenRestaurantId);
        }
    }
    private void initNotificationMessage(User user, String chosenPlaceId) {
        ArrayList<User> users = new ArrayList<>();
        userCollectionRef.whereEqualTo("datesAndPlaceIds." + User.getTodaysDate(), chosenPlaceId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                if (task.getResult() != null) {
                    users.addAll(task.getResult().toObjects(User.class));
                    formatNotificationMessage(user, users);
                }
                else if (task.getException() != null)
                    Log.e(TAG, "getUsersByPlaceId: " + (task.getException().getMessage()));
        });
    }

    private void formatNotificationMessage(User user, ArrayList<User> users) {
        String notificationMessage;
        StringBuilder stringBuilderWorkmates = new StringBuilder();
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
            notificationMessage = (context.getString(R.string.notification_message,
                    user.getChosenRestaurantName(), stringBuilderWorkmates.toString()));
        } else
            notificationMessage = (context.getString(R.string.notification_message_solo_lunch,
                    user.getChosenRestaurantName()));

        displayNotification(notificationMessage);
    }

    private void displayNotification(String notificationMessage) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper
                .getChannelNotification(notificationMessage);
        notificationHelper.getManager().notify(NOTIFICATION_ID, nb.build());
    }
}