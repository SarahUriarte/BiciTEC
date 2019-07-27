package com.example.bicitec_project.Classes;

import com.example.bicitec_project.User;

public class Record {
    private String user;
    private String horaInicio;
    private String horaFin;
    private String adressFeather;
    private String estacionSalida;
    private String estacionLlegada;

    public Record(String user, String horaInicio, String horaFin, String adressFeather, String estacionSalida, String estacionLlegada) {
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

    public String getEstacionSalida() {
        return estacionSalida;
    }

    public String getEstacionLlegada() {
        return estacionLlegada;
    }
}
