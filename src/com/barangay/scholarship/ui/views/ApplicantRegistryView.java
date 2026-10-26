package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.ui.components.ModernTable;
import com.barangay.scholarship.ui.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dedicated Applicant Registry View displaying the master database of registered applicants.
 * Full width, stretched table with search, counts, and reactive synchronization.
 */
public class ApplicantRegistryView extends JPanel {
    private final ScholarshipManager manager;

    // Stat Cards
    private StatCard cardTotalApplicants;
    private StatCard cardVerifiedApplicants;

    // Table
    private DefaultTableModel regTableModel;
    private ModernTable regTable;
    private JTextField txtSearch;

    public ApplicantRegistryView(ScholarshipManager manager) {
        this.manager = manager;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        refreshTable();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Master Applicant Registry");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Master record repository of all applicants submitted across the Student Portal and Staff Intake");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content Container
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Stats Row
        JPanel statsGrid = new JPanel(new GridLayout(1, 2, 16, 12));
        statsGrid.setOpaque(false);

        cardTotalApplicants = new StatCard("Total Registered Applicants", "0", "All Recorded Intakes", Theme.PRIMARY);
        cardVerifiedApplicants = new StatCard("Screened / Verified", "0", "Eligible or Advanced in Pipeline", Theme.ACCENT);

        statsGrid.add(cardTotalApplicants);
        statsGrid.add(cardVerifiedApplicants);
        centerPanel.add(statsGrid);
        centerPanel.add(Box.createVerticalStrut(14));

        // Toolbar Card: Search + Filter
        JPanel toolbarCard = Theme.createCardPanel();
        JPanel toolRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        toolRow.setOpaque(false);

        JLabel lblSearch = new JLabel("Search Registry:");
        lblSearch.setFont(Theme.FONT_BODY_BOLD);
        txtSearch = new JTextField(16);
        txtSearch.setPreferredSize(new Dimension(180, 32));

        JButton btnFilter = new JButton("Filter");
        btnFilter.addActionListener(e -> applyFilter());

        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            refreshTable();
        });

        toolRow.add(lblSearch);
        toolRow.add(txtSearch);
        toolRow.add(btnFilter);
        toolRow.add(btnReset);

        toolbarCard.add(toolRow, BorderLayout.CENTER);
        centerPanel.add(toolbarCard);
        centerPanel.add(Box.createVerticalStrut(14));

        // Table Card: Full Width
        JPanel tableCard = Theme.createCardPanel();
        JLabel tableTitle = new JLabel("All Applicant Intake Records");
        tableTitle.setFont(Theme.FONT_HEADER_SM);
        tableTitle.setForeground(Theme.TEXT_TITLE);

        String[] cols = {"Applicant ID", "Full Name", "Purok / Zone", "GWA", "Monthly Income", "School / University", "Year Level", "Status"};
        regTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        regTable = new ModernTable(regTableModel);
        JScrollPane tblScroll = ModernTable.createScrollPane(regTable);

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(tblScroll, BorderLayout.CENTER);
        centerPanel.add(tableCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshTable() {
        populateTable(manager.getMasterList());
        updateStats();
    }

    private void applyFilter() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }

        List<Applicant> filtered = new java.util.ArrayList<>();
        for (Applicant a : manager.getMasterList()) {
            if (a.getApplicantId().toLowerCase().contains(query)
                    || a.getFullName().toLowerCase().contains(query)
                    || a.getPurok().toLowerCase().contains(query)
                    || a.getSchool().toLowerCase().contains(query)
                    || a.getStatus().getDisplayName().toLowerCase().contains(query)) {
                filtered.add(a);
            }
        }
        populateTable(filtered);
    }

    private void populateTable(List<Applicant> list) {
        regTableModel.setRowCount(0);
        for (Applicant a : list) {
            regTableModel.addRow(new Object[]{
                    a.getApplicantId(),
                    a.getFullName(),
                    a.getPurok(),
                    a.getFormattedGwa(),
                    a.getFormattedIncome(),
                    a.getSchool(),
                    a.getYearLevel(),
                    a.getStatus()
            });
        }
    }

    private void updateStats() {
        List<Applicant> list = manager.getMasterList();
        int total = list.size();
        int verified = 0;
        for (Applicant a : list) {
            if (a.getStatus() != com.barangay.scholarship.model.ApplicationStatus.PENDING_VERIFICATION
                    && a.getStatus() != com.barangay.scholarship.model.ApplicationStatus.REJECTED) {
                verified++;
            }
        }
        cardTotalApplicants.setValue(String.valueOf(total));
        cardVerifiedApplicants.setValue(String.valueOf(verified));
    }
}
