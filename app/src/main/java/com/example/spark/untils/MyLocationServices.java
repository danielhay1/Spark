package com.example.spark.untils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.TimeUnit;

public class MyLocationServices {

    public enum TRACKLOCATION {
        ON,
        OFF,
    }

    private LocationManager locationManager;
    private Context appContext;
    private static MyLocationServices instance;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private TRACKLOCATION trackLocation = TRACKLOCATION.OFF;

    public interface CallBack_Location {
        void locationReady(Location location);
        void onError(String error);
    }

    public static MyLocationServices getInstance() {
        return instance;
    }

    private MyLocationServices(Context context) {
        this.appContext = context.getApplicationContext();
        this.locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        if (!isGpsEnabled()) {
            //GPS is not enabled !!
            Log.d("pttt", "GPS is not enabled");
        }
    }

    public static void Init(Context appContext) {
        if (instance == null) {
            Log.d("pttt", "Init: LocationServices");
            instance = new MyLocationServices(appContext);
        }
    }

    public void setLastBestLocation(CallBack_Location callBack_location) {
        /**
         * Method sample current location and use callBack_location when location is ready
         */
        if (checkLocationPermission()) {
            if (callBack_location != null) {
                callBack_location.onError("LOCATION PERMISSION IS NOT ALLOW");
            }
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //Location found
                locationSuccess(callBack_location,location);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                locationFailure(callBack_location,e);
            }
        });
    }

    private void locationSuccess(CallBack_Location callBack_location,Location location) {
        if (location != null) {
            if (callBack_location != null) {
                callBack_location.locationReady(location);
            }
        } else {
            if (callBack_location != null) {
                callBack_location.onError("Location is null");
            }
        }
    }

    private void locationFailure(CallBack_Location callBack_location,@NonNull Exception e) {
        if (callBack_location != null) {
            callBack_location.onError(e.getLocalizedMessage());
        }
    }

    public void onLocationUpdate(CallBack_Location callBack_location) {
        if (checkLocationPermission()) {
            if (callBack_location != null) {
                callBack_location.onError("LOCATION PERMISSION IS NOT ALLOW");
            }
            return;
        }
        if (isTrackingLocation()){
            callBack_location.onError("Device is already receiving location updates");
            return;
        }
        Log.d("pttt", "onLocationUpdate: trackLocation="+trackLocation);
        LocationRequest req = new LocationRequest();
        req.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        req.setFastestInterval(2000);
        req.setInterval(4000);
        fusedLocationProviderClient.requestLocationUpdates(req,new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                locationSuccess(callBack_location,location);
            }
        }, Looper.getMainLooper());

    }

    public boolean checkLocationPermission() {

        boolean res = (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
        Log.d("pttt", "permission="+res);
        return res;
    }

    public boolean isTrackingLocation() {
        if (this.trackLocation == TRACKLOCATION.ON)
            return true;
        else {
            return false;
        }
    }

    public void toggleTrackLocation(TRACKLOCATION trackLocation) {
        this.trackLocation = trackLocation;
        Log.d("pttt", "toggleTrackLocation = "+ trackLocation.toString());
    }

    public boolean isGpsEnabled() {
        return  this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void stopLocationUpdate(CallBack_Location callBack_location) {
        if(isTrackingLocation()) {
            if(fusedLocationProviderClient != null) {
                fusedLocationProviderClient.removeLocationUpdates(new LocationCallback() {
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        callBack_location.locationReady(locationResult.getLastLocation());
                    }
                });
            }
        }
        else {
            callBack_location.onError("Location update not requested");
        }
    }
}
