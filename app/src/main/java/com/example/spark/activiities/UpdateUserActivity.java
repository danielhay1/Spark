package com.example.spark.activiities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.spark.R;
import com.example.spark.objects.User;
import com.example.spark.objects.Vehicle;
import com.example.spark.untils.MyFireBaseServices;
import com.example.spark.untils.MySignal;
import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateUserActivity extends AppCompatActivity {
    private EditText updateUser_EDT_name;
    private EditText updateUser_EDT_vehicleNumber;
    private EditText updateUser_EDT_vehicleNick;
    private com.google.android.material.button.MaterialButton updateUser_BTN_apply;

    private User user;
    private Vehicle vehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);
        getUser();
        this.vehicle = new Vehicle();
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
                boolean res = updateDetails();
                if(res == true) {
                    //save to firebase
                    MySignal.getInstance().toast("User updated!");
                    finish();
                }
                else {
                    MySignal.getInstance().toast("One or more fields are empty or wrong value inserted, to apply fill all fields.");
                }
            }
        });
    }

    private void saveUserChanges(User user, Vehicle vehicle) {
        /**
         * Method user changes to firebase
         */
        MyFireBaseServices.getInstance().saveVehicleToFireBase(vehicle);
        MyFireBaseServices.getInstance().saveUserToFireBase(user);
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

    private boolean updateDetails() {
        String name = updateUser_EDT_name.getText().toString();
        String vehicleNumber = updateUser_EDT_vehicleNumber.getText().toString();
        String VehicleNick = updateUser_EDT_vehicleNick.getText().toString();
        boolean userUpdated = updateUser(name,vehicleNumber);
        if(userUpdated) {
            Vehicle vehicle = createVehicle(vehicleNumber,VehicleNick);
            if(!vehicle.isOwnedBy(user.getUid())) {
                vehicle.addOwner(user.getUid());
                saveUserChanges(user, vehicle);
            }
        }
        return userUpdated;
    }

    private boolean updateUser(String name, String vehicleNumber) {
        //check if inserted values are legal
        boolean updatesResults[] = new boolean[2];
        updatesResults[0] = updateValue("name",name);
        updatesResults[1] = updateValue("Vehicle number",vehicleNumber);
        return updatesResults[0] && updatesResults[1];
    }

    private Vehicle createVehicle(String vehicleNumber, String vehicleNick) {
        /**
         * Method load vehicle from DB, if vehicle_number is in DB method update vehicle nick
         * if vehicle_number isn't in the DB, creates new vehicle and insert it to DB.
         */
        if(vehicleNick.equalsIgnoreCase("")) {
            Log.d("pttt", "createVehicle: no car nickname inserted");
        }
        else {
            vehicleNick = "myCar";
        }
        Vehicle vehicle = new Vehicle()
                .setVehicleID(vehicleNumber)
                .setVehicleNick(vehicleNick);
        vehicle.addOwner(user.getUid());
        return vehicle;
    }

    private boolean updateValue(String valType, String value) {
        if(!value.equalsIgnoreCase("")) {
            if(valType == "name") {
                validInputCheck("[A-Za-z]+",value);
                user.setName(value);
            } else if(valType.equalsIgnoreCase("Vehicle number")) {
                validInputCheck("[0-9]+",value);
                user.setVehicleID(value);
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