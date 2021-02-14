package com.example.spark;

import android.app.Application;

import com.example.spark.untils.MyFireBaseServices;
import com.example.spark.untils.ImgLoader;
import com.example.spark.untils.MyLocationServices;
import com.example.spark.untils.MyPreference;
import com.example.spark.untils.MySignal;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MyLocationServices.Init(this);
        MySignal.Init(this);
        ImgLoader.Init(this);
        MyPreference.Init(this);
        MyFireBaseServices.Init();
        //MyPreference.getInstance().deleteAllData();
    }
}
