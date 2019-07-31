package com.example.bicitec_project.Classes;

public class Station {
    private String Name;
    private int Space;

    public Station(String name, int space) {
        Name = name;
        Space = space;
    }

    public String getName() {
        return Name;
    }

    public int getSpace() {
        return Space;
    }
}
