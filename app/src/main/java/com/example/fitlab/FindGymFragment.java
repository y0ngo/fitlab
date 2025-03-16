package com.example.fitlab;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.PlacesStatusCodes;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FindGymFragment extends Fragment implements OnMapReadyCallback {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private RecyclerView gymListView;
    private ArrayList<String> gymList;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GoogleMap mMap;
    private int retryCount = 0;
    private TextInputEditText locationInput;
    private MaterialButton searchButton, useCurrentLocationButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_gym, container, false);

        gymListView = view.findViewById(R.id.gym_list_view);
        gymListView.setLayoutManager(new LinearLayoutManager(getContext()));
        gymList = new ArrayList<>();
        locationInput = view.findViewById(R.id.location_input);
        searchButton = view.findViewById(R.id.search_button);
        useCurrentLocationButton = view.findViewById(R.id.use_current_location_button);

        // Initialize the Places API with your API key
        Places.initialize(requireContext(), "AIzaSyDJUI_2IL5Calbb44Pw4_7EsFsqEDI2f5M");
        placesClient = Places.createClient(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Set up location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(requireContext(), "Failed to get current location", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    fetchNearbyGyms(currentLatLng);
                    if (mMap != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    }
                }
            }
        };

        // Set up button listeners
        useCurrentLocationButton.setOnClickListener(v -> fetchCurrentLocation());
        searchButton.setOnClickListener(v -> {
            String location = locationInput.getText().toString();
            if (!location.isEmpty()) {
                fetchLocationFromInput(location);
            } else {
                Toast.makeText(requireContext(), "Please enter a location or zip code", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    private void fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(requireContext(), "Location permission is needed to show nearby gyms", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestLocationUpdates();
        }
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void fetchLocationFromInput(String location) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                fetchNearbyGyms(latLng);
                if (mMap != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            } else {
                Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error fetching location", Toast.LENGTH_SHORT).show();
            Log.e("FindGymFragment", "Error fetching location", e);
        }
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
            if (!isAdded()) {
                return;
            }
            gymList.clear();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                String placeId = prediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener((fetchPlaceResponse) -> {
                    Place place = fetchPlaceResponse.getPlace();
                    gymList.add(place.getName() + "\n" + prediction.getSecondaryText(null).toString());
                    Log.d("FindGymFragment", "Gym found: " + place.getName());
                    if (mMap != null) {
                        LatLng gymLatLng = place.getLatLng();
                        if (gymLatLng != null) {
                            mMap.addMarker(new MarkerOptions()
                                    .position(gymLatLng)
                                    .title(place.getName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                    }
                    if (gymList.isEmpty()) {
                        Toast.makeText(requireContext(), "No gyms found nearby", Toast.LENGTH_SHORT).show();
                    } else {
                        GymAdapter adapter = new GymAdapter(gymList);
                        gymListView.setAdapter(adapter);
                    }
                }).addOnFailureListener((exception) -> {
                    Log.e("FindGymFragment", "Place not found: " + exception.getMessage());
                });
            }
        }).addOnFailureListener((exception) -> {
            if (!isAdded()) {
                return;
            }
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                if (apiException.getStatusCode() == PlacesStatusCodes.TIMEOUT && retryCount < MAX_RETRIES) {
                    retryCount++;
                    Log.e("FindGymFragment", "Location timeout, retrying... (" + retryCount + "/" + MAX_RETRIES + ")");
                    new Handler().postDelayed(() -> fetchNearbyGyms(currentLatLng), RETRY_DELAY_MS);
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch gyms: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FindGymFragment", "Error fetching gyms", exception);
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
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }
}