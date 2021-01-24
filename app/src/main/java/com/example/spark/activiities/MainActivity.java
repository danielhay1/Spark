package com.example.spark.activiities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.example.spark.R;
import com.example.spark.fragments.MyProfileFragment;
import com.example.spark.objects.MyFireBaseServices;
import com.example.spark.objects.PagerAdapter;
import com.example.spark.objects.User;
import com.example.spark.objects.Vehicle;
import com.example.spark.untils.MyLocationServices;
import com.example.spark.untils.GpsTracker_service;
import com.example.spark.untils.MySignal;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.gson.Gson;


public class MainActivity extends AppCompatActivity{

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    public final static String USER_INTENT = "user";
    private TabLayout main_NAVBAR;
    private TabItem main_NAVBAR_main,main_NAVBAR_history,main_NAVBAR_profile;
    private ViewPager main_VIEWPAGER;
    public PagerAdapter pagerAdapter;
    private User user;
    private boolean isLocationTrakerOn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(fireBaseLogin()) {   //FireBase login
            findViews();
            createUser();
            initNavBar();
        }


/*        findViews();            //without firebase connection
        initNavBar();*/
    }

    private void findViews() {
        main_VIEWPAGER = findViewById(R.id.main_VIEWPAGER);
        //Tablayout
        main_NAVBAR = findViewById(R.id.main_NAVBAR);
        main_NAVBAR_main = findViewById(R.id.main_NAVBAR_main);
        main_NAVBAR_history = findViewById(R.id.main_NAVBAR_history);
        main_NAVBAR_profile =findViewById(R.id.main_NAVBAR_profile);
    }

    private void initNavBar() {
        Bundle bundle = this.sendUserToFragment();
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(),main_NAVBAR.getTabCount(),bundle);
        main_VIEWPAGER.setAdapter(pagerAdapter);

        //change the tabs view when the tab is clicked.
        main_NAVBAR.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                main_VIEWPAGER.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        main_VIEWPAGER.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(main_NAVBAR));
        //EnableMyLocationServices();
    }

    private boolean fireBaseLogin() {
        /**
         * function check if user login to system, if not intent login activity.
         * if user is login Method returns true, else Method returns false.
         */
        boolean loginSuccess;
        if(!MyFireBaseServices.getInstance().isLogin()) {
            loginSuccess = false;
            Intent myIntent = new Intent(this,LoginActivity.class);
            startActivity(myIntent);
            finish();
        } else {
            loginSuccess = true;

        }
        return loginSuccess;
    }

    private void updateUser(String userName) {
        MyFireBaseServices.getInstance().updateUserDisplayName(userName);

        Vehicle vehicle = new Vehicle();

        user = new User()
                .setUid(MyFireBaseServices.getInstance().getUID())
                .setName(MyFireBaseServices.getInstance().getUserName())
                .setPhone(MyFireBaseServices.getInstance().getUserPhone())
                .setVehicle(vehicle);
    }

    private void createUser() {
        Vehicle vehicle = new Vehicle();
        user = new User()
                .setUid(MyFireBaseServices.getInstance().getUID())
                .setName(MyFireBaseServices.getInstance().getUserName())
                .setPhone(MyFireBaseServices.getInstance().getUserPhone())
                .setVehicle(vehicle);
        detailUpdateAsk();
    }

    private boolean isNewUser() {
        /**
         * Method checks if user details are empty if it does return false, else method returns true.
         */
        if(user.getName().equalsIgnoreCase("")) {
            return true;
        } else {
            return false;
        }
    }

    private void detailUpdateAsk() {
        /**
         * if user name is equal "", user is new user.
         * in case of new user method popup dialog alert that asks to fill user details.
         */
        if (isNewUser()) {
            MySignal.getInstance().alertDialog(this,"NEW USER!","Welcome ,It's look like your are a new user, press \'Update Settings\'" +
                            "to update your details and enable account vehicle share.","Update Settings","Cancel",
                    new  DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String jsonUser = getJsonUser(user);
                            Intent intent = new Intent(MainActivity.this, UpdateUserActivity.class);
                            intent.putExtra(USER_INTENT,jsonUser);
                            startActivity(intent);
                        }
                    });
        }
    }

    private Bundle sendUserToFragment() {
        String jsonUser = getJsonUser(user);
        Bundle bundle = new Bundle();
        bundle.putString(USER_INTENT,jsonUser);
        return bundle;
    }

    private String getJsonUser (User user){
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user);
        return jsonUser;
    }

    private void EnableMyLocationServices() {
        // Bind to LocalService
        if(MyLocationServices.getInstance().checkLocationPermission()) {
            if(MyLocationServices.getInstance().isGpsEnabled()) {
                isLocationTrakerOn = true;
                Log.d("pttt", "EnableMyLocationServices: ");
                Intent intent = new Intent(this, GpsTracker_service.class);
                startService(intent);
            }
        }
    }

    private void disableMyLocationServices() {
        Log.d("pttt", "disableMyLocationServices: ");
        isLocationTrakerOn=false;
        this.stopService(new Intent(this,GpsTracker_service.class));
    }

    public User getUser() {
        return this.user;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("pttt", "MainActivity - onRequestPermissionsResult:\t requestCode="+requestCode);
        final int unmaskedRequestCode = requestCode & 0x0000ffff;
        Log.d("pttt", "MainActivity - onRequestPermissionsResult:\t UnMaskedRequestCode="+unmaskedRequestCode);
        switch (unmaskedRequestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("pttt", "onRequestPermissionsResult: APPROVE by main");
                    if(!isLocationTrakerOn){
                        EnableMyLocationServices();
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("pttt", "onRequestPermissionsResult: DENY by main");

                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("pttt", "onStart: ");
        if(!isLocationTrakerOn) {
            EnableMyLocationServices();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("pttt", "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("pttt", "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("pttt", "onStop: ");
        disableMyLocationServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("pttt", "onDestroy: ");
    }
}