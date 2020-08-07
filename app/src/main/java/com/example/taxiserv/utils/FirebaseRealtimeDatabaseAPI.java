package com.example.taxiserv.utils;

import android.util.Log;

import com.example.taxiserv.models.Client;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FirebaseRealtimeDatabaseAPI {
    private final static String PATH_USERS = "users";
    private final static String PATH_CLIENT = "client";
    private final static String PATH_PROFILE = "profile";

    private FirebaseDatabase mDatabase;

    public FirebaseRealtimeDatabaseAPI() {
        mDatabase = FirebaseDatabase.getInstance();
    }

    public FirebaseDatabase getmDatabase() {
        return mDatabase;
    }

    public DatabaseReference getRootReference() {
        return mDatabase.getReference();
    }

    public DatabaseReference getByIdReference(String uid) {
        return getRootReference().child(PATH_USERS).child(PATH_CLIENT).child(uid);
    }

    public DatabaseReference getProfileByIdReference(String uid) {
        return getByIdReference(uid).child(PATH_PROFILE);
    }
}
