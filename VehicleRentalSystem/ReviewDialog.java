package com.vehiclerental.ui.customer;

import com.vehiclerental.dao.BookingDAO;
import com.vehiclerental.dao.ReviewDAO;
import com.vehiclerental.exceptions.DatabaseException;
import com.vehiclerental.models.Booking;
import com.vehiclerental.models.Review;
import com.vehiclerental.notifications.NotificationService;

import javax.swing.*;
import java.awt.*;

/**
 * Customer Rating & Feedback Dialog.
 * Allows customers to rate a returned rental from 1 to 5 stars and leave written feedback.
 */
public class ReviewDialog extends JDialog {
    private final Booking booking;
    private final ReviewDAO reviewDAO;
    private final BookingDAO bookingDAO;
    private final Runnable onSuccessCallback;

    private JComboBox<String> starCombo;
    private JTextArea commentArea;

    public ReviewDialog(Window parent, Booking booking, Runnable onSuccessCallback) {
        super(parent, "Rate Your Rental Experience", ModalityType.APPLICATION_MODAL);
        this.booking = booking;
        this.reviewDAO = new ReviewDAO();
        this.bookingDAO = new BookingDAO();
        this.onSuccessCallback = onSuccessCallback;
        initUI();
    }

    private void initUI() {
        setSize(550, 420);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel topHeader = new JPanel(new GridLayout(2, 1, 4, 4));
        topHeader.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));
        JLabel title = new JLabel("Rate & Review Your Rental", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        JLabel subtitle = new JLabel(booking.getVehicleInfo() + " (Agency: " + booking.getAgencyName() + ")", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        topHeader.add(title);
        topHeader.add(subtitle);
        add(topHeader, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel starLbl = new JLabel("Overall Rating:");
        starLbl.setFont(new Font("Arial", Font.BOLD, 15));
        centerPanel.add(starLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        starCombo = new JComboBox<>(new String[]{
            "★★★★★ (5 Stars - Exceptional Experience)",
            "★★★★☆ (4 Stars - Very Good)",
            "★★★☆☆ (3 Stars - Average / Satisfactory)",
            "★★☆☆☆ (2 Stars - Below Average)",
            "★☆☆☆☆ (1 Star - Poor / Needs Improvement)"
        });
        starCombo.setFont(new Font("Arial", Font.PLAIN, 15));
        centerPanel.add(starCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel commentLbl = new JLabel("Your Feedback:");
        commentLbl.setFont(new Font("Arial", Font.BOLD, 15));
        centerPanel.add(commentLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        commentArea = new JTextArea("Clean car, great mileage, and smooth handover! Highly recommended.", 4, 20);
        commentArea.setFont(new Font("Arial", Font.PLAIN, 14));
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        centerPanel.add(new JScrollPane(commentArea), gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton submitBtn = new JButton("Submit Review");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 14));

        cancelBtn.addActionListener(e -> dispose());
        submitBtn.addActionListener(e -> submitReview());

        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void submitReview() {
        int stars = 5 - starCombo.getSelectedIndex();
        String comment = commentArea.getText().trim();
        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide a brief feedback comment.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Review r = new Review(0, booking.getId(), booking.getCustomerId(), booking.getCustomerName(),
                booking.getAgencyId(), booking.getVehicleId(), booking.getVehicleInfo(),
                stars, comment, null);

        try {
            boolean ok = reviewDAO.addReview(r);
            if (ok) {
                // Dispatch notification to agency
                int agencyUserId = bookingDAO.getAgencyUserId(booking.getAgencyId());
                if (agencyUserId > 0) {
                    NotificationService.getInstance().onReviewSubmitted(agencyUserId, booking.getVehicleInfo(), stars);
                }

                JOptionPane.showMessageDialog(this,
                        "Thank you! Your " + stars + "-star review has been posted successfully.",
                        "Review Submitted", JOptionPane.INFORMATION_MESSAGE);

                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit review.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error submitting review: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
