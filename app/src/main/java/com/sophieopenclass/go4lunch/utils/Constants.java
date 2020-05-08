package com.sophieopenclass.go4lunch.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class Constants {
    public static final int RESTAURANT_ACTIVITY = 97;
    public static final int WORKMATES_FRAGMENT = 96;
    @Retention(SOURCE)

    @IntDef({RESTAURANT_ACTIVITY, WORKMATES_FRAGMENT})
    public @interface Controller{}
}
