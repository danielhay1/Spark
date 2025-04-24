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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.spark.R;
import com.example.spark.activities.MainActivity;
import com.example.spark.objects.CustomListView;
import com.example.spark.objects.Parking;
import com.example.spark.objects.User;
import com.example.spark.untils.MyFireBaseServices;
import com.google.gson.Gson;

import java.util.ArrayList;

public class ParkingHistoryFragment extends Fragment {

    private final int MAX_ROWS = 20;

    private User user;
    private TextView parkinghistoy_TV_title;
    private ListView parkinghistoy_LV_parking;
    private Parking[] parkings;
    private CustomListView customListViewAdapter;
    private SendLatLng sendLatLngCallBack;

    public interface SendLatLng {
        public void sendLatLng(Parking parking);
    }
    public void setCallBack(SendLatLng sendLatLngCallBack) {
        this.sendLatLngCallBack = sendLatLngCallBack;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parking_history, container, false);
        Log.e("pttt", "(ONCRATE: PARKINGHISTORY)");
        findViews(view);
        this.getUser();
        return view;
    }

    private void findViews(View view) {
        Log.e("pttt", "(PARKINGHISTORY- findViews)");
        parkinghistoy_TV_title = view.findViewById(R.id.parkinghistoy_TV_title);
        parkinghistoy_LV_parking = view.findViewById(R.id.parkinghistoy_LV_parking);
        Log.e("pttt", "(parkinghistoy_LV_parking="+parkinghistoy_LV_parking);
    }

    private void InitViews(){
        parkinghistoy_LV_parking.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(parkings[position] != null && sendLatLngCallBack!=null) {
                    sendLatLngCallBack.sendLatLng(parkings[position]);  //Send parking to add maker on map fragment.
                }
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
                    getParkingHistory();
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
                    getParkingHistory();
                }
            }
            @Override
            public void loadFailed(Exception e) {
                Log.d("pttt", "loadFailed: Failed to read value "+e.getMessage());
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int userRequest = MainActivity.UPDATE_USER;
        Log.d("pttt", "onActivityResult (ParkingHistoryFragment): request= "+requestCode +", result= "+resultCode);
        if (requestCode == userRequest) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra(MainActivity.USER_INTENT);
                Gson gson = new Gson();
                this.user = gson.fromJson(result,User.class);
                Log.d("pttt", "onActivityResult (ParkingHistoryFragment): user updated, user ="+user);
                //ADD:
                getParkingHistory();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("pttt", "onActivityResult (ParkingHistoryFragment): user isn't updated.");
            }
        }
    }

    public void getParkingHistory() {
        Log.d("pttt", "getParkingHistory: (for vehicle): "+this.user.getConnectedVehicleID());
        if(parkinghistoy_LV_parking==null) {
            Log.e("pttt", "parkinghistoy_LV_parking: null");
        } else {
            parkinghistoy_LV_parking.setAdapter(null);
        }
        customListViewAdapter = null;
        MyFireBaseServices.getInstance().loadParkingHistoryFromFireBase(user.getConnectedVehicleID(), MAX_ROWS, new MyFireBaseServices.CallBack_LoadParking() {
            @Override
            public void parkingLocationUpdated(Parking parking) { }
            @Override
            public void loadParkingHistory(ArrayList<Parking> result) {
                Log.d("pttt", "loadParkingHistory: PARKINGS "+result);
                if(result!=null) {
                    if(!result.isEmpty()){
                        parkings = (Parking[]) result.toArray(new Parking[result.size()]);
                        initCustomListView(parkings);
                    }
                }
            }

            @Override
            public void loadFailed(Exception e) {
                Log.d("pttt", "loadFailed: "+e.getStackTrace());
            }
        });
    }

    private void initCustomListView(Parking[] parkings) {
        ArrayList<Integer> indexs = new ArrayList<Integer>();
        int parkingCounter = parkings.length;
        String vehicleIds[] = new String[parkingCounter];
        String userNames[] = new String[parkingCounter];
        String times[] = new String[parkingCounter];
        double latitudes[] = new double[parkingCounter];
        double longitudes[] = new double[parkingCounter];

        for (int i=0; i < parkingCounter; i++) {
            vehicleIds[i] = parkings[i].getVehicleId();
            times[i] = parkings[i].getTime();
            latitudes[i] = parkings[i].getLatitude();
            longitudes[i] = parkings[i].getLongitude();
            if(this.user.getUid().equals(parkings[i].getUid())) {
                userNames[i] = this.user.getName();
            } else {
                indexs.add(i);
            }
        }
        if(indexs.isEmpty()) {
            customListViewAdapter = new CustomListView(getActivity(),vehicleIds,userNames,times);
            parkinghistoy_LV_parking.setAdapter(customListViewAdapter);
            InitViews();
        } else {
            for (int index:indexs) {
                MyFireBaseServices.getInstance().loadUserFromFireBase(parkings[index].getUid(), new MyFireBaseServices.CallBack_LoadUser() {
                    @Override
                    public void userDetailsUpdated(User result) {
                        userNames[index] = result.getName();
                        if(index == indexs.size()-1) {    //last element.
                            CustomListView customListView = new CustomListView(getActivity(),vehicleIds,userNames,times);
                            parkinghistoy_LV_parking.setAdapter(customListView);
                            InitViews();
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d("pttt", "onResume: (ParkingHistoryFragment)");
    }

}