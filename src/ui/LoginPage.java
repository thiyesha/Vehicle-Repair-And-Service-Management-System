package ui;

import model.User;
import dao.UserDao;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Image backgroundImage;

    public LoginPage() {
        setTitle("Vehicle Repair and service management System - Login");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load background image
        backgroundImage = new ImageIcon("src/images/login_bg.jpg").getImage();

        // Create a panel with background image
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        // Create login panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        loginPanel.setBackground(new Color(255, 255, 255, 200));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'>VEHICLE REPAIR AND SERVICE<br>MANAGEMENT SYSTEM</div></html>", JLabel.CENTER);

        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 0, 128));
        loginPanel.add(titleLabel, gbc);

        // Username
        gbc.gridy++;
        gbc.gridwidth = 1;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        loginPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        loginPanel.add(passwordField, gbc);

        // Login Button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 100, 0));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(this::performLogin);
        loginPanel.add(loginButton, gbc);

        backgroundPanel.add(loginPanel);
        add(backgroundPanel);
    }

    private void performLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        UserDao userDao = new UserDao();
        try {
            User user = userDao.authenticate(username, password);
            if (user != null) {
                dispose();
                new FunctionSelectionPage(user).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}