package com.example.spark.objects;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.spark.untils.MySignal;

public class BluetoothBackgroundService extends Service {

    private final int INTERVAL_TIME = 10000;

    //public Context context = this;
    private static Handler handler = null;
    private static Runnable runnable = null;
    private static boolean isBtConnected = false;
    private static boolean stopped= false;
    private static BluetoothCallBack bluetoothCallBack;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        MySignal.getInstance().toast("Parking Auto Parking Service - ON");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /**
         * Check in background service if device connection to a bluetooth device, if it does when bluetooth disconnect application save parking location.
         */
        Log.d("pttt", "onStartCommand:");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothTurnOn(mBluetoothAdapter);
        stopped = false;
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                if(isBluetoothConnectedToDevice(mBluetoothAdapter)) {
                    isBtConnected = true;
                    Log.d("pttt", "Bluetooth connected!");
                } else if (!isBluetoothConnectedToDevice(mBluetoothAdapter) && isBtConnected) {
                    MySignal.getInstance().toast("Parking Detected!");
                    if(bluetoothCallBack!=null){
                        bluetoothCallBack.onDisconnected();
                        stopped =true;
                    }
                }
                if(!stopped || !isBluetoothEnabled(mBluetoothAdapter)) {
                    handler.postDelayed(runnable, INTERVAL_TIME);
                }
            }
        };
        handler.postDelayed(runnable, INTERVAL_TIME);
        return START_STICKY;
    }

    public static void setCallback(BluetoothCallBack mCallBack) {
        bluetoothCallBack = mCallBack;
    }

    public static boolean isBluetoothConnectedToDevice(BluetoothAdapter mBluetoothAdapter) {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED;
    }

    public boolean isBluetoothEnabled(BluetoothAdapter mBluetoothAdapter)
    {
        return mBluetoothAdapter.isEnabled();
    }

    public void bluetoothTurnOn(BluetoothAdapter mBluetoothAdapter){
        /**
         * Turn on bluetooth only if needed
         */
        if(!isBluetoothEnabled(mBluetoothAdapter)) {
            Log.d("pttt", "Bluetooth enabled!");
            mBluetoothAdapter.enable();
        }
    }

    public void bluetoothTurnOff(BluetoothAdapter mBluetoothAdapter){
        if(isBluetoothEnabled(mBluetoothAdapter)) {
            Log.d("pttt", "Bluetooth disable!");
            mBluetoothAdapter.disable();
        }
    }

    public static void stopService() {
        if(handler!=null) {
            handler.removeCallbacks(runnable);
            handler = null;
            runnable = null;
            isBtConnected = false;
            stopped = false;
            MySignal.getInstance().toast("Parking Auto Parking Service - OFF");
        }
    }
}

