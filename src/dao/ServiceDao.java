package dao;


import model.Service;
import database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDao {

    public List<Service> getAllServices() throws SQLException {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Service service = new Service(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("duration_minutes")
                );
                services.add(service);
            }
        }

        return services;
    }

    public void addService(Service service) throws SQLException {
        String sql = "INSERT INTO services (name, price, duration_minutes) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, service.getName());
            stmt.setDouble(2, service.getPrice());
            stmt.setInt(3, service.getDurationMinutes());
            stmt.executeUpdate();
        }
    }
    public void updateService(int id, String name, double price, int durationMinutes) throws SQLException {
        String sql = "UPDATE services SET name = ?, price = ?, duration_minutes = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, durationMinutes);
            stmt.setInt(4, id);
            stmt.executeUpdate();
        }
    }

}
