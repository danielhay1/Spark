package com.example.spark.activiities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
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
    private String previousUserVehicleId;
    private Vehicle vehicle;

    private enum InputStatus {
        OK,
        INVALID_INPUT,
        FIELD_IS_MISSING,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);
        getUser();
        findView();
        previousUserVehicleId = this.user.getConnectedVehicleID();
        initView();
    }

    private void findView() {
        updateUser_EDT_name = findViewById(R.id.updateUser_EDT_name);
        updateUser_EDT_vehicleNumber = findViewById(R.id.updateUser_EDT_vehicleNumber);
        updateUser_EDT_vehicleNick = findViewById(R.id.updateUser_EDT_vehicleNick);
        updateUser_BTN_apply = findViewById(R.id.updateUser_BTN_apply);
    }

    private void initView() {
        updateUser_BTN_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputStatus res = updateDetails();
            }
        });
    }

    private void submitInsertedData(InputStatus inputStatus) {
        if (inputStatus == InputStatus.OK) {
            submit();
        } else if (inputStatus == InputStatus.INVALID_INPUT) {
            MySignal.getInstance().toast("Username or Vehicle_id has invalid input please make sure the input is legal.");
        } else {
            MySignal.getInstance().toast("Username or Vehicle_id isn't inserted, to apply fill all fields!");
        }
    }

    private void submit() {
        //save to firebase
        saveUserChanges(user, vehicle);
        MySignal.getInstance().toast("User updated!");
        //send result
        Log.e("pttt", "submitInsertedData: user="+user );
        Intent returnIntent = new Intent();
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user);
        returnIntent.putExtra(MainActivity.USER_INTENT,jsonUser);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void saveUserChanges(User user, Vehicle vehicle) {
        /**
         * Method saves user and vehicle changes to firebase
         */
        MyFireBaseServices.getInstance().saveVehicleToFireBase(vehicle);
        MyFireBaseServices.getInstance().saveUserToFireBase(user);
    }

    private void getUser() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Gson gson = new Gson();
            String jsonUser = bundle.getString(MainActivity.USER_INTENT);
            User user = gson.fromJson(jsonUser, User.class);
            if (user == null) {
                Log.d("pttt", "getUser: \t USER IS NULL!");
            } else {
                this.user = user;
                Log.d("pttt", "getUser: \tUID = " + user.getUid());
            }
        }
    }

    private InputStatus updateDetails() {
        String name = updateUser_EDT_name.getText().toString();
        String vehicleNumber = updateUser_EDT_vehicleNumber.getText().toString();
        String VehicleNick = updateUser_EDT_vehicleNick.getText().toString();
        InputStatus userUpdated = updateUser(name, vehicleNumber);
/*        if(previousUserVehicleId !=null && user.isOwnedVehicle(previousUserVehicleId) && (!previousUserVehicleId.equals(vehicleNumber))) {
            removeVehicleOwner(previousUserVehicleId,vehicleNumber,user.getUid());
        }*/
        Log.e("pttt","user="+user);
        if (userUpdated == InputStatus.OK) {
            loadVehicle(name, vehicleNumber, VehicleNick,InputStatus.OK);
        }
        return userUpdated;
    }

    private void removeVehicleOwner(String vehicleID, String updatedVehicleId, String uid) {
        /**
         * Method checks if vehicle has no owners, if does delete vehicle from DB.
         */
        Log.e("pttt", "removeVehicleOwner: ");
        MyFireBaseServices.getInstance().loadVehicleFromFireBase(vehicleID, new MyFireBaseServices.CallBack_LoadVehicle() {
            @Override
            public void vehicleDetailsUpdated(Vehicle result) {
                if(result != null) {
                    if(result.isOwnedBy(uid)) {
                        result.removeOwner(uid);
                        if(result.hasNoOwners()){
                            MyFireBaseServices.getInstance().deleteVehicleFromFireBase(result.getVehicleID());
                            user.removeVehicle(vehicleID);
                            user.setConnectedVehicleID(updatedVehicleId);
                        }
                    }
                } else {
                    Log.d("pttt", "loadFailed: Failed to read value: VALUE IS NULL!");
                }
            }

            @Override
            public void loadFailed(Exception e) {
                Log.d("pttt", "loadFailed: Failed to read value "+e.getMessage());
            }
        });
    }

    private InputStatus updateUser(String name, String vehicleNumber) {
        //check if inserted values are legal
        InputStatus updatesResults[] = new InputStatus[2];
        updatesResults[0] = updateValue("name", name);
        updatesResults[1] = updateValue("Vehicle number", vehicleNumber);
        if (updatesResults[0] == InputStatus.OK && updatesResults[1] == InputStatus.OK) {
            return InputStatus.OK;
        } else {
            if (updatesResults[0] == InputStatus.FIELD_IS_MISSING || updatesResults[1] == InputStatus.FIELD_IS_MISSING) {
                return InputStatus.FIELD_IS_MISSING;
            } else {
                return InputStatus.INVALID_INPUT;
            }
        }
    }

    private void loadVehicle(String userName, String vehicleNumber, String vehicleNick,InputStatus inputStatus) {
        /**
         * Method load vehicle from DB, if vehicle_number is in DB method update vehicle nick
         * if vehicle_number isn't in the DB, creates new vehicle and insert it to DB.
         */
        if (vehicleNick.equalsIgnoreCase("")) {
            Log.d("pttt", "createVehicle: no vehicle nickname inserted");
            vehicleNick = "My vehicle";
        }

        vehicle = new Vehicle()
                .setVehicleID(vehicleNumber)
                .setVehicleNick(vehicleNick);
        vehicle.addOwner(user.getUid());
        //vehicle.addOwnerName(userName);
        MyFireBaseServices.getInstance().loadVehicleFromFireBase(vehicleNumber, new MyFireBaseServices.CallBack_LoadVehicle() {
            @Override
            public void vehicleDetailsUpdated(Vehicle result) {
                Log.e("pttt","user="+user);
                if(result != null) {
                    if (!result.isOwnedBy(user.getUid())) {
                        Log.d("pttt", "Vehicle has new owner!");
                        result.addOwner(user.getUid());
                        //result.addOwnerName(userName);
                    } else {
                        //result.changeOwnerName(user.getUid(),userName);
                    }

                    vehicle = result;
                    Log.d("pttt", "vehicleDetailsUpdated: "+vehicle);
                    submitInsertedData(inputStatus);
                } else {
                    Log.d("pttt", "vehicleDetailsUpdated: NULL RESULT!");
                    submitInsertedData(inputStatus);
                }
            }
            @Override
            public void loadFailed(Exception e) {
                Log.d("pttt", "loadFailed: "+e.getStackTrace());
            }
        });

    }

    private InputStatus updateValue(String valType, String value) {
        InputStatus inputStatus = InputStatus.OK;
        if (!value.equalsIgnoreCase("")) {
            if (valType.equalsIgnoreCase("name")) {
                if (!validInputCheck("[A-Za-z]+", value)) {
                    inputStatus = InputStatus.INVALID_INPUT;
                } else {
                    user.setName(value);
                }
            } else if (valType.equalsIgnoreCase("Vehicle number")) {
                if (!validInputCheck("[0-9]+", value)) {
                    inputStatus = InputStatus.INVALID_INPUT;
                } else {
                    user.setConnectedVehicleID(value);
                }
            }
        } else {
            inputStatus = InputStatus.FIELD_IS_MISSING;
        }
        return inputStatus;
    }


    private boolean validInputCheck(String pattern, String insertedVal) {
        /**
         * Receive pattern and input string to check and method returns if input string is match to pattern.
         */
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(insertedVal);
        return matcher.find();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}
