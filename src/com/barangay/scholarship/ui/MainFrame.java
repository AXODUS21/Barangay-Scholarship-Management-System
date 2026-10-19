package com.barangay.scholarship.ui;

import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.components.SidebarPanel;
import com.barangay.scholarship.ui.views.*;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame integrating modern sidebar navigation,
 * CardLayout view switcher, and reactive data synchronization.
 */
public class MainFrame extends JFrame {
    private final ScholarshipManager manager;
    private final SidebarPanel sidebar;
    private final JPanel contentArea;
    private final CardLayout cardLayout;

    // View references
    private final DashboardView dashboardView;
    private final RegistrationView registrationView;
    private final ScreeningQueueView screeningQueueView;
    private final RankingAllocationView rankingAllocationView;
    private final DisbursementView disbursementView;
    private final ScholarDirectoryView scholarDirectoryView;
    private final AuditLogView auditLogView;
    private final SettingsView settingsView;

    public MainFrame(ScholarshipManager manager) {
        super("Barangay Scholarship & Educational Assistance Management System");
        this.manager = manager;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);

        // CardLayout container for views
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Theme.BG_APP);

        // Sidebar
        sidebar = new SidebarPanel(this::navigateTo);

        // Instantiate Views
        dashboardView = new DashboardView(manager, this::navigateTo);
        registrationView = new RegistrationView(manager);
        screeningQueueView = new ScreeningQueueView(manager);
        rankingAllocationView = new RankingAllocationView(manager);
        disbursementView = new DisbursementView(manager);
        scholarDirectoryView = new ScholarDirectoryView(manager);
        auditLogView = new AuditLogView(manager);
        settingsView = new SettingsView(manager);

        // Add to CardLayout
        contentArea.add(dashboardView, "DASHBOARD");
        contentArea.add(registrationView, "REGISTRATION");
        contentArea.add(screeningQueueView, "SCREENING");
        contentArea.add(rankingAllocationView, "RANKING");
        contentArea.add(disbursementView, "DISBURSEMENT");
        contentArea.add(scholarDirectoryView, "DIRECTORY");
        contentArea.add(auditLogView, "AUDIT");
        contentArea.add(settingsView, "SETTINGS");

        // Layout Assembly
        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);

        // Register Global Manager Change Listener
        manager.addChangeListener(this::refreshAllViews);

        // Show default view
        navigateTo("DASHBOARD");
    }

    public void navigateTo(String viewKey) {
        sidebar.setActiveView(viewKey);
        cardLayout.show(contentArea, viewKey);

        // Refresh specific view upon entering
        switch (viewKey) {
            case "DASHBOARD":
                dashboardView.refreshData();
                break;
            case "REGISTRATION":
                registrationView.refreshTable();
                break;
            case "SCREENING":
                screeningQueueView.refreshData();
                break;
            case "RANKING":
                rankingAllocationView.previewRanking();
                break;
            case "DISBURSEMENT":
                disbursementView.refreshData();
                break;
            case "DIRECTORY":
                scholarDirectoryView.refreshDirectory();
                break;
            case "AUDIT":
                auditLogView.refreshLog();
                break;
            case "SETTINGS":
                settingsView.refreshValues();
                break;
        }
    }

    private void refreshAllViews() {
        SwingUtilities.invokeLater(() -> {
            dashboardView.refreshData();
            registrationView.refreshTable();
            screeningQueueView.refreshData();
            rankingAllocationView.previewRanking();
            disbursementView.refreshData();
            scholarDirectoryView.refreshDirectory();
            auditLogView.refreshLog();
            settingsView.refreshValues();
        });
    }
}
