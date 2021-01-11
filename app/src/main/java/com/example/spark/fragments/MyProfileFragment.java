package com.example.spark.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.spark.R;
import com.example.spark.activiities.MainActivity;
import com.example.spark.activiities.SplashActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MyProfileFragment extends Fragment {

    private Button myprofile_BTN_logout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        findViews(view);
        initViews();
        return view;
    }

    private void findViews(View view) {
        myprofile_BTN_logout = view.findViewById(R.id.myprofile_BTN_logout);
    }

    private void initViews() {
        myprofile_BTN_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireBaseLogout();
            }
        });
    }


    private void fireBaseLogout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getActivity(), SplashActivity.class));
        getActivity().finish();
    }

}

