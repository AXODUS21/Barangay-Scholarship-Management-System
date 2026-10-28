package com.barangay.scholarship;

import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.MainFrame;
import com.barangay.scholarship.ui.PortalSelectorFrame;
import com.barangay.scholarship.ui.student.StudentPortalFrame;
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
            // Load the benchmark dataset before opening any portal.
            ScholarshipManager manager = new ScholarshipManager(150000.0, 3000.0);
            SampleDataSeeder.seedDemoData(manager);

            // Launch designated portal or default landing gateway
            if (args.length > 0 && "student".equalsIgnoreCase(args[0])) {
                StudentPortalFrame studentFrame = new StudentPortalFrame(manager);
                studentFrame.setVisible(true);
            } else if (args.length > 0 && "admin".equalsIgnoreCase(args[0])) {
                MainFrame adminFrame = new MainFrame(manager);
                adminFrame.setVisible(true);
            } else {
                PortalSelectorFrame portalSelector = new PortalSelectorFrame(manager);
                portalSelector.setVisible(true);
            }
        });
    }
}
