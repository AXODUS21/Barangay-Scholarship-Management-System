package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.datastructures.ScholarshipQueue;
import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.ApplicationStatus;
import com.barangay.scholarship.model.DocumentChecklist;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.ui.components.ModernTable;
import com.barangay.scholarship.ui.components.StatCard;
import com.barangay.scholarship.ui.components.StatusBadgeRenderer;
import com.barangay.scholarship.util.FileUploadManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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

    // Search field
    private JTextField txtSearch;

    // Table of queued applicants
    private DefaultTableModel queueTableModel;
    private ModernTable queueTable;

    // Document Inspector Panel
    private JLabel lblSelectedName;
    private JLabel lblSelectedDetails;
    private JLabel lblStatusBadge;
    private JButton btnClosePreview;
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
    private JButton btnNext;

    private Applicant currentSelectedApplicant;
    private boolean userExplicitlyDeselected = false;

    public ScreeningQueueView(ScholarshipManager manager) {
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

        JLabel title = new JLabel("Document Verification & Screening Queue");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Custom FIFO Queue: Applicants are screened in First-Come, First-Served order");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);
        topPanel.add(headerPanel, BorderLayout.NORTH);

        // Queue Stats: 2 cards spanning 50% each to fill 100% of the horizontal space with no remaining gap
        JPanel statsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        statsGrid.setOpaque(false);

        cardQueueSize = new StatCard("Screening Queue Length", "0 Applicants", "First-In, First-Out (FIFO)", Theme.INFO);
        cardFrontApplicant = new StatCard("Front of Line (Next to Process)", "None", "Will be evaluated first", Theme.PRIMARY);

        statsGrid.add(cardQueueSize);
        statsGrid.add(cardFrontApplicant);
        topPanel.add(statsGrid, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Queue Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setMinimumSize(new Dimension(320, 260));
        JLabel tblTitle = new JLabel("Live Screening Line (Front to Rear)");
        tblTitle.setFont(Theme.FONT_HEADER_SM);
        tblTitle.setForeground(Theme.TEXT_TITLE);

        String[] cols = {"Position", "ID", "Name", "School", "Purok", "Docs", "Status"};
        queueTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Class<?> getColumnClass(int c) {
                if (c == 6) return ApplicationStatus.class;
                return Object.class;
            }
        };
        queueTable = new ModernTable(queueTableModel);
        queueTable.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());
        queueTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = queueTable.getSelectedRow();
                if (row >= 0 && row < queueTableModel.getRowCount()) {
                    userExplicitlyDeselected = false;
                    String id = (String) queueTableModel.getValueAt(row, 1);
                    selectApplicant(manager.findApplicantById(id));
                }
            }
        });

        JScrollPane scrollTable = ModernTable.createScrollPane(queueTable);
        tableCard.add(tblTitle, BorderLayout.NORTH);
        tableCard.add(scrollTable, BorderLayout.CENTER);

        // Document Inspector Card
        JPanel inspectorCard = Theme.createCardPanel();
        inspectorCard.setLayout(new BorderLayout(0, 10));

        JPanel insHeader = new JPanel();
        insHeader.setOpaque(false);
        insHeader.setLayout(new BoxLayout(insHeader, BoxLayout.Y_AXIS));

        JPanel titleBadgeRow = new JPanel(new BorderLayout(10, 0));
        titleBadgeRow.setOpaque(false);
        titleBadgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel insTitle = new JLabel("PHYSICAL DOCUMENT SCREENING DESK");
        insTitle.setFont(Theme.FONT_SMALL_BOLD);
        insTitle.setForeground(Theme.TEXT_MUTED);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topActions.setOpaque(false);

        lblStatusBadge = new JLabel("NO SELECTION");
        lblStatusBadge.setFont(Theme.FONT_SMALL_BOLD);
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setVisible(false);
        topActions.add(lblStatusBadge);

        btnClosePreview = new JButton("Close Selection");
        btnClosePreview.setFont(Theme.FONT_SMALL_BOLD);
        btnClosePreview.setForeground(Theme.TEXT_MUTED);
        btnClosePreview.setFocusPainted(false);
        btnClosePreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClosePreview.setVisible(false);
        btnClosePreview.addActionListener(e -> {
            userExplicitlyDeselected = true;
            selectApplicant(null);
            queueTable.clearSelection();
        });
        topActions.add(btnClosePreview);

        titleBadgeRow.add(insTitle, BorderLayout.WEST);
        titleBadgeRow.add(topActions, BorderLayout.EAST);

        lblSelectedName = new JLabel("Select an applicant from queue");
        lblSelectedName.setFont(Theme.FONT_HEADER_MD);
        lblSelectedName.setForeground(Theme.TEXT_TITLE);
        lblSelectedName.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblSelectedDetails = new JLabel("Click any row or peek at the front of line to inspect requirements.");
        lblSelectedDetails.setFont(Theme.FONT_BODY);
        lblSelectedDetails.setForeground(Theme.TEXT_MUTED);
        lblSelectedDetails.setAlignmentX(Component.LEFT_ALIGNMENT);

        insHeader.add(titleBadgeRow);
        insHeader.add(Box.createVerticalStrut(6));
        insHeader.add(lblSelectedName);
        insHeader.add(Box.createVerticalStrut(4));
        insHeader.add(lblSelectedDetails);
        insHeader.add(Box.createVerticalStrut(10));

        inspectorCard.add(insHeader, BorderLayout.NORTH);

        // Checkbox panel (2x2 grid for full-width horizontal inspection)
        JPanel chkPanel = new JPanel(new GridLayout(2, 2, 16, 8));
        chkPanel.setOpaque(false);

        chkIndigency = new JCheckBox("1. Certificate of Indigency (Barangay)");
        chkGrades = new JCheckBox("2. Certified Copy of Grades (Form 138 / TOR)");
        chkEnrollment = new JCheckBox("3. Certificate of Registration / Enrollment Form");
        chkId = new JCheckBox("4. Valid Barangay ID or Student ID");

        // React to live checkbox changes immediately
        chkIndigency.addActionListener(e -> updateScreeningActionsState());
        chkGrades.addActionListener(e -> updateScreeningActionsState());
        chkEnrollment.addActionListener(e -> updateScreeningActionsState());
        chkId.addActionListener(e -> updateScreeningActionsState());

        btnViewIndigency = new JButton("View Document");
        btnViewGrades = new JButton("View Document");
        btnViewEnrollment = new JButton("View Document");
        btnViewId = new JButton("View Document");

        lblIndigencyInfo = new JLabel("No applicant selected");
        lblGradesInfo = new JLabel("No applicant selected");
        lblEnrollmentInfo = new JLabel("No applicant selected");
        lblIdInfo = new JLabel("No applicant selected");

        btnAttachIndigency = new JButton("Attach File");
        btnAttachGrades = new JButton("Attach File");
        btnAttachEnrollment = new JButton("Attach File");
        btnAttachId = new JButton("Attach File");

        btnAttachIndigency.addActionListener(e -> staffUploadDocument("Indigency", (f, path) -> {
            if (currentSelectedApplicant != null) {
                currentSelectedApplicant.getDocuments().setIndigencyFilePath(path);
                chkIndigency.setSelected(true);
                updateDocAttachmentRow(true, f.getName(), path, btnViewIndigency, lblIndigencyInfo);
                updateScreeningActionsState();
            }
        }));

        btnAttachGrades.addActionListener(e -> staffUploadDocument("Grades", (f, path) -> {
            if (currentSelectedApplicant != null) {
                currentSelectedApplicant.getDocuments().setGradesFilePath(path);
                chkGrades.setSelected(true);
                updateDocAttachmentRow(true, f.getName(), path, btnViewGrades, lblGradesInfo);
                updateScreeningActionsState();
            }
        }));

        btnAttachEnrollment.addActionListener(e -> staffUploadDocument("Enrollment", (f, path) -> {
            if (currentSelectedApplicant != null) {
                currentSelectedApplicant.getDocuments().setEnrollmentFilePath(path);
                chkEnrollment.setSelected(true);
                updateDocAttachmentRow(true, f.getName(), path, btnViewEnrollment, lblEnrollmentInfo);
                updateScreeningActionsState();
            }
        }));

        btnAttachId.addActionListener(e -> staffUploadDocument("ID", (f, path) -> {
            if (currentSelectedApplicant != null) {
                currentSelectedApplicant.getDocuments().setIdFilePath(path);
                chkId.setSelected(true);
                updateDocAttachmentRow(true, f.getName(), path, btnViewId, lblIdInfo);
                updateScreeningActionsState();
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
        chkPanel.add(createStaffDocRow(chkGrades, btnViewGrades, lblGradesInfo, btnAttachGrades));
        chkPanel.add(createStaffDocRow(chkEnrollment, btnViewEnrollment, lblEnrollmentInfo, btnAttachEnrollment));
        chkPanel.add(createStaffDocRow(chkId, btnViewId, lblIdInfo, btnAttachId));

        inspectorCard.add(chkPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        btnReject = new JButton("Mark Rejected");
        btnReject.setFont(Theme.FONT_BODY_BOLD);
        btnReject.setForeground(Theme.DANGER);
        btnReject.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReject.addActionListener(e -> rejectCurrentApplicant());

        btnNext = new JButton("Next in Line");
        btnNext.setFont(Theme.FONT_BODY_BOLD);
        btnNext.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNext.addActionListener(e -> selectNextApplicantInQueue());

        btnVerifyApprove = Theme.createStyledButton("Save & Mark Verified", Theme.ACCENT, Color.WHITE);
        btnVerifyApprove.addActionListener(e -> verifyCurrentApplicant());

        btnPanel.add(btnReject);
        btnPanel.add(btnNext);
        btnPanel.add(btnVerifyApprove);

        inspectorCard.add(btnPanel, BorderLayout.SOUTH);

        // Toolbar Card: Full-Width Responsive Search Bar for Queue
        JPanel toolbarCard = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(32767, getPreferredSize().height);
            }
        };
        toolbarCard.setBackground(Theme.CARD_BG);
        toolbarCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel searchBar = new JPanel(new BorderLayout(14, 0));
        searchBar.setOpaque(false);

        JLabel lblSearch = new JLabel("Search Queue:");
        lblSearch.setFont(Theme.FONT_BODY_BOLD);
        lblSearch.setForeground(Theme.TEXT_TITLE);

        txtSearch = Theme.createRoundedTextField("Search queue by ID, name, school, purok, or verification status...");
        txtSearch.setPreferredSize(new Dimension(300, 38));
        txtSearch.setMaximumSize(new Dimension(32767, 38));
        txtSearch.setToolTipText("Type to filter queue live in real-time");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshData(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshData(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshData(); }
        });
        txtSearch.addActionListener(e -> refreshData());

        JButton btnSearch = Theme.createStyledButton("Search", Theme.PRIMARY, Color.WHITE);
        btnSearch.setFont(Theme.FONT_BODY_BOLD);
        btnSearch.setPreferredSize(new Dimension(100, 38));
        btnSearch.addActionListener(e -> refreshData());

        searchBar.add(lblSearch, BorderLayout.WEST);
        searchBar.add(txtSearch, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);
        toolbarCard.add(searchBar, BorderLayout.CENTER);

        // Lower area: Search bar on top of Table Card
        JPanel tableContainer = new JPanel(new BorderLayout(0, 14));
        tableContainer.setOpaque(false);
        tableContainer.add(toolbarCard, BorderLayout.NORTH);
        tableContainer.add(tableCard, BorderLayout.CENTER);

        // Center Panel: Stacked vertically with BorderLayout to give full width to both
        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setOpaque(false);
        centerPanel.add(inspectorCard, BorderLayout.NORTH);
        centerPanel.add(tableContainer, BorderLayout.CENTER);

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
            btnView.setText("View (" + fileName + ")");
            btnView.setToolTipText("Open file: " + filePath);
            lblInfo.setText("Digital File Archived");
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
            if (btnClosePreview != null) btnClosePreview.setVisible(false);
            lblSelectedName.setText("Select an applicant from queue");
            lblSelectedDetails.setText("Click any row or peek at the front of line to inspect requirements.");
            lblStatusBadge.setVisible(false);

            chkIndigency.setSelected(false);
            chkGrades.setSelected(false);
            chkEnrollment.setSelected(false);
            chkId.setSelected(false);

            chkIndigency.setEnabled(false);
            chkGrades.setEnabled(false);
            chkEnrollment.setEnabled(false);
            chkId.setEnabled(false);

            btnVerifyApprove.setEnabled(false);
            btnReject.setEnabled(false);
            btnNext.setEnabled(false);

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

        if (btnClosePreview != null) btnClosePreview.setVisible(true);
        lblSelectedName.setText(applicant.getFullName() + " (" + applicant.getApplicantId() + ")");
        lblSelectedDetails.setText(String.format("%s | %s | GWA: %s | Income: %s",
                applicant.getSchool(), applicant.getPurok(), applicant.getFormattedGwa(), applicant.getFormattedIncome()));

        DocumentChecklist docs = applicant.getDocuments();
        chkIndigency.setEnabled(true);
        chkGrades.setEnabled(true);
        chkEnrollment.setEnabled(true);
        chkId.setEnabled(true);
        btnNext.setEnabled(true);

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

        // Update status badge pill
        ApplicationStatus status = applicant.getStatus();
        lblStatusBadge.setVisible(true);
        if (status == ApplicationStatus.VERIFIED) {
            lblStatusBadge.setText("  VERIFIED — READY FOR RANKING  ");
            lblStatusBadge.setBackground(Theme.ACCENT_BG);
            lblStatusBadge.setForeground(new Color(6, 95, 70));
            lblStatusBadge.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(Theme.ACCENT, 1),
                    new EmptyBorder(3, 8, 3, 8)
            ));
        } else if (status == ApplicationStatus.PENDING_VERIFICATION) {
            lblStatusBadge.setText("  PENDING VERIFICATION (" + docs.getStatusSummary() + ")  ");
            lblStatusBadge.setBackground(Theme.WARNING_BG);
            lblStatusBadge.setForeground(Theme.WARNING);
            lblStatusBadge.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(Theme.WARNING, 1),
                    new EmptyBorder(3, 8, 3, 8)
            ));
        } else if (status == ApplicationStatus.REJECTED) {
            lblStatusBadge.setText("  REJECTED / DISQUALIFIED  ");
            lblStatusBadge.setBackground(Theme.DANGER_BG);
            lblStatusBadge.setForeground(Theme.DANGER);
            lblStatusBadge.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(Theme.DANGER, 1),
                    new EmptyBorder(3, 8, 3, 8)
            ));
        } else {
            lblStatusBadge.setText("  " + status.name() + "  ");
            lblStatusBadge.setBackground(Theme.BG_APP);
            lblStatusBadge.setForeground(Theme.TEXT_BODY);
            lblStatusBadge.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER, 1),
                    new EmptyBorder(3, 8, 3, 8)
            ));
        }

        updateScreeningActionsState();
    }

    private void updateScreeningActionsState() {
        if (currentSelectedApplicant == null) {
            btnVerifyApprove.setEnabled(false);
            btnVerifyApprove.setText("Save & Mark Verified");
            btnVerifyApprove.setBackground(Theme.ACCENT);
            btnReject.setEnabled(false);
            return;
        }

        boolean allChecked = chkIndigency.isSelected() && chkGrades.isSelected()
                && chkEnrollment.isSelected() && chkId.isSelected();
        int countChecked = (chkIndigency.isSelected() ? 1 : 0)
                + (chkGrades.isSelected() ? 1 : 0)
                + (chkEnrollment.isSelected() ? 1 : 0)
                + (chkId.isSelected() ? 1 : 0);

        ApplicationStatus currentStatus = currentSelectedApplicant.getStatus();

        if (currentStatus == ApplicationStatus.VERIFIED) {
            if (allChecked) {
                btnVerifyApprove.setText("Verified (Ready for Ranking)");
                btnVerifyApprove.setEnabled(false);
                btnVerifyApprove.setBackground(Theme.ACCENT);
                btnVerifyApprove.setForeground(Color.WHITE);
            } else {
                // User unchecked a document -> allow updating to pending
                btnVerifyApprove.setText("Update Checklist (Mark Pending)");
                btnVerifyApprove.setEnabled(true);
                btnVerifyApprove.setBackground(Theme.WARNING);
                btnVerifyApprove.setForeground(Color.WHITE);
            }
            btnReject.setEnabled(true);
            btnReject.setText("Mark Rejected");
        } else if (currentStatus == ApplicationStatus.PENDING_VERIFICATION) {
            if (allChecked) {
                btnVerifyApprove.setText("Save & Mark Verified");
                btnVerifyApprove.setEnabled(true);
                btnVerifyApprove.setBackground(Theme.ACCENT);
                btnVerifyApprove.setForeground(Color.WHITE);
            } else {
                btnVerifyApprove.setText("Save Checklist (" + countChecked + "/4)");
                btnVerifyApprove.setEnabled(true);
                btnVerifyApprove.setBackground(Theme.PRIMARY);
                btnVerifyApprove.setForeground(Color.WHITE);
            }
            btnReject.setEnabled(true);
            btnReject.setText("Mark Rejected");
        } else if (currentStatus == ApplicationStatus.REJECTED) {
            btnVerifyApprove.setText("Reinstate & Mark Verified");
            btnVerifyApprove.setEnabled(allChecked);
            btnVerifyApprove.setBackground(Theme.ACCENT);
            btnVerifyApprove.setForeground(Color.WHITE);
            btnReject.setEnabled(false);
        } else {
            btnVerifyApprove.setText(currentStatus.toString());
            btnVerifyApprove.setEnabled(false);
            btnVerifyApprove.setBackground(new Color(226, 232, 240));
            btnVerifyApprove.setForeground(new Color(100, 116, 139));
            btnReject.setEnabled(true);
        }
    }

    private void selectNextApplicantInQueue() {
        ScholarshipQueue<Applicant> queue = manager.getScreeningQueue();
        List<Applicant> list = queue.toList();
        if (list.isEmpty()) return;
        if (currentSelectedApplicant == null) {
            selectApplicant(list.get(0));
            if (queueTable.getRowCount() > 0) {
                queueTable.setRowSelectionInterval(0, 0);
            }
            return;
        }

        int currentIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getApplicantId().equals(currentSelectedApplicant.getApplicantId())) {
                currentIndex = i;
                break;
            }
        }

        int nextIndex = (currentIndex + 1) % list.size();
        Applicant nextApp = list.get(nextIndex);
        selectApplicant(nextApp);
        if (nextIndex < queueTable.getRowCount()) {
            queueTable.setRowSelectionInterval(nextIndex, nextIndex);
            queueTable.scrollRectToVisible(queueTable.getCellRect(nextIndex, 0, true));
        }
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
                        currentSelectedApplicant.getFullName() + " has passed all document checks and is now VERIFIED!\nApplicant is recorded and ready for Slot Ranking.",
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
            currentSelectedApplicant = null;
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
        List<Applicant> fullList = queue.toList();
        String q = (txtSearch != null) ? txtSearch.getText().trim().toLowerCase() : "";
        List<Applicant> list;
        if (!q.isEmpty()) {
            list = new java.util.ArrayList<>();
            for (Applicant a : fullList) {
                if (a.getApplicantId().toLowerCase().contains(q)
                        || a.getFullName().toLowerCase().contains(q)
                        || a.getSchool().toLowerCase().contains(q)
                        || a.getPurok().toLowerCase().contains(q)
                        || a.getStatus().getDisplayName().toLowerCase().contains(q)
                        || a.getStatus().name().toLowerCase().contains(q)
                        || a.getDocuments().getStatusSummary().toLowerCase().contains(q)) {
                    list.add(a);
                }
            }
        } else {
            list = fullList;
        }

        int selectedRow = -1;
        for (int i = 0; i < list.size(); i++) {
            Applicant a = list.get(i);
            if (currentSelectedApplicant != null && a.getApplicantId().equals(currentSelectedApplicant.getApplicantId())) {
                selectedRow = i;
            }
            int queuePos = fullList.indexOf(a) + 1;
            queueTableModel.addRow(new Object[]{
                    "#" + queuePos,
                    a.getApplicantId(),
                    a.getFullName(),
                    a.getSchool(),
                    a.getPurok(),
                    a.getDocuments().getStatusSummary(),
                    a.getStatus()
            });
        }

        if (selectedRow >= 0 && selectedRow < queueTable.getRowCount()) {
            queueTable.setRowSelectionInterval(selectedRow, selectedRow);
        }

        if (currentSelectedApplicant == null && front != null && !userExplicitlyDeselected && q.isEmpty()) {
            selectApplicant(front);
            if (queueTable.getRowCount() > 0) {
                queueTable.setRowSelectionInterval(0, 0);
            }
        } else if (currentSelectedApplicant != null) {
            Applicant reloaded = manager.findApplicantById(currentSelectedApplicant.getApplicantId());
            if (reloaded != null && (q.isEmpty() || list.contains(reloaded))) {
                selectApplicant(reloaded);
            } else if (!q.isEmpty()) {
                if (!list.isEmpty()) {
                    selectApplicant(list.get(0));
                    queueTable.setRowSelectionInterval(0, 0);
                } else {
                    selectApplicant(null);
                    queueTable.clearSelection();
                }
            }
        }
    }
}
