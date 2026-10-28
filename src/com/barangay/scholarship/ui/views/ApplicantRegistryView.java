package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.DocumentChecklist;
import com.barangay.scholarship.model.Scholar;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.ui.components.ModernTable;
import com.barangay.scholarship.ui.components.StatCard;
import com.barangay.scholarship.util.FileUploadManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dedicated Applicant Registry View displaying the master database of registered applicants.
 * Full width, stretched table with search, counts, reactive synchronization,
 * and a detailed inspection card when a row is selected.
 */
public class ApplicantRegistryView extends JPanel {
    private final ScholarshipManager manager;

    // Stat Cards
    private StatCard cardTotalApplicants;
    private StatCard cardVerifiedApplicants;

    // Search & Filter
    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;
    private JComboBox<String> cmbPurokFilter;

    // Selected Applicant Detail Panel
    private JPanel detailCard;
    private JLabel lblDetailTitle;
    private JLabel lblDetailSubtitle;
    private JLabel valId, valName, valContact, valSchool, valYear, valPurok, valGwa, valIncome, valStatus;
    private JPanel docBadgesPanel;
    private Applicant currentSelectedApplicant;

    // Table
    private DefaultTableModel regTableModel;
    private ModernTable regTable;

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
        statsGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardTotalApplicants = new StatCard("Total Registered Applicants", "0", "All Recorded Intakes", Theme.PRIMARY);
        cardVerifiedApplicants = new StatCard("Screened / Verified", "0", "Eligible or Advanced in Pipeline", Theme.ACCENT);

        statsGrid.add(cardTotalApplicants);
        statsGrid.add(cardVerifiedApplicants);
        centerPanel.add(statsGrid);
        centerPanel.add(Box.createVerticalStrut(14));

