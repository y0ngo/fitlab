package com.example.fitlab;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_summary, container, false);

        workoutListView = view.findViewById(R.id.workout_list_view);
        timerTextView = view.findViewById(R.id.timer_text_view);
        startWorkoutButton = view.findViewById(R.id.start_workout_button);
        finishWorkoutButton = view.findViewById(R.id.finish_workout_button);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            addedExercises = getArguments().getStringArrayList("addedExercises");
        }

        if (addedExercises != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, addedExercises);
            workoutListView.setAdapter(adapter);
            workoutListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
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
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> workoutSummary = new HashMap<>();
            workoutSummary.put("userId", userId);
            workoutSummary.put("completedExercises", completedExercises);
            workoutSummary.put("timestamp", System.currentTimeMillis());

            // Add formatted date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = sdf.format(new Date());
            workoutSummary.put("date", date);

            db.collection("workout_summaries").add(workoutSummary)
                    .addOnSuccessListener(documentReference -> {
                        Context context = getContext();
                        if (context != null) {
                            Toast.makeText(context, "Workout summary saved", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("WorkoutSummaryFragment", "Context is null, cannot show Toast");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Context context = getContext();
                        if (context != null) {
                            Toast.makeText(context, "Error saving workout summary", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("WorkoutSummaryFragment", "Context is null, cannot show Toast");
                        }
                    });
        } else {
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("WorkoutSummaryFragment", "Context is null, cannot show Toast");
            }
        }
    }
}