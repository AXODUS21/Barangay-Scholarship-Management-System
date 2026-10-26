package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.model.DocumentChecklist;
import com.barangay.scholarship.model.Scholar;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.ui.components.ModernTable;
import com.barangay.scholarship.ui.components.StatCard;
import com.barangay.scholarship.util.DataPersistence;
import com.barangay.scholarship.util.FileUploadManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
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

    // Selected Scholar Preview Panel
    private JPanel detailCard;
    private JLabel lblDetailTitle;
    private JLabel lblDetailSubtitle;
    private JLabel lblDetailBadge;
    private JButton btnDetailDisburse;
    private JLabel valScholarId, valAppId, valName, valContact, valSchool, valYear, valPurok, valGrant, valStatus;
    private JPanel docBadgesPanel;
    private Scholar currentSelectedScholar;

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
        // Top Container: Header + Full-Width Stats Grid
        JPanel topPanel = new JPanel(new BorderLayout(0, 14));
        topPanel.setOpaque(false);

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
        topPanel.add(headerPanel, BorderLayout.NORTH);

        // Metric Cards - 3 cards spanning 33.3% each with zero trailing gap
        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        statsGrid.setOpaque(false);

        cardBudgetRemaining = new StatCard("Remaining SK Funds", "₱0.00", "Available for release", Theme.ACCENT);
        cardDisbursed = new StatCard("Total Disbursed", "₱0.00", "Successfully claimed by scholars", Theme.PRIMARY);
        cardClaimedRatio = new StatCard("Disbursement Progress", "0 / 0 Scholars", "0% completed", Theme.INFO);

        statsGrid.add(cardBudgetRemaining);
        statsGrid.add(cardDisbursed);
        statsGrid.add(cardClaimedRatio);
        topPanel.add(statsGrid, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Action Toolbar Card
        JPanel toolbarCard = Theme.createCardPanel();
        JPanel toolRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        toolRow.setOpaque(false);

        // Search Bar
        JLabel lblSearch = new JLabel("Search Scholar:");
        lblSearch.setFont(Theme.FONT_BODY_BOLD);
        txtSearch = new JTextField(14);
        txtSearch.setPreferredSize(new Dimension(160, 32));
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

        JButton btnSearch = new JButton("Filter");
        btnSearch.addActionListener(e -> applyFilter());
        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            refreshData();
        });

        toolRow.add(lblSearch);
        toolRow.add(txtSearch);
        toolRow.add(btnSearch);
        toolRow.add(btnReset);
        toolRow.add(Box.createHorizontalStrut(12));

        // Disbursement & Export Buttons
        JButton btnClaim = Theme.createStyledButton("Disburse Selected Stipend", Theme.ACCENT, Color.WHITE);
        btnClaim.addActionListener(e -> disburseSelected());

        JButton btnExport = Theme.createStyledButton("Export Payout Attendance Sheet", Theme.PRIMARY, Color.WHITE);
        btnExport.addActionListener(e -> exportAttendanceSheet());

        toolRow.add(btnClaim);
        toolRow.add(btnExport);

        toolbarCard.add(toolRow, BorderLayout.CENTER);
        toolbarCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolbarCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, toolbarCard.getPreferredSize().height));
        centerPanel.add(toolbarCard);
        centerPanel.add(Box.createVerticalStrut(14));

        // Selected Row Preview Dossier Card (Between Toolbar and Payout Table)
        detailCard = createDetailInspectionCard();
        detailCard.setVisible(false);
        detailCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(detailCard);

        // Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel tblTitle = new JLabel("Approved Scholars Payout Masterlist");
        tblTitle.setFont(Theme.FONT_HEADER_SM);
        tblTitle.setForeground(Theme.TEXT_TITLE);

        String[] cols = {"Scholar ID", "Applicant ID", "Full Name", "Purok/Zone", "School", "Grant Amount", "Disbursement Status", "Claimed Date"};
        payoutTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        payoutTable = new ModernTable(payoutTableModel);

        // Status badge styling on column 6: Disbursement Status
        payoutTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(Theme.FONT_SMALL_BOLD);
                String text = (value != null) ? value.toString() : "";
                if ("CLAIMED".equalsIgnoreCase(text)) {
                    c.setBackground(isSelected ? Theme.ACCENT : Theme.ACCENT_BG);
                    c.setForeground(isSelected ? Color.WHITE : new Color(6, 95, 70));
                } else {
                    c.setBackground(isSelected ? Theme.WARNING : Theme.WARNING_BG);
                    c.setForeground(isSelected ? Color.WHITE : Theme.WARNING);
                }
                c.setOpaque(true);
                return c;
            }
        });

        payoutTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = payoutTable.getSelectedRow();
                if (row >= 0 && row < payoutTableModel.getRowCount()) {
                    String schId = (String) payoutTableModel.getValueAt(row, 0);
                    Scholar s = manager.getScholarDirectory().searchById(schId);
                    if (s != null) {
                        displayScholarDetails(s);
                    }
                }
            }
        });

        JScrollPane tblScroll = ModernTable.createScrollPane(payoutTable);
        tableCard.add(tblTitle, BorderLayout.NORTH);
        tableCard.add(tblScroll, BorderLayout.CENTER);
        centerPanel.add(tableCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createDetailInspectionCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));
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

        lblDetailTitle = new JLabel("SCHOLAR PAYOUT & DISBURSEMENT DOSSIER");
        lblDetailTitle.setFont(Theme.FONT_HEADER_MD);
        lblDetailTitle.setForeground(Theme.TEXT_TITLE);

        lblDetailSubtitle = new JLabel("Official barangay scholar profile, verified documents, and disbursement claim record");
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

        btnDetailDisburse = Theme.createStyledButton("Disburse Stipend", Theme.ACCENT, Color.WHITE);
        btnDetailDisburse.addActionListener(e -> {
            if (currentSelectedScholar != null) {
                disburseScholar(currentSelectedScholar);
            }
        });
        actionGroup.add(btnDetailDisburse);

        JButton btnClose = new JButton("Close Preview");
        btnClose.setFont(Theme.FONT_SMALL_BOLD);
        btnClose.setForeground(Theme.TEXT_MUTED);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> {
            detailCard.setVisible(false);
            currentSelectedScholar = null;
            payoutTable.clearSelection();
            revalidate();
            repaint();
        });
        actionGroup.add(btnClose);

        topHeader.add(actionGroup, BorderLayout.EAST);
        card.add(topHeader, BorderLayout.NORTH);

        // Body: Information Fields (3x3 grid)
        JPanel body = new JPanel(new GridLayout(3, 3, 20, 10));
        body.setOpaque(false);

        valScholarId = new JLabel("-");
        valAppId = new JLabel("-");
        valName = new JLabel("-");
        valContact = new JLabel("-");
        valSchool = new JLabel("-");
        valYear = new JLabel("-");
        valPurok = new JLabel("-");
        valGrant = new JLabel("-");
        valStatus = new JLabel("-");

        body.add(createDetailField("SCHOLAR ID", valScholarId));
        body.add(createDetailField("APPLICANT ID", valAppId));
        body.add(createDetailField("FULL NAME", valName));
        body.add(createDetailField("CONTACT & EMAIL", valContact));
        body.add(createDetailField("SCHOOL / UNIVERSITY", valSchool));
        body.add(createDetailField("YEAR LEVEL", valYear));
        body.add(createDetailField("BARANGAY PUROK", valPurok));
        body.add(createDetailField("GRANT AMOUNT", valGrant));
        body.add(createDetailField("DISBURSEMENT STATUS", valStatus));

        JPanel centerArea = new JPanel();
        centerArea.setOpaque(false);
        centerArea.setLayout(new BoxLayout(centerArea, BoxLayout.Y_AXIS));
        centerArea.add(body);
        centerArea.add(Box.createVerticalStrut(12));

        // Documents Attached Bar
        JPanel docRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        docRow.setOpaque(false);

        JLabel lblDocsTitle = new JLabel("Verified Requirements:");
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

    private void displayScholarDetails(Scholar s) {
        this.currentSelectedScholar = s;
        if (s == null) {
            detailCard.setVisible(false);
            return;
        }

        lblDetailTitle.setText(s.getFullName() + " (" + s.getScholarId() + ")");
        lblDetailSubtitle.setText(String.format("Grant: %s • Academic Term: %s • %s • %s",
                s.getFormattedGrant(), s.getSemester(), s.getSchool(), s.getPurok()));

        valScholarId.setText(s.getScholarId());
        valAppId.setText(s.getApplicantId());
        valName.setText(s.getFullName());
        valContact.setText(s.getEmail() + " | " + s.getContactNumber());
        valSchool.setText(s.getSchool());
        valYear.setText(s.getYearLevel());
        valPurok.setText(s.getPurok());
        valGrant.setText(s.getFormattedGrant());

        if (s.isStipendDisbursed()) {
            valStatus.setText("CLAIMED on " + s.getFormattedDisbursedDate());
            valStatus.setForeground(Theme.ACCENT_HOVER);

            lblDetailBadge.setText("  STIPEND CLAIMED & DISBURSED  ");
            lblDetailBadge.setBackground(Theme.ACCENT_BG);
            lblDetailBadge.setForeground(new Color(6, 95, 70));
            lblDetailBadge.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(Theme.ACCENT, 1),
                    new EmptyBorder(4, 10, 4, 10)
            ));

            btnDetailDisburse.setText("Payout Already Claimed");
            btnDetailDisburse.setEnabled(false);
            btnDetailDisburse.setBackground(Theme.ACCENT);
            btnDetailDisburse.setForeground(Color.WHITE);
        } else {
            valStatus.setText("UNCLAIMED (Ready for Payout)");
            valStatus.setForeground(Theme.WARNING);

            lblDetailBadge.setText("  UNCLAIMED — READY FOR DISBURSEMENT  ");
            lblDetailBadge.setBackground(Theme.WARNING_BG);
            lblDetailBadge.setForeground(Theme.WARNING);
            lblDetailBadge.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(Theme.WARNING, 1),
                    new EmptyBorder(4, 10, 4, 10)
            ));

            btnDetailDisburse.setText("Disburse Stipend (" + s.getFormattedGrant() + ")");
            btnDetailDisburse.setEnabled(true);
            btnDetailDisburse.setBackground(Theme.ACCENT);
            btnDetailDisburse.setForeground(Color.WHITE);
        }

        // Populate doc badges
        docBadgesPanel.removeAll();
        DocumentChecklist docs = s.getDocuments();
        if (docs != null) {
            addDocBadge("Certificate of Indigency", docs.isCertificateOfIndigency(), docs.getIndigencyFilePath());
            addDocBadge("Copy of Grades / TOR", docs.isCertifiedCopyOfGrades(), docs.getGradesFilePath());
            addDocBadge("Enrollment Form (COR)", docs.isEnrollmentForm(), docs.getEnrollmentFilePath());
            addDocBadge("Valid ID", docs.isValidBarangayOrSchoolId(), docs.getIdFilePath());
        }

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

    private void applyFilter() {
        String q = txtSearch.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            refreshData();
            return;
        }

        payoutTableModel.setRowCount(0);
        List<Scholar> list = manager.getAllScholarsFromBST();
        int reselectRow = -1;
        Scholar reselectScholar = null;

        for (Scholar s : list) {
            if (s.getScholarId().toLowerCase().contains(q)
                    || s.getFullName().toLowerCase().contains(q)
                    || s.getPurok().toLowerCase().contains(q)) {
                if (currentSelectedScholar != null && currentSelectedScholar.getScholarId().equals(s.getScholarId())) {
                    reselectRow = payoutTableModel.getRowCount();
                    reselectScholar = s;
                }
                addScholarRow(s);
            }
        }

        if (reselectRow >= 0 && reselectScholar != null) {
            payoutTable.setRowSelectionInterval(reselectRow, reselectRow);
            displayScholarDetails(reselectScholar);
        } else if (payoutTableModel.getRowCount() == 0) {
            detailCard.setVisible(false);
            currentSelectedScholar = null;
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

        disburseScholar(scholar);
    }

    private void disburseScholar(Scholar scholar) {
        if (scholar == null) return;
        if (scholar.isStipendDisbursed()) {
            JOptionPane.showMessageDialog(this,
                    "Stipend for " + scholar.getFullName() + " has already been claimed on " + scholar.getFormattedDisbursedDate(),
                    "Already Disbursed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Confirm stipend payout release:\n\nScholar: %s (%s)\nPurok: %s\nGrant Amount: %s\n\nRemaining SK Budget will be deducted by %s.",
                        scholar.getFullName(), scholar.getScholarId(), scholar.getPurok(), scholar.getFormattedGrant(), scholar.getFormattedGrant()),
                "Confirm Payout Release", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = manager.releaseStipend(scholar.getScholarId());
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

        String previouslySelectedScholarId = (currentSelectedScholar != null) ? currentSelectedScholar.getScholarId() : null;

        payoutTableModel.setRowCount(0);
        int reselectRow = -1;
        Scholar reselectScholar = null;

        for (int i = 0; i < list.size(); i++) {
            Scholar s = list.get(i);
            if (previouslySelectedScholarId != null && previouslySelectedScholarId.equals(s.getScholarId())) {
                reselectRow = i;
                reselectScholar = s;
            }
            addScholarRow(s);
        }

        if (reselectRow >= 0 && reselectScholar != null) {
            payoutTable.setRowSelectionInterval(reselectRow, reselectRow);
            displayScholarDetails(reselectScholar);
        }
    }
}
