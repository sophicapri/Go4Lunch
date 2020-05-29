package com.sophieopenclass.go4lunch.models;

import androidx.annotation.Nullable;

import com.sophieopenclass.go4lunch.utils.DateFormatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.sophieopenclass.go4lunch.utils.DateFormatting.getTodayDateInString;

public class User {
    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    private Map<String, Restaurant> datesAndRestaurants = new HashMap<>();
    private String email;
    private Map<String, Restaurant> favoriteRestaurants = new HashMap<>();

    public User() {
    }

    public User(String uid, String username, @Nullable String urlPicture, String email) {
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

    public Map<String, Restaurant> getDatesAndRestaurants() {
        return datesAndRestaurants;
    }

    public void setDatesAndRestaurants(Map<String, Restaurant> datesAndRestaurants) {
        this.datesAndRestaurants = datesAndRestaurants;
    }

    public Map<String, Restaurant> getFavoriteRestaurants() {
        return favoriteRestaurants;
    }

    public void setFavoriteRestaurants(Map<String, Restaurant> favoriteRestaurants) {
        this.favoriteRestaurants = favoriteRestaurants;
    }

    public boolean restaurantNotFavorite(String placeId) {
        for (Restaurant restaurant : favoriteRestaurants.values())
            if (restaurant.getPlaceId().equals(placeId))
                return false;
        return true;
    }

    public boolean restaurantIsSelected(String placeId) {
        if (datesAndRestaurants.get(getTodayDateInString()) != null)
            return Objects.requireNonNull(datesAndRestaurants.get(getTodayDateInString())).getPlaceId().equals(placeId);
        return false;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}


