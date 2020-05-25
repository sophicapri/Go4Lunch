package com.sophieopenclass.go4lunch;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.share.Share;

import java.util.Locale;

import static com.sophieopenclass.go4lunch.base.BaseActivity.PREF_LANGUAGE;

public class MyApplication extends Application {
    public static SharedPreferences sharedPrefs;
    public static final String SHARED_PREFS = "sharedPrefs";

}
