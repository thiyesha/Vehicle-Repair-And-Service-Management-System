package ui;

import dao.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class ReportsPanel extends JPanel {
    private final CustomerDao customerDao;
    private final AppointmentDao appointmentDao;
    private final InvoiceDao invoiceDao;
    private final InventoryDao inventoryDao;

    public ReportsPanel() {
        this.customerDao = new CustomerDao();
        this.appointmentDao = new AppointmentDao();
        this.invoiceDao = new InvoiceDao();
        this.inventoryDao = new InventoryDao();

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 248, 255)); // Light gray/blue
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        generateReport();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(70, 130, 180)); // Steel blue

        JLabel titleLabel = new JLabel("System Summary Report");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerPanel.add(titleLabel);

        return headerPanel;
    }

    private void generateReport() {
        try {
            JTextArea reportArea = new JTextArea();
            reportArea.setEditable(false);
            reportArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            reportArea.setBackground(Color.WHITE);
            reportArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            StringBuilder report = new StringBuilder();
            report.append("=== REPAIR SHOP REPORT ===\n\n");
            report.append("Customers: ").append(customerDao.getCustomerCount()).append("\n");
            report.append("Appointments: ").append(appointmentDao.getAppointmentCount()).append("\n");
            report.append("Invoices: ").append(invoiceDao.getInvoiceCount()).append("\n");
            report.append("Pending Invoices: ").append(invoiceDao.getPendingInvoiceCount()).append("\n");
            report.append("Paid Invoices: ").append(invoiceDao.getPaidInvoiceCount()).append("\n\n");
            report.append("Inventory Items: ").append(inventoryDao.getItemCount()).append("\n");
            report.append("Low Stock Items (Qty < 5): ").append(inventoryDao.getLowStockCount()).append("\n");

            reportArea.setText(report.toString());

            JScrollPane scrollPane = new JScrollPane(reportArea);
            scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            scrollPane.setPreferredSize(new Dimension(600, 400));
            add(scrollPane, BorderLayout.CENTER);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to generate report: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
