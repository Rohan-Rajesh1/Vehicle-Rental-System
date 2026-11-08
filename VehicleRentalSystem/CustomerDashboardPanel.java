package com.vehiclerental.ui.customer;

import com.vehiclerental.dao.BookingDAO;
import com.vehiclerental.dao.NotificationDAO;
import com.vehiclerental.dao.ReviewDAO;
import com.vehiclerental.dao.VehicleDAO;
import com.vehiclerental.exceptions.BookingException;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.*;
import com.vehiclerental.notifications.NotificationService;
import com.vehiclerental.pricing.PricingStrategy;
import com.vehiclerental.pricing.PricingStrategyFactory;
import com.vehiclerental.ui.MainFrame;
import com.vehiclerental.ui.common.NotificationDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerDashboardPanel extends JPanel {
    private final MainFrame mainFrame;
    private final VehicleDAO vehicleDAO;
    private final BookingDAO bookingDAO;
    private final ReviewDAO reviewDAO;
    private final NotificationDAO notificationDAO;

    private Customer currentCustomer;
    private JLabel headerLabel;
    private JButton notifBtn;
    private JPanel dueAlertBanner;
    private JLabel dueAlertText;

    // Browse Vehicles
    private JComboBox<String> typeFilterCombo;
    private JTextField searchField;
    private DefaultTableModel vehicleTableModel;
    private JTable vehicleTable;
    private List<Vehicle> currentVehicleList = new ArrayList<>();

    // Dynamic Calculation & Booking Section
    private JLabel selectedVehicleLabel;
    private JSpinner daysSpinner;
    private JTextField startDateField; // YYYY-MM-DD
    private JRadioButton standardRateRadio;
    private JRadioButton weekendSurgeRadio;
    private JRadioButton longTermRadio;
    private JLabel costDisplayLabel;
    private JLabel breakdownLabel;
    private JLabel ecoLabel;
    private JTextField notesField;
    private JButton bookButton;
    private JButton compareBtn;

    // My Bookings Tab
    private DefaultTableModel myBookingsModel;
    private JTable myBookingsTable;
    private List<Booking> currentBookingsList = new ArrayList<>();
    private JButton rateRentalBtn;

    public CustomerDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.vehicleDAO = new VehicleDAO();
        this.bookingDAO = new BookingDAO();
        this.reviewDAO = new ReviewDAO();
        this.notificationDAO = new NotificationDAO();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // Top Panel: Header + Due Date Alert Banner
        JPanel topContainer = new JPanel(new BorderLayout(8, 8));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerLabel = new JLabel("Customer Portal", SwingConstants.LEFT);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        notifBtn = new JButton("🔔 Notifications (0)");
        notifBtn.setFont(new Font("Arial", Font.BOLD, 14));
        notifBtn.setForeground(new Color(26, 54, 93));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 14));

        btnPanel.add(notifBtn);
        btnPanel.add(logoutBtn);

        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(btnPanel, BorderLayout.EAST);
        topContainer.add(headerPanel, BorderLayout.NORTH);

        // Due Date Alert Banner
        dueAlertBanner = new JPanel(new BorderLayout());
        dueAlertBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 160, 50), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        dueAlertBanner.setBackground(new Color(255, 250, 235));
        dueAlertText = new JLabel("Checking active rental due dates...");
        dueAlertText.setFont(new Font("Arial", Font.BOLD, 14));
        dueAlertBanner.add(dueAlertText, BorderLayout.CENTER);
        topContainer.add(dueAlertBanner, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 15));
        tabbedPane.addTab("Browse & Book Vehicles", createBrowsePanel());
        tabbedPane.addTab("My Bookings & Due Status", createMyBookingsPanel());
        add(tabbedPane, BorderLayout.CENTER);

        notifBtn.addActionListener(e -> {
            if (currentCustomer != null) {
                new NotificationDialog(SwingUtilities.getWindowAncestor(this), currentCustomer.getId(), this::updateNotificationCount).setVisible(true);
            }
        });
        logoutBtn.addActionListener(e -> mainFrame.logout());
    }

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        headerLabel.setText("Customer Portal - Welcome, " + customer.getFullName() + " (DL: " + customer.getLicenseNumber() + ")");
        reloadData();
    }

    private JPanel createBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search & Filter Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        
        JLabel typeLbl = new JLabel("Vehicle Type:");
        typeLbl.setFont(new Font("Arial", Font.BOLD, 15));
        searchBar.add(typeLbl);

        typeFilterCombo = new JComboBox<>(new String[]{"ALL", "CAR", "BIKE"});
        typeFilterCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        searchBar.add(typeFilterCombo);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(new Font("Arial", Font.BOLD, 15));
        searchBar.add(searchLbl);

        searchField = new JTextField(12);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchBar.add(searchField);

        JButton searchBtn = new JButton("Search Vehicles");
        searchBtn.setFont(new Font("Arial", Font.BOLD, 14));
        searchBar.add(searchBtn);

        compareBtn = new JButton("Side-by-Side Compare");
        compareBtn.setFont(new Font("Arial", Font.BOLD, 14));
        compareBtn.setForeground(new Color(26, 54, 93));
        searchBar.add(compareBtn);

        panel.add(searchBar, BorderLayout.NORTH);

        // Vehicles Table
        String[] cols = {"ID", "Type", "Brand & Model", "Rating", "Year", "Colour", "Mileage", "Condition Check", "GPS", "Rate/Day", "Agency", "Status", "Engine / Powertrain"};
        vehicleTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        vehicleTable = new JTable(vehicleTableModel);
        vehicleTable.setRowHeight(38);
        vehicleTable.setFont(new Font("Arial", Font.PLAIN, 15));
        vehicleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vehicleTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        panel.add(new JScrollPane(vehicleTable), BorderLayout.CENTER);

        // Booking & Dynamic Calculator Panel (South)
        JPanel bookingCard = new JPanel(new BorderLayout(6, 6));
        bookingCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Vehicle Details & Dynamic Rental Calculator (Strategy Pattern)",
                0, 0, new Font("Arial", Font.BOLD, 16)));

        JPanel calcForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Selected Vehicle Info & Specifications
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel selTitle = new JLabel("Selected Vehicle:");
        selTitle.setFont(new Font("Arial", Font.BOLD, 15));
        calcForm.add(selTitle, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 3;
        selectedVehicleLabel = new JLabel("<html><i>Please select a vehicle from the table above to view complete specifications and rental cost.</i></html>");
        selectedVehicleLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        calcForm.add(selectedVehicleLabel, gbc);

        // Row 1: Start Date & Number of Days
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel startLbl = new JLabel("Rental Start Date:");
        startLbl.setFont(new Font("Arial", Font.BOLD, 15));
        calcForm.add(startLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        startDateField = new JTextField(LocalDate.now().plusDays(1).toString(), 10);
        startDateField.setFont(new Font("Arial", Font.PLAIN, 15));
        calcForm.add(startDateField, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        JLabel daysLbl = new JLabel("Number of Days:");
        daysLbl.setFont(new Font("Arial", Font.BOLD, 15));
        calcForm.add(daysLbl, gbc);

        gbc.gridx = 3; gbc.gridy = 1;
        daysSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 90, 1));
        daysSpinner.setFont(new Font("Arial", Font.BOLD, 15));
        calcForm.add(daysSpinner, gbc);

        // Row 2: Rate Selection (Standard Daily Rate vs Dynamic Strategies)
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel rateLbl = new JLabel("Rental Rate Option:");
        rateLbl.setFont(new Font("Arial", Font.BOLD, 15));
        calcForm.add(rateLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3;
        JPanel ratePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        standardRateRadio = new JRadioButton("Standard Daily Rate", true);
        standardRateRadio.setFont(new Font("Arial", Font.BOLD, 15));
        standardRateRadio.setForeground(new Color(26, 54, 93));

        weekendSurgeRadio = new JRadioButton("Weekend Surge (+15%)");
        weekendSurgeRadio.setFont(new Font("Arial", Font.PLAIN, 14));

        longTermRadio = new JRadioButton("Long-Term Discount (-10% for 7+ days)");
        longTermRadio.setFont(new Font("Arial", Font.PLAIN, 14));

        ButtonGroup rateGroup = new ButtonGroup();
        rateGroup.add(standardRateRadio);
        rateGroup.add(weekendSurgeRadio);
        rateGroup.add(longTermRadio);

        ratePanel.add(standardRateRadio);
        ratePanel.add(weekendSurgeRadio);
        ratePanel.add(longTermRadio);
        calcForm.add(ratePanel, gbc);

        // Row 3: Corresponding Calculated Total Rent
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        JLabel costTitle = new JLabel("Corresponding Total Rent:");
        costTitle.setFont(new Font("Arial", Font.BOLD, 15));
        calcForm.add(costTitle, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 3;
        costDisplayLabel = new JLabel("<html><font size='+1' color='#006600'><b>$0.00</b></font></html>");
        costDisplayLabel.setFont(new Font("Arial", Font.BOLD, 16));
        calcForm.add(costDisplayLabel, gbc);

        // Row 4: Pricing formula breakdown & Scheduled Due Date
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel formulaTitle = new JLabel("Calculation & Due Date:");
        formulaTitle.setFont(new Font("Arial", Font.BOLD, 15));
        calcForm.add(formulaTitle, gbc);

        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 3;
        breakdownLabel = new JLabel("Select vehicle to see corresponding rental cost and due date");
        breakdownLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        calcForm.add(breakdownLabel, gbc);

        // Row 5: Eco Impact (EV)
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        JLabel ecoTitle = new JLabel("Eco Impact (CO₂):");
        ecoTitle.setFont(new Font("Arial", Font.BOLD, 15));
        calcForm.add(ecoTitle, gbc);

        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 3;
        ecoLabel = new JLabel("Standard Vehicle");
        ecoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        calcForm.add(ecoLabel, gbc);

        // Row 6: Notes & Submit
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        JLabel notesLbl = new JLabel("Notes / Requests:");
        notesLbl.setFont(new Font("Arial", Font.BOLD, 15));
        calcForm.add(notesLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 2;
        notesField = new JTextField(25);
        notesField.setFont(new Font("Arial", Font.PLAIN, 15));
        calcForm.add(notesField, gbc);

        gbc.gridx = 3; gbc.gridy = 6; gbc.gridwidth = 1;
        bookButton = new JButton("Submit Booking Request");
        bookButton.setFont(new Font("Arial", Font.BOLD, 15));
        bookButton.setEnabled(false);
        calcForm.add(bookButton, gbc);

        bookingCard.add(calcForm, BorderLayout.CENTER);
        panel.add(bookingCard, BorderLayout.SOUTH);

        // Listeners
        searchBtn.addActionListener(e -> searchVehicles());
        typeFilterCombo.addActionListener(e -> searchVehicles());
        compareBtn.addActionListener(e -> openVehicleComparison());

        vehicleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                standardRateRadio.setSelected(true);
                updateSelectedVehicleCalculation();
            }
        });

        vehicleTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = vehicleTable.getSelectedRow();
                if (row >= 0) {
                    standardRateRadio.setSelected(true);
                    updateSelectedVehicleCalculation();
                    daysSpinner.requestFocusInWindow();
                }
            }
        });

        standardRateRadio.addActionListener(e -> {
            updateSelectedVehicleCalculation();
            daysSpinner.requestFocusInWindow();
        });
        weekendSurgeRadio.addActionListener(e -> updateSelectedVehicleCalculation());
        longTermRadio.addActionListener(e -> {
            if ((int) daysSpinner.getValue() < 7) {
                daysSpinner.setValue(7);
            }
            updateSelectedVehicleCalculation();
        });

        daysSpinner.addChangeListener(e -> updateSelectedVehicleCalculation());
        startDateField.addActionListener(e -> updateSelectedVehicleCalculation());
        bookButton.addActionListener(e -> submitBookingRequest());

        return panel;
    }

    private JPanel createMyBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Includes dedicated Due / Days Left column
        String[] cols = {"Booking ID", "Vehicle Info", "Agency", "Start Date", "End Date", "Total Days", "Total Rent", "Status", "Due / Days Left", "Pricing Strategy", "Notes"};
        myBookingsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        myBookingsTable = new JTable(myBookingsModel);
        myBookingsTable.setRowHeight(38);
        myBookingsTable.setFont(new Font("Arial", Font.PLAIN, 15));
        myBookingsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        myBookingsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        myBookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(myBookingsTable), BorderLayout.CENTER);

        // Actions Bar: Rating
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        rateRentalBtn = new JButton("⭐ Rate & Review Rental");
        rateRentalBtn.setFont(new Font("Arial", Font.BOLD, 14));
        rateRentalBtn.setEnabled(false);

        actionPanel.add(rateRentalBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        myBookingsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateBookingActionButtons();
            }
        });

        rateRentalBtn.addActionListener(e -> openReviewDialog());

        return panel;
    }

    private void updateBookingActionButtons() {
        int row = myBookingsTable.getSelectedRow();
        if (row >= 0 && row < currentBookingsList.size()) {
            Booking b = currentBookingsList.get(row);
            rateRentalBtn.setEnabled(b.getStatus() == BookingStatus.RETURNED);
        } else {
            rateRentalBtn.setEnabled(false);
        }
    }

    private void openReviewDialog() {
        int row = myBookingsTable.getSelectedRow();
        if (row >= 0 && row < currentBookingsList.size()) {
            Booking b = currentBookingsList.get(row);
            new ReviewDialog(SwingUtilities.getWindowAncestor(this), b, this::reloadData).setVisible(true);
        }
    }

    private void openVehicleComparison() {
        if (currentVehicleList.size() < 2) {
            JOptionPane.showMessageDialog(this, "At least two vehicles are needed to compare.", "Comparison", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Vehicle v1 = getCurrentlySelectedVehicle();
        Vehicle v2 = null;

        if (v1 == null) {
            v1 = currentVehicleList.get(0);
            v2 = currentVehicleList.get(1);
        } else {
            for (Vehicle other : currentVehicleList) {
                if (other.getId() != v1.getId()) {
                    v2 = other;
                    break;
                }
            }
        }

        if (v2 != null) {
            new VehicleComparisonDialog(SwingUtilities.getWindowAncestor(this), v1, v2, selected -> {
                for (int i = 0; i < currentVehicleList.size(); i++) {
                    if (currentVehicleList.get(i).getId() == selected.getId()) {
                        vehicleTable.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            }).setVisible(true);
        }
    }

    private Vehicle getCurrentlySelectedVehicle() {
        int row = vehicleTable.getSelectedRow();
        if (row >= 0 && row < currentVehicleList.size()) {
            return currentVehicleList.get(row);
        }
        return null;
    }

    private String getSelectedStrategyName() {
        if (weekendSurgeRadio != null && weekendSurgeRadio.isSelected()) {
            return PricingStrategyFactory.WEEKEND_SURGE;
        } else if (longTermRadio != null && longTermRadio.isSelected()) {
            return PricingStrategyFactory.LONG_TERM;
        }
        return PricingStrategyFactory.STANDARD;
    }


    private void updateSelectedVehicleCalculation() {
        Vehicle v = getCurrentlySelectedVehicle();
        if (v == null) {
            selectedVehicleLabel.setText("<html><i>Please select a vehicle from the table above to view complete specifications and rental cost.</i></html>");
            costDisplayLabel.setText("<html><font size='+1' color='#006600'><b>$0.00</b></font></html>");
            breakdownLabel.setText("Select vehicle to see pricing breakdown and due date");
            ecoLabel.setText("Standard Vehicle");
            bookButton.setEnabled(false);
            return;
        }

        int days = (int) daysSpinner.getValue();
        String stratName = getSelectedStrategyName();
        PricingStrategy strategy = PricingStrategyFactory.getStrategy(stratName);

        LocalDate startDate = LocalDate.now();
        try {
            startDate = LocalDate.parse(startDateField.getText().trim());
        } catch (Exception ignored) {}

        double totalCost = strategy.calculateTotal(v, days, startDate);
        String breakdown = strategy.getDescription(v, days, startDate);

        LocalDate projectedDueDate = startDate.plusDays(days);
        String duePreview = "Scheduled Due Date: <b>" + projectedDueDate + "</b> (" + (days == 1 ? "1 day duration" : days + " days duration") + ")";

        // Eco impact calculation
        if (v instanceof ElectricCar ec) {
            double saved = ec.calculateCarbonSavedKg(days);
            ecoLabel.setText("<html><font color='#059669'><b>🌱 100% Electric EV: Saves ~" + saved + " kg of CO₂ emissions vs a gasoline car!</b></font></html>");
        } else if (v instanceof ElectricBike eb) {
            double saved = eb.calculateCarbonSavedKg(days);
            ecoLabel.setText("<html><font color='#059669'><b>🌱 Eco-Friendly EV Scooter: Saves ~" + saved + " kg of CO₂ emissions!</b></font></html>");
        } else {
            ecoLabel.setText("<html><font color='#64748b'>Internal Combustion Engine (Certified Emission Inspected &#10003;)</font></html>");
        }

        String conditionStr = "Colour: <b>" + v.getColour() + "</b> | Mileage: <b>" + v.getMileage() + "</b> | " +
                (v.isTyreConditionGood() ? "<font color='#007700'>✓ Good Tyres</font>" : "<font color='#CC0000'>✗ Tyres Worn</font>") + " | " +
                (v.isEngineConditionGood() ? "<font color='#007700'>✓ Engine Certified</font>" : "<font color='#CC0000'>✗ Needs Inspection</font>") + " | " +
                (v.isGpsEnabled() ? "<font color='#0055AA'>✓ GPS Navigation</font>" : "GPS: No") + " | " +
                (v.isInsuranceVerified() ? "<font color='#007700'>✓ Insured</font>" : "Insurance: Pending");

        if (v.getStatus() != VehicleStatus.AVAILABLE) {
            selectedVehicleLabel.setText("<html><b>" + v.getBrand() + " " + v.getModel() + " (" + v.getYear() + ")</b> - Agency: <b>" + v.getAgencyName() + "</b><br>" +
                    conditionStr + "<br>" +
                    "<font color='#CC0000'><b>● CURRENTLY RENTED:</b> This vehicle is currently on rent with another customer. All specifications remain visible, but booking is locked until returned by the agency.</font></html>");
            costDisplayLabel.setText("<html><font color='#CC0000'><b>● Currently Rented / Unavailable for Booking</b></font></html>");
            breakdownLabel.setText("<html><i>Estimated daily rate when returned: $" + String.format("%.2f", v.getDailyRate()) + "/day</i> &nbsp;|&nbsp; " + duePreview + "</html>");
            bookButton.setEnabled(false);
        } else {
            selectedVehicleLabel.setText("<html><b>" + v.getBrand() + " " + v.getModel() + " (" + v.getYear() + ")</b> - Agency: <b>" + v.getAgencyName() + "</b> | Rate: <b>$" + String.format("%.2f", v.getDailyRate()) + "/day</b><br>" +
                    conditionStr + "<br>" +
                    "<font color='#007700'><b>✓ AVAILABLE FOR IMMEDIATE BOOKING</b></font></html>");
            costDisplayLabel.setText("<html><font size='+1' color='#006600'><b>$" + String.format("%.2f", totalCost) + "</b></font></html>");
            breakdownLabel.setText("<html><font color='#333333'>" + breakdown + " &nbsp;|&nbsp; <b>" + duePreview + "</b></font></html>");
            bookButton.setEnabled(true);
        }
    }

    private void submitBookingRequest() {
        Vehicle v = getCurrentlySelectedVehicle();
        if (v == null) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle first.");
            return;
        }

        if (v.getStatus() != VehicleStatus.AVAILABLE) {
            JOptionPane.showMessageDialog(this,
                    "This vehicle (" + v.getBrand() + " " + v.getModel() + ") is currently RENTED.\n" +
                    "It will remain in the vehicle list for viewing, but cannot be booked until returned by the agency.",
                    "Vehicle Currently Rented", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String startDateStr = startDateField.getText().trim();
        int days = (int) daysSpinner.getValue();
        String notes = notesField.getText().trim();

        LocalDate startDate;
        try {
            startDate = LocalDate.parse(startDateStr);
            if (startDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Start date cannot be in the past.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid start date format (YYYY-MM-DD).", "Format Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate endDate = startDate.plusDays(days);
        String stratName = getSelectedStrategyName();
        PricingStrategy strategy = PricingStrategyFactory.getStrategy(stratName);
        double totalCost = strategy.calculateTotal(v, days, startDate);

        Booking booking = new Booking(0, currentCustomer.getCustomerId(), v.getId(),
                Date.valueOf(startDate), Date.valueOf(endDate), days, totalCost,
                BookingStatus.PENDING, notes);
        booking.setPricingStrategyName(strategy.getName());

        try {
            boolean ok = bookingDAO.createBooking(booking);
            if (ok) {
                // Dispatch notification to agency owner
                int agencyUserId = bookingDAO.getAgencyUserId(v.getAgencyId());
                if (agencyUserId > 0) {
                    NotificationService.getInstance().onBookingSubmitted(agencyUserId, v.getBrand() + " " + v.getModel(), currentCustomer.getFullName());
                }

                JOptionPane.showMessageDialog(this,
                        "Booking request submitted successfully!\n\n" +
                        "Vehicle: " + v.getBrand() + " " + v.getModel() + " (" + v.getColour() + ")\n" +
                        "Pricing Strategy: " + strategy.getName() + "\n" +
                        "Duration: " + days + " days (" + startDate + " to " + endDate + ")\n" +
                        "Total Rent: $" + String.format("%.2f", totalCost) + "\n" +
                        "Agency: " + v.getAgencyName() + "\n" +
                        "Notes: " + (notes.isEmpty() ? "None" : notes) + "\n\n" +
                        "Notice: The vehicle remains visible in the catalog with status PENDING.\n" +
                        "You can track approval and days remaining under 'My Bookings & Due Status'.",
                        "Booking Request Placed", JOptionPane.INFORMATION_MESSAGE);

                loadCustomerBookings();
                searchVehicles();
            }
        } catch (BookingException be) {
            JOptionPane.showMessageDialog(this, be.getMessage(), "Booking Failed", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException dbe) {
            JOptionPane.showMessageDialog(this, dbe.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void reloadData() {
        searchVehicles();
        if (currentCustomer != null) {
            loadCustomerBookings();
            updateNotificationCount();
        }
    }

    private void updateNotificationCount() {
        if (currentCustomer != null) {
            int unread = notificationDAO.getUnreadCount(currentCustomer.getId());
            notifBtn.setText("🔔 Notifications (" + unread + ")");
            if (unread > 0) {
                notifBtn.setForeground(new Color(220, 38, 38)); // Red if unread
            } else {
                notifBtn.setForeground(new Color(26, 54, 93));
            }
        }
    }

    private void searchVehicles() {
        vehicleTableModel.setRowCount(0);
        try {
            String filter = (String) typeFilterCombo.getSelectedItem();
            String kw = searchField.getText().trim();
            currentVehicleList = vehicleDAO.searchAvailableVehicles(filter, kw);

            for (Vehicle v : currentVehicleList) {
                String specs = "";
                if (v instanceof ElectricCar ec) {
                    specs = "⚡ " + ec.getBatteryCapacityKwh() + "kWh EV, " + ec.getRangeKm() + "km range, " + ec.getTransmission();
                } else if (v instanceof ElectricBike eb) {
                    specs = "⚡ " + eb.getBatteryCapacityKwh() + "kWh EV, " + eb.getRangeKm() + "km range";
                } else if (v instanceof Car car) {
                    specs = car.getSeatingCapacity() + " Seats, " + car.getTransmission() + ", " + car.getFuelType();
                } else if (v instanceof Bike bike) {
                    specs = bike.getEngineCc() + "cc, " + bike.getBikeType();
                }

                String cond = (v.isTyreConditionGood() ? "✓ Tyres" : "✗ Tyres") + " | " +
                              (v.isEngineConditionGood() ? "✓ Engine" : "✗ Engine");
                String gps = v.isGpsEnabled() ? "✓ Yes" : "No";

                String statusDisplay;
                if (v.getStatus() == VehicleStatus.AVAILABLE) {
                    statusDisplay = "✓ Available";
                } else if (v.getStatus() == VehicleStatus.RENTED) {
                    statusDisplay = "● Rented";
                } else {
                    statusDisplay = "🔧 Maintenance";
                }

                double rating = reviewDAO.getAverageRatingForVehicle(v.getId());
                String ratingStr = "★ " + String.format("%.1f", rating);

                vehicleTableModel.addRow(new Object[]{
                        v.getId(),
                        v.getVehicleType(),
                        v.getBrand() + " " + v.getModel(),
                        ratingStr,
                        v.getYear(),
                        v.getColour(),
                        v.getMileage(),
                        cond,
                        gps,
                        String.format("$%.2f", v.getDailyRate()),
                        v.getAgencyName(),
                        statusDisplay,
                        specs
                });
            }
            updateSelectedVehicleCalculation();
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load vehicles: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCustomerBookings() {
        myBookingsModel.setRowCount(0);
        String urgentAlert = null;
        try {
            currentBookingsList = bookingDAO.getBookingsByCustomer(currentCustomer.getCustomerId());
            for (Booking b : currentBookingsList) {
                String notes = (b.getNotes() != null && !b.getNotes().trim().isEmpty()) ? b.getNotes() : "None";
                String dueStatus = b.getDueStatus();

                // Check for imminent due date alert
                Long daysLeft = b.getRawDaysLeft();
                if (daysLeft != null && urgentAlert == null) {
                    if (b.getStatus() == BookingStatus.APPROVED) {
                        if (daysLeft == 0) {
                            urgentAlert = "🚨 URGENT: Your rental for " + b.getVehicleInfo() + " is DUE TODAY! Please arrange return with " + b.getAgencyName() + ".";
                        } else if (daysLeft == 1) {
                            urgentAlert = "⚠️ 1 DAY LEFT: Your rental for " + b.getVehicleInfo() + " is due tomorrow (" + b.getEndDate() + ").";
                        } else if (daysLeft == 2) {
                            urgentAlert = "ℹ️ 2 DAYS LEFT: Your rental for " + b.getVehicleInfo() + " is due in 2 days (" + b.getEndDate() + ").";
                        } else if (daysLeft > 2) {
                            urgentAlert = "ℹ️ " + daysLeft + " DAYS LEFT: Your active rental for " + b.getVehicleInfo() + " is due on " + b.getEndDate() + ".";
                        } else if (daysLeft < 0) {
                            urgentAlert = "🚨 OVERDUE: Your rental for " + b.getVehicleInfo() + " was due on " + b.getEndDate() + " (" + Math.abs(daysLeft) + " days overdue)!";
                        }
                    } else if (b.getStatus() == BookingStatus.PENDING) {
                        if (daysLeft == 1) {
                            urgentAlert = "⏳ Pending Request: " + b.getVehicleInfo() + " (1 day left until scheduled return date: " + b.getEndDate() + ") - Awaiting agency approval.";
                        } else if (daysLeft > 1) {
                            urgentAlert = "⏳ Pending Request: " + b.getVehicleInfo() + " (" + daysLeft + " days left until scheduled return date: " + b.getEndDate() + ") - Awaiting agency approval.";
                        }
                    }
                }

                myBookingsModel.addRow(new Object[]{
                        b.getId(),
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

            // Update top alert banner
            if (urgentAlert != null) {
                dueAlertText.setText("<html>" + urgentAlert + "</html>");
                dueAlertBanner.setVisible(true);
            } else {
                dueAlertText.setText("<html><font color='#006600'>✓ All rental statuses up to date. No rentals due today.</font></html>");
                dueAlertBanner.setVisible(true);
            }

            updateBookingActionButtons();

        } catch (DatabaseException e) {
            System.err.println("Error loading customer bookings: " + e.getMessage());
        }
    }
}
