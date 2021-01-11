package com.example.spark.untils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class Sound {

    private static Sound instance;
    private MediaPlayer mp;
    private Context appContext;

    public interface KEYS {
    }
    public static Sound getInstance() {
        return instance;
    }

    private Sound(Context context) {
        this.mp = new MediaPlayer();
        this.appContext = context.getApplicationContext();
    }

    public static void Init(Context appContext){
        if(instance == null) {
            Log.d("pttt", "Init: Sound");
            instance = new Sound(appContext);
        }
    }

    public void playSound(int rawSound) {
        stopSound();
        Log.d("pttt", "~~Playing sound~~");
        mp = MediaPlayer.create(this.appContext,rawSound);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
                mp=null;
            }
        });
        mp.start();
        }

    public void stopSound() {
        if (mp != null) {
            Log.d("pttt", "~~Stoping sound~~");
            try {
                mp.reset();
                mp.prepare();
                mp.stop();
                mp.release();
                mp=null;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
