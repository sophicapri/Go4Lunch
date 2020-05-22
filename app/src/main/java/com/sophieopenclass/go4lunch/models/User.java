package com.sophieopenclass.go4lunch.models;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class User {
    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    private Map<String, String> datesAndPlaceIds = new HashMap<>();
    private String chosenRestaurantName;
    private List<String> favoriteRestaurantIds = new ArrayList<>();

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
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        return formatter.format(date);
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

    public List<String> getFavoriteRestaurantIds() {
        return favoriteRestaurantIds;
    }

    public void setFavoriteRestaurantIds(List<String> favoriteRestaurantIds) {
        this.favoriteRestaurantIds = favoriteRestaurantIds;
    }
}
