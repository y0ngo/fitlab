package com.example.fitlab;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.PlacesStatusCodes;

import java.util.ArrayList;
import java.util.List;

public class FindGymActivity extends AppCompatActivity {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    private ListView gymListView;
    private ArrayList<String> gymList;
    private PlacesClient placesClient;
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_gym);

        gymListView = findViewById(R.id.gym_list_view);
        gymList = new ArrayList<>();

        // Initialize the Places API with your API key
        Places.initialize(getApplicationContext(), "AIzaSyBYOW97thHzt-wz72ncqioZO77KolQez2w");
        placesClient = Places.createClient(this);

        // Fetch nearby gyms
        fetchNearbyGyms();
    }

    private void fetchNearbyGyms() {
        // Set the starting location to Nottingham
        LatLng nottinghamLatLng = new LatLng(52.9548, -1.1581);

        // Create a request object
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery("gym")
                .setLocationBias(RectangularBounds.newInstance(nottinghamLatLng, nottinghamLatLng))
                .build();

        // Fetch the gyms near Nottingham
        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                gymList.add(prediction.getPrimaryText(null).toString() + "\n" + prediction.getSecondaryText(null).toString());
                Log.d("FindGymActivity", "Gym found: " + prediction.getPrimaryText(null).toString());
            }
            if (gymList.isEmpty()) {
                Toast.makeText(this, "No gyms found nearby", Toast.LENGTH_SHORT).show();
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gymList);
                gymListView.setAdapter(adapter);
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                if (apiException.getStatusCode() == PlacesStatusCodes.TIMEOUT && retryCount < MAX_RETRIES) {
                    retryCount++;
                    Log.e("FindGymActivity", "Location timeout, retrying... (" + retryCount + "/" + MAX_RETRIES + ")");
                    new Handler().postDelayed(this::fetchNearbyGyms, RETRY_DELAY_MS);
                } else {
                    Toast.makeText(this, "Failed to fetch gyms: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FindGymActivity", "Error fetching gyms", exception);
                }
            }
        });
    }
}