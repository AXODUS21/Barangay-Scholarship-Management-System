package com.barangay.scholarship.ui.student;

import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.DocumentChecklist;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Dedicated Student Application & Registration View.
 * Provides a clean, welcoming, distraction-free environment for resident students
 * to submit their scholarship applications without any exposure to administrative controls
 * or other applicants' sensitive records.
 */
public class StudentApplicationView extends JPanel {
    private final ScholarshipManager manager;
    private final Consumer<String> onNavigate;

    // Personal & Contact Fields
    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JComboBox<String> cmbPurok;

    // Academic & Financial Fields
    private JTextField txtSchool;
    private JComboBox<String> cmbYear;
    private JTextField txtGwa;
    private JTextField txtIncome;

    // Physical Document Checklist Checkboxes
    private JCheckBox chkIndigency;
    private JCheckBox chkGrades;
    private JCheckBox chkEnrollment;
    private JCheckBox chkId;

    // Data Privacy & Truthfulness
    private JCheckBox chkConsent;

    // Real-Time Eligibility Preview Banner
    private JPanel eligibilityBanner;
    private JLabel lblEligibilityText;

    public StudentApplicationView(ScholarshipManager manager, Consumer<String> onNavigate) {
        this.manager = manager;
        this.onNavigate = onNavigate;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(24, 32, 32, 32));

        initUI();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel tag = new JLabel("ONLINE REGISTRATION DESK");
        tag.setFont(Theme.FONT_SMALL_BOLD);
        tag.setForeground(Theme.PRIMARY);

        JLabel title = new JLabel("Barangay Scholarship & Educational Assistance Application");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Complete the form below to apply. An official Application Reference ID will be generated upon submission.");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(tag);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Main Scrollable Form Container
        JPanel mainFormContainer = new Theme.ScrollablePanel();
        mainFormContainer.setOpaque(false);
        mainFormContainer.setLayout(new BoxLayout(mainFormContainer, BoxLayout.Y_AXIS));

        // CARD 1: Personal & Contact Information
        JPanel cardPersonal = Theme.createFullWidthCardPanel();
        cardPersonal.setLayout(new BoxLayout(cardPersonal, BoxLayout.Y_AXIS));

        JLabel titleSec1 = new JLabel("1. PERSONAL & RESIDENCE INFORMATION");
        titleSec1.setFont(Theme.FONT_HEADER_SM);
        titleSec1.setForeground(Theme.PRIMARY);
        titleSec1.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPersonal.add(titleSec1);
        cardPersonal.add(Theme.createVerticalStrut(12));

        txtName = new JTextField();
        txtEmail = new JTextField();
        txtPhone = new JTextField();

        String[] puroks = {
                "Purok 1 Maligaya", "Purok 2 Pag-asa", "Purok 3 Riverside",
                "Purok 4 San Jose", "Purok 5 Kawayan", "Purok 6 Ilaya"
        };
        cmbPurok = new JComboBox<>(puroks);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row1.add(createField("Full Name (Last, First M.I.) *", txtName, "e.g., Dela Cruz, Juan M."));
        row1.add(createField("Barangay Zone / Purok *", cmbPurok, "Select your residential purok"));
        cardPersonal.add(row1);

        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row2.add(createField("Email Address *", txtEmail, "student@example.edu.ph"));
        row2.add(createField("Mobile Contact Number *", txtPhone, "09XXXXXXXXX"));
        cardPersonal.add(row2);

        mainFormContainer.add(cardPersonal);
        mainFormContainer.add(Theme.createVerticalStrut(16));

        // CARD 2: Academic Background & Financial Profile
        JPanel cardAcademic = Theme.createFullWidthCardPanel();
        cardAcademic.setLayout(new BoxLayout(cardAcademic, BoxLayout.Y_AXIS));

        JLabel titleSec2 = new JLabel("2. ACADEMIC & FINANCIAL PROFILE");
        titleSec2.setFont(Theme.FONT_HEADER_SM);
        titleSec2.setForeground(Theme.PRIMARY);
        titleSec2.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardAcademic.add(titleSec2);
        cardAcademic.add(Theme.createVerticalStrut(12));

        txtSchool = new JTextField();
        String[] years = {"1st Year College", "2nd Year College", "3rd Year College", "4th Year College", "Grade 11", "Grade 12"};
        cmbYear = new JComboBox<>(years);

        txtGwa = new JTextField();
        txtIncome = new JTextField();

