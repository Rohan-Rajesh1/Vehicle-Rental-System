package com.vehiclerental.ui.admin;

import com.vehiclerental.dao.*;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.*;
import com.vehiclerental.notifications.NotificationService;
import com.vehiclerental.ui.MainFrame;
import com.vehiclerental.ui.common.NotificationDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboardPanel extends JPanel {
    private final MainFrame mainFrame;
    private final AgencyDAO agencyDAO;
    private final UserDAO userDAO;
    private final VehicleDAO vehicleDAO;
    private final BookingDAO bookingDAO;
    private final ReviewDAO reviewDAO;
    private final NotificationDAO notificationDAO;

    private Admin currentAdmin;
    private JLabel headerLabel;
    private JButton notifBtn;

    // Tables & Models
    private DefaultTableModel agencyTableModel;
    private JTable agencyTable;

    private DefaultTableModel customerTableModel;
    private JTable customerTable;

    private DefaultTableModel vehicleTableModel;
    private JTable vehicleTable;

    private DefaultTableModel bookingTableModel;
    private JTable bookingTable;

    private JComboBox<String> agencyFilterCombo;
    private JLabel agencySummaryLabel;

    // Customer Reviews
    private DefaultTableModel reviewTableModel;
    private JTable reviewTable;

    public AdminDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.agencyDAO = new AgencyDAO();
        this.userDAO = new UserDAO();
        this.vehicleDAO = new VehicleDAO();
        this.bookingDAO = new BookingDAO();
        this.reviewDAO = new ReviewDAO();
        this.notificationDAO = new NotificationDAO();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerLabel = new JLabel("Administrator Dashboard", SwingConstants.LEFT);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 22));
        
        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        notifBtn = new JButton("🔔 Notifications (0)");
        notifBtn.setFont(new Font("Arial", Font.BOLD, 14));
        notifBtn.setForeground(new Color(26, 54, 93));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 14));

        headerBtns.add(notifBtn);
        headerBtns.add(logoutBtn);

        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(headerBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 15));
        tabbedPane.addTab("Agencies Directory & Approvals", createAgencyPanel());
        tabbedPane.addTab("Registered Customers", createCustomerPanel());
        tabbedPane.addTab("All Vehicles (Platform-wide)", createVehiclePanel());
        tabbedPane.addTab("All Bookings & Due Status", createBookingPanel());
        tabbedPane.addTab("Customer Reviews & Ratings", createReviewsPanel());

        add(tabbedPane, BorderLayout.CENTER);

        notifBtn.addActionListener(e -> {
            if (currentAdmin != null) {
                new NotificationDialog(SwingUtilities.getWindowAncestor(this), currentAdmin.getId(), this::updateNotificationCount).setVisible(true);
            }
        });
        logoutBtn.addActionListener(e -> mainFrame.logout());
    }

    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        headerLabel.setText("Administrator Dashboard - " + admin.getFullName() + " (" + admin.getUsername() + ")");
        reloadAllData();
    }

    private JPanel createAgencyPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 8, 8, 8),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(),
                        "Rental Agencies Directory (Accepted, Rejected & Pending Grouped Together)",
                        0, 0, new Font("Arial", Font.BOLD, 16))
        ));

        // Top Block: Summary Stats & Filter Controls
        JPanel topBlock = new JPanel(new BorderLayout(6, 6));
        topBlock.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        agencySummaryLabel = new JLabel("<html><b>Total Agencies:</b> 0 &nbsp;&nbsp;|&nbsp;&nbsp; <font color='#059669'><b>✓ Accepted:</b> 0</font> &nbsp;&nbsp;|&nbsp;&nbsp; <font color='#DC2626'><b>✗ Rejected:</b> 0</font> &nbsp;&nbsp;|&nbsp;&nbsp; <font color='#D97706'><b>⏳ Pending:</b> 0</font></html>");
        agencySummaryLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        topBlock.add(agencySummaryLabel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JLabel filterLbl = new JLabel("Filter View:");
        filterLbl.setFont(new Font("Arial", Font.BOLD, 14));
        filterPanel.add(filterLbl);

        agencyFilterCombo = new JComboBox<>(new String[]{
                "ALL (Accepted, Rejected & Pending)",
                "ACCEPTED Only",
                "REJECTED Only",
                "PENDING Only"
        });
        agencyFilterCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        agencyFilterCombo.setSelectedItem("ALL (Accepted, Rejected & Pending)");
        filterPanel.add(agencyFilterCombo);

        JButton filterBtn = new JButton("Apply Filter");
        filterBtn.setFont(new Font("Arial", Font.BOLD, 14));
        filterPanel.add(filterBtn);

        topBlock.add(filterPanel, BorderLayout.EAST);
        panel.add(topBlock, BorderLayout.NORTH);

        // Table
        String[] columns = {"Agency ID", "Agency Name", "Owner Name", "Reg No", "Phone", "Email", "Address", "Status"};
        agencyTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        agencyTable = new JTable(agencyTableModel);
        agencyTable.setRowHeight(38);
        agencyTable.setFont(new Font("Arial", Font.PLAIN, 15));
        agencyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        agencyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        agencyTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Custom Renderer for Status Column (Column 7) to highlight Accepted, Rejected, and Pending
        agencyTable.getColumnModel().getColumn(7).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(new Font("Arial", Font.BOLD, 14));
                String text = (value != null) ? value.toString() : "";
                if (!isSelected) {
                    if (text.contains("ACCEPTED") || text.contains("APPROVED")) {
                        c.setForeground(new Color(5, 150, 105)); // Emerald Green
                        c.setBackground(new Color(240, 253, 244)); // Soft light green
                    } else if (text.contains("REJECTED")) {
                        c.setForeground(new Color(220, 38, 38)); // Red
                        c.setBackground(new Color(254, 242, 242)); // Soft light red
                    } else if (text.contains("PENDING")) {
                        c.setForeground(new Color(217, 119, 6)); // Amber
                        c.setBackground(new Color(255, 251, 235)); // Soft light amber
                    } else {
                        c.setForeground(Color.BLACK);
                        c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        });

        panel.add(new JScrollPane(agencyTable), BorderLayout.CENTER);

        // Bottom Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        JButton approveBtn = new JButton("✓ Accept / Approve Agency");
        approveBtn.setFont(new Font("Arial", Font.BOLD, 15));
        approveBtn.setForeground(new Color(5, 150, 105));

        JButton rejectBtn = new JButton("✗ Reject Agency");
        rejectBtn.setFont(new Font("Arial", Font.BOLD, 15));
        rejectBtn.setForeground(new Color(220, 38, 38));

        actionPanel.add(approveBtn);
        actionPanel.add(rejectBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        filterBtn.addActionListener(e -> loadAgencies());
        agencyFilterCombo.addActionListener(e -> loadAgencies());
        approveBtn.addActionListener(e -> updateSelectedAgencyStatus(AgencyStatus.APPROVED));
        rejectBtn.addActionListener(e -> updateSelectedAgencyStatus(AgencyStatus.REJECTED));

        return panel;
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Customer ID", "Full Name", "Username", "Phone", "Email", "License No", "Address"};
        customerTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        customerTable = new JTable(customerTableModel);
        customerTable.setRowHeight(38);
        customerTable.setFont(new Font("Arial", Font.PLAIN, 15));
        customerTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        customerTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        panel.add(new JScrollPane(customerTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createVehiclePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Agency", "Type", "Brand & Model", "Year", "Colour", "Mileage", "Condition Check", "Plate", "Rate/Day", "Status", "Engine / Powertrain"};
        vehicleTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        vehicleTable = new JTable(vehicleTableModel);
        vehicleTable.setRowHeight(38);
        vehicleTable.setFont(new Font("Arial", Font.PLAIN, 15));
        vehicleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        vehicleTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        panel.add(new JScrollPane(vehicleTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Booking ID", "Customer", "Phone", "Vehicle", "Agency", "Start Date", "End Date", "Days", "Total Cost", "Status", "Due / Days Left", "Pricing Strategy", "Customer Notes"};
        bookingTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        bookingTable = new JTable(bookingTableModel);
        bookingTable.setRowHeight(38);
        bookingTable.setFont(new Font("Arial", Font.PLAIN, 15));
        bookingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        bookingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Review ID", "Booking #", "Vehicle Info", "Customer Name", "Rating", "Customer Review Feedback", "Submitted At"};
        reviewTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        reviewTable = new JTable(reviewTableModel);
        reviewTable.setRowHeight(38);
        reviewTable.setFont(new Font("Arial", Font.PLAIN, 14));
        reviewTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        reviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        panel.add(new JScrollPane(reviewTable), BorderLayout.CENTER);
        return panel;
    }

    private void updateSelectedAgencyStatus(AgencyStatus status) {
        int row = agencyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an agency from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int agencyId = (int) agencyTableModel.getValueAt(row, 0);
        String name = (String) agencyTableModel.getValueAt(row, 1);

        String action = (status == AgencyStatus.APPROVED) ? "accept" : "reject";
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + action.toUpperCase() + " agency: " + name + " (ID: " + agencyId + ")?",
                "Confirm " + action.toUpperCase(),
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                boolean ok = agencyDAO.updateAgencyStatus(agencyId, status);
                if (ok) {
                    // Send notification to agency owner
                    int agencyUserId = bookingDAO.getAgencyUserId(agencyId);
                    if (agencyUserId > 0) {
                        if (status == AgencyStatus.APPROVED) {
                            NotificationService.getInstance().onAgencyApproved(agencyUserId, name);
                        } else if (status == AgencyStatus.REJECTED) {
                            NotificationService.getInstance().onAgencyRejected(agencyUserId, name);
                        }
                    }

                    String statusWord = (status == AgencyStatus.APPROVED) ? "ACCEPTED" : "REJECTED";
                    JOptionPane.showMessageDialog(this,
                            "Agency '" + name + "' (ID: " + agencyId + ") has been " + statusWord + ".\n" +
                            "It remains grouped in this block displaying status: " + statusWord + ".",
                            "Agency Status Updated", JOptionPane.INFORMATION_MESSAGE);
                    loadAgencies();

                    // Re-select the agency in the table so admin sees it immediately
                    for (int i = 0; i < agencyTableModel.getRowCount(); i++) {
                        if ((int) agencyTableModel.getValueAt(i, 0) == agencyId) {
                            agencyTable.setRowSelectionInterval(i, i);
                            break;
                        }
                    }
                }
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void reloadAllData() {
        loadAgencies();
        loadCustomers();
        loadVehicles();
        loadBookings();
        loadReviews();
        updateNotificationCount();
    }

    private void updateNotificationCount() {
        if (currentAdmin != null) {
            int unread = notificationDAO.getUnreadCount(currentAdmin.getId());
            notifBtn.setText("🔔 Notifications (" + unread + ")");
            if (unread > 0) {
                notifBtn.setForeground(new Color(220, 38, 38));
            } else {
                notifBtn.setForeground(new Color(26, 54, 93));
            }
        }
    }

    private void loadAgencies() {
        agencyTableModel.setRowCount(0);
        try {
            String filter = (String) agencyFilterCombo.getSelectedItem();
            List<RentalAgency> list = agencyDAO.getAgenciesByStatus(filter);

            // Group: PENDING first, then APPROVED (ACCEPTED), then REJECTED
            list.sort((a1, a2) -> {
                int s1 = (a1.getStatus() == AgencyStatus.PENDING) ? 0 : (a1.getStatus() == AgencyStatus.APPROVED ? 1 : 2);
                int s2 = (a2.getStatus() == AgencyStatus.PENDING) ? 0 : (a2.getStatus() == AgencyStatus.APPROVED ? 1 : 2);
                if (s1 != s2) return Integer.compare(s1, s2);
                return Integer.compare(a2.getAgencyId(), a1.getAgencyId());
            });

            int total = list.size();
            int acceptedCount = 0;
            int rejectedCount = 0;
            int pendingCount = 0;

            for (RentalAgency a : list) {
                String displayStatus;
                if (a.getStatus() == AgencyStatus.APPROVED) {
                    displayStatus = "✓ ACCEPTED";
                    acceptedCount++;
                } else if (a.getStatus() == AgencyStatus.REJECTED) {
                    displayStatus = "✗ REJECTED";
                    rejectedCount++;
                } else {
                    displayStatus = "⏳ PENDING";
                    pendingCount++;
                }

                agencyTableModel.addRow(new Object[]{
                        a.getAgencyId(),
                        a.getAgencyName(),
                        a.getFullName(),
                        a.getRegistrationNo(),
                        a.getPhone(),
                        a.getEmail(),
                        a.getAddress(),
                        displayStatus
                });
            }

            if (agencySummaryLabel != null) {
                agencySummaryLabel.setText("<html><b>Total Agencies:</b> " + total +
                        " &nbsp;&nbsp;|&nbsp;&nbsp; <font color='#059669'><b>✓ Accepted:</b> " + acceptedCount + "</font>" +
                        " &nbsp;&nbsp;|&nbsp;&nbsp; <font color='#DC2626'><b>✗ Rejected:</b> " + rejectedCount + "</font>" +
                        " &nbsp;&nbsp;|&nbsp;&nbsp; <font color='#D97706'><b>⏳ Pending:</b> " + pendingCount + "</font></html>");
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading agencies: " + e.getMessage());
        }
    }

    private void loadCustomers() {
        customerTableModel.setRowCount(0);
        try {
            List<Customer> list = userDAO.getAllCustomers();
            for (Customer c : list) {
                customerTableModel.addRow(new Object[]{
                        c.getCustomerId(),
                        c.getFullName(),
                        c.getUsername(),
                        c.getPhone(),
                        c.getEmail(),
                        c.getLicenseNumber(),
                        c.getAddress()
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
    }

    private void loadVehicles() {
        vehicleTableModel.setRowCount(0);
        try {
            List<Vehicle> list = vehicleDAO.getAllVehicles();
            for (Vehicle v : list) {
                String details = "";
                if (v instanceof ElectricCar ec) {
                    details = "⚡ " + ec.getBatteryCapacityKwh() + "kWh EV, " + ec.getRangeKm() + "km, " + ec.getTransmission();
                } else if (v instanceof ElectricBike eb) {
                    details = "⚡ " + eb.getBatteryCapacityKwh() + "kWh EV, " + eb.getRangeKm() + "km";
                } else if (v instanceof Car car) {
                    details = car.getSeatingCapacity() + " Seats, " + car.getTransmission() + ", " + car.getFuelType();
                } else if (v instanceof Bike bike) {
                    details = bike.getEngineCc() + "cc, " + bike.getBikeType();
                }

                String cond = (v.isTyreConditionGood() ? "✓ Tyres" : "✗") + " | " +
                              (v.isEngineConditionGood() ? "✓ Engine" : "✗") + " | " +
                              (v.isGpsEnabled() ? "✓ GPS" : "No GPS");

                vehicleTableModel.addRow(new Object[]{
                        v.getId(),
                        v.getAgencyName(),
                        v.getVehicleType(),
                        v.getBrand() + " " + v.getModel(),
                        v.getYear(),
                        v.getColour(),
                        v.getMileage(),
                        cond,
                        v.getLicensePlate(),
                        String.format("$%.2f", v.getDailyRate()),
                        v.getStatus().name(),
                        details
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading vehicles: " + e.getMessage());
        }
    }

    private void loadBookings() {
        bookingTableModel.setRowCount(0);
        try {
            List<Booking> list = bookingDAO.getAllBookings();
            for (Booking b : list) {
                String notes = (b.getNotes() != null && !b.getNotes().trim().isEmpty()) ? b.getNotes() : "None";
                String dueStatus = b.getDueStatus();
                bookingTableModel.addRow(new Object[]{
                        b.getId(),
                        b.getCustomerName(),
                        b.getCustomerPhone(),
                        b.getVehicleInfo(),
                        b.getAgencyName(),
                        b.getStartDate(),
                        b.getEndDate(),
                        b.getTotalDays(),
                        String.format("$%.2f", b.getTotalCost()),
                        b.getStatus().name(),
                        dueStatus,
                        b.getPricingStrategyName(),
                        notes
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
    }

    private void loadReviews() {
        reviewTableModel.setRowCount(0);
        try {
            List<Review> list = reviewDAO.getAllReviews();
            for (Review r : list) {
                String time = (r.getCreatedAt() != null) ? r.getCreatedAt().toString().substring(0, 16) : "-";
                reviewTableModel.addRow(new Object[]{
                        r.getId(),
                        r.getBookingId(),
                        r.getVehicleInfo(),
                        r.getCustomerName(),
                        r.getStarsDisplay() + " (" + r.getRating() + "/5)",
                        r.getComment(),
                        time
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading reviews: " + e.getMessage());
        }
    }
}
