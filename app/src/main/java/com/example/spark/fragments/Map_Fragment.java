package com.example.spark.fragments;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.spark.R;
import com.example.spark.untils.ImgLoader;
import com.example.spark.untils.MyLocationServices;
import com.example.spark.untils.MyPreference;
import com.example.spark.untils.MySignal;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class Map_Fragment extends Fragment {

    private enum PARKING_STATE {
        DRIVING,
        PARKING,
    }

    private final String CURRENT_LOCATION_ICON = "user_png_marker";
    private final String CAR_ICON = "car_png_marker";
    private final String MY_PREFERENCE_PARKING = "parking_marker";

    private GoogleMap googleMap;
    private Marker myLocationMarker;
    private Marker myParkingMarker;
    private PARKING_STATE parking_state = PARKING_STATE.DRIVING;

    //Buttons
    private ImageView map_BTN_park;
    private ImageView map_BTN_LocationFocus;
    private ImageView map_BTN_cancelparking;

    //Permission
    private boolean locationPermission = false;
    private final static int LOCATION_REQUEST_CODE = 23;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        findViews(view);
        requestPermision();   //Ask for location permission
        if (!MyLocationServices.getInstance().isGpsEnabled()) {
            MySignal.getInstance().toast("Gps conncetion is off.");
        }

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_maps);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                //When map is loaded
                googleMap = map;
                initCurrentLocation("Me", CURRENT_LOCATION_ICON);

                myParkingMarker = loadParkingLocation(MY_PREFERENCE_PARKING);
                if (myLocationMarker != null) {
                    setFocus(myLocationMarker.getPosition(), 15);
                }
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setFocus(marker.getPosition(), 15);
                            }
                        }, 300);
                        return false;
                    }
                });
                iniViews();
            }
        });
        return view;
    }

    private void getPermissionLocation() {
    }

    private void findViews(View view) {
        map_BTN_park = view.findViewById(R.id.map_BTN_park);
        map_BTN_LocationFocus = view.findViewById(R.id.map_BTN_LocationFocus);
        map_BTN_cancelparking = view.findViewById(R.id.map_BTN_cancelparking);

    }

    private void iniViews() {
        map_BTN_LocationFocus.setOnClickListener(onClickFocusMyLocation);
        map_BTN_park.setOnClickListener(onClickPark);
        map_BTN_cancelparking.setOnClickListener(onClickCancelparking);
    }

    private void onParkClick(String markerTitle, String markerIcon) {
        MyLocationServices.getInstance().setLastBestLocation(new MyLocationServices.CallBack_Location() {
            @Override
            public void locationReady(Location location) {
                LatLng currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
                if(currentLocation.latitude !=0||currentLocation.longitude!=0)
                {
                    Log.d("pttt", "locationReady: currentParkingLocation = "+currentLocation.toString());
                    if(myParkingMarker == null) {
                        myParkingMarker = park(googleMap, currentLocation,markerTitle,markerIcon);
                        saveParkingLocation(MY_PREFERENCE_PARKING, myParkingMarker.getPosition());
                    }
                }
            }
            @Override
            public void onError(String error) {
                Log.d("pttt", "onError: "+error);
            }
        });
    }

    private Marker park(GoogleMap googleMap,LatLng location,String markerTitle,String markerIcon) {
        Marker myParkMarker;
        parking_state = PARKING_STATE.PARKING;
        if(this.myParkingMarker == null) {
            myParkMarker = addMarkerToMap(googleMap, location,markerTitle,markerIcon);
        }
        else {
            this.myParkingMarker.setPosition(location);
            myParkMarker = this.myParkingMarker;
        }
        updateUI();
        Log.d("pttt", "parking_state = PARKING");
        return myParkMarker;
    }

    private void cancelParking() {
        if(myParkingMarker != null) {
            parking_state = PARKING_STATE.DRIVING;
            myParkingMarker.remove();
            myParkingMarker = null;
            localRemoveParkingLocation(MY_PREFERENCE_PARKING);
            updateUI();
            Log.d("pttt", "parking_state = DRIVING");
        }
    }

    private void updateUI() {
        if(parking_state == PARKING_STATE.DRIVING) {
            ImgLoader.getInstance().loadImg("parking_btn_icon",map_BTN_park);
            map_BTN_cancelparking.setVisibility(View.INVISIBLE);
            map_BTN_cancelparking.setClickable(false);
        }
        else {
            map_BTN_cancelparking.setVisibility(View.VISIBLE);
            map_BTN_cancelparking.setClickable(true);
            ImgLoader.getInstance().loadImg("parking_btn_navigate_icon",map_BTN_park);
        }
    }

    private void navigateToParking() {
        LatLng destination = myParkingMarker.getPosition();
        LatLng currentLocation = myLocationMarker.getPosition();
        //If google maps app isnt installed, redirect to play store
        try {
            Uri uri = Uri.parse("https://www.google.co.in/maps/dir/"+currentLocation+"/"+destination);
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            //when google maps isn't installed
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    private void initCurrentLocation(String markerTitle,String markerIcon) {
        MyLocationServices.getInstance().setLastBestLocation(new MyLocationServices.CallBack_Location() {
            @Override
            public void locationReady(Location location) {
                LatLng currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
                if(currentLocation.latitude !=0||currentLocation.longitude!=0)
                {
                    Log.d("pttt", "locationReady: currentLocation = "+currentLocation.toString());
                    if(myLocationMarker == null) {
                        myLocationMarker = addMarkerToMap(googleMap, currentLocation,markerTitle,markerIcon);
                    }
                    setFocus(currentLocation,15);
                    updateCurrentLocation(myLocationMarker);
                }
            }
            @Override
            public void onError(String error) {
                Log.d("pttt", "onError: "+error);
            }
        });
    }

    private void updateCurrentLocation(Marker marker) {
        Log.d("pttt", "updateCurrentLocation: ------");
        if(MyLocationServices.getInstance().isGpsEnabled()) {
            MyLocationServices.getInstance().onLocationUpdate(new MyLocationServices.CallBack_Location() {
                @Override
                public void locationReady(Location location) {
                    Log.d("pttt", "locationReady: ====");
                    LatLng currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
                    if(currentLocation.latitude !=0||currentLocation.longitude!=0)
                    {
                        if(marker != null) {
                            marker.setPosition(currentLocation);
                            Log.d("pttt", "setMarkerPosition: "+currentLocation.toString());
                        }
                    }
                    MyLocationServices.getInstance().toggleTrackLocation(MyLocationServices.TRACKLOCATION.ON);
                }
                @Override
                public void onError(String error) {
                    Log.d("pttt", "onError: "+error);
                }
            });
        }
    }

    public void setFocus(LatLng focusLatlng, float scale) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(focusLatlng);
        if(focusLatlng == null) {
            Log.d("pttt", "setFocus: NULL");
        }
        else {
            Log.d("pttt", "setFocus: \tLatlng= " + focusLatlng.toString() + "Zoom scale="+scale);
            CameraUpdate zoom= CameraUpdateFactory.zoomTo(scale);
            googleMap.moveCamera(center);
            googleMap.animateCamera(zoom);
        }
    }

    public Marker addMarkerToMap(GoogleMap map, LatLng currentLocation, String titleName, String iconName) {
        /*
         * add marker to map, receive map, latlang and icon to set for marker and set it on map.
         * if marker received is null use default marker icon.
         * */
        Log.d("pttt", "addMarkerToMap: (title="+titleName+"), (LatLng="+currentLocation.toString()+")");
        MarkerOptions marker = new MarkerOptions()
                .position(new LatLng(currentLocation.latitude, currentLocation.longitude))
                .title(titleName);
        if(iconName != null) {
            int icon = ImgLoader.getInstance().getImgIdentifier(iconName);  //load icon
            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(icon);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 150, 150, false);
            marker.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        }
        return map.addMarker(marker);
    }

