package com.sophieopenclass.go4lunch.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class Constants {
    public static final String UID = "uid";
    public static final String PLACE_ID = "placeId";
    public static final String MY_LUNCH = "myLunch";

    public static final int RESTAURANT_ACTIVITY = 97;
    public static final int WORKMATES_FRAGMENT = 96;
    @Retention(SOURCE)

    @IntDef({RESTAURANT_ACTIVITY, WORKMATES_FRAGMENT})
    public @interface Controller{}


    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY= 3;
    public static final int THURSDAY = 4;
    public static final int FRIDAY= 5;
    public static final int SATURDAY = 6;
    public static final int SUNDAY = 0;
}