        // Toolbar Card: Full-Width Responsive Search Bar
        JPanel toolbarCard = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(32767, getPreferredSize().height);
            }
        };
        toolbarCard.setBackground(Theme.CARD_BG);
        toolbarCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        toolbarCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel searchBar = new JPanel(new BorderLayout(14, 0));
        searchBar.setOpaque(false);

        JLabel lblSearch = new JLabel("Search Registry:");
        lblSearch.setFont(Theme.FONT_BODY_BOLD);
        lblSearch.setForeground(Theme.TEXT_TITLE);

        txtSearch = Theme.createRoundedTextField("Search by ID, name, purok, school...");
        txtSearch.setPreferredSize(new Dimension(220, 38));
        txtSearch.setMaximumSize(new Dimension(32767, 38));
        txtSearch.setToolTipText("Type to filter live in real-time");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        txtSearch.addActionListener(e -> applyFilter());

        // Multi-Filter Dropdowns: Status & Purok
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filtersPanel.setOpaque(false);

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(Theme.FONT_SMALL_BOLD);
        lblStatus.setForeground(Theme.TEXT_MUTED);

        String[] statuses = {"All", "Pending Verification", "Verified", "Approved", "Waitlisted", "Rejected"};
        cmbStatusFilter = new JComboBox<>(statuses);
        cmbStatusFilter.setFont(Theme.FONT_SMALL);
        cmbStatusFilter.setPreferredSize(new Dimension(150, 36));
        cmbStatusFilter.addActionListener(e -> applyFilter());

        JLabel lblPurok = new JLabel("Purok:");
        lblPurok.setFont(Theme.FONT_SMALL_BOLD);
        lblPurok.setForeground(Theme.TEXT_MUTED);

        String[] puroks = {"All", "Purok 1", "Purok 2", "Purok 3", "Purok 4", "Purok 5", "Purok 6"};
        cmbPurokFilter = new JComboBox<>(puroks);
        cmbPurokFilter.setFont(Theme.FONT_SMALL);
        cmbPurokFilter.setPreferredSize(new Dimension(110, 36));
        cmbPurokFilter.addActionListener(e -> applyFilter());

        JButton btnReset = new JButton("Reset");
        btnReset.setFont(Theme.FONT_SMALL);
        btnReset.setPreferredSize(new Dimension(70, 36));
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatusFilter.setSelectedIndex(0);
            cmbPurokFilter.setSelectedIndex(0);
            applyFilter();
        });

        filtersPanel.add(lblStatus);
        filtersPanel.add(cmbStatusFilter);
        filtersPanel.add(lblPurok);
        filtersPanel.add(cmbPurokFilter);
        filtersPanel.add(btnReset);

        searchBar.add(lblSearch, BorderLayout.WEST);
        searchBar.add(txtSearch, BorderLayout.CENTER);
        searchBar.add(filtersPanel, BorderLayout.EAST);

        toolbarCard.add(searchBar, BorderLayout.CENTER);
        centerPanel.add(toolbarCard);
        centerPanel.add(Box.createVerticalStrut(14));

        // Middle: Selected Row Information Inspection Card (initially visible or showing placeholder)
        detailCard = createDetailInspectionCard();
        detailCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        detailCard.setVisible(false); // Shown when a row is selected
        centerPanel.add(detailCard);

        // Table Card: Full Width
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel tableTitle = new JLabel("All Applicant Intake Records");
        tableTitle.setFont(Theme.FONT_HEADER_SM);
        tableTitle.setForeground(Theme.TEXT_TITLE);

        String[] cols = {"Applicant ID", "Full Name", "Purok / Zone", "GWA", "Monthly Income", "School / University", "Year Level", "Status"};
        regTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        regTable = new ModernTable(regTableModel);
        regTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Row selection listener to update detail panel
        regTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = regTable.getSelectedRow();
                if (row >= 0 && row < regTableModel.getRowCount()) {
                    String appId = (String) regTableModel.getValueAt(row, 0);
                    Applicant selected = manager.findApplicantById(appId);
                    if (selected != null) {
                        displayApplicantDetails(selected);
                    }
                }
            }
        });

        JScrollPane tblScroll = ModernTable.createScrollPane(regTable);

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(tblScroll, BorderLayout.CENTER);
        centerPanel.add(tableCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createDetailInspectionCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Header with close button
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel titleGrp = new JPanel();
        titleGrp.setOpaque(false);
        titleGrp.setLayout(new BoxLayout(titleGrp, BoxLayout.Y_AXIS));

        lblDetailTitle = new JLabel("Selected Applicant Information");
        lblDetailTitle.setFont(Theme.FONT_HEADER_MD);
        lblDetailTitle.setForeground(Theme.TEXT_TITLE);

        lblDetailSubtitle = new JLabel("Full student profile, academic metrics, and verified requirements");
        lblDetailSubtitle.setFont(Theme.FONT_SMALL);
        lblDetailSubtitle.setForeground(Theme.TEXT_MUTED);

        titleGrp.add(lblDetailTitle);
        titleGrp.add(Box.createVerticalStrut(2));
        titleGrp.add(lblDetailSubtitle);
        topHeader.add(titleGrp, BorderLayout.WEST);

        JButton btnClose = new JButton("Close Details");
        btnClose.setFont(Theme.FONT_SMALL_BOLD);
        btnClose.setForeground(Theme.TEXT_MUTED);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> {
            detailCard.setVisible(false);
            currentSelectedApplicant = null;
            regTable.clearSelection();
            revalidate();
            repaint();
        });
        topHeader.add(btnClose, BorderLayout.EAST);

        card.add(topHeader, BorderLayout.NORTH);

        // Body: Information Fields (3 columns for spacious text and zero clipping)
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
        valStatus = new JLabel("-");

        body.add(createDetailField("APPLICANT ID", valId));
        body.add(createDetailField("FULL NAME", valName));
        body.add(createDetailField("CONTACT & EMAIL", valContact));
        body.add(createDetailField("SCHOOL / UNIVERSITY", valSchool));
        body.add(createDetailField("YEAR LEVEL", valYear));
        body.add(createDetailField("BARANGAY PUROK", valPurok));
        body.add(createDetailField("ACADEMIC GWA", valGwa));
        body.add(createDetailField("MONTHLY FAMILY INCOME", valIncome));
        body.add(createDetailField("PIPELINE STATUS", valStatus));

        JPanel centerArea = new JPanel();
        centerArea.setOpaque(false);
        centerArea.setLayout(new BoxLayout(centerArea, BoxLayout.Y_AXIS));
        centerArea.add(body);
        centerArea.add(Box.createVerticalStrut(12));

        // Documents Attached Bar
        JPanel docRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        docRow.setOpaque(false);

        JLabel lblDocsTitle = new JLabel("Attached Documents:");
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

    private void displayApplicantDetails(Applicant a) {
        this.currentSelectedApplicant = a;
        lblDetailTitle.setText(a.getFullName() + " (" + a.getApplicantId() + ")");
        String subText = "Registered: " + a.getFormattedDate() + " • ";
        if (a instanceof Scholar) {
            Scholar s = (Scholar) a;
            subText += "Approved Scholar [ID: " + s.getScholarId() + "] • Grant: ₱" + String.format("%,.2f", s.getGrantAmount());
        } else {
            subText += a.getEligibilityReason();
        }
        lblDetailSubtitle.setText(subText);

        valId.setText(a.getApplicantId());
        valName.setText(a.getFullName());
        valContact.setText(a.getContactNumber() + " | " + a.getEmail());
        valSchool.setText(a.getSchool());
        valYear.setText(a.getYearLevel());
        valPurok.setText(a.getPurok());
        valGwa.setText(a.getFormattedGwa() + (a.isCollegeScale() ? " (Collegiate)" : " (%)"));
        valIncome.setText(a.getFormattedIncome() + " / mo");
        valStatus.setText(a.getStatus().getDisplayName());

        // Update Document Badges & View Links
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
            badge.setForeground(new Color(4, 120, 87));
            badge.setBackground(Theme.ACCENT_BG);
            badge.setBorder(BorderFactory.createLineBorder(new Color(167, 243, 208), 1));
        } else {
            badge.setForeground(Theme.TEXT_MUTED);
            badge.setBackground(new Color(241, 245, 249));
            badge.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        }

        if (hasFile) {
            badge.setCursor(new Cursor(Cursor.HAND_CURSOR));
            badge.setToolTipText("Click to view uploaded attachment: " + filePath);
            badge.addActionListener(e -> FileUploadManager.openFile(this, filePath));
        } else {
            badge.setEnabled(verified);
        }

        docBadgesPanel.add(badge);
    }

    public void refreshTable() {
        populateTable(manager.getMasterList());
        updateStats();
    }

    private void applyFilter() {
        String query = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        String selectedStatus = cmbStatusFilter != null ? (String) cmbStatusFilter.getSelectedItem() : "All";
        String selectedPurok = cmbPurokFilter != null ? (String) cmbPurokFilter.getSelectedItem() : "All";

        List<Applicant> filtered = new java.util.ArrayList<>();
        for (Applicant a : manager.getMasterList()) {
            boolean matchesQuery = query.isEmpty()
                    || a.getApplicantId().toLowerCase().contains(query)
                    || a.getFullName().toLowerCase().contains(query)
                    || a.getPurok().toLowerCase().contains(query)
                    || a.getSchool().toLowerCase().contains(query)
                    || a.getYearLevel().toLowerCase().contains(query)
                    || a.getEmail().toLowerCase().contains(query)
                    || a.getContactNumber().toLowerCase().contains(query)
                    || a.getStatus().getDisplayName().toLowerCase().contains(query)
                    || a.getStatus().name().toLowerCase().contains(query);

            boolean matchesStatus = true;
            if (selectedStatus != null && !selectedStatus.equalsIgnoreCase("All")) {
                if (selectedStatus.equalsIgnoreCase("Pending Verification")) {
                    matchesStatus = (a.getStatus() == com.barangay.scholarship.model.ApplicationStatus.PENDING_VERIFICATION);
                } else if (selectedStatus.equalsIgnoreCase("Verified")) {
                    matchesStatus = (a.getStatus() == com.barangay.scholarship.model.ApplicationStatus.VERIFIED);
                } else if (selectedStatus.equalsIgnoreCase("Approved")) {
                    matchesStatus = (a.getStatus() == com.barangay.scholarship.model.ApplicationStatus.APPROVED
                            || a.getStatus() == com.barangay.scholarship.model.ApplicationStatus.DISBURSED);
                } else if (selectedStatus.equalsIgnoreCase("Waitlisted")) {
                    matchesStatus = (a.getStatus() == com.barangay.scholarship.model.ApplicationStatus.WAITLISTED);
                } else if (selectedStatus.equalsIgnoreCase("Rejected")) {
                    matchesStatus = (a.getStatus() == com.barangay.scholarship.model.ApplicationStatus.REJECTED);
                }
            }

            boolean matchesPurok = true;
            if (selectedPurok != null && !selectedPurok.equalsIgnoreCase("All")) {
                matchesPurok = a.getPurok().toLowerCase().contains(selectedPurok.toLowerCase());
            }

            if (matchesQuery && matchesStatus && matchesPurok) {
                filtered.add(a);
            }
        }
        populateTable(filtered);
    }

    private void populateTable(List<Applicant> list) {
        String selectedId = (currentSelectedApplicant != null) ? currentSelectedApplicant.getApplicantId() : null;
        regTableModel.setRowCount(0);
        int reselectRow = -1;
        Applicant reselectApp = null;

        for (int i = 0; i < list.size(); i++) {
            Applicant a = list.get(i);
            if (selectedId != null && selectedId.equals(a.getApplicantId())) {
                reselectRow = i;
                reselectApp = a;
            }
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

        if (reselectRow >= 0 && reselectApp != null) {
            regTable.setRowSelectionInterval(reselectRow, reselectRow);
            displayApplicantDetails(reselectApp);
        } else if (list.isEmpty() || currentSelectedApplicant != null) {
            detailCard.setVisible(false);
            currentSelectedApplicant = null;
        }
    }

    private void updateStats() {
        List<Applicant> list = manager.getMasterList();
        cardTotalApplicants.setValue(String.valueOf(list.size()));
        cardVerifiedApplicants.setValue(String.valueOf(manager.getVerifiedApplicantCount()));
    }
}
