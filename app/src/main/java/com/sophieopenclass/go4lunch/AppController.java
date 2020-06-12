package com.sophieopenclass.go4lunch;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

import com.sophieopenclass.go4lunch.utils.PreferenceHelper;

import java.util.Locale;

import static com.sophieopenclass.go4lunch.utils.Constants.PREF_LANGUAGE;

public class AppController extends Application {
    private static AppController instance;
    private Location currentLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        PreferenceHelper.initPreferenceHelper(this);
        PreferenceHelper.initReminderPreference();
    }

    public static AppController getInstance() {
        return instance;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void checkCurrentLocale(Context context) {
        String defaultLocale = Locale.getDefault().getLanguage();
        if (!PreferenceHelper.getSharedPrefs().contains(PREF_LANGUAGE))
            PreferenceHelper.setCurrentLocale(defaultLocale);
        else if (!PreferenceHelper.getCurrentLocale().equals(defaultLocale))
            updateLocale(context);
    }

    public void updateLocale(Context context) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(PreferenceHelper.getCurrentLocale()));
        res.updateConfiguration(conf, dm);
    }

    // Used when the user moves the camera on the map
    public String getLatLngString(Location currentLocation) {
        return currentLocation.getLatitude() + "," + currentLocation.getLongitude();
    }

    public String getLatLngString() {
        return currentLocation.getLatitude() + "," + currentLocation.getLongitude();
    }
}
