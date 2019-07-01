package com.example.bicitec_project.Classes;

public class TimerRequest {
    private String token;
    private int accion; // 1 para iniciar timer, 2 para finalizar

    public TimerRequest(String token, int accion) {
        this.token = token;
        this.accion = accion;
    }

    public String getToken() {
        return token;
    }

    public int getAccion() {
        return accion;
    }
}
