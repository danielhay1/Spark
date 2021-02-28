package com.example.spark.untils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.spark.objects.Parking;
import com.example.spark.objects.User;
import com.example.spark.objects.Vehicle;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyFireBaseServices {
    private FirebaseUser firebaseUser;   // Current login user
    private  static MyFireBaseServices instance;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;

    public static MyFireBaseServices getInstance() {
        return instance;
    }

    private final String MY_USERS = "users";
    private final String MY_VEHICLES = "vehicles";
    private final String MY_PARKING = "parking";
    private final String MY_PARKING_HISTORY = "parking_history";

    public interface CallBack_LoadVehicle {
        void vehicleDetailsUpdated(Vehicle result);
        void loadFailed(Exception e);
    }
    public interface CallBack_LoadParking {
        void parkingLocationUpdated(Parking parking);
        void loadParkingHistory(ArrayList<Parking> parkings);
        void loadFailed(Exception e);
    }
    public interface CallBack_LoadUser {
        void userDetailsUpdated(User result);
        void loadFailed(Exception e);
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
        DatabaseReference myRef = database.getReference(MY_USERS);
        myRef.child(user.getUid()).setValue(user);
    }

    public void saveVehicleToFireBase(Vehicle vehicle) {
        if(!vehicle.getVehicleID().equalsIgnoreCase("")) {
            DatabaseReference myRef = database.getReference(MY_VEHICLES);
            myRef.child(vehicle.getVehicleID()).setValue(vehicle);
        }
    }

    public void saveParkingToFireBase(Parking parking) {
        if(!parking.getVehicleId().equalsIgnoreCase("")) {
            DatabaseReference myRef = database.getReference(MY_PARKING);
            myRef.child(parking.getVehicleId()).setValue(parking);
        }
        DatabaseReference myRef2 = database.getReference(MY_PARKING_HISTORY).child(parking.getVehicleId()).child(parking.getTime());
        myRef2.setValue(parking);
    }

    public void deleteVehicleFromFireBase(String vehicleId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(MY_VEHICLES);
        Query applesQuery = ref.child(vehicleId).equalTo(vehicleId);
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null) {
                    Log.d("ptt", "DELETE VEHICLE!");
                    deletePakringFromFireBase(vehicleId);
                    deleteParkingHistoryFromFireBase(vehicleId);
                    dataSnapshot.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("pttt", "onCancelled", databaseError.toException());
            }
        });
    }
    public void deleteParkingHistoryFromFireBase(String vehicleId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(MY_PARKING_HISTORY);
        Query applesQuery = ref.child(vehicleId).equalTo(vehicleId);
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null) {
                    Log.d("ptt", "DELETE PARKING HISTORY!");
                    dataSnapshot.getRef().removeValue();
                } else {
                    Log.d("pttt", "onDataChange:no vehicle found!");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("pttt", "onCancelled:", error.toException());
            }
        });
    }

    public void deletePakringFromFireBase(String vehicleId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(MY_PARKING);
        Query applesQuery = ref.child(vehicleId).equalTo(vehicleId);
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null) {
                    Log.d("ptt", "DELETE PARKING!" );
                    dataSnapshot.getRef().removeValue();
                } else {
                    Log.d("pttt", "onDataChange:no vehicle found!");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("pttt", "onCancelled:", error.toException());
            }
        });
    }

    public void loadUserFromFireBase(String userId, CallBack_LoadUser callBack_loadUser) {
        DatabaseReference myRef = database.getReference(MY_USERS);
        if(userId!=null){
            myRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot != null) {
                        User value = snapshot.getValue(User.class);
                        Log.d("pttt", "Value is: "+ value);
                        callBack_loadUser.userDetailsUpdated(value);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("pttt", "Failed to read value ",error.toException());
                }
            });
        }
    }

    public void loadVehicleFromFireBase(String vehicleId, CallBack_LoadVehicle callBack_loadVehicle) {
        DatabaseReference myRef = database.getReference(MY_VEHICLES);
        myRef.child(vehicleId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot != null) {
                    Vehicle value = snapshot.getValue(Vehicle.class);
                    Log.d("pttt", "Value is: "+ value);
                    callBack_loadVehicle.vehicleDetailsUpdated(value);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callBack_loadVehicle.loadFailed(error.toException());
            }
        });
    }

    public void loadParkingLocation(String vehicleId, CallBack_LoadParking callBack_loadParking) {
        DatabaseReference myRef = database.getReference(MY_PARKING);
        myRef.child(vehicleId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    Parking value = snapshot.getValue(Parking.class);
                    if(value!=null) {
                        Log.d("pttt", "onDataChange:\t parking="+value);
                        callBack_loadParking.parkingLocationUpdated(value);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callBack_loadParking.loadFailed(error.toException());
            }
        });
    }

    public void loadParkingHistoryFromFireBase(String vehicleId, int parkingsAmount ,CallBack_LoadParking callBack_loadParking){
        ArrayList<Parking> parkings = new ArrayList<Parking>();
        DatabaseReference myRef = database.getReference(MY_PARKING_HISTORY);
        myRef.child(vehicleId).limitToLast(parkingsAmount).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    for (DataSnapshot data: snapshot.getChildren()) {
                        if(data!=null) {
                            Parking parking = data.getValue(Parking.class);
                            parkings.add(parking);
                        }
                    }
                    if(!parkings.isEmpty()) {
                        Log.d("pttt", "Parking history - Value is: "+ parkings+"\nNumOFParkings= "+parkings.size());
                        callBack_loadParking.loadParkingHistory(parkings);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callBack_loadParking.loadFailed(error.toException());
            }
        });
    }
}