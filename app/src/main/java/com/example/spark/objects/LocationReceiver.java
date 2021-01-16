package com.example.spark.objects;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

public class LocationReceiver extends BroadcastReceiver {
    public static final String CURRENT_LOCATION = "CURRENT_LOCATION";
    public static final String LOCATION= "Location";
    private CallBack_LatLngUpdate callBack_latLngUpdate;

    public interface CallBack_LatLngUpdate {
        void LatLngUpdate(LatLng latLng);
    }
    public LocationReceiver(CallBack_LatLngUpdate callBack_latLngUpdate) {
        this.callBack_latLngUpdate = callBack_latLngUpdate;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String latLng = intent.getStringExtra(LOCATION);
        Gson gson = new Gson();
        LatLng currentLocation = gson.fromJson(latLng,LatLng.class);
        if(callBack_latLngUpdate!=null) {
            callBack_latLngUpdate.LatLngUpdate(currentLocation);
        }
    }
}
