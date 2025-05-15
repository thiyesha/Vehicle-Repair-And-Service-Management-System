package dao;

import database.DatabaseConnection;
import model.Customer;
import model.Vehicle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CustomerDao {

    // Get total number of customers
    public int getCustomerCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Get all customers with their vehicles
    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = """
                SELECT c.*, v.registration_number, v.make, v.model, v.year 
                FROM customers c
                LEFT JOIN vehicles v ON c.id = v.customer_id
                ORDER BY c.name
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vehicle vehicle = null;
                if (rs.getString("registration_number") != null) {
                    vehicle = new Vehicle(
                            rs.getString("registration_number"),
                            rs.getString("make"),
                            rs.getString("model"),
                            rs.getInt("year")
                    );
                }
                customers.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("id_card_number"),
                        vehicle
                ));
            }
        }
        return customers;
    }

    // Add a new customer and their vehicle
    public void addCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, phone, id_card_number) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {


            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getIdCardNumber());
            stmt.executeUpdate();

            // Link vehicle to customer if exists
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int customerId = rs.getInt(1);
                    if (customer.getVehicle() != null) {
                        saveVehicle(customerId, customer.getVehicle());
                    }
                }
            }
        }
    }

    // Update customer information
    public void updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET name = ?, phone = ?, id_card_number = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(4, customer.getId());
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getIdCardNumber());

            stmt.executeUpdate();

            // Update vehicle if exists
            if (customer.getVehicle() != null) {
                updateVehicle(customer.getId(), customer.getVehicle());
            }
        }
    }

    // Get a single customer by ID with their vehicle
    public Customer getCustomerById(int customerId) throws SQLException {
        String sql = """
                SELECT c.*, v.registration_number, v.make, v.model, v.year 
                FROM customers c
                LEFT JOIN vehicles v ON c.id = v.customer_id
                WHERE c.id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Vehicle vehicle = null;
                if (rs.getString("registration_number") != null) {
                    vehicle = new Vehicle(
                            rs.getString("registration_number"),
                            rs.getString("make"),
                            rs.getString("model"),
                            rs.getInt("year")
                    );
                }
                return new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("id_card_number"),
                        vehicle
                );
            }
        }
        throw new NoSuchElementException("Customer not found with ID: " + customerId);
    }

    // Save vehicle information for a customer
    private void saveVehicle(int customerId, Vehicle vehicle) throws SQLException {
        if (vehicle == null) return;

        String sql = "INSERT INTO vehicles (registration_number, make, model, year, customer_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicle.getRegistrationNumber());
            stmt.setString(2, vehicle.getMake());
            stmt.setString(3, vehicle.getModel());
            stmt.setInt(4, vehicle.getYear());
            stmt.setInt(5, customerId);
            stmt.executeUpdate();
        }
    }

    // Update vehicle information
    private void updateVehicle(int customerId, Vehicle vehicle) throws SQLException {
        String sql = """
        UPDATE vehicles 
        SET make = ?, model = ?, year = ? 
        WHERE customer_id = ? AND registration_number = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vehicle.getMake());
            stmt.setString(2, vehicle.getModel());
            stmt.setInt(3, vehicle.getYear());
            stmt.setInt(4, customerId);
            stmt.setString(5, vehicle.getRegistrationNumber());

            stmt.executeUpdate();
        }
    }


    // Delete a customer
    public void deleteCustomer(int customerId) throws SQLException {
        // First delete the vehicle if exists
        String deleteVehicleSql = "DELETE FROM vehicles WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteVehicleSql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        }

        // Then delete the customer
        String deleteCustomerSql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteCustomerSql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        }
    }
}