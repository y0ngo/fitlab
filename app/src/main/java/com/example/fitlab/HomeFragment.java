package com.example.fitlab;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private Spinner spinnerMuscleGroup;
    private ExerciseAdapterFilterable exerciseAdapter;
    private WgerApi api;
    private Map<String, Integer> muscleMap;
    private boolean isSpinnerInitialized = false;
    private YouTubePlayerView youTubePlayerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_exercises);
        spinnerMuscleGroup = view.findViewById(R.id.spinner_muscle_group);
        youTubePlayerView = view.findViewById(R.id.youtube_player_view);


        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup Spinner
        setupMuscleGroupSpinner();

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wger.de/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(WgerApi.class);

        // Fetch exercises
        fetchExercises(2); // Default to English exercises

        // Initialize YouTube Player
        getLifecycle().addObserver(youTubePlayerView);
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                String videoId = "gcNh17Ckjgg";
                youTubePlayer.cueVideo(videoId, 0);
            }
        });



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setTitle("Home");
        }
    }

    private void setupMuscleGroupSpinner() {
        String[] muscleGroups = {
                "Select Muscle Group", "Biceps", "Anterior deltoid (Front Shoulders)", "Serratus anterior", "Pectoralis major (Chest)",
                "Triceps", "Trapezius (Upper Back)", "Quadriceps", "Gastrocnemius (Calves)", "Gluteus maximus (Glutes)",
                "Hamstrings", "Latissimus dorsi (Lats)", "Obliques", "Abdominals (Abs)", "Posterior deltoid (Rear Shoulders)",
                "Lower back (Erector Spinae)", "Brachialis (Forearm)", "Soleus (Lower Calf)"
        };

        muscleMap = new HashMap<>();
        muscleMap.put("Biceps", 1);
        muscleMap.put("Anterior deltoid (Front Shoulders)", 2);
        muscleMap.put("Serratus anterior", 3);
        muscleMap.put("Pectoralis major (Chest)", 4);
        muscleMap.put("Triceps", 5);
        muscleMap.put("Trapezius (Upper Back)", 6);
        muscleMap.put("Quadriceps", 7);
        muscleMap.put("Gastrocnemius (Calves)", 8);
        muscleMap.put("Gluteus maximus (Glutes)", 9);
        muscleMap.put("Hamstrings", 10);
        muscleMap.put("Latissimus dorsi (Lats)", 11);
        muscleMap.put("Obliques", 12);
        muscleMap.put("Abdominals (Abs)", 13);
        muscleMap.put("Posterior deltoid (Rear Shoulders)", 14);
        muscleMap.put("Lower back (Erector Spinae)", 15);
        muscleMap.put("Brachialis (Forearm)", 16);
        muscleMap.put("Soleus (Lower Calf)", 17);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, muscleGroups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMuscleGroup.setAdapter(adapter);

        spinnerMuscleGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitialized) {
                    String selectedMuscleGroup = (String) parent.getItemAtPosition(position);
                    if (!selectedMuscleGroup.equals("Select Muscle Group")) {
                        int muscleId = muscleMap.get(selectedMuscleGroup);
                        fetchExercisesByMuscle(2, muscleId);
                    } else {
                        fetchExercises(2); // Fetch all exercises if no specific muscle group is selected
                    }
                } else {
                    isSpinnerInitialized = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    public String removeHtmlTags(String input) {
        return input.replaceAll("<[^>]*>", "");
    }

    private void fetchExercises(int language) {
        Call<ExerciseResponse> exerciseCall = api.getExercises(language);
        exerciseCall.enqueue(new Callback<ExerciseResponse>() {
            @Override
            public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercise> exercises = response.body().getExercises();
                    exerciseAdapter = new ExerciseAdapterFilterable(exercises, exercise -> {
                        // Handle exercise click
                    });
                    recyclerView.setAdapter(exerciseAdapter);
                } else {
                    Log.e("API Error", "Response unsuccessful or body is null");
                }
            }

            @Override
            public void onFailure(Call<ExerciseResponse> call, Throwable t) {
                Log.e("API Error", "Failed to fetch exercises", t);
            }
        });
    }

    private void fetchExercisesByMuscle(int language, int muscleId) {
        Call<ExerciseResponse> exerciseCall = api.getExercisesByMuscle(language, muscleId);
        exerciseCall.enqueue(new Callback<ExerciseResponse>() {
            @Override
            public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercise> exercises = response.body().getExercises();
                    exerciseAdapter = new ExerciseAdapterFilterable(exercises, exercise -> {
                        // Handle exercise click
                    });
                    recyclerView.setAdapter(exerciseAdapter);
                } else {
                    Log.e("API Error", "Response unsuccessful or body is null");
                }
            }

            @Override
            public void onFailure(Call<ExerciseResponse> call, Throwable t) {
                Log.e("API Error", "Failed to fetch exercises", t);
            }
        });
    }
}