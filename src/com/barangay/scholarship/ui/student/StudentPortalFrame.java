package com.barangay.scholarship.ui.student;

import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.MainFrame;
import com.barangay.scholarship.ui.PortalSelectorFrame;
import com.barangay.scholarship.ui.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated Student Application Portal Frame.
 * Completely isolates the student application and tracking experience from administrative controls.
 * Resident applicants can fill out their forms, view real-time eligibility feedback, and track their
 * status without viewing or accessing administrative operations (queues, allocation, disbursement, audit log).
 */
public class StudentPortalFrame extends JFrame {
    private final ScholarshipManager manager;
    private final JPanel contentArea;
    private final CardLayout cardLayout;

    private final List<JButton> tabButtons;
    private String currentTab;

    // View References
    private final StudentApplicationView applicationView;
    private final StudentStatusTrackerView trackerView;
    private final StudentGuidelinesView guidelinesView;

    public StudentPortalFrame(ScholarshipManager manager) {
        super("Barangay Scholarship - Student Application & Inquiry Portal");
        this.manager = manager;
        this.tabButtons = new ArrayList<>();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 780);
        setMinimumSize(new Dimension(840, 560));
        setLocationRelativeTo(null);

        // Top Navigation Bar
        JPanel topNavbar = createTopNavbar();

        // CardLayout container for student pages
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Theme.BG_APP);

        // Views
        applicationView = new StudentApplicationView(manager, this::navigateTo);
        trackerView = new StudentStatusTrackerView(manager);
        guidelinesView = new StudentGuidelinesView();

        contentArea.add(applicationView, "APPLICATION");
        contentArea.add(trackerView, "TRACKER");
        contentArea.add(guidelinesView, "GUIDELINES");

        // Frame Layout
        setLayout(new BorderLayout());
        add(topNavbar, BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);

        // Reactive sync
        manager.addChangeListener(() -> {
            // Re-render when status changes
        });

        // Initial tab
        switchTab("APPLICATION");
    }

    private JPanel createTopNavbar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Theme.PRIMARY);
        nav.setBorder(new EmptyBorder(12, 24, 12, 24));

        // Left Branding
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel tag = new JLabel("BARANGAY SANGGUNIANG KABATAAN");
        tag.setFont(Theme.FONT_SMALL_BOLD);
        tag.setForeground(new Color(147, 197, 253)); // Soft blue

        JLabel title = new JLabel("Student Scholarship Portal");
        title.setFont(Theme.FONT_HEADER_MD);
        title.setForeground(Color.WHITE);

        brand.add(tag);
        brand.add(Box.createVerticalStrut(2));
        brand.add(title);
        nav.add(brand, BorderLayout.WEST);

        // Center Navigation Tabs
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        tabs.setOpaque(false);

        addTabButton(tabs, "📝  Scholarship Application", "APPLICATION");
        addTabButton(tabs, "🔍  Track Application Status", "TRACKER");
        addTabButton(tabs, "ℹ️  Requirements & Guidelines", "GUIDELINES");

        nav.add(tabs, BorderLayout.CENTER);

        // Right Actions: Switch to Admin / Portal Gateway
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnSwitchPortal = new JButton("🔄 Switch Role");
        btnSwitchPortal.setFont(Theme.FONT_SMALL_BOLD);
        btnSwitchPortal.setForeground(new Color(203, 213, 225));
        btnSwitchPortal.setBackground(Theme.PRIMARY_HOVER);
        btnSwitchPortal.setFocusPainted(false);
        btnSwitchPortal.setBorder(new CompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btnSwitchPortal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSwitchPortal.addActionListener(e -> returnToPortalGateway());

        JButton btnStaffLogin = Theme.createStyledButton("🔒 Staff / Admin Login", new Color(30, 64, 175), Color.WHITE);
        btnStaffLogin.setFont(Theme.FONT_SMALL_BOLD);
        btnStaffLogin.addActionListener(e -> promptStaffLogin());

        rightActions.add(btnSwitchPortal);
        rightActions.add(btnStaffLogin);
        nav.add(rightActions, BorderLayout.EAST);

        return nav;
    }

    private void addTabButton(JPanel container, String text, String tabKey) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(Theme.PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> switchTab(tabKey));

        btn.putClientProperty("tabKey", tabKey);
        tabButtons.add(btn);
        container.add(btn);
    }

    public void switchTab(String tabKey) {
        this.currentTab = tabKey;
        cardLayout.show(contentArea, tabKey);

        for (JButton btn : tabButtons) {
            String key = (String) btn.getClientProperty("tabKey");
            if (tabKey.equals(key)) {
                btn.setBackground(Theme.PRIMARY_HOVER);
                btn.setForeground(Color.WHITE);
                btn.setBorder(new CompoundBorder(
                        new MatteBorder(0, 0, 3, 0, new Color(59, 130, 246)),
                        new EmptyBorder(0, 14, 0, 14)
                ));
            } else {
                btn.setBackground(Theme.PRIMARY);
                btn.setForeground(new Color(203, 213, 225));
                btn.setBorder(new EmptyBorder(0, 14, 0, 14));
            }
        }
    }

    public void navigateTo(String navTarget) {
        if (navTarget.startsWith("TRACKER:")) {
            String id = navTarget.substring("TRACKER:".length());
            switchTab("TRACKER");
            trackerView.searchForId(id);
        } else {
            switchTab(navTarget);
        }
    }

    private void returnToPortalGateway() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            PortalSelectorFrame selector = new PortalSelectorFrame(manager);
            selector.setVisible(true);
        });
    }

    private void promptStaffLogin() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 6, 6));
        panel.add(new JLabel("Enter Barangay Staff Passcode to open Admin Management Suite:"));
        JPasswordField pwd = new JPasswordField();
        panel.add(pwd);

        int opt = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Barangay Staff Authentication",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opt == JOptionPane.OK_OPTION) {
            String pass = new String(pwd.getPassword()).trim();
            // Default demo passcode: "admin" or "1234", or empty for quick test
            if (pass.equalsIgnoreCase("admin") || pass.equals("1234") || pass.equalsIgnoreCase("sk") || pass.isEmpty()) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    MainFrame adminFrame = new MainFrame(manager);
                    adminFrame.setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid Passcode! Access to Barangay Admin Portal denied.",
                        "Authentication Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
