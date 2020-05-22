package com.sophieopenclass.go4lunch.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sophieopenclass.go4lunch.R;
import com.sophieopenclass.go4lunch.controllers.activities.RestaurantDetailsActivity;
import com.sophieopenclass.go4lunch.models.User;

import java.util.ArrayList;

import static android.content.Intent.EXTRA_UID;
import static com.sophieopenclass.go4lunch.injection.Injection.USER_COLLECTION_NAME;
import static com.sophieopenclass.go4lunch.utils.Constants.DATES_AND_PLACE_IDS_FIELD;
import static com.sophieopenclass.go4lunch.utils.Constants.PLACE_ID;

public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";
    private static final int NOTIFICATION_ID = 1;
    private static final int RC_PENDING_INTENT = 44;
    private CollectionReference userCollectionRef;
    private Context context;
    private String chosenRestaurantId;
    private User currentUser;

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
        currentUser = user;
        chosenRestaurantId = user.getDatesAndPlaceIds().get(User.getTodaysDate());
        if (chosenRestaurantId != null) {
            initNotificationMessage();
        }
    }
    private void initNotificationMessage() {
        ArrayList<User> users = new ArrayList<>();
        userCollectionRef.whereEqualTo(DATES_AND_PLACE_IDS_FIELD + User.getTodaysDate(), chosenRestaurantId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                if (task.getResult() != null) {
                    users.addAll(task.getResult().toObjects(User.class));
                    formatNotificationMessage(users);
                }
                else if (task.getException() != null)
                    Log.e(TAG, "getUsersByPlaceId: " + (task.getException().getMessage()));
        });
    }

    private void formatNotificationMessage(ArrayList<User> users) {
        String notificationMessage;
        StringBuilder stringBuilderWorkmates = new StringBuilder();
        if (users.size() != 1) {
            ArrayList<String> workmates = new ArrayList<>();
            for (User workmate : users)
                if (!workmate.getUid().equals(currentUser.getUid()))
                    workmates.add(workmate.getUsername());

            for (int i = 0; i < workmates.size(); i++) {
                stringBuilderWorkmates.append(workmates.get(i));
                if (i < workmates.size() - 2)
                    stringBuilderWorkmates.append(", ");
                else if (i == workmates.size() - 2)
                    stringBuilderWorkmates.append(context.getString(R.string.and));
            }
            notificationMessage = (context.getString(R.string.notification_message,
                    currentUser.getChosenRestaurantName(), currentUser.getChosenRestaurantAddress(), stringBuilderWorkmates.toString()));
        } else
            notificationMessage = (context.getString(R.string.notification_message_solo_lunch,
                    currentUser.getChosenRestaurantName(), currentUser.getChosenRestaurantAddress()));
        displayNotification(notificationMessage);
    }

    private void displayNotification(String notificationMessage) {
        Intent intent = new Intent(context, RestaurantDetailsActivity.class);
        intent.putExtra(PLACE_ID, chosenRestaurantId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, RC_PENDING_INTENT, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper
                .getChannelNotification(notificationMessage, pendingIntent);
        notificationHelper.getManager().notify(NOTIFICATION_ID, nb.build());
    }
}