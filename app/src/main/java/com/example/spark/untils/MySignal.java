package com.example.spark.untils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.spark.R;

public class MySignal {
    private  static MySignal instance;
    private Context appContext;

    public static MySignal getInstance() {
        return instance;
    }

    private MySignal(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static void Init(Context appContext){
        if(instance == null) {
            Log.d("pttt", "Init: MySignal");
            instance = new MySignal(appContext);
        }
    }

    public void toast(String msg) {
        Log.d("pttt", "toast: "+msg);
        Toast.makeText(appContext,msg,Toast.LENGTH_SHORT).show();
    }

    public void alertDialog(Activity activity, String title, String msg, String pos, String neg, DialogInterface.OnClickListener onClickListener) {
                AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(msg)
                //set positive button
                .setPositiveButton(pos, onClickListener)
                //set negative button
                .setNegativeButton(neg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what should happen when negative button is clicked
                    }
                }).show();
    }
}
