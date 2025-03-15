package com.example.fitlab;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private ListView exerciseListView;
    private Button addExerciseButton;
    private Button doneButton;
    private WgerApi api;
    private Exercise selectedExercise;
    private List<String> addedExercises;
    private Spinner spinnerMuscleGroup;
    private HashMap<String, Integer> muscleMap;
    private boolean isSpinnerInitialized = false;
    private RecyclerView recyclerView;
    private ExerciseAdapterFilterable exerciseAdapter;

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
                .baseUrl("https://wger.de/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(WgerApi.class);
        fetchExercises(2);
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
            WorkoutSummaryFragment fragment = new WorkoutSummaryFragment();
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, muscleGroups);
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
           public void onFailure(Call<ExerciseResponse> call, Throwable t) {
               Log.e("API Error", "Failed to fetch exercises", t);
           }
       });
   }
}