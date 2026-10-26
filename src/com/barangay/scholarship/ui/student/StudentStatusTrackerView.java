package com.barangay.scholarship.ui.student;

import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.ApplicationStatus;
import com.barangay.scholarship.model.DocumentChecklist;
import com.barangay.scholarship.model.Scholar;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.util.FileUploadManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Dedicated Application Status Inquiry & Tracking View for students.
 * Allows students to independently track their application progress, queue position,
 * and document submission status using their Application ID or Name, without exposing
 * other applicants' confidential records or administrative controls.
 */
public class StudentStatusTrackerView extends JPanel {
    private final ScholarshipManager manager;

    private JTextField txtSearch;
    private JPanel resultContainer;
    private JLabel lblStatusSummary;

    public StudentStatusTrackerView(ScholarshipManager manager) {
        this.manager = manager;

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

        JLabel tag = new JLabel("STUDENT INQUIRY DESK");
        tag.setFont(Theme.FONT_SMALL_BOLD);
        tag.setForeground(Theme.PRIMARY);

        JLabel title = new JLabel("Track Your Scholarship Application Status");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Enter your Application Reference ID (e.g., APP-2026-001) or your Full Name to view your real-time evaluation status.");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(tag);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Center: Search bar + Results Card
        JPanel centerPanel = new Theme.ScrollablePanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Search Input Card (Full width responsive card)
        JPanel searchCard = Theme.createFullWidthCardPanel();
        searchCard.setLayout(new BoxLayout(searchCard, BoxLayout.Y_AXIS));

        JLabel searchPrompt = new JLabel("Application Reference ID or Student Full Name:");
        searchPrompt.setFont(Theme.FONT_BODY_BOLD);
        searchPrompt.setForeground(Theme.TEXT_TITLE);
        searchPrompt.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);
        searchBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        txtSearch = new JTextField();
        txtSearch.setFont(Theme.FONT_BODY);
        txtSearch.setPreferredSize(new Dimension(300, 38));
        txtSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtSearch.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = Theme.createStyledButton("Check Application Status", Theme.PRIMARY, Color.WHITE);
        btnSearch.setFont(Theme.FONT_BODY_BOLD);
        btnSearch.setPreferredSize(new Dimension(220, 38));
        btnSearch.setMaximumSize(new Dimension(220, 38));
        btnSearch.addActionListener(e -> performSearch());

        searchBar.add(txtSearch, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);

        lblStatusSummary = new JLabel("Tip: Enter your exact ID or name, then press Enter or click 'Check Application Status'");
        lblStatusSummary.setFont(Theme.FONT_SMALL);
        lblStatusSummary.setForeground(Theme.TEXT_MUTED);
        lblStatusSummary.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchCard.add(searchPrompt);
        searchCard.add(Box.createVerticalStrut(8));
        searchCard.add(searchBar);
        searchCard.add(Box.createVerticalStrut(8));
        searchCard.add(lblStatusSummary);

        centerPanel.add(searchCard);
        centerPanel.add(Box.createVerticalStrut(16));

        // Results Container
        resultContainer = new JPanel();
        resultContainer.setOpaque(false);
        resultContainer.setLayout(new BoxLayout(resultContainer, BoxLayout.Y_AXIS));
        resultContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Default empty state placeholder
        showEmptyPlaceholder();

