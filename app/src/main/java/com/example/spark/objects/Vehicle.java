package com.example.spark.objects;

public class Vehicle {
    private String key = "";
    private String vehicleLicenceNumber = "";
    private String vehicleNick = "";

    public Vehicle() {
    }

    public String getKey() {
        return key;
    }

    public Vehicle setKey(String key) {
        this.key = key;
        return this;
    }

    public String getVehicleLicenceNumber() {
        return vehicleLicenceNumber;
    }

    public Vehicle setVehicleLicenceNumber(String vehicleLicenceNumber) {
        this.vehicleLicenceNumber = vehicleLicenceNumber;
        return this;
    }

    public String getVehicleNick() {
        return vehicleNick;
    }

    public Vehicle setVehicleNick(String vehicleNick) {
        this.vehicleNick = vehicleNick;
        return this;
    }
}
