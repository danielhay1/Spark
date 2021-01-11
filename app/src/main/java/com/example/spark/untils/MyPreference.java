package com.example.spark.untils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

public class MyPreference {
    private static MyPreference instance;
    private SharedPreferences sharedPreferences;

    public interface KEYS {
        public static final String TOP_SCORES_ARRAY = "topScores";
    }
    public static MyPreference getInstance() {
        //Singleton design pattern
        return instance;
    }

    private MyPreference(Context appContext) {
        sharedPreferences = appContext.getSharedPreferences("myPreference",Context.MODE_PRIVATE);
    }

    public static void Init(Context appContext) {
        if (instance == null) {
            Log.d("pttt", "Init: MyPreference");
            instance = new MyPreference(appContext);
        }
    }

    public void putString(String key, String value) {
        Log.d("pttt", "putString");
        SharedPreferences.Editor editor  = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        Log.d("pttt", "getString");
        return sharedPreferences.getString(key,"");
    }

    public void putObject(String key, Object object) {
        Log.d("pttt", "putObject");
        Gson gson = new Gson();
        String jsonElement = gson.toJson(object);
        this.putString(key, jsonElement);
    }

    public Object getObject(String key) {
        Log.d("pttt", "getObject");
        Gson gson = new Gson();
        return gson.fromJson(getString(key),Object.class);
    }

    public Object getLatLng(String key) {
        Log.d("pttt", "getObject");
        Gson gson = new Gson();
        return gson.fromJson(getString(key), LatLng.class);
    }


    public void deleteKey(String key) {
        Log.d("pttt", "deleteKey");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public void deleteAllData() {
        Log.d("pttt", "deleteAllData");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
