package com.example.fitlab;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.PlacesStatusCodes;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class FindGymActivity extends AppCompatActivity {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private ListView gymListView;
    private ArrayList<String> gymList;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_gym);

        gymListView = findViewById(R.id.gym_list_view);
        gymList = new ArrayList<>();

        // Initialize the Places API with your API key
        Places.initialize(getApplicationContext(), "AIzaSyDJUI_2IL5Calbb44Pw4_7EsFsqEDI2f5M");
        placesClient = Places.createClient(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(FindGymActivity.this, "Failed to get current location", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    fetchNearbyGyms(currentLatLng);
                }
            }
        };

        // Fetch nearby gyms
        fetchCurrentLocation();
        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);

        FloatingActionButton fab = findViewById(R.id.add_workout_fab);
        fab.setOnClickListener(view -> {
            // Handle FAB click
            Toast.makeText(FindGymActivity.this, "FAB clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Location permission is needed to show nearby gyms", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestLocationUpdates();
        }
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void fetchNearbyGyms(LatLng currentLatLng) {
        double lat = currentLatLng.latitude;
        double lng = currentLatLng.longitude;
        double radius = 0.05; // Approx. 5km radius

        LatLng southwest = new LatLng(lat - radius, lng - radius);
        LatLng northeast = new LatLng(lat + radius, lng + radius);

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery("gym")
                .setLocationBias(RectangularBounds.newInstance(southwest, northeast))
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            gymList.clear();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                gymList.add(prediction.getPrimaryText(null).toString() + "\n" + prediction.getSecondaryText(null).toString());
                Log.d("FindGymActivity", "Gym found: " + prediction.getPrimaryText(null).toString());
            }
            if (gymList.isEmpty()) {
                Toast.makeText(this, "No gyms found nearby", Toast.LENGTH_SHORT).show();
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.gym_list_item, R.id.gym_name, gymList);
                gymListView.setAdapter(adapter);
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                if (apiException.getStatusCode() == PlacesStatusCodes.TIMEOUT && retryCount < MAX_RETRIES) {
                    retryCount++;
                    Log.e("FindGymActivity", "Location timeout, retrying... (" + retryCount + "/" + MAX_RETRIES + ")");
                    new Handler().postDelayed(() -> fetchNearbyGyms(currentLatLng), RETRY_DELAY_MS);
                } else {
                    Toast.makeText(this, "Failed to fetch gyms: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FindGymActivity", "Error fetching gyms", exception);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}