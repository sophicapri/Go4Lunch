package com.sophieopenclass.go4lunch.models;

public class UserPlaceId extends User {
    private String uid;
    private String placeId;

    public UserPlaceId(String uid, String placeId) {
        this.uid = uid;
        this.placeId = placeId;
    }

    public UserPlaceId() {
    }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }

    public String getPlaceId() { return placeId; }

    public void setPlaceId(String placeId) { this.placeId = placeId; }
}