        centerPanel.add(resultContainer);
        centerPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = Theme.createBorderlessScrollPane(centerPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void searchForId(String query) {
        if (query != null && !query.trim().isEmpty()) {
            txtSearch.setText(query.trim());
            performSearch();
        }
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your Application Reference ID or Full Name to search.",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Applicant> matches = manager.findApplicantsByIdOrName(query);
        resultContainer.removeAll();

        if (matches.isEmpty()) {
            showNotFoundPlaceholder(query);
        } else if (matches.size() == 1) {
            displayApplicantCard(matches.get(0));
            lblStatusSummary.setText("Showing status record for: " + matches.get(0).getFullName());
        } else {
            displayMultipleMatches(matches);
            lblStatusSummary.setText("Found " + matches.size() + " matches. Please select your specific application record below:");
        }

        resultContainer.revalidate();
        resultContainer.repaint();
    }

    private void displayApplicantCard(Applicant a) {
        JPanel card = Theme.createFullWidthCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Top Banner Row: Name + Status Badge
        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel nameBox = new JPanel();
        nameBox.setOpaque(false);
        nameBox.setLayout(new BoxLayout(nameBox, BoxLayout.Y_AXIS));
        nameBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblName = new JLabel(a.getFullName());
        lblName.setFont(Theme.FONT_HEADER_MD);
        lblName.setForeground(Theme.TEXT_TITLE);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        String idText = "Reference ID: " + a.getApplicantId();
        if (a instanceof Scholar) {
            idText += "  •  Scholar ID: " + ((Scholar) a).getScholarId();
        }
        JLabel lblId = new JLabel(idText);
        lblId.setFont(Theme.FONT_BODY_BOLD);
        lblId.setForeground(Theme.PRIMARY);
        lblId.setAlignmentX(Component.LEFT_ALIGNMENT);

        nameBox.add(lblName);
        nameBox.add(Box.createVerticalStrut(2));
        nameBox.add(lblId);

        // Status Badge Pill
        JLabel badge = createStatusBadge(a.getStatus());
        topRow.add(nameBox, BorderLayout.WEST);
        topRow.add(badge, BorderLayout.EAST);

        card.add(topRow);
        card.add(Box.createVerticalStrut(14));

        // Status Narrative Box (Plain student-friendly explanation)
        JPanel explainBox = new JPanel(new BorderLayout());
        explainBox.setBorder(new EmptyBorder(12, 14, 12, 14));
        explainBox.setBackground(getStatusBgColor(a.getStatus()));
        explainBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblExplainTitle = new JLabel("STATUS OVERVIEW & NEXT STEPS:");
        lblExplainTitle.setFont(Theme.FONT_SMALL_BOLD);
        lblExplainTitle.setForeground(getStatusFgColor(a.getStatus()));

        JLabel lblExplanation = new JLabel("<html>" + getStatusExplanation(a) + "</html>");
        lblExplanation.setFont(Theme.FONT_BODY);
        lblExplanation.setForeground(Theme.TEXT_TITLE);

        explainBox.add(lblExplainTitle, BorderLayout.NORTH);
        explainBox.add(Box.createVerticalStrut(4), BorderLayout.CENTER);
        explainBox.add(lblExplanation, BorderLayout.SOUTH);

        card.add(explainBox);
        card.add(Box.createVerticalStrut(16));

        // Details Grid (Academic & Profile info)
        JPanel detailsGrid = new JPanel(new GridLayout(3, 2, 16, 8));
        detailsGrid.setOpaque(false);
        detailsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        addDetailItem(detailsGrid, "School / University:", a.getSchool());
        addDetailItem(detailsGrid, "Year Level:", a.getYearLevel());
        addDetailItem(detailsGrid, "Barangay Zone / Purok:", a.getPurok());
        addDetailItem(detailsGrid, "GWA Recorded:", a.getFormattedGwa());
        addDetailItem(detailsGrid, "Submission Timestamp:", a.getFormattedDate());

        if (a instanceof Scholar) {
            Scholar s = (Scholar) a;
            String stipendStr = s.getFormattedGrant() + (s.isStipendDisbursed() ? " [CLAIMED / DISBURSED]" : " [AVAILABLE FOR PAYOUT]");
            addDetailItem(detailsGrid, "Scholarship Grant:", stipendStr);
        } else {
            addDetailItem(detailsGrid, "Target Grant Amount:", "₱3,000.00 / semester");
        }

        card.add(detailsGrid);
        card.add(Box.createVerticalStrut(18));

        // Physical Document Requirements Status Box
        JPanel docBox = new JPanel();
        docBox.setOpaque(false);
        docBox.setLayout(new BoxLayout(docBox, BoxLayout.Y_AXIS));
        docBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel docTitle = new JLabel("PHYSICAL DOCUMENT VERIFICATION CHECKLIST");
        docTitle.setFont(Theme.FONT_SMALL_BOLD);
        docTitle.setForeground(Theme.TEXT_MUTED);
        docTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        docBox.add(docTitle);
        docBox.add(Box.createVerticalStrut(8));

        DocumentChecklist docs = a.getDocuments();
        JPanel docGrid = new JPanel(new GridLayout(2, 2, 12, 8));
        docGrid.setOpaque(false);
        docGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        docGrid.add(createDocPill("Certificate of Indigency", docs.isCertificateOfIndigency(), docs.getIndigencyFilePath()));
        docGrid.add(createDocPill("Copy of Grades / TOR", docs.isCertifiedCopyOfGrades(), docs.getGradesFilePath()));
        docGrid.add(createDocPill("Certificate of Registration (COR)", docs.isEnrollmentForm(), docs.getEnrollmentFilePath()));
        docGrid.add(createDocPill("Valid Student / Resident ID", docs.isValidBarangayOrSchoolId(), docs.getIdFilePath()));

        docBox.add(docGrid);
        card.add(docBox);

        resultContainer.add(card);
    }

    private void displayMultipleMatches(List<Applicant> matches) {
        JPanel container = Theme.createFullWidthCardPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Multiple Matching Records Found (" + matches.size() + ")");
        title.setFont(Theme.FONT_HEADER_SM);
        title.setForeground(Theme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(title);
        container.add(Box.createVerticalStrut(10));

        for (Applicant a : matches) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setBorder(new CompoundBorder(
                    new LineBorder(Theme.BORDER, 1, true),
                    new EmptyBorder(8, 12, 8, 12)
            ));

            JLabel info = new JLabel("<html><b>" + a.getFullName() + "</b> (" + a.getApplicantId() + ") - " + a.getSchool() + " • " + a.getPurok() + "</html>");
            info.setFont(Theme.FONT_BODY);

            JButton btnView = Theme.createStyledButton("View Record", Theme.PRIMARY, Color.WHITE);
            btnView.addActionListener(e -> {
                resultContainer.removeAll();
                displayApplicantCard(a);
                resultContainer.revalidate();
                resultContainer.repaint();
            });

            row.add(info, BorderLayout.CENTER);
            row.add(btnView, BorderLayout.EAST);

            container.add(row);
            container.add(Box.createVerticalStrut(6));
        }

        resultContainer.add(container);
    }

    private void showEmptyPlaceholder() {
        JPanel p = Theme.createFullWidthCardPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(40, 20, 40, 20));

        JLabel h = new JLabel("No Application Record Loaded", SwingConstants.CENTER);
        h.setFont(Theme.FONT_HEADER_MD);
        h.setForeground(Theme.TEXT_TITLE);
        h.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Type your Application Reference ID or Name in the search box above to inspect your real-time status.", SwingConstants.CENTER);
        desc.setFont(Theme.FONT_BODY);
        desc.setForeground(Theme.TEXT_MUTED);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(h);
        p.add(Box.createVerticalStrut(4));
        p.add(desc);

        resultContainer.add(p);
    }

