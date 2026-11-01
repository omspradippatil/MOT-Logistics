package com.transport;

import com.transport.db.DatabaseConnection;
import com.transport.ui.LoginFrame;

import javax.swing.*;

/**
 * Main.java - Application Entry Point for MOT Logistics
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("====================================================");
        System.out.println("     MOT LOGISTICS - Starting Desktop System...     ");
        System.out.println("====================================================");

        // Pre-flight database connectivity check
        System.out.print("Connecting to MySQL Database 'transport_db'... ");
        if (DatabaseConnection.testConnection()) {
            System.out.println("ONLINE & READY!");
        } else {
            System.out.println("OFFLINE / FAILED!");
            JOptionPane.showMessageDialog(null,
                "Unable to connect to MySQL database 'transport_db'!\n\n" +
                "Troubleshooting Steps:\n" +
                "1. Confirm MySQL server is running (e.g. brew services start mysql)\n" +
                "2. Ensure database 'transport_db' is imported (mysql -u root < database/schema.sql)\n" +
                "3. Check password in DatabaseConnection.java (default is blank)\n" +
                "4. Verify lib/mysql-connector-j.jar is present",
                "MOT Logistics - Database Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Apply clean Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Using default Swing Look and Feel: " + e.getMessage());
        }

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            System.out.println("MOT Logistics UI successfully initialized.");
        });
    }
}
