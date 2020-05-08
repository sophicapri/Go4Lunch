package com.sophieopenclass.go4lunch.models;

import androidx.annotation.Nullable;

import com.sophieopenclass.go4lunch.models.json_to_java.PlaceDetails;

public class User {
    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    @Nullable
    private PlaceDetails restaurantChosen;
    private String restaurantName;

    @Nullable
    public PlaceDetails getRestaurantChosen() {
        return restaurantChosen;
    }

    public void setRestaurantChosen(@Nullable PlaceDetails restaurantChosen) {
        this.restaurantChosen = restaurantChosen;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    private String placeId;

    public User() { }

    public User(String uid, String username, @Nullable String urlPicture, String placeId) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.placeId = placeId;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    @Nullable
    public String getUrlPicture() { return urlPicture; }
    public PlaceDetails getChosenRestaurant(){ return restaurantChosen;}

    //PROVISOIRE
    public String getPlaceId(){return placeId;}
    public void setPlaceId(String placeId){this.placeId = placeId;}


    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(@Nullable String urlPicture) { this.urlPicture = urlPicture; }
}
