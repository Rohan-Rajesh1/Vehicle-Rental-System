package com.vehiclerental.ui.common;

import com.vehiclerental.dao.NotificationDAO;
import com.vehiclerental.notifications.Notification;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Common in-app notification center modal for Customer, Agency, and Admin.
 */
public class NotificationDialog extends JDialog {
    private final int userId;
    private final NotificationDAO notificationDAO;
    private final Runnable onDismissCallback;

    private DefaultTableModel tableModel;
    private JTable notifTable;

    public NotificationDialog(Window parent, int userId, Runnable onDismissCallback) {
        super(parent, "Notification Center & Activity Feed", ModalityType.APPLICATION_MODAL);
        this.userId = userId;
        this.notificationDAO = new NotificationDAO();
        this.onDismissCallback = onDismissCallback;
        initUI();
    }

    private void initUI() {
        setSize(750, 480);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        JLabel title = new JLabel("In-App Notification Center", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        header.add(title, BorderLayout.WEST);

        JButton markReadBtn = new JButton("Mark All as Read");
        markReadBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        markReadBtn.addActionListener(e -> {
            notificationDAO.markAllAsRead(userId);
            loadNotifications();
            if (onDismissCallback != null) onDismissCallback.run();
        });
        header.add(markReadBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Notifications Table
        String[] cols = {"Status", "Timestamp", "Title", "Message"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        notifTable = new JTable(tableModel);
        notifTable.setRowHeight(36);
        notifTable.setFont(new Font("Arial", Font.PLAIN, 14));
        notifTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        notifTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        add(new JScrollPane(notifTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        closeBtn.addActionListener(e -> {
            if (onDismissCallback != null) onDismissCallback.run();
            dispose();
        });
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        loadNotifications();
    }

    private void loadNotifications() {
        tableModel.setRowCount(0);
        try {
            List<Notification> list = notificationDAO.getNotificationsForUser(userId);
            if (list.isEmpty()) {
                tableModel.addRow(new Object[]{"-", "-", "No notifications", "You are all caught up!"});
                return;
            }
            for (Notification n : list) {
                String status = n.isRead() ? "Read" : "● NEW";
                String time = (n.getCreatedAt() != null) ? n.getCreatedAt().toString().substring(0, 16) : "-";
                tableModel.addRow(new Object[]{status, time, n.getTitle(), n.getMessage()});
            }
        } catch (Exception e) {
            tableModel.addRow(new Object[]{"!", "-", "Error", e.getMessage()});
        }
    }
}
