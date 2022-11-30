package com.example.myapplicationdemo.controller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplicationdemo.LocationView;
import com.example.myapplicationdemo.R;
import com.example.myapplicationdemo.model.FirebaseManagement;
import com.example.myapplicationdemo.view.ReviewEditor;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// מטרת המחלקה למצוא את המיקום האוטומטי של איפה שאנחנו נמצאים
public class GPSDetector implements LocationListener, GoogleMap.OnMapClickListener {
    LocationManager locationManager;

    private final GoogleMap map;
    private final Context context;
    Marker currentLocationMarker, lastClickMarker;
    public LatLng currentLocation;
    public boolean firstZoom;

    public GPSDetector(GoogleMap map, Context context) {
        this.map = map;
        this.context = context;
        this.firstZoom = true;

        this.map.setOnMapClickListener(this);
        this.map.setOnInfoWindowClickListener(marker -> openLocationScreen(marker.getTag().toString()));

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
        this.currentLocationMarker = addMarker(currentLocation, R.drawable.ic_round_my_location);

        if (firstZoom) {
            moveAndZoom(currentLocation);
        }
        firstZoom = false;
    }

    public Marker addMarker(LatLng latLng, int locationType) {
        return addMarker(latLng, latLng.latitude + "-" + latLng.longitude, locationType);
    }

    public Marker addMarker(LatLng latLng, String markerTag, int locationType) {
        try {
            String placeName = "מקום לא ידוע";
            if (FirebaseManagement.getInstance().locationsAndReviews.containsKey(markerTag)) {
                placeName = FirebaseManagement.getInstance().locationsAndReviews.get(markerTag).get(0).placeName;
            } else {
                List<Address> addresses = new Geocoder(context).getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses.size() > 0) {
                    placeName = addresses.get(0).getAddressLine(0);
                }
            }

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(bitmapDescriptorFromVector(context, locationType))
                    .title(placeName)
                    .snippet("לחץ על מנת להוסיף מידע חדש"));
            marker.setTag(markerTag);
            return marker;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(context, "לזיהוי מיקום אוטמטי הפעל GPS", Toast.LENGTH_LONG).show();
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

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        if (this.lastClickMarker != null) {
            this.lastClickMarker.remove();
        }
        this.lastClickMarker = addMarker(latLng, R.drawable.ic_round_new_location);
        this.lastClickMarker.showInfoWindow();
    }
}
