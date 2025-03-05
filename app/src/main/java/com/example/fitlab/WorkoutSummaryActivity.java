package com.example.fitlab;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class WorkoutSummaryActivity extends AppCompatActivity {

    private ListView workoutListView;
    private TextView timerTextView;
    private Button startWorkoutButton;
    private Button finishWorkoutButton;
    private ArrayList<String> addedExercises;
    private boolean workoutStarted = false;
    private Handler handler = new Handler();
    private Runnable timerRunnable;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_summary);

        workoutListView = findViewById(R.id.workout_list_view);
        timerTextView = findViewById(R.id.timer_text_view);
        startWorkoutButton = findViewById(R.id.start_workout_button);
        finishWorkoutButton = findViewById(R.id.finish_workout_button);

        addedExercises = getIntent().getStringArrayListExtra("addedExercises");

        if (addedExercises != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, addedExercises);
            workoutListView.setAdapter(adapter);
        }

        startWorkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!workoutStarted) {
                    workoutStarted = true;
                    startTime = System.currentTimeMillis();
                    timerRunnable = new Runnable() {
                        @Override
                        public void run() {
                            long elapsedTime = System.currentTimeMillis() - startTime;
                            timerTextView.setText("Workout Time: " + (elapsedTime / 1000) + " seconds");
                            handler.postDelayed(this, 1000);
                        }
                    };
                    handler.post(timerRunnable);
                }
            }
        });

        finishWorkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (workoutStarted) {
                    workoutStarted = false;
                    handler.removeCallbacks(timerRunnable);
                    saveWorkoutSummary();
                }
            }
        });
    }

    private void saveWorkoutSummary() {
        String filename = "workout_summary.json";
        StringBuilder workoutSummary = new StringBuilder();
        workoutSummary.append("{ \"workout\": [");
        for (String exercise : addedExercises) {
            workoutSummary.append("\"").append(exercise).append("\",");
        }
        workoutSummary.deleteCharAt(workoutSummary.length() - 1); // Remove last comma
        workoutSummary.append("]}");

        try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
            fos.write(workoutSummary.toString().getBytes());
            Toast.makeText(this, "Workout summary saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save workout summary", Toast.LENGTH_SHORT).show();
        }
    }
}