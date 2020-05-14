package com.sophieopenclass.go4lunch.models;

import java.util.Calendar;
import java.util.Date;

public class Restaurant {
    private String placeId;
    private Date date;
    private String name;


    public Restaurant(String placeId, Date date, String name) {
        this.placeId = placeId;
        this.date = date;
        this.name = name;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
