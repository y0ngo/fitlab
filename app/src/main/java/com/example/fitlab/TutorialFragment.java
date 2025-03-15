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
import java.util.ArrayList;
import java.util.List;

public class TutorialFragment extends Fragment {
    private RecyclerView recyclerView;
    private TutorialAdapter tutorialAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_tutorials);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Tutorial> tutorials = new ArrayList<>();
        tutorials.add(new Tutorial("ykJmrZ5v0Oo", "Bicep Curls"));
        tutorials.add(new Tutorial("gRVjAtPip0Y", "Bench Press"));
        tutorials.add(new Tutorial("m8wZNGL4iA4", "Squats"));
        // Add more tutorials as needed

        tutorialAdapter = new TutorialAdapter(tutorials, getLifecycle());
        recyclerView.setAdapter(tutorialAdapter);

        return view;
    }
}