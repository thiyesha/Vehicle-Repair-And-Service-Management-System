package ui;

import model.User;
import model.ShopOwner;
import model.Cashier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class FunctionSelectionPage extends JFrame {
    private User currentUser;
    private Image backgroundImage;

    public FunctionSelectionPage(User user) {
        this.currentUser = user;
        setTitle("Vehicle Repair System - Welcome " + user.getName());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load background image
        backgroundImage = new ImageIcon("src/images/background.jpg").getImage();

        // Create background panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getName() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        if (user instanceof ShopOwner) {
            addButton(buttonPanel, "Inventory Management", new Color(33, 150, 243) , e -> openInventory());
            addButton(buttonPanel, "Customer Management", new Color(33, 150, 243), e -> openCustomers());
            addButton(buttonPanel, "View Reports", new Color(33, 150, 243), e -> openReports());
            addButton(buttonPanel, "Add New Service", new Color(33, 150, 243), e -> openServices());

        } else if (user instanceof Cashier) {
            addButton(buttonPanel, "Appointment Management", new Color(33, 150, 243), e -> openAppointments());
            addButton(buttonPanel, "Customer Management", new Color(33, 150, 243), e -> openCustomers());
            addButton(buttonPanel, "Invoice Management", new Color(33, 150, 243), e -> openInvoices());
            addButton(buttonPanel, "View Inventory", new Color(33, 150, 243), e -> openInventory());
        }

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginPage().setVisible(true);
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(logoutButton);

        backgroundPanel.add(headerPanel, BorderLayout.NORTH);
        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);
        backgroundPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(backgroundPanel);
    }

    private void addButton(JPanel panel, String text, Color color, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.addActionListener(action);
        panel.add(button);
    }

    private void openInventory() {
        dispose();
        new Dashboard(currentUser, "inventory").setVisible(true);
    }

    private void openCustomers() {
        dispose();
        new Dashboard(currentUser, "customers").setVisible(true);
    }

    private void openReports() {
        dispose();
        new Dashboard(currentUser, "reports").setVisible(true);
    }

    private void openAppointments() {
        dispose();
        new Dashboard(currentUser, "appointments").setVisible(true);
    }

    private void openInvoices() {
        dispose();
        new Dashboard(currentUser, "invoices").setVisible(true);
    }
    private void openServices() {
        dispose();
        new Dashboard(currentUser,"services").setVisible(true);
    }

}