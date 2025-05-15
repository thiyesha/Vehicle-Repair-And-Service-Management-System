package ui;

import model.*;
import dao.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

public class CustomerPanel extends JPanel {
    private JTable customerTable;
    private final CustomerDao customerDao;
    private final AppointmentDao appointmentDao;
    private final InvoiceDao invoiceDao;
    private final boolean canEdit;

    public CustomerPanel(boolean canEdit) {
        this.canEdit = canEdit;
        this.customerDao = new CustomerDao();
        this.appointmentDao = new AppointmentDao();
        this.invoiceDao = new InvoiceDao();

        setLayout(new BorderLayout());
        setBackground(Color.decode("#f0f4f7"));

        add(createTitlePanel(), BorderLayout.NORTH);
        initTable();
        initButtons();
        refreshCustomers();
    }

    private JPanel createTitlePanel() {
        JLabel titleLabel = new JLabel("Customer Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(40, 40, 40));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.decode("#dce3ea"));
        panel.setBorder(new EmptyBorder(10, 15, 10, 10));
        panel.add(titleLabel);

        return panel;
    }

    private void initTable() {
        String[] columnNames = {
                "ID", "Name", "Phone", "ID Card Number",
                "Registration Number", "Make", "Model", "Year"
        };

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return canEdit && column > 0;
            }
        };
        customerTable = new JTable(model);
        customerTable.setRowHeight(25);
        customerTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        customerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        customerTable.getTableHeader().setBackground(new Color(200, 221, 242));
        customerTable.setGridColor(Color.LIGHT_GRAY);
        customerTable.setSelectionBackground(new Color(180, 200, 250));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < columnNames.length; i++) {
            customerTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.decode("#f0f4f7"));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton addButton = createStyledButton("Add Customer", new Color(0, 153, 76));
        addButton.addActionListener(this::addCustomer);
        buttonPanel.add(addButton);

        if (canEdit) {
            JButton saveButton = createStyledButton("Save Changes", new Color(255, 153, 0));
            saveButton.addActionListener(this::saveChanges);
            buttonPanel.add(saveButton);
        }

        JButton historyButton = createStyledButton("View History", new Color(51, 102, 255));
        historyButton.addActionListener(this::viewHistory);
        buttonPanel.add(historyButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void refreshCustomers() {
        try {
            DefaultTableModel model = (DefaultTableModel) customerTable.getModel();
            model.setRowCount(0);

            List<Customer> customers = customerDao.getAllCustomers();
            for (Customer customer : customers) {
                Vehicle v = customer.getVehicle();
                model.addRow(new Object[]{
                        customer.getId(),
                        customer.getName(),
                        customer.getPhone(),
                        customer.getIdCardNumber(),
                        v != null ? v.getRegistrationNumber() : "N/A",
                        v != null ? v.getMake() : "N/A",
                        v != null ? v.getModel() : "N/A",
                        v != null ? v.getYear() : "N/A"
                });

            }

        } catch (SQLException e) {
            showError("Failed to load customers: " + e.getMessage());
        }
    }

    private void addCustomer(ActionEvent e) {
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField idCardField = new JTextField();
        JTextField vehicleRegField = new JTextField();
        JTextField makeField = new JTextField();
        JTextField modelField = new JTextField();
        JTextField yearField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Name:")); panel.add(nameField);
        panel.add(new JLabel("Phone:")); panel.add(phoneField);
        panel.add(new JLabel("ID Card Number:")); panel.add(idCardField);
        panel.add(new JLabel("Vehicle Registration:")); panel.add(vehicleRegField);
        panel.add(new JLabel("Make:")); panel.add(makeField);
        panel.add(new JLabel("Model:")); panel.add(modelField);
        panel.add(new JLabel("Year:")); panel.add(yearField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Add New Customer",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                int year = Integer.parseInt(yearField.getText());
                Vehicle vehicle = new Vehicle(
                        vehicleRegField.getText(),
                        makeField.getText(),
                        modelField.getText(),
                        year
                );

                Customer newCustomer = new Customer(
                        0,
                        nameField.getText(),
                        phoneField.getText(),
                        idCardField.getText(),
                        vehicle
                );

                customerDao.addCustomer(newCustomer);
                refreshCustomers();
                JOptionPane.showMessageDialog(this, "Customer added successfully!");
            } catch (NumberFormatException ex) {
                showError("Invalid year format");
            } catch (SQLException ex) {
                showError("Failed to add customer: " + ex.getMessage());
            }
        }
    }

    private void saveChanges(ActionEvent e) {
        try {
            DefaultTableModel model = (DefaultTableModel) customerTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                int id = (int) model.getValueAt(i, 0);
                String name = (String) model.getValueAt(i, 1);
                String phone = (String) model.getValueAt(i, 2);
                String idCard = (String) model.getValueAt(i, 3);

                Customer customer = new Customer(id, name, phone, idCard, null);
                customerDao.updateCustomer(customer);
            }
            JOptionPane.showMessageDialog(this, "Changes saved successfully!");
        } catch (SQLException ex) {
            showError("Failed to save changes: " + ex.getMessage());
        }
    }

    private void viewHistory(ActionEvent e) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a customer first");
            return;
        }

        try {
            int customerId = (int) customerTable.getValueAt(selectedRow, 0);
            Customer customer = customerDao.getCustomerById(customerId);
            List<Appointment> appointments = appointmentDao.getAppointmentsByCustomer(customerId);
            List<Invoice> invoices = invoiceDao.getInvoicesByCustomer(customerId);

            StringBuilder history = new StringBuilder();
            history.append("Customer: ").append(customer.getName()).append("\n");
            history.append("Vehicle: ").append(customer.getVehicle() != null ?
                    customer.getVehicle().getRegistrationNumber() : "N/A").append("\n\n");

            history.append("Appointments:\n");
            if (appointments.isEmpty()) {
                history.append("- No appointments found\n");
            } else {
                for (Appointment a : appointments) {
                    history.append("- ").append(a.getService() != null ?
                                    a.getService().getName() : "Unknown Service")
                            .append(" (").append(a.getDateTime()).append(")\n");
                }
            }

            history.append("\nInvoices:\n");
            if (invoices.isEmpty()) {
                history.append("- No invoices found\n");
            } else {
                for (Invoice i : invoices) {
                    history.append("- Invoice #").append(i.getId())
                            .append(" ($").append(i.getTotal()).append(")\n");
                }
            }

            JTextArea textArea = new JTextArea(history.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            JOptionPane.showMessageDialog(this, scrollPane,
                    "Customer History", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
