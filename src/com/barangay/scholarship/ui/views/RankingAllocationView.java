package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.algorithms.AlgorithmUtils;
import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.ApplicationStatus;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.ui.components.ModernTable;
import com.barangay.scholarship.ui.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction Stage 3: Slot Allocation & Ranking Engine.
 * Features Merge Sort (Merit) and Quick Sort (Financial Need).
 */
public class RankingAllocationView extends JPanel {
    private final ScholarshipManager manager;

    // Control UI
    private JComboBox<ScholarshipManager.RankingCriteria> cmbCriteria;
    private JSpinner spnSlots;
    private StatCard cardBudgetSlots;
    private StatCard cardEligibleCandidates;

    // Ranking Table
    private DefaultTableModel rankTableModel;
    private ModernTable rankTable;

    public RankingAllocationView(ScholarshipManager manager) {
        this.manager = manager;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        previewRanking();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Slot Allocation & Applicant Ranking Engine");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Automatically rank qualified candidates using Merge Sort (Merit) or Quick Sort (Need)");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Top Metrics
        JPanel statsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(2000, 90));

        cardBudgetSlots = new StatCard("Funded Slots Limit", "0 Slots", "Based on ₱150,000 Budget", Theme.PRIMARY);
        cardEligibleCandidates = new StatCard("Verified Candidates", "0 Students", "Ready for ranking & slot allocation", Theme.ACCENT);

        statsGrid.add(cardBudgetSlots);
        statsGrid.add(cardEligibleCandidates);
        centerPanel.add(statsGrid);
        centerPanel.add(Box.createVerticalStrut(16));

        // Controls Card
        JPanel controlsCard = Theme.createCardPanel();
        controlsCard.setMaximumSize(new Dimension(2000, 110));
        JPanel ctrlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        ctrlRow.setOpaque(false);

        JLabel lblCrit = new JLabel("Ranking Algorithm:");
        lblCrit.setFont(Theme.FONT_BODY_BOLD);
        cmbCriteria = new JComboBox<>(ScholarshipManager.RankingCriteria.values());
        cmbCriteria.setPreferredSize(new Dimension(320, 32));
        cmbCriteria.addActionListener(e -> previewRanking());

        JLabel lblSlots = new JLabel("Max Slot Quota:");
        lblSlots.setFont(Theme.FONT_BODY_BOLD);
        spnSlots = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
        spnSlots.setPreferredSize(new Dimension(80, 32));
        spnSlots.addChangeListener(e -> previewRanking());

        JButton btnPreview = new JButton("Refresh Ranking");
        btnPreview.setFont(Theme.FONT_BODY_BOLD);
        btnPreview.addActionListener(e -> previewRanking());

        JButton btnAllocate = Theme.createStyledButton("Confirm & Allocate Slots ✔", Theme.ACCENT, Color.WHITE);
        btnAllocate.addActionListener(e -> executeAllocation());

        ctrlRow.add(lblCrit);
        ctrlRow.add(cmbCriteria);
        ctrlRow.add(lblSlots);
        ctrlRow.add(spnSlots);
        ctrlRow.add(btnPreview);
        ctrlRow.add(btnAllocate);

        controlsCard.add(ctrlRow, BorderLayout.CENTER);
        centerPanel.add(controlsCard);
        centerPanel.add(Box.createVerticalStrut(16));

        // Ranking Table Card
        JPanel tableCard = Theme.createCardPanel();
        JLabel tblTitle = new JLabel("Algorithm Ranking & Allocation Cutoff Preview");
        tblTitle.setFont(Theme.FONT_HEADER_SM);
        tblTitle.setForeground(Theme.TEXT_TITLE);

        String[] cols = {"Rank", "Allocation Outcome", "Applicant ID", "Full Name", "GWA (Merit)", "Monthly Income (Need)", "School", "Purok"};
        rankTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        rankTable = new ModernTable(rankTableModel);
        JScrollPane tblScroll = ModernTable.createScrollPane(rankTable);

        tableCard.add(tblTitle, BorderLayout.NORTH);
        tableCard.add(tblScroll, BorderLayout.CENTER);
        centerPanel.add(tableCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    private List<Applicant> getCandidates() {
        List<Applicant> list = new ArrayList<>();
        for (Applicant a : manager.getMasterList()) {
            if (a.getStatus() == ApplicationStatus.VERIFIED
                    || a.getStatus() == ApplicationStatus.WAITLISTED
                    || a.getStatus() == ApplicationStatus.APPROVED) {
                list.add(a);
            }
        }
        return list;
    }

    public void previewRanking() {
        int maxSlots = manager.getMaxPossibleSlots();
        cardBudgetSlots.setValue(maxSlots + " Slots Funded");
        cardBudgetSlots.setSubtitle(String.format("₱%,.2f per scholar | Total ₱%,.2f",
                manager.getGrantPerScholar(), manager.getTotalBudget()));

        List<Applicant> candidates = getCandidates();
        cardEligibleCandidates.setValue(candidates.size() + " Students");

        ScholarshipManager.RankingCriteria criteria = (ScholarshipManager.RankingCriteria) cmbCriteria.getSelectedItem();
        if (criteria == ScholarshipManager.RankingCriteria.MERIT_GWA) {
            // Rubric: Merge Sort
            AlgorithmUtils.mergeSortByGWA(candidates, true);
        } else {
            // Rubric: Quick Sort
            AlgorithmUtils.quickSortByIncome(candidates, true);
        }

        int slotQuota = (Integer) spnSlots.getValue();
        rankTableModel.setRowCount(0);

        for (int i = 0; i < candidates.size(); i++) {
            Applicant a = candidates.get(i);
            int rank = i + 1;
            String outcome = (rank <= slotQuota) ? "★ OFFICIAL SLOT (APPROVED)" : "⏳ WAITLIST QUEUE (FIFO)";

            rankTableModel.addRow(new Object[]{
                    "#" + rank,
                    outcome,
                    a.getApplicantId(),
                    a.getFullName(),
                    a.getFormattedGwa(),
                    a.getFormattedIncome(),
                    a.getSchool(),
                    a.getPurok()
            });
        }
    }

    private void executeAllocation() {
        ScholarshipManager.RankingCriteria criteria = (ScholarshipManager.RankingCriteria) cmbCriteria.getSelectedItem();
        int slotQuota = (Integer) spnSlots.getValue();

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Execute Slot Allocation with:\n- Algorithm: %s\n- Available Slots: %d\n\nTop %d will be APPROVED into the Scholarship Directory.\nRemaining will be placed in the Waitlist Queue.",
                        criteria.getTitle(), slotQuota, slotQuota),
                "Confirm Slot Allocation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            manager.evaluateAndAllocateSlots(criteria, slotQuota);
            JOptionPane.showMessageDialog(this,
                    "Slot Allocation successfully completed!\nOfficial scholars have been added to the Scholar Directory (BST).\nExcess applicants are queued in the Waitlist.",
                    "Allocation Complete", JOptionPane.INFORMATION_MESSAGE);
            previewRanking();
        }
    }
}
