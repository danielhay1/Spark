package com.example.spark.fragments;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.spark.R;
import com.example.spark.activities.MainActivity;
import com.example.spark.objects.BluetoothBackgroundService;
import com.example.spark.objects.BluetoothCallBack;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Date;


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
    private final String PARKING_ICON = "car_png_marker";
    private final String PARKING_HISTORY_ICON = "history_clock_icon";

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
    private Marker myHistoryParkingMarker;
    private PARKING_STATE parking_state = PARKING_STATE.DRIVING;
    private boolean autoFocusCurrentLocation;

    //Buttons
    private ImageView map_BTN_park;
    private ImageView map_BTN_LocationFocus;
    private ImageView map_BTN_cancel;
    private ImageView map_BTN_cancelHistoryParking;
    private ImageView map_BTN_parkingFocus;
    private ImageView map_BTN_FollowMyCurrentLocation;
    private ImageView map_BTN_daynight;
    private Switch map_SWITCH_autopark;
    //EditText
    private TextView map_TV_distance;
    private TextView map_TV_estimateTime;
    //LocationTrack
    private LocationSource.OnLocationChangedListener locationChangedListener;
    private LocationReceiver locationReceiver;
    private boolean gpsEnabled; //Checks of gps enabled
    private String preferenceLatlagKey = "parking_marker";
    //bluetooth connection
    private SendSignal sendSignalCallBack;

    private BluetoothCallBack bluetoothCallBack = new BluetoothCallBack() {
        @Override
        public void onDisconnected() {
            if(parking_state == PARKING_STATE.DRIVING) {
                String msg = getParkingMarkerMsg(user.getName(),java.text.DateFormat.getDateTimeInstance().format(new Date()));
                onParkClick(msg, PARKING_ICON);
                map_SWITCH_autopark.setChecked(false);
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(mBluetoothAdapter.isEnabled()) {
                    map_SWITCH_autopark.setChecked(true);
                }
            }
        }
    };

    public interface SendSignal {
        public void parkingHistoryLoadSignal();
    }

    public void setCallback(SendSignal sendParking_CallBack) {
        this.sendSignalCallBack = sendParking_CallBack;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("pttt", "onCreateView: map_fragment");
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
        map_SWITCH_autopark = view.findViewById(R.id.map_SWITCH_autopark);
        map_BTN_cancelHistoryParking = view.findViewById(R.id.map_BTN_cancelHistoryParking);
        map_TV_estimateTime = view.findViewById(R.id.map_TV_estimateTime);
        map_BTN_daynight = view.findViewById(R.id.map_BTN_daynight);
    }

    private void initViews() {
        map_BTN_LocationFocus.setOnClickListener(onClickFocusMyLocation);
        map_BTN_park.setOnClickListener(onClickPark);
        map_BTN_cancel.setOnClickListener(onClickCancel);
        map_BTN_parkingFocus.setOnClickListener(onClickParkingFocus);
        map_BTN_FollowMyCurrentLocation.setOnClickListener(onAutoFocusMyLocation);
        map_BTN_cancelHistoryParking.setOnClickListener(removeHistoryParkingMarker);
        map_SWITCH_autopark.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveBtSwitchStatus(isChecked);
                if(map_SWITCH_autopark.isChecked() == true){
                    getActivity().startService(new Intent(getContext(), BluetoothBackgroundService.class));
                    BluetoothBackgroundService.setCallback(bluetoothCallBack);

                } else {
                    BluetoothBackgroundService.stopService();
                }
            }
        });
        map_BTN_daynight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDarkModeAvailable()) {
                    //change to day mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    //change to night mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                setTheme(googleMap);

            }
        });
        map_SWITCH_autopark.setChecked(loadBtSwitchStatus());
    }

    private void saveBtSwitchStatus(boolean status) {
        MyPreference.getInstance().putBoolean(MyPreference.KEYS.BT_AUTO_PARKING,status);
    }

    private boolean loadBtSwitchStatus() {
        return MyPreference.getInstance().getBoolean(MyPreference.KEYS.BT_AUTO_PARKING);
    }


    public void setupMap(GoogleMap map) {
        if(map != null) {
            Log.d("pttt", "Setting up map");
            enableMyLocation(map);

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

    private boolean isDarkModeAvailable() {
        int nightModeFlags =getContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void setTheme(GoogleMap map) {
        if(isDarkModeAvailable()) {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this.getContext(),R.raw.map_in_night));
            map_SWITCH_autopark.setBackgroundColor(Color.BLACK);
            map_TV_distance.setBackgroundColor(Color.BLACK);
            map_TV_estimateTime.setBackgroundColor(Color.BLACK);
            ImgLoader.getInstance().loadImg("moon_icon",map_BTN_daynight);
        } else {
            ImgLoader.getInstance().loadImg("sun_icon",map_BTN_daynight);
        }
    }

    private void enableMyLocation(GoogleMap map) {
        if (MyLocationServices.getInstance().checkLocationPermission()) {
            if (map != null) {
                initCurrentLocation();
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
        if(googleMap != null) {
            setTheme(googleMap);
            if(!isMapSetUp && gpsEnabled) {
                setupMap(googleMap);
                setLocationSource(googleMap);
                if (myCurrentLocation != null) {
                    setFocus(myCurrentLocation, NORMAL_SCALE);
                }
            }
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
                        myParkingMarker = park(googleMap,currentLocation,markerTitle,markerIcon);
                        saveParkingLocation(preferenceLatlagKey, myParkingMarker.getPosition());
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
        if(sendSignalCallBack!=null) {
            Log.d("pttt", "park: Send parkingHistory update signal");
            sendSignalCallBack.parkingHistoryLoadSignal();
        }
        return myParkMarker;
    }

    private void drawRoute(LatLng origin, LatLng destination, RouteBulider routeBulider) {
        if(myLocationMarker == null) {
            myLocationMarker = addMarkerToMap(googleMap,origin,"Origin",CURRENT_LOCATION_ICON);
        }
        route = googleMap.addPolyline(routeBulider.getPolylineRoute());
    }

    private void updateRouteDetails(RouteBulider routeBulider , LatLng origin, LatLng destination) {
        /**
         * Method update distance and estimate walking time between origin to destination.
         */
        if(origin!=null && destination!=null) {
            Log.d("pttt", "updateRouteLbls: origin="+origin+", dest="+destination);
            String distance = routeBulider.routeDistanceToString(origin,destination);
            String estimateTime = routeBulider.routeEstimateTimeToString(origin,destination);
            map_TV_distance.setText("Route distance: "+distance);
            map_TV_estimateTime.setText("Estimate Time: "+estimateTime);
            double dist = Double.parseDouble(distance.substring(0,distance.length()-3));
            if(dist == 0){
                parking_state = PARKING_STATE.PARKING;
                removeRoute();
                updateUI();
            }
        }
    }

    private void removeRoute() {
        if(myLocationMarker != null) {
            myLocationMarker.remove();
            myLocationMarker = null;
        }
        route.remove();
        route=null;
    }

    private void cancelParking() {
        parking_state = PARKING_STATE.DRIVING;
        if(myParkingMarker != null) {
            localRemoveParkingLocation(preferenceLatlagKey);  //local parking remove
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
            map_TV_estimateTime.setVisibility(View.VISIBLE);
        } else {
            map_BTN_park.setClickable(true);
            map_BTN_park.setVisibility(View.VISIBLE);
            map_TV_distance.setText("Route distance: ");
            map_TV_estimateTime.setText("Estimate Time: ");
            map_TV_distance.setVisibility(View.INVISIBLE);
            map_TV_estimateTime.setVisibility(View.INVISIBLE);
        }
        if(myHistoryParkingMarker!=null){
            //SET VISABLE, SET CANCEL HISTORYMAKER VISABLE
            map_BTN_cancelHistoryParking.setVisibility(View.VISIBLE);
            map_BTN_cancelHistoryParking.setClickable(true);
        } else {
            //SET INVISABLE, SET CANCEL HISTORYMAKER INVISABLE AND NOT CLICKABLE
            map_BTN_cancelHistoryParking.setVisibility(View.INVISIBLE);
            map_BTN_cancelHistoryParking.setClickable(false);
        }
    }

    private void navigateToParking() {
        if(parking_state != PARKING_STATE.NAVIGATING) {
            parking_state = PARKING_STATE.NAVIGATING;
            updateUI();
        }
        LatLng origin = myCurrentLocation;
        LatLng destination = myParkingMarker.getPosition();
        routeBulider = new RouteBulider(origin,destination,this.getActivity().getApplicationContext());
        drawRoute(origin,destination,routeBulider);
        updateRouteDetails(routeBulider,origin,destination);
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
                        if (MyLocationServices.getInstance().checkLocationPermission()) {
                            googleMap.setMyLocationEnabled(true);
                        }
                        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        googleMap.getUiSettings().setCompassEnabled(false);
                        googleMap.getUiSettings().setRotateGesturesEnabled(true);
                        googleMap.getUiSettings().setZoomControlsEnabled(true);
                        googleMap.getUiSettings().setTiltGesturesEnabled(false);
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
        /**
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
         * Asks the user do accept location permission.
         */
        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, MainActivity.MY_PERMISSIONS_REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("pttt", "Map_Fragment - onRequestPermissionsResult:\t requestCode="+requestCode);
        switch (requestCode) {
            case MainActivity.MY_PERMISSIONS_REQUEST_LOCATION : {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMapFragment();
                    Log.d("pttt", "onRequestPermissionsResult: APPROVE");
                    MySignal.getInstance().toast("Permission granted");

                } else {
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
                Log.d("pttt", "onActivityResult (MapFragment): user updated, user ="+temp);
                if(user!=null) {
                    preferenceLatlagKey = user.getConnectedVehicleID();
                    if(!temp.getConnectedVehicleID().equals(user.getConnectedVehicleID())&& preferenceLatlagKey !=null) {
                        Log.d("pttt", "onActivityResult: \tconnectedUser="+user.getConnectedVehicleID()+",updatedUser="+temp.getConnectedVehicleID());
                        this.user = temp;
                        cancelParking();
                        myParkingMarker = loadParkingLocation(preferenceLatlagKey);   //setMyParking
                    } else {
                        this.user = temp;
                    }
                } else {
                    this.user = temp;
                }
                preferenceLatlagKey = user.getConnectedVehicleID();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("pttt", "onActivityResult (MapFragment): user isn't updated.");
            }
        }
    }

    private void getUser() {
        /**
         * Method load get user from activity, if it fail -> try to load user from firebase,
         * if it fail -> return new user.
         */
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
                    preferenceLatlagKey = user.getConnectedVehicleID();
                    Log.d("pttt", "getUser: \tUID = "+user);
                    //setMyParking
                    if(myParkingMarker == null) {
                        myParkingMarker = loadParkingLocation(preferenceLatlagKey);
                    }
                }
            }
        }

    }

    private void getUserFromFireBase(String uid){
        /**
         * Method load user from firebase.
         */
        MyFireBaseServices.getInstance().loadUserFromFireBase(uid, new MyFireBaseServices.CallBack_LoadUser() {
            @Override
            public void userDetailsUpdated(User result) {
                if(result != null) {
                    user = result;
                    Log.d("pttt", "userDetailsUpdated: \n"+user);
                    preferenceLatlagKey = user.getConnectedVehicleID();
                    //setMyParking
                    if(myParkingMarker == null) {
                        myParkingMarker = loadParkingLocation(preferenceLatlagKey);
                    }
                }
            }
            @Override
            public void loadFailed(Exception e) {
                Log.d("pttt", "loadFailed: Failed to read value "+e.getMessage());
            }
        });
    }

    //Load & Save Methdos:
    private void localSaveParkingLocation(String key, LatLng markerPos) {
        String markerMsg = getParkingMarkerMsg(user.getName(),java.text.DateFormat.getDateTimeInstance().format(new Date()));
        MyPreference.getInstance().putObject(key,markerPos);
        MyPreference.getInstance().putString(MyPreference.KEYS.PREFERENCE_MARKER_MSG_PREFIX+key,markerMsg);
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
                MyFireBaseServices.getInstance().saveParkingToFireBase(parking);
            }
        } else {
            Log.d("pttt", "User has no vehicle!");
        }
    }

    private void onlineLoadParkingLocation() {
        Log.d("pttt", "parkingLocationUpdated "+user);
        MyFireBaseServices.getInstance().loadParkingLocation(user.getConnectedVehicleID(), new MyFireBaseServices.CallBack_LoadParking() {
            @Override
            public void parkingLocationUpdated(Parking parking) {
                if(parking != null) {
                    if(parking.getLatitude()!=0 || parking.getLongitude()!=0) {
                        if(parking.getUid() != user.getUid()) {
                            MyFireBaseServices.getInstance().loadUserFromFireBase(parking.getUid(), new MyFireBaseServices.CallBack_LoadUser() {
                                @Override
                                public void userDetailsUpdated(User result) {
                                    Log.d("pttt", "parkingLocationUpdated: parkingLocation="+parking);
                                    String msg = getParkingMarkerMsg(result.getName(),parking.getTime());
                                    myParkingMarker = park(googleMap,new LatLng(parking.getLatitude(),parking.getLongitude()),msg, PARKING_ICON);

                                }
                                @Override
                                public void loadFailed(Exception e) {
                                    Log.e("pttt", "loadFailed: "+e.getStackTrace());
                                }
                            });
                        } else {
                            Log.d("pttt", "parkingLocationUpdated: parkingLocation="+parking);
                            String msg = getParkingMarkerMsg(user.getName(),parking.getTime());
                            myParkingMarker = park(googleMap,new LatLng(parking.getLatitude(),parking.getLongitude()),msg, PARKING_ICON);
                        }
                    }
                } else {
                    Log.d("pttt", "parkingLocationUpdated: No parking found!");
                    parking_state = PARKING_STATE.DRIVING;
                    updateUI();
                }
            }

            @Override
            public void loadParkingHistory(ArrayList<Parking> parkings) { }

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
    private String localLoadParkingMarkerMsg(String key) {
        return MyPreference.getInstance().getString(MyPreference.KEYS.PREFERENCE_MARKER_MSG_PREFIX+key);
    }

    private void localRemoveParkingLocation(String key) {
        MyPreference.getInstance().deleteKey(key);
        MyPreference.getInstance().deleteKey(MyPreference.KEYS.PREFERENCE_MARKER_MSG_PREFIX+key);
    }

    private Marker loadParkingLocation(String key) {
        /***
         * Method load parking marker and update parking_state,myParkingMarker and UI.
         * Method load parking marker from shared_preference and if it fails, method load parking marker from fire_base.
         * if both loads fails method won't park, change parking_state to DRIVING and return null.
         */
        Marker marker = null;
        String markerMsg = "My parking";
        if(myParkingMarker == null) {
            LatLng markerPos = localLoadParkingLocation(key);
            if(!user.getConnectedVehicleID().equalsIgnoreCase("")) {
                markerMsg = localLoadParkingMarkerMsg(user.getConnectedVehicleID());
            }
            if(markerPos != null) {
                Log.d("pttt", "loadParkingLocation: \tmarker_position= "+markerPos);
                marker = park(googleMap,markerPos,markerMsg, PARKING_ICON);
            } else if (markerPos == null) {    //No parking latlng found in shared_preference trying to find parking latlng at fire_base
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
                if(parking_state == PARKING_STATE.NAVIGATING) {
                    if(myParkingMarker!=null) {
                        LatLng destination = myParkingMarker.getPosition();
                        updateRouteDetails(routeBulider,myCurrentLocation,destination);
                    }
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

    //Callback method
    public void addHistoryMarkerToMap(Parking parking) {
        /**
         * Callback method that call by click on element from list view in ParkingHistoryFragment, method set marker of the history parking on map.
         */
        LatLng latLng = new LatLng(parking.getLatitude(),parking.getLongitude());
        String name = user.getName();
        Log.d("pttt", "addHistoryMarkerToMap: Marker Latlng= "+latLng);
        //TO CONTINUE
        if(googleMap!=null) {
            if(this.user.getUid() != parking.getUid()) {
                MyFireBaseServices.getInstance().loadUserFromFireBase(parking.getUid(), new MyFireBaseServices.CallBack_LoadUser() {
                    @Override
                    public void userDetailsUpdated(User result) {
                        String msg = getParkingMarkerMsg(result.getName(),parking.getTime());
                        if(myHistoryParkingMarker == null){
                            myHistoryParkingMarker = addMarkerToMap(googleMap,latLng,msg,PARKING_HISTORY_ICON);

                        } else {
                            myHistoryParkingMarker.setPosition(latLng);
                            myHistoryParkingMarker.setTitle(msg);
                        }
                        setFocus(latLng,NORMAL_SCALE);
                        myHistoryParkingMarker.showInfoWindow();
                        updateUI();
                    }
                    @Override
                    public void loadFailed(Exception e) {
                        Log.d("pttt", "loadFailed: "+e.getStackTrace());
                    }
                });
            } else {
                String msg = getParkingMarkerMsg(user.getName(),parking.getTime());
                if(myHistoryParkingMarker == null){
                    myHistoryParkingMarker = addMarkerToMap(googleMap,latLng,msg,PARKING_HISTORY_ICON);

                } else {
                    myHistoryParkingMarker.setPosition(latLng);
                    myHistoryParkingMarker.setTitle(msg);
                }
                setFocus(latLng,NORMAL_SCALE);
                myHistoryParkingMarker.showInfoWindow();
                updateUI();
            }
        }
    }

    private String getParkingMarkerMsg(String name,String date){
        if(name == null || name.equalsIgnoreCase("")) {
            return date;
        } else {
            return (name+": "+date);
        }
    }

    private void cancelHistoryParking() {
        myHistoryParkingMarker.remove();
        myHistoryParkingMarker=null;
        updateUI();
    }

    public boolean onBackPress() {
        /**
         * If parking_state = PARKING_STATE.DRIVING method return false
         * else method return true.
         */
        if(parking_state == PARKING_STATE.DRIVING) {
            return true;
        } else if(parking_state == PARKING_STATE.PARKING) {
            cancelParking();
        } else {
            parking_state = PARKING_STATE.PARKING;
            removeRoute();
            updateUI();
        }
        return false;
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
                myParkingMarker = loadParkingLocation(preferenceLatlagKey);
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
                String msg = getParkingMarkerMsg(user.getName(),java.text.DateFormat.getDateTimeInstance().format(new Date()));
                onParkClick(msg, PARKING_ICON);
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

    private View.OnClickListener removeHistoryParkingMarker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cancelHistoryParking();
        }
    };

}