package com.example.fitlab;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface ExerciseDBApi {
    @Headers({
            "X-RapidAPI-Key: 2c88eb9997msh93e1ed470796691p1b5f63jsn9cc88cd36945",
            "X-RapidAPI-Host: exercisedb.p.rapidapi.com"
    })
    @GET("exercises")
    Call<List<Exercise>> getExercises();
}