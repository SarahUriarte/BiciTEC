package com.example.bicitec_project.Classes;

import com.example.bicitec_project.User;

public class Record {
    private String user;
    private String horaInicio;
    private String horaFin;
    private int  cronometro;
    private int temporizador;
    private String adressFeather;
    private  String estado;

    public Record(String user, String horaInicio, String horaFin, int cronometro, int temporizador, String adressFeather, String estado) {
        this.user = user;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.cronometro = cronometro;
        this.temporizador = temporizador;
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

    public int getCronometro() {
        return cronometro;
    }

    public int getTemporizador() {
        return temporizador;
    }

    public String getAdressFeather() {
        return adressFeather;
    }

    public String getEstado() {
        return estado;
    }
}
