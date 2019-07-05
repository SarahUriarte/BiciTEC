package com.example.bicitec_project.Classes;

public class UserLoan {
    private String adressFeather;
    private String date;
    private int kmsTraveled;
    private int carbonFootprint;
    private String loanState;

    public UserLoan(String adressFeather, String date, int kmsTraveled, int carbonFootprint, String loanState) {
        this.adressFeather = adressFeather;
        this.date = date;
        this.kmsTraveled = kmsTraveled;
        this.carbonFootprint = carbonFootprint;
        this.loanState = loanState;
    }

    public String getAdressFeather() {
        return adressFeather;
    }

    public String getDate() {
        return date;
    }

    public int getKmsTraveled() {
        return kmsTraveled;
    }

    public int getCarbonFootprint() {
        return carbonFootprint;
    }

    public String getLoanState() {
        return loanState;
    }
}
