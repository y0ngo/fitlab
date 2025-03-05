package com.example.fitlab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SelectExerciseActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_exercise);

        selectedExerciseTextView = findViewById(R.id.selected_exercise_text_view);
        kgEditText = findViewById(R.id.kg_edit_text);
        repsEditText = findViewById(R.id.reps_edit_text);
        setsEditText = findViewById(R.id.sets_edit_text);
        exerciseListView = findViewById(R.id.exercise_list_view);
        addExerciseButton = findViewById(R.id.add_exercise_button);
        doneButton = findViewById(R.id.done_button);
        addedExercises = new ArrayList<>();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wger.de/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(WgerApi.class);
        fetchExercises();

        addExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedExercise != null) {
                    String kg = kgEditText.getText().toString();
                    String reps = repsEditText.getText().toString();
                    String sets = setsEditText.getText().toString();

                    String exerciseDetails = selectedExercise.getName() + " - " + kg + "kg, " + reps + " reps, " + sets + " sets";
                    addedExercises.add(exerciseDetails);
                    Toast.makeText(SelectExerciseActivity.this, "Exercise added: " + exerciseDetails, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SelectExerciseActivity.this, "Please select an exercise", Toast.LENGTH_SHORT).show();
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectExerciseActivity.this, WorkoutSummaryActivity.class);
                intent.putStringArrayListExtra("addedExercises", (ArrayList<String>) addedExercises);
                startActivity(intent);
            }
        });
    }

    private void fetchExercises() {
        Call<ExerciseResponse> exerciseCall = api.getExercises(2); // 2 is the language ID for English
        exerciseCall.enqueue(new Callback<ExerciseResponse>() {
            @Override
            public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercise> exercises = response.body().getExercises();
                    ArrayAdapter<Exercise> adapter = new ArrayAdapter<>(SelectExerciseActivity.this, android.R.layout.simple_list_item_1, exercises);
                    exerciseListView.setAdapter(adapter);

                    exerciseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            selectedExercise = exercises.get(position);
                            selectedExerciseTextView.setText(selectedExercise.getName());
                        }
                    });
                } else {
                    Toast.makeText(SelectExerciseActivity.this, "Failed to fetch exercises", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ExerciseResponse> call, Throwable t) {
                Toast.makeText(SelectExerciseActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}