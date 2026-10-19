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

        JButton btnNewApp = Theme.createStyledButton("+ New Applicant", Theme.PRIMARY, Color.WHITE);
        btnNewApp.addActionListener(e -> navigator.accept("REGISTRATION"));

        JButton btnUndo = Theme.createStyledButton("⏪ Undo Last Action", Theme.CARD_BG, Theme.TEXT_TITLE);
        btnUndo.addActionListener(e -> {
            boolean undone = manager.undoLastAction();
            if (undone) {
                JOptionPane.showMessageDialog(this, "Last action successfully undone!", "Undo Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No previous actions available to undo.", "Undo Stack Empty", JOptionPane.WARNING_MESSAGE);
            }
        });

        quickActions.add(btnUndo);
        quickActions.add(btnNewApp);
        headerPanel.add(quickActions, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Content: Cards + Budget Progress + Recent Activity
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // 1. KPI Metric Cards
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsGrid.setOpaque(false);
        cardsGrid.setMaximumSize(new Dimension(2000, 100));

        cardBudget = new StatCard("Remaining Budget", "₱0.00", "Total: ₱0.00", Theme.ACCENT);
        cardScholars = new StatCard("Approved Scholars", "0", "0 Payouts Claimed", Theme.PRIMARY);
        cardWaitlist = new StatCard("Waitlist Queue", "0 Applicants", "FIFO Next in Line", Theme.WARNING);
        cardScreening = new StatCard("Screening Line", "0 Applicants", "Awaiting Verification", Theme.INFO);

        cardsGrid.add(cardBudget);
        cardsGrid.add(cardScholars);
        cardsGrid.add(cardWaitlist);
        cardsGrid.add(cardScreening);
        centerPanel.add(cardsGrid);
        centerPanel.add(Box.createVerticalStrut(16));

        // 2. Budget Tracker Progress Card
        JPanel budgetCard = Theme.createCardPanel();
        budgetCard.setMaximumSize(new Dimension(2000, 100));
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
        centerPanel.add(Box.createVerticalStrut(16));

        // 3. Lower Section: Quick Workflows + Recent Activity Stack
        JPanel lowerPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        lowerPanel.setOpaque(false);

        // Left: Workflow Launcher Card
        JPanel workflowCard = Theme.createCardPanel();
        JLabel wfTitle = new JLabel("Scholarship Transaction Workflows");
        wfTitle.setFont(Theme.FONT_HEADER_SM);
        wfTitle.setForeground(Theme.TEXT_TITLE);

        JPanel btnGrid = new JPanel(new GridLayout(4, 1, 8, 8));
        btnGrid.setOpaque(false);
        btnGrid.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnNavScreening = new JButton("1. Document Screening Queue (Verify Folders)");
        btnNavScreening.setFont(Theme.FONT_BODY);
        btnNavScreening.addActionListener(e -> navigator.accept("SCREENING"));

        JButton btnNavRanking = new JButton("2. Merit & Need Ranking (Merge / Quick Sort)");
        btnNavRanking.setFont(Theme.FONT_BODY);
        btnNavRanking.addActionListener(e -> navigator.accept("RANKING"));

        JButton btnNavDisbursement = new JButton("3. Payout Disbursement & Attendance Payroll");
        btnNavDisbursement.setFont(Theme.FONT_BODY);
        btnNavDisbursement.addActionListener(e -> navigator.accept("DISBURSEMENT"));

        JButton btnNavBST = new JButton("4. Scholar Directory & Binary Search Tree (BST)");
        btnNavBST.setFont(Theme.FONT_BODY);
        btnNavBST.addActionListener(e -> navigator.accept("DIRECTORY"));

        btnGrid.add(btnNavScreening);
        btnGrid.add(btnNavRanking);
        btnGrid.add(btnNavDisbursement);
        btnGrid.add(btnNavBST);

        workflowCard.add(wfTitle, BorderLayout.NORTH);
        workflowCard.add(btnGrid, BorderLayout.CENTER);

        // Right: Recent Activity Log (ActionStack snapshot)
        JPanel activityCard = Theme.createCardPanel();
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

        lowerPanel.add(workflowCard);
        lowerPanel.add(activityCard);
        centerPanel.add(lowerPanel);

        add(centerPanel, BorderLayout.CENTER);
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
        lblBudgetDetails.setText(String.format("Allocated Grant per Scholar: ₱%,.2f | Available Slots by Budget: %d scholars",
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
