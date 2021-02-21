package com.example.spark.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.spark.R;
import com.example.spark.activiities.MainActivity;
import com.example.spark.objects.Parking;
import com.example.spark.objects.RouteBulider;
import com.example.spark.objects.LocationReceiver;
import com.example.spark.objects.User;
import com.example.spark.untils.ImgLoader;
import com.example.spark.untils.MyFireBaseServices;
import com.example.spark.untils.MyLocationServices;
import com.example.spark.untils.MyPreference;
import com.example.spark.untils.MySignal;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.gson.Gson;


public class Map_Fragment extends Fragment{

    private final int BIG_SCALE = 18;
    private final int NORMAL_SCALE = 17;
    private final int SMALL_SCALE = 16;
    private enum PARKING_STATE {
        DRIVING,
        PARKING,
        NAVIGATING
    }

    private final String CURRENT_LOCATION_ICON = "user_png_marker";
    private final String CAR_ICON = "car_png_marker";


    private User user;

    //map
    private GoogleMap googleMap;
    private boolean isMapSetUp = false;
    //route draw
    private RouteBulider routeBulider;
    private Polyline route;

    private LatLng myCurrentLocation;
    private Marker myParkingMarker;
    private Marker myLocationMarker;
    private PARKING_STATE parking_state = PARKING_STATE.DRIVING;
    private boolean autoFocusCurrentLocation;

    //Buttons
    private ImageView map_BTN_park;
    private ImageView map_BTN_LocationFocus;
    private ImageView map_BTN_cancel;
    private ImageView map_BTN_parkingFocus;
    private ImageView map_BTN_FollowMyCurrentLocation;
    //EditText
    private TextView map_TV_distance;

