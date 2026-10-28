package com.barangay.scholarship.ui.student;

import com.barangay.scholarship.ui.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Guidelines, Eligibility Criteria, and FAQ View for student applicants.
 */
public class StudentGuidelinesView extends JPanel {

    public StudentGuidelinesView() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(24, 32, 32, 32));

        initUI();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel tag = new JLabel("SCHOLARSHIP INFORMATION & FAQ");
        tag.setFont(Theme.FONT_SMALL_BOLD);
        tag.setForeground(Theme.PRIMARY);

        JLabel title = new JLabel("Program Guidelines & Eligibility Requirements");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Everything you need to know about the Barangay Educational Assistance & SK Scholarship Fund.");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(tag);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel container = new Theme.ScrollablePanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // 1. Eligibility Criteria Card
        JPanel cardEligibility = Theme.createFullWidthCardPanel();
        cardEligibility.setLayout(new BoxLayout(cardEligibility, BoxLayout.Y_AXIS));

        JLabel h1 = new JLabel("1. Who Can Apply? (Eligibility Criteria)");
        h1.setFont(Theme.FONT_HEADER_SM);
        h1.setForeground(Theme.PRIMARY);
        h1.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardEligibility.add(h1);
        cardEligibility.add(Theme.createVerticalStrut(10));

        cardEligibility.add(createBulletItem("Barangay Residency:", "Must be a bona fide resident of the Barangay (any recognized Purok/Zone) for at least 6 months."));
        cardEligibility.add(createBulletItem("Enrolled Student:", "Must be currently enrolled in an accredited Senior High School (Grade 11/12) or College / State University."));
        cardEligibility.add(createBulletItem("Academic Standing (Dual Scale):",
                "Collegiate Scale: GWA of 2.50 or better (1.00 - 2.50 qualifying range)\n" +
                "Percentage Scale: General Average of 85.0% or higher (Senior High School)"));
        cardEligibility.add(createBulletItem("Financial Need Threshold:", "Gross monthly household family income must not exceed ₱25,000.00."));

        container.add(cardEligibility);
        container.add(Theme.createVerticalStrut(16));

        // 2. Required Documents Card
        JPanel cardDocs = Theme.createFullWidthCardPanel();
        cardDocs.setLayout(new BoxLayout(cardDocs, BoxLayout.Y_AXIS));

        JLabel h2 = new JLabel("2. Required Physical Documents for Screening");
        h2.setFont(Theme.FONT_HEADER_SM);
        h2.setForeground(Theme.PRIMARY);
        h2.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardDocs.add(h2);
        cardDocs.add(Theme.createVerticalStrut(10));

        cardDocs.add(createDocRequirement("1. Certificate of Indigency", "Issued by the Barangay Hall proving economic qualification."));
        cardDocs.add(createDocRequirement("2. Certified Copy of Grades / Transcript (TOR)", "Form 138 or Certified True Copy of Grades from previous semester."));
        cardDocs.add(createDocRequirement("3. Certificate of Registration (COR)", "Official school enrollment form for the current academic term."));
        cardDocs.add(createDocRequirement("4. Valid ID", "School ID with current school year validation or Barangay Resident ID."));

        container.add(cardDocs);
        container.add(Theme.createVerticalStrut(16));

        // 3. Transparent Selection Process Card
        JPanel cardProcess = Theme.createFullWidthCardPanel();
        cardProcess.setLayout(new BoxLayout(cardProcess, BoxLayout.Y_AXIS));

        JLabel h3 = new JLabel("3. How Applications Are Processed (Transparent Algorithmic Evaluation)");
        h3.setFont(Theme.FONT_HEADER_SM);
        h3.setForeground(Theme.PRIMARY);
        h3.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardProcess.add(h3);
        cardProcess.add(Theme.createVerticalStrut(10));

        cardProcess.add(createBulletItem("Document Screening (FIFO Queue):", "Applications are screened strictly on a first-come, first-served basis using a custom Queue data structure."));
        cardProcess.add(createBulletItem("Merit Ranking (Merge Sort):", "Evaluates and ranks applicants by General Weighted Average, prioritizing top academic achievers."));
        cardProcess.add(createBulletItem("Need-Based Ranking (Quick Sort):", "Sorts qualified candidates by lowest household income to ensure aid reaches the most in-need students."));
        cardProcess.add(createBulletItem("Automated Waitlist Promotion:", "Excess qualified candidates are queued in the Waitlist. If an approved scholar drops out or fails to claim, the next applicant is automatically promoted!"));

        container.add(cardProcess);
        container.add(Theme.createVerticalStrut(16));

        // 4. Contact & Schedule Card
        JPanel cardContact = Theme.createFullWidthCardPanel();
        cardContact.setLayout(new BorderLayout());
        cardContact.setBackground(Theme.PRIMARY_LIGHT);
        cardContact.setBorder(new CompoundBorder(
                new LineBorder(new Color(199, 210, 254), 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel contactTitle = new JLabel("BARANGAY HALL SCHOLARSHIP HELP DESK");
        contactTitle.setFont(Theme.FONT_SMALL_BOLD);
        contactTitle.setForeground(Theme.PRIMARY);

        JLabel contactBody = new JLabel("<html><b>Office Hours:</b> Monday to Friday, 8:00 AM – 5:00 PM<br>" +
                "<b>Location:</b> Sangguniang Kabataan (SK) Affairs Office, 2nd Floor, Barangay Hall<br>" +
                "<b>Grant Amount:</b> ₱3,000.00 cash stipend per semester upon official approval</html>");
        contactBody.setFont(Theme.FONT_BODY);
        contactBody.setForeground(Theme.TEXT_TITLE);

        cardContact.add(contactTitle, BorderLayout.NORTH);
        cardContact.add(Box.createVerticalStrut(6), BorderLayout.CENTER);
        cardContact.add(contactBody, BorderLayout.SOUTH);

        container.add(cardContact);

        JScrollPane scrollPane = Theme.createBorderlessScrollPane(container);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createBulletItem(String label, String desc) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel l = new JLabel("• " + label);
        l.setFont(Theme.FONT_BODY_BOLD);
        l.setForeground(Theme.TEXT_TITLE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea d = new JTextArea(desc);
        d.setFont(Theme.FONT_BODY);
        d.setForeground(Theme.TEXT_BODY);
        d.setLineWrap(true);
        d.setWrapStyleWord(true);
        d.setEditable(false);
        d.setFocusable(false);
        d.setOpaque(false);
        d.setBorder(null);
        d.setRows(Math.max(1, (int) Math.ceil(desc.length() / 100.0)));
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.getPreferredSize().height));

        p.add(l);
        p.add(d);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private JPanel createDocRequirement(String title, String desc) {
        JPanel p = new JPanel(new BorderLayout(8, 2));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_BODY_BOLD);
        t.setForeground(Theme.PRIMARY);

        JLabel d = new JLabel(desc);
        d.setFont(Theme.FONT_BODY);
        d.setForeground(Theme.TEXT_BODY);

        p.add(t, BorderLayout.NORTH);
        p.add(d, BorderLayout.CENTER);
        return p;
    }
}
