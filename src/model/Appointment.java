package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Appointment {
    private int id;
    private Customer customer;
    private Service service;
    private LocalDateTime dateTime;
    private String status;

    public Appointment(int id, Customer customer, Service service, LocalDateTime dateTime,String status) {
        this.id = id;
        this.customer = customer;
        this.service = service;
        this.dateTime = dateTime;
        this.status = "SCHEDULED";
    }

    // Getters and setters
    public int getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Service getService() { return service; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return customer.getName() + " - " + service.getName() + " (" +
                dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")) + ")";
    }
}