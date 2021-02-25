
package com.example.spark.objects;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.spark.untils.MyFireBaseServices;
import com.example.spark.untils.MyPreference;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public class Vehicle {

    private String vehicleID = "";
    private String vehicleNick = "";
    private ArrayList<String> ownersUID = new ArrayList<String>();

    public Vehicle() { }

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

    public ArrayList<String> getOwnersUID() {
        return ownersUID;
    }



    public void removeOwner(String uid) {   //***
        if(!ownersUID.isEmpty()) {
            int index = ownersUID.indexOf(uid);
            ownersUID.remove(index);
        }
    }

    public boolean isOwnedBy(String uid) {
        for (String userId:ownersUID) {
            if(userId.equals(uid)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasNoOwners() {
        return ownersUID.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return "Vehicle{" +
                "vehicle_id='" + vehicleID + '\'' +
                ", vehicle_nick='" + vehicleNick + '\'' +
                ", owners_uid='" + ownersUID.toString() + '\'' +
                //", owners_name='" + ownersName.toString() + '\'' +
                '}';
    }
}