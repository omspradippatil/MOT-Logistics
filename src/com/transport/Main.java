package com.transport;

import com.transport.db.DatabaseConnection;
import com.transport.ui.LoginFrame;

import javax.swing.*;

/**
 * Main.java - Application Entry Point
 *
 * This is where the program starts.
 * The main() method is the STARTING POINT of every Java application.
 *
 * What this does:
 * 1. Tests the database connection
 * 2. Sets the UI look and feel
 * 3. Opens the Login window
 *
 * KEY CONCEPTS:
 * - main() method (program entry point)
 * - SwingUtilities.invokeLater() (ensures UI runs on the right thread)
 * - UIManager (changes the look of Swing components)
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("  SBP Kubre Tempo Service - Starting App...  ");
        System.out.println("==============================================");

        // Step 1: Test database connection
        System.out.print("Testing database connection... ");
        if (DatabaseConnection.testConnection()) {
            System.out.println("SUCCESS!");
        } else {
            System.out.println("FAILED!");
            JOptionPane.showMessageDialog(null,
                "Cannot connect to MySQL database!\n\n" +
                "Please check:\n" +
                "1. MySQL is running\n" +
                "2. Database 'transport_db' exists (run database/schema.sql)\n" +
                "3. Password in DatabaseConnection.java is correct\n" +
                "4. mysql-connector-j.jar is in the lib/ folder",
                "Database Connection Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Stop the app
        }

        // Step 2: Set a clean look and feel for Swing UI
        try {
            // Use the system's native look (looks like a real Windows/Mac/Linux app)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If it fails, use default Swing look - not critical
            System.out.println("Could not set system look and feel: " + e.getMessage());
        }

        // Step 3: Open the Login window
        // SwingUtilities.invokeLater ensures UI is created on the Event Dispatch Thread
        // This is the CORRECT way to start a Swing application
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            System.out.println("Login window opened. Application is running.");
        });
    }
}
