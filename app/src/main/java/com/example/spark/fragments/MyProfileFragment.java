package com.example.spark.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;


import com.example.spark.R;
import com.example.spark.activities.MainActivity;
import com.example.spark.activities.SplashActivity;
import com.example.spark.activities.UpdateUserActivity;
import com.example.spark.objects.Vehicle;
import com.example.spark.untils.MyFireBaseServices;
import com.example.spark.objects.User;
import com.example.spark.untils.MySignal;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MyProfileFragment extends Fragment {

    private TextView myprofile_TV_name;
    private TextView myprofile_TV_PhoneNumber;
    private TextView myprofile_TV_vehicleNumber;
    private TextView myprofile_TV_vehicleNick;
    private TextView myprofile_TV_vehicleOwners;
    private TextInputLayout myprofile_TF_myVehicles;
    private AutoCompleteTextView myprofile_ACT_myVehicles;
    private com.google.android.material.button.MaterialButton updateUser_BTN_removeVehicle;
    private com.google.android.material.button.MaterialButton myprofile_BTN_logout;
    private com.google.android.material.button.MaterialButton updateUser_BTN_editProfile;
    private User user;
    private Vehicle vehicle;
    private ArrayAdapter<String> adapter;
    private FragmentToActivity fragmentToActivityCallBack;
    private String userNames="";
    private boolean isResumed=false;

    public interface FragmentToActivity {
        public void onDataPass(String data);
    }

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
        myprofile_TF_myVehicles = view.findViewById(R.id.myprofile_TF_myVehicles);
        myprofile_ACT_myVehicles = view.findViewById(R.id.myprofile_ACT_myVehicles);
        updateUser_BTN_removeVehicle = view.findViewById(R.id.updateUser_BTN_removeVehicle);
    }

    private void initViews() {
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
        updateUser_BTN_removeVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeVehicleFromUser();
            }
        });
    }

    private void removeVehicleFromUser() {
        if(!user.getConnectedVehicleID().equalsIgnoreCase("") && (!this.user.getMyVehicles().isEmpty())) {
            String msg;
            if(vehicle==null) {
                msg = "Are you sure you want to remove this vehicle?" +
                        "\n Vehicle number: \'"+user.getConnectedVehicleID()+"\'";
            }
            msg = "Are you sure you want to remove this vehicle?" +
                    "\n Vehicle number: \'"+user.getConnectedVehicleID()+"\'" +
                    "\nVehicle nickname: \'"+vehicle.getVehicleNick()+"\'";
            MySignal.getInstance().alertDialog(this.getActivity(), "Delete Vehicle:", msg, "Delete", "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeVehicleOwner();
                }
            });
        } else {
            MySignal.getInstance().toast("User has no vehicles!");
        }

    }

    private void removeVehicleOwner() {
        /**
         * Method checks if vehicle has no owners, if does delete vehicle from DB.
         */
        Log.d("pttt", "removeVehicleOwner: (MyProfileFragment)");
        MyFireBaseServices.getInstance().loadVehicleFromFireBase(user.getConnectedVehicleID(), new MyFireBaseServices.CallBack_LoadVehicle() {
            @Override
            public void vehicleDetailsUpdated(Vehicle result) {
                if(result != null) {
                    if(result.isOwnedBy(user.getUid())) {
                        result.removeOwner(user.getUid());
                        user.removeVehicle(result.getVehicleID());
                        if(result.hasNoOwners()){
                            MyFireBaseServices.getInstance().deleteVehicleFromFireBase(result.getVehicleID());
                        }
                        MyFireBaseServices.getInstance().saveVehicleToFireBase(result);
                        sendResult(user);
                        if(user.getMyVehicles().isEmpty()) {
                            myprofile_ACT_myVehicles.setText("User has no vehicles!");
                            myprofile_ACT_myVehicles.setTextColor(Color.RED);
                        } else {
                            myprofile_ACT_myVehicles.setHint("");
                            myprofile_ACT_myVehicles.setHintTextColor((Color.WHITE));
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

    private void getUser() {
        /**
         * Method load get user from activity, if it fail -> try to load user from firebase,
         * if it fail -> return new user.
         */
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
        /**
         * Method load user from firebase.
         */
        MyFireBaseServices.getInstance().loadUserFromFireBase(uid, new MyFireBaseServices.CallBack_LoadUser() {
            @Override
            public void userDetailsUpdated(User result) {
                if(result != null) {
                    user = result;
                    Log.d("pttt", "userDetailsUpdated: \n"+user);
                    //setUserDetails
                }
            }
            @Override
            public void loadFailed(Exception e) {
                Log.d("pttt", "loadFailed: Failed to read value "+e.getMessage());
            }
        });
    }

    private void setmMyVehicles(ArrayList<String> myVehicles){
        /**
         * Method build droplist of all user vehicles.
         */
        if(this.adapter.isEmpty()) {
            myprofile_ACT_myVehicles.setText("User has no vehicles!");
            myprofile_ACT_myVehicles.setTextColor((Color.RED));
        } else {
            myprofile_ACT_myVehicles.setText(""+user.getConnectedVehicleID());
            myprofile_ACT_myVehicles.setTextColor((Color.WHITE));
        }
        adapter = new ArrayAdapter<String>(this.getContext(),R.layout.dropdown_item,myVehicles);
        myprofile_ACT_myVehicles.setAdapter(adapter);
        myprofile_ACT_myVehicles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TO CONTINUE: Log in the selected vehicle.
                String selected = adapter.getItem(position);
                Log.d("pttt", "onItemSelected: selected item = "+selected);
                user.setConnectedVehicleID(selected);
                sendResult(user);
            }
        });
    }

    private void sendResult(User user) {
        /**
         * Method saves user connected vehicle and update it in MainActivity and all his fragments, update save it in firebase too.
         */
        MyFireBaseServices.getInstance().saveUserToFireBase(user);  //save user connected vehicle in firebase.
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user);
        fragmentToActivityCallBack.onDataPass(jsonUser);
    }


    private String getJsonUser (User user){
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user);
        return jsonUser;
    }

    private void updateUserData(User user) {
        /**
         * Method updates my profile data.
         */
        if(user != null) {
            adapter = new ArrayAdapter<String>(this.getContext(),R.layout.dropdown_item,user.getMyVehicles());
            updateField(user.getName(),myprofile_TV_name);
            updateField(user.getPhone(),myprofile_TV_PhoneNumber);
            updateField(user.getConnectedVehicleID(),myprofile_TV_vehicleNumber);
            setmMyVehicles(user.getMyVehicles());
            MyFireBaseServices.getInstance().loadVehicleFromFireBase(user.getConnectedVehicleID(), new MyFireBaseServices.CallBack_LoadVehicle() {
                @Override
                public void vehicleDetailsUpdated(Vehicle result) {
                    vehicle = result;
                    if(vehicle != null) {
                        updateField(vehicle.getVehicleNick(),myprofile_TV_vehicleNick);
                        loadVehicleOwnersName(vehicle);
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            fragmentToActivityCallBack = (FragmentToActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragmentToActivity");
        }
    }

    @Override
    public void onDetach() {
        fragmentToActivityCallBack = null;
        super.onDetach();
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
                if(isResumed){
                    updateUserData(user);
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("pttt", "onActivityResult (MyProfileFragment): user isn't updated.");
            }
        }
    }

    public void loadVehicleOwnersName(Vehicle vehicle) {
        /**
         * Method load all user names of the connected vehicle.
         */
        if(!vehicle.getOwnersUID().isEmpty()) {
            ArrayList<String> ownersUid = vehicle.getOwnersUID();
            myprofile_TV_vehicleOwners.setText("");
            Log.e("pttt", "numberOfOwners="+ownersUid.size());
            for (int i=0;i<ownersUid.size();i++) {
                int finalI = i;
                MyFireBaseServices.getInstance().loadUserFromFireBase(ownersUid.get(i), new MyFireBaseServices.CallBack_LoadUser() {
                    @Override
                    public void userDetailsUpdated(User result) {
                        if(result != null) {
                            userNames += ", "+result.getName();
                            if(finalI == ownersUid.size()-1) {
                                userNames = userNames.substring(2,userNames.length());
                                myprofile_TV_vehicleOwners.setText(userNames);
                                Log.e("pttt", "userNames="+userNames);
                                userNames="";
                            }
                        }
                    }
                    @Override
                    public void loadFailed(Exception e) {
                        Log.d("pttt", "loadFailed: "+e.getStackTrace());
                    }
                });
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

    @Override
    public void onResume() {
        isResumed=true;
        updateUserData(user);
        super.onResume();
    }

    @Override
    public void onPause() {
        isResumed=false;
        super.onPause();
    }
}

