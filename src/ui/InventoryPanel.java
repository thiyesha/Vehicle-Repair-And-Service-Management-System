package ui;

import dao.InventoryDao;
import model.InventoryItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class InventoryPanel extends JPanel {

    private final InventoryDao inventoryDao;
    private JTable table;
    private InventoryTableModel tableModel;
    private final boolean isEditable;

    public InventoryPanel(boolean isEditable) {
        this.inventoryDao = new InventoryDao();
        this.isEditable = isEditable;

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 248, 255)); // Light blue/gray background
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        refreshInventory();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(70, 130, 180)); // Steel blue

        JLabel header = new JLabel("Inventory Management");
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerPanel.add(header);

        return headerPanel;
    }

    private void refreshInventory() {
        try {
            List<InventoryItem> items = inventoryDao.getAllItems();
            tableModel = new InventoryTableModel(items);
            table = new JTable(tableModel);
            table.setRowHeight(28);
            table.setFont(new Font("SansSerif", Font.PLAIN, 14));
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
            table.setGridColor(Color.LIGHT_GRAY);

            // Center cell contents
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
            JOptionPane.showMessageDialog(this, "Failed to load inventory.");
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(new Color(245, 248, 255));

        JButton addButton = new JButton("Add Item");
        styleButton(addButton);
        addButton.addActionListener(e -> {
            try {
                String name = JOptionPane.showInputDialog(this, "Item Name:");
                int quantity = Integer.parseInt(JOptionPane.showInputDialog(this, "Quantity:"));
                double price = Double.parseDouble(JOptionPane.showInputDialog(this, "Unit Price:"));

                InventoryItem newItem = new InventoryItem(0, name, quantity, price);
                inventoryDao.addItem(newItem);
                refreshInventory();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Invalid input or failed to add item.");
            }
        });

        JButton updateButton = new JButton("Update Selected");
        styleButton(updateButton);
        updateButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                InventoryItem item = tableModel.getItemAt(selectedRow);
                try {
                    String name = JOptionPane.showInputDialog(this, "New Name:", item.getName());
                    int quantity = Integer.parseInt(JOptionPane.showInputDialog(this, "New Quantity:", item.getQuantity()));
                    double price = Double.parseDouble(JOptionPane.showInputDialog(this, "New Price:", item.getUnitPrice()));

                    inventoryDao.updateItem(item.getId(), name, quantity, price);
                    refreshInventory();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Invalid input or failed to update item.");
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

    private static class InventoryTableModel extends AbstractTableModel {
        private final List<InventoryItem> items;
        private final String[] columnNames = {"ID", "Name", "Quantity", "Unit Price"};

        public InventoryTableModel(List<InventoryItem> items) {
            this.items = items;
        }

        public InventoryItem getItemAt(int row) {
            return items.get(row);
        }

        @Override
        public int getRowCount() {
            return items.size();
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
            InventoryItem item = items.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.getId();
                case 1 -> item.getName();
                case 2 -> item.getQuantity();
                case 3 -> item.getUnitPrice();
                default -> null;
            };
        }
    }
}
