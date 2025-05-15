package dao;

import database.DatabaseConnection;
import model.Invoice;
import model.InventoryItem;
import model.Service;
import model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDao {
    public int getInvoiceCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM invoices";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getPendingInvoiceCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM invoices WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getPaidInvoiceCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM invoices WHERE status = 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    private final CustomerDao customerDao;
    private final ServiceDao serviceDao;
    private final InventoryDao inventoryDao;

    public InvoiceDao() {
        this.customerDao = new CustomerDao();
        this.serviceDao = new ServiceDao();
        this.inventoryDao = new InventoryDao();
    }

    // Create a new invoice
    public void addInvoice(Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoices (customer_id, total, status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, invoice.getCustomer().getId());
            stmt.setDouble(2, invoice.getTotal());
            stmt.setString(3, invoice.getStatus());
            stmt.executeUpdate();

            // Link services/items to invoice
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int invoiceId = rs.getInt(1);
                    linkServices(invoiceId, invoice.getServices());
                    linkItems(invoiceId, invoice.getUsedItems());
                }
            }
        }
    }

    // Get all invoices
    public List<Invoice> getAllInvoices() throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM invoices";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

                   while (rs.next()) {
                // Load customer - ensure this can't return null
                Customer customer = customerDao.getCustomerById(rs.getInt("customer_id"));
                if (customer == null) {
                    throw new SQLException("Customer not found for ID: " + rs.getInt("customer_id"));
                }
                       Invoice invoice = new Invoice(
                               rs.getInt("id"),
                               customer,
                               getServicesForInvoice(rs.getInt("id")),
                               getItemsForInvoice(rs.getInt("id")),
                               rs.getDouble("total"),
                               rs.getString("status")
                       );
                invoices.add(invoice);
            }
        }
        return invoices;
    }

    // Mark invoice as paid
    public void markAsPaid(int invoiceId) throws SQLException {
        String sql = "UPDATE invoices SET status = 'PAID' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, invoiceId);
            stmt.executeUpdate();
        }
    }

    // Get invoices by customer ID
    public List<Invoice> getInvoicesByCustomer(int customerId) throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Invoice invoice = new Invoice(
                        rs.getInt("id"),
                        customerDao.getCustomerById(customerId),
                        getServicesForInvoice(rs.getInt("id")),
                        getItemsForInvoice(rs.getInt("id")),
                        rs.getDouble("total"),
                        rs.getString("status")
                );
                invoices.add(invoice);
            }
        }
        return invoices;
    }

    // Helper method to get services for an invoice
    private List<Service> getServicesForInvoice(int invoiceId) throws SQLException {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.id, s.name, s.price, s.duration_minutes " +
                "FROM services s " +
                "JOIN invoice_services i ON s.id = i.service_id " +
                "WHERE i.invoice_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Service service = new Service(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"), // Ensure you're using 'price' from services table
                            rs.getInt("duration_minutes")
                    );
                    services.add(service);
                }
            }
        }
        return services;
    }


    // Helper method to get items for an invoice
    private List<InventoryItem> getItemsForInvoice(int invoiceId) throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT i.id, i.name, i.unit_price, inv.quantity_used FROM inventory_items i " +
                "JOIN invoice_inventory inv ON i.id = inv.inventory_id " +
                "WHERE inv.invoice_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InventoryItem item = new InventoryItem(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("unit_price"),
                            rs.getInt("quantity_used")
                    );
                    items.add(item);
                }
            }
        }
        return items;
    }

    // Link services to invoice
    private void linkServices(int invoiceId, List<Service> services) throws SQLException {
        String sql = "INSERT INTO invoice_services (invoice_id, service_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Service service : services) {
                stmt.setInt(1, invoiceId);
                stmt.setInt(2, service.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // Link items to invoice
    private void linkItems(int invoiceId, List<InventoryItem> items) throws SQLException {
        String sql = "INSERT INTO invoice_inventory (invoice_id, inventory_id, quantity_used) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (InventoryItem item : items) {
                stmt.setInt(1, invoiceId);
                stmt.setInt(2, item.getId());
                stmt.setInt(3, item.getQuantity());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}