package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.model.Scholar;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.ui.components.ModernTable;
import com.barangay.scholarship.ui.components.StatCard;
import com.barangay.scholarship.util.DataPersistence;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Transaction Stage 4: Payout & Allowance Disbursement.
 * Manages stipend release, budget tracking deductions, and official attendance sheet exports.
 */
public class DisbursementView extends JPanel {
    private final ScholarshipManager manager;

    // Stat Cards
    private StatCard cardBudgetRemaining;
    private StatCard cardDisbursed;
    private StatCard cardClaimedRatio;

    // Table
    private DefaultTableModel payoutTableModel;
    private ModernTable payoutTable;
    private JTextField txtSearch;

    public DisbursementView(ScholarshipManager manager) {
        this.manager = manager;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Payout & Allowance Disbursement Desk");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Log stipend claims, deduct barangay scholarship budget, and generate payroll attendance sheets");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Metric Cards
        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(2000, 90));

        cardBudgetRemaining = new StatCard("Remaining SK Funds", "₱0.00", "Available for release", Theme.ACCENT);
        cardDisbursed = new StatCard("Total Disbursed", "₱0.00", "Successfully claimed by scholars", Theme.PRIMARY);
        cardClaimedRatio = new StatCard("Disbursement Progress", "0 / 0 Scholars", "0% completed", Theme.INFO);

        statsGrid.add(cardBudgetRemaining);
        statsGrid.add(cardDisbursed);
        statsGrid.add(cardClaimedRatio);
        centerPanel.add(statsGrid);
        centerPanel.add(Box.createVerticalStrut(16));

        // Action Toolbar Card
        JPanel toolbarCard = Theme.createCardPanel();
        toolbarCard.setMaximumSize(new Dimension(2000, 70));
        JPanel toolRow = new JPanel(new BorderLayout(12, 0));
        toolRow.setOpaque(false);

        // Search Bar on Left
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setOpaque(false);
        JLabel lblSearch = new JLabel("Search Scholar:");
        lblSearch.setFont(Theme.FONT_BODY_BOLD);
        txtSearch = new JTextField(18);
        txtSearch.setPreferredSize(new Dimension(200, 32));
        JButton btnSearch = new JButton("Filter");
        btnSearch.addActionListener(e -> applyFilter());
        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            refreshData();
        });

        searchBox.add(lblSearch);
        searchBox.add(txtSearch);
        searchBox.add(btnSearch);
        searchBox.add(btnReset);
        toolRow.add(searchBox, BorderLayout.WEST);

        // Disbursement & Export Buttons on Right
        JPanel btnGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGrp.setOpaque(false);

        JButton btnClaim = Theme.createStyledButton("💰 Disburse Selected Stipend", Theme.ACCENT, Color.WHITE);
        btnClaim.addActionListener(e -> disburseSelected());

        JButton btnExport = Theme.createStyledButton("📄 Export Payout Attendance Sheet", Theme.PRIMARY, Color.WHITE);
        btnExport.addActionListener(e -> exportAttendanceSheet());

        btnGrp.add(btnClaim);
        btnGrp.add(btnExport);
        toolRow.add(btnGrp, BorderLayout.EAST);

        toolbarCard.add(toolRow, BorderLayout.CENTER);
        centerPanel.add(toolbarCard);
        centerPanel.add(Box.createVerticalStrut(16));

        // Table Card
        JPanel tableCard = Theme.createCardPanel();
        JLabel tblTitle = new JLabel("Approved Scholars Payout Masterlist");
        tblTitle.setFont(Theme.FONT_HEADER_SM);
        tblTitle.setForeground(Theme.TEXT_TITLE);

        String[] cols = {"Scholar ID", "Applicant ID", "Full Name", "Purok/Zone", "School", "Grant Amount", "Disbursement Status", "Claimed Date"};
        payoutTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        payoutTable = new ModernTable(payoutTableModel);
        JScrollPane tblScroll = ModernTable.createScrollPane(payoutTable);

        tableCard.add(tblTitle, BorderLayout.NORTH);
        tableCard.add(tblScroll, BorderLayout.CENTER);
        centerPanel.add(tableCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void applyFilter() {
        String q = txtSearch.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            refreshData();
            return;
        }

        payoutTableModel.setRowCount(0);
        List<Scholar> list = manager.getAllScholarsFromBST();
        for (Scholar s : list) {
            if (s.getScholarId().toLowerCase().contains(q)
                    || s.getFullName().toLowerCase().contains(q)
                    || s.getPurok().toLowerCase().contains(q)) {
                addScholarRow(s);
            }
        }
    }

    private void addScholarRow(Scholar s) {
        String statusText = s.isStipendDisbursed() ? "CLAIMED" : "UNCLAIMED";
        payoutTableModel.addRow(new Object[]{
                s.getScholarId(),
                s.getApplicantId(),
                s.getFullName(),
                s.getPurok(),
                s.getSchool(),
                s.getFormattedGrant(),
                statusText,
                s.getFormattedDisbursedDate()
        });
    }

    private void disburseSelected() {
        int row = payoutTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an approved scholar from the table to disburse stipend.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String schId = (String) payoutTableModel.getValueAt(row, 0);
        Scholar scholar = manager.getScholarDirectory().searchById(schId);
        if (scholar == null) return;

        if (scholar.isStipendDisbursed()) {
            JOptionPane.showMessageDialog(this, "Stipend for " + scholar.getFullName() + " has already been claimed on " + scholar.getFormattedDisbursedDate(), "Already Disbursed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Confirm stipend payout release:\n\nScholar: %s (%s)\nPurok: %s\nGrant Amount: %s\n\nRemaining SK Budget will be deducted by %s.",
                        scholar.getFullName(), scholar.getScholarId(), scholar.getPurok(), scholar.getFormattedGrant(), scholar.getFormattedGrant()),
                "Confirm Payout Release", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = manager.releaseStipend(schId);
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            String.format("Stipend of %s successfully disbursed to %s!\nUpdated Remaining SK Budget: ₱%,.2f",
                                    scholar.getFormattedGrant(), scholar.getFullName(), manager.getRemainingBudget()),
                            "Payout Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Payout Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportAttendanceSheet() {
        List<Scholar> list = manager.getAllScholarsFromBST();
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No scholars available to export.", "Empty Roster", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Barangay_Scholarship_Payout_Attendance_Sheet.txt"));
        int res = chooser.showSaveDialog(this);

        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                DataPersistence.exportAttendanceSheet(
                        file,
                        list,
                        "1st Semester AY 2026-2027",
                        manager.getTotalBudget(),
                        manager.getDisbursedBudget()
                );
                JOptionPane.showMessageDialog(this,
                        "Official Payout Attendance Sheet successfully generated!\nSaved to: " + file.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to export file: " + e.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshData() {
        cardBudgetRemaining.setValue(String.format("₱%,.2f", manager.getRemainingBudget()));
        cardDisbursed.setValue(String.format("₱%,.2f", manager.getDisbursedBudget()));

        List<Scholar> list = manager.getAllScholarsFromBST();
        int claimed = 0;
        for (Scholar s : list) {
            if (s.isStipendDisbursed()) claimed++;
        }
        int total = list.size();
        double pct = total > 0 ? (claimed * 100.0 / total) : 0.0;
        cardClaimedRatio.setValue(String.format("%d / %d Claimed", claimed, total));
        cardClaimedRatio.setSubtitle(String.format("%.1f%% payout completion", pct));

        payoutTableModel.setRowCount(0);
        for (Scholar s : list) {
            addScholarRow(s);
        }
    }
}
