package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.util.SampleDataSeeder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Settings & Tools View for budget customization, sample data seeding,
 * and project rubric compliance verification.
 */
public class SettingsView extends JPanel {
    private final ScholarshipManager manager;

    private JTextField txtTotalBudget;
    private JTextField txtGrantAmount;
    private JLabel lblCalculatedSlots;

    public SettingsView(ScholarshipManager manager) {
        this.manager = manager;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        refreshValues();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("System Settings & Rubric Verification");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Configure budget caps, reload presentation demo datasets, and inspect rubric alignment");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Column 1: Budget & Data Operations
        JPanel col1 = new JPanel();
        col1.setOpaque(false);
        col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS));
        col1.setMinimumSize(new Dimension(320, 320));

        // Budget Config Card
        JPanel budgetCard = Theme.createCardPanel();

        JPanel bHeader = new JPanel(new BorderLayout());
        bHeader.setOpaque(false);
        JLabel bTitle = new JLabel("BARANGAY SCHOLARSHIP BUDGET CONFIGURATION");
        bTitle.setFont(Theme.FONT_SMALL_BOLD);
        bTitle.setForeground(Theme.TEXT_MUTED);
        bHeader.add(bTitle, BorderLayout.WEST);

        JPanel bBody = new JPanel(new GridLayout(3, 2, 12, 12));
        bBody.setOpaque(false);
        bBody.setBorder(new EmptyBorder(12, 0, 12, 0));

        JLabel l1 = new JLabel("Total Barangay SK Budget (₱):");
        l1.setFont(Theme.FONT_BODY_BOLD);
        txtTotalBudget = new JTextField(String.valueOf(manager.getTotalBudget()));

        JLabel l2 = new JLabel("Grant Amount per Scholar (₱):");
        l2.setFont(Theme.FONT_BODY_BOLD);
        txtGrantAmount = new JTextField(String.valueOf(manager.getGrantPerScholar()));

        JLabel l3 = new JLabel("Calculated Funded Slots:");
        l3.setFont(Theme.FONT_BODY_BOLD);
        lblCalculatedSlots = new JLabel(manager.getMaxPossibleSlots() + " Scholars");
        lblCalculatedSlots.setFont(Theme.FONT_HEADER_SM);
        lblCalculatedSlots.setForeground(Theme.PRIMARY);

        bBody.add(l1); bBody.add(txtTotalBudget);
        bBody.add(l2); bBody.add(txtGrantAmount);
        bBody.add(l3); bBody.add(lblCalculatedSlots);

        JButton btnSaveBudget = Theme.createStyledButton("Save Budget Settings", Theme.PRIMARY, Color.WHITE);
        btnSaveBudget.addActionListener(e -> saveBudget());

        budgetCard.add(bHeader, BorderLayout.NORTH);
        budgetCard.add(bBody, BorderLayout.CENTER);
        budgetCard.add(btnSaveBudget, BorderLayout.SOUTH);

        col1.add(budgetCard);
        col1.add(Box.createVerticalStrut(14));

        // Data Reset & Seeder Card
        JPanel dataCard = Theme.createCardPanel();

        JLabel dTitle = new JLabel("PRESENTATION & DEMO CONTROLS");
        dTitle.setFont(Theme.FONT_SMALL_BOLD);
        dTitle.setForeground(Theme.TEXT_MUTED);

        JPanel dBody = new JPanel(new GridLayout(4, 1, 8, 8));
        dBody.setOpaque(false);
        dBody.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnSeed5 = Theme.createStyledButton("Preload 5 Students (DSA Simulation Preset)", Theme.PRIMARY, Color.WHITE);
        btnSeed5.setToolTipText("Loads 5 students: 3 Approved (BST), 1 Waitlisted (FIFO Head), 1 Verified (Screening)");
        btnSeed5.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Preload 5 students configured for DSA simulations (Queue, Stack, BST, Sorts)?", "Confirm Preload", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                SampleDataSeeder.seedFiveDemoData(manager);
                JOptionPane.showMessageDialog(this, "5 DSA Simulation records preloaded successfully!\n• 3 Approved Scholars in ScholarBST\n• 1 Waitlisted Applicant at FIFO Head\n• 1 Verified Applicant in Screening Queue", "5 Records Loaded", JOptionPane.INFORMATION_MESSAGE);
                refreshValues();
            }
        });

        JButton btnSeed5Candidates = Theme.createStyledButton("Preload 5 Verified Candidates (Ranking Lab)", Theme.ACCENT, Color.WHITE);
        btnSeed5Candidates.setToolTipText("Loads 5 verified applicants ready for Merge Sort and Quick Sort live ranking");
        btnSeed5Candidates.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Preload 5 verified applicants ready for live ranking in Slot Allocation?", "Confirm Preload", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                SampleDataSeeder.seedFiveVerifiedCandidates(manager);
                JOptionPane.showMessageDialog(this, "5 Verified Candidates preloaded!\nOpen 'Slot Allocation & Ranking Engine' to test Merge Sort (Merit) and Quick Sort (Financial Need).", "Ranking Lab Loaded", JOptionPane.INFORMATION_MESSAGE);
                refreshValues();
            }
        });

        JButton btnSeed = new JButton("Reset & Preload Full Demo (20 Students)");
        btnSeed.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Preload 20 realistic student records and reset queues?", "Confirm Preload", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                SampleDataSeeder.seedDemoData(manager);
                JOptionPane.showMessageDialog(this, "20 Demo student records preloaded successfully!", "Demo Loaded", JOptionPane.INFORMATION_MESSAGE);
                refreshValues();
            }
        });

        JButton btnClear = new JButton("Clear All System Records");
        btnClear.setForeground(Theme.DANGER);
        btnClear.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all applicants, queues, and records?", "Confirm Clear", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                manager.clearAll();
                JOptionPane.showMessageDialog(this, "All records cleared.", "Cleared", JOptionPane.INFORMATION_MESSAGE);
                refreshValues();
            }
        });

        dBody.add(btnSeed5);
        dBody.add(btnSeed5Candidates);
        dBody.add(btnSeed);
        dBody.add(btnClear);

        dataCard.add(dTitle, BorderLayout.NORTH);
        dataCard.add(dBody, BorderLayout.CENTER);
        col1.add(dataCard);

        // Column 2: Academic Rubric Alignment Card
        JPanel col2 = Theme.createCardPanel();
        col2.setMinimumSize(new Dimension(340, 320));

        JLabel rTitle = new JLabel("Course Project Rubric Alignment");
        rTitle.setFont(Theme.FONT_HEADER_SM);
        rTitle.setForeground(Theme.TEXT_TITLE);

        JTextArea txtRubric = new JTextArea();
        txtRubric.setEditable(false);
        txtRubric.setFont(Theme.FONT_BODY);
        txtRubric.setForeground(Theme.TEXT_BODY);
        txtRubric.setBackground(Theme.CARD_BG);
        txtRubric.setLineWrap(true);
        txtRubric.setWrapStyleWord(true);
        txtRubric.setText(
                "RUBRIC IMPLEMENTATION MAPPING:\n\n" +
                "1. Core Transactions (Section 2 Requirement):\n" +
                "  • Stage 1: Registration & Profiling (Dual-scale GWA, Income, Purok)\n" +
                "  • Stage 2: Document Verification & Screening (Indigency, TOR, Enrollment)\n" +
                "  • Stage 3: Slot Allocation & Ranking (Merit vs Need ranking)\n" +
                "  • Stage 4: Payout & Allowance Disbursement (Budget tracking & Payroll sheet)\n\n" +
                "2. Custom Data Structures (Section 3 Requirement):\n" +
                "  • Queue (FIFO): ScholarshipQueue<T> for Screening & Waitlist\n" +
                "  • Stack (LIFO): ActionStack<T> for 1-Click Undo Operations\n" +
                "  • Binary Search Tree: ScholarBST for Fast Scholar Lookups & Traversals\n\n" +
                "3. Custom Algorithms:\n" +
                "  • Merge Sort: Academic Merit ranking by GWA (O(n log n))\n" +
                "  • Quick Sort: Financial Need ranking by Family Income (O(n log n))\n" +
                "  • Binary Search: O(log n) lookup by ID and Name\n" +
                "  • Linear Search: Filter by Purok, School, or Status\n\n" +
                "4. Object-Oriented Design (Section 4):\n" +
                "  • Inheritance: Scholar extends Applicant\n" +
                "  • Encapsulation: Strict private fields, validated mutators\n" +
                "  • Separation of Concerns: Model-View-Controller architecture\n\n" +
                "5. Bonus Features:\n" +
                "  • Live Eligibility Auto-Checker (Collegiate 1.0-5.0 vs Percentage 75-100)\n" +
                "  • Real-time Budget Tracker (₱150,000 auto-decrement on payout)\n" +
                "  • Automated Waitlist Promotion upon dropping a scholar\n" +
                "  • Printable Official Barangay Payout Attendance Sheet Generator"
        );

        JScrollPane scrollRubric = new JScrollPane(txtRubric);
        scrollRubric.setBorder(null);

        col2.add(rTitle, BorderLayout.NORTH);
        col2.add(scrollRubric, BorderLayout.CENTER);

        JSplitPane split = Theme.createSplitPane(JSplitPane.HORIZONTAL_SPLIT, col1, col2, 0.48);
        add(split, BorderLayout.CENTER);
    }

    private void saveBudget() {
        try {
            double total = Double.parseDouble(txtTotalBudget.getText().trim());
            double grant = Double.parseDouble(txtGrantAmount.getText().trim());

            if (total <= 0 || grant <= 0) {
                JOptionPane.showMessageDialog(this, "Amounts must be greater than zero.", "Invalid Values", JOptionPane.ERROR_MESSAGE);
                return;
            }

            manager.setTotalBudget(total);
            manager.setGrantPerScholar(grant);
            refreshValues();
            JOptionPane.showMessageDialog(this, "Budget settings saved successfully!", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numerical currency amounts.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshValues() {
        txtTotalBudget.setText(String.format("%.2f", manager.getTotalBudget()));
        txtGrantAmount.setText(String.format("%.2f", manager.getGrantPerScholar()));
        lblCalculatedSlots.setText(manager.getMaxPossibleSlots() + " Scholars");
    }
}
