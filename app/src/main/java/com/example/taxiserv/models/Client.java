package com.example.taxiserv.models;

import com.google.firebase.database.Exclude;

public class Client {
    public static final String UID = "uid";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String NUMBER = "number";
    public static final String PHOTO_URL = "photoUrl";

    @Exclude
    private String uid;
    private String username;
    private String email;
    private String number;
    private String photoUrl;

    public Client() {
    }
    @Exclude
    public String getUid() {
        return uid;
    }
    @Exclude
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
