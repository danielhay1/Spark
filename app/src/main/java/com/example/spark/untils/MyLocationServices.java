
package com.example.spark.untils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
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
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.TimeUnit;

public class MyLocationServices {

    private LocationManager locationManager;
    private Context appContext;
    private static MyLocationServices instance;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest req;

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
        if (!checkLocationPermission()) {
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
                locationSuccess(callBack_location, location);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                locationFailure(callBack_location, e);
            }
        });
    }

    private void locationSuccess(CallBack_Location callBack_location, Location location) {
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

    private void locationFailure(CallBack_Location callBack_location, @NonNull Exception e) {
        if (callBack_location != null) {
            callBack_location.onError(e.getLocalizedMessage());
        }
    }

    public void startLocationUpdateds(CallBack_Location callBack_location,LocationCallback locationCallback) {
        if (!checkLocationPermission()) {
            if (callBack_location != null) {
                callBack_location.onError("LOCATION PERMISSION IS NOT ALLOW");
            }
            return;
        }
        if(locationCallback != null) {
            if (req == null) {
                req = new LocationRequest();
                req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                req.setFastestInterval(2000);
                req.setInterval(4000);
                fusedLocationProviderClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper());
            } else {
                callBack_location.onError("Device is already receiving location updates");
            }
        }
    }

    public boolean checkLocationPermission() {
        boolean res = (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
        Log.d("pttt", "permission=" + res);
        return res;
    }

    public boolean isGpsEnabled() {
        return this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void stopLocationUpdate(CallBack_Location callBack_location) {
        if (fusedLocationProviderClient != null && req != null) {
            fusedLocationProviderClient.removeLocationUpdates(new LocationCallback() {
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    callBack_location.locationReady(locationResult.getLastLocation());
                    req = null;
                    if (req == null)
                        Log.d("pttt", "stopLocationUpdate: req is null");
                }
            });
        } else {
            callBack_location.onError("Location update not requested");
        }
    }

    public void stopLocationUpdate2(LocationCallback locationCallback) {
        if (fusedLocationProviderClient != null && req!=null && locationCallback != null) {
            try {
                final Task<Void> voidTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                if (voidTask.isSuccessful()) {
                    req = null;
                    Log.d("pttt", "StopLocation updates successful! ");
                } else {
                    Log.d("pttt", "StopLocation updates unsuccessful! " + voidTask.toString());
                }
            } catch (SecurityException exp) {
                Log.d("pttt", " Security exception while removeLocationUpdates");
            }
        }
    }
}
