package com.example.spark.activiities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.spark.R;
import com.example.spark.objects.PagerAdapter;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class MainActivity extends AppCompatActivity{

    private TabLayout main_NAVBAR;
    private TabItem main_NAVBAR_main,main_NAVBAR_history,main_NAVBAR_profile;
    private ViewPager main_VIEWPAGER;
    public PagerAdapter pagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(fireBaseLogin()) {   //FireBase login
            findViews();
            initNavBar();
        }
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
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(),main_NAVBAR.getTabCount());
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
    }

    private boolean fireBaseLogin() {
        /**
         * function check if user login to system, if not intent login activity.
         * if user is login Method returns true, else Method returns false.
         */
        boolean loginSuccess;
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            Intent myIntent = new Intent(this,LoginActivity.class);
            startActivity(myIntent);
            finish();
            loginSuccess = false;
        } else {
            Log.d("pttt", "Uid = " + firebaseUser.getUid()
                    + "\nDisplayName = " + firebaseUser.getDisplayName()
                    + "\nEmail = " + firebaseUser.getEmail()
                    + "\nPhoneNumber = " + firebaseUser.getPhoneNumber()
                    + "\nPhotoUrl = " + firebaseUser.getPhotoUrl());
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName("").build();
            firebaseUser.updateProfile(profileUpdates);
            firebaseAuth.updateCurrentUser(firebaseUser);
            loginSuccess = true;
        }
        return loginSuccess;
    }


}