package com.example.bicitec_project.Classes;

import com.example.bicitec_project.User;

public class Record {
    private String user;
    private String horaInicio;
    private String horaFin;
    private String horaActual;
    private String adressFeather;
    private  String estado;

    public Record(String user, String horaInicio, String horaFin, String horaActual, String adressFeather, String estado) {
        this.user = user;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.horaActual = horaActual;
        this.adressFeather = adressFeather;
        this.estado = estado;
    }

    public String getUser() {
        return user;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public String getHoraActual() {
        return horaActual;
    }

    public String getAdressFeather() {
        return adressFeather;
    }

    public String getEstado() {
        return estado;
    }
}