/*    private void getPermissionLocation() {
        *//**
         *         In case of no permission asks the user do accept permission.
         *//*
        ActivityCompat.requestPermissions(this.getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
    }*/

    private void requestPermision() {
        if(ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }
        else{
            locationPermission=true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //if permission granted.
                    locationPermission=true;
                    initCurrentLocation("Me", CURRENT_LOCATION_ICON);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    MySignal.getInstance().toast(" Location Permission Denied!");
                }
                return;
            }
        }
    }

    //Load & Save Methdos.
    private void localSaveParkingLocation(String key, LatLng markerPos) {
        MyPreference.getInstance().putObject(key,markerPos);
    }

    private void onlineSaveParkingLocation(String key, LatLng markerPos) {
        //firebase save LatLang
    }

    private void saveParkingLocation(String key, LatLng markerPos) {
        /***
         * Method save local and online parking marker position
         */
        localSaveParkingLocation(key,markerPos);    //Local save parking using shared_preference.
        onlineSaveParkingLocation(key,markerPos);   //Online save parking using fire_base.
    }

    private LatLng localLoadParkingLocation(String key) {
        return (LatLng) MyPreference.getInstance().getLatLng(key);  //Local load parking using shared_preference
    }
    
    private LatLng onlineLoadParkingLocation(String key) {
        LatLng markerPos = null;
        return markerPos;
    }

    private void localRemoveParkingLocation(String key) {
        MyPreference.getInstance().deleteKey(key);
    }

    private Marker loadParkingLocation(String key) {
        /***
         * Method load parking marker and update parking_state,myParkingMarker and UI.
         * Method load parking marker from shared_preference and if it fails, method load parking marker from fire_base.
         * if both loads fails method return null.
         */
        Marker marker = null;
        LatLng markerPos = localLoadParkingLocation(key);
        if (markerPos == null) {    //No parking latlng found in shared_preference trying to find parking latlng at fire_base
            markerPos = onlineLoadParkingLocation(key);
        }
        if (markerPos != null) {  // In case maker position isn't null (found on shared_preference or fire_base).
            marker = park(googleMap,markerPos,"My parking", CAR_ICON);
        }
        else {
            Log.d("pttt", "parking_state = DRIVING");
        }
        return marker;
    }
    

    @Override
    public void onPause() {
        super.onPause();
        Log.d("pttt", "onPause: ");
        MyLocationServices.getInstance().stopLocationUpdate(new MyLocationServices.CallBack_Location() {
            @Override
            public void locationReady(Location location) {
                MyLocationServices.getInstance().toggleTrackLocation(MyLocationServices.TRACKLOCATION.OFF);
            }
            @Override
            public void onError(String error) {
                Log.d("pttt", "onError: "+error);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("pttt", "onResume: ");
        if(googleMap != null)
        {
/*            if(!MyLocationServices.getInstance().isTrackingLocation()) {
                updateCurrentLocation(this.myLocationMarker);
            }*/
            myParkingMarker = loadParkingLocation(MY_PREFERENCE_PARKING);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("pttt", "onStop: ");

    }

    private View.OnClickListener onClickPark = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (myParkingMarker != null) {
                //Already parked - set Gps navigation to park
                MySignal.getInstance().toast("Setting google maps walk navigation to parking");
                navigateToParking();
            } else {
                onParkClick("My parking", CAR_ICON);
            }
        }
    };

    private View.OnClickListener onClickFocusMyLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (myLocationMarker != null) {
                setFocus(myLocationMarker.getPosition(), 15);
                myLocationMarker.showInfoWindow();
            }
        }
    };

    private View.OnClickListener onClickCancelparking = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(map_BTN_cancelparking.isClickable()) {
                cancelParking();
            }
        }
    };
}
