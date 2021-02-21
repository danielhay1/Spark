package com.example.spark.objects;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.spark.fragments.Map_Fragment;
import com.example.spark.fragments.MyProfileFragment;
import com.example.spark.fragments.ParkHistoryFragment;
import com.google.android.gms.maps.MapFragment;

public class PagerAdapter extends FragmentPagerAdapter {
    private int numOfTabs;
    private Bundle fragmentBundle;

    private Map_Fragment map_fragment;
    private ParkHistoryFragment parkHistoryFragment;
    private MyProfileFragment myProfileFragment;

    public PagerAdapter(@NonNull FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
        initFragments(fragmentBundle);
    }

    private void initFragments(Bundle fragmentBundle) {
        map_fragment = new Map_Fragment();
        parkHistoryFragment = new ParkHistoryFragment();
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
