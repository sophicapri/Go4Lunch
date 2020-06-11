package com.sophieopenclass.go4lunch;

import android.app.Application;
import android.location.Location;

public class AppController extends Application {
    private static AppController instance;
    private Location currentLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
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

}
