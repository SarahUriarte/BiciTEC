package com.example.bicitec_project.Classes;

import com.example.bicitec_project.User;

public class Record {
    private String user;
    private String horaInicio;
    private String horaFin;
    private String adressFeather;
    private int estacionSalida;
    private int estacionLlegada;

    public Record(String user, String horaInicio, String horaFin, String adressFeather, int estacionSalida, int estacionLlegada) {
        this.user = user;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.adressFeather = adressFeather;
        this.estacionSalida = estacionSalida;
        this.estacionLlegada = estacionLlegada;
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

    public String getAdressFeather() {
        return adressFeather;
    }

    public int getEstacionSalida() {
        return estacionSalida;
    }

    public int getEstacionLlegada() {
        return estacionLlegada;
    }
}
