package com.example.fitlab;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WgerApi {
    @GET("exercise/")
    Call<ExerciseResponse> getExercises(@Query("language") int language);

    @GET("exercise/")
    Call<ExerciseResponse> getExercisesByMuscle(@Query("language") int language, @Query("muscles") int muscleId);

    @GET("exerciseimage/")
    Call<ExerciseImageResponse> getExerciseImages(@Query("exercise") int exerciseId);
}