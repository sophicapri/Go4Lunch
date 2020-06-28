package com.sophieopenclass.go4lunch.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.sophieopenclass.go4lunch.utils.Constants.PREF_LANGUAGE;
import static com.sophieopenclass.go4lunch.utils.Constants.PREF_REMINDER;
import static com.sophieopenclass.go4lunch.utils.Constants.SHARED_PREFS;

public class PreferenceHelper {
    private static SharedPreferences sharedPrefs;

    public static void initPreferenceHelper(Context context) {
        sharedPrefs = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        initReminderPreference();
    }

    private static void initReminderPreference() {
        if (!sharedPrefs.contains(PREF_REMINDER))
            sharedPrefs.edit().putBoolean(PREF_REMINDER, true).apply();
    }

    public static SharedPreferences getSharedPrefs(){
        return sharedPrefs;
    }

    public static boolean getReminderPreference(){
        return sharedPrefs.getBoolean(PREF_REMINDER, false);
    }

    public static void setReminderPreference(boolean notificationState){
        sharedPrefs.edit().putBoolean(PREF_REMINDER, notificationState).apply();
    }

    public static String getCurrentLocale(){
        return sharedPrefs.getString(PREF_LANGUAGE, Locale.getDefault().getLanguage());
    }

    public static void setCurrentLocale(String locale){
        sharedPrefs.edit().putString(PREF_LANGUAGE, locale).apply();
    }
}
