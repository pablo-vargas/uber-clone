package com.example.taxiserv.utils;


import com.example.taxiserv.models.Client;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthenticationAPI {
    private FirebaseAuth mAuth;

    public FirebaseAuthenticationAPI() {
        mAuth = FirebaseAuth.getInstance();
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public Client getAuthClient() {
        Client client = new Client();
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            client.setUid(mAuth.getCurrentUser().getUid());
            client.setUsername(mAuth.getCurrentUser().getDisplayName());
            client.setEmail(mAuth.getCurrentUser().getEmail());
            client.setPhotoUrl(mAuth.getCurrentUser().getPhotoUrl().toString());
        }
        return client;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void signOut() {
        if(mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }
}
