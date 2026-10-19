package com.barangay.scholarship.ui;

import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.student.StudentPortalFrame;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern Portal Gateway & Role Selector window.
 * Serves as the landing hub of the system, cleanly separating the public
 * Student Application Portal from the protected Barangay Administration Suite.
 */
public class PortalSelectorFrame extends JFrame {
    private final ScholarshipManager manager;

    public PortalSelectorFrame(ScholarshipManager manager) {
        super("Barangay Scholarship & Educational Assistance - Portal Gateway");
        this.manager = manager;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(880, 580);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_APP);

        // Top Banner
        JPanel topBanner = new JPanel();
        topBanner.setBackground(Theme.PRIMARY);
        topBanner.setBorder(new EmptyBorder(24, 32, 24, 32));
        topBanner.setLayout(new BoxLayout(topBanner, BoxLayout.Y_AXIS));

        JLabel country = new JLabel("REPUBLIC OF THE PHILIPPINES • SANGGUNIANG KABATAAN");
        country.setFont(Theme.FONT_SMALL_BOLD);
        country.setForeground(new Color(147, 197, 253)); // Soft blue

        JLabel title = new JLabel("Barangay Scholarship Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Select your designated portal below to proceed with application or management.");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(new Color(203, 213, 225));

        topBanner.add(country);
        topBanner.add(Box.createVerticalStrut(4));
        topBanner.add(title);
        topBanner.add(Box.createVerticalStrut(4));
        topBanner.add(subtitle);

        root.add(topBanner, BorderLayout.NORTH);

        // Center: Two Role Selection Cards Side-by-Side
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 24, 0));
        centerGrid.setOpaque(false);
        centerGrid.setBorder(new EmptyBorder(32, 36, 32, 36));

        // LEFT CARD: Student Portal
        JPanel studentCard = createRoleCard(
                "🎓",
                "Student Applicant Portal",
                "For resident students submitting applications & tracking evaluation status",
                new String[]{
                        "✔  Online Scholarship Registration Form",
                        "✔  Instant Dual-Scale Eligibility Auto-Check",
                        "✔  Document Requirements Checklist",
                        "✔  Personal Application & Queue Tracker",
                        "🔒  Zero Administrative Controls Exposed"
                },
                "Enter Student Portal  ➔",
                Theme.ACCENT,
                e -> launchStudentPortal()
        );

        // RIGHT CARD: Barangay Staff / Admin Portal
        JPanel adminCard = createRoleCard(
                "🏛️",
                "Barangay Staff & Admin",
                "For SK officials, evaluators, and treasurers managing scholarship records",
                new String[]{
                        "📋  FIFO Document Screening Queue",
                        "⚖️  Merge & Quick Sort Slot Allocator",
                        "💰  Stipend Payout Desk & Payroll Export",
                        "🌳  Scholar Directory (Binary Search Tree)",
                        "⏪  1-Click Transaction Undo ActionStack"
                },
                "Access Staff Portal  ➔",
                Theme.PRIMARY,
                e -> launchAdminPortal()
        );

        centerGrid.add(studentCard);
        centerGrid.add(adminCard);

        root.add(centerGrid, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 36, 18, 36));

        JLabel info = new JLabel("Barangay Council & SK Educational Assistance Program • Java Swing SE");
        info.setFont(Theme.FONT_SMALL);
        info.setForeground(Theme.TEXT_MUTED);

        footer.add(info, BorderLayout.WEST);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createRoleCard(String emoji, String title, String subtitle,
                                  String[] highlights, String btnText, Color accentColor,
                                  java.awt.event.ActionListener onAction) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Icon Header
        JLabel iconLbl = new JLabel(emoji);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_HEADER_MD);
        titleLbl.setForeground(Theme.TEXT_TITLE);

        JLabel descLbl = new JLabel("<html>" + subtitle + "</html>");
        descLbl.setFont(Theme.FONT_SMALL);
        descLbl.setForeground(Theme.TEXT_MUTED);
        descLbl.setPreferredSize(new Dimension(300, 36));

        card.add(iconLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(descLbl);
        card.add(Box.createVerticalStrut(14));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        card.add(sep);
        card.add(Box.createVerticalStrut(14));

        // Feature Bullets
        for (String h : highlights) {
            JLabel bullet = new JLabel(h);
            bullet.setFont(Theme.FONT_BODY);
            bullet.setForeground(Theme.TEXT_BODY);
            card.add(bullet);
            card.add(Box.createVerticalStrut(6));
        }

        card.add(Box.createVerticalGlue());

        // Launch Button
        JButton btn = Theme.createStyledButton(btnText, accentColor, Color.WHITE);
        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setPreferredSize(new Dimension(300, 42));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.addActionListener(onAction);

        card.add(btn);
        return card;
    }

    private void launchStudentPortal() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            StudentPortalFrame studentFrame = new StudentPortalFrame(manager);
            studentFrame.setVisible(true);
        });
    }

    private void launchAdminPortal() {
        // Quick authentication modal
        JPanel panel = new JPanel(new GridLayout(2, 1, 6, 6));
        panel.add(new JLabel("Enter Barangay Staff Passcode to open Admin Management Suite:"));
        JPasswordField pwd = new JPasswordField();
        panel.add(pwd);

        int opt = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Barangay Staff Verification",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opt == JOptionPane.OK_OPTION) {
            String pass = new String(pwd.getPassword()).trim();
            // Default demo passcode: "admin", "1234", "sk", or empty for quick test
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
