package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.DocumentChecklist;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.ui.components.ModernTable;
import com.barangay.scholarship.util.FileUploadManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction Stage 1: Student Registration & Profiling with Real-time Eligibility Auto-Checker.
 */
public class RegistrationView extends JPanel {
    private final ScholarshipManager manager;

    // Form Fields
    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtSchool;
    private JComboBox<String> cmbYear;
    private JTextField txtGwa;
    private JTextField txtIncome;
    private JComboBox<String> cmbPurok;

    // Document Checkboxes & Attachments
    private JCheckBox chkIndigency;
    private JCheckBox chkGrades;
    private JCheckBox chkEnrollment;
    private JCheckBox chkId;

    private File fileIndigency;
    private File fileGrades;
    private File fileEnrollment;
    private File fileId;
    private final List<Runnable> fileResetCallbacks = new ArrayList<>();

    // Eligibility Live Feedback Panel
    private JPanel eligibilityBanner;
    private JLabel lblEligibilityText;

    // Recent Registrations Table
    private DefaultTableModel regTableModel;
    private ModernTable regTable;

    public RegistrationView(ScholarshipManager manager) {
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

        JLabel title = new JLabel("Staff Applicant Intake & Master Registry");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Administrative walk-in student encoding and master records. (Students self-register via the dedicated Student Portal)");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Center: Form on Left, Live Table on Right
        // LEFT: Registration Form Card
        JPanel formCard = Theme.createCardPanel();
        formCard.setLayout(new BorderLayout());

        Theme.ScrollablePanel formScrollContainer = new Theme.ScrollablePanel();
        formScrollContainer.setLayout(new BoxLayout(formScrollContainer, BoxLayout.Y_AXIS));
        formScrollContainer.setOpaque(false);

        // 1. Student Personal & Academic Details
        JLabel sec1 = new JLabel("STUDENT PROFILE");
        sec1.setFont(Theme.FONT_SMALL_BOLD);
        sec1.setForeground(Theme.TEXT_MUTED);
        sec1.setAlignmentX(Component.LEFT_ALIGNMENT);
        formScrollContainer.add(sec1);
        formScrollContainer.add(Box.createVerticalStrut(8));

        txtName = new JTextField();
        txtEmail = new JTextField();
        txtPhone = new JTextField();
        txtSchool = new JTextField();

        String[] years = {"1st Year College", "2nd Year College", "3rd Year College", "4th Year College", "Grade 11", "Grade 12"};
        cmbYear = new JComboBox<>(years);

        String[] puroks = {"Purok 1 Maligaya", "Purok 2 Pag-asa", "Purok 3 Riverside", "Purok 4 San Jose", "Purok 5 Kawayan", "Purok 6 Ilaya"};
        cmbPurok = new JComboBox<>(puroks);

        txtGwa = new JTextField();
        txtIncome = new JTextField();

        // Listeners for live auto-eligibility evaluation
        DocumentListener liveListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateEligibilityPreview(); }
            public void removeUpdate(DocumentEvent e) { updateEligibilityPreview(); }
            public void changedUpdate(DocumentEvent e) { updateEligibilityPreview(); }
        };
        txtGwa.getDocument().addDocumentListener(liveListener);
        txtIncome.getDocument().addDocumentListener(liveListener);

        formScrollContainer.add(createField("Full Name (Last, First M.I.)", txtName));
        formScrollContainer.add(createField("Email Address", txtEmail));
        formScrollContainer.add(createField("Contact Number (Mobile)", txtPhone));
        formScrollContainer.add(createField("School / University", txtSchool));
        formScrollContainer.add(createField("Year Level", cmbYear));
        formScrollContainer.add(createField("Barangay Zone / Purok", cmbPurok));

        JPanel dualRow = new JPanel(new GridLayout(1, 2, 12, 0));
        dualRow.setOpaque(false);
        dualRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        dualRow.add(createField("GWA (1.00-5.00 or %)", txtGwa));
        dualRow.add(createField("Monthly Income (₱)", txtIncome));
        dualRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, dualRow.getPreferredSize().height));
        formScrollContainer.add(dualRow);
        formScrollContainer.add(Box.createVerticalStrut(12));

        // 2. Real-Time Eligibility Banner
        eligibilityBanner = new JPanel(new BorderLayout());
        eligibilityBanner.setBorder(new EmptyBorder(10, 14, 10, 14));
        eligibilityBanner.setBackground(Theme.INFO_BG);
        eligibilityBanner.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblEligibilityText = new JLabel("Enter GWA and Monthly Income to auto-evaluate eligibility.");
        lblEligibilityText.setFont(Theme.FONT_BODY_BOLD);
        lblEligibilityText.setForeground(Theme.INFO);
        lblEligibilityText.setAlignmentX(Component.LEFT_ALIGNMENT);
        eligibilityBanner.add(lblEligibilityText, BorderLayout.CENTER);
        eligibilityBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, eligibilityBanner.getPreferredSize().height));

        formScrollContainer.add(eligibilityBanner);
        formScrollContainer.add(Box.createVerticalStrut(16));

        // 3. Document Checklist & File Attachments
        JLabel sec2 = new JLabel("DOCUMENT REQUIREMENTS & DIGITAL ATTACHMENTS");
        sec2.setFont(Theme.FONT_SMALL_BOLD);
        sec2.setForeground(Theme.TEXT_MUTED);
        sec2.setAlignmentX(Component.LEFT_ALIGNMENT);
        formScrollContainer.add(sec2);
        formScrollContainer.add(Box.createVerticalStrut(8));

        chkIndigency = new JCheckBox();
        chkGrades = new JCheckBox();
        chkEnrollment = new JCheckBox();
        chkId = new JCheckBox();

        formScrollContainer.add(createDocUploadRow("Certificate of Indigency (Barangay)", chkIndigency, "Indigency", f -> this.fileIndigency = f));
        formScrollContainer.add(Box.createVerticalStrut(6));
        formScrollContainer.add(createDocUploadRow("Certified Copy of Grades / TOR", chkGrades, "Grades", f -> this.fileGrades = f));
        formScrollContainer.add(Box.createVerticalStrut(6));
        formScrollContainer.add(createDocUploadRow("Certificate of Registration (COR)", chkEnrollment, "Enrollment", f -> this.fileEnrollment = f));
        formScrollContainer.add(Box.createVerticalStrut(6));
        formScrollContainer.add(createDocUploadRow("Valid Barangay ID or Student ID", chkId, "Valid ID", f -> this.fileId = f));
        formScrollContainer.add(Box.createVerticalStrut(16));

        // Submit & Clear Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnClear = new JButton("Clear Form");
        btnClear.addActionListener(e -> clearForm());

        JButton btnSubmit = Theme.createStyledButton("Submit Registration", Theme.PRIMARY, Color.WHITE);
        btnSubmit.addActionListener(e -> submitForm());

        actionRow.add(btnClear);
        actionRow.add(btnSubmit);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, actionRow.getPreferredSize().height));
        formScrollContainer.add(actionRow);

        JScrollPane formScroll = new JScrollPane(formScrollContainer);
        formScroll.setBorder(null);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getViewport().setBackground(Color.WHITE);
        Theme.enableSmoothMouseWheel(formScroll);
        formCard.add(formScroll, BorderLayout.CENTER);
        formCard.setMinimumSize(new Dimension(320, 320));

        // RIGHT: Recent Registrations Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setMinimumSize(new Dimension(340, 320));
        JLabel tableTitle = new JLabel("Master Applicant Registry");
        tableTitle.setFont(Theme.FONT_HEADER_SM);
        tableTitle.setForeground(Theme.TEXT_TITLE);

        String[] cols = {"ID", "Name", "Purok", "GWA", "Income", "Status"};
        regTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        regTable = new ModernTable(regTableModel);
        JScrollPane tblScroll = ModernTable.createScrollPane(regTable);

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(tblScroll, BorderLayout.CENTER);

        JSplitPane splitPane = Theme.createSplitPane(JSplitPane.HORIZONTAL_SPLIT, formCard, tableCard, 0.48);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createField(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.FONT_SMALL_BOLD);
        lbl.setForeground(Theme.TEXT_TITLE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl, BorderLayout.NORTH);

        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        p.add(field, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        p.setBorder(new EmptyBorder(0, 0, 8, 0));
        return p;
    }

    private void updateEligibilityPreview() {
        String gwaStr = txtGwa.getText().trim();
        String incStr = txtIncome.getText().trim();

        if (gwaStr.isEmpty() || incStr.isEmpty()) {
            eligibilityBanner.setBackground(Theme.INFO_BG);
            lblEligibilityText.setForeground(Theme.INFO);
            lblEligibilityText.setText("Enter GWA and Monthly Income to auto-evaluate eligibility.");
            return;
        }

        try {
            double gwa = Double.parseDouble(gwaStr);
            double income = Double.parseDouble(incStr);

            boolean isCollege = gwa <= 5.0;
            boolean gwaOk = isCollege ? (gwa <= 2.50 && gwa >= 1.0) : (gwa >= 85.0 && gwa <= 100.0);
            boolean incomeOk = income <= 25000.0;

            if (gwaOk && incomeOk) {
                eligibilityBanner.setBackground(Theme.ACCENT_BG);
                lblEligibilityText.setForeground(Theme.ACCENT_HOVER);
                lblEligibilityText.setText("AUTO-CHECK: ELIGIBLE (Meets grades & household income criteria)");
            } else {
                eligibilityBanner.setBackground(Theme.DANGER_BG);
                lblEligibilityText.setForeground(Theme.DANGER);
                StringBuilder sb = new StringBuilder("AUTO-CHECK: INELIGIBLE - ");
                if (!incomeOk) sb.append("Income exceeds ₱25k limit; ");
                if (!gwaOk) {
                    if (isCollege) sb.append("College GWA > 2.50; ");
                    else sb.append("HS Grade < 85.0%; ");
                }
                lblEligibilityText.setText(sb.toString().trim());
            }
        } catch (NumberFormatException ex) {
            eligibilityBanner.setBackground(Theme.WARNING_BG);
            lblEligibilityText.setForeground(Theme.WARNING);
            lblEligibilityText.setText("Please input valid numerical values for GWA and Income.");
        }
    }

    private void submitForm() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String school = txtSchool.getText().trim();
        String year = (String) cmbYear.getSelectedItem();
        String purok = (String) cmbPurok.getSelectedItem();

        if (name.isEmpty() || school.isEmpty() || txtGwa.getText().trim().isEmpty() || txtIncome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (Name, School, GWA, Income).", "Incomplete Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double gwa;
        double income;
        try {
            gwa = Double.parseDouble(txtGwa.getText().trim());
            income = Double.parseDouble(txtIncome.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "GWA and Income must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DocumentChecklist docs = new DocumentChecklist(
                chkIndigency.isSelected(),
                chkGrades.isSelected(),
                chkEnrollment.isSelected(),
                chkId.isSelected()
        );

        Applicant applicant = manager.registerApplicant(
                name, email, phone, school, year, gwa, income, purok, docs,
                fileIndigency, fileGrades, fileEnrollment, fileId
        );

        String msg = String.format("Applicant %s registered successfully!\nID: %s\nStatus: %s\nScreening Line: Queued at position #%d\nAttached Files: %d digital document(s)",
                applicant.getFullName(),
                applicant.getApplicantId(),
                applicant.getStatus().getDisplayName(),
                manager.getScreeningQueue().size(),
                applicant.getDocuments().getUploadedFilesCount());

        JOptionPane.showMessageDialog(this, msg, "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
        clearForm();
        refreshTable();
    }

    private void clearForm() {
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtSchool.setText("");
        txtGwa.setText("");
        txtIncome.setText("");
        chkIndigency.setSelected(false);
        chkGrades.setSelected(false);
        chkEnrollment.setSelected(false);
        chkId.setSelected(false);
        fileIndigency = null;
        fileGrades = null;
        fileEnrollment = null;
        fileId = null;
        for (Runnable r : fileResetCallbacks) {
            r.run();
        }
        updateEligibilityPreview();
    }

    private JPanel createDocUploadRow(String title, JCheckBox chk, String docType, java.util.function.Consumer<File> onFileSet) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        chk.setText(title);
        chk.setFont(Theme.FONT_BODY);
        chk.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(chk);

        JPanel sub = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        sub.setOpaque(false);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnUpload = new JButton("📎 Attach File");
        btnUpload.setFont(Theme.FONT_SMALL);
        btnUpload.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel("Optional digital copy");
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);

        JButton btnView = new JButton("👁 View");
        btnView.setFont(Theme.FONT_SMALL);
        btnView.setVisible(false);
        btnView.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnRemove = new JButton("✕");
        btnRemove.setFont(Theme.FONT_SMALL);
        btnRemove.setForeground(Theme.DANGER);
        btnRemove.setVisible(false);
        btnRemove.setCursor(new Cursor(Cursor.HAND_CURSOR));

        final File[] chosenRef = new File[1];

        btnUpload.addActionListener(e -> {
            JFileChooser chooser = FileUploadManager.createFileChooser("Attach " + docType);
            int res = chooser.showOpenDialog(RegistrationView.this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                String err = FileUploadManager.getValidationError(file);
                if (err != null) {
                    JOptionPane.showMessageDialog(RegistrationView.this, err, "Invalid File", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                chosenRef[0] = file;
                onFileSet.accept(file);
                chk.setSelected(true);
                lbl.setText("✓ " + file.getName());
                lbl.setForeground(Theme.ACCENT_HOVER);
                btnView.setVisible(true);
                btnRemove.setVisible(true);
            }
        });

        btnView.addActionListener(e -> {
            if (chosenRef[0] != null) {
                FileUploadManager.openFile(RegistrationView.this, chosenRef[0].getAbsolutePath());
            }
        });

        btnRemove.addActionListener(e -> {
            chosenRef[0] = null;
            onFileSet.accept(null);
            lbl.setText("Optional digital copy");
            lbl.setForeground(Theme.TEXT_MUTED);
            btnView.setVisible(false);
            btnRemove.setVisible(false);
        });

        fileResetCallbacks.add(() -> {
            chosenRef[0] = null;
            lbl.setText("Optional digital copy");
            lbl.setForeground(Theme.TEXT_MUTED);
            btnView.setVisible(false);
            btnRemove.setVisible(false);
        });

        sub.add(btnUpload);
        sub.add(lbl);
        sub.add(btnView);
        sub.add(btnRemove);
        p.add(sub);

        return p;
    }

    public void refreshTable() {
        regTableModel.setRowCount(0);
        List<Applicant> list = manager.getMasterList();
        for (Applicant a : list) {
            regTableModel.addRow(new Object[]{
                    a.getApplicantId(),
                    a.getFullName(),
                    a.getPurok(),
                    a.getFormattedGwa(),
                    a.getFormattedIncome(),
                    a.getStatus()
            });
        }
    }
}
