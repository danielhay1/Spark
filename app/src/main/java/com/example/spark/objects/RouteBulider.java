package com.example.spark.objects;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.directions.route.AbstractRouting;
import com.example.spark.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.util.ArrayList;
import java.util.List;

public class RouteBulider {
    private LatLng origin;
    private LatLng destination;
    private Context appContext;
    private List<LatLng> path;

    public RouteBulider() {
    }

    public RouteBulider(LatLng origin, LatLng destination,Context appContext) {
        this.origin = origin;
        this.destination = destination;
        this.appContext = appContext;
        this.path = new ArrayList();
    }

    private List<LatLng> calcRoute() {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(appContext.getString(R.string.api_key))
                .build();
        DirectionsApiRequest req = DirectionsApi.getDirections(context, stringLatLng(origin), stringLatLng(destination));
        try {
            DirectionsResult res = req.await();
            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];
                if (route.legs !=null) {
                    for(int i=0; i<route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j=0; j<leg.steps.length;j++){
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length >0) {
                                    for (int k=0; k<step.steps.length;k++){
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {
            Log.d("pttt", "drawRoute: "+e.getLocalizedMessage());
        }
        return path;
    }

    public PolylineOptions getPolylineRoute() {
        PolylineOptions line = new PolylineOptions();
        path = calcRoute();
        //Draw the polyline
        if (path.size() > 0) {
            line.addAll(path).color(appContext.getColor(R.color.teal_200)).width(15);
        }
        return line;
    }

    public void cancelRoute() {
    }

    private void removeRouteFromMap() {

    }

    private String stringLatLng (LatLng latLng) {
        return latLng.latitude+","+latLng.longitude;
    }

}
