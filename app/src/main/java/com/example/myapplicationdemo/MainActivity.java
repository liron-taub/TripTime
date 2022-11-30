package com.example.myapplicationdemo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.myapplicationdemo.controller.GPSDetector;
import com.example.myapplicationdemo.model.FirebaseManagement;
import com.example.myapplicationdemo.model.Review;
import com.example.myapplicationdemo.model.SearchCriteria;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jakewharton.rxbinding4.widget.RxSearchView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map = null;
    SearchView searchView;
    GPSDetector detector;
    boolean permissionsGranted = false;
    FloatingActionButton myLocationButton;
    LinearLayout tagsBar;
    LinearLayout selectedTagsBar;

    List<String> tags = new ArrayList<>(Arrays.asList("שירותים", "נגישות לנכים", "קמפינג", "הדלקת אש", "קפיטריה", "מכונות אוכל", "מקלחת", "4X4"));
    List<String> selectedTags;
    HashMap<String, SearchCriteria> searchCriteriaHashMap;

    String queryText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        initTags();
        selectedTagsBar = findViewById(R.id.selected_tags_bar);
        tagsBar = findViewById(R.id.tags_bar);
        for (String tag : tags) {
            unselectTag(tag);
        }

        myLocationButton = findViewById(R.id.my_location_button);
        myLocationButton.setOnClickListener(v -> {
            if (detector != null && detector.currentLocation != null) {
                detector.moveAndZoom(detector.currentLocation);
            }
        });

        searchView = findViewById(R.id.search_view);
        RxSearchView.queryTextChanges(searchView)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(query -> {
                    queryText = query.toString();
                    startSearch();
                });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // מבקש הרשאות למיקום ואם אין אז מקפיץ חלון שמבקש הרשאות
    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 5);
        } else {
            permissionsGranted = true;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    // פונקציה שאם לא היה הרשאה אז מחכים להרשאה ומתחילים להאזין לשינויים של הגי פי אס
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 5) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                permissionsGranted = true;
                if (this.map != null) {
                    setupMap(this.map);
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Until you grant the permission, we cannot display the location", Toast.LENGTH_SHORT).show();
        }
    }

    // אתחול של הGPS וזיהוי המיקום
    private void setupMap(@NonNull GoogleMap googleMap) {
        LatLng location = new LatLng(32.46508836969692, 35.30493286338767);
        map.moveCamera(CameraUpdateFactory.newLatLng(location));
        map.animateCamera(CameraUpdateFactory.zoomTo(8), 2000, null);
        detector = new GPSDetector(googleMap, this);
        FirebaseManagement.detector = detector;
        FirebaseManagement.getInstance().getAllLocations();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        this.map.setTrafficEnabled(true);
        if (permissionsGranted) {
            setupMap(this.map);
        }
    }

    private void unselectTag(String tag) {
        if (selectedTagsBar.findViewWithTag(tag) != null) {
            selectedTagsBar.removeView(selectedTagsBar.findViewWithTag(tag));
            selectedTags.remove(tag);
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }

        Chip chip = new Chip(this);
        chip.setText(tag);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(4, 0, 4, 0);
        chip.setLayoutParams(params);
        chip.setOnClickListener(v -> selectTag(tag));
        chip.setTag(tag);
        tagsBar.addView(chip);

        startSearch();
    }

    private void selectTag(String tag) {
        if (tagsBar.findViewWithTag(tag) != null) {
            tagsBar.removeView(tagsBar.findViewWithTag(tag));
            tags.remove(tag);
        }
        if (!selectedTags.contains(tag)) {
            selectedTags.add(tag);
        }

        Chip chip = new Chip(this);
        chip.setText(tag);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(4, 0, 4, 0);
        chip.setLayoutParams(params);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> unselectTag(tag));
        chip.setTag(tag);
        selectedTagsBar.addView(chip);

        startSearch();
    }

    private void initTags() {
        searchCriteriaHashMap = new HashMap<>();
        searchCriteriaHashMap.put("שירותים", SearchCriteria.TOILET);
        searchCriteriaHashMap.put("נגישות לנכים", SearchCriteria.ACCESSIBILITY);
        searchCriteriaHashMap.put("קמפינג", SearchCriteria.CAMPING);
        searchCriteriaHashMap.put("הדלקת אש", SearchCriteria.FIRE);
        searchCriteriaHashMap.put("קפיטריה", SearchCriteria.CAFETERIA);
        searchCriteriaHashMap.put("מכונות אוכל", SearchCriteria.FOOD);
        searchCriteriaHashMap.put("מקלחת", SearchCriteria.SHOWER);
        searchCriteriaHashMap.put("4X4", SearchCriteria.CAR);

        tags = new ArrayList<>(searchCriteriaHashMap.keySet());
        selectedTags = new ArrayList<>();
    }

    private void startSearch() {
        if (detector == null) return;

        HashMap<SearchCriteria, Object> searchCriteria = new HashMap<>();

        if (!queryText.equals("")) {
            searchCriteria.put(SearchCriteria.OPEN_TEXT, queryText);
        }

        for (String tag : selectedTags) {
            searchCriteria.put(searchCriteriaHashMap.get(tag), true);
        }

        if (searchCriteria.keySet().size() > 0) {
            HashMap<String, List<Review>> searchResults = FirebaseManagement.getInstance().search(searchCriteria);
            detector.removeAllMarkers();
            for (String key : searchResults.keySet()) {
                String[] latLang = key.split("-");
                detector.addMarker(new LatLng(Double.parseDouble(latLang[0]), Double.parseDouble(latLang[1])), key, R.drawable.ic_round_location);
            }
        } else {
            detector.removeAllMarkers();
            FirebaseManagement.getInstance().getAllLocations();
        }
    }
}