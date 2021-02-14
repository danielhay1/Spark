package com.example.spark.objects;

import com.example.spark.untils.MyFireBaseServices;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public class Vehicle {
    private String vehicleID = "";
    private String vehicleNick = "";
    private ArrayList<String> ownersUID = new ArrayList<String>();
    private LatLng parkingLocation = new LatLng(0, 0);

    public Vehicle() {
    }

    public String getVehicleID() {
        return vehicleID;
    }

    public Vehicle setVehicleID(String vehicleLicenceNumber) {
        this.vehicleID = vehicleLicenceNumber;
        return this;
    }

    public String getVehicleNick() {
        return vehicleNick;
    }

    public Vehicle setVehicleNick(String vehicleNick) {
        this.vehicleNick = vehicleNick;
        return this;
    }

    public void addOwner(String uid) {
        this.ownersUID.add(uid);
    }

    public void removeOwner(String uid) {
        this.ownersUID.remove(uid);
    }

    public boolean isOwnedBy(String uid) {
        for (String userId:ownersUID) {
            if(userId.equals(uid)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getOwnersUID() {
        return ownersUID;
    }

/*    private void getOwnerName(String uid) {
        MyFireBaseServices.getInstance().loadUserFromFireBase(uid, new MyFireBaseServices.CallBack_LoadUser() {
            @Override
            public void userDetailsUpdated(User result) {
                res+=result.getName();
            }
        });
    }*/

    public String getOwnersName() {
        String res = "( ";
        for (String userId:ownersUID) {

        }
        res+=" )";
        return res;
    }

    public Vehicle setParkingLocation(LatLng parkingLocation) {
        this.parkingLocation = parkingLocation;
        return this;
    }

    public LatLng getParkingLocation() {
        return parkingLocation;
    }
}
