package com.example.bicitec_project.Classes;

public class LogInResponse {
    private String authentication;
    private int user_id;

    public LogInResponse(String authentication, int user_id) {
        this.authentication = authentication;
        this.user_id = user_id;
    }

    public String getAuthentication() {
        return authentication;
    }

    public int getUser_id() {
        return user_id;
    }
}
