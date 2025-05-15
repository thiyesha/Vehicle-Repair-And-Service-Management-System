package dao;

import database.DatabaseConnection;
import model.Appointment;
import model.Customer;
import model.Service;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDao {
    public int getAppointmentCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    // Add new appointment
    public void addAppointment(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO appointments (customer_id, service_id, date_time, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, appointment.getCustomer().getId());
            stmt.setInt(2, appointment.getService().getId());
            stmt.setTimestamp(3, Timestamp.valueOf(appointment.getDateTime()));
            stmt.setString(4, appointment.getStatus());
            stmt.executeUpdate();


            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {

                   appointment.setId(rs.getInt(1));
                }
            }
        }
    }

    // Get all appointments with customer/service details
    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = """
            SELECT a.id, a.date_time, a.status,
                   c.id AS customer_id, c.name AS customer_name,
                   s.id AS service_id, s.name AS service_name,
                   s.price, s.duration_minutes
        
            FROM appointments a
            JOIN customers c ON a.customer_id = c.id
            JOIN services s ON a.service_id = s.id
            ORDER BY a.date_time
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        "",         // phone
                        "",         // idCardNumber
                        null        // vehicle
                );




            Service service = new Service(
                        rs.getInt("service_id"),
                        rs.getString("service_name"),
                        rs.getDouble("price"),
                        rs.getInt("duration_minutes")

            );

                appointments.add(new Appointment(
                        rs.getInt("id"),
                        customer,
                        service,
                        rs.getTimestamp("date_time").toLocalDateTime(),
                        rs.getString("status")
                ));
            }
        }
        return appointments;
    }

    // Update appointment status
    public void updateAppointmentStatus(int appointmentId, String newStatus) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, appointmentId);
            stmt.executeUpdate();
        }
    }

    // Get appointments by customer ID (simplified version)
    public List<Appointment> getAppointmentsByCustomer(int customerId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = """
            SELECT a.id, a.service_id, a.date_time, a.status,
                   s.name AS service_name, s.price, s.duration_minutes
            FROM appointments a
            JOIN services s ON a.service_id = s.id
            WHERE a.customer_id = ?
            ORDER BY a.date_time
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Service service = new Service(
                            rs.getInt("service_id"),
                            rs.getString("service_name"),
                            rs.getDouble("price"),
                            rs.getInt("duration_minutes")

                    );

                    appointments.add(new Appointment(
                            rs.getInt("id"),
                            null, // Omit customer to avoid circular reference
                            service,
                            rs.getTimestamp("date_time").toLocalDateTime(),
                            rs.getString("status")
                    ));
                }
            }
        }
        return appointments;
    }

    // Check for overlapping appointments
    public boolean hasOverlappingAppointment(LocalDateTime startTime, int durationMinutes) throws SQLException {
        String sql = """
            SELECT COUNT(*) 
            FROM appointments 
            WHERE date_time BETWEEN ? AND ?
            AND status != 'CANCELLED'
            """;

        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startTime));
            stmt.setTimestamp(2, Timestamp.valueOf(endTime));

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}