package com.example.bicitec_project;

public class User {
    private String email;
    private String password;
    private String genero;
    private int telefono;
    private int carnet;
    private String mac;
    private String estadoUsuario;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getGenero() {
        return genero;
    }

    public int getTelefono() {
        return telefono;
    }

    public int getCarnet() {
        return carnet;
    }

    public String getMac() {
        return mac;
    }

    public String getEstadoUsuario() {
        return estadoUsuario;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCarnet(int carnet) {
        this.carnet = carnet;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
    public void setEstadoUsuario(String estadoUsuario) {
        this.estadoUsuario = estadoUsuario;
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

}
