package com.example.spark.fragments;

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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        getUser();
        findViews(view);
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
                startActivity(intent);
            }
        });
    }

    private void getUser() {
        Gson gson = new Gson();
        String jsonUser = getArguments().getString(MainActivity.USER_INTENT);
        User user = gson.fromJson(jsonUser,User.class);
        if(user == null) {
            Log.d("pttt", "getUser: \t USER IS NULL!");
        }
        else {
            this.user = user;
            Log.d("pttt", "getUser: \tUID = "+user.getUid());
        }
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
            updateField(user.getVehicleID(),myprofile_TV_vehicleNumber);


            MyFireBaseServices.getInstance().loadVehicleFromFireBase(user.getVehicleID(), new MyFireBaseServices.CallBack_LoadVehicle() {
                @Override
                public void vehicleDetailsUpdated(Vehicle result) {
                    vehicle = result;
                }
            });
            if(vehicle != null) {
                updateField(vehicle.getVehicleNick(),myprofile_TV_vehicleNick);
                updateField(vehicle.getOwnersName(),myprofile_TV_vehicleOwners);
            }
            else {
                updateField("",myprofile_TV_vehicleNick);
                updateField("",myprofile_TV_vehicleOwners);
            }

        }
    }

    private void updateField(String value, TextView textView) {
        if(value.equalsIgnoreCase("")) {
            textView.setText(textView.getText() + " - ");
        }
        else {
            textView.setText(textView.getText() + " " + value);
        }
    }


    private void logOut() {
        MyFireBaseServices.getInstance().signOut();
        startActivity(new Intent(getActivity(), SplashActivity.class));
        getActivity().finish();
    }

}

