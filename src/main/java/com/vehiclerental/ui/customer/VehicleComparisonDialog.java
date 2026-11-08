package com.vehiclerental.ui.customer;

import com.vehiclerental.dao.ReviewDAO;
import com.vehiclerental.models.Bike;
import com.vehiclerental.models.Car;
import com.vehiclerental.models.ElectricBike;
import com.vehiclerental.models.ElectricCar;
import com.vehiclerental.models.Vehicle;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Interactive Side-by-Side Comparison Dialog for two selected vehicles.
 */
public class VehicleComparisonDialog extends JDialog {
    private final Vehicle vehicleA;
    private final Vehicle vehicleB;
    private final Consumer<Vehicle> onSelectCallback;
    private final ReviewDAO reviewDAO;

    public VehicleComparisonDialog(Window parent, Vehicle vehicleA, Vehicle vehicleB, Consumer<Vehicle> onSelectCallback) {
        super(parent, "Vehicle Comparison Tool - Side-by-Side Analysis", ModalityType.APPLICATION_MODAL);
        this.vehicleA = vehicleA;
        this.vehicleB = vehicleB;
        this.onSelectCallback = onSelectCallback;
        this.reviewDAO = new ReviewDAO();
        initUI();
    }

    private void initUI() {
        setSize(850, 680);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel topHeader = new JPanel(new GridLayout(2, 1, 4, 4));
        topHeader.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));
        JLabel title = new JLabel("Side-by-Side Vehicle Comparison", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        JLabel subtitle = new JLabel("Compare specifications, rental rates, condition checks, and eco-ratings to make the best choice.", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        topHeader.add(title);
        topHeader.add(subtitle);
        add(topHeader, BorderLayout.NORTH);

        // Center: 2 Comparison Cards
        JPanel comparisonPanel = new JPanel(new GridLayout(1, 2, 16, 16));
        comparisonPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        comparisonPanel.add(createVehicleCard(vehicleA, "Option 1"));
        comparisonPanel.add(createVehicleCard(vehicleB, "Option 2"));

        add(new JScrollPane(comparisonPanel), BorderLayout.CENTER);

        // Bottom close button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        JButton closeBtn = new JButton("Close Comparison");
        closeBtn.setFont(new Font("Arial", Font.PLAIN, 15));
        closeBtn.addActionListener(e -> dispose());
        bottomBar.add(closeBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createVehicleCard(Vehicle v, String badgeTitle) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225), 2, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        card.setBackground(Color.WHITE);

        // Card Header
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(Color.WHITE);
        JLabel badge = new JLabel(badgeTitle.toUpperCase());
        badge.setFont(new Font("Arial", Font.BOLD, 12));
        badge.setForeground(new Color(100, 116, 139));
        
        JLabel name = new JLabel(v.getBrand() + " " + v.getModel() + " (" + v.getYear() + ")");
        name.setFont(new Font("Arial", Font.BOLD, 18));
        name.setForeground(new Color(26, 54, 93));

        head.add(badge, BorderLayout.NORTH);
        head.add(name, BorderLayout.CENTER);
        card.add(head, BorderLayout.NORTH);

        // Body with Specs
        double avgRating = reviewDAO.getAverageRatingForVehicle(v.getId());
        String ratingStars = "★".repeat(Math.max(1, (int) Math.round(avgRating))) + "☆".repeat(Math.max(0, 5 - (int) Math.round(avgRating)));

        StringBuilder html = new StringBuilder("<html><body style='font-family: Arial; font-size: 11px; line-height: 1.6;'>");
        html.append("<table width='100%' cellpadding='4' cellspacing='0'>");
        
        appendRow(html, "Rental Rate", "<font color='#047857' size='+1'><b>$" + String.format("%.2f", v.getDailyRate()) + "</b> / day</font>");
        appendRow(html, "Agency", "<b>" + v.getAgencyName() + "</b>");
        appendRow(html, "Vehicle Type", v.getVehicleType());
        appendRow(html, "Customer Rating", "<font color='#D97706'><b>" + ratingStars + "</b> (" + String.format("%.1f", avgRating) + "/5.0)</font>");
        appendRow(html, "Colour & Mileage", v.getColour() + " | " + v.getMileage());
        
        if (v instanceof ElectricCar ec) {
            appendRow(html, "Powertrain", "<font color='#2563EB'><b>⚡ 100% Electric (EV)</b></font>");
            appendRow(html, "Battery & Range", ec.getBatteryCapacityKwh() + " kWh | " + ec.getRangeKm() + " km range");
            appendRow(html, "Fast Charging", ec.getChargingTimeHours() + " hours (80%)");
            appendRow(html, "Eco Impact", "<font color='#059669'>🌱 ~" + ec.calculateCarbonSavedKg(3) + " kg CO₂ saved in 3 days</font>");
        } else if (v instanceof ElectricBike eb) {
            appendRow(html, "Powertrain", "<font color='#2563EB'><b>⚡ Electric Scooter / EV</b></font>");
            appendRow(html, "Battery & Range", eb.getBatteryCapacityKwh() + " kWh | " + eb.getRangeKm() + " km range");
            appendRow(html, "Eco Impact", "<font color='#059669'>🌱 ~" + eb.calculateCarbonSavedKg(3) + " kg CO₂ saved in 3 days</font>");
        } else if (v instanceof Car c) {
            appendRow(html, "Capacity & Gear", c.getSeatingCapacity() + " Seats | " + c.getTransmission());
            appendRow(html, "Fuel Type", c.getFuelType());
        } else if (v instanceof Bike b) {
            appendRow(html, "Engine & Type", b.getEngineCc() + "cc | " + b.getBikeType());
        }

        String condStr = (v.isTyreConditionGood() ? "<font color='#059669'>✓ Good Tyres</font>" : "<font color='#DC2626'>✗ Tyres Worn</font>") + "<br>" +
                         (v.isEngineConditionGood() ? "<font color='#059669'>✓ Engine Certified</font>" : "<font color='#DC2626'>✗ Needs Check</font>") + "<br>" +
                         (v.isGpsEnabled() ? "<font color='#2563EB'>✓ GPS Navigation</font>" : "GPS: No") + "<br>" +
                         (v.isInsuranceVerified() ? "<font color='#059669'>✓ Insured</font>" : "Insurance: Pending");
        appendRow(html, "Safety & Inspection", condStr);
        appendRow(html, "Availability", v.getStatus() == com.vehiclerental.models.VehicleStatus.AVAILABLE ? 
                "<font color='#059669'><b>✓ Available Now</b></font>" : "<font color='#DC2626'><b>● Currently Rented</b></font>");

        html.append("</table></body></html>");

        JLabel contentLabel = new JLabel(html.toString());
        card.add(contentLabel, BorderLayout.CENTER);

        // Action Button
        JButton bookBtn = new JButton("Select " + v.getBrand() + " " + v.getModel());
        bookBtn.setFont(new Font("Arial", Font.BOLD, 14));
        bookBtn.setEnabled(v.getStatus() == com.vehiclerental.models.VehicleStatus.AVAILABLE);
        bookBtn.addActionListener(e -> {
            if (onSelectCallback != null) {
                onSelectCallback.accept(v);
            }
            dispose();
        });
        card.add(bookBtn, BorderLayout.SOUTH);

        return card;
    }

    private void appendRow(StringBuilder sb, String label, String value) {
        sb.append("<tr style='border-bottom: 1px solid #f1f5f9;'>")
          .append("<td width='40%' valign='top' style='color: #64748b;'><b>").append(label).append(":</b></td>")
          .append("<td width='60%' valign='top'>").append(value).append("</td>")
          .append("</tr>");
    }
}
