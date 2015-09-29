package edu.umich.si.inteco.minuku.util;

import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.R;

/**
 * Created by Armuro on 7/3/14.
 */
public class VisualizationManager {


    public static float GOOGLE_MAP_DEFAULT_ZOOM_LEVEL = 13;
    public static LatLng GOOGLE_MAP_DEFAULT_CAMERA_CENTER = new LatLng(42.279469, -83.740973);

    public static void showMapWithPaths(GoogleMap map, ArrayList<LatLng> points, LatLng cameraCenter) {

        //map option
        GoogleMapOptions options = new GoogleMapOptions();
        options.tiltGesturesEnabled(false);
        options.rotateGesturesEnabled(false);

        //center the map
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraCenter, 13));

        // Marker start = map.addMarker(new MarkerOptions().position(startLatLng));



        //draw linges between points and add end and start points
        PolylineOptions pathPolyLineOption = new PolylineOptions().color(Color.RED).geodesic(true);
        pathPolyLineOption.addAll(points);

        //draw lines
        Polyline path = map.addPolyline(pathPolyLineOption);
    }

    public static void showMapWithPathsAndCurLocation(GoogleMap map, ArrayList<LatLng> points, LatLng curLoc) {


        map.clear();

        //map option
        GoogleMapOptions options = new GoogleMapOptions();
        options.tiltGesturesEnabled(false);
        options.rotateGesturesEnabled(false);

        //get current zoom level
        float zoomlevel = map.getCameraPosition().zoom;

        //center the map
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, zoomlevel));

        // Marker start = map.addMarker(new MarkerOptions().position(startLatLng));
        Marker me =  map.addMarker(new MarkerOptions()
                        .position(curLoc)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation))

        );


        //draw linges between points and add end and start points
        PolylineOptions pathPolyLineOption = new PolylineOptions().color(Color.RED).geodesic(true);
        pathPolyLineOption.addAll(points);

        //draw lines
        Polyline path = map.addPolyline(pathPolyLineOption);


    }


}
