package com.example.bicitec_project.Classes;

public class LogInResponse {
    private String authentication;
    private String user_id;

    public LogInResponse(String authentication, String user_id) {
        this.authentication = authentication;
        this.user_id = user_id;
    }

    public String getAuthentication() {
        return authentication;
    }

    public String getUser_id() {
        return user_id;
    }
}
