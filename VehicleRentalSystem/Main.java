package com.vehiclerental;

import com.vehiclerental.db.DatabaseConnection;
import com.vehiclerental.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Use system look and feel for native presentation
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to standard cross-platform look and feel
        }

        // Increase global font and content sizes for enhanced visibility
        java.awt.Font baseFont = new java.awt.Font("Arial", java.awt.Font.PLAIN, 15);
        java.awt.Font boldFont = new java.awt.Font("Arial", java.awt.Font.BOLD, 15);
        String[] plainKeys = {
            "Label.font", "TextField.font", "PasswordField.font", "TextArea.font",
            "ComboBox.font", "List.font", "Table.font", "Spinner.font",
            "CheckBox.font", "RadioButton.font", "ToolTip.font", "OptionPane.messageFont"
        };
        String[] boldKeys = {
            "Button.font", "TableHeader.font", "TabbedPane.font", "TitledBorder.font",
            "Menu.font", "MenuItem.font", "OptionPane.buttonFont"
        };
        for (String k : plainKeys) {
            UIManager.put(k, baseFont);
        }
        for (String k : boldKeys) {
            UIManager.put(k, boldFont);
        }

        System.out.println("=================================================");
        System.out.println("  VEHICLE RENTAL SYSTEM - Centralized Platform   ");
        System.out.println("  Java 21 / Swing / MySQL JDBC                   ");
        System.out.println("=================================================");
        
        boolean connected = DatabaseConnection.testConnection();
        if (connected) {
            System.out.println("-> MySQL Database connected successfully!");
        } else {
            System.err.println("-> MySQL is currently offline or unreachable at: " + DatabaseConnection.getUrl());
            System.err.println("-> Please make sure MySQL server is running and database 'vehicle_rental_db' is imported from database/schema.sql.");
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
