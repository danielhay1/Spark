package com.example.spark.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.example.spark.R;
import com.example.spark.activiities.MainActivity;
import com.example.spark.activiities.SplashActivity;
import com.example.spark.activiities.UpdateUserActivity;
import com.example.spark.objects.Vehicle;
import com.example.spark.untils.MyFireBaseServices;
import com.example.spark.objects.User;
import com.google.gson.Gson;

public class MyProfileFragment extends Fragment {

    private TextView myprofile_TV_name;
    private TextView myprofile_TV_PhoneNumber;
    private TextView myprofile_TV_vehicleNumber;
    private TextView myprofile_TV_vehicleNick;
    private TextView myprofile_TV_vehicleOwners;
    private com.google.android.material.button.MaterialButton myprofile_BTN_logout;
    private com.google.android.material.button.MaterialButton updateUser_BTN_editProfile;
    private User user;
    private Vehicle vehicle;
    private boolean isFragmentCreated = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        findViews(view);
        getUser();
        initViews();

        return view;
    }

    private void findViews(View view) {
        myprofile_BTN_logout = view.findViewById(R.id.myprofile_BTN_logout);
        updateUser_BTN_editProfile = view.findViewById(R.id.updateUser_BTN_editProfile);
        myprofile_TV_name = view.findViewById(R.id.myprofile_TV_name);
        myprofile_TV_PhoneNumber = view.findViewById(R.id.myprofile_TV_PhoneNumber);
        myprofile_TV_vehicleNumber = view.findViewById(R.id.myprofile_TV_vehicleNumber);
        myprofile_TV_vehicleNick = view.findViewById(R.id.myprofile_TV_vehicleNick);
        myprofile_TV_vehicleOwners = view.findViewById(R.id.myprofile_TV_vehicleOwners);
        isFragmentCreated = true;
    }

    private void initViews() {
        updateUserData();
        myprofile_BTN_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });
        updateUser_BTN_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jsonUser = getJsonUser(user);
                Intent intent = new Intent(getActivity(), UpdateUserActivity.class);
                intent.putExtra(MainActivity.USER_INTENT,jsonUser);
                startActivityForResult(intent, MainActivity.UPDATE_USER);
            }
        });
    }

    private void getUser() {
        if(user == null || user.getConnectedVehicleID().equalsIgnoreCase("")) {
            user = new User()
                    .setUid(MyFireBaseServices.getInstance().getUID())
                    .setName(MyFireBaseServices.getInstance().getUserName())
                    .setPhone(MyFireBaseServices.getInstance().getUserPhone())
                    .setConnectedVehicleID("");

            Gson gson = new Gson();
            if (getArguments() == null) {
                getUserFromFireBase(user.getUid());
            } else {
                String jsonUser = getArguments().getString(MainActivity.USER_INTENT);
                User loadedJsonUser = gson.fromJson(jsonUser, User.class);
                if (loadedJsonUser == null) {
                    Log.d("pttt", "getUser: \t USER IS NULL!");
                } else {
                    this.user = loadedJsonUser;
                    Log.d("pttt", "getUser: \tUID = " + user);
                }
            }
        }
    }

    private void getUserFromFireBase(String uid) {
        MyFireBaseServices.getInstance().loadUserFromFireBase(uid, new MyFireBaseServices.CallBack_LoadUser() {
            @Override
            public void userDetailsUpdated(User result) {
                if(result != null) {
                    user = result;
                    Log.d("pttt", "userDetailsUpdated: \n"+user);
                    //setUserDetails
                    updateUserData();
                }
            }
            @Override
            public void loadFailed(Exception e) {
                Log.d("pttt", "loadFailed: Failed to read value "+e.getMessage());
            }
        });
    }


    private String getJsonUser (User user){
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user);
        return jsonUser;
    }

    private void updateUserData() {
        if(user != null) {
            updateField(user.getName(),myprofile_TV_name);
            updateField(user.getPhone(),myprofile_TV_PhoneNumber);
            updateField(user.getConnectedVehicleID(),myprofile_TV_vehicleNumber);


            MyFireBaseServices.getInstance().loadVehicleFromFireBase(user.getConnectedVehicleID(), new MyFireBaseServices.CallBack_LoadVehicle() {
                @Override
                public void vehicleDetailsUpdated(Vehicle result) {
                    vehicle = result;
                    if(vehicle != null) {
                        String ownersName = vehicle.ownersNamesToString();
                        //Log.d("pttt", "owners = "+ownersName);
                        updateField(vehicle.getVehicleNick(),myprofile_TV_vehicleNick);
                        updateField(ownersName,myprofile_TV_vehicleOwners);
                    }
                    else {
                        updateField("",myprofile_TV_vehicleNick);
                        updateField("",myprofile_TV_vehicleOwners);
                    }
                }

                @Override
                public void loadFailed(Exception e) {
                    Log.d("pttt", "loadFailed: Failed to read value "+e.getMessage());
                    updateField("",myprofile_TV_vehicleNick);
                    updateField("",myprofile_TV_vehicleOwners);
                }
            });


        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int userRequest = MainActivity.UPDATE_USER;
        Log.d("pttt", "onActivityResult (MyProfileFragment): request= "+requestCode +", result= "+resultCode);
        if (requestCode == userRequest) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra(MainActivity.USER_INTENT);
                Gson gson = new Gson();
                user = gson.fromJson(result,User.class);
                Log.d("pttt", "onActivityResult (MyProfileFragment): user updated, user ="+user);
                if(isFragmentCreated) {
                    updateUserData();
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("pttt", "onActivityResult (MyProfileFragment): user isn't updated.");
            }
        }
    }

    private void updateField(String value, TextView textView) {
        if(textView!=null) {
            if(value.equalsIgnoreCase("")||value == null) {
                textView.setText("-");
            }
            else {
                textView.setText("" + value);
            }
        } else {
            Log.d("pttt", "updateField: textview is null");
        }
    }


    private void logOut() {
        MyFireBaseServices.getInstance().signOut();
        startActivity(new Intent(getActivity(), SplashActivity.class));
        getActivity().finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(user==null || user.getConnectedVehicleID().equalsIgnoreCase("")) {
            getUser();
        }
    }
}

