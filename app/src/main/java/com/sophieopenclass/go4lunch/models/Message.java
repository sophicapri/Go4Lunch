package com.sophieopenclass.go4lunch.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.Map;

public class Message {
    private String message;
    private Date dateCreated;
    private String userSenderId;
    private String urlImage;

    public Message() { }

    public Message(String message, String userSenderId) {
        this.message = message;
        this.userSenderId = userSenderId;
        this.dateCreated = new Date();
    }

    public Message(String message, String urlImage, String userSenderId) {
        this.message = message;
        this.urlImage = urlImage;
        this.userSenderId = userSenderId;
        this.dateCreated = new Date();
    }

    // --- GETTERS ---
    public String getMessage() { return message; }
    public Date getDateCreated() { return dateCreated; }
    public String getUserSenderId() { return userSenderId; }
    public String getUrlImage() { return urlImage; }

    // --- SETTERS ---
    public void setMessage(String message) { this.message = message; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }
    public void setUserSenderId(String userSenderId) { this.userSenderId = userSenderId; }
    public void setUrlImage(String urlImage) { this.urlImage = urlImage; }
}
