package com.example.fitlab;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText nameField, userIdField, emailField, passwordField;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameField = findViewById(R.id.name);
        userIdField = findViewById(R.id.userId);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        signupButton = findViewById(R.id.signupButton);

        signupButton.setOnClickListener(v -> signUpUser());
    }

    private void signUpUser() {
        String name = nameField.getText().toString();
        String userId = userIdField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if (name.isEmpty() || userId.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Store user details in Firestore
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("userId", userId);
                        userData.put("email", email);

                        db.collection("users").document(user.getUid())
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                    replaceFragment(new HomeFragment());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void replaceFragment(Fragment fragment) {
        // Clear the back stack to ensure no fragments are lingering
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Replace the current fragment with the new one
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .setReorderingAllowed(true)
                .commit(); // Use commit() instead of addToBackStack(null)
    }
}
