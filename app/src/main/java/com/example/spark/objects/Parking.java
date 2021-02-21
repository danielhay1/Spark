package com.example.spark.objects;

import android.util.Log;

import com.example.spark.untils.MyPreference;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;

public class Parking {
    private final String MY_PREFERENCE_PARKING = "parking_marker";

    private String vehicleId;
    private String uid;
    private double latitude = 0;
    private double longitude = 0;
    private String time;

    public Parking() { }

    public String getVehicleId() {
        return vehicleId;
    }

    public Parking setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
        return this;
    }

    public String getUid() {
        return uid;
    }

    public Parking setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTime() {
        return time;
    }

    public Parking setCurrentDateAndtime() {
        this.time = java.text.DateFormat.getDateTimeInstance().format(new Date());;
        return this;
    }

    public boolean isParking() {
        if(latitude==0 && longitude==0) {
            return false;
        } else {
            return true;
        }
    }

    public Parking setParkingLocation(double latitude, double longitude) {
        if(latitude == 0 && longitude ==0) {
            LatLng loadedParking = (LatLng) MyPreference.getInstance().getLatLng(MY_PREFERENCE_PARKING);
            if(loadedParking == null) {
                this.latitude = 0;
                this.longitude = 0;
            }
            else {
                this.latitude = loadedParking.latitude;
                this.longitude = loadedParking.longitude;
            }
        }
        else {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        return this;
    }

    @Override
    public String toString() {
        return "Parking{" +
                "vehicleId='" + vehicleId + '\'' +
                ", uid='" + uid + '\'' +
                ", latitude='" + getLatitude() + '\'' +
                ", longitude='" + getLongitude();
    }
}
