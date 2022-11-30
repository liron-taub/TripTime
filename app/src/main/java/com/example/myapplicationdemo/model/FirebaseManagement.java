package com.example.myapplicationdemo.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.myapplicationdemo.R;
import com.example.myapplicationdemo.controller.GPSDetector;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FirebaseManagement {
    private static FirebaseManagement instance;
    private final FirebaseFirestore db;
    private final StorageReference storageRef;
    public final String LOCATIONS = "LOCATIONS";
    public final String REVIEWS = "REVIEWS";
    public final String NAME = "NAME";
    public final HashMap<String, List<Review>> locationsAndReviews;
    private ImageView imageView;
    public static GPSDetector detector;

    private FirebaseManagement() {
        this.locationsAndReviews = new HashMap<>();
        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        db.collection(LOCATIONS).addSnapshotListener((x, y) -> getAllLocations());
    }

    public static FirebaseManagement getInstance() {
        if (instance == null) {
            instance = new FirebaseManagement();
        }
        return instance;
    }

    public void saveReview(Review review) {
        String locationKey = review.latitude + "-" + review.longitude;

        db
                .collection(LOCATIONS)
                .document(locationKey)
                .collection(REVIEWS)
                .add(review);

        HashMap<String, Object> map = new HashMap<>();
        map.put(NAME, review.placeName);
        db
                .collection(LOCATIONS)
                .document(locationKey)
                .set(map);

        if (locationsAndReviews.containsKey(locationKey)) {
            locationsAndReviews.get(locationKey).add(review);
        } else {
            List<Review> reviews = new ArrayList<>();
            reviews.add(review);
            locationsAndReviews.put(locationKey, reviews);
        }
    }

    public void getAllLocations() {
        if (detector == null) return;

        db.collection(LOCATIONS).get().addOnSuccessListener(command ->
                command.forEach(queryDocumentSnapshot ->
                        queryDocumentSnapshot.getReference().collection(REVIEWS).get().addOnSuccessListener(task -> {
                            String[] latLang = queryDocumentSnapshot.getId().split("-");
                            double lang = Double.parseDouble(latLang[0]), lat = Double.parseDouble(latLang[1]);

                            String tag = lang + "-" + lat;
                            locationsAndReviews.put(tag, task.toObjects(Review.class));
                            detector.addMarker(new LatLng(lang, lat), tag, R.drawable.ic_round_location);
                        })))
                .addOnFailureListener(command -> System.out.println(""));
    }

    public void uploadImage(Uri filePath, String location, Context context) {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("מעלה מידע...");
            progressDialog.show();

            StorageReference ref = storageRef.child("images/" + location + "/" + UUID.randomUUID().toString());

            ref.putFile(filePath)
                    .addOnSuccessListener(
                            taskSnapshot -> {
                                progressDialog.dismiss();
                                if (imageView != null) {
                                    downloadImages(location, imageView, context, 0);
                                }
                            })

                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                    })

                    .addOnProgressListener(
                            taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("מעלה " + (int) progress + "%");
                            });
        }
    }

    public void downloadImages(String location, ImageView imageView, Context context, int index) {
        try {
            this.imageView = imageView;
            storageRef.child("images/" + location).listAll().addOnSuccessListener(refList -> {
                List<StorageReference> items = refList.getItems();
                if (items.size() > 0) {
                    int i = index % items.size();
                    items.get(i).getDownloadUrl().addOnSuccessListener(imageRef ->
                            Glide.with(context)
                                    .load(imageRef)
                                    .into(imageView));
                }
            });
        } catch (Exception ex) {
        }
    }

    public HashMap<String, List<Review>> search(HashMap<SearchCriteria, Object> criteria) {
        Stream<Map.Entry<String, List<Review>>> result = new HashMap<>(locationsAndReviews).entrySet().stream();

        for (SearchCriteria criterion : criteria.keySet()) {
            switch (criterion) {
                case OPEN_TEXT:
                    result = result.filter(
                            x -> x.getValue()
                                    .stream()
                                    .anyMatch(review -> review.placeName.contains((CharSequence) criteria.get(criterion))));
                    break;

                case TOILET:
                    result = result.filter(
                            x -> calculateBooleanAvg(x.getValue().stream()
                                    .map(review -> review.toilet).collect(Collectors.toList())) >= 50);
                    break;

                case ACCESSIBILITY:
                    result = result.filter(
                            x -> calculateBooleanAvg(x.getValue().stream()
                                    .map(review -> review.accessibility).collect(Collectors.toList())) >= 50);
                    break;

                case CAMPING:
                    result = result.filter(
                            x -> calculateBooleanAvg(x.getValue().stream()
                                    .map(review -> review.camping).collect(Collectors.toList())) >= 50);
                    break;

                case FIRE:
                    result = result.filter(
                            x -> calculateBooleanAvg(x.getValue().stream()
                                    .map(review -> review.fireAllowed).collect(Collectors.toList())) >= 50);
                    break;

                case CAFETERIA:
                    result = result.filter(
                            x -> calculateBooleanAvg(x.getValue().stream()
                                    .map(review -> review.cafeteria).collect(Collectors.toList())) >= 50);
                    break;

                case FOOD:
                    result = result.filter(
                            x -> calculateBooleanAvg(x.getValue().stream()
                                    .map(review -> review.vendingMachine).collect(Collectors.toList())) >= 50);
                    break;

                case SHOWER:
                    result = result.filter(
                            x -> calculateBooleanAvg(x.getValue().stream()
                                    .map(review -> review.shower).collect(Collectors.toList())) >= 50);
                    break;

                case CAR:
                    result = result.filter(
                            x -> calculateBooleanAvg(x.getValue().stream()
                                    .map(review -> !
                                            review.regularCar).collect(Collectors.toList())) >= 50);
                    break;
            }
        }

        return result.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (prev, next) -> next, HashMap::new));
    }

    private int calculateBooleanAvg(List<Boolean> booleans) {
        return (int) (100 * ((float) booleans.stream().filter(b -> b).count() / booleans.size()));
    }
}
