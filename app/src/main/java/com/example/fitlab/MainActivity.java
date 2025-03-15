package com.example.fitlab;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize FloatingActionButton
        FloatingActionButton fabAddWorkout = findViewById(R.id.add_workout_fab);
        fabAddWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch SelectExerciseFragment when FAB is clicked
                SelectExerciseFragment fragment = new SelectExerciseFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    replaceFragment(new HomeFragment());
                    return true;
                } else if (itemId == R.id.workouts) {
                    replaceFragment(new WorkoutsFragment());
                    return true;
                } else if (itemId == R.id.tutorials) {
                    replaceFragment(new TutorialFragment());
                    return true;
                } else if (itemId == R.id.activity) {
                    replaceFragment(new ActivityFragment());
                    return true;
                } else if (itemId == R.id.profile) {
                    replaceFragment(new FindGymFragment());
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Load HomeFragment by default
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.home);
        }
    }

    // Helper method to replace fragments in the container
    void replaceFragment(Fragment fragment) {
        // Optional: clear the back stack if necessary
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .setReorderingAllowed(true)
                .commit();
    }
}