    private void showNotFoundPlaceholder(String query) {
        JPanel p = Theme.createFullWidthCardPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(32, 20, 32, 20));

        JLabel h = new JLabel("No Record Found for \"" + query + "\"", SwingConstants.CENTER);
        h.setFont(Theme.FONT_HEADER_MD);
        h.setForeground(Theme.DANGER);
        h.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Please verify your Application Reference ID (e.g. APP-2026-001) or ensure your name was entered correctly.", SwingConstants.CENTER);
        desc.setFont(Theme.FONT_BODY);
        desc.setForeground(Theme.TEXT_MUTED);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(h);
        p.add(Box.createVerticalStrut(4));
        p.add(desc);

        resultContainer.add(p);
    }

    private JLabel createStatusBadge(ApplicationStatus status) {
        JLabel badge = new JLabel(" " + status.getDisplayName().toUpperCase() + " ");
        badge.setFont(Theme.FONT_BODY_BOLD);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(6, 14, 6, 14));

        switch (status) {
            case APPROVED:
                badge.setBackground(Theme.ACCENT_BG);
                badge.setForeground(Theme.ACCENT_HOVER);
                badge.setText("OFFICIAL SCHOLAR");
                break;
            case DISBURSED:
                badge.setBackground(new Color(224, 242, 254));
                badge.setForeground(new Color(3, 105, 161));
                badge.setText("STIPEND DISBURSED");
                break;
            case VERIFIED:
                badge.setBackground(Theme.INFO_BG);
                badge.setForeground(Theme.INFO);
                badge.setText("DOCUMENTS VERIFIED");
                break;
            case WAITLISTED:
                badge.setBackground(new Color(243, 232, 255));
                badge.setForeground(new Color(126, 34, 206));
                badge.setText("ON WAITLIST QUEUE");
                break;
            case PENDING_VERIFICATION:
                badge.setBackground(Theme.WARNING_BG);
                badge.setForeground(new Color(180, 83, 9));
                badge.setText("PENDING SCREENING");
                break;
            case REJECTED:
                badge.setBackground(Theme.DANGER_BG);
                badge.setForeground(Theme.DANGER);
                badge.setText("NOT APPROVED");
                break;
        }
        return badge;
    }

    private String getStatusExplanation(Applicant a) {
        ApplicationStatus s = a.getStatus();
        switch (s) {
            case PENDING_VERIFICATION:
                int qPos = manager.getQueuePosition(a.getApplicantId());
                String posText = qPos > 0 ? ("You are currently <b>#" + qPos + " in line</b> in the Document Screening Queue. ") : "";
                return posText + "Your application has been received! Please bring your complete physical document requirements to the Barangay Hall for staff verification.";

            case VERIFIED:
                return "<b>Excellent news!</b> All your physical documents have been examined and confirmed by Barangay Staff. Your application is queued and ready for the upcoming <b>Algorithmic Slot Allocation & Ranking</b> batch.";

            case APPROVED:
                Scholar sch = (a instanceof Scholar) ? (Scholar) a : null;
                String schId = sch != null ? sch.getScholarId() : "Assigned";
                return "<b>Congratulations!</b> You have been awarded an official Barangay Scholarship Slot! (Scholar ID: <b>" + schId + "</b>, Grant: <b>₱3,000.00</b>). Please stay tuned for barangay announcements regarding payout day.";

            case WAITLISTED:
                int wPos = manager.getWaitlistPosition(a.getApplicantId());
                String wText = wPos > 0 ? ("You are currently <b>#" + wPos + "</b> on the official FIFO Waitlist Queue. ") : "";
                return wText + "You qualified for the scholarship, but the initial slot quota was filled by higher-ranking applicants. If any current scholar drops out or fails to claim their grant, you will be <b>automatically promoted</b> in order!";

            case DISBURSED:
                return "<b>Grant Successfully Claimed!</b> Your scholarship allowance of ₱3,000.00 has been officially released and logged in the Barangay Treasury payroll registry.";

            case REJECTED:
                return "Your application does not meet the current rubric criteria (" + a.getEligibilityReason() + "). You may visit the Barangay Hall for inquiries or apply during the next semester.";

            default:
                return s.getDescription();
        }
    }

    private Color getStatusBgColor(ApplicationStatus status) {
        switch (status) {
            case APPROVED: return Theme.ACCENT_BG;
            case DISBURSED: return new Color(224, 242, 254);
            case VERIFIED: return Theme.INFO_BG;
            case WAITLISTED: return new Color(243, 232, 255);
            case PENDING_VERIFICATION: return Theme.WARNING_BG;
            case REJECTED: return Theme.DANGER_BG;
            default: return Theme.BG_APP;
        }
    }

    private Color getStatusFgColor(ApplicationStatus status) {
        switch (status) {
            case APPROVED: return Theme.ACCENT_HOVER;
            case DISBURSED: return new Color(3, 105, 161);
            case VERIFIED: return Theme.INFO;
            case WAITLISTED: return new Color(126, 34, 206);
            case PENDING_VERIFICATION: return new Color(180, 83, 9);
            case REJECTED: return Theme.DANGER;
            default: return Theme.TEXT_MUTED;
        }
    }

    private JPanel createDocPill(String docTitle, boolean isSubmitted, String filePath) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(isSubmitted ? Theme.ACCENT_BG : Theme.WARNING_BG);
        p.setBorder(new CompoundBorder(
                new LineBorder(isSubmitted ? new Color(167, 243, 208) : new Color(253, 230, 138), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel title = new JLabel(docTitle);
        title.setFont(Theme.FONT_SMALL_BOLD);
        title.setForeground(Theme.TEXT_TITLE);
        p.add(title, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        if (filePath != null && !filePath.trim().isEmpty()) {
            JButton btnView = new JButton("👁 View");
            btnView.setFont(Theme.FONT_SMALL);
            btnView.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnView.setMargin(new Insets(2, 6, 2, 6));
            btnView.addActionListener(e -> FileUploadManager.openFile(this, filePath));
            right.add(btnView);
        }

        JLabel status = new JLabel(isSubmitted ? (filePath != null ? "✓ Uploaded" : "✓ Received") : "Pending");
        status.setFont(Theme.FONT_SMALL_BOLD);
        status.setForeground(isSubmitted ? Theme.ACCENT_HOVER : new Color(180, 83, 9));
        right.add(status);

        p.add(right, BorderLayout.EAST);
        return p;
    }

    private void addDetailItem(JPanel grid, String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(Theme.FONT_BODY_BOLD);
        v.setForeground(Theme.TEXT_TITLE);

        p.add(l);
        p.add(v);
        grid.add(p);
    }
}
