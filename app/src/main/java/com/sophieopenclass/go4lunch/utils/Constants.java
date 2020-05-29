package com.sophieopenclass.go4lunch.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static java.lang.annotation.RetentionPolicy.SOURCE;

public class Constants {
    public static final String PLACE_ID = "placeId";
    public static final String DATES_AND_RESTAURANTS_FIELD = "datesAndRestaurants.";
    public static final String FAVORITE_RESTAURANTS_FIELD = "favoriteRestaurants.";
    public static final String PLACE_ID_FIELD = ".placeId";

    //
    public static final String USERNAME_FIELD = "username";
    public static final String NAME_RESTAURANT = "name";
    public static final String ADDRESS_RESTAURANT = "address";
    //

    public static final String MESSAGES_SUBCOLLECTION = "messages";
    public static final String DATE_CREATED = "dateCreated";
    public static final String PARTICIPANTS_FIELD = "participants.";
    public static final String USER_SENDER_ID = "userSenderId";


    public static final int ACTIVITY_MY_LUNCH = 3;
    public static final int ACTIVITY_SETTINGS = 1;
    public static final int FRAGMENT_MAP_VIEW = 10;
    public static final int FRAGMENT_RESTAURANT_LIST_VIEW = 20;
    public static final int FRAGMENT_WORKMATES_LIST = 30;


    public static final String PREF_LANGUAGE = "pref_language";
    public static final String PREF_REMINDER = "pref_reminder";
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String WORK_REQUEST_NAME = "Lunch reminder";

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    public static final String PERMS = ACCESS_FINE_LOCATION;


    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY = 4;
    public static final int FRIDAY = 5;
    public static final int SATURDAY = 6;
    public static final int SUNDAY = 0;
}
