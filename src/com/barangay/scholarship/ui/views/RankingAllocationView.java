package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.algorithms.AlgorithmUtils;
import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.ApplicationStatus;
import com.barangay.scholarship.model.DocumentChecklist;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.ui.components.ModernTable;
import com.barangay.scholarship.ui.components.StatCard;
import com.barangay.scholarship.util.FileUploadManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
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
    private JTextField txtSearch;
    private StatCard cardBudgetSlots;
    private StatCard cardEligibleCandidates;

    // Selected Row Preview Panel
    private JPanel detailCard;
    private JLabel lblDetailTitle;
    private JLabel lblDetailSubtitle;
    private JLabel lblDetailBadge;
    private JLabel valId, valName, valContact, valSchool, valYear, valPurok, valGwa, valIncome, valOutcome;
    private JPanel docBadgesPanel;

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
        // Top Container: Header + Full-Width Stats Grid
        JPanel topPanel = new JPanel(new BorderLayout(0, 14));
        topPanel.setOpaque(false);

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
        topPanel.add(headerPanel, BorderLayout.NORTH);

        // Top Metrics - 2 cards spanning 50% each with no trailing gap
        JPanel statsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        statsGrid.setOpaque(false);

        cardBudgetSlots = new StatCard("Funded Slots Limit", "0 Slots", "Based on ₱150,000 Budget", Theme.PRIMARY);
        cardEligibleCandidates = new StatCard("Verified Candidates", "0 Students", "Ready for ranking & slot allocation", Theme.ACCENT);

        statsGrid.add(cardBudgetSlots);
        statsGrid.add(cardEligibleCandidates);
        topPanel.add(statsGrid, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Controls Card: Responsive GridBagLayout / Wrap-proof header
        JPanel controlsCard = Theme.createCardPanel();
        JPanel ctrlRow = new JPanel(new GridBagLayout());
        ctrlRow.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblCrit = new JLabel("Ranking Algorithm:");
        lblCrit.setFont(Theme.FONT_BODY_BOLD);
        cmbCriteria = new JComboBox<>(ScholarshipManager.RankingCriteria.values());
        cmbCriteria.setPreferredSize(new Dimension(210, 32));
        cmbCriteria.addActionListener(e -> previewRanking());

        JLabel lblSlots = new JLabel("Max Slots:");
        lblSlots.setFont(Theme.FONT_BODY_BOLD);
        spnSlots = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
        spnSlots.setPreferredSize(new Dimension(60, 32));
        spnSlots.addChangeListener(e -> previewRanking());

        JButton btnPreview = new JButton("Refresh");
        btnPreview.setFont(Theme.FONT_BODY_BOLD);
        btnPreview.addActionListener(e -> previewRanking());

        JButton btnAllocate = Theme.createStyledButton("Confirm & Allocate Slots", Theme.PRIMARY, Color.WHITE);
        btnAllocate.setFont(Theme.FONT_BODY_BOLD);
        btnAllocate.addActionListener(e -> executeAllocation());

        gbc.gridx = 0; gbc.gridy = 0; ctrlRow.add(lblCrit, gbc);
        gbc.gridx = 1; gbc.gridy = 0; ctrlRow.add(cmbCriteria, gbc);
        gbc.gridx = 2; gbc.gridy = 0; ctrlRow.add(lblSlots, gbc);
        gbc.gridx = 3; gbc.gridy = 0; ctrlRow.add(spnSlots, gbc);
        gbc.gridx = 4; gbc.gridy = 0; ctrlRow.add(btnPreview, gbc);
        gbc.gridx = 5; gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        ctrlRow.add(btnAllocate, gbc);

        controlsCard.add(ctrlRow, BorderLayout.CENTER);
        controlsCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlsCard.setMaximumSize(new Dimension(32767, controlsCard.getPreferredSize().height));
        centerPanel.add(controlsCard);
        centerPanel.add(Box.createVerticalStrut(14));

        // Toolbar Card: Full-Width Responsive Search Bar for Candidates
        JPanel searchCard = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(32767, getPreferredSize().height);
            }
        };
        searchCard.setBackground(Theme.CARD_BG);
        searchCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel searchBar = new JPanel(new BorderLayout(14, 0));
        searchBar.setOpaque(false);

        JLabel lblSearch = new JLabel("Search Candidates:");
        lblSearch.setFont(Theme.FONT_BODY_BOLD);
        lblSearch.setForeground(Theme.TEXT_TITLE);

        txtSearch = Theme.createRoundedTextField("Search candidates by ID, name, purok, school, or rank outcome...");
        txtSearch.setPreferredSize(new Dimension(300, 38));
        txtSearch.setMaximumSize(new Dimension(32767, 38));
        txtSearch.setToolTipText("Type to filter candidates live in real-time");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { previewRanking(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { previewRanking(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { previewRanking(); }
        });
        txtSearch.addActionListener(e -> previewRanking());

        JButton btnSearch = Theme.createStyledButton("Search", Theme.PRIMARY, Color.WHITE);
        btnSearch.setFont(Theme.FONT_BODY_BOLD);
        btnSearch.setPreferredSize(new Dimension(100, 38));
        btnSearch.addActionListener(e -> previewRanking());

        searchBar.add(lblSearch, BorderLayout.WEST);
        searchBar.add(txtSearch, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);
        searchCard.add(searchBar, BorderLayout.CENTER);

        centerPanel.add(searchCard);
        centerPanel.add(Box.createVerticalStrut(14));

        // Selected Row Preview Dossier Card (Between Algorithm Controls and Ranking Table)
        detailCard = createDetailInspectionCard();
        detailCard.setVisible(false);
        detailCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(detailCard);

        // Ranking Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel tblTitle = new JLabel("Algorithm Ranking & Allocation Cutoff Preview");
        tblTitle.setFont(Theme.FONT_HEADER_SM);
        tblTitle.setForeground(Theme.TEXT_TITLE);

        String[] cols = {"Rank", "Allocation Outcome", "Applicant ID", "Full Name", "GWA (Merit)", "Monthly Income (Need)", "School", "Purok"};
        rankTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        rankTable = new ModernTable(rankTableModel);

        // Badge styling on column 1: Allocation Outcome
        rankTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(Theme.FONT_SMALL_BOLD);
                String text = (value != null) ? value.toString() : "";
                if (text.contains("APPROVED") || text.contains("OFFICIAL")) {
                    c.setBackground(isSelected ? Theme.ACCENT : Theme.ACCENT_BG);
                    c.setForeground(isSelected ? Color.WHITE : new Color(6, 95, 70));
                } else {
                    c.setBackground(isSelected ? new Color(147, 51, 234) : new Color(243, 232, 255));
                    c.setForeground(isSelected ? Color.WHITE : new Color(126, 34, 206));
                }
                c.setOpaque(true);
                return c;
            }
        });

        rankTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = rankTable.getSelectedRow();
                if (row >= 0 && row < rankTableModel.getRowCount()) {
                    String appId = (String) rankTableModel.getValueAt(row, 2);
                    Applicant a = manager.findApplicantById(appId);
                    if (a != null) {
                        String rankStr = (String) rankTableModel.getValueAt(row, 0);
                        int rank = row + 1;
                        try {
                            rank = Integer.parseInt(rankStr.replace("#", "").trim());
                        } catch (Exception ignored) {}
                        String outcome = (String) rankTableModel.getValueAt(row, 1);
                        displayApplicantDetails(a, rank, outcome);
                    }
                }
            }
        });

        JScrollPane tblScroll = ModernTable.createScrollPane(rankTable);
        tableCard.add(tblTitle, BorderLayout.NORTH);
        tableCard.add(tblScroll, BorderLayout.CENTER);
        centerPanel.add(tableCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createDetailInspectionCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));
        // Add bottom margin inside the card so there's always clean spacing before the table
        card.setBorder(new CompoundBorder(
                new EmptyBorder(0, 0, 14, 0),
                Theme.createCardBorder()
        ));

        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel titleGrp = new JPanel();
        titleGrp.setOpaque(false);
        titleGrp.setLayout(new BoxLayout(titleGrp, BoxLayout.Y_AXIS));

        lblDetailTitle = new JLabel("APPLICANT EVALUATION DOSSIER");
        lblDetailTitle.setFont(Theme.FONT_HEADER_MD);
        lblDetailTitle.setForeground(Theme.TEXT_TITLE);

        lblDetailSubtitle = new JLabel("Detailed academic merit, financial need, and allocation status");
        lblDetailSubtitle.setFont(Theme.FONT_SMALL);
        lblDetailSubtitle.setForeground(Theme.TEXT_MUTED);

        titleGrp.add(lblDetailTitle);
        titleGrp.add(Box.createVerticalStrut(2));
        titleGrp.add(lblDetailSubtitle);
        topHeader.add(titleGrp, BorderLayout.WEST);

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionGroup.setOpaque(false);

        lblDetailBadge = new JLabel();
        lblDetailBadge.setFont(Theme.FONT_SMALL_BOLD);
        lblDetailBadge.setOpaque(true);
        actionGroup.add(lblDetailBadge);

        JButton btnClose = new JButton("Close Preview");
        btnClose.setFont(Theme.FONT_SMALL_BOLD);
        btnClose.setForeground(Theme.TEXT_MUTED);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> {
            detailCard.setVisible(false);
            rankTable.clearSelection();
            revalidate();
            repaint();
        });
        actionGroup.add(btnClose);

        topHeader.add(actionGroup, BorderLayout.EAST);
        card.add(topHeader, BorderLayout.NORTH);

        // Body: Information Fields (3x3 grid)
        JPanel body = new JPanel(new GridLayout(3, 3, 20, 10));
        body.setOpaque(false);

        valId = new JLabel("-");
        valName = new JLabel("-");
        valContact = new JLabel("-");
        valSchool = new JLabel("-");
        valYear = new JLabel("-");
        valPurok = new JLabel("-");
        valGwa = new JLabel("-");
        valIncome = new JLabel("-");
        valOutcome = new JLabel("-");

        body.add(createDetailField("APPLICANT ID", valId));
        body.add(createDetailField("FULL NAME", valName));
        body.add(createDetailField("CONTACT & EMAIL", valContact));
        body.add(createDetailField("SCHOOL / UNIVERSITY", valSchool));
        body.add(createDetailField("YEAR LEVEL", valYear));
        body.add(createDetailField("BARANGAY PUROK", valPurok));
        body.add(createDetailField("ACADEMIC GWA", valGwa));
        body.add(createDetailField("MONTHLY FAMILY INCOME", valIncome));
        body.add(createDetailField("PROJECTED OUTCOME", valOutcome));

        JPanel centerArea = new JPanel();
        centerArea.setOpaque(false);
        centerArea.setLayout(new BoxLayout(centerArea, BoxLayout.Y_AXIS));
        centerArea.add(body);
        centerArea.add(Box.createVerticalStrut(12));

        // Documents Attached Bar
        JPanel docRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        docRow.setOpaque(false);

        JLabel lblDocsTitle = new JLabel("Submitted Requirements:");
        lblDocsTitle.setFont(Theme.FONT_SMALL_BOLD);
        lblDocsTitle.setForeground(Theme.TEXT_MUTED);
        docRow.add(lblDocsTitle);

        docBadgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        docBadgesPanel.setOpaque(false);
        docRow.add(docBadgesPanel);

        centerArea.add(docRow);
        card.add(centerArea, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDetailField(String label, JLabel valLabel) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL_BOLD);
        lbl.setForeground(Theme.TEXT_MUTED);

        valLabel.setFont(Theme.FONT_BODY_BOLD);
        valLabel.setForeground(Theme.TEXT_TITLE);

        p.add(lbl);
        p.add(Box.createVerticalStrut(2));
        p.add(valLabel);
        return p;
    }

    private void displayApplicantDetails(Applicant a, int rank, String outcome) {
        lblDetailTitle.setText(a.getFullName() + " (" + a.getApplicantId() + ")");
        lblDetailSubtitle.setText(String.format("Candidate Rank #%d • %s • %s • %s",
                rank, a.getSchool(), a.getYearLevel(), a.getPurok()));

        valId.setText(a.getApplicantId());
        valName.setText(a.getFullName());
        valContact.setText(a.getEmail() + " | " + a.getContactNumber());
        valSchool.setText(a.getSchool());
        valYear.setText(a.getYearLevel());
        valPurok.setText(a.getPurok());
        valGwa.setText(a.getFormattedGwa());
        valIncome.setText(a.getFormattedIncome());

        boolean isApproved = outcome.contains("APPROVED") || outcome.contains("OFFICIAL");
        if (isApproved) {
            valOutcome.setText(outcome);
            valOutcome.setForeground(Theme.ACCENT_HOVER);

            lblDetailBadge.setText("  OFFICIAL SCHOLARSHIP RECIPIENT  ");
            lblDetailBadge.setBackground(Theme.ACCENT_BG);
            lblDetailBadge.setForeground(new Color(6, 95, 70));
            lblDetailBadge.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(Theme.ACCENT, 1),
                    new EmptyBorder(4, 10, 4, 10)
            ));
        } else {
            valOutcome.setText(outcome);
            valOutcome.setForeground(new Color(126, 34, 206));

            lblDetailBadge.setText("  WAITLIST QUEUE CANDIDATE (FIFO)  ");
            lblDetailBadge.setBackground(new Color(243, 232, 255));
            lblDetailBadge.setForeground(new Color(126, 34, 206));
            lblDetailBadge.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(new Color(192, 132, 252), 1),
                    new EmptyBorder(4, 10, 4, 10)
            ));
        }

        // Populate doc badges
        docBadgesPanel.removeAll();
        DocumentChecklist docs = a.getDocuments();

        addDocBadge("Certificate of Indigency", docs.isCertificateOfIndigency(), docs.getIndigencyFilePath());
        addDocBadge("Copy of Grades / TOR", docs.isCertifiedCopyOfGrades(), docs.getGradesFilePath());
        addDocBadge("Enrollment Form (COR)", docs.isEnrollmentForm(), docs.getEnrollmentFilePath());
        addDocBadge("Valid ID", docs.isValidBarangayOrSchoolId(), docs.getIdFilePath());

        docBadgesPanel.revalidate();
        docBadgesPanel.repaint();

        detailCard.setVisible(true);
        revalidate();
        repaint();
    }

    private void addDocBadge(String name, boolean verified, String filePath) {
        boolean hasFile = (filePath != null && !filePath.trim().isEmpty());
        JButton badge = new JButton((verified ? "[Verified] " : "[Missing] ") + name + (hasFile ? " [View File]" : ""));
        badge.setFont(Theme.FONT_SMALL);
        badge.setFocusPainted(false);
        badge.setMargin(new Insets(2, 6, 2, 6));

        if (verified) {
            badge.setBackground(Theme.ACCENT_BG);
            badge.setForeground(Theme.ACCENT_HOVER);
        } else {
            badge.setBackground(Theme.WARNING_BG);
            badge.setForeground(Theme.WARNING);
        }

        if (hasFile) {
            badge.setCursor(new Cursor(Cursor.HAND_CURSOR));
            badge.setToolTipText("Click to view archived digital file");
            badge.addActionListener(e -> FileUploadManager.openFile(this, filePath));
        } else {
            badge.setEnabled(false);
        }

        docBadgesPanel.add(badge);
    }

    private List<Applicant> getCandidates() {
        List<Applicant> list = new ArrayList<>();
        for (Applicant a : manager.getMasterList()) {
            if (a.getStatus() == ApplicationStatus.VERIFIED
                    || a.getStatus() == ApplicationStatus.WAITLISTED) {
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

        String previouslySelectedAppId = null;
        if (detailCard != null && detailCard.isVisible() && valId != null && !"-".equals(valId.getText())) {
            previouslySelectedAppId = valId.getText();
        }

        int slotQuota = (Integer) spnSlots.getValue();
        rankTableModel.setRowCount(0);
        int reselectRow = -1;
        Applicant reselectApp = null;
        String reselectOutcome = null;

        String q = (txtSearch != null) ? txtSearch.getText().trim().toLowerCase() : "";

        for (int i = 0; i < candidates.size(); i++) {
            Applicant a = candidates.get(i);
            int rank = i + 1;
            String outcome = (rank <= slotQuota) ? "OFFICIAL SLOT (APPROVED)" : "WAITLIST QUEUE (FIFO)";

            if (!q.isEmpty()) {
                boolean matches = a.getApplicantId().toLowerCase().contains(q)
                        || a.getFullName().toLowerCase().contains(q)
                        || a.getPurok().toLowerCase().contains(q)
                        || a.getSchool().toLowerCase().contains(q)
                        || a.getFormattedGwa().toLowerCase().contains(q)
                        || a.getFormattedIncome().toLowerCase().contains(q)
                        || outcome.toLowerCase().contains(q);
                if (!matches) {
                    continue;
                }
            }

            if (previouslySelectedAppId != null && previouslySelectedAppId.equals(a.getApplicantId())) {
                reselectRow = rankTableModel.getRowCount();
                reselectApp = a;
                reselectOutcome = outcome;
            }

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

        if (reselectRow >= 0 && reselectApp != null) {
            rankTable.setRowSelectionInterval(reselectRow, reselectRow);
            displayApplicantDetails(reselectApp, reselectRow + 1, reselectOutcome);
        } else if (!q.isEmpty() && rankTableModel.getRowCount() == 0) {
            detailCard.setVisible(false);
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
