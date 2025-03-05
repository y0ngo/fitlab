package com.example.fitlab;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String BASE_URL = "https://wger.de/api/v2/";

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private ExerciseAdapterFilterable exerciseAdapter;
    private WgerApi api;
    private Spinner spinnerMuscleGroup;
    private boolean isSpinnerInitialized = false;
    private Map<String, Integer> muscleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fabAddWorkout = findViewById(R.id.add_workout_fab);
        fabAddWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SelectExerciseActivity
                Intent intent = new Intent(MainActivity.this, SelectExerciseActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.recycler_view_exercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        spinnerMuscleGroup = findViewById(R.id.spinner_muscle_group);
        setupMuscleGroupSpinner();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(WgerApi.class);
        fetchExercises(2); // Default to English exercises

        Toolbar toolbar = findViewById(R.id.toolbar); // Ignore red line errors
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    private void setupMuscleGroupSpinner() {
        String[] muscleGroups = {
                "Select Muscle Group", "Biceps", "Anterior deltoid (Front Shoulders)", "Serratus anterior", "Pectoralis major (Chest)",
                "Triceps", "Trapezius (Upper Back)", "Quadriceps", "Gastrocnemius (Calves)", "Gluteus maximus (Glutes)",
                "Hamstrings", "Latissimus dorsi (Lats)", "Obliques", "Abdominals (Abs)", "Posterior deltoid (Rear Shoulders)",
                "Lower back (Erector Spinae)", "Brachialis (Forearm)", "Soleus (Lower Calf)"
        };

        muscleMap = new HashMap<>();
        muscleMap.put("Biceps", 1);
        muscleMap.put("Anterior deltoid (Front Shoulders)", 2);
        muscleMap.put("Serratus anterior", 3);
        muscleMap.put("Pectoralis major (Chest)", 4);
        muscleMap.put("Triceps", 5);
        muscleMap.put("Trapezius (Upper Back)", 6);
        muscleMap.put("Quadriceps", 7);
        muscleMap.put("Gastrocnemius (Calves)", 8);
        muscleMap.put("Gluteus maximus (Glutes)", 9);
        muscleMap.put("Hamstrings", 10);
        muscleMap.put("Latissimus dorsi (Lats)", 11);
        muscleMap.put("Obliques", 12);
        muscleMap.put("Abdominals (Abs)", 13);
        muscleMap.put("Posterior deltoid (Rear Shoulders)", 14);
        muscleMap.put("Lower back (Erector Spinae)", 15);
        muscleMap.put("Brachialis (Forearm)", 16);
        muscleMap.put("Soleus (Lower Calf)", 17);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, muscleGroups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMuscleGroup.setAdapter(adapter);

        spinnerMuscleGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitialized) {
                    String selectedMuscleGroup = (String) parent.getItemAtPosition(position);
                    if (!selectedMuscleGroup.equals("Select Muscle Group")) {
                        int muscleId = muscleMap.get(selectedMuscleGroup);
                        fetchExercisesByMuscle(2, muscleId);
                    } else {
                        fetchExercises(2); // Fetch all exercises if no specific muscle group is selected
                    }
                } else {
                    isSpinnerInitialized = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void fetchExercises(int language) {
        Call<ExerciseResponse> exerciseCall = api.getExercises(language);
        exerciseCall.enqueue(new Callback<ExerciseResponse>() {
            @Override
            public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercise> exercises = response.body().getExercises();
                    exerciseAdapter = new ExerciseAdapterFilterable(exercises);
                    recyclerView.setAdapter(exerciseAdapter); // Set the adapter here
                } else {
                    Log.e("API Error", "Response unsuccessful or body is null");
                }
            }

            @Override
            public void onFailure(Call<ExerciseResponse> call, Throwable t) {
                Log.e("API Error", "Failed to fetch exercises", t);
            }
        });
    }

    private void fetchExercisesByMuscle(int language, int muscleId) {
        Call<ExerciseResponse> exerciseCall = api.getExercisesByMuscle(language, muscleId);
        exerciseCall.enqueue(new Callback<ExerciseResponse>() {
            @Override
            public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercise> exercises = response.body().getExercises();
                    exerciseAdapter = new ExerciseAdapterFilterable(exercises);
                    recyclerView.setAdapter(exerciseAdapter); // Set the adapter here
                } else {
                    Log.e("API Error", "Response unsuccessful or body is null");
                }
            }

            @Override
            public void onFailure(Call<ExerciseResponse> call, Throwable t) {
                Log.e("API Error", "Failed to fetch exercises", t);
            }

        });
    }
    private void fetchExerciseImages(Exercise exercise) {
        Call<ExerciseImageResponse> imageCall = api.getExerciseImages(exercise.getId());
        imageCall.enqueue(new Callback<ExerciseImageResponse>() {
            @Override
            public void onResponse(Call<ExerciseImageResponse> call, Response<ExerciseImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ExerciseImage> images = response.body().getExerciseImages();
                    if (!images.isEmpty()) {
                        exercise.setImageUrl(images.get(0).getImageUrl());
                    }
                    exerciseAdapter.notifyDataSetChanged();
                } else {
                    Log.e("API Error", "Response unsuccessful or body is null");
                }
            }

            @Override
            public void onFailure(Call<ExerciseImageResponse> call, Throwable t) {
                Log.e("API Error", "Failed to fetch exercise images", t);
            }
        });
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        } else if (item.getItemId() == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
        } else if (item.getItemId() == R.id.nav_me) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MeFragment()).commit();
        } else if (item.getItemId() == R.id.nav_gym) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GymFragment()).commit();
        } else if (item.getItemId() == R.id.nav_logout) {
            Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
