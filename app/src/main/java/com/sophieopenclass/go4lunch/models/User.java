package com.sophieopenclass.go4lunch.models;

import android.icu.text.RelativeDateTimeFormatter;
import android.util.ArrayMap;

import androidx.annotation.Nullable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class User {
    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    private Map<String, String> datesAndPlacesIds = new HashMap<>();

    public User() { }

    public User(String uid, String username, @Nullable String urlPicture) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    @Nullable
    public String getUrlPicture() { return urlPicture; }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(@Nullable String urlPicture) { this.urlPicture = urlPicture; }

    public static Date getTodaysDate(){
        Date date = null;
        try {
            DateFormat formatter = SimpleDateFormat.getDateInstance(DateFormat.SHORT, Locale.FRENCH);
            date = formatter.parse(formatter.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public Map<String, String> getDatesAndPlacesIds() {
        return datesAndPlacesIds;
    }

    public void setDatesAndPlacesIds(Map<String, String> datesAndPlacesIds) {
        this.datesAndPlacesIds = datesAndPlacesIds;
    }

}
