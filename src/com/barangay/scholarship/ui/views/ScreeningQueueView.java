package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.datastructures.ScholarshipQueue;
import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.ApplicationStatus;
import com.barangay.scholarship.model.DocumentChecklist;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.ui.components.ModernTable;
import com.barangay.scholarship.ui.components.StatCard;
import com.barangay.scholarship.util.FileUploadManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
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

    private JButton btnViewIndigency;
    private JButton btnViewGrades;
    private JButton btnViewEnrollment;
    private JButton btnViewId;

    private JLabel lblIndigencyInfo;
    private JLabel lblGradesInfo;
    private JLabel lblEnrollmentInfo;
    private JLabel lblIdInfo;

    private JButton btnAttachIndigency;
    private JButton btnAttachGrades;
    private JButton btnAttachEnrollment;
    private JButton btnAttachId;

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
        statsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, statsGrid.getPreferredSize().height));
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
        insTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblSelectedName = new JLabel("Select an applicant from queue");
        lblSelectedName.setFont(Theme.FONT_HEADER_MD);
        lblSelectedName.setForeground(Theme.TEXT_TITLE);
        lblSelectedName.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblSelectedDetails = new JLabel("Click any row or peek at the front of line to inspect requirements.");
        lblSelectedDetails.setFont(Theme.FONT_BODY);
        lblSelectedDetails.setForeground(Theme.TEXT_MUTED);
        lblSelectedDetails.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        chkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        chkIndigency = new JCheckBox("1. Certificate of Indigency (Barangay)");
        chkGrades = new JCheckBox("2. Certified Copy of Grades (Form 138 / TOR)");
        chkEnrollment = new JCheckBox("3. Certificate of Registration / Enrollment Form");
        chkId = new JCheckBox("4. Valid Barangay ID or Student ID");

        btnViewIndigency = new JButton("👁 View Document");
        btnViewGrades = new JButton("👁 View Document");
        btnViewEnrollment = new JButton("👁 View Document");
        btnViewId = new JButton("👁 View Document");

        lblIndigencyInfo = new JLabel("No applicant selected");
        lblGradesInfo = new JLabel("No applicant selected");
        lblEnrollmentInfo = new JLabel("No applicant selected");
        lblIdInfo = new JLabel("No applicant selected");

        btnAttachIndigency = new JButton("📎 Attach File");
        btnAttachGrades = new JButton("📎 Attach File");
        btnAttachEnrollment = new JButton("📎 Attach File");
        btnAttachId = new JButton("📎 Attach File");

        btnAttachIndigency.addActionListener(e -> staffUploadDocument("Indigency", (f, path) -> {
            if (currentSelectedApplicant != null) {
                currentSelectedApplicant.getDocuments().setIndigencyFilePath(path);
                chkIndigency.setSelected(true);
                updateDocAttachmentRow(true, f.getName(), path, btnViewIndigency, lblIndigencyInfo);
            }
        }));

        btnAttachGrades.addActionListener(e -> staffUploadDocument("Grades", (f, path) -> {
            if (currentSelectedApplicant != null) {
                currentSelectedApplicant.getDocuments().setGradesFilePath(path);
                chkGrades.setSelected(true);
                updateDocAttachmentRow(true, f.getName(), path, btnViewGrades, lblGradesInfo);
            }
        }));

        btnAttachEnrollment.addActionListener(e -> staffUploadDocument("Enrollment", (f, path) -> {
            if (currentSelectedApplicant != null) {
                currentSelectedApplicant.getDocuments().setEnrollmentFilePath(path);
                chkEnrollment.setSelected(true);
                updateDocAttachmentRow(true, f.getName(), path, btnViewEnrollment, lblEnrollmentInfo);
            }
        }));

        btnAttachId.addActionListener(e -> staffUploadDocument("ID", (f, path) -> {
            if (currentSelectedApplicant != null) {
                currentSelectedApplicant.getDocuments().setIdFilePath(path);
                chkId.setSelected(true);
                updateDocAttachmentRow(true, f.getName(), path, btnViewId, lblIdInfo);
            }
        }));

        btnViewIndigency.addActionListener(e -> {
            if (currentSelectedApplicant != null) {
                FileUploadManager.openFile(this, currentSelectedApplicant.getDocuments().getIndigencyFilePath());
            }
        });
        btnViewGrades.addActionListener(e -> {
            if (currentSelectedApplicant != null) {
                FileUploadManager.openFile(this, currentSelectedApplicant.getDocuments().getGradesFilePath());
            }
        });
        btnViewEnrollment.addActionListener(e -> {
            if (currentSelectedApplicant != null) {
                FileUploadManager.openFile(this, currentSelectedApplicant.getDocuments().getEnrollmentFilePath());
            }
        });
        btnViewId.addActionListener(e -> {
            if (currentSelectedApplicant != null) {
                FileUploadManager.openFile(this, currentSelectedApplicant.getDocuments().getIdFilePath());
            }
        });

        chkPanel.add(createStaffDocRow(chkIndigency, btnViewIndigency, lblIndigencyInfo, btnAttachIndigency));
        chkPanel.add(Box.createVerticalStrut(6));
        chkPanel.add(createStaffDocRow(chkGrades, btnViewGrades, lblGradesInfo, btnAttachGrades));
        chkPanel.add(Box.createVerticalStrut(6));
        chkPanel.add(createStaffDocRow(chkEnrollment, btnViewEnrollment, lblEnrollmentInfo, btnAttachEnrollment));
        chkPanel.add(Box.createVerticalStrut(6));
        chkPanel.add(createStaffDocRow(chkId, btnViewId, lblIdInfo, btnAttachId));
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
        workSplit.setAlignmentX(Component.LEFT_ALIGNMENT);
        workSplit.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        centerPanel.add(workSplit);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStaffDocRow(JCheckBox chk, JButton btnView, JLabel lblInfo, JButton btnAttach) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        chk.setFont(Theme.FONT_BODY_BOLD);
        chk.setForeground(Theme.TEXT_TITLE);
        chk.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(chk);
        p.add(Box.createVerticalStrut(2));

        JPanel subRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        subRow.setOpaque(false);
        subRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnView.setFont(Theme.FONT_SMALL_BOLD);
        btnView.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnView.setBackground(Theme.PRIMARY_LIGHT);
        btnView.setForeground(Theme.PRIMARY);
        btnView.setVisible(false);

        lblInfo.setFont(Theme.FONT_SMALL);
        lblInfo.setForeground(Theme.TEXT_MUTED);

        btnAttach.setFont(Theme.FONT_SMALL);
        btnAttach.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAttach.setEnabled(false);

        subRow.add(btnView);
        subRow.add(lblInfo);
        subRow.add(btnAttach);
        p.add(subRow);

        p.setBorder(new EmptyBorder(4, 6, 6, 6));
        return p;
    }

    private void updateDocAttachmentRow(boolean hasFile, String fileName, String filePath, JButton btnView, JLabel lblInfo) {
        if (hasFile) {
            btnView.setVisible(true);
            btnView.setText("👁 View (" + fileName + ")");
            btnView.setToolTipText("Open file: " + filePath);
            lblInfo.setText("✓ Digital File Archived");
            lblInfo.setForeground(Theme.ACCENT_HOVER);
        } else {
            btnView.setVisible(false);
            lblInfo.setText("No digital file (Physical inspection)");
            lblInfo.setForeground(Theme.TEXT_MUTED);
        }
    }

    private void staffUploadDocument(String docType, java.util.function.BiConsumer<File, String> onSaved) {
        if (currentSelectedApplicant == null) return;
        JFileChooser chooser = FileUploadManager.createFileChooser("Attach " + docType + " for " + currentSelectedApplicant.getFullName());
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String err = FileUploadManager.getValidationError(file);
            if (err != null) {
                JOptionPane.showMessageDialog(this, err, "Invalid File", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String savedPath = FileUploadManager.saveUploadedFile(currentSelectedApplicant.getApplicantId(), docType, file);
                onSaved.accept(file, savedPath);
                JOptionPane.showMessageDialog(this, "Document successfully attached and archived!",
                        "Upload Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(),
                        "Storage Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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

            btnViewIndigency.setVisible(false);
            btnViewGrades.setVisible(false);
            btnViewEnrollment.setVisible(false);
            btnViewId.setVisible(false);

            lblIndigencyInfo.setText("No applicant selected");
            lblGradesInfo.setText("No applicant selected");
            lblEnrollmentInfo.setText("No applicant selected");
            lblIdInfo.setText("No applicant selected");

            btnAttachIndigency.setEnabled(false);
            btnAttachGrades.setEnabled(false);
            btnAttachEnrollment.setEnabled(false);
            btnAttachId.setEnabled(false);
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

        btnAttachIndigency.setEnabled(true);
        btnAttachGrades.setEnabled(true);
        btnAttachEnrollment.setEnabled(true);
        btnAttachId.setEnabled(true);

        updateDocAttachmentRow(docs.hasIndigencyFile(), docs.getIndigencyFileName(), docs.getIndigencyFilePath(),
                btnViewIndigency, lblIndigencyInfo);
        updateDocAttachmentRow(docs.hasGradesFile(), docs.getGradesFileName(), docs.getGradesFilePath(),
                btnViewGrades, lblGradesInfo);
        updateDocAttachmentRow(docs.hasEnrollmentFile(), docs.getEnrollmentFileName(), docs.getEnrollmentFilePath(),
                btnViewEnrollment, lblEnrollmentInfo);
        updateDocAttachmentRow(docs.hasIdFile(), docs.getIdFileName(), docs.getIdFilePath(),
                btnViewId, lblIdInfo);

        btnVerifyApprove.setEnabled(true);
        btnReject.setEnabled(true);
    }

    private void verifyCurrentApplicant() {
        if (currentSelectedApplicant == null) return;

        DocumentChecklist existing = currentSelectedApplicant.getDocuments();
        DocumentChecklist updatedDocs = new DocumentChecklist(
                chkIndigency.isSelected(),
                chkGrades.isSelected(),
                chkEnrollment.isSelected(),
                chkId.isSelected(),
                existing != null ? existing.getIndigencyFilePath() : null,
                existing != null ? existing.getGradesFilePath() : null,
                existing != null ? existing.getEnrollmentFilePath() : null,
                existing != null ? existing.getIdFilePath() : null
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
