package com.example.fitlab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ActivityFragment extends Fragment {

    private RecyclerView recyclerView;
    private WorkoutAdapter workoutAdapter;
    private List<Workout> workoutList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_workouts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize workout list and adapter
        workoutList = new ArrayList<>();
        workoutAdapter = new WorkoutAdapter(workoutList);
        recyclerView.setAdapter(workoutAdapter);

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Load previous workouts
        loadPreviousWorkouts();

        return view;
    }

    private void loadPreviousWorkouts() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("workout_summaries")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            workoutList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String title = document.getId();
                                List<String> completedExercises = (List<String>) document.get("completedExercises");
                                workoutList.add(new Workout(title, completedExercises.toString()));
                            }
                            workoutAdapter.notifyDataSetChanged();
                        } else {
                            // Handle the error
                        }
                    });
        }
    }
}