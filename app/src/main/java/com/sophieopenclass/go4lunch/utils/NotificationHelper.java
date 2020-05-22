package com.sophieopenclass.go4lunch.utils;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.sophieopenclass.go4lunch.R;

public class NotificationHelper extends ContextWrapper {
    public static final String channelID = "channelID";
    public static final String LUNCH_REMINDER = "Lunch reminder";
    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, LUNCH_REMINDER, NotificationManager.IMPORTANCE_HIGH);
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public NotificationCompat.Builder getChannelNotification(String message, PendingIntent intent) {
        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle("C'est bientôt l'heure !")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_logo_auth);
    }
}