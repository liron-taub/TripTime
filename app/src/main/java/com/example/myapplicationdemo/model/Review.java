package com.example.myapplicationdemo.model;

import android.location.Location;

import java.io.Serializable;
import java.util.Date;

public class Review implements Serializable {
    public String placeName;
    public String writerName;
    public boolean toilet;
    public boolean shower;
    public int startAgeRange;
    public int endAgeRange;
    public boolean indoor;
    public boolean regularCar;
    public boolean accessibility;
    public boolean camping;
    public boolean fireAllowed;
    public String comment;
    public Date date;
    public boolean cafeteria;
    public boolean vendingMachine;
    public float rating;
    public double latitude;
    public double longitude;
    public int recommendedStay;
    public float satisfaction;
    public float cleanliness;

    public Review(String placeName, String writerName, boolean toilet, boolean shower, int startAgeRange, int endAgeRange, boolean indoor, boolean regularCar, boolean accessibility, boolean camping, boolean fireAllowed, String comment, Date date, boolean cafeteria, boolean vendingMachine, float rating, double latitude, double longitude, int recommendedStay, float satisfaction, float cleanliness) {
        this.placeName = placeName;
        this.writerName = writerName;
        this.toilet = toilet;
        this.shower = shower;
        this.startAgeRange = startAgeRange;
        this.endAgeRange = endAgeRange;
        this.indoor = indoor;
        this.regularCar = regularCar;
        this.accessibility = accessibility;
        this.camping = camping;
        this.fireAllowed = fireAllowed;
        this.comment = comment;
        this.date = date;
        this.cafeteria = cafeteria;
        this.vendingMachine = vendingMachine;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recommendedStay = recommendedStay;
        this.satisfaction = satisfaction;
        this.cleanliness = cleanliness;
    }

    public Review() {

    }
}
