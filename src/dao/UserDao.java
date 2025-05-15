package dao;

import database.DatabaseConnection;
import model.Cashier;
import model.ShopOwner;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    // Authenticate user (login)
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                if (dbPassword.equals(password)) { // In production, use BCrypt
                    int id = rs.getInt("id");                 // ← added
                    String name = rs.getString("name");       // ← added
                    String role = rs.getString("role");

                    // Return specific user type based on role
                    if ("SHOP_OWNER".equalsIgnoreCase(role)) {
                        return new ShopOwner(id, username, password, name, role);
                    } else if ("CASHIER".equalsIgnoreCase(role)) {
                        return new Cashier(id, username, password, name, role);
                    } else {
                        System.out.println("Unknown role in DB: " + role);
                        return null;
                    }

                }
            }
            return null; // Authentication failed
        }
    }

    // Get all users (for admin views)
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("role")
                ));
            }
        }
        return users;
    }
}