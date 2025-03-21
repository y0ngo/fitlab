package com.example.fitlab;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView profileName, profileEmail, heightValue, weightValue, bmiValue, fitnessGoal;
    private MaterialButton editProfileButton, updateStatsButton, changeGoalButton;
    private LinearLayout settingsButton, helpButton, logoutButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        heightValue = view.findViewById(R.id.height_value);
        weightValue = view.findViewById(R.id.weight_value);
        bmiValue = view.findViewById(R.id.bmi_value);
        fitnessGoal = view.findViewById(R.id.fitness_goal);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        updateStatsButton = view.findViewById(R.id.update_stats_button);
        changeGoalButton = view.findViewById(R.id.change_goal_button);
        settingsButton = view.findViewById(R.id.settings_button);
        helpButton = view.findViewById(R.id.help_button);
        logoutButton = view.findViewById(R.id.logout_button);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load user data
        loadUserData();

        // Set logout button click listener
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");
                            Long height = documentSnapshot.getLong("height");
                            Long weight = documentSnapshot.getLong("weight");
                            String goal = documentSnapshot.getString("fitnessGoal");

                            profileName.setText(name != null ? name : "N/A");
                            profileEmail.setText(email != null ? email : "N/A");
                            heightValue.setText(height != null ? String.valueOf(height) : "N/A");
                            weightValue.setText(weight != null ? String.valueOf(weight) : "N/A");
                            fitnessGoal.setText(goal != null ? goal : "N/A");

                            if (height != null && weight != null) {
                                double bmi = calculateBMI(height, weight);
                                bmiValue.setText(String.format("%.1f", bmi));
                            } else {
                                bmiValue.setText("N/A");
                            }
                        } else {
                            Log.e("ProfileFragment", "Document does not exist");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileFragment", "Error fetching document", e);
                    });
        } else {
            Log.e("ProfileFragment", "User is not authenticated");
        }
    }

    private double calculateBMI(long height, long weight) {
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }
}