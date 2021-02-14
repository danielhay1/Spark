package com.example.spark.untils;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.spark.objects.User;
import com.example.spark.objects.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyFireBaseServices {
    private FirebaseUser firebaseUser;   // Current login user
    private  static MyFireBaseServices instance;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private User currentUser;
    private Vehicle vehicle;

    public static MyFireBaseServices getInstance() {
        return instance;
    }

    private final String MY_USERS = "users";
    private final String MY_VEHICLES = "vehicles";

    public interface CallBack_LoadVehicle {
        void vehicleDetailsUpdated(Vehicle result);
    }
    public interface CallBack_LoadUser {
        void userDetailsUpdated(User result);
    }

    private MyFireBaseServices() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.database = FirebaseDatabase.getInstance();
    }

    public static void Init(){
        if(instance == null) {
            Log.d("pttt", "Init: MyFireBaseServices");
            instance = new MyFireBaseServices();
        }
    }

    public boolean login() {
        /**
         * function check if user is login to system:
         * if user is login ,Method sets the user as the current user and returns true.
         * else Method returns false and not sets user.
         * */
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        boolean loginSuccess;
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

    public void saveUserToFireBase(User user) {
        // Write a message to the database
        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(MY_USERS);
        myRef.child(user.getUid()).setValue(user);
    }

    public void saveVehicleToFireBase(Vehicle vehicle) {
        // Write a message to the database
        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(MY_VEHICLES);
        myRef.child(vehicle.getVehicleID()).setValue(vehicle);
    }

    public void loadUserFromFireBase(String userId, CallBack_LoadUser callBack_loadUser) {
        DatabaseReference myRef = database.getReference(MY_USERS);
        myRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User value = snapshot.getValue(User.class);
                Log.d("pttt", "Value is: "+ value);
                callBack_loadUser.userDetailsUpdated(value);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("pttt", "Failed to read value ",error.toException());
            }
        });

    }

    public void loadVehicleFromFireBase(String vehicleId, CallBack_LoadVehicle callBack_loadVehicle) {
        Vehicle v = new Vehicle();
        DatabaseReference myRef = database.getReference(MY_USERS);
        myRef.child(vehicleId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Vehicle value = snapshot.getValue(Vehicle.class);
                Log.d("pttt", "Value is: "+ value);
                callBack_loadVehicle.vehicleDetailsUpdated(value);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("pttt", "Failed to read value ",error.toException());
            }
        });
    }
}
