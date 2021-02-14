package com.example.spark.objects;

public class User {
    private String uid = "";
    private String name = "";
    private String phone = "";
    private String vehicleID = "";

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

    public String getVehicleID() {
        return vehicleID;
    }

    public User setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
        return this;
    }

}
