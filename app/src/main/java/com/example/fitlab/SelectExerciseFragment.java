package com.example.fitlab;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SelectExerciseFragment extends Fragment {

    private TextView selectedExerciseTextView;
    private EditText kgEditText;
    private EditText repsEditText;
    private EditText setsEditText;
    private Button addExerciseButton;
    private Button doneButton;
    private Spinner spinnerMuscleGroup;
    private RecyclerView recyclerView;
    private ExerciseAdapterFilterable exerciseAdapter;
    private ExerciseDBApi api;
    private Exercise selectedExercise;
    private List<String> addedExercises;
    private Map<String, String> muscleMap;
    private boolean isSpinnerInitialized = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_exercise, container, false);

        selectedExerciseTextView = view.findViewById(R.id.selected_exercise_text_view);
        kgEditText = view.findViewById(R.id.kg_edit_text);
        repsEditText = view.findViewById(R.id.reps_edit_text);
        setsEditText = view.findViewById(R.id.sets_edit_text);
        addExerciseButton = view.findViewById(R.id.add_exercise_button);
        doneButton = view.findViewById(R.id.done_button);
        spinnerMuscleGroup = view.findViewById(R.id.spinner_muscle_group);
        recyclerView = view.findViewById(R.id.recycler_view_exercises);
        addedExercises = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://exercisedb.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(ExerciseDBApi.class);
        fetchExercises();
        setupMuscleGroupSpinner();

        addExerciseButton.setOnClickListener(v -> {
            if (selectedExercise != null) {
                String kg = kgEditText.getText().toString();
                String reps = repsEditText.getText().toString();
                String sets = setsEditText.getText().toString();

                String exerciseDetails = selectedExercise.getName() + " - " + kg + "kg, " + reps + " reps, " + sets + " sets";
                addedExercises.add(exerciseDetails);
                Toast.makeText(getContext(), "Exercise added: " + exerciseDetails, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please select an exercise", Toast.LENGTH_SHORT).show();
            }
        });

        doneButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("addedExercises", new ArrayList<>(addedExercises));
            WorkoutSummaryFragment fragment = new WorkoutSummaryFragment();
            fragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
        });

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
                    exerciseAdapter = new ExerciseAdapterFilterable(exercises, exercise -> {
                        selectedExercise = exercise;
                        selectedExerciseTextView.setText("Selected: " + exercise.getName());
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
                        selectedExercise = exercise;
                        selectedExerciseTextView.setText("Selected: " + exercise.getName());
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