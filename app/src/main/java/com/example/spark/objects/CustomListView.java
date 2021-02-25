package com.example.spark.objects;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.spark.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

public class CustomListView extends ArrayAdapter<String> {
    private String[] vehicleId;
    private String[] userName;
    private String[] time;
    private double[] latitude;
    private double[] longitude;
    private Context context;

    public CustomListView(Activity context, String[] vehicleId, String[] userName, String[] time, double[] latitude, double[] longitude) {
        super(context, R.layout.listview_layout,vehicleId);
        this.context = context;
        this.vehicleId = vehicleId;
        this.userName = userName;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        ViewHolder viewHolder;
        if(v==null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
            v = layoutInflater.inflate(R.layout.listview_layout,null,true);
            viewHolder = new ViewHolder(v);
        }
        else {
            viewHolder = (ViewHolder) v.getTag();
            if(viewHolder == null) {
                viewHolder = new ViewHolder(v);
            }
        }
        viewHolder.listview_Lbl_vehicleId.setText("Vehicle number:"+this.vehicleId[position]);
        viewHolder.listview_Lbl_userName.setText(""+this.userName[position]);
        viewHolder.listview_Lbl_time.setText(""+this.time[position]);
        viewHolder.listview_Lbl_latlng.setText("("+this.latitude[position]+","+this.longitude[position]+")");
        return v;
    }

    private class ViewHolder {
       private TextView listview_Lbl_vehicleId;
       private TextView listview_Lbl_userName;
       private TextView listview_Lbl_time;
       private TextView listview_Lbl_latlng;

        public ViewHolder(View v) {
            listview_Lbl_vehicleId = v.findViewById(R.id.listview_Lbl_vehicleId);
            listview_Lbl_userName = v.findViewById(R.id.listview_Lbl_userName);
            listview_Lbl_time = v.findViewById(R.id.listview_Lbl_time);
            listview_Lbl_latlng = v.findViewById(R.id.listview_Lbl_latlng);
        }
    }
}
