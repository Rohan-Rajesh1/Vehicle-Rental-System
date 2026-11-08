package com.vehiclerental.ui.agency;

import com.vehiclerental.dao.BookingDAO;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.Booking;
import com.vehiclerental.notifications.NotificationService;

import javax.swing.*;
import java.awt.*;

/**
 * Return Handover & Inspection Checklist Dialog.
 * Allows the agency to verify vehicle return condition, fuel level,
 * odometer distance, and compute any refueling or late return penalties.
 */
public class ReturnInspectionDialog extends JDialog {
    private final Booking booking;
    private final BookingDAO bookingDAO;
    private final Runnable onSuccessCallback;

    private JSpinner startOdoSpinner;
    private JSpinner returnOdoSpinner;
    private JLabel distanceTraveledLabel;
    private JComboBox<String> fuelCombo;
    private JLabel latePenaltyLabel;
    private JTextArea inspectionNotesArea;
    private JLabel finalSettlementLabel;

    private double calculatedFuelFee = 0.0;
    private double calculatedLateFee = 0.0;

    public ReturnInspectionDialog(Window parent, Booking booking, BookingDAO bookingDAO, Runnable onSuccessCallback) {
        super(parent, "Vehicle Handover & Return Inspection Checklist", ModalityType.APPLICATION_MODAL);
        this.booking = booking;
        this.bookingDAO = bookingDAO;
        this.onSuccessCallback = onSuccessCallback;
        initUI();
    }

    private void initUI() {
        setSize(700, 680);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel topHeader = new JPanel(new GridLayout(2, 1, 4, 4));
        topHeader.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));
        JLabel title = new JLabel("Vehicle Handover & Check-in Inspection", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        JLabel subtitle = new JLabel("Booking #" + booking.getId() + " - " + booking.getVehicleInfo() + " | Customer: " + booking.getCustomerName(), SwingConstants.LEFT);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 15));
        topHeader.add(title);
        topHeader.add(subtitle);
        add(topHeader, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        // 1. Odometer Inspection
        JPanel odoCard = createTitledCard("1. Odometer Inspection (Distance Traveled)");
        odoCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        odoCard.add(new JLabel("Departure Odometer (km):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        startOdoSpinner = new JSpinner(new SpinnerNumberModel(12000, 0, 999999, 10));
        odoCard.add(startOdoSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        odoCard.add(new JLabel("Return Odometer (km):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        returnOdoSpinner = new JSpinner(new SpinnerNumberModel(12250, 0, 999999, 10));
        odoCard.add(returnOdoSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        distanceTraveledLabel = new JLabel("<html>Distance Driven: <b>250 km</b></html>");
        distanceTraveledLabel.setFont(new Font("Arial", Font.BOLD, 14));
        distanceTraveledLabel.setForeground(new Color(26, 54, 93));
        odoCard.add(distanceTraveledLabel, gbc);
        centerPanel.add(odoCard);
        centerPanel.add(Box.createVerticalStrut(10));

        // 2. Fuel & Penalty Calculation
        JPanel fuelCard = createTitledCard("2. Fuel Level & Overdue Settlement");
        fuelCard.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        fuelCard.add(new JLabel("Return Fuel / Battery Level:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        fuelCombo = new JComboBox<>(new String[]{
            "100% Full / Recharged (No Refuel Fee)",
            "75% Level (+$15.00 Refueling Fee)",
            "50% Level (+$30.00 Refueling Fee)",
            "Low / Empty (+$50.00 Refueling Fee)"
        });
        fuelCard.add(fuelCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        fuelCard.add(new JLabel("Due Date Overdue Status:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;

        Long daysLeft = booking.getRawDaysLeft();
        if (daysLeft != null && daysLeft < 0) {
            long overdueDays = Math.abs(daysLeft);
            double daily = booking.getTotalCost() / Math.max(1, booking.getTotalDays());
            calculatedLateFee = Math.round(overdueDays * daily * 1.5 * 100.0) / 100.0; // 1.5x late rate
            latePenaltyLabel = new JLabel("<html><font color='#DC2626'><b>Overdue by " + overdueDays + " days! (Penalty: +$" + String.format("%.2f", calculatedLateFee) + ")</b></font></html>");
        } else {
            calculatedLateFee = 0.0;
            latePenaltyLabel = new JLabel("<html><font color='#059669'><b>On-time Return (No Late Penalty)</b></font></html>");
        }
        fuelCard.add(latePenaltyLabel, gbc);
        centerPanel.add(fuelCard);
        centerPanel.add(Box.createVerticalStrut(10));

        // 3. Condition Checklist
        JPanel checkCard = createTitledCard("3. Physical Condition Certification");
        checkCard.setLayout(new GridLayout(2, 2, 8, 6));
        JCheckBox c1 = new JCheckBox("Exterior Clean / No New Dents", true);
        JCheckBox c2 = new JCheckBox("Interior Sanitized & Checked", true);
        JCheckBox c3 = new JCheckBox("Keys, Documents & Accessories Returned", true);
        JCheckBox c4 = new JCheckBox("Tyres & Brakes Inspected", true);
        checkCard.add(c1);
        checkCard.add(c2);
        checkCard.add(c3);
        checkCard.add(c4);
        centerPanel.add(checkCard);
        centerPanel.add(Box.createVerticalStrut(10));

        // 4. Notes & Final Settlement Summary
        JPanel settleCard = createTitledCard("4. Final Settlement Summary");
        settleCard.setLayout(new BorderLayout(8, 8));
        inspectionNotesArea = new JTextArea("Vehicle returned in good condition. Handover inspection passed.", 2, 20);
        inspectionNotesArea.setFont(new Font("Arial", Font.PLAIN, 14));
        settleCard.add(new JScrollPane(inspectionNotesArea), BorderLayout.CENTER);

        finalSettlementLabel = new JLabel();
        finalSettlementLabel.setFont(new Font("Arial", Font.BOLD, 16));
        settleCard.add(finalSettlementLabel, BorderLayout.SOUTH);
        centerPanel.add(settleCard);

        add(new JScrollPane(centerPanel), BorderLayout.CENTER);

        // Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 15));
        JButton confirmBtn = new JButton("Complete Handover & Mark Returned");
        confirmBtn.setFont(new Font("Arial", Font.BOLD, 15));
        confirmBtn.setBackground(new Color(5, 150, 105));

        bottomBar.add(cancelBtn);
        bottomBar.add(confirmBtn);
        add(bottomBar, BorderLayout.SOUTH);

        // Listeners
        startOdoSpinner.addChangeListener(e -> updateOdoDistance());
        returnOdoSpinner.addChangeListener(e -> updateOdoDistance());
        fuelCombo.addActionListener(e -> updateSettlement());

        cancelBtn.addActionListener(e -> dispose());
        confirmBtn.addActionListener(e -> submitReturnInspection());

        updateOdoDistance();
        updateSettlement();
    }

    private JPanel createTitledCard(String title) {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title, 0, 0, new Font("Arial", Font.BOLD, 15), new Color(26, 54, 93)
        ));
        return p;
    }

    private void updateOdoDistance() {
        int start = (int) startOdoSpinner.getValue();
        int ret = (int) returnOdoSpinner.getValue();
        int diff = Math.max(0, ret - start);
        distanceTraveledLabel.setText("<html>Distance Driven: <b>" + diff + " km</b> (From " + start + " km to " + ret + " km)</html>");
    }

    private void updateSettlement() {
        int fuelIdx = fuelCombo.getSelectedIndex();
        switch (fuelIdx) {
            case 1 -> calculatedFuelFee = 15.00;
            case 2 -> calculatedFuelFee = 30.00;
            case 3 -> calculatedFuelFee = 50.00;
            default -> calculatedFuelFee = 0.00;
        }

        double base = booking.getTotalCost();
        double total = base + calculatedFuelFee + calculatedLateFee;

        String breakdown = String.format("Base: $%.2f", base);
        if (calculatedFuelFee > 0) breakdown += String.format(" + Refuel: $%.2f", calculatedFuelFee);
        if (calculatedLateFee > 0) breakdown += String.format(" + Late Penalty: $%.2f", calculatedLateFee);

        finalSettlementLabel.setText("<html><font color='#047857'>" + breakdown + " = <b>Final Settlement: $" + String.format("%.2f", total) + "</b></font></html>");
    }

    private void submitReturnInspection() {
        int start = (int) startOdoSpinner.getValue();
        int ret = (int) returnOdoSpinner.getValue();
        int diff = Math.max(0, ret - start);

        String notes = inspectionNotesArea.getText().trim();
        String fullInspectionSummary = "Distance: " + diff + "km | Fuel: " + fuelCombo.getSelectedItem() + " | Notes: " + notes;

        try {
            boolean ok = bookingDAO.completeReturnInspection(booking.getId(), calculatedFuelFee, calculatedLateFee, fullInspectionSummary);
            if (ok) {
                // Dispatch event notification to Customer
                int customerUserId = bookingDAO.getCustomerUserId(booking.getCustomerId());
                if (customerUserId > 0) {
                    double finalTotal = booking.getTotalCost() + calculatedFuelFee + calculatedLateFee;
                    NotificationService.getInstance().onVehicleReturned(customerUserId, booking.getVehicleInfo(), finalTotal);
                }

                JOptionPane.showMessageDialog(this,
                        "Vehicle successfully inspected and marked as RETURNED!\n\n" +
                        "Vehicle Status: Restored to AVAILABLE in catalog\n" +
                        "Distance Driven: " + diff + " km\n" +
                        "Final Settlement Total: $" + String.format("%.2f", booking.getTotalCost() + calculatedFuelFee + calculatedLateFee) + "\n\n" +
                        "Customer has been notified and can now rate their rental.",
                        "Handover Completed", JOptionPane.INFORMATION_MESSAGE);

                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to complete return inspection in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error saving inspection: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
