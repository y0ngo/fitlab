package com.example.fitlab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
                    showWorkoutSummaryDialog();
                }
            }
        });
    }

    private void showWorkoutSummaryDialog() {
        ArrayList<String> completedExercises = new ArrayList<>();
        for (int i = 0; i < workoutListView.getCount(); i++) {
            if (workoutListView.isItemChecked(i)) {
                completedExercises.add((String) workoutListView.getItemAtPosition(i));
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Congratulations! You finished your workout");
        builder.setMessage("Workout Summary:\n" + String.join("\n", completedExercises));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveWorkoutSummary(completedExercises);
                Intent intent = new Intent(WorkoutSummaryActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.show();
    }

    private void saveWorkoutSummary(ArrayList<String> completedExercises) {
        StringBuilder workoutSummary = new StringBuilder();
        for (String exercise : completedExercises) {
            workoutSummary.append(exercise).append("\n");
        }

        SharedPreferences sharedPreferences = getSharedPreferences("workout_summaries", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("summary_" + System.currentTimeMillis(), workoutSummary.toString());
        editor.apply();

        Toast.makeText(this, "Workout summary saved", Toast.LENGTH_SHORT).show();
    }
}