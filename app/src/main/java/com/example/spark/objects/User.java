package com.example.spark.objects;

public class User {
    private String uid = "";
    private String name = "";
    private String phone = "";
    private Vehicle vehicle = null;

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

    public Vehicle getVehicle() {
        return vehicle;
    }

    public User setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        return this;
    }

}
