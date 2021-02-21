package com.example.spark.objects;

import java.util.ArrayList;

public class User {
    private String uid = "";
    private String name = "";
    private String phone = "";
    private String connectedVehicleID = "";
    private ArrayList<String> myVehicles = new ArrayList<String>();

    public User() { }

    public String getUid() {
        return uid;
    }

    public User setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public User setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getConnectedVehicleID() {
        return connectedVehicleID;
    }

    public User setConnectedVehicleID(String connectedVehicleID) {
        if(!this.isOwnedVehicle(connectedVehicleID) && !connectedVehicleID.equalsIgnoreCase("")) {
            this.myVehicles.add(connectedVehicleID);
        }
        this.connectedVehicleID = connectedVehicleID;
        return this;
    }

    public void removeVehicle(String vehicleId) {
        if(!myVehicles.isEmpty()) {
            setConnectedVehicleID(myVehicles.get(myVehicles.size()-1));
        } else {
            setConnectedVehicleID("");
        }
        this.myVehicles.remove(vehicleId);
    }

    public ArrayList<String> getMyVehicles() {
        return myVehicles;
    }

    public boolean isOwnedVehicle(String vehicleId) {
        /**
         * Method checks if user already own a vehicle.
         */
        return this.myVehicles.contains(vehicleId);
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", ConnectedVehicleID='" + connectedVehicleID + '\'' +
                ", Vehicles='" + myVehicles.toString() + '\'' +
                '}';
    }
}