        // Live eligibility listeners
        DocumentListener liveListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateEligibilityPreview(); }
            public void removeUpdate(DocumentEvent e) { updateEligibilityPreview(); }
            public void changedUpdate(DocumentEvent e) { updateEligibilityPreview(); }
        };
        txtGwa.getDocument().addDocumentListener(liveListener);
        txtIncome.getDocument().addDocumentListener(liveListener);

        JPanel row3 = new JPanel(new GridLayout(1, 2, 16, 0));
        row3.setOpaque(false);
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row3.add(createField("Current School / University / College *", txtSchool, "e.g., Polytechnic University of the Philippines"));
        row3.add(createField("Year Level *", cmbYear, "Select your current educational level"));
        cardAcademic.add(row3);

        JPanel row4 = new JPanel(new GridLayout(1, 2, 16, 0));
        row4.setOpaque(false);
        row4.setAlignmentX(Component.LEFT_ALIGNMENT);
        row4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row4.add(createField("General Weighted Average (GWA) *", txtGwa, "1.00 - 5.00 (College) or 75 - 100 (Senior High)"));
        row4.add(createField("Estimated Monthly Household Income (₱) *", txtIncome, "Combined monthly earnings of parents/guardians"));
        cardAcademic.add(row4);

        // Real-Time Eligibility Preview Box
        eligibilityBanner = new JPanel(new BorderLayout());
        eligibilityBanner.setBorder(new EmptyBorder(10, 14, 10, 14));
        eligibilityBanner.setBackground(Theme.INFO_BG);
        eligibilityBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        eligibilityBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        lblEligibilityText = new JLabel("ℹ️ Enter your GWA and Monthly Income above to view real-time eligibility evaluation.");
        lblEligibilityText.setFont(Theme.FONT_BODY_BOLD);
        lblEligibilityText.setForeground(Theme.INFO);
        lblEligibilityText.setAlignmentX(Component.LEFT_ALIGNMENT);
        eligibilityBanner.add(lblEligibilityText, BorderLayout.CENTER);

        cardAcademic.add(Theme.createVerticalStrut(6));
        cardAcademic.add(eligibilityBanner);

        mainFormContainer.add(cardAcademic);
        mainFormContainer.add(Theme.createVerticalStrut(16));

        // CARD 3: Physical Document Checklist & Data Consent
        JPanel cardDocs = Theme.createFullWidthCardPanel();
        cardDocs.setLayout(new BoxLayout(cardDocs, BoxLayout.Y_AXIS));

        JLabel titleSec3 = new JLabel("3. DOCUMENT REQUIREMENTS & DECLARATION");
        titleSec3.setFont(Theme.FONT_HEADER_SM);
        titleSec3.setForeground(Theme.PRIMARY);
        titleSec3.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardDocs.add(titleSec3);
        cardDocs.add(Theme.createVerticalStrut(6));

        JLabel docNote = new JLabel("<html>Check the physical documents you currently have available. <i>(You can submit your application online now, then bring original hardcopies to the Barangay Hall for document screening)</i>:</html>");
        docNote.setFont(Theme.FONT_BODY);
        docNote.setForeground(Theme.TEXT_MUTED);
        docNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardDocs.add(docNote);
        cardDocs.add(Theme.createVerticalStrut(10));

        chkIndigency = new JCheckBox("Certificate of Indigency (Issued by Barangay Hall)");
        chkGrades = new JCheckBox("Certified True Copy of Grades / Transcript of Records / Form 138");
        chkEnrollment = new JCheckBox("Certificate of Registration (COR) / Official School Enrollment Form");
        chkId = new JCheckBox("Valid Student ID or Barangay Resident ID");

        chkIndigency.setFont(Theme.FONT_BODY);
        chkGrades.setFont(Theme.FONT_BODY);
        chkEnrollment.setFont(Theme.FONT_BODY);
        chkId.setFont(Theme.FONT_BODY);

        JPanel chkGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        chkGrid.setOpaque(false);
        chkGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        chkGrid.add(chkIndigency);
        chkGrid.add(chkGrades);
        chkGrid.add(chkEnrollment);
        chkGrid.add(chkId);
        cardDocs.add(chkGrid);
        cardDocs.add(Theme.createVerticalStrut(16));

        // Consent Checkbox
        chkConsent = new JCheckBox("<html><b>Data Privacy & Truthfulness Certification:</b> I hereby certify that the information provided is true, accurate, and complete. I consent to the processing of this data by the Barangay Council and SK under the Data Privacy Act of 2012 for the purpose of scholarship evaluation.</html>");
        chkConsent.setFont(Theme.FONT_SMALL);
        chkConsent.setForeground(Theme.TEXT_TITLE);
        chkConsent.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardDocs.add(chkConsent);
        cardDocs.add(Theme.createVerticalStrut(16));

        // Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton btnClear = new JButton("Clear Form");
        btnClear.setFont(Theme.FONT_BODY);
        btnClear.addActionListener(e -> clearForm());

        JButton btnSubmit = Theme.createStyledButton("Submit Scholarship Application  ➔", Theme.ACCENT, Color.WHITE);
        btnSubmit.setFont(Theme.FONT_HEADER_SM);
        btnSubmit.setPreferredSize(new Dimension(280, 42));
        btnSubmit.addActionListener(e -> submitApplication());

        actionRow.add(btnClear);
        actionRow.add(btnSubmit);
        cardDocs.add(actionRow);

        mainFormContainer.add(cardDocs);

        JScrollPane scrollPane = Theme.createBorderlessScrollPane(mainFormContainer);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createField(String labelText, JComponent field, String tooltip) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.FONT_SMALL_BOLD);
        lbl.setForeground(Theme.TEXT_TITLE);
        p.add(lbl, BorderLayout.NORTH);

        if (field instanceof JTextField) {
            JTextField tf = (JTextField) field;
            tf.setFont(Theme.FONT_BODY);
            tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 36));
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            tf.setBorder(new CompoundBorder(
                    new LineBorder(Theme.BORDER, 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        } else if (field instanceof JComboBox) {
            field.setPreferredSize(new Dimension(field.getPreferredSize().width, 36));
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        }

        if (tooltip != null) {
            field.setToolTipText(tooltip);
        }

        p.add(field, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        p.setBorder(new EmptyBorder(0, 0, 10, 0));
        return p;
    }

    private void updateEligibilityPreview() {
        String gwaStr = txtGwa.getText().trim();
        String incStr = txtIncome.getText().trim();

        if (gwaStr.isEmpty() || incStr.isEmpty()) {
            eligibilityBanner.setBackground(Theme.INFO_BG);
            lblEligibilityText.setForeground(Theme.INFO);
            lblEligibilityText.setText("ℹ️ Enter your GWA and Monthly Income above to view real-time eligibility evaluation.");
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
                lblEligibilityText.setText("✔ AUTO-CHECK: ELIGIBLE! You meet both academic standing and household income criteria (₱25k max).");
            } else {
                eligibilityBanner.setBackground(Theme.WARNING_BG);
                lblEligibilityText.setForeground(new Color(180, 83, 9)); // Dark amber
                StringBuilder sb = new StringBuilder("⚠ AUTO-CHECK NOTICE: ");
                if (!incomeOk) sb.append("Income exceeds ₱25,000 threshold. ");
                if (!gwaOk) {
                    if (isCollege) sb.append("College GWA exceeds 2.50 cut-off. ");
                    else sb.append("High School Average is below 85.0% cut-off. ");
                }
                sb.append("You may still submit your application for case-by-case SK Council evaluation.");
                lblEligibilityText.setText(sb.toString().trim());
            }
        } catch (NumberFormatException ex) {
            eligibilityBanner.setBackground(Theme.DANGER_BG);
            lblEligibilityText.setForeground(Theme.DANGER);
            lblEligibilityText.setText("⚠ Please input valid numerical values for GWA and Monthly Income.");
        }
    }

    private void submitApplication() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String school = txtSchool.getText().trim();
        String year = (String) cmbYear.getSelectedItem();
        String purok = (String) cmbPurok.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || school.isEmpty() ||
                txtGwa.getText().trim().isEmpty() || txtIncome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields (marked with *).",
                    "Incomplete Application",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!chkConsent.isSelected()) {
            JOptionPane.showMessageDialog(this,
                    "Please review and check the Data Privacy & Truthfulness Certification checkbox to proceed.",
                    "Certification Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        double gwa;
        double income;
        try {
            gwa = Double.parseDouble(txtGwa.getText().trim());
            income = Double.parseDouble(txtIncome.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "GWA and Monthly Income must be valid numbers.",
                    "Invalid Numeric Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        DocumentChecklist docs = new DocumentChecklist(
                chkIndigency.isSelected(),
                chkGrades.isSelected(),
                chkEnrollment.isSelected(),
                chkId.isSelected()
        );

        // Register student applicant
        Applicant applicant = manager.registerApplicant(name, email, phone, school, year, gwa, income, purok, docs);

        // Show official confirmation modal slip
        showConfirmationReceipt(applicant);

        clearForm();
    }

    private void showConfirmationReceipt(Applicant applicant) {
        int queuePos = manager.getQueuePosition(applicant.getApplicantId());
        String queueDisplay = queuePos > 0 ? ("#" + queuePos + " in Line") : "Evaluated";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Official Application Receipt", true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JLabel emblem = new JLabel("BARANGAY SCHOLARSHIP & EDUCATIONAL AID");
        emblem.setFont(Theme.FONT_SMALL_BOLD);
        emblem.setForeground(Theme.PRIMARY);

        JLabel h1 = new JLabel("Application Submitted Successfully!");
        h1.setFont(Theme.FONT_HEADER_MD);
        h1.setForeground(Theme.ACCENT_HOVER);

        card.add(emblem);
        card.add(Box.createVerticalStrut(4));
        card.add(h1);
        card.add(Box.createVerticalStrut(14));

        // Ref ID Box
        JPanel refBox = new JPanel(new BorderLayout());
        refBox.setBackground(Theme.PRIMARY_LIGHT);
        refBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(199, 210, 254), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblRefTag = new JLabel("YOUR APPLICATION REFERENCE NUMBER:");
        lblRefTag.setFont(Theme.FONT_SMALL_BOLD);
        lblRefTag.setForeground(Theme.PRIMARY);

        JLabel lblRefNum = new JLabel(applicant.getApplicantId());
        lblRefNum.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblRefNum.setForeground(Theme.PRIMARY);

        refBox.add(lblRefTag, BorderLayout.NORTH);
        refBox.add(lblRefNum, BorderLayout.CENTER);
        card.add(refBox);
        card.add(Box.createVerticalStrut(16));

        // Details grid
        JPanel grid = new JPanel(new GridLayout(5, 2, 8, 6));
        grid.setOpaque(false);

        addDetailRow(grid, "Applicant Name:", applicant.getFullName());
        addDetailRow(grid, "School:", applicant.getSchool());
        addDetailRow(grid, "Year & Purok:", applicant.getYearLevel() + " • " + applicant.getPurok());
        addDetailRow(grid, "Screening Queue:", queueDisplay);
        addDetailRow(grid, "Initial Status:", applicant.getStatus().getDisplayName());

        card.add(grid);
        card.add(Box.createVerticalStrut(16));

        // Next Steps Notice
        JPanel noteBox = new JPanel(new BorderLayout());
        noteBox.setBackground(Theme.INFO_BG);
        noteBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel noteTitle = new JLabel("📌 NEXT STEPS FOR DOCUMENT SCREENING:");
        noteTitle.setFont(Theme.FONT_SMALL_BOLD);
        noteTitle.setForeground(Theme.INFO);

        JLabel noteBody = new JLabel("<html>Please present your <b>Application Reference Number (" + applicant.getApplicantId() + ")</b> along with original physical copies of your documents to the Barangay Hall during office hours (Mon-Fri 8:00 AM - 5:00 PM).</html>");
        noteBody.setFont(Theme.FONT_SMALL);
        noteBody.setForeground(Theme.TEXT_BODY);

        noteBox.add(noteTitle, BorderLayout.NORTH);
        noteBox.add(noteBody, BorderLayout.CENTER);
        card.add(noteBox);
        card.add(Box.createVerticalStrut(16));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton btnTrack = Theme.createStyledButton("Track Status Now  ➔", Theme.PRIMARY, Color.WHITE);
        btnTrack.addActionListener(e -> {
            dialog.dispose();
            if (onNavigate != null) {
                onNavigate.accept("TRACKER:" + applicant.getApplicantId());
            }
        });

        JButton btnClose = new JButton("Close Receipt");
        btnClose.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnClose);
        btnPanel.add(btnTrack);
        card.add(btnPanel);

        dialog.add(card, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel grid, String label, String val) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL_BOLD);
        l.setForeground(Theme.TEXT_MUTED);

        JLabel v = new JLabel(val);
        v.setFont(Theme.FONT_SMALL_BOLD);
        v.setForeground(Theme.TEXT_TITLE);

        grid.add(l);
        grid.add(v);
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
        chkConsent.setSelected(false);
        updateEligibilityPreview();
    }
}
