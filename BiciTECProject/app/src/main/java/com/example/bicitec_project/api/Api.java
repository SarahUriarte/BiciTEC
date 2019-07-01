package com.example.bicitec_project.api;

import com.example.bicitec_project.Classes.LogInResponse;
import com.example.bicitec_project.Classes.TimerRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Api {
    @GET("authentication/{username}/{password}")
    Call<LogInResponse> authentication(@Path("username")String userName, @Path("password")String password);

    @POST("setTimer")
    Call<String>requestTimer(@Body TimerRequest timerRequest);
}