package com.example.bicitec_project.Classes;

import com.example.bicitec_project.User;

public class Record {
    private User user;
    private String horaInicio;
    private String horaFin;
    private String horaActual;
    private String adressFeather;

    public Record(User user, String horaInicio, String horaFin, String horaActual, String adressFeather) {
        this.user = user;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.horaActual = horaActual;
        this.adressFeather = adressFeather;
    }
}
