package com.example.myapplicationdemo.controller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.myapplicationdemo.LocationView;
import com.example.myapplicationdemo.R;
import com.example.myapplicationdemo.model.FirebaseManagement;
import com.example.myapplicationdemo.view.ReviewEditor;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// מטרת המחלקה למצוא את המיקום האוטומטי של איפה שאנחנו נמצאים
public class GPSDetector implements LocationListener, GoogleMap.OnMapLongClickListener {

    // Acquire a reference to the system Location Manager
    LocationManager locationManager;

    private final GoogleMap map;
    private final Context context;
    Marker currentLocationMarker;
    List<Marker> markers = new ArrayList<>();
    public LatLng currentLocation;
    public boolean firstZoom;

    public GPSDetector(GoogleMap map, Context context) {
        this.map = map;
        this.context = context;
        this.firstZoom = true;

        this.map.setOnMapLongClickListener(this);

        // צריך לקשר מסך חדש
        map.setOnInfoWindowClickListener(marker -> openLocationScreen(marker.getTag().toString()));

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);// פונקציה של אדרואיד שיודעת לזהות את המיקום האוטומטי לוקח את זה מתוך הקונטקסט
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);// מתחיל להאזין לשינויים של הגי פי אס במידה וקיימת הרשאה
    }

    public void removeAllMarkers() {
        map.clear();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (this.currentLocationMarker != null) {
            this.currentLocationMarker.remove();
        }
        addMarker(currentLocation);

        if (firstZoom) {
            moveAndZoom(currentLocation);
        }
        firstZoom = false;
    }

    public void addMarker(LatLng latLng) {
        addMarker(latLng, latLng.latitude + "-" + latLng.longitude);
    }

    public void addMarker(LatLng latLng, String markerTag) {
        try {
            Address address = new Geocoder(context).getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
            latLng = new LatLng(address.getLatitude(), address.getLongitude());

            Location temp = new Location(LocationManager.GPS_PROVIDER);
            temp.setLatitude(address.getLatitude());
            temp.setLongitude(address.getLongitude());

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_icon))
                    .title(address.getAddressLine(0))
                    .snippet("לחץ על מנת להוסיף מידע חדש"));
            marker.setTag(markerTag);
            this.markers.add(marker);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(context, "לזיהוי מיקום אוטמטי הפעל GPS", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapLongClick(@NonNull LatLng point) {
        addMarker(point);
    }

    public void moveAndZoom(LatLng location) {
        map.moveCamera(CameraUpdateFactory.newLatLng(location));
        map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
    }

    private void openLocationScreen(String latLng) {
        Class<?> classType = ReviewEditor.class;

        if (FirebaseManagement.getInstance().locationsAndReviews.keySet().contains(latLng)) {
            classType = LocationView.class;
        }

        Intent switchActivityIntent = new Intent(context, classType);
        switchActivityIntent.putExtra("lat_lang", latLng);
        context.startActivity(switchActivityIntent);
    }
}
