package com.example.spark.objects;

import android.content.Context;
import android.util.Log;

import com.example.spark.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.Distance;
import com.google.maps.model.Duration;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RouteBulider {

    private final int AVERAGE_WALKING_SPEED = 4;
    private LatLng origin;
    private LatLng destination;
    private Context appContext;
    private List<LatLng> path;
    private GeoApiContext geoApiContext;
    public RouteBulider() {
    }

    public RouteBulider(LatLng origin, LatLng destination,Context appContext) {
        this.origin = origin;
        this.destination = destination;
        this.appContext = appContext;
        this.path = new ArrayList();
        geoApiContext = new GeoApiContext.Builder()
                .apiKey(appContext.getString(R.string.api_key))
                .build();
    }

    private List<LatLng> calcRoute() {
        DirectionsApiRequest req = DirectionsApi.newRequest(geoApiContext)
                .origin(stringLatLng(origin))
                .destination(stringLatLng(destination))
                .mode(TravelMode.WALKING);

        Log.d("pttt", "calcRoute: Origin= "+String.valueOf(origin)+", Destination"+String.valueOf(destination));
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
            line.addAll(path).color(appContext.getColor(R.color.appBackgroundColor)).width(15);
        }
        return line;
    }

    private DirectionsResult getDirectionsResult(LatLng origin, LatLng destination) {
        DirectionsResult directionsResult = null;
        try {
            directionsResult = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.WALKING)
                    .origin(stringLatLng(origin))
                    .destination(stringLatLng(destination))
                    .await();
        } catch (ApiException e) {
            Log.d("pttt", "API Exception in getDirectionsResult: "+e.getStackTrace());
        } catch (InterruptedException e) {
            Log.d("pttt", "INTERRUPTED Exception in getDirectionsResult: "+e.getStackTrace());
        } catch (IOException e) {
            Log.d("pttt", "I/O Exception in getDirectionsResult: "+e.getStackTrace());
        }
        return directionsResult;
    }

    private double getRouteDistance(LatLng origin, LatLng destination) {
        DirectionsResult directionsResult = getDirectionsResult(origin,destination);
        Log.e("pttt", "getRouteDistance: "+directionsResult);
        // - Parse the result
        DirectionsRoute route = directionsResult.routes[0];
        DirectionsLeg leg = route.legs[0];
        Distance distance = leg.distance;
        return (double)distance.inMeters/1000;
    }

    public String routeEstimateTimeToString(LatLng origin, LatLng destination) {
        int walkingSpeed = AVERAGE_WALKING_SPEED;       //walking speed in KM
        double distance = getRouteDistance(origin,destination);
        double res = distance/walkingSpeed;
        return String.format("%.1f",res*10) + " mins";
    }

    public String routeDistanceToString(LatLng origin, LatLng destination) {
        double distance = getRouteDistance(origin,destination);
        return String.format("%.2f",distance)+" km";
    }

    private String stringLatLng (LatLng latLng) {
        return latLng.latitude+","+latLng.longitude;
    }

}
