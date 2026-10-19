package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.datastructures.ScholarshipQueue;
import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.ApplicationStatus;
import com.barangay.scholarship.model.DocumentChecklist;
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
 * Transaction Stage 2: Document Verification & Screening Queue.
 * Visualizes the custom generic FIFO Queue (ScholarshipQueue).
 */
public class ScreeningQueueView extends JPanel {
    private final ScholarshipManager manager;

    // Stat Cards
    private StatCard cardQueueSize;
    private StatCard cardFrontApplicant;

    // Table of queued applicants
    private DefaultTableModel queueTableModel;
    private ModernTable queueTable;

    // Document Inspector Panel
    private JLabel lblSelectedName;
    private JLabel lblSelectedDetails;
    private JCheckBox chkIndigency;
    private JCheckBox chkGrades;
    private JCheckBox chkEnrollment;
    private JCheckBox chkId;
    private JButton btnVerifyApprove;
    private JButton btnReject;

    private Applicant currentSelectedApplicant;

    public ScreeningQueueView(ScholarshipManager manager) {
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

        JLabel title = new JLabel("Document Verification & Screening Queue");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Custom FIFO Queue: Applicants are screened in First-Come, First-Served order");
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

        // Queue Stats
        JPanel statsGrid = new JPanel(new GridLayout(1, 2, 16, 12));
        statsGrid.setOpaque(false);

        cardQueueSize = new StatCard("Screening Queue Length", "0 Applicants", "First-In, First-Out (FIFO)", Theme.INFO);
        cardFrontApplicant = new StatCard("Front of Line (Next to Process)", "None", "Will be evaluated first", Theme.PRIMARY);

        statsGrid.add(cardQueueSize);
        statsGrid.add(cardFrontApplicant);
        centerPanel.add(statsGrid);
        centerPanel.add(Box.createVerticalStrut(14));

        // Left: Queue Table
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setMinimumSize(new Dimension(320, 260));
        JLabel tblTitle = new JLabel("Live Screening Line (Front to Rear)");
        tblTitle.setFont(Theme.FONT_HEADER_SM);
        tblTitle.setForeground(Theme.TEXT_TITLE);

        String[] cols = {"Position", "ID", "Name", "School", "Purok", "Docs", "Status"};
        queueTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        queueTable = new ModernTable(queueTableModel);
        queueTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = queueTable.getSelectedRow();
                if (row >= 0 && row < queueTableModel.getRowCount()) {
                    String id = (String) queueTableModel.getValueAt(row, 1);
                    selectApplicant(manager.findApplicantById(id));
                }
            }
        });

        JScrollPane scrollTable = ModernTable.createScrollPane(queueTable);
        tableCard.add(tblTitle, BorderLayout.NORTH);
        tableCard.add(scrollTable, BorderLayout.CENTER);

        // Right: Document Inspector Card
        JPanel inspectorCard = Theme.createCardPanel();
        inspectorCard.setMinimumSize(new Dimension(280, 260));
        inspectorCard.setLayout(new BorderLayout());

        JPanel insHeader = new JPanel();
        insHeader.setOpaque(false);
        insHeader.setLayout(new BoxLayout(insHeader, BoxLayout.Y_AXIS));

        JLabel insTitle = new JLabel("PHYSICAL DOCUMENT SCREENING DESK");
        insTitle.setFont(Theme.FONT_SMALL_BOLD);
        insTitle.setForeground(Theme.TEXT_MUTED);

        lblSelectedName = new JLabel("Select an applicant from queue");
        lblSelectedName.setFont(Theme.FONT_HEADER_MD);
        lblSelectedName.setForeground(Theme.TEXT_TITLE);

        lblSelectedDetails = new JLabel("Click any row or peek at the front of line to inspect requirements.");
        lblSelectedDetails.setFont(Theme.FONT_BODY);
        lblSelectedDetails.setForeground(Theme.TEXT_MUTED);

        insHeader.add(insTitle);
        insHeader.add(Box.createVerticalStrut(6));
        insHeader.add(lblSelectedName);
        insHeader.add(Box.createVerticalStrut(4));
        insHeader.add(lblSelectedDetails);
        insHeader.add(Box.createVerticalStrut(14));

        inspectorCard.add(insHeader, BorderLayout.NORTH);

        // Checkbox panel
        JPanel chkPanel = new JPanel();
        chkPanel.setOpaque(false);
        chkPanel.setLayout(new BoxLayout(chkPanel, BoxLayout.Y_AXIS));

        chkIndigency = new JCheckBox("1. Certificate of Indigency (Barangay)");
        chkGrades = new JCheckBox("2. Certified Copy of Grades (Form 138 / TOR)");
        chkEnrollment = new JCheckBox("3. Certificate of Registration / Enrollment Form");
        chkId = new JCheckBox("4. Valid Barangay ID or Student ID");

        chkPanel.add(chkIndigency);
        chkPanel.add(Box.createVerticalStrut(8));
        chkPanel.add(chkGrades);
        chkPanel.add(Box.createVerticalStrut(8));
        chkPanel.add(chkEnrollment);
        chkPanel.add(Box.createVerticalStrut(8));
        chkPanel.add(chkId);
        chkPanel.add(Box.createVerticalGlue());

        JScrollPane chkScroll = new JScrollPane(chkPanel);
        chkScroll.setBorder(null);
        chkScroll.setOpaque(false);
        chkScroll.getViewport().setOpaque(false);
        inspectorCard.add(chkScroll, BorderLayout.CENTER);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        btnReject = new JButton("Mark Rejected");
        btnReject.setForeground(Theme.DANGER);
        btnReject.addActionListener(e -> rejectCurrentApplicant());

        btnVerifyApprove = Theme.createStyledButton("Save & Mark Verified", Theme.ACCENT, Color.WHITE);
        btnVerifyApprove.addActionListener(e -> verifyCurrentApplicant());

        btnPanel.add(btnReject);
        btnPanel.add(btnVerifyApprove);

        inspectorCard.add(btnPanel, BorderLayout.SOUTH);

        JSplitPane workSplit = Theme.createSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableCard, inspectorCard, 0.55);
        centerPanel.add(workSplit);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void selectApplicant(Applicant applicant) {
        this.currentSelectedApplicant = applicant;
        if (applicant == null) {
            lblSelectedName.setText("Select an applicant from queue");
            lblSelectedDetails.setText("Click any row or peek at the front of line to inspect requirements.");
            chkIndigency.setSelected(false);
            chkGrades.setSelected(false);
            chkEnrollment.setSelected(false);
            chkId.setSelected(false);
            btnVerifyApprove.setEnabled(false);
            btnReject.setEnabled(false);
            return;
        }

        lblSelectedName.setText(applicant.getFullName() + " (" + applicant.getApplicantId() + ")");
        lblSelectedDetails.setText(String.format("%s | %s | GWA: %s | Income: %s",
                applicant.getSchool(), applicant.getPurok(), applicant.getFormattedGwa(), applicant.getFormattedIncome()));

        DocumentChecklist docs = applicant.getDocuments();
        chkIndigency.setSelected(docs.isCertificateOfIndigency());
        chkGrades.setSelected(docs.isCertifiedCopyOfGrades());
        chkEnrollment.setSelected(docs.isEnrollmentForm());
        chkId.setSelected(docs.isValidBarangayOrSchoolId());

        btnVerifyApprove.setEnabled(true);
        btnReject.setEnabled(true);
    }

    private void verifyCurrentApplicant() {
        if (currentSelectedApplicant == null) return;

        DocumentChecklist updatedDocs = new DocumentChecklist(
                chkIndigency.isSelected(),
                chkGrades.isSelected(),
                chkEnrollment.isSelected(),
                chkId.isSelected()
        );

        boolean success = manager.verifyApplicantDocuments(currentSelectedApplicant.getApplicantId(), updatedDocs);
        if (success) {
            if (updatedDocs.isComplete()) {
                JOptionPane.showMessageDialog(this,
                        currentSelectedApplicant.getFullName() + " has passed all document checks and is now VERIFIED!\nApplicant is ready for Slot Ranking.",
                        "Document Verification Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Checklist updated, but some documents are still missing (" + updatedDocs.getStatusSummary() + ").",
                        "Checklist Updated", JOptionPane.WARNING_MESSAGE);
            }
            refreshData();
        }
    }

    private void rejectCurrentApplicant() {
        if (currentSelectedApplicant == null) return;
        int opt = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to disqualify " + currentSelectedApplicant.getFullName() + "?",
                "Confirm Rejection", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            currentSelectedApplicant.setStatus(ApplicationStatus.REJECTED);
            manager.getScreeningQueue().remove(currentSelectedApplicant);
            manager.notifyChange();
            refreshData();
        }
    }

    public void refreshData() {
        ScholarshipQueue<Applicant> queue = manager.getScreeningQueue();
        cardQueueSize.setValue(queue.size() + " Applicants");

        Applicant front = queue.peek();
        if (front != null) {
            cardFrontApplicant.setValue(front.getFullName());
            cardFrontApplicant.setSubtitle("ID: " + front.getApplicantId() + " (" + front.getPurok() + ")");
        } else {
            cardFrontApplicant.setValue("Queue Empty");
            cardFrontApplicant.setSubtitle("No pending screenings");
        }

        queueTableModel.setRowCount(0);
        List<Applicant> list = queue.toList();
        int pos = 1;
        for (Applicant a : list) {
            queueTableModel.addRow(new Object[]{
                    "#" + pos++,
                    a.getApplicantId(),
                    a.getFullName(),
                    a.getSchool(),
                    a.getPurok(),
                    a.getDocuments().getStatusSummary(),
                    a.getStatus()
            });
        }

        if (currentSelectedApplicant == null && front != null) {
            selectApplicant(front);
        } else if (currentSelectedApplicant != null) {
            selectApplicant(manager.findApplicantById(currentSelectedApplicant.getApplicantId()));
        }
    }
}
