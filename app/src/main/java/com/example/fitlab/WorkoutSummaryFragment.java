package com.example.fitlab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class WorkoutSummaryFragment extends Fragment {

    private ListView workoutListView;
    private TextView timerTextView;
    private Button startWorkoutButton;
    private Button finishWorkoutButton;
    private ArrayList<String> addedExercises;
    private boolean workoutStarted = false;
    private Handler handler = new Handler();
    private Runnable timerRunnable;
    private long startTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_summary, container, false);

        workoutListView = view.findViewById(R.id.workout_list_view);
        timerTextView = view.findViewById(R.id.timer_text_view);
        startWorkoutButton = view.findViewById(R.id.start_workout_button);
        finishWorkoutButton = view.findViewById(R.id.finish_workout_button);

        if (getArguments() != null) {
            addedExercises = getArguments().getStringArrayList("addedExercises");
        }

        if (addedExercises != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, addedExercises);
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

        return view;
    }

    private void showWorkoutSummaryDialog() {
        ArrayList<String> completedExercises = new ArrayList<>();
        for (int i = 0; i < workoutListView.getCount(); i++) {
            if (workoutListView.isItemChecked(i)) {
                completedExercises.add((String) workoutListView.getItemAtPosition(i));
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Congratulations! You finished your workout");
        builder.setMessage("Workout Summary:\n" + String.join("\n", completedExercises));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveWorkoutSummary(completedExercises);
                // Navigate back to MainActivity or another fragment
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        builder.show();
    }

    private void saveWorkoutSummary(ArrayList<String> completedExercises) {
        StringBuilder workoutSummary = new StringBuilder();
        for (String exercise : completedExercises) {
            workoutSummary.append(exercise).append("\n");
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("workout_summaries", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("summary_" + System.currentTimeMillis(), workoutSummary.toString());
        editor.apply();

        Toast.makeText(getContext(), "Workout summary saved", Toast.LENGTH_SHORT).show();
    }
}