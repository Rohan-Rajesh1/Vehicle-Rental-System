package com.vehiclerental.ui.agency;

import com.vehiclerental.dao.BookingDAO;
import com.vehiclerental.dao.NotificationDAO;
import com.vehiclerental.dao.VehicleDAO;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.*;
import com.vehiclerental.notifications.NotificationService;
import com.vehiclerental.ui.MainFrame;
import com.vehiclerental.ui.common.NotificationDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AgencyDashboardPanel extends JPanel {
    private final MainFrame mainFrame;
    private final VehicleDAO vehicleDAO;
    private final BookingDAO bookingDAO;
    private final NotificationDAO notificationDAO;

    private RentalAgency currentAgency;
    private JLabel headerLabel;
    private JButton notifBtn;

    private DefaultTableModel vehicleTableModel;
    private JTable vehicleTable;
    private List<Vehicle> currentVehicleList = new ArrayList<>();

    private DefaultTableModel bookingTableModel;
    private JTable bookingTable;
    private List<Booking> currentBookingList = new ArrayList<>();
    private JButton approveBtn;
    private JButton rejectBtn;
    private JButton returnBtn;
    private JTextField notesDisplayField;

    public AgencyDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.vehicleDAO = new VehicleDAO();
        this.bookingDAO = new BookingDAO();
        this.notificationDAO = new NotificationDAO();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerLabel = new JLabel("Rental Agency Dashboard", SwingConstants.LEFT);
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
        add(headerPanel, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 15));
        tabbedPane.addTab("My Vehicle Listings", createVehiclesPanel());
        tabbedPane.addTab("Customer Booking Requests & Due Dates", createBookingsPanel());
        add(tabbedPane, BorderLayout.CENTER);

        notifBtn.addActionListener(e -> {
            if (currentAgency != null) {
                new NotificationDialog(SwingUtilities.getWindowAncestor(this), currentAgency.getId(), this::updateNotificationCount).setVisible(true);
            }
        });
        logoutBtn.addActionListener(e -> mainFrame.logout());
    }

    public void setAgency(RentalAgency agency) {
        this.currentAgency = agency;
        headerLabel.setText("Rental Agency: " + agency.getAgencyName() + " (Reg: " + agency.getRegistrationNo() + ")");
        reloadData();
    }

    private JPanel createVehiclesPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Type", "Brand & Model", "Year", "Colour", "Mileage", "Condition Check", "Plate", "Daily Rate", "Status", "Engine / Powertrain"};
        vehicleTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        vehicleTable = new JTable(vehicleTableModel);
        vehicleTable.setRowHeight(38);
        vehicleTable.setFont(new Font("Arial", Font.PLAIN, 15));
        vehicleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        vehicleTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        panel.add(new JScrollPane(vehicleTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JButton addBtn = new JButton("Add Vehicle / EV");
        addBtn.setFont(new Font("Arial", Font.BOLD, 15));
        JButton toggleStatusBtn = new JButton("Toggle Availability / Maintenance");
        toggleStatusBtn.setFont(new Font("Arial", Font.PLAIN, 15));
        JButton deleteBtn = new JButton("Delete Vehicle");
        deleteBtn.setFont(new Font("Arial", Font.PLAIN, 15));

        actions.add(addBtn);
        actions.add(toggleStatusBtn);
        actions.add(deleteBtn);
        panel.add(actions, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showAddVehicleDialog());
        toggleStatusBtn.addActionListener(e -> toggleVehicleStatus());
        deleteBtn.addActionListener(e -> deleteSelectedVehicle());

        return panel;
    }

    private JPanel createBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Booking ID", "Customer", "Phone", "Vehicle", "Start Date", "End Date", "Days", "Total Cost", "Status", "Due / Days Left", "Strategy", "Notes / Requests"};
        bookingTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingTable = new JTable(bookingTableModel);
        bookingTable.setRowHeight(38);
        bookingTable.setFont(new Font("Arial", Font.PLAIN, 15));
        bookingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        // South section: Note preview + Action buttons
        JPanel southPanel = new JPanel(new BorderLayout(6, 6));

        // Note display box
        JPanel noteBox = new JPanel(new BorderLayout(5, 5));
        noteBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Selected Booking Details & Customer Requests",
                0, 0, new Font("Arial", Font.BOLD, 14)));
        notesDisplayField = new JTextField("Select a booking from the table to view customer notes and due date status.");
        notesDisplayField.setEditable(false);
        notesDisplayField.setBackground(new Color(245, 245, 245));
        notesDisplayField.setFont(new Font("Arial", Font.PLAIN, 14));
        noteBox.add(notesDisplayField, BorderLayout.CENTER);
        southPanel.add(noteBox, BorderLayout.NORTH);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        approveBtn = new JButton("Approve Booking");
        approveBtn.setFont(new Font("Arial", Font.BOLD, 14));
        rejectBtn = new JButton("Reject Booking");
        rejectBtn.setFont(new Font("Arial", Font.BOLD, 14));
        returnBtn = new JButton("Mark as Returned (Inspection Checklist)");
        returnBtn.setFont(new Font("Arial", Font.BOLD, 14));
        returnBtn.setForeground(new Color(5, 150, 105));

        approveBtn.setEnabled(false);
        rejectBtn.setEnabled(false);
        returnBtn.setEnabled(false);

        actions.add(approveBtn);
        actions.add(rejectBtn);
        actions.add(returnBtn);
        southPanel.add(actions, BorderLayout.SOUTH);

        panel.add(southPanel, BorderLayout.SOUTH);

        // Selection listener to dynamically update buttons & note box
        bookingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateBookingButtonStates();
            }
        });

        approveBtn.addActionListener(e -> updateBookingStatus(BookingStatus.APPROVED));
        rejectBtn.addActionListener(e -> updateBookingStatus(BookingStatus.REJECTED));
        returnBtn.addActionListener(e -> openReturnInspectionDialog());

        return panel;
    }

    private Booking getSelectedBooking() {
        int row = bookingTable.getSelectedRow();
        if (row >= 0 && row < currentBookingList.size()) {
            return currentBookingList.get(row);
        }
        return null;
    }

    private void openReturnInspectionDialog() {
        Booking b = getSelectedBooking();
        if (b != null && b.getStatus() == BookingStatus.APPROVED) {
            new ReturnInspectionDialog(SwingUtilities.getWindowAncestor(this), b, bookingDAO, this::reloadData).setVisible(true);
        }
    }

    private void updateBookingButtonStates() {
        Booking b = getSelectedBooking();
        if (b == null) {
            approveBtn.setEnabled(false);
            rejectBtn.setEnabled(false);
            returnBtn.setEnabled(false);
            notesDisplayField.setText("Select a booking from the table to view customer notes and due date status.");
            return;
        }

        String notes = (b.getNotes() != null && !b.getNotes().trim().isEmpty()) ? b.getNotes() : "No special notes provided";
        String dueStatus = b.getDueStatus();
        notesDisplayField.setText("Customer: " + b.getCustomerName() + " (" + b.getCustomerPhone() + ") | Due Status: " + dueStatus + " | Notes: " + notes);

        if (b.getStatus() == BookingStatus.PENDING) {
            approveBtn.setEnabled(true);
            rejectBtn.setEnabled(true);
            returnBtn.setEnabled(false);
        } else if (b.getStatus() == BookingStatus.APPROVED) {
            approveBtn.setEnabled(false);
            rejectBtn.setEnabled(false);
            returnBtn.setEnabled(true);
        } else {
            approveBtn.setEnabled(false);
            rejectBtn.setEnabled(false);
            returnBtn.setEnabled(false);
        }
    }

    private void showAddVehicleDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Add New Vehicle Listing / EV", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(620, 680);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Vehicle Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"CAR", "BIKE"});
        typeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Brand:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; JTextField brandField = new JTextField(15); brandField.setFont(new Font("Arial", Font.PLAIN, 14)); form.add(brandField, gbc);

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Model:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; JTextField modelField = new JTextField(15); modelField.setFont(new Font("Arial", Font.PLAIN, 14)); form.add(modelField, gbc);

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; JTextField yearField = new JTextField("2024", 15); yearField.setFont(new Font("Arial", Font.PLAIN, 14)); form.add(yearField, gbc);

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Colour:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; JTextField colourField = new JTextField("Pearl White", 15); colourField.setFont(new Font("Arial", Font.PLAIN, 14)); form.add(colourField, gbc);

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Mileage:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; JTextField mileageField = new JTextField("18 km/l", 15); mileageField.setFont(new Font("Arial", Font.PLAIN, 14)); form.add(mileageField, gbc);

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("License Plate:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; JTextField plateField = new JTextField(15); plateField.setFont(new Font("Arial", Font.PLAIN, 14)); form.add(plateField, gbc);

        gbc.gridx = 0; gbc.gridy = row; form.add(new JLabel("Daily Rate ($):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; JTextField rateField = new JTextField("45.00", 15); rateField.setFont(new Font("Arial", Font.PLAIN, 14)); form.add(rateField, gbc);

        // EV Checkbox
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JCheckBox evCheckBox = new JCheckBox("⚡ 100% Electric Vehicle (Zero Emission EV)");
        evCheckBox.setFont(new Font("Arial", Font.BOLD, 14));
        evCheckBox.setForeground(new Color(5, 150, 105));
        form.add(evCheckBox, gbc);
        row++;

        // Car fields
        JLabel seatsLabel = new JLabel("Seats:");
        JTextField seatsField = new JTextField("5", 15);
        JLabel transLabel = new JLabel("Transmission:");
        JComboBox<String> transCombo = new JComboBox<>(new String[]{"Automatic", "Manual"});
        JLabel fuelLabel = new JLabel("Fuel Type:");
        JComboBox<String> fuelCombo = new JComboBox<>(new String[]{"Petrol", "Diesel", "Electric", "Hybrid"});

        // Bike fields
        JLabel ccLabel = new JLabel("Engine (CC):");
        JTextField ccField = new JTextField("150", 15);
        JLabel bikeTypeLabel = new JLabel("Bike Category:");
        JComboBox<String> bikeTypeCombo = new JComboBox<>(new String[]{"Standard", "Sport", "Cruiser", "Scooter"});

        // EV Extra Fields
        JLabel batteryLabel = new JLabel("Battery (kWh):");
        JTextField batteryField = new JTextField("60.0", 15);
        JLabel rangeLabel = new JLabel("Range (km):");
        JTextField rangeField = new JTextField("450", 15);

        // Panel holding type specific fields
        JPanel dynamicPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        dynamicPanel.add(seatsLabel); dynamicPanel.add(seatsField);
        dynamicPanel.add(transLabel); dynamicPanel.add(transCombo);
        dynamicPanel.add(fuelLabel); dynamicPanel.add(fuelCombo);
        dynamicPanel.add(batteryLabel); dynamicPanel.add(batteryField);

        batteryLabel.setVisible(false);
        batteryField.setVisible(false);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        form.add(dynamicPanel, gbc);

        // Condition Checkmarks Panel
        JPanel conditionPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        conditionPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Inspection & Condition Checkmarks"));
        JCheckBox tyreBox = new JCheckBox("✓ Tyres in Good Condition", true);
        JCheckBox engineBox = new JCheckBox("✓ Engine Inspected & Certified", true);
        JCheckBox gpsBox = new JCheckBox("✓ GPS Navigation Included", true);
        JCheckBox insuranceBox = new JCheckBox("✓ Comprehensive Insurance Verified", true);

        conditionPanel.add(tyreBox);
        conditionPanel.add(engineBox);
        conditionPanel.add(gpsBox);
        conditionPanel.add(insuranceBox);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        form.add(conditionPanel, gbc);

        evCheckBox.addActionListener(e -> {
            boolean isEv = evCheckBox.isSelected();
            batteryLabel.setVisible(isEv);
            batteryField.setVisible(isEv);
            if (isEv) {
                fuelCombo.setSelectedItem("Electric");
                mileageField.setText("Zero Emission");
            } else {
                fuelCombo.setSelectedItem("Petrol");
                mileageField.setText("18 km/l");
            }
            dynamicPanel.revalidate();
            dynamicPanel.repaint();
        });

        typeCombo.addActionListener(e -> {
            String selected = (String) typeCombo.getSelectedItem();
            dynamicPanel.removeAll();
            if ("CAR".equalsIgnoreCase(selected)) {
                dynamicPanel.add(seatsLabel); dynamicPanel.add(seatsField);
                dynamicPanel.add(transLabel); dynamicPanel.add(transCombo);
                dynamicPanel.add(fuelLabel); dynamicPanel.add(fuelCombo);
                if (evCheckBox.isSelected()) {
                    dynamicPanel.add(batteryLabel); dynamicPanel.add(batteryField);
                }
            } else {
                dynamicPanel.add(ccLabel); dynamicPanel.add(ccField);
                dynamicPanel.add(bikeTypeLabel); dynamicPanel.add(bikeTypeCombo);
                if (evCheckBox.isSelected()) {
                    dynamicPanel.add(batteryLabel); dynamicPanel.add(batteryField);
                }
            }
            dynamicPanel.revalidate();
            dynamicPanel.repaint();
        });

        // Bottom dialog buttons
        JPanel dlgBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save Vehicle");
        saveBtn.setFont(new Font("Arial", Font.BOLD, 14));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        dlgBtns.add(saveBtn);
        dlgBtns.add(cancelBtn);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(dlgBtns, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                String type = (String) typeCombo.getSelectedItem();
                String brand = brandField.getText().trim();
                String model = modelField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());
                String colour = colourField.getText().trim();
                String mileage = mileageField.getText().trim();
                String plate = plateField.getText().trim();
                double rate = Double.parseDouble(rateField.getText().trim());

                if (brand.isEmpty() || model.isEmpty() || plate.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                boolean tyre = tyreBox.isSelected();
                boolean engine = engineBox.isSelected();
                boolean gps = gpsBox.isSelected();
                boolean ins = insuranceBox.isSelected();
                boolean isEv = evCheckBox.isSelected();

                Vehicle newVehicle;
                if ("CAR".equalsIgnoreCase(type)) {
                    int seats = Integer.parseInt(seatsField.getText().trim());
                    String trans = (String) transCombo.getSelectedItem();
                    String fuel = (String) fuelCombo.getSelectedItem();

                    if (isEv) {
                        double battery = Double.parseDouble(batteryField.getText().trim());
                        newVehicle = new ElectricCar(0, currentAgency.getAgencyId(), brand, model, year, plate, rate,
                                VehicleStatus.AVAILABLE, seats, trans, battery, 450, 0.75,
                                colour, mileage, tyre, engine, gps, ins);
                    } else {
                        newVehicle = new Car(0, currentAgency.getAgencyId(), brand, model, year, plate, rate,
                                VehicleStatus.AVAILABLE, seats, trans, fuel,
                                colour, mileage, tyre, engine, gps, ins);
                    }
                } else {
                    int cc = Integer.parseInt(ccField.getText().trim());
                    String bType = (String) bikeTypeCombo.getSelectedItem();
                    if (isEv) {
                        double battery = Double.parseDouble(batteryField.getText().trim());
                        newVehicle = new ElectricBike(0, currentAgency.getAgencyId(), brand, model, year, plate, rate,
                                VehicleStatus.AVAILABLE, battery, 120, colour, mileage, tyre, engine, gps, ins);
                    } else {
                        newVehicle = new Bike(0, currentAgency.getAgencyId(), brand, model, year, plate, rate,
                                VehicleStatus.AVAILABLE, cc, bType, colour, mileage, tyre, engine, gps, ins);
                    }
                }

                boolean ok = vehicleDAO.addVehicle(newVehicle);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Vehicle successfully added to your catalog!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    reloadData();
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numeric values for year, rate, seats/cc, and battery.", "Format Error", JOptionPane.ERROR_MESSAGE);
            } catch (DatabaseException dbe) {
                JOptionPane.showMessageDialog(dialog, dbe.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private void toggleVehicleStatus() {
        int row = vehicleTable.getSelectedRow();
        if (row < 0 || row >= currentVehicleList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vehicle v = currentVehicleList.get(row);
        if (v.getStatus() == VehicleStatus.RENTED) {
            JOptionPane.showMessageDialog(this, "Cannot toggle status while vehicle is actively RENTED to a customer.", "Action Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }

        VehicleStatus newStatus = (v.getStatus() == VehicleStatus.AVAILABLE) ? VehicleStatus.MAINTENANCE : VehicleStatus.AVAILABLE;
        try {
            boolean ok = vehicleDAO.updateVehicleStatus(v.getId(), newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Vehicle status updated to: " + newStatus.name(), "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                reloadData();
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedVehicle() {
        int row = vehicleTable.getSelectedRow();
        if (row < 0 || row >= currentVehicleList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vehicle v = currentVehicleList.get(row);
        if (v.getStatus() == VehicleStatus.RENTED) {
            JOptionPane.showMessageDialog(this, "Cannot delete a vehicle that is currently RENTED.", "Action Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete " + v.getBrand() + " " + v.getModel() + " (" + v.getLicensePlate() + ")?\n" +
                "This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                boolean ok = vehicleDAO.deleteVehicle(v.getId());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Vehicle deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    reloadData();
                }
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateBookingStatus(BookingStatus newStatus) {
        Booking b = getSelectedBooking();
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String actionWord = (newStatus == BookingStatus.APPROVED) ? "approve" : "reject";
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + actionWord + " booking #" + b.getId() + " for " + b.getCustomerName() + "?",
                "Confirm " + actionWord.substring(0, 1).toUpperCase() + actionWord.substring(1),
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                boolean ok = bookingDAO.updateBookingStatus(b.getId(), newStatus);
                if (ok) {
                    // Send notification to customer
                    int customerUserId = bookingDAO.getCustomerUserId(b.getCustomerId());
                    if (customerUserId > 0) {
                        if (newStatus == BookingStatus.APPROVED) {
                            NotificationService.getInstance().onBookingApproved(customerUserId, b.getVehicleInfo(), currentAgency.getAgencyName());
                        } else if (newStatus == BookingStatus.REJECTED) {
                            NotificationService.getInstance().onBookingRejected(customerUserId, b.getVehicleInfo(), currentAgency.getAgencyName());
                        }
                    }

                    JOptionPane.showMessageDialog(this, "Booking #" + b.getId() + " has been " + newStatus.name() + ".");
                    reloadData();
                }
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void reloadData() {
        if (currentAgency != null) {
            loadAgencyVehicles();
            loadAgencyBookings();
            updateNotificationCount();
        }
    }

    private void updateNotificationCount() {
        if (currentAgency != null) {
            int unread = notificationDAO.getUnreadCount(currentAgency.getId());
            notifBtn.setText("🔔 Notifications (" + unread + ")");
            if (unread > 0) {
                notifBtn.setForeground(new Color(220, 38, 38));
            } else {
                notifBtn.setForeground(new Color(26, 54, 93));
            }
        }
    }

    private void loadAgencyVehicles() {
        vehicleTableModel.setRowCount(0);
        try {
            currentVehicleList = vehicleDAO.getVehiclesByAgency(currentAgency.getAgencyId());
            for (Vehicle v : currentVehicleList) {
                String details = "";
                if (v instanceof ElectricCar ec) {
                    details = "⚡ " + ec.getBatteryCapacityKwh() + "kWh EV, " + ec.getRangeKm() + "km range, " + ec.getTransmission();
                } else if (v instanceof ElectricBike eb) {
                    details = "⚡ " + eb.getBatteryCapacityKwh() + "kWh EV, " + eb.getRangeKm() + "km range";
                } else if (v instanceof Car car) {
                    details = car.getSeatingCapacity() + " Seats, " + car.getTransmission() + ", " + car.getFuelType();
                } else if (v instanceof Bike bike) {
                    details = bike.getEngineCc() + "cc, " + bike.getBikeType();
                }

                String cond = (v.isTyreConditionGood() ? "✓ Tyres" : "✗ Tyres") + " | " +
                              (v.isEngineConditionGood() ? "✓ Engine" : "✗ Engine");

                vehicleTableModel.addRow(new Object[]{
                        v.getId(),
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
            JOptionPane.showMessageDialog(this, "Failed to load vehicles: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAgencyBookings() {
        bookingTableModel.setRowCount(0);
        try {
            currentBookingList = bookingDAO.getBookingsByAgency(currentAgency.getAgencyId());
            for (Booking b : currentBookingList) {
                String notes = (b.getNotes() != null && !b.getNotes().trim().isEmpty()) ? b.getNotes() : "None";
                String dueStatus = b.getDueStatus();
                bookingTableModel.addRow(new Object[]{
                        b.getId(),
                        b.getCustomerName(),
                        b.getCustomerPhone(),
                        b.getVehicleInfo(),
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
            updateBookingButtonStates();
        } catch (DatabaseException e) {
            System.err.println("Error loading agency bookings: " + e.getMessage());
        }
    }
}
