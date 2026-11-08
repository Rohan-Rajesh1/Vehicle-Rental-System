package com.vehiclerental.ui.auth;

import com.vehiclerental.dao.UserDAO;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.AgencyStatus;
import com.vehiclerental.models.RentalAgency;
import com.vehiclerental.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class RegisterAgencyPanel extends JPanel {
    private final MainFrame mainFrame;
    private final UserDAO userDAO;

    private JTextField agencyNameField;
    private JTextField ownerNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField regNoField;
    private JTextField addressField;

    public RegisterAgencyPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userDAO = new UserDAO();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        JLabel titleLabel = new JLabel("Rental Agency Registration", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JLabel noteLabel = new JLabel("<html><center><font color='#555555'>Note: Newly registered agencies must be reviewed and approved by Admin before login.</font></center></html>", SwingConstants.CENTER);
        noteLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        topPanel.add(titleLabel);
        topPanel.add(noteLabel);
        add(topPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        agencyNameField = addRow(formPanel, gbc, "Agency Name:", row++);
        ownerNameField = addRow(formPanel, gbc, "Contact Person Name:", row++);
        regNoField = addRow(formPanel, gbc, "Business Reg Number:", row++);
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

        emailField = addRow(formPanel, gbc, "Official Email:", row++);
        phoneField = addRow(formPanel, gbc, "Contact Phone:", row++);
        addressField = addRow(formPanel, gbc, "Office Address:", row++);

        // Buttons
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        JButton registerBtn = new JButton("Submit Registration");
        registerBtn.setFont(new Font("Arial", Font.BOLD, 15));
        registerBtn.setPreferredSize(new Dimension(190, 38));

        JButton backBtn = new JButton("Back to Login");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 15));
        backBtn.setPreferredSize(new Dimension(140, 38));

        btnPanel.add(registerBtn);
        btnPanel.add(backBtn);
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        registerBtn.addActionListener(e -> performRegistration());
        backBtn.addActionListener(e -> mainFrame.showLoginForRole("AGENCY"));
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
        String agencyName = agencyNameField.getText().trim();
        String ownerName = ownerNameField.getText().trim();
        String regNo = regNoField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        if (agencyName.isEmpty() || ownerName.isEmpty() || regNo.isEmpty() ||
            username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all details.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RentalAgency agency = new RentalAgency(0, username, password, ownerName, email, phone,
                0, agencyName, regNo, address, AgencyStatus.PENDING);

        try {
            boolean success = userDAO.registerAgency(agency);
            if (success) {
                JOptionPane.showMessageDialog(this, 
                        "Registration submitted successfully!\nYour application is in PENDING status.\nPlease wait for Admin approval before logging in.", 
                        "Registration Submitted", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                mainFrame.showLoginForRole("AGENCY");
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearFields() {
        agencyNameField.setText("");
        ownerNameField.setText("");
        regNoField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
    }
}
