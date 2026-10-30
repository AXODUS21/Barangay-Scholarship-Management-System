package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.model.ActionRecord;
import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.ApplicationStatus;
import com.barangay.scholarship.model.Scholar;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.ui.components.ModernTable;
import com.barangay.scholarship.ui.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dashboard View providing executive metrics, budget tracker progress, quick actions,
 * and recent transaction logs.
 */
public class DashboardView extends JPanel {
    private final ScholarshipManager manager;
    private final Consumer<String> navigator;

    // Stat Cards
    private StatCard cardBudget;
    private StatCard cardScholars;
    private StatCard cardWaitlist;
    private StatCard cardScreening;

    // Budget Tracker UI
    private JProgressBar budgetBar;
    private JLabel lblBudgetPercent;
    private JLabel lblBudgetDetails;

    // Recent Activity Table
    private DefaultTableModel activityTableModel;
    private ModernTable activityTable;

    public DashboardView(ScholarshipManager manager, Consumer<String> navigator) {
        this.manager = manager;
        this.navigator = navigator;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleGrp = new JPanel();
        titleGrp.setOpaque(false);
        titleGrp.setLayout(new BoxLayout(titleGrp, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Barangay Scholarship Dashboard");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Live overview of educational aid funds, slot rankings, and applicant status");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);

        titleGrp.add(title);
        titleGrp.add(Box.createVerticalStrut(4));
        titleGrp.add(subtitle);
        headerPanel.add(titleGrp, BorderLayout.WEST);

        // Action Buttons on Header
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        quickActions.setOpaque(false);

        JButton btnSeed5 = Theme.createStyledButton("Preload 5 DSA Records", Theme.ACCENT, Color.WHITE);
        btnSeed5.setToolTipText("Preload exactly 5 benchmark students to simulate Queue, Stack, BST, and Sorts");
        btnSeed5.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this,
                    "Preload 5 student records for algorithm and data structure simulations?",
                    "Confirm Preload", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                com.barangay.scholarship.util.SampleDataSeeder.seedFiveDemoData(manager);
                JOptionPane.showMessageDialog(this,
                        "5 DSA Simulation records loaded!\n"
                        + "• 3 Approved Scholars in ScholarBST\n"
                        + "• 1 Waitlisted Applicant at FIFO Head (ScholarshipQueue)\n"
                        + "• 1 Verified Applicant in Screening Queue",
                        "Preload Complete", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            }
        });

        JButton btnNewApp = Theme.createStyledButton("+ New Applicant", Theme.PRIMARY, Color.WHITE);
        btnNewApp.addActionListener(e -> navigator.accept("REGISTRATION"));

        quickActions.add(btnSeed5);
        quickActions.add(btnNewApp);
        headerPanel.add(quickActions, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Content: Cards + Budget Progress + Recent Activity
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // 1. KPI Metric Cards
        cardsGrid = new JPanel(new GridLayout(1, 4, 14, 12));
        cardsGrid.setOpaque(false);

        cardBudget = new StatCard("Remaining Budget", "₱0.00", "Total: ₱0.00", Theme.ACCENT);
        cardScholars = new StatCard("Approved Scholars", "0", "0 Payouts Claimed", Theme.PRIMARY);
        cardWaitlist = new StatCard("Waitlist Queue", "0 Applicants", "FIFO Next in Line", Theme.WARNING);
        cardScreening = new StatCard("Screening Line", "0 Applicants", "Awaiting Verification", Theme.INFO);

        cardsGrid.add(cardBudget);
        cardsGrid.add(cardScholars);
        cardsGrid.add(cardWaitlist);
        cardsGrid.add(cardScreening);
        centerPanel.add(cardsGrid);
        centerPanel.add(Box.createVerticalStrut(14));

        // 2. Budget Tracker Progress Card
        JPanel budgetCard = Theme.createCardPanel();
        JPanel budgetHeader = new JPanel(new BorderLayout());
        budgetHeader.setOpaque(false);

        JLabel lblBudgetTitle = new JLabel("BARANGAY SK SCHOLARSHIP FUND UTILIZATION");
        lblBudgetTitle.setFont(Theme.FONT_SMALL_BOLD);
        lblBudgetTitle.setForeground(Theme.TEXT_MUTED);

        lblBudgetPercent = new JLabel("0% Disbursed");
        lblBudgetPercent.setFont(Theme.FONT_BODY_BOLD);
        lblBudgetPercent.setForeground(Theme.PRIMARY);

        budgetHeader.add(lblBudgetTitle, BorderLayout.WEST);
        budgetHeader.add(lblBudgetPercent, BorderLayout.EAST);

        budgetBar = new JProgressBar(0, 100);
        budgetBar.setValue(0);
        budgetBar.setForeground(Theme.ACCENT);
        budgetBar.setBackground(new Color(226, 232, 240));
        budgetBar.setBorderPainted(false);
        budgetBar.setPreferredSize(new Dimension(100, 12));

        lblBudgetDetails = new JLabel("Loading budget details...");
        lblBudgetDetails.setFont(Theme.FONT_SMALL);
        lblBudgetDetails.setForeground(Theme.TEXT_MUTED);

        JPanel budgetBody = new JPanel();
        budgetBody.setOpaque(false);
        budgetBody.setLayout(new BoxLayout(budgetBody, BoxLayout.Y_AXIS));
        budgetBody.add(budgetHeader);
        budgetBody.add(Box.createVerticalStrut(8));
        budgetBody.add(budgetBar);
        budgetBody.add(Box.createVerticalStrut(6));
        budgetBody.add(lblBudgetDetails);

        budgetCard.add(budgetBody, BorderLayout.CENTER);
        centerPanel.add(budgetCard);
        centerPanel.add(Box.createVerticalStrut(14));

        // 3. Lower Section: Recent Activity Stack Table Card (Full Width)
        JPanel activityCard = Theme.createCardPanel();
        activityCard.setPreferredSize(new Dimension(350, 260));
        JLabel actTitle = new JLabel("Audit Log / Recent Transactions (LIFO Stack)");
        actTitle.setFont(Theme.FONT_HEADER_SM);
        actTitle.setForeground(Theme.TEXT_TITLE);

        String[] cols = {"Time", "Action", "Target", "Status / Details"};
        activityTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        activityTable = new ModernTable(activityTableModel);
        JScrollPane actScroll = ModernTable.createScrollPane(activityTable);

        activityCard.add(actTitle, BorderLayout.NORTH);
        activityCard.add(actScroll, BorderLayout.CENTER);

        centerPanel.add(activityCard);

        add(centerPanel, BorderLayout.CENTER);

        // Responsive resize listener
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateResponsiveLayout();
            }
        });
    }

    private JPanel cardsGrid;

    private void updateResponsiveLayout() {
        int w = getWidth();
        if (w <= 0) return;

        if (w < 840) {
            if (cardsGrid != null && ((GridLayout) cardsGrid.getLayout()).getRows() != 2) {
                cardsGrid.setLayout(new GridLayout(2, 2, 12, 12));
                cardsGrid.revalidate();
            }
        } else {
            if (cardsGrid != null && ((GridLayout) cardsGrid.getLayout()).getRows() != 1) {
                cardsGrid.setLayout(new GridLayout(1, 4, 14, 0));
                cardsGrid.revalidate();
            }
        }
    }

    public void refreshData() {
        double total = manager.getTotalBudget();
        double disbursed = manager.getDisbursedBudget();
        double remaining = manager.getRemainingBudget();
        double pct = (total > 0) ? (disbursed / total * 100.0) : 0.0;

        cardBudget.setValue(String.format("₱%,.2f", remaining));
        cardBudget.setSubtitle(String.format("Total: ₱%,.2f | Disbursed: ₱%,.2f", total, disbursed));

        int approvedCount = manager.getCountByStatus(ApplicationStatus.APPROVED);
        int disbursedCount = manager.getCountByStatus(ApplicationStatus.DISBURSED);
        cardScholars.setValue(String.valueOf(approvedCount + disbursedCount));
        cardScholars.setSubtitle(String.format("%d Claimed | %d Unclaimed", disbursedCount, approvedCount));

        cardWaitlist.setValue(manager.getWaitlistQueue().size() + " Students");
        cardWaitlist.setSubtitle("Next in line when slots open");

        cardScreening.setValue(manager.getScreeningQueue().size() + " In Line");
        cardScreening.setSubtitle("First-Come, First-Served (FIFO)");

        budgetBar.setValue((int) pct);
        lblBudgetPercent.setText(String.format("%.1f%% Disbursed", pct));
        lblBudgetDetails.setText(String.format("Default Grant: ₱%,.2f | Available Slots by Budget: %d scholars",
                manager.getGrantPerScholar(), manager.getRemainingSlotsByBudget()));

        // Populate Activity Stack
        activityTableModel.setRowCount(0);
        List<ActionRecord> records = manager.getActionHistory().toList();
        int max = Math.min(10, records.size());
        for (int i = 0; i < max; i++) {
            ActionRecord r = records.get(i);
            activityTableModel.addRow(new Object[]{
                    r.getFormattedTime(),
                    r.getType().getLabel(),
                    r.getTargetName(),
                    r.getDescription()
            });
        }
    }
}
