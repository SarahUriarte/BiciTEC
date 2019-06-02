package com.example.bicitec_project.api;

import com.example.bicitec_project.Classes.LogInResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Api {
    @GET("authentication/{username}/{password}")
    Call<LogInResponse> authentication(@Path("username")String userName, @Path("password")String password);
}