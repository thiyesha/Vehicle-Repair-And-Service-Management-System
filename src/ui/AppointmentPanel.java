package ui;

import model.*;
import dao.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentPanel extends JPanel {
    private JTable appointmentTable;
    private final AppointmentDao appointmentDao = new AppointmentDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final ServiceDao serviceDao = new ServiceDao();

    public AppointmentPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240)); // Match background
        initComponents();
        loadAppointments();
    }

    private void initComponents() {
        // Header panel like InvoicePanel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("APPOINTMENT MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Table setup
        String[] columns = {"ID", "Customer", "Service", "Date/Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentTable = new JTable(model);
        appointmentTable.setRowHeight(30);
        appointmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        appointmentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Button panel styled
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton addButton = createStyledButton("Book Appointment", new Color(46, 204, 113));
        addButton.addActionListener(this::bookAppointment);

        JButton cancelButton = createStyledButton("Cancel Appointment", new Color(231, 76, 60));
        cancelButton.addActionListener(this::cancelAppointment);

        JButton refreshButton = createStyledButton("Refresh", new Color(149, 165, 166));
        refreshButton.addActionListener(e -> loadAppointments());

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        // Final layout
        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(appointmentTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadAppointments() {
        try {
            DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
            model.setRowCount(0); // Clear existing data

            List<Appointment> appointments = appointmentDao.getAllAppointments();
            for (Appointment appt : appointments) {
                model.addRow(new Object[]{
                        appt.getId(),
                        appt.getCustomer().getName(),
                        appt.getService().getName(),
                        appt.getDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")),
                        appt.getStatus()
                });
            }
        } catch (SQLException e) {
            showError("Failed to load appointments: " + e.getMessage());
        }
    }

    private void bookAppointment(ActionEvent e) {
        try {
            List<Customer> customers = customerDao.getAllCustomers();
            List<Service> services = serviceDao.getAllServices();

            if (customers.isEmpty() || services.isEmpty()) {
                showError(customers.isEmpty() ? "No customers available" : "No services available");
                return;
            }

            Customer selectedCustomer = (Customer) JOptionPane.showInputDialog(
                    this, "Select Customer:", "Book Appointment",
                    JOptionPane.PLAIN_MESSAGE, null,
                    customers.toArray(), customers.get(0));
            if (selectedCustomer == null) return;

            Service selectedService = (Service) JOptionPane.showInputDialog(
                    this, "Select Service:", "Book Appointment",
                    JOptionPane.PLAIN_MESSAGE, null,
                    services.toArray(), services.get(0));
            if (selectedService == null) return;

            JPanel inputPanel = new JPanel(new GridLayout(0, 2));
            JTextField dateField = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            JTextField timeField = new JTextField("09:00");
            inputPanel.add(new JLabel("Date (YYYY-MM-DD):"));
            inputPanel.add(dateField);
            inputPanel.add(new JLabel("Time (HH:MM):"));
            inputPanel.add(timeField);

            if (JOptionPane.showConfirmDialog(this, inputPanel, "Enter Date/Time",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                LocalDateTime dateTime = LocalDateTime.parse(dateField.getText() + "T" + timeField.getText());
                Appointment newAppt = new Appointment(0, selectedCustomer, selectedService,
                        dateTime, "SCHEDULED");

                appointmentDao.addAppointment(newAppt);
                loadAppointments();

                JOptionPane.showMessageDialog(this, "Appointment booked successfully!");
            }
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private void cancelAppointment(ActionEvent e) {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an appointment first");
            return;
        }

        try {
            int appointmentId = (int) appointmentTable.getValueAt(selectedRow, 0);
            appointmentDao.updateAppointmentStatus(appointmentId, "CANCELLED");

            ((DefaultTableModel) appointmentTable.getModel()).setValueAt("CANCELLED", selectedRow, 4);
            JOptionPane.showMessageDialog(this, "Appointment cancelled!");
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
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

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
