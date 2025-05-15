package ui;

import dao.ServiceDao;
import model.Service;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ServicePanel extends JPanel {

    private final ServiceDao serviceDao;
    private JTable table;
    private ServiceTableModel tableModel;
    private final boolean isEditable;

    public ServicePanel(boolean isEditable) {
        this.serviceDao = new ServiceDao();
        this.isEditable = isEditable;

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 248, 255));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        refreshServices();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(70, 130, 180)); // Steel blue

        JLabel header = new JLabel("Service Management");
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerPanel.add(header);

        return headerPanel;
    }

    private void refreshServices() {
        try {
            List<Service> services = serviceDao.getAllServices();
            tableModel = new ServiceTableModel(services);
            table = new JTable(tableModel);
            table.setRowHeight(28);
            table.setFont(new Font("SansSerif", Font.PLAIN, 14));
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
            table.setGridColor(Color.LIGHT_GRAY);

            // Center align cells
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

            table.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()) {
                @Override
                public boolean isCellEditable(java.util.EventObject e) {
                    int row = table.getSelectedRow();
                    int column = table.getSelectedColumn();
                    return isEditable && column > 0;
                }
            });

            removeAll();
            add(createHeader(), BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);

            if (isEditable) {
                add(createButtonPanel(), BorderLayout.SOUTH);
            }

            revalidate();
            repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load services.");
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(new Color(245, 248, 255));

        JButton addButton = new JButton("Add Service");
        styleButton(addButton);
        addButton.addActionListener(e -> {
            try {
                String name = JOptionPane.showInputDialog(this, "Service Name:");
                double price = Double.parseDouble(JOptionPane.showInputDialog(this, "Price:"));
                int duration = Integer.parseInt(JOptionPane.showInputDialog(this, "Duration (in minutes):"));

                Service newService = new Service(0, name, price, duration);
                serviceDao.addService(newService);
                refreshServices();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Invalid input or failed to add service.");
            }
        });

        JButton updateButton = new JButton("Update Selected");
        styleButton(updateButton);
        updateButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                Service service = tableModel.getServiceAt(selectedRow);
                try {
                    String name = JOptionPane.showInputDialog(this, "New Name:", service.getName());
                    double price = Double.parseDouble(JOptionPane.showInputDialog(this, "New Price:", service.getPrice()));
                    int duration = Integer.parseInt(JOptionPane.showInputDialog(this, "New Duration (minutes):", service.getDurationMinutes()));

                    serviceDao.updateService(service.getId(), name, price, duration);
                    refreshServices();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Invalid input or failed to update service.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row first.");
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        return buttonPanel;
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private static class ServiceTableModel extends AbstractTableModel {
        private final List<Service> services;
        private final String[] columnNames = {"ID", "Name", "Price", "Duration (mins)"};

        public ServiceTableModel(List<Service> services) {
            this.services = services;
        }

        public Service getServiceAt(int row) {
            return services.get(row);
        }

        @Override
        public int getRowCount() {
            return services.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Service s = services.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> s.getId();
                case 1 -> s.getName();
                case 2 -> s.getPrice();
                case 3 -> s.getDurationMinutes();
                default -> null;
            };
        }
    }
}
