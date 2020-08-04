package com.example.taxiserv.utils;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseStorageAPI {
    private final static String PATH_PROFILE = "profile";
    private final static String PATH_MY_PROFILE = "my_profile";

    private FirebaseStorage mStorage;

    public FirebaseStorageAPI() {
        mStorage = FirebaseStorage.getInstance();
    }

    public StorageReference getRootReference() {
        return mStorage.getReference();
    }

    public StorageReference getProfileReferenceByEmail(String email) {
        return getRootReference().child(email).child(PATH_PROFILE).child(PATH_MY_PROFILE);
    }
}
