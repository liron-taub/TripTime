package com.example.myapplicationdemo.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.myapplicationdemo.R;

public class LocationProfile extends AppCompatActivity {

    @Override
    // מקשרת בין הxml לבין הגאבה
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.copy);
    }
}