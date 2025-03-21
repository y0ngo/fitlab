package com.example.fitlab;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private Spinner spinnerMuscleGroup;
    private ExerciseAdapterFilterable exerciseAdapter;
    private ExerciseDBApi api;
    private Map<String, String> muscleMap;
    private boolean isSpinnerInitialized = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_exercises);
        spinnerMuscleGroup = view.findViewById(R.id.spinner_muscle_group);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup Spinner
        setupMuscleGroupSpinner();

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://exercisedb.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ExerciseDBApi.class);

        // Fetch exercises
        fetchExercises();

        return view;
    }

    private void setupMuscleGroupSpinner() {
        String[] muscleGroups = {
                "Select Muscle Group", "back", "cardio", "chest", "lower arms", "lower legs", "neck", "shoulders", "upper arms", "upper legs", "waist"
        };

        muscleMap = new HashMap<>();
        for (String muscle : muscleGroups) {
            muscleMap.put(muscle, muscle);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, muscleGroups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMuscleGroup.setAdapter(adapter);

        spinnerMuscleGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitialized) {
                    String selectedMuscleGroup = (String) parent.getItemAtPosition(position);
                    if (!selectedMuscleGroup.equals("Select Muscle Group")) {
                        fetchExercisesByMuscle(selectedMuscleGroup);
                    } else {
                        fetchExercises(); // Fetch all exercises if no specific muscle group is selected
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

    private void fetchExercises() {
        Call<List<Exercise>> exerciseCall = api.getExercises();
        exerciseCall.enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercise> exercises = response.body();
                    Log.d("API Success", "Number of exercises fetched: " + exercises.size());
                    exerciseAdapter = new ExerciseAdapterFilterable(exercises, exercise -> {
                        // Handle exercise click
                    });
                    recyclerView.setAdapter(exerciseAdapter);
                } else {
                    Log.e("API Error", "Response unsuccessful or body is null");
                    if (response.errorBody() != null) {
                        try {
                            Log.e("API Error", "Error body: " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                Log.e("API Error", "Failed to fetch exercises", t);
            }
        });
    }

    private void fetchExercisesByMuscle(String muscle) {
        Call<List<Exercise>> exerciseCall = api.getExercises();
        exerciseCall.enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercise> exercises = response.body();
                    exercises.removeIf(exercise -> !exercise.getBodyPart().equalsIgnoreCase(muscle));
                    exerciseAdapter = new ExerciseAdapterFilterable(exercises, exercise -> {
                        // Handle exercise click
                    });
                    recyclerView.setAdapter(exerciseAdapter);
                } else {
                    Log.e("API Error", "Response unsuccessful or body is null");
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                Log.e("API Error", "Failed to fetch exercises", t);
            }
        });
    }
}