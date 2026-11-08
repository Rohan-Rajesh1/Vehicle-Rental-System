package com.vehiclerental.ui.auth;

import com.vehiclerental.dao.UserDAO;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.Admin;
import com.vehiclerental.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class RegisterAdminPanel extends JPanel {
    private final MainFrame mainFrame;
    private final UserDAO userDAO;

    private JTextField fullNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField secretKeyField;

    public RegisterAdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userDAO = new UserDAO();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel titleLabel = new JLabel("Admin Registration", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        fullNameField = addRow(formPanel, gbc, "Full Name:", row++);
        usernameField = addRow(formPanel, gbc, "Username:", row++);

        gbc.gridx = 0; gbc.gridy = row;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(passLbl, gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        passwordField = new JPasswordField(18);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 15));
        passwordField.setPreferredSize(new Dimension(240, 34));
        formPanel.add(passwordField, gbc);

        emailField = addRow(formPanel, gbc, "Email Address:", row++);
        phoneField = addRow(formPanel, gbc, "Phone Number:", row++);

        gbc.gridx = 0; gbc.gridy = row;
        JLabel keyLbl = new JLabel("Admin Secret Key:");
        keyLbl.setFont(new Font("Arial", Font.BOLD, 15));
        formPanel.add(keyLbl, gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        secretKeyField = new JPasswordField(18);
        secretKeyField.setFont(new Font("Arial", Font.PLAIN, 15));
        secretKeyField.setPreferredSize(new Dimension(240, 34));
        secretKeyField.setToolTipText("Default key: ADMIN2024");
        formPanel.add(secretKeyField, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        JButton registerBtn = new JButton("Register Admin");
        registerBtn.setFont(new Font("Arial", Font.BOLD, 15));
        registerBtn.setPreferredSize(new Dimension(160, 38));

        JButton backBtn = new JButton("Back to Login");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 15));
        backBtn.setPreferredSize(new Dimension(140, 38));

        btnPanel.add(registerBtn);
        btnPanel.add(backBtn);
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        registerBtn.addActionListener(e -> performRegistration());
        backBtn.addActionListener(e -> mainFrame.showLoginForRole("ADMIN"));
    }

    private JTextField addRow(JPanel panel, GridBagConstraints gbc, String label, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Arial", Font.BOLD, 15));
        panel.add(l, gbc);
        gbc.gridx = 1; gbc.gridy = row;
        JTextField field = new JTextField(18);
        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(240, 34));
        panel.add(field, gbc);
        return field;
    }

    private void performRegistration() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String key = new String(secretKeyField.getPassword()).trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate security key to protect admin creation
        if (!key.isEmpty() && !"ADMIN2024".equals(key)) {
            JOptionPane.showMessageDialog(this, "Invalid Admin Secret Key. (Hint: ADMIN2024 or leave empty for demo)", "Authorization Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Admin admin = new Admin(0, username, password, fullName, email, phone);
        try {
            boolean success = userDAO.registerAdmin(admin);
            if (success) {
                JOptionPane.showMessageDialog(this, "Admin created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                mainFrame.showLoginForRole("ADMIN");
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearFields() {
        fullNameField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
        phoneField.setText("");
        secretKeyField.setText("");
    }
}
