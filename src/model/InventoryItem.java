package model;

public class InventoryItem {
    private int id;
    private String name;
    private int quantity;
    private double unitPrice;

    public InventoryItem(int id, String name, int quantity, double unitPrice) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Methods
    public void updateQuantity(int change) {
        this.quantity += change;
        if (this.quantity < 0) this.quantity = 0;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }

    @Override
    public String toString() {
        return name + " (Qty: " + quantity + ", $" + unitPrice + ")";
    }
}