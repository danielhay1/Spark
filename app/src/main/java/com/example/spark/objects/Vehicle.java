
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
    private ArrayList<String> ownersName = new ArrayList<String>();    //******

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

    public ArrayList<String> getOwnersName() {
        return ownersName;
    }

    public void removeOwner(String uid) {   //***
        if(!ownersUID.isEmpty() && !ownersName.isEmpty()) {
            int index = ownersUID.indexOf(uid);
            ownersUID.remove(index);
            ownersName.remove(index);
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

    public void changeOwnerName(String uid,String userName) {
        if(this.ownersUID.contains(uid)) {
            int index = ownersUID.indexOf(uid);
            this.ownersName.set(index,userName);
        }
    }

    public boolean hasNoOwners() {
        return ownersUID.isEmpty();
    }

    public void addOwnerName(String userName) {
        if(!userName.equalsIgnoreCase("")) {
            this.ownersName.add(userName);
            Log.e("pttt", "userDetailsUpdated: owner_name = "+ownersNamesToString());
        }
        else {
            this.ownersName.add("");
        }
    }

    public String ownersNamesToString() {
        String ownerNamesToString = "(";
        if(!this.ownersName.isEmpty()) {
            for (String ownerName: ownersName) {
                if(!ownerName.equalsIgnoreCase("")) {
                    ownerNamesToString += ownerName + ", ";
                }
            }
            ownerNamesToString = ownerNamesToString.substring(0,ownerNamesToString.length()-2); //delete the last ", " in ownerName String.
        }
        return ownerNamesToString + ")";
    }


    @NonNull
    @Override
    public String toString() {
        return "Vehicle{" +
                "vehicle_id='" + vehicleID + '\'' +
                ", vehicle_nick='" + vehicleNick + '\'' +
                ", owners_uid='" + ownersUID.toString() + '\'' +
                ", owners_name='" + ownersName.toString() + '\'' +
                '}';
    }
}