package com.sophieopenclass.go4lunch.models;

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
    }

    // --- GETTERS ---
    public String getPlaceId() {
        return placeId;
    }
    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public String getUrlPhoto() {
        return urlPhoto;
    }
    public int getNumberOfStars() {
        return numberOfStars;
    }
    public String getDateOfLunch() {
        return dateOfLunch;
    }

    // --- SETTERS ---
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDateOfLunch(String dateOfLunch) {
        this.dateOfLunch = dateOfLunch;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }
    public void setNumberOfStars(int numberOfStars) {
        this.numberOfStars = numberOfStars;
    }
}
