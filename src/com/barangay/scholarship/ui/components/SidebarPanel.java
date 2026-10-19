package com.barangay.scholarship.ui.components;

import com.barangay.scholarship.ui.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern navigation sidebar panel with clean active indicators and badge counts.
 */
public class SidebarPanel extends JPanel {
    private final Consumer<String> onNavigate;
    private final List<JButton> navButtons;
    private String currentView;

    public SidebarPanel(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        this.navButtons = new ArrayList<>();
        this.currentView = "DASHBOARD";

        setPreferredSize(new Dimension(230, 650));
        setMinimumSize(new Dimension(200, 350));
        setBackground(Theme.PRIMARY);
        setLayout(new BorderLayout());

        // Header / Logo
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(18, 16, 16, 16));

        JLabel titleBadge = new JLabel("BARANGAY SK");
        titleBadge.setFont(Theme.FONT_SMALL_BOLD);
        titleBadge.setForeground(new Color(147, 197, 253)); // Soft blue

        JLabel titleMain = new JLabel("Scholarship Hub");
        titleMain.setFont(Theme.FONT_HEADER_MD);
        titleMain.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Educational Assistance");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(new Color(203, 213, 225));

        headerPanel.add(titleBadge);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(titleMain);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(subtitle);

        add(headerPanel, BorderLayout.NORTH);

        // Navigation Items
        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(new EmptyBorder(6, 0, 6, 0));

        addNavItem(menuPanel, "Dashboard", "DASHBOARD");
        addNavItem(menuPanel, "Registration", "REGISTRATION");
        addNavItem(menuPanel, "Screening Queue", "SCREENING");
        addNavItem(menuPanel, "Slot Allocation", "RANKING");
        addNavItem(menuPanel, "Disbursement", "DISBURSEMENT");
        addNavItem(menuPanel, "Scholar Directory", "DIRECTORY");
        addNavItem(menuPanel, "Audit & Undo Log", "AUDIT");
        addNavItem(menuPanel, "Settings & Rubric", "SETTINGS");

        menuPanel.add(Box.createVerticalGlue());

        JScrollPane menuScroll = new JScrollPane(menuPanel);
        menuScroll.setBorder(null);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(menuScroll, BorderLayout.CENTER);

        // Footer version info and portal quick switcher
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 14, 12, 14));
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));

        JButton btnStudentPortal = Theme.createStyledButton("🎓  Student Portal", Theme.ACCENT, Color.WHITE);
        btnStudentPortal.setFont(Theme.FONT_SMALL_BOLD);
        btnStudentPortal.setMaximumSize(new Dimension(200, 34));
        btnStudentPortal.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnStudentPortal.addActionListener(e -> {
            if (onNavigate != null) {
                onNavigate.accept("STUDENT_PORTAL");
            }
        });

        JButton btnSwitchRole = new JButton("🔄  Switch Role");
        btnSwitchRole.setFont(Theme.FONT_SMALL);
        btnSwitchRole.setForeground(new Color(203, 213, 225));
        btnSwitchRole.setBackground(Theme.PRIMARY_HOVER);
        btnSwitchRole.setFocusPainted(false);
        btnSwitchRole.setBorderPainted(false);
        btnSwitchRole.setMaximumSize(new Dimension(200, 28));
        btnSwitchRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSwitchRole.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSwitchRole.addActionListener(e -> {
            if (onNavigate != null) {
                onNavigate.accept("SWITCH_ROLE");
            }
        });

        JLabel verLbl = new JLabel("Barangay Edition v2.0");
        verLbl.setFont(Theme.FONT_SMALL);
        verLbl.setForeground(new Color(148, 163, 184));

        footerPanel.add(btnStudentPortal);
        footerPanel.add(Box.createVerticalStrut(6));
        footerPanel.add(btnSwitchRole);
        footerPanel.add(Box.createVerticalStrut(8));
        footerPanel.add(verLbl);

        add(footerPanel, BorderLayout.SOUTH);

        // Select initial
        setActiveView("DASHBOARD");
    }

    private void addNavItem(JPanel container, String text, String viewKey) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setForeground(new Color(226, 232, 240));
        btn.setBackground(Theme.PRIMARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(230, 38));
        btn.setPreferredSize(new Dimension(230, 38));
        btn.setBorder(new EmptyBorder(0, 16, 0, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            setActiveView(viewKey);
            if (onNavigate != null) {
                onNavigate.accept(viewKey);
            }
        });

        navButtons.add(btn);
        btn.putClientProperty("viewKey", viewKey);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    public void setActiveView(String viewKey) {
        this.currentView = viewKey;
        for (JButton btn : navButtons) {
            String key = (String) btn.getClientProperty("viewKey");
            if (viewKey.equals(key)) {
                btn.setBackground(Theme.PRIMARY_HOVER);
                btn.setForeground(Color.WHITE);
                btn.setBorder(new CompoundBorder(
                        new MatteBorder(0, 4, 0, 0, new Color(59, 130, 246)),
                        new EmptyBorder(0, 16, 0, 12)
                ));
            } else {
                btn.setBackground(Theme.PRIMARY);
                btn.setForeground(new Color(203, 213, 225));
                btn.setBorder(new EmptyBorder(0, 20, 0, 12));
            }
        }
    }
}
