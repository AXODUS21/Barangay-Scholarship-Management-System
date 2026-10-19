package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.DocumentChecklist;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.ui.components.ModernTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

    // Document Checkboxes
    private JCheckBox chkIndigency;
    private JCheckBox chkGrades;
    private JCheckBox chkEnrollment;
    private JCheckBox chkId;

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
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        centerGrid.setOpaque(false);

        // LEFT: Registration Form Card
        JPanel formCard = Theme.createCardPanel();
        formCard.setLayout(new BorderLayout());

        JPanel formScrollContainer = new JPanel();
        formScrollContainer.setLayout(new BoxLayout(formScrollContainer, BoxLayout.Y_AXIS));
        formScrollContainer.setOpaque(false);

        // 1. Student Personal & Academic Details
        JLabel sec1 = new JLabel("STUDENT PROFILE");
        sec1.setFont(Theme.FONT_SMALL_BOLD);
        sec1.setForeground(Theme.TEXT_MUTED);
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
        dualRow.add(createField("GWA (1.00-5.00 or %)", txtGwa));
        dualRow.add(createField("Monthly Income (₱)", txtIncome));
        formScrollContainer.add(dualRow);
        formScrollContainer.add(Box.createVerticalStrut(12));

        // 2. Real-Time Eligibility Banner
        eligibilityBanner = new JPanel(new BorderLayout());
        eligibilityBanner.setBorder(new EmptyBorder(10, 14, 10, 14));
        eligibilityBanner.setBackground(Theme.INFO_BG);

        lblEligibilityText = new JLabel("Enter GWA and Monthly Income to auto-evaluate eligibility.");
        lblEligibilityText.setFont(Theme.FONT_BODY_BOLD);
        lblEligibilityText.setForeground(Theme.INFO);
        eligibilityBanner.add(lblEligibilityText, BorderLayout.CENTER);

        formScrollContainer.add(eligibilityBanner);
        formScrollContainer.add(Box.createVerticalStrut(16));

        // 3. Document Checklist
        JLabel sec2 = new JLabel("PHYSICAL DOCUMENT REQUIREMENTS CHECKLIST");
        sec2.setFont(Theme.FONT_SMALL_BOLD);
        sec2.setForeground(Theme.TEXT_MUTED);
        formScrollContainer.add(sec2);
        formScrollContainer.add(Box.createVerticalStrut(8));

        chkIndigency = new JCheckBox("Certificate of Indigency (Issued by Barangay)");
        chkGrades = new JCheckBox("Certified True Copy of Grades / Transcript of Records");
        chkEnrollment = new JCheckBox("Certificate of Registration / Official Enrollment Form");
        chkId = new JCheckBox("Valid Barangay ID or Valid Student ID");

        formScrollContainer.add(chkIndigency);
        formScrollContainer.add(chkGrades);
        formScrollContainer.add(chkEnrollment);
        formScrollContainer.add(chkId);
        formScrollContainer.add(Box.createVerticalStrut(16));

        // Submit & Clear Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);

        JButton btnClear = new JButton("Clear Form");
        btnClear.addActionListener(e -> clearForm());

        JButton btnSubmit = Theme.createStyledButton("Submit Registration", Theme.PRIMARY, Color.WHITE);
        btnSubmit.addActionListener(e -> submitForm());

        actionRow.add(btnClear);
        actionRow.add(btnSubmit);
        formScrollContainer.add(actionRow);

        JScrollPane formScroll = new JScrollPane(formScrollContainer);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(Color.WHITE);
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
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
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

        Applicant applicant = manager.registerApplicant(name, email, phone, school, year, gwa, income, purok, docs);

        String msg = String.format("Applicant %s registered successfully!\nID: %s\nStatus: %s\nScreening Line: Queued at position #%d",
                applicant.getFullName(),
                applicant.getApplicantId(),
                applicant.getStatus().getDisplayName(),
                manager.getScreeningQueue().size());

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
        updateEligibilityPreview();
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
