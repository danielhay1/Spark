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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MyLocationServices2 {
    private LocationManager locationManager;
    private Context appContext;
    private static MyLocationServices2 instance;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean trackLocation = false;

    public interface TRACKLOCATION {
        final boolean ON = true;
        final boolean OFF = false;
    }
    public interface CallBack_Location {
        void locationReady(Location location);
        void onError(String error);
    }

    public static MyLocationServices2 getInstance() {
        return instance;
    }

    private MyLocationServices2(Context context) {
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
            instance = new MyLocationServices2(appContext);
        }
    }

    public void setLastBestLocation(CallBack_Location callBack_location) {
        if (checkLocatoniPermission(appContext)) {
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
                trackLocation = true;
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
        if (checkLocatoniPermission(appContext)) {
            if (callBack_location != null) {
                callBack_location.onError("LOCATION PERMISSION IS NOT ALLOW");
            }
            return;
        }
        if (trackLocation){
            callBack_location.onError("Device is already receiving location updates");
            return;
        }
        LocationRequest req = new LocationRequest();
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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

    public boolean checkLocatoniPermission(Context context) {
        return (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED);
    }

    public boolean IsTrackLocation() {
        return this.trackLocation;
    }

    public boolean toggleTrackLocation(boolean isTrackLocationRequested) {
        return this.trackLocation = isTrackLocationRequested;
    }

    public boolean isGpsEnabled() {
        return  this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void stopLocationUpdate(CallBack_Location callBack_location) {
        if(trackLocation) {
            fusedLocationProviderClient.removeLocationUpdates(new LocationCallback() {
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    trackLocation = false;
                    Log.d("pttt", "IsTrackLocationRequested = "+ trackLocation);
                }
            });
        }
        else {
            callBack_location.onError("Location update not requested");
        }
    }
}
