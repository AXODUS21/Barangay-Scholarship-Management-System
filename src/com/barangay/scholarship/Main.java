package com.barangay.scholarship;

import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.MainFrame;
import com.barangay.scholarship.util.SampleDataSeeder;

import javax.swing.*;

/**
 * Main application entry point for the Barangay Scholarship & Educational Assistance Management System.
 */
public class Main {

    public static void main(String[] args) {
        // Enable high-DPI scaling and text anti-aliasing
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Barangay Scholarship System");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            // Use modern system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to standard swing styling if platform L&F is unavailable
        }

        SwingUtilities.invokeLater(() -> {
            // Instantiate service controller
            ScholarshipManager manager = new ScholarshipManager(150000.0, 3000.0);

            // Preload presentation demo dataset (20 realistic Filipino student applicants)
            SampleDataSeeder.seedDemoData(manager);

            // Launch Main UI Frame
            MainFrame frame = new MainFrame(manager);
            frame.setVisible(true);
        });
    }
}
