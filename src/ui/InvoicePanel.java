package ui;

import model.*;
import dao.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

public class InvoicePanel extends JPanel {
    private JTable invoiceTable;
    private DefaultTableModel model;
    private final InvoiceDao invoiceDao;
    private final CustomerDao customerDao;
    private final ServiceDao serviceDao;
    private final InventoryDao inventoryDao;

    public InvoicePanel() {
        this.invoiceDao = new InvoiceDao();
        this.customerDao = new CustomerDao();
        this.serviceDao = new ServiceDao();
        this.inventoryDao = new InventoryDao();

        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));
        initUI();
        refreshInvoiceTable();
    }

    private void initUI() {
        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("INVOICE MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Table Setup
        String[] columns = {"ID", "Customer", "Services", "Items", "Total", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        invoiceTable = new JTable(model);
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoiceTable.setRowHeight(30);
        invoiceTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton createButton = createStyledButton("Create New Invoice", new Color(46, 204, 113));
        createButton.addActionListener(this::handleCreateInvoice);

        JButton payButton = createStyledButton("Mark as Paid", new Color(52, 152, 219));
        payButton.addActionListener(this::handleMarkAsPaid);

        JButton refreshButton = createStyledButton("Refresh", new Color(149, 165, 166));
        refreshButton.addActionListener(e -> refreshInvoiceTable());

        buttonPanel.add(refreshButton);
        buttonPanel.add(createButton);
        buttonPanel.add(payButton);

        // Assembly
        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(invoiceTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshInvoiceTable() {
        try {
            model.setRowCount(0); // Clear existing data

            List<Invoice> invoices = invoiceDao.getAllInvoices();
            for (Invoice invoice : invoices) {
                // Handle potential null customer
                String customerName = (invoice.getCustomer() != null)
                        ? invoice.getCustomer().getName()
                        : "Unknown Customer";

                model.addRow(new Object[]{
                        invoice.getId(),
                        customerName,
                        invoice.getServices().size() + " services",
                        invoice.getUsedItems().size() + " items",
                        String.format("$%.2f", invoice.getTotal()),
                        invoice.getStatus()

                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load invoices: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCreateInvoice(ActionEvent e) {
        try {
            // Get required data
            List<Customer> customers = customerDao.getAllCustomers();
            List<Service> services = serviceDao.getAllServices();
            List<InventoryItem> inventoryItems = inventoryDao.getAllItems();

            if (customers.isEmpty() || services.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        customers.isEmpty() ? "No customers available" : "No services available");
                return;
            }

            // Create selection dialog
            JTabbedPane tabbedPane = new JTabbedPane();

            // Customer selection
            JList<Customer> customerList = new JList<>(customers.toArray(new Customer[0]));
            customerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tabbedPane.addTab("Customer", new JScrollPane(customerList));

            // Service selection
            JList<Service> serviceList = new JList<>(services.toArray(new Service[0]));
            serviceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            tabbedPane.addTab("Services", new JScrollPane(serviceList));

            // Inventory items selection
            JList<InventoryItem> itemList = new JList<>(inventoryItems.toArray(new InventoryItem[0]));
            itemList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            tabbedPane.addTab("Items", new JScrollPane(itemList));

            if (JOptionPane.showConfirmDialog(this, tabbedPane, "Create New Invoice",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                Customer selectedCustomer = customerList.getSelectedValue();
                List<Service> selectedServices = serviceList.getSelectedValuesList();
                List<InventoryItem> selectedItems = itemList.getSelectedValuesList();

                if (selectedCustomer == null || selectedServices.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please select at least a customer and one service");
                    return;
                }

                // Calculate total
                double total = selectedServices.stream().mapToDouble(Service::getPrice).sum();
                total += selectedItems.stream().mapToDouble(item -> item.getUnitPrice()).sum();

                // Create and save invoice
                Invoice newInvoice = new Invoice(0, selectedCustomer, selectedServices, selectedItems, total, "PENDING");

                invoiceDao.addInvoice(newInvoice);
                refreshInvoiceTable();

                JOptionPane.showMessageDialog(this, "Invoice created successfully!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMarkAsPaid(ActionEvent e) {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice first");
            return;
        }

        try {
            int invoiceId = (int) invoiceTable.getValueAt(selectedRow, 0);
            invoiceDao.markAsPaid(invoiceId);
            refreshInvoiceTable();
            JOptionPane.showMessageDialog(this, "Invoice marked as paid!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    private JLabel createStatusLabel(String status) {
        JLabel label = new JLabel(status);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(status.equals("PAID") ? new Color(39, 174, 96) : new Color(192, 57, 43));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}