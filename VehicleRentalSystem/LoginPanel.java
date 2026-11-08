package com.vehiclerental.ui.auth;

import com.vehiclerental.dao.UserDAO;
import com.vehiclerental.exceptions.AuthenticationException;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.Admin;
import com.vehiclerental.models.Customer;
import com.vehiclerental.models.RentalAgency;
import com.vehiclerental.models.User;
import com.vehiclerental.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private final UserDAO userDAO;
    private String currentRole = "CUSTOMER";

    private JLabel titleLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton backButton;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userDAO = new UserDAO();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Top Title
        titleLabel = new JLabel("Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // Center Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.BOLD, 15);
        Font fieldFont = new Font("Arial", Font.PLAIN, 15);

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel uLbl = new JLabel("Username:");
        uLbl.setFont(labelFont);
        formPanel.add(uLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        usernameField = new JTextField(18);
        usernameField.setFont(fieldFont);
        usernameField.setPreferredSize(new Dimension(240, 36));
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel pLbl = new JLabel("Password:");
        pLbl.setFont(labelFont);
        formPanel.add(pLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        passwordField = new JPasswordField(18);
        passwordField.setFont(fieldFont);
        passwordField.setPreferredSize(new Dimension(240, 36));
        formPanel.add(passwordField, gbc);

        // Action Buttons
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 14));
        
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 15));
        loginButton.setPreferredSize(new Dimension(110, 38));

        registerButton = new JButton("Create New Account");
        registerButton.setFont(new Font("Arial", Font.BOLD, 15));
        registerButton.setPreferredSize(new Dimension(220, 38));

        backButton = new JButton("Back to Roles");
        backButton.setFont(new Font("Arial", Font.PLAIN, 15));
        backButton.setPreferredSize(new Dimension(140, 38));

        btnPanel.add(loginButton);
        btnPanel.add(registerButton);
        btnPanel.add(backButton);

        formPanel.add(btnPanel, gbc);

        // Demo Helper Bar (Quick Fill for academic evaluation/testing)
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton demoFillBtn = new JButton("⚡ Quick Demo Fill");
        demoFillBtn.setFont(new Font("Arial", Font.PLAIN, 13));
        demoFillBtn.setToolTipText("Auto-fill standard demo credentials for the current role");
        demoFillBtn.addActionListener(e -> fillDemoCredentials());

        JLabel demoHint = new JLabel("<html><font color='#64748B'>Demo: <b>admin</b>/admin123 &bull; <b>john</b>/john123 &bull; <b>apex</b>/apex123</font></html>");
        demoHint.setFont(new Font("Arial", Font.PLAIN, 13));

        demoPanel.add(demoFillBtn);
        demoPanel.add(demoHint);
        formPanel.add(demoPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Events
        loginButton.addActionListener(e -> performLogin());
        registerButton.addActionListener(e -> mainFrame.showRegistrationForRole(currentRole));
        backButton.addActionListener(e -> mainFrame.showRoleSelection());
    }

    private void fillDemoCredentials() {
        if ("ADMIN".equalsIgnoreCase(currentRole)) {
            usernameField.setText("admin");
            passwordField.setText("admin123");
        } else if ("AGENCY".equalsIgnoreCase(currentRole)) {
            usernameField.setText("apex");
            passwordField.setText("apex123");
        } else {
            usernameField.setText("john");
            passwordField.setText("john123");
        }
    }

    public void setRole(String role) {
        this.currentRole = role;
        usernameField.setText("");
        passwordField.setText("");
        String roleName = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
        if ("AGENCY".equalsIgnoreCase(role)) roleName = "Rental Agency";
        titleLabel.setText(roleName + " Login");
        registerButton.setText("Create New " + roleName);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.authenticate(username, password, currentRole);
            if (user instanceof Admin admin) {
                mainFrame.openAdminDashboard(admin);
            } else if (user instanceof Customer customer) {
                mainFrame.openCustomerDashboard(customer);
            } else if (user instanceof RentalAgency agency) {
                mainFrame.openAgencyDashboard(agency);
            }
        } catch (AuthenticationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Authentication Failed", JOptionPane.ERROR_MESSAGE);
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
