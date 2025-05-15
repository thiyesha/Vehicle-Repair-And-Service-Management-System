package model;

public class Vehicle {
    private String registrationNumber;
    private String make;
    private String model;
    private int year;

    public Vehicle(String registrationNumber, String make, String model, int year) {
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.year = year;
    }

    // Getters
    public String getRegistrationNumber() { return registrationNumber; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getYear() { return year; }
}