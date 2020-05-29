package com.sophieopenclass.go4lunch.models;

import com.sophieopenclass.go4lunch.utils.DateFormatting;

// Local Restaurant object for users profile
public class Restaurant {
    private String placeId;
    private String name;
    private String address;
    private String urlPhoto;
    private int numberOfStars;
    private String dateOfLunch;

    public Restaurant (){
        //
    }
    public Restaurant(String placeId, String name, String address, String urlPhoto, int numberOfStars) {
        this.placeId = placeId;
        this.name = name;
        this.address = address;
        this.urlPhoto = urlPhoto;
        this.numberOfStars = numberOfStars;
        //dateOfLunch = DateFormatting.getTodayDateInString();
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUrlPhoto() {
        return urlPhoto;
    }

    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }

    public int getNumberOfStars() {
        return numberOfStars;
    }

    public void setNumberOfStars(int numberOfStars) {
        this.numberOfStars = numberOfStars;
    }


    public String getDateOfLunch() {
        return dateOfLunch;
    }

    public void setDateOfLunch(String dateOfLunch) {
        this.dateOfLunch = dateOfLunch;
    }

}
