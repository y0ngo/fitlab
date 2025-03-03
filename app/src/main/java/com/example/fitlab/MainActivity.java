package com.example.fitlab;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private ExerciseAdapter exerciseAdapter;
    private WgerApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view_exercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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

    private void fetchExercises(int language) {
        Call<ExerciseResponse> exerciseCall = api.getExercises(language);
        exerciseCall.enqueue(new Callback<ExerciseResponse>() {
            @Override
            public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercise> exercises = response.body().getExercises();
                    fetchExerciseImages(exercises);
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
                    fetchExerciseImages(exercises);
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

    private void fetchExerciseImages(List<Exercise> exercises) {
        Call<ExerciseImageResponse> imageCall = api.getExerciseImages(true); // Main images
        imageCall.enqueue(new Callback<ExerciseImageResponse>() {
            @Override
            public void onResponse(Call<ExerciseImageResponse> call, Response<ExerciseImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<Integer, String> exerciseImageMap = new HashMap<>();
                    for (ExerciseImage image : response.body().getExerciseImages()) {
                        exerciseImageMap.put(image.getExerciseId(), image.getImageUrl());
                    }
                    for (Exercise exercise : exercises) {
                        exercise.setImageUrl(exerciseImageMap.get(exercise.getId()));
                    }
                    exerciseAdapter = new ExerciseAdapter(exercises);
                    recyclerView.setAdapter(exerciseAdapter);
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
        } else if (item.getItemId() == R.id.nav_filter_muscle) {
            // Example muscle group ID, replace with actual selection logic
            int selectedMuscleId = 1;
            fetchExercisesByMuscle(2, selectedMuscleId);
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