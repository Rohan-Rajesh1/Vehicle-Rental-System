package com.vehiclerental.ui;

import com.vehiclerental.db.DatabaseConnection;
import com.vehiclerental.models.Admin;
import com.vehiclerental.models.Customer;
import com.vehiclerental.models.RentalAgency;
import com.vehiclerental.ui.admin.AdminDashboardPanel;
import com.vehiclerental.ui.agency.AgencyDashboardPanel;
import com.vehiclerental.ui.auth.LoginPanel;
import com.vehiclerental.ui.auth.RegisterAdminPanel;
import com.vehiclerental.ui.auth.RegisterAgencyPanel;
import com.vehiclerental.ui.auth.RegisterCustomerPanel;
import com.vehiclerental.ui.customer.CustomerDashboardPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public static final String CARD_ROLE_SELECT = "ROLE_SELECT";
    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_REG_CUSTOMER = "REG_CUSTOMER";
    public static final String CARD_REG_AGENCY = "REG_AGENCY";
    public static final String CARD_REG_ADMIN = "REG_ADMIN";
    public static final String CARD_ADMIN_DASH = "ADMIN_DASH";
    public static final String CARD_AGENCY_DASH = "AGENCY_DASH";
    public static final String CARD_CUSTOMER_DASH = "CUSTOMER_DASH";

    private final CardLayout cardLayout;
    private final JPanel cardsPanel;

    private final LoginPanel loginPanel;
    private final RegisterCustomerPanel regCustomerPanel;
    private final RegisterAgencyPanel regAgencyPanel;
    private final RegisterAdminPanel regAdminPanel;
    private final AdminDashboardPanel adminDashboardPanel;
    private final AgencyDashboardPanel agencyDashboardPanel;
    private final CustomerDashboardPanel customerDashboardPanel;

    public MainFrame() {
        super("Vehicle Rental System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1260, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        // Initialize panels
        loginPanel = new LoginPanel(this);
        regCustomerPanel = new RegisterCustomerPanel(this);
        regAgencyPanel = new RegisterAgencyPanel(this);
        regAdminPanel = new RegisterAdminPanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);
        agencyDashboardPanel = new AgencyDashboardPanel(this);
        customerDashboardPanel = new CustomerDashboardPanel(this);

        // Add to card layout
        cardsPanel.add(createRoleSelectionPanel(), CARD_ROLE_SELECT);
        cardsPanel.add(loginPanel, CARD_LOGIN);
        cardsPanel.add(regCustomerPanel, CARD_REG_CUSTOMER);
        cardsPanel.add(regAgencyPanel, CARD_REG_AGENCY);
        cardsPanel.add(regAdminPanel, CARD_REG_ADMIN);
        cardsPanel.add(adminDashboardPanel, CARD_ADMIN_DASH);
        cardsPanel.add(agencyDashboardPanel, CARD_AGENCY_DASH);
        cardsPanel.add(customerDashboardPanel, CARD_CUSTOMER_DASH);

        add(cardsPanel, BorderLayout.CENTER);
        showRoleSelection();
    }

    private JPanel createRoleSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        // Top Header
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        JLabel titleLabel = new JLabel("VEHICLE RENTAL SYSTEM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel subtitleLabel = new JLabel("<html><center><font color='#555555'>Connecting Customers and Registered Rental Services through a Centralised Platform</font></center></html>", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 15));

        topPanel.add(titleLabel);
        topPanel.add(subtitleLabel);
        panel.add(topPanel, BorderLayout.NORTH);

        // Center: 3 Role Selection Options
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(16, 16, 16, 16);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        JLabel promptLabel = new JLabel("Please select your module to continue:", SwingConstants.CENTER);
        promptLabel.setFont(new Font("Arial", Font.BOLD, 17));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        centerPanel.add(promptLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        // 1. Admin Option
        JButton adminBtn = new JButton("<html><center><h2>Admin</h2><p>Review & Approve Agencies<br>Platform Management</p></center></html>");
        adminBtn.setPreferredSize(new Dimension(230, 130));
        gbc.gridx = 0;
        centerPanel.add(adminBtn, gbc);

        // 2. Customer Option
        JButton customerBtn = new JButton("<html><center><h2>Customer</h2><p>Browse & Search Vehicles<br>Calculate Rent & Book</p></center></html>");
        customerBtn.setPreferredSize(new Dimension(230, 130));
        gbc.gridx = 1;
        centerPanel.add(customerBtn, gbc);

        // 3. Rental Agency Option
        JButton agencyBtn = new JButton("<html><center><h2>Rental Agency</h2><p>List Cars & Bikes<br>Manage Bookings</p></center></html>");
        agencyBtn.setPreferredSize(new Dimension(230, 130));
        gbc.gridx = 2;
        centerPanel.add(agencyBtn, gbc);

        panel.add(centerPanel, BorderLayout.CENTER);

        // Event Handlers
        adminBtn.addActionListener(e -> showLoginForRole("ADMIN"));
        customerBtn.addActionListener(e -> showLoginForRole("CUSTOMER"));
        agencyBtn.addActionListener(e -> showLoginForRole("AGENCY"));

        return panel;
    }

    public void showRoleSelection() {
        cardLayout.show(cardsPanel, CARD_ROLE_SELECT);
    }

    public void showLoginForRole(String role) {
        loginPanel.setRole(role);
        cardLayout.show(cardsPanel, CARD_LOGIN);
    }

    public void showRegistrationForRole(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            regAdminPanel.clearFields();
            cardLayout.show(cardsPanel, CARD_REG_ADMIN);
        } else if ("AGENCY".equalsIgnoreCase(role)) {
            regAgencyPanel.clearFields();
            cardLayout.show(cardsPanel, CARD_REG_AGENCY);
        } else {
            regCustomerPanel.clearFields();
            cardLayout.show(cardsPanel, CARD_REG_CUSTOMER);
        }
    }

    public void openAdminDashboard(Admin admin) {
        adminDashboardPanel.setAdmin(admin);
        cardLayout.show(cardsPanel, CARD_ADMIN_DASH);
    }

    public void openCustomerDashboard(Customer customer) {
        customerDashboardPanel.setCustomer(customer);
        cardLayout.show(cardsPanel, CARD_CUSTOMER_DASH);
    }

    public void openAgencyDashboard(RentalAgency agency) {
        agencyDashboardPanel.setAgency(agency);
        cardLayout.show(cardsPanel, CARD_AGENCY_DASH);
    }

    public void logout() {
        showRoleSelection();
    }
}
