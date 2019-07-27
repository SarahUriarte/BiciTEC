package com.example.bicitec_project.Classes;

public class Bicycle {
    private String adress;
    private String state;
    private int station;

    public Bicycle(){}

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setStation(int station) {
        this.station = station;
    }

    public String getAdress() {
        return adress;
    }

    public String getState() {
        return state;
    }

    public int getStation() {
        return station;
    }
}
