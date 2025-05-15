package model;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class Invoice {
    private int id;
    private Customer customer;
    private List<Service> services;
    private List<InventoryItem> usedItems;
    private double total;
    private String status;
    private Date invoiceDate;

    public Invoice(int id, Customer customer, List<Service> services,
                   List<InventoryItem> usedItems, double total, String status) {
        this.id = id;
        this.customer = customer;
        this.services = (services != null) ? new ArrayList<>(services) : new ArrayList<>();
        this.usedItems = (usedItems != null) ? new ArrayList<>(usedItems) : new ArrayList<>();
        this.total = total;
        this.status = (status != null) ? status : "PENDING";
    }


    // ✅ Added no-arg constructor for flexibility
    public Invoice() {
        this.services = new ArrayList<>();
        this.usedItems = new ArrayList<>();
        this.status = "PENDING";
    }



    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }  // ✅ Added setter

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }  // ✅ Added setter

    public List<Service> getServices() { return new ArrayList<>(services); }
    public List<InventoryItem> getUsedItems() { return new ArrayList<>(usedItems); }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }  // ✅ Added setter

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }  // ✅ Added setter

    public void markAsPaid() { this.status = "PAID"; }

    @Override
    public String toString() {
        return "Invoice #" + id + " - " + customer.getName() + " ($" + total + ")";
    }
}