    //LocationTrack
    private LocationSource.OnLocationChangedListener locationChangedListener;
    private LocationReceiver locationReceiver;
    private boolean gpsEnabled; //Checks of gps enabled
    private String preferenceKey = "parking_marker";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        findViews(view);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_maps);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                //When map is loaded
                googleMap = map;
                initViews();
                getUser();
                gpsEnabled = isGpsEnabled();    //Ask to enable gps sensor if needed
                if(!MyLocationServices.getInstance().checkLocationPermission()) {
                    requestPermision();
                } else {
                    initMapFragment();
                }

            }
        });
        return view;
    }

    private void findViews(View view) {
        map_BTN_park = view.findViewById(R.id.map_BTN_park);
        map_BTN_LocationFocus = view.findViewById(R.id.map_BTN_LocationFocus);
        map_BTN_cancel = view.findViewById(R.id.map_BTN_cancel);
        map_BTN_parkingFocus = view.findViewById(R.id.map_BTN_parkingFocus);
        map_TV_distance = view.findViewById(R.id.map_TV_distance);
        map_BTN_FollowMyCurrentLocation = view.findViewById(R.id.map_BTN_FollowMyCurrentLocation);
    }

    private void initViews() {
        map_BTN_LocationFocus.setOnClickListener(onClickFocusMyLocation);
        map_BTN_park.setOnClickListener(onClickPark);
        map_BTN_cancel.setOnClickListener(onClickCancel);
        map_BTN_parkingFocus.setOnClickListener(onClickParkingFocus);
        map_BTN_FollowMyCurrentLocation.setOnClickListener(onAutoFocusMyLocation);
    }

    public void setupMap(GoogleMap map) {
        if(map != null) {
            Log.d("pttt", "Setting up map");
            enableMyLocation(map);
            if (myCurrentLocation != null) {
                setFocus(myCurrentLocation, NORMAL_SCALE);
            }
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setRotateGesturesEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            Log.d("pttt", "setMap: btn="+map.getUiSettings().isMyLocationButtonEnabled());
            map.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
                @Override
                public void onMyLocationClick(@NonNull Location location) {
                    setFocus(myCurrentLocation, NORMAL_SCALE);
                }
            });
            map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    setFocus(myCurrentLocation, NORMAL_SCALE);
                    return false;
                }
            });
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            marker.showInfoWindow();
                        }
                    }, 300);
                    return false;
                }
            });
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if(myParkingMarker!=null && myParkingMarker.isInfoWindowShown()){
                        myParkingMarker.hideInfoWindow();
                    }
                }
            });
        }
        isMapSetUp = true;
        Log.d("pttt", "setupMap: MAP STARTED!");
    }

    private void enableMyLocation(GoogleMap map) {
        if (MyLocationServices.getInstance().checkLocationPermission()) {
            if (map != null) {
                initCurrentLocation();
                map.setMyLocationEnabled(true);
            }
        }
    }

    private void setLocationSource(GoogleMap map) {
        map.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                locationChangedListener = onLocationChangedListener;
                registerLocationReceiver(locationReceiver);
                Log.d("pttt", "activate: Tracking location");
            }
            @Override
            public void deactivate() {
                locationChangedListener = null;
                Log.d("pttt", "deactivate: stop tracking location");
                MyLocationServices.getInstance().stopLocationUpdate(new MyLocationServices.CallBack_Location() {
                    @Override
                    public void locationReady(Location location) {
                        unRegisterLocationReceiver(locationReceiver);
                    }

                    @Override
                    public void onError(String error) {
                        Log.d("pttt", "onError: "+error);
                    }
                });
            }
        });
    }

    private void initMapFragment() {
        if(googleMap != null && !isMapSetUp && gpsEnabled) {
            setupMap(googleMap);
            setLocationSource(googleMap);
        }
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
                        saveParkingLocation(preferenceKey, myParkingMarker.getPosition());
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
        /**
         * Method receive map,latlng,marker title,marker icon -> method set new Parking marker,sets parking_state to PARKING and updateUI.
         * Method return the added marker.
         */
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

    private void drawRoute(LatLng origin, LatLng destination) {
        if(myLocationMarker == null) {
            myLocationMarker = addMarkerToMap(googleMap,origin,"Origin",CURRENT_LOCATION_ICON);
        }
        RouteBulider routeBulider = new RouteBulider(origin,destination,this.getActivity().getApplicationContext());
        route = googleMap.addPolyline(routeBulider.getPolylineRoute());
    }

    private  void removeRoute() {
        if(myLocationMarker != null) {
            myLocationMarker.remove();
            myLocationMarker = null;
        }
        route.remove();
    }

    private void cancelParking() {
        parking_state = PARKING_STATE.DRIVING;
        if(myParkingMarker != null) {
            localRemoveParkingLocation(preferenceKey);  //local parking remove
            myParkingMarker.remove();
            myParkingMarker = null;
            MyFireBaseServices.getInstance().deletePakringFromFireBase(user.getConnectedVehicleID());    //online parking remove
        }
        updateUI();
        Log.d("pttt", "parking_state = " + parking_state);

    }

    private void updateUI() {
        /**
         * Method update UI by parking_state value.
         */
        Log.d("pttt", "updateUI: parking_state= "+parking_state);
        if(parking_state == PARKING_STATE.DRIVING) {
            ImgLoader.getInstance().loadImg("parking_btn_icon",map_BTN_park);
            map_BTN_cancel.setVisibility(View.INVISIBLE);
            map_BTN_cancel.setClickable(false);
            map_BTN_parkingFocus.setVisibility(View.INVISIBLE);
            map_BTN_cancel.setClickable(false);
        }

        else if(parking_state == PARKING_STATE.PARKING || parking_state == PARKING_STATE.NAVIGATING) {
            map_BTN_cancel.setVisibility(View.VISIBLE);
            map_BTN_cancel.setClickable(true);
            map_BTN_parkingFocus.setVisibility(View.VISIBLE);
            map_BTN_parkingFocus.setClickable(true);
            ImgLoader.getInstance().loadImg("parking_btn_navigate_icon",map_BTN_park);
        }

        if(parking_state == PARKING_STATE.NAVIGATING) {
            map_BTN_park.setClickable(false);
            map_BTN_park.setVisibility(View.INVISIBLE);
            map_TV_distance.setVisibility(View.VISIBLE);
        } else {
            map_BTN_park.setClickable(true);
            map_BTN_park.setVisibility(View.VISIBLE);
            map_TV_distance.setVisibility(View.INVISIBLE);
        }
    }

    private void navigateToParking() {
        parking_state = PARKING_STATE.NAVIGATING;
        updateUI();
        LatLng destination = myParkingMarker.getPosition();
        LatLng origin = myCurrentLocation;
        origin = new LatLng(32.05853290879904, 34.82934680220547);
        //RouteBuilder2 routeBuilder2 = new RouteBuilder2(origin,destination,this.getActivity().getApplicationContext(),googleMap);
        //routeBuilder2.buildRoute();
        drawRoute(origin,destination);
    }

    private void initCurrentLocation() {
        MyLocationServices.getInstance().setLastBestLocation(new MyLocationServices.CallBack_Location() {
            @Override
            public void locationReady(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                if(latLng.latitude !=0||latLng.longitude!=0)
                {
                    Log.d("pttt", "locationReady: currentLocation = "+latLng.toString());
                    if(myCurrentLocation == null) {
                        myCurrentLocation = latLng;

                        if(locationChangedListener!=null) {
                            locationChangedListener.onLocationChanged(location);
                        }
                    }
                    setFocus(latLng,NORMAL_SCALE);
                }
            }
            @Override
            public void onError(String error) {
                Log.d("pttt", "onError: "+error);
            }
        });
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

    private boolean isGpsEnabled() {
        if (!MyLocationServices.getInstance().isGpsEnabled()) {
            MySignal.getInstance().alertDialog(this.getActivity(),"GPS IS DISABLED","Press \'Enable GPS\' to open gps settings","Enable GPS","Cancel",
                    new  DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            getActivity().startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
            return false;
        }
        return true;
    }

    private void requestPermision() {
        /**
         *         Asks the user do accept location permission.
         */
        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, MainActivity.MY_PERMISSIONS_REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("pttt", "Map_Fragment - onRequestPermissionsResult:\t requestCode="+requestCode);
        //this.getActivity().onRequestPermissionsResult(MainActivity.MY_PERMISSIONS_REQUEST_LOCATION, permissions,grantResults);
        switch (requestCode) {
            case MainActivity.MY_PERMISSIONS_REQUEST_LOCATION : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    initMapFragment();
                    Log.d("pttt", "onRequestPermissionsResult: APPROVE");
                    MySignal.getInstance().toast("Permission granted");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("pttt", "onRequestPermissionsResult: DENY");
                    MySignal.getInstance().toast("Permission denied");
                }
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int userRequest = MainActivity.UPDATE_USER;
        Log.d("pttt", "onActivityResult (MapFragment): request= "+requestCode +", result= "+resultCode);
        if (requestCode == userRequest) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra(MainActivity.USER_INTENT);
                Gson gson = new Gson();
                User temp = gson.fromJson(result,User.class);
                Log.d("pttt", "onActivityResult (MapFragment): user updated, user ="+user);
                preferenceKey = user.getConnectedVehicleID();
                if(!temp.getConnectedVehicleID().equals(user.getConnectedVehicleID())) {
                    Log.e("pttt", "onActivityResult: \tconnectedUser="+user.getConnectedVehicleID()+",updatedUser="+temp.getConnectedVehicleID());
                    this.user = temp;
                    cancelParking();
                    myParkingMarker = loadParkingLocation(preferenceKey);   //setMyParking
                } else {
                    this.user = temp;
                }
                preferenceKey = user.getConnectedVehicleID();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("pttt", "onActivityResult (MapFragment): user isn't updated.");
            }
        }
    }


    private void getUser() {
        if(user == null || user.getConnectedVehicleID().equalsIgnoreCase("")) {
            user = new User()
                    .setUid(MyFireBaseServices.getInstance().getUID())
                    .setName(MyFireBaseServices.getInstance().getUserName())
                    .setPhone(MyFireBaseServices.getInstance().getUserPhone())
                    .setConnectedVehicleID("");
            Gson gson = new Gson();
            if (getArguments()==null) {
                getUserFromFireBase(user.getUid());
            }
            else{
                String jsonUser = getArguments().getString(MainActivity.USER_INTENT);
                User loadedUser = gson.fromJson(jsonUser,User.class);
                if(loadedUser == null) {
                    Log.d("pttt", "getUser: \t USER IS NULL!");
                }
                else {
                    this.user = loadedUser;
                    preferenceKey = user.getConnectedVehicleID();
                    Log.d("pttt", "getUser: \tUID = "+user);
                    //setMyParking
                    if(myParkingMarker == null) {
                        myParkingMarker = loadParkingLocation(preferenceKey);
                    }
                }
            }
        }

    }

    private void getUserFromFireBase(String uid){
        MyFireBaseServices.getInstance().loadUserFromFireBase(uid, new MyFireBaseServices.CallBack_LoadUser() {
            @Override
            public void userDetailsUpdated(User result) {
                if(result != null) {
                    user = result;
                    Log.d("pttt", "userDetailsUpdated: \n"+user);
                    preferenceKey = user.getConnectedVehicleID();
                    //setMyParking
                    if(myParkingMarker == null) {
                        myParkingMarker = loadParkingLocation(preferenceKey);
                    }
                }
            }
            @Override
            public void loadFailed(Exception e) {
                Log.d("pttt", "loadFailed: Failed to read value "+e.getMessage());
            }
        });
    }

    //Load & Save Methdos.
    private void localSaveParkingLocation(String key, LatLng markerPos) {
        MyPreference.getInstance().putObject(key,markerPos);
    }

    private void onlineSaveParkingLocation(LatLng markerPos) {
        String vehicleId = user.getConnectedVehicleID();
        if(vehicleId != null) {
            if(markerPos.latitude!=0 || markerPos.longitude !=0){
                Log.d("pttt", "onlineSaveParkingLocation: MarkerPos "+markerPos);
                Parking parking = new Parking()
                        .setVehicleId(vehicleId)
                        .setUid(user.getUid())
                        .setParkingLocation(markerPos.latitude,markerPos.longitude)
                        .setCurrentDateAndtime();
                //firebase updateParking
                MyFireBaseServices.getInstance().saveParkingToFireBase(parking);
            }
        } else {
            Log.d("pttt", "User has no vehicle!");
        }
    }

    private void onlineLoadParkingLocation() {
        Log.d("pttt", "parkingLocationUpdated "+user);
        MyFireBaseServices.getInstance().loadParkingLocation(user.getConnectedVehicleID(), new MyFireBaseServices.CallBack_LoadParkingLocation() {
            @Override
            public void parkingLocationUpdated(LatLng result) {
                if(result != null) {
                    if(result.latitude!=0 || result.longitude!=0) {
                        myParkingMarker = park(googleMap,result,"My parking", CAR_ICON);
                        Log.d("pttt", "parkingLocationUpdated: parkingLocation="+result);
                    }
                } else {
                    Log.d("pttt", "parkingLocationUpdated: No parking found!");
                    parking_state = PARKING_STATE.DRIVING;
                    updateUI();
                }
            }
            @Override
            public void loadFailed(Exception e) {
                Log.d("pttt", "loadFailed: Failed to read value "+e.getMessage());
            }
        });
    }

    private void saveParkingLocation(String key, LatLng markerPos) {
        /***
         * Method save local and online parking marker position
         */
        localSaveParkingLocation(key,markerPos);    //Local save parking using shared_preference.
        onlineSaveParkingLocation(markerPos);   //Online save parking using fire_base.
    }

    private LatLng localLoadParkingLocation(String key) {
        return (LatLng) MyPreference.getInstance().getLatLng(key);  //Local load parking using shared_preference
    }

    private void localRemoveParkingLocation(String key) {
        MyPreference.getInstance().deleteKey(key);
    }

    private Marker loadParkingLocation(String key) {
        /***
         * Method load parking marker and update parking_state,myParkingMarker and UI.
         * Method load parking marker from shared_preference and if it fails, method load parking marker from fire_base.
         * if both loads fails method won't park, change parking_state to DRIVING and return null.
         */
        Marker marker = null;
        if(myParkingMarker == null) {
            LatLng markerPos = localLoadParkingLocation(key);
            if(markerPos != null) {
                Log.d("pttt", "loadParkingLocation: \tmarker_position= "+markerPos);
                marker = park(googleMap,markerPos,"My parking", CAR_ICON);
            } else if (markerPos == null && !user.getConnectedVehicleID().equalsIgnoreCase("")) {    //No parking latlng found in shared_preference trying to find parking latlng at fire_base
                onlineLoadParkingLocation();
                marker = myParkingMarker;
            }
        }
        return marker;
    }

    private void registerLocationReceiver(LocationReceiver locationReceiver) {
        locationReceiver = new LocationReceiver(new LocationReceiver.CallBack_LatLngUpdate() {
            @Override
            public void latLngUpdate(LatLng latLng) {
                myCurrentLocation = latLng;
                Location location = new Location("");
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                if(locationChangedListener!=null) {
                    locationChangedListener.onLocationChanged(location);
                }
                if(autoFocusCurrentLocation) {
                    setFocus(myCurrentLocation,BIG_SCALE);
                }
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationReceiver.CURRENT_LOCATION);
        this.getActivity().registerReceiver(locationReceiver,intentFilter);
    }

    private void unRegisterLocationReceiver(LocationReceiver locationReceiver) {
        if(locationReceiver!=null) {
            this.getActivity().unregisterReceiver(locationReceiver);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("pttt", "MAP FRAGMENT- onPause: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("pttt", "MAP FRAGMENT- onResume: ");
        if(MyLocationServices.getInstance().isGpsEnabled()) {
            gpsEnabled = true;
            if(MyLocationServices.getInstance().checkLocationPermission()) {
                initMapFragment();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("pttt", "MAP FRAGMENT- onStart: ");
        if(googleMap != null) {
            //setMyParking
            if(myParkingMarker == null) {
                Log.d("pttt", "Setting Parking");
                myParkingMarker = loadParkingLocation(preferenceKey);
            }
            if(!this.map_BTN_cancel.isClickable() && parking_state != PARKING_STATE.DRIVING) {
                updateUI();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("pttt", "MAP FRAGMENT- onStop: ");
        unRegisterLocationReceiver(this.locationReceiver);
    }

    // Listeners
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
            if (myCurrentLocation != null) {
                setFocus(myCurrentLocation, NORMAL_SCALE);
            }
        }
    };

    private View.OnClickListener onClickCancel = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(parking_state == PARKING_STATE.PARKING) {
                if(map_BTN_cancel.isClickable()) {
                    cancelParking();
                }
            } else if(parking_state == PARKING_STATE.NAVIGATING) {
                parking_state = PARKING_STATE.PARKING;
                removeRoute();
                updateUI();
                Log.d("pttt", "parking_state = " + parking_state);
            }
        }
    };

    private View.OnClickListener onClickParkingFocus = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (myParkingMarker != null) {
                setFocus(myParkingMarker.getPosition(),NORMAL_SCALE);
                myParkingMarker.showInfoWindow();
            }
        }
    };

    private View.OnClickListener onAutoFocusMyLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            autoFocusCurrentLocation = !autoFocusCurrentLocation;
            Log.d("pttt", "autoFocusCurrentLocation = "+autoFocusCurrentLocation);
            if(autoFocusCurrentLocation == false) {
                ImgLoader.getInstance().loadImg("icon_mylocation_focus_gray",map_BTN_FollowMyCurrentLocation);
            } else {
                ImgLoader.getInstance().loadImg("icon_mylocation_focus",map_BTN_FollowMyCurrentLocation);
            }
        }
    };
}