package com.example.spark.untils;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.spark.objects.LocationReceiver;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

public class GpsTracker_service extends Service {

    private LatLng currentLocation;
    private boolean isTracking;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
            Log.d("pttt", "GPS Traker: Location= "+currentLocation.toString());
            sendCurrentLocation(currentLocation);
        }
    };
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location location = locationResult.getLastLocation();
            if (location.getLatitude() != 0 || location.getLongitude() != 0) {
                if (locationListener != null) {
                    locationListener.onLocationChanged(location);
                }
            }
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("pttt", "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("pttt", "Working Thread: "+Thread.currentThread().getName());
                setCurrentLocation();
            }
        }).start();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //handler
    }

    private void setCurrentLocation() {
/*        if(currentLocation == null) {
            initCurrentLocation();
        } else {
            updateCurrentLocation();
        }*/
        updateCurrentLocation();
    }

    private void sendCurrentLocation(LatLng latLng) {
        Gson gson = new Gson();
        String currentLoc = gson.toJson(latLng);
        Intent intent = new Intent(LocationReceiver.CURRENT_LOCATION);
        intent.putExtra(LocationReceiver.LOCATION,currentLoc);
        sendBroadcast(intent);
    }

    private void initCurrentLocation() {
        MyLocationServices.getInstance().setLastBestLocation(new MyLocationServices.CallBack_Location() {
            @Override
            public void locationReady(Location location) {
                if(location.getLatitude() !=0||location.getLongitude()!=0)
                {
                    if(locationListener!=null) {
                        locationListener.onLocationChanged(location);
                    }
                }
            }
            @Override
            public void onError(String error) {
                Log.d("pttt", "onError: "+error);
            }
        });
    }

    private void updateCurrentLocation() {
        if (MyLocationServices.getInstance().isGpsEnabled()) {
            MyLocationServices.getInstance().startLocationUpdateds(new MyLocationServices.CallBack_Location() {
                @Override
                public void locationReady(Location location) {
                }
                @Override
                public void onError(String error) {
                    Log.d("pttt", "onError: " + error);
                }
            },locationCallback);
        }
    }



    public void stopUpdateCurrentLocation() {
        Log.d("pttt", "stopUpdateCurrentLocation");
        MyLocationServices.getInstance().stopLocationUpdate(new MyLocationServices.CallBack_Location() {
            @Override
            public void locationReady(Location location) {
                locationListener.onLocationChanged(location);
                Log.d("pttt", "stopUpdateCurrentLocation - \tlocationReady: "+location);
            }

            @Override
            public void onError(String error) {
                Log.d("pttt", "onError: "+error);
            }
        });
    }

    public void stopUpdateCurrentLocation2() {
        Log.d("pttt", "stopUpdateCurrentLocation2");
        MyLocationServices.getInstance().stopLocationUpdate2(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);

    }

    @Override
    public void onDestroy() {
        stopUpdateCurrentLocation2();
        super.onDestroy();
    }
}
