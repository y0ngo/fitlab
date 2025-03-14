package com.example.fitlab;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FitnessDetailsFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText heightField, weightField;
    private Spinner fitnessGoalSpinner;
    private Button saveDetailsButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fitness_details, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        heightField = view.findViewById(R.id.height);
        weightField = view.findViewById(R.id.weight);
        fitnessGoalSpinner = view.findViewById(R.id.fitnessGoalSpinner);
        saveDetailsButton = view.findViewById(R.id.saveDetailsButton);

        // Set up the Spinner with the fitness goals and hint
        ArrayList<String> goals = new ArrayList<>();
        goals.add("What are your fitness goals?");
        goals.add("Improve cardiovascular endurance");
        goals.add("Get stronger");
        goals.add("Tone up");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, goals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fitnessGoalSpinner.setAdapter(adapter);

        saveDetailsButton.setOnClickListener(v -> saveFitnessDetails());

        return view;
    }

    private void saveFitnessDetails() {
        String height = heightField.getText().toString();
        String weight = weightField.getText().toString();
        String fitnessGoal = fitnessGoalSpinner.getSelectedItem().toString();

        if (height.isEmpty() || weight.isEmpty() || fitnessGoal.equals("What are your fitness goals?")) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> fitnessDetails = new HashMap<>();
        fitnessDetails.put("height", Integer.parseInt(height));
        fitnessDetails.put("weight", Integer.parseInt(weight));
        fitnessDetails.put("fitnessGoal", fitnessGoal);

        FirebaseUser user = mAuth.getCurrentUser();
        db.collection("users").document(user.getUid())
                .update(fitnessDetails)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Details saved successfully!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error saving details", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }
}