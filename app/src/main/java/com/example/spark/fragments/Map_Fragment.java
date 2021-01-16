package com.example.spark.fragments;

import android.Manifest;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.spark.R;
import com.example.spark.activiities.RouteBulider;
import com.example.spark.objects.LocationReceiver;
import com.example.spark.untils.ImgLoader;
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


public class Map_Fragment extends Fragment{

    private final int NORMAL_SCALE = 15;
    private enum PARKING_STATE {
        DRIVING,
        PARKING,
        NAVIGATING
    }

    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private final String CURRENT_LOCATION_ICON = "user_png_marker";
    private final String CAR_ICON = "car_png_marker";
    private final String MY_PREFERENCE_PARKING = "parking_marker";

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

    //Buttons
    private ImageView map_BTN_park;
    private ImageView map_BTN_LocationFocus;
    private ImageView map_BTN_cancelparking;
    private ImageView map_BTN_parkingFocus;
    //EditText
    private TextView map_TV_distance;

    //LocationTrack
    private LocationSource.OnLocationChangedListener locationChangedListener;
    private LocationReceiver locationReceiver;


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
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_maps);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                //When map is loaded
                googleMap = map;
                if(!isMapSetUp) {
                    setupMap(googleMap);
                }
                iniViews();
            }
        });
        return view;
    }

    private void findViews(View view) {
        map_BTN_park = view.findViewById(R.id.map_BTN_park);

        map_BTN_LocationFocus = view.findViewById(R.id.map_BTN_LocationFocus);
        map_BTN_cancelparking = view.findViewById(R.id.map_BTN_cancelparking);
        map_BTN_parkingFocus = view.findViewById(R.id.map_BTN_parkingFocus);
        map_TV_distance = view.findViewById(R.id.map_TV_distance);
    }

    private void iniViews() {
        map_BTN_LocationFocus.setOnClickListener(onClickFocusMyLocation);
        map_BTN_park.setOnClickListener(onClickPark);
        map_BTN_cancelparking.setOnClickListener(onClickCancelparking);
        map_BTN_parkingFocus.setOnClickListener(onClickParkingFocus);
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
                            setFocus(marker.getPosition(), NORMAL_SCALE);
                        }
                    }, 300);
                    return false;
                }
            });
            //setMyParking
            myParkingMarker = loadParkingLocation(MY_PREFERENCE_PARKING);
        }
        isMapSetUp = true;
    }

    private void enableMyLocation(GoogleMap map) {
        if (MyLocationServices.getInstance().checkLocationPermission()) {
            if (map != null) {
                initCurrentLocation();
                map.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermision();
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
                        registerLocationReceiver(locationReceiver);
                    }

                    @Override
                    public void onError(String error) {
                        Log.d("pttt", "onError: "+error);
                    }
                });
            }
        });
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
            myParkingMarker.remove();
            myParkingMarker = null;
            localRemoveParkingLocation(MY_PREFERENCE_PARKING);
        }
        updateUI();
        Log.d("pttt", "parking_state = " + parking_state);

    }

    private void updateUI() {
        if(parking_state == PARKING_STATE.DRIVING) {
            ImgLoader.getInstance().loadImg("parking_btn_icon",map_BTN_park);
            //UI visibility
            map_BTN_cancelparking.setVisibility(View.INVISIBLE);
            map_BTN_cancelparking.setClickable(false);
            map_BTN_parkingFocus.setVisibility(View.INVISIBLE);
            map_BTN_cancelparking.setClickable(false);
            //remove parking marker
        }

        else if(parking_state == PARKING_STATE.PARKING || parking_state == PARKING_STATE.NAVIGATING) {
            // UI visibility
            map_BTN_cancelparking.setVisibility(View.VISIBLE);
            map_BTN_cancelparking.setClickable(true);
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
        ActivityCompat.requestPermissions(this.getActivity(),new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Enable the my location layer if the permission has been granted.
            MySignal.getInstance().toast("Location permission denied!");
            enableMyLocation(googleMap);
        }
        else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            MySignal.getInstance().toast("Location permission approved.");
            requestPermision();
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

    private void registerLocationReceiver(LocationReceiver locationReceiver) {
        locationReceiver = new LocationReceiver(new LocationReceiver.CallBack_LatLngUpdate() {
            @Override
            public void LatLngUpdate(LatLng latLng) {
                myCurrentLocation = latLng;
                Location location = new Location("");
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                locationChangedListener.onLocationChanged(location);
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
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("pttt", "MAP FRAGMENT- onStart: ");
        if(MyLocationServices.getInstance().checkLocationPermission()) {
            boolean gpsEnabled = isGpsEnabled();    //Ask to enable gps sensor if needed
            if(googleMap != null && !isMapSetUp) {
                setupMap(googleMap);
                setLocationSource(googleMap);
            }
        } else {
            requestPermision();   //Ask for location permission
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

    private View.OnClickListener onClickCancelparking = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(parking_state == PARKING_STATE.PARKING) {
                if(map_BTN_cancelparking.isClickable()) {
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
                setFocus(myParkingMarker.getPosition(), 15);
            }
        }
    };
}
