package com.example.spark.objects;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.spark.fragments.Map_Fragment;
import com.example.spark.fragments.MyProfileFragment;
import com.example.spark.fragments.ParkingHistoryFragment;
import com.google.android.gms.maps.model.LatLng;

public class PagerAdapter extends FragmentPagerAdapter {
    private int numOfTabs;
    private Bundle fragmentBundle;

    private Map_Fragment map_fragment;
    private ParkingHistoryFragment parkHistoryFragment;
    private MyProfileFragment myProfileFragment;

    private Map_Fragment.SendSignal sendParking_CallBack = new Map_Fragment.SendSignal() {
        @Override
        public void parkingHistoryLoadSignal() {
            parkHistoryFragment.getParkingHistory();
        }
    };

    private ParkingHistoryFragment.SendLatLng sendLatLng_CallBack = new ParkingHistoryFragment.SendLatLng(){
        @Override
        public void sendLatLng(Parking parking) {
            map_fragment.addHistoryMarkerToMap(parking);
        }
    };

    public PagerAdapter(@NonNull FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
        initFragments(fragmentBundle);
    }

    private void initFragments(Bundle fragmentBundle) {
        map_fragment = new Map_Fragment();
        map_fragment.setCallback(sendParking_CallBack);
        parkHistoryFragment = new ParkingHistoryFragment();
        parkHistoryFragment.setCallBack(sendLatLng_CallBack);
        myProfileFragment = new MyProfileFragment();
    }

    public void initBundle(Bundle fragmentBundle) {
        setBundle(fragmentBundle);
        if(fragmentBundle != null) {
            map_fragment.setArguments(fragmentBundle);
            parkHistoryFragment.setArguments(fragmentBundle);
            myProfileFragment.setArguments(fragmentBundle);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        map_fragment.onActivityResult(requestCode,resultCode,data);
        parkHistoryFragment.onActivityResult(requestCode,resultCode,data);
        myProfileFragment.onActivityResult(requestCode,resultCode,data);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return map_fragment;
            case 1:
                return parkHistoryFragment;
            case 2:
                return myProfileFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }

    public void setBundle(Bundle bundle) {
        this.fragmentBundle = bundle;
    }
}
