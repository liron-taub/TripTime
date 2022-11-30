package com.example.myapplicationdemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.myapplicationdemo.controller.CommentsAdapter;
import com.example.myapplicationdemo.model.FirebaseManagement;
import com.example.myapplicationdemo.model.Review;
import com.example.myapplicationdemo.view.ReviewEditor;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ramijemli.percentagechartview.PercentageChartView;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class LocationView extends AppCompatActivity {

    private int currentImageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_view);

        getSupportActionBar().hide();

        // get the list of all the reviews
        String latLang = getIntent().getExtras().getString("lat_lang");
        List<Review> reviews = FirebaseManagement.getInstance().locationsAndReviews.get(latLang);
        reviews.sort((r1, r2) -> r1.date.compareTo(r2.date) * -1);

        ImageView imageView = findViewById(R.id.imageView);
        FirebaseManagement.getInstance().downloadImages(latLang, imageView, this, currentImageIndex);

        ImageButton nextImageButton = findViewById(R.id.next_image_button);
        nextImageButton.setOnClickListener(v -> {
            currentImageIndex++;
            FirebaseManagement.getInstance().downloadImages(latLang, imageView, this, currentImageIndex);
        });

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        });

        ImageButton newReview = findViewById(R.id.create_review);
        newReview.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), ReviewEditor.class);
            i.putExtra("lat_lang", latLang);
            startActivity(i);
        });

        // show the of the location
        TextView name = findViewById(R.id.name);
        name.setText(reviews.get(0).placeName);

        TextView recommendedStay = findViewById(R.id.recommended_stay_textview);
        recommendedStay.setText((int) avg(reviews.stream().map(review -> (float) review.recommendedStay).collect(Collectors.toList())) + " שעות");

        TextView ageRange = findViewById(R.id.age_range_textview);
        int ageRangeStart = (int) avg(reviews.stream().map(review -> (float) review.startAgeRange).collect(Collectors.toList()));
        int ageRangeEnd = (int) avg(reviews.stream().map(review -> (float) review.endAgeRange).collect(Collectors.toList()));
        ageRange.setText(ageRangeEnd + " - " + ageRangeStart);

        findViewById(R.id.service_button).setOnClickListener(v -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_qustions, null);
            dialogBuilder.setView(dialogView);

            int value = calculateBooleanAvg(reviews.stream().map(review -> review.toilet).collect(Collectors.toList()));
            ((TextView) dialogView.findViewById(R.id.question1_text)).setText(value + "% סימנו שיש שירותים");
            ((ProgressBar) dialogView.findViewById(R.id.question1_value)).setProgress(value);

            value = calculateBooleanAvg(reviews.stream().map(review -> review.shower).collect(Collectors.toList()));
            ((TextView) dialogView.findViewById(R.id.question2_text)).setText(value + "% סימנו שיש מקלחת");
            ((ProgressBar) dialogView.findViewById(R.id.question2_value)).setProgress(value);

            value = (int) avg(reviews.stream().map(review -> review.cleanliness).collect(Collectors.toList()));
            ((TextView) dialogView.findViewById(R.id.question3_text)).setText("רמת שביעות רצון כללית: " + value + "%");
            ((ProgressBar) dialogView.findViewById(R.id.question3_value)).setProgress(value);

            value = (int) avg(reviews.stream().map(review -> review.satisfaction).collect(Collectors.toList()));
            View view = dialogView.findViewById(R.id.question4_text);
            view.setVisibility(View.VISIBLE);
            ((TextView) view).setText("רמת שביעות רצון מהניקיון: " + value + "%");
            view = dialogView.findViewById(R.id.question4_value);
            view.setVisibility(View.VISIBLE);
            ((ProgressBar) view).setProgress(value);

            AlertDialog dialog = dialogBuilder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        });

        findViewById(R.id.luxury_button).setOnClickListener(v -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_qustions, null);
            dialogBuilder.setView(dialogView);

            int value = calculateBooleanAvg(reviews.stream().map(review -> review.camping).collect(Collectors.toList()));
            ((TextView) dialogView.findViewById(R.id.question1_text)).setText(value + "% סימנו שניתן לעשות קמפינג");
            ((ProgressBar) dialogView.findViewById(R.id.question1_value)).setProgress(value);

            value = calculateBooleanAvg(reviews.stream().map(review -> review.fireAllowed).collect(Collectors.toList()));
            ((TextView) dialogView.findViewById(R.id.question2_text)).setText(value + "% סימנו שמותר להדליק אש");
            ((ProgressBar) dialogView.findViewById(R.id.question2_value)).setProgress(value);

            value = calculateBooleanAvg(reviews.stream().map(review -> review.cafeteria).collect(Collectors.toList()));
            ((TextView) dialogView.findViewById(R.id.question3_text)).setText(value + "% סימנו שיש קפיטריה");
            ((ProgressBar) dialogView.findViewById(R.id.question3_value)).setProgress(value);

            value = calculateBooleanAvg(reviews.stream().map(review -> review.vendingMachine).collect(Collectors.toList()));
            View view = dialogView.findViewById(R.id.question4_text);
            view.setVisibility(View.VISIBLE);
            ((TextView) view).setText(value + "% סימנו שיש מכונות אוכל");
            view = dialogView.findViewById(R.id.question4_value);
            view.setVisibility(View.VISIBLE);
            ((ProgressBar) view).setProgress(value);

            AlertDialog dialog = dialogBuilder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        });

        findViewById(R.id.accessibility_button).setOnClickListener(v -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_qustions, null);
            dialogBuilder.setView(dialogView);

            int value = calculateBooleanAvg(reviews.stream().map(review -> review.indoor).collect(Collectors.toList()));
            ((TextView) dialogView.findViewById(R.id.question1_text)).setText(value + "% סימנו שהמקום מקורה");
            ((ProgressBar) dialogView.findViewById(R.id.question1_value)).setProgress(value);

            value = calculateBooleanAvg(reviews.stream().map(review -> review.regularCar).collect(Collectors.toList()));
            ((TextView) dialogView.findViewById(R.id.question2_text)).setText(value + "% סימנו שמתאים לרכב רגיל");
            ((ProgressBar) dialogView.findViewById(R.id.question2_value)).setProgress(value);

            value = calculateBooleanAvg(reviews.stream().map(review -> review.accessibility).collect(Collectors.toList()));
            ((TextView) dialogView.findViewById(R.id.question3_text)).setText(value + "% סימנו שהמקום נגיש לנכים");
            ((ProgressBar) dialogView.findViewById(R.id.question3_value)).setProgress(value);

            AlertDialog dialog = dialogBuilder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        });

        findViewById(R.id.comments).setOnClickListener(v -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_comments, null);
            dialogBuilder.setView(dialogView);

            RecyclerView commentsView = dialogView.findViewById(R.id.comments_recycler_view);
            commentsView.setLayoutManager(new LinearLayoutManager(this));
            List<Review> reviewsWithComment = reviews.stream().filter(review -> review.comment != null && review.comment.length() > 0).collect(Collectors.toList());
            CommentsAdapter adapter = new CommentsAdapter(reviewsWithComment);
            commentsView.setAdapter(adapter);

            AlertDialog dialog = dialogBuilder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        });

        // show the rating
        RatingBar ratingBar = findViewById(R.id.rating_bar);
        ratingBar.setRating(avg(reviews.stream().map(review -> review.rating).collect(Collectors.toList())));
    }

    private int calculateBooleanAvg(List<Boolean> booleans) {
        return (int) (100 * ((float) booleans.stream().filter(b -> b).count() / booleans.size()));
    }

    private float avg(List<Float> integers) {
        double sum = 0;
        for (float i : integers) {
            sum += i;
        }
        return (float) (sum / integers.size());
    }
}