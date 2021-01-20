package com.example.spark.objects;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class MyFireBaseServices {
    private FirebaseUser firebaseUser;   // Current login user
    private  static MyFireBaseServices instance;
    private FirebaseAuth firebaseAuth;

    public static MyFireBaseServices getInstance() {
        return instance;
    }

    private MyFireBaseServices() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public static void Init(){
        if(instance == null) {
            Log.d("pttt", "Init: MyFireBaseServices");
            instance = new MyFireBaseServices();
        }
    }

    public boolean isLogin() {
        /**
         * function check if user login to system:
         * if user is login ,Method sets the user as the current user and returns true,
         * else Method returns false and not sets user.
         */
        boolean loginSuccess;
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            loginSuccess = false;
        } else {
            setFirebaseUser(firebaseUser);
            Log.d("pttt", "Uid = " + firebaseUser.getUid()
                    + "\nDisplayName = " + firebaseUser.getDisplayName()
                    + "\nEmail = " + firebaseUser.getEmail()
                    + "\nPhoneNumber = " + firebaseUser.getPhoneNumber()
                    + "\nPhotoUrl = " + firebaseUser.getPhotoUrl());
            loginSuccess = true;
        }
        return loginSuccess;
    }

    private void setFirebaseUser(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public String getUID() {
        if(firebaseUser.getUid() == null) {
            return "";
        } else {
            return firebaseUser.getUid();
        }
    }

    public String getUserName() {
        if(firebaseUser.getDisplayName() == null) {
            return "";
        } else {
            return firebaseUser.getDisplayName();
        }
    }

    public String getUserPhone() {
        if(firebaseUser.getPhoneNumber() == null) {
            return "";
        } else {
            return firebaseUser.getPhoneNumber();
        }
    }

    public void signOut() {
        this.firebaseAuth.signOut();
    }

    public void updateUserDisplayName(String name) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        firebaseUser.updateProfile(userProfileChangeRequest);
        firebaseAuth.updateCurrentUser(firebaseUser);
    }
}
