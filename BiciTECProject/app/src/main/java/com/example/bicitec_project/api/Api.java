package com.example.bicitec_project.api;

import com.example.bicitec_project.Classes.LogInResponse;
import com.example.bicitec_project.Classes.TimeResponse;
import com.example.bicitec_project.Classes.TimerRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Api {
    @GET("authentication/{username}/{password}")
    Call<LogInResponse> authentication(@Path("username")String userName, @Path("password")String password);

    @POST("email/{user_id}/{recipient_email}/{subject}/{message}")
    Call<String> sendMail(@Path("user_id")String user_id, @Path("recipient_email")String recipient_email,
                          @Path("subject")String subject, @Path("message") String message);

    @GET("server_time/{user_id}")
    Call<TimeResponse> time(@Path("user_id")String user_id);


    @POST("setTimer")
    Call<String>requestTimer(@Body TimerRequest timerRequest);
}