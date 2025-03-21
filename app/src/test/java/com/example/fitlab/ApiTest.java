package com.example.fitlab;

import org.junit.Test;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.ResponseBody;

import java.io.IOException;

public class ApiTest {

    @Test
    public void testApiCall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wger.de/api/v2/") // Replace with your API base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WgerApi api = retrofit.create(WgerApi.class);

        Call<ResponseBody> call = api.getExercise(2); // Replace with appropriate language ID
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        System.out.println("API Success: Response body: " + response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("API Error: Response unsuccessful or body is null");
                    if (response.errorBody() != null) {
                        try {
                            System.err.println("Error body: " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.err.println("API Error: Failed to fetch exercises");
                t.printStackTrace();
            }
        });
    }
}