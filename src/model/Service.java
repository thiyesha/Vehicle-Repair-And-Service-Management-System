package model;

public class Service {
    private int id;
    private String name;
    private double price;
    private int duration_Minutes;

    public Service(int id, String name, double price, int duration_Minutes) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.duration_Minutes = duration_Minutes;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getDurationMinutes() { return duration_Minutes; }

    @Override
    public String toString() {
        return name + " ($" + price + ")";
    }
}