package com.example.spark.objects;

import android.content.Context;
import android.util.Log;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.spark.R;
import com.example.spark.untils.MySignal;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RouteBuilder2 implements RoutingListener{
    private LatLng origin;
    private LatLng destination;
    private Context appContext;
    private List<LatLng> path;
    private GoogleMap googleMap;
    private ArrayList<Polyline> polylines;

    public RouteBuilder2(LatLng origin, LatLng destination, Context appContext, GoogleMap googleMap) {
        this.origin = origin;
        this.destination = destination;
        this.appContext = appContext;
        this.googleMap = googleMap;
    }

    public void buildRoute() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.WALKING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(origin, destination)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Log.d("pttt", "onRoutingFailure: ");
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(origin);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(17);

        googleMap.moveCamera(center);
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }
        polylines = new ArrayList<Polyline>();
        // Add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(appContext.getColor(R.color.purple_500));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = googleMap.addPolyline(polyOptions);
            polylines.add(polyline);
            MySignal.getInstance().toast("Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue());
        }
    }

    @Override
    public void onRoutingCancelled() {
        Log.d("pttt", "onRoutingCancelled: ");
    }

    public void drawRoute() {

    }

    public void removeRoute() {

    }
}
