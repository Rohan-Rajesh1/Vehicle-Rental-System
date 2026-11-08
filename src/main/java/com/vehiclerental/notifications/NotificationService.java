package com.vehiclerental.notifications;

import com.vehiclerental.dao.NotificationDAO;
import java.util.ArrayList;
import java.util.List;

/**
 * Observer Pattern implementation for platform events and notification dispatching.
 */
public class NotificationService {
    private static NotificationService instance;
    private final NotificationDAO notificationDAO;
    private final List<RentalEventListener> listeners = new ArrayList<>();

    public interface RentalEventListener {
        void onEvent(String eventType, int targetUserId, String title, String message);
    }

    private NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public void addListener(RentalEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(RentalEventListener listener) {
        listeners.remove(listener);
    }

    public void notifyUser(int userId, String title, String message, String eventType) {
        // Persist to database
        notificationDAO.sendNotification(userId, title, message);

        // Notify active runtime observers
        for (RentalEventListener listener : listeners) {
            try {
                listener.onEvent(eventType, userId, title, message);
            } catch (Exception ignored) {}
        }
    }

    // Convenience event triggers
    public void onBookingSubmitted(int agencyUserId, String vehicleInfo, String customerName) {
        notifyUser(agencyUserId,
                "New Booking Request Placed",
                "Customer " + customerName + " has requested to book " + vehicleInfo + ". Please review and approve/reject.",
                "BOOKING_SUBMITTED");
    }

    public void onBookingApproved(int customerUserId, String vehicleInfo, String agencyName) {
        notifyUser(customerUserId,
                "Booking Approved!",
                "Great news! Your booking for " + vehicleInfo + " was approved by " + agencyName + ". Have a safe trip!",
                "BOOKING_APPROVED");
    }

    public void onBookingRejected(int customerUserId, String vehicleInfo, String agencyName) {
        notifyUser(customerUserId,
                "Booking Request Declined",
                "Your booking request for " + vehicleInfo + " was declined by " + agencyName + ".",
                "BOOKING_REJECTED");
    }

    public void onVehicleReturned(int customerUserId, String vehicleInfo, double finalTotal) {
        notifyUser(customerUserId,
                "Rental Completed & Vehicle Returned",
                "Your rental for " + vehicleInfo + " has been marked as returned. Final settlement: $" +
                        String.format("%.2f", finalTotal) + ". You can now rate your experience!",
                "VEHICLE_RETURNED");
    }

    public void onAgencyApproved(int agencyUserId, String agencyName) {
        notifyUser(agencyUserId,
                "Agency Registration Approved",
                "Congratulations " + agencyName + "! Your rental agency has been approved by the Administrator. You can now list vehicles.",
                "AGENCY_APPROVED");
    }

    public void onAgencyRejected(int agencyUserId, String agencyName) {
        notifyUser(agencyUserId,
                "Agency Registration Rejected",
                "Your rental agency registration for " + agencyName + " was declined by the Administrator.",
                "AGENCY_REJECTED");
    }

    public void onReviewSubmitted(int agencyUserId, String vehicleInfo, int rating) {
        notifyUser(agencyUserId,
                "New Customer Review",
                "A customer just left a " + rating + "-star rating for " + vehicleInfo + "!",
                "REVIEW_RECEIVED");
    }
}
