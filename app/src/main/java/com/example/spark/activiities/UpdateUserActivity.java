package com.example.spark.activiities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.spark.R;
import com.example.spark.objects.User;
import com.example.spark.untils.MySignal;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateUserActivity extends AppCompatActivity {
    private EditText updateUser_EDT_name;
    private EditText updateUser_EDT_vehicleNumber;
    private EditText updateUser_EDT_vehicleNick;
    private com.google.android.material.button.MaterialButton updateUser_BTN_apply;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);
        getUser();
        findView();
        initView();
    }

    private void findView() {
        updateUser_EDT_name = findViewById(R.id.updateUser_EDT_name);
        updateUser_EDT_vehicleNumber= findViewById(R.id.updateUser_EDT_vehicleNumber);
        updateUser_EDT_vehicleNick = findViewById(R.id.updateUser_EDT_vehicleNick);
        updateUser_BTN_apply = findViewById(R.id.updateUser_BTN_apply);
    }

    private void initView() {
        updateUser_BTN_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean res = updateUser();
                if(res == true) {
                    MySignal.getInstance().toast("User updated!");
                    finish();
                }
                else {
                    MySignal.getInstance().toast("All fields are empty, to apply fill at least one field.");
                }
            }
        });
    }

    private void getUser() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Gson gson = new Gson();
            String jsonUser = bundle.getString(MainActivity.USER_INTENT);
            User user = gson.fromJson(jsonUser,User.class);
            if(user == null) {
                Log.d("pttt", "getUser: \t USER IS NULL!");
            }
            else {
                this.user = user;
                Log.d("pttt", "getUser: \tUID = "+user.getUid());
            }
        }
    }

    private boolean updateUser() {
        //checks all EDT if EDT received text isnt null update user value
        String name = updateUser_EDT_name.getText().toString();
        String vehicleNumber = updateUser_EDT_vehicleNumber.getText().toString();
        String VehicleNick = updateUser_EDT_vehicleNick.getText().toString();
        boolean updatesResults[] = new boolean[3];
        updatesResults[0] = updateValue("name",name);
        updatesResults[1] = updateValue("Vehicle number",vehicleNumber);
        updatesResults[2] =updateValue("Vehicle nickname",VehicleNick);
        for (boolean res:updatesResults) {
            if(res == true) {
                return true;
            }
        }
        return false;
    }

    private boolean updateValue(String valType, String value) {
        if(!value.equalsIgnoreCase("")) {
            if(valType == "name") {
                //validInputCheck("[A-Za-z]+",value)
                user.setName(value);
            } else if(valType.equalsIgnoreCase("Vehicle number")) {
                user.getVehicle().setVehicleLicenceNumber(value);
            } else if(valType.equalsIgnoreCase("Vehicle nickname")) {
                user.getVehicle().setVehicleNick(value);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean validInputCheck(String pattern,String insertedVal) {
        /**
         * Receive pattern and input string to check and method returns if input string is match to pattern.
         */
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(insertedVal);
        return matcher.find();
    }


}