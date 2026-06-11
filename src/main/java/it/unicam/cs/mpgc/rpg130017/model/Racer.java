package it.unicam.cs.mpgc.rpg130017.model;

public class Racer {
    private String name;
    private Car car;

    public Racer() {}

    public Racer(String name, Car car) {
        this.name = name;
        this.car = car;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
