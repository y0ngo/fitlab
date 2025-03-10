package com.example.fitlab;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlab.R;
import com.example.fitlab.Workout;
import com.example.fitlab.WorkoutAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActivityFragment extends Fragment {

    private RecyclerView recyclerView;
    private WorkoutAdapter workoutAdapter;
    private List<Workout> workoutList;

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

        // Load previous workouts
        loadPreviousWorkouts();

        return view;
    }

    private void loadPreviousWorkouts() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("workout_summaries", getContext().MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            workoutList.add(new Workout(entry.getKey(), entry.getValue().toString()));
        }
        workoutAdapter.notifyDataSetChanged();
    }
}