package com.sophieopenclass.go4lunch.models;

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
    private Map<String, String> datesAndPlaceIds = new HashMap<>();
    private String chosenRestaurantName;

    public User() {
    }

    public User(String uid, String username, @Nullable String urlPicture) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
    }

    // --- GETTERS ---
    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    @Nullable
    public String getUrlPicture() {
        return urlPicture;
    }

    // --- SETTERS ---
    public void setUsername(String username) {
        this.username = username;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUrlPicture(@Nullable String urlPicture) {
        this.urlPicture = urlPicture;
    }

    // Always save the date as Locale.US in Firebase
    public static String getTodaysDate() {
        Date date = new Date();
        String formatted = date.toString().substring(4, 10) + " " + date.toString().substring(24);
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy", Locale.US);
        try {
            date = formatter.parse(formatted);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        if (date != null)
            return dateFormat.format(date);
        return "";
    }

    // To display the date of the previous restaurants in WorkmatesDetailActivity
    // either in French or in English
    public static String formatDate(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Date date = null;
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        if (date != null)
            return dateFormat.format(date);
        return "";
    }

    public Map<String, String> getDatesAndPlaceIds() {
        return datesAndPlaceIds;
    }

    public void setDatesAndPlaceIds(Map<String, String> datesAndPlaceIds) {
        this.datesAndPlaceIds = datesAndPlaceIds;
    }

    public String getChosenRestaurantName() {
        return chosenRestaurantName;
    }

    public void setChosenRestaurantName(String chosenRestaurantName) {
        this.chosenRestaurantName = chosenRestaurantName;
    }
}
