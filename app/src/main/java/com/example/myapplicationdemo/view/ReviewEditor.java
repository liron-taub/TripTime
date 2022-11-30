package com.example.myapplicationdemo.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplicationdemo.LocationView;
import com.example.myapplicationdemo.R;
import com.example.myapplicationdemo.model.FirebaseManagement;
import com.example.myapplicationdemo.model.Review;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewEditor extends AppCompatActivity {
    private ActivityResultLauncher<Intent> selectImageResultLauncher;
    String[] carOptions = {"4X4", "רכב רגיל"};
    private Uri filePath;

    TextView nameOfPlace, firstAndLastName, recommendedStay, comment;
    AutoCompleteTextView suitableVehicleType;
    AppCompatCheckBox indoor, isThereAShower, isThereAToilet, accessForDisabled, camping, fire,
            cafeteria, vendingMachine;
    RangeSlider ageRange;
    Slider satisfaction, cleanliness;
    RatingBar rating;
    Button upload;
    ImageView selectedImage;
    String lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reviewaditor);

        String[] latLang = getIntent().getExtras().getString("lat_lang").split("-");
        lat = latLang[0];
        lng = latLang[1];

        selectedImage = findViewById(R.id.selected_image_view);
        selectImageResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        filePath = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                            selectedImage.setImageBitmap(bitmap);
                            selectedImage.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Button selectImage = findViewById(R.id.choose_image_btn);
        selectImage.setOnClickListener(v -> selectImage());


        //Creating the instance of ArrayAdapter containing list of fruit names
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, carOptions);
        //Getting the instance of AutoCompleteTextView
        suitableVehicleType = findViewById(R.id.suitable_vehicle_type);
        suitableVehicleType.setThreshold(1);//will start working from first character
        suitableVehicleType.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        suitableVehicleType.setOnTouchListener((v, event) -> {
            ((AutoCompleteTextView) v).showDropDown();
            return v.performClick();
        });

        ageRange = findViewById(R.id.age_range);
        ageRange.setStepSize(1);

        nameOfPlace = findViewById(R.id.name_of_place);
        firstAndLastName = findViewById(R.id.first_and_last_name);
        recommendedStay = findViewById(R.id.recommended_stay);
        comment = findViewById(R.id.comment);
        indoor = findViewById(R.id.indoor);
        isThereAShower = findViewById(R.id.is_there_a_shower);
        isThereAToilet = findViewById(R.id.is_there_a_toilet);
        accessForDisabled = findViewById(R.id.access_for_disabled);
        camping = findViewById(R.id.camping);
        fire = findViewById(R.id.fire);
        cafeteria = findViewById(R.id.cafeteria);
        vendingMachine = findViewById(R.id.vendingMachine);
        satisfaction = findViewById(R.id.satisfaction);
        cleanliness = findViewById(R.id.cleanliness);
        rating = findViewById(R.id.rating);
        upload = findViewById(R.id.upload);
        upload.setOnClickListener(v -> upload());
    }

    private void upload() {
        TextView[] textViews = new TextView[]{nameOfPlace, firstAndLastName, recommendedStay};
        List<String> texts = Arrays.stream(textViews).map(tv -> tv.getText().toString()).collect(Collectors.toList());
        texts.add(filePath != null ? filePath.getScheme() : "");
        for (String str : texts) {
            if (str.equals("")) {
                Toast.makeText(this, "אחד או יותר מהשדות ריקים, נא למלא את כל השדות", Toast.LENGTH_LONG).show();
                return;
            }
        }

        Review review = new Review(
                nameOfPlace.getText().toString(),
                firstAndLastName.getText().toString(),
                isThereAToilet.isChecked(),
                isThereAShower.isChecked(),
                ageRange.getValues().get(0).intValue(),
                ageRange.getValues().get(1).intValue(),
                indoor.isChecked(),
                suitableVehicleType.getText().toString().equals(carOptions[1]),
                accessForDisabled.isChecked(),
                camping.isChecked(),
                fire.isChecked(),
                comment.getText().toString(),
                new Date(),
                cafeteria.isChecked(),
                vendingMachine.isChecked(),
                rating.getRating(),
                Double.parseDouble(lat),
                Double.parseDouble(lng),
                Integer.parseInt(recommendedStay.getText().toString()),
                satisfaction.getValue(),
                cleanliness.getValue());

        FirebaseManagement.getInstance().saveReview(review);
        FirebaseManagement.getInstance().uploadImage(filePath, lat + "-" + lng, this);
        Intent switchActivityIntent = new Intent(this, LocationView.class);
        switchActivityIntent.putExtra("lat_lang", lat + "-" + lng);
        this.startActivity(switchActivityIntent);
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        selectImageResultLauncher.launch(intent);
    }
}