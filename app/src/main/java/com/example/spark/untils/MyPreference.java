package com.example.spark.untils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.spark.objects.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

public class MyPreference {
    private static MyPreference instance;
    private SharedPreferences sharedPreferences;

    private final String MY_PARKINGLOCATION = "my_parking_location";

    public static MyPreference getInstance() {
        //Singleton design pattern
        return instance;
    }

    private MyPreference(Context appContext) {
        sharedPreferences = appContext.getSharedPreferences(MY_PARKINGLOCATION,Context.MODE_PRIVATE);
    }

    public static void Init(Context appContext) {
        if (instance == null) {
            Log.d("pttt", "Init: MyPreference");
            instance = new MyPreference(appContext);
        }
    }

    public void putString(String key, String value) {
        Log.d("pttt", "putString \tkey= "+key+", Value= "+value);
        SharedPreferences.Editor editor  = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        Log.d("pttt", "getString ,key= "+key);
        return sharedPreferences.getString(key,"");
    }

    public void putObject(String key, Object object) {
        Log.d("pttt", "putObject \tkey= "+key+", Object= "+object);
        Gson gson = new Gson();
        String jsonElement = gson.toJson(object);
        this.putString(key, jsonElement);
        Log.d("pttt", "putObject: show all " + prefernceToString());
    }

    public Object getObject(String key) {
        Log.d("pttt", "getObject, key= "+key);
        Gson gson = new Gson();
        return gson.fromJson(getString(key),Object.class);
    }

    public Object getLatLng(String key) {
        Log.d("pttt", "getLatLng, key= "+key);
        Gson gson = new Gson();
        return gson.fromJson(getString(key), LatLng.class);
    }

    public void deleteKey(String key) {
        Log.d("pttt", "deleteKey, key= "+key);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
        Log.d("pttt", "putObject: show all " + prefernceToString());
    }

    public String prefernceToString() {
        return this.sharedPreferences.getAll().toString();
    }

    public void deleteAllData() {
        Log.d("pttt", "deleteAllData");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
