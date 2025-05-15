package dao;

import model.InventoryItem;
import database.DatabaseConnection;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDao {
    public int getItemCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inventory_items";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getLowStockCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inventory_items WHERE quantity < 5";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public List<InventoryItem> getAllItems() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory_items";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                InventoryItem item = new InventoryItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price")
                );
                items.add(item);
            }
        }
        return items;
    }

    public void addItem(InventoryItem item) throws SQLException {
        String sql = "INSERT INTO inventory_items (name, quantity, unit_price) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getName());
            stmt.setInt(2, item.getQuantity());
            stmt.setDouble(3, item.getUnitPrice());
            stmt.executeUpdate();
        }
    }

    public void updateItem(int id, String name, int quantity, double unitPrice) throws SQLException {
        String sql = "UPDATE inventory_items SET name = ?, quantity = ?, unit_price = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, quantity);
            stmt.setDouble(3, unitPrice);
            stmt.setInt(4, id);
            stmt.executeUpdate();
        }
    }
}
