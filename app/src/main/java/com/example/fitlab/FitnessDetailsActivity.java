package com.example.fitlab;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FitnessDetailsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText heightField, weightField;
    private Spinner fitnessGoalSpinner;
    private Button saveDetailsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        heightField = findViewById(R.id.height);
        weightField = findViewById(R.id.weight);
        fitnessGoalSpinner = findViewById(R.id.fitnessGoalSpinner);
        saveDetailsButton = findViewById(R.id.saveDetailsButton);

        // Set up the Spinner with the fitness goals and hint
        ArrayList<String> goals = new ArrayList<>();
        goals.add("What are your fitness goals?");
        goals.add("Improve cardiovascular endurance");
        goals.add("Get stronger");
        goals.add("Tone up");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, goals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fitnessGoalSpinner.setAdapter(adapter);

        saveDetailsButton.setOnClickListener(v -> saveFitnessDetails());
    }

    private void saveFitnessDetails() {
        String height = heightField.getText().toString();
        String weight = weightField.getText().toString();
        String fitnessGoal = fitnessGoalSpinner.getSelectedItem().toString();

        if (height.isEmpty() || weight.isEmpty() || fitnessGoal.equals("What are your fitness goals?")) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Details saved successfully!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving details", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}