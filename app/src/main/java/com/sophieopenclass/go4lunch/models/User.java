package com.sophieopenclass.go4lunch.models;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.sophieopenclass.go4lunch.utils.Constants.ADDRESS_RESTAURANT;
import static com.sophieopenclass.go4lunch.utils.Constants.NAME_RESTAURANT;

public class User {
    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    private Map<String, String> datesAndPlaceIds = new HashMap<>();
    private Map<String, String> chosenRestaurant;
    private String email;
    private List<String> favoriteRestaurantIds = new ArrayList<>();

    public User() {
    }

    public User(String uid, String username, @org.jetbrains.annotations.Nullable String urlPicture, String email) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.email = email;
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
        if (urlPicture == null)
            return "https://i.ibb.co/QHLLGNk/nopicc.png";
        else
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

    public Map<String, String> getDatesAndPlaceIds() {
        return datesAndPlaceIds;
    }

    public void setDatesAndPlaceIds(Map<String, String> datesAndPlaceIds) {
        this.datesAndPlaceIds = datesAndPlaceIds;
    }

    public Map<String, String> getChosenRestaurant() {
        return chosenRestaurant;
    }

    public void setChosenRestaurant(Map<String, String> chosenRestaurant) {
        this.chosenRestaurant = chosenRestaurant;
    }

    public List<String> getFavoriteRestaurantIds() {
        return favoriteRestaurantIds;
    }

    public void setFavoriteRestaurantIds(List<String> favoriteRestaurantIds) {
        this.favoriteRestaurantIds = favoriteRestaurantIds;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}


