package ui;

import model.User;
import model.ShopOwner;
import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {
    private User currentUser;
    private Image backgroundImage;

    public Dashboard(User user, String selectedFunction) {
        this.currentUser = user;
        setTitle("Vehicle Repair System - " + selectedFunction.toUpperCase());
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load background image
        backgroundImage = new ImageIcon("src/images/background2.jpg").getImage();

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
        JLabel titleLabel = new JLabel(selectedFunction.toUpperCase() + " MANAGEMENT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        switch (selectedFunction.toLowerCase()) {
            case "inventory":
                contentPanel.add(new InventoryPanel(user instanceof ShopOwner), BorderLayout.CENTER);
                break;
            case "customers":
                contentPanel.add(new CustomerPanel(user instanceof ShopOwner), BorderLayout.CENTER);
                break;
            case "reports":
                contentPanel.add(new ReportsPanel(), BorderLayout.CENTER);
                break;
            case "appointments":
                contentPanel.add(new AppointmentPanel(), BorderLayout.CENTER);
                break;
            case "invoices":
                contentPanel.add(new InvoicePanel(), BorderLayout.CENTER);
                break;
            case "services":
                contentPanel.add(new ServicePanel(user instanceof ShopOwner), BorderLayout.CENTER);
                break;

        }

        // Back button
        JButton backButton = new JButton("Back to Menu");
        backButton.setBackground(Color.BLUE);
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> {
            dispose();
            new FunctionSelectionPage(currentUser).setVisible(true);
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(backButton);

        backgroundPanel.add(headerPanel, BorderLayout.NORTH);
        backgroundPanel.add(contentPanel, BorderLayout.CENTER);
        backgroundPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(backgroundPanel);
    }
}