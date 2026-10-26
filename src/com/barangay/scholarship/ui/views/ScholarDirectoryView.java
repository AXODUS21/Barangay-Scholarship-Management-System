package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.algorithms.AlgorithmUtils;
import com.barangay.scholarship.datastructures.ScholarBST;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Scholar Directory View featuring custom Binary Search Tree (BST) lookups,
 * tree traversals (In-Order, Pre-Order, Post-Order), Binary Search, and automatic
 * waitlist candidate promotion upon dropping a scholar.
 */
public class ScholarDirectoryView extends JPanel {
    private final ScholarshipManager manager;

    // Stat Cards
    private StatCard cardTreeSize;
    private StatCard cardTreeRoot;

    // Selected Scholar Preview Panel
    private JPanel detailCard;
    private JLabel lblDetailTitle;
    private JLabel lblDetailSubtitle;
    private JLabel lblDetailBadge;
    private JButton btnDetailDrop;
    private JLabel valScholarId, valAppId, valName, valContact, valSchool, valYear, valPurok, valGwa, valStatus;
    private JPanel docBadgesPanel;
    private Scholar currentSelectedScholar;

    // Controls
    private JTextField txtSearchId;
    private JComboBox<String> cmbTraversal;
    private JComboBox<String> cmbPurokFilter;

    // Table
    private DefaultTableModel dirTableModel;
    private ModernTable dirTable;

    public ScholarDirectoryView(ScholarshipManager manager) {
        this.manager = manager;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        refreshDirectory();
    }

    private void initUI() {
        // Top Container: Header + Full-Width Stats Grid
        JPanel topPanel = new JPanel(new BorderLayout(0, 14));
        topPanel.setOpaque(false);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Official Scholar Directory & Binary Search Tree (BST)");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Fast O(log n) directory lookups, In-Order/Pre-Order/Post-Order traversals, and automated waitlist promotion");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);
        topPanel.add(headerPanel, BorderLayout.NORTH);

        // BST Metrics - 2 cards spanning 50% each with no trailing gap
        JPanel statsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        statsGrid.setOpaque(false);

        cardTreeSize = new StatCard("BST Active Nodes", "0 Scholars", "Indexed in Binary Search Tree", Theme.PRIMARY);
        cardTreeRoot = new StatCard("BST Root Node", "None", "Balanced Key Anchor", Theme.ACCENT);

        statsGrid.add(cardTreeSize);
        statsGrid.add(cardTreeRoot);
        topPanel.add(statsGrid, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Search Toolbar Card: Full-Width Responsive Search Bar
        JPanel searchCard = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(32767, getPreferredSize().height);
            }
        };
        searchCard.setBackground(Theme.CARD_BG);
        searchCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchCard.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel searchBar = new JPanel(new BorderLayout(14, 0));
        searchBar.setOpaque(false);

        JLabel lblSearch = new JLabel("Search Scholar (ID / Name):");
        lblSearch.setFont(Theme.FONT_BODY_BOLD);
        lblSearch.setForeground(Theme.TEXT_TITLE);

        txtSearchId = Theme.createRoundedTextField("Search scholars by ID, name, school, purok, or term (Enter for BST lookup)...");
        txtSearchId.setPreferredSize(new Dimension(300, 38));
        txtSearchId.setMaximumSize(new Dimension(32767, 38));
        txtSearchId.setToolTipText("Type to filter live in real-time, or press Enter for BST O(log n) lookup");
        txtSearchId.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshDirectory(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshDirectory(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshDirectory(); }
        });
        txtSearchId.addActionListener(e -> executeBinarySearch());

        JButton btnSearch = Theme.createStyledButton("Search", Theme.PRIMARY, Color.WHITE);
        btnSearch.setFont(Theme.FONT_BODY_BOLD);
        btnSearch.setPreferredSize(new Dimension(100, 38));
        btnSearch.addActionListener(e -> executeBinarySearch());

        searchBar.add(lblSearch, BorderLayout.WEST);
        searchBar.add(txtSearchId, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);
        searchCard.add(searchBar, BorderLayout.CENTER);

        centerPanel.add(searchCard);
        centerPanel.add(Box.createVerticalStrut(14));

        // Selected Row Preview Dossier Card (Between Search and Directory Table)
        detailCard = createDetailInspectionCard();
        detailCard.setVisible(false);
        detailCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(detailCard);

        // Directory Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel tblHeader = new JPanel(new BorderLayout(10, 0));
        tblHeader.setOpaque(false);
        tblHeader.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel tblTitle = new JLabel("BST Scholar Directory & Traversal Output");
        tblTitle.setFont(Theme.FONT_HEADER_SM);
        tblTitle.setForeground(Theme.TEXT_TITLE);
        tblHeader.add(tblTitle, BorderLayout.WEST);

        JPanel tblActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tblActions.setOpaque(false);

        JLabel lblTrav = new JLabel("BST Traversal:");
        lblTrav.setFont(Theme.FONT_BODY_BOLD);
        String[] traversals = {"In-Order (Sorted by ID)", "Pre-Order (Root First)", "Post-Order (Leaves First)"};
        cmbTraversal = new JComboBox<>(traversals);
        cmbTraversal.setPreferredSize(new Dimension(185, 32));
        cmbTraversal.addActionListener(e -> refreshDirectory());

        JButton btnDrop = Theme.createStyledButton("Drop & Promote Waitlist", Theme.DANGER, Color.WHITE);
        btnDrop.addActionListener(e -> dropSelectedScholar());

        tblActions.add(lblTrav);
        tblActions.add(cmbTraversal);
        tblActions.add(btnDrop);
        tblHeader.add(tblActions, BorderLayout.EAST);

        tableCard.add(tblHeader, BorderLayout.NORTH);

        String[] cols = {"Index", "Scholar ID", "Applicant ID", "Full Name", "Purok/Zone", "School", "GWA", "Grant", "Stipend Status"};
        dirTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        dirTable = new ModernTable(dirTableModel);

        // Status badge styling on column 8: Stipend Status
        dirTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
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

        dirTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = dirTable.getSelectedRow();
                if (row >= 0 && row < dirTableModel.getRowCount()) {
                    String schId = (String) dirTableModel.getValueAt(row, 1);
                    Scholar s = manager.getScholarDirectory().searchById(schId);
                    if (s != null) {
                        displayScholarDetails(s);
                    }
                }
            }
        });

        JScrollPane tblScroll = ModernTable.createScrollPane(dirTable);
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

        lblDetailTitle = new JLabel("SCHOLAR DIRECTORY & BST NODE DOSSIER");
        lblDetailTitle.setFont(Theme.FONT_HEADER_MD);
        lblDetailTitle.setForeground(Theme.TEXT_TITLE);

        lblDetailSubtitle = new JLabel("Official scholarship profile, academic standing, and verified documents");
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

        btnDetailDrop = Theme.createStyledButton("Drop & Promote Waitlist", Theme.DANGER, Color.WHITE);
        btnDetailDrop.addActionListener(e -> {
            if (currentSelectedScholar != null) {
                dropScholar(currentSelectedScholar);
            }
        });
        actionGroup.add(btnDetailDrop);

        JButton btnClose = new JButton("Close Preview");
        btnClose.setFont(Theme.FONT_SMALL_BOLD);
        btnClose.setForeground(Theme.TEXT_MUTED);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> {
            detailCard.setVisible(false);
            currentSelectedScholar = null;
            dirTable.clearSelection();
            revalidate();
            repaint();
        });
        actionGroup.add(btnClose);

        topHeader.add(actionGroup, BorderLayout.EAST);
        card.add(topHeader, BorderLayout.NORTH);

        // Body: 3x3 Grid
        JPanel body = new JPanel(new GridLayout(3, 3, 20, 10));
        body.setOpaque(false);

        valScholarId = new JLabel("-");
        valAppId = new JLabel("-");
        valName = new JLabel("-");
        valContact = new JLabel("-");
        valSchool = new JLabel("-");
        valYear = new JLabel("-");
        valPurok = new JLabel("-");
        valGwa = new JLabel("-");
        valStatus = new JLabel("-");

        body.add(createDetailField("SCHOLAR ID", valScholarId));
        body.add(createDetailField("APPLICANT ID", valAppId));
        body.add(createDetailField("FULL NAME", valName));
        body.add(createDetailField("CONTACT & EMAIL", valContact));
        body.add(createDetailField("SCHOOL / UNIVERSITY", valSchool));
        body.add(createDetailField("YEAR LEVEL", valYear));
        body.add(createDetailField("BARANGAY PUROK", valPurok));
        body.add(createDetailField("ACADEMIC GWA", valGwa));
        body.add(createDetailField("STIPEND STATUS", valStatus));

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
        lblDetailSubtitle.setText(String.format("Official Scholar • Term: %s • Grant: %s • %s • %s",
                s.getSemester(), s.getFormattedGrant(), s.getSchool(), s.getPurok()));

        valScholarId.setText(s.getScholarId());
        valAppId.setText(s.getApplicantId());
        valName.setText(s.getFullName());
        valContact.setText(s.getEmail() + " | " + s.getContactNumber());
        valSchool.setText(s.getSchool());
        valYear.setText(s.getYearLevel());
        valPurok.setText(s.getPurok());
        valGwa.setText(s.getFormattedGwa());

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
        } else {
            valStatus.setText("UNCLAIMED (Pending Payout)");
            valStatus.setForeground(Theme.WARNING);

            lblDetailBadge.setText("  STIPEND UNCLAIMED  ");
            lblDetailBadge.setBackground(Theme.WARNING_BG);
            lblDetailBadge.setForeground(Theme.WARNING);
            lblDetailBadge.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(Theme.WARNING, 1),
                    new EmptyBorder(4, 10, 4, 10)
            ));
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

    private void executeBinarySearch() {
        String query = txtSearchId.getText().trim();
        if (query.isEmpty()) {
            refreshDirectory();
            return;
        }

        // Direct BST O(log n) lookup
        Scholar directMatch = manager.getScholarDirectory().searchById(query);
        if (directMatch != null) {
            dirTableModel.setRowCount(0);
            addScholarRow(1, directMatch);
            dirTable.setRowSelectionInterval(0, 0);
            displayScholarDetails(directMatch);
            JOptionPane.showMessageDialog(this,
                    String.format("Found Scholar via BST search in O(log n) time:\n\nID: %s\nName: %s\nPurok: %s\nSchool: %s\nStatus: %s",
                            directMatch.getScholarId(), directMatch.getFullName(), directMatch.getPurok(), directMatch.getSchool(),
                            directMatch.isStipendDisbursed() ? "Claimed" : "Unclaimed"),
                    "BST Search Match Found", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Binary Search on Name
        List<Scholar> sortedByName = new ArrayList<>(manager.getAllScholarsFromBST());
        List<Applicant> appList = new ArrayList<>(sortedByName);
        AlgorithmUtils.quickSortByName(appList, true);

        int idx = AlgorithmUtils.binarySearchByName(appList, query);
        if (idx >= 0) {
            Scholar found = (Scholar) appList.get(idx);
            dirTableModel.setRowCount(0);
            addScholarRow(1, found);
            dirTable.setRowSelectionInterval(0, 0);
            displayScholarDetails(found);
            JOptionPane.showMessageDialog(this,
                    "Binary Search found matching scholar: " + found.getFullName() + " (" + found.getScholarId() + ")",
                    "Binary Search Result", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No scholar found matching query: \"" + query + "\" via BST or Binary Search.",
                    "Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void dropSelectedScholar() {
        int row = dirTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a scholar from the table to drop.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String schId = (String) dirTableModel.getValueAt(row, 1);
        Scholar scholar = manager.getScholarDirectory().searchById(schId);
        if (scholar == null) return;

        dropScholar(scholar);
    }

    private void dropScholar(Scholar scholar) {
        if (scholar == null) return;

        int waitlistSize = manager.getWaitlistQueue().size();
        Applicant nextWaitlist = manager.getWaitlistQueue().peek();

        String prompt = String.format("Confirm dropping scholar:\n\nScholar: %s (%s)\nPurok: %s\n\nWaitlist Queue Status: %d students in queue.\n%s\n\nProceed with removal?",
                scholar.getFullName(), scholar.getScholarId(), scholar.getPurok(), waitlistSize,
                (nextWaitlist != null ? "Next in line to be auto-promoted: " + nextWaitlist.getFullName() + " (" + nextWaitlist.getPurok() + ")" : "Waitlist is empty; slot will remain vacant."));

        int confirm = JOptionPane.showConfirmDialog(this, prompt, "Confirm Drop & Promotion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Scholar promoted = manager.dropScholarAndPromoteWaitlist(scholar.getScholarId(), "Dropped / Disqualified");
            if (promoted != null) {
                JOptionPane.showMessageDialog(this,
                        String.format("Scholar %s dropped from BST.\n\nAUTOMATIC PROMOTION TRIGGERED:\n%s was dequeued from Waitlist Queue and promoted to Slot %s!",
                                scholar.getFullName(), promoted.getFullName(), promoted.getScholarId()),
                        "Waitlist Auto-Promotion Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Scholar " + scholar.getFullName() + " dropped from directory.", "Scholar Dropped", JOptionPane.INFORMATION_MESSAGE);
            }
            detailCard.setVisible(false);
            currentSelectedScholar = null;
            refreshDirectory();
        }
    }

    private void addScholarRow(int index, Scholar s) {
        dirTableModel.addRow(new Object[]{
                "#" + index,
                s.getScholarId(),
                s.getApplicantId(),
                s.getFullName(),
                s.getPurok(),
                s.getSchool(),
                s.getFormattedGwa(),
                s.getFormattedGrant(),
                s.isStipendDisbursed() ? "CLAIMED" : "UNCLAIMED"
        });
    }

    public void refreshDirectory() {
        ScholarBST bst = manager.getScholarDirectory();
        cardTreeSize.setValue(bst.size() + " Active Scholars");

        ScholarBST.BSTNode root = bst.getRoot();
        if (root != null && root.scholar != null) {
            cardTreeRoot.setValue(root.getKey());
            cardTreeRoot.setSubtitle(root.scholar.getFullName());
        } else {
            cardTreeRoot.setValue("Tree Empty");
            cardTreeRoot.setSubtitle("No scholars stored in BST");
        }

        int travIdx = cmbTraversal != null ? cmbTraversal.getSelectedIndex() : 0;
        List<Scholar> list;
        if (travIdx == 1) {
            list = bst.preOrderTraversal();
        } else if (travIdx == 2) {
            list = bst.postOrderTraversal();
        } else {
            list = bst.inOrderTraversal();
        }

        String filterText = (txtSearchId != null) ? txtSearchId.getText().trim().toLowerCase() : "";
        if (!filterText.isEmpty()) {
            List<Scholar> filtered = new ArrayList<>();
            for (Scholar s : list) {
                if (s.getScholarId().toLowerCase().contains(filterText) ||
                    s.getApplicantId().toLowerCase().contains(filterText) ||
                    s.getFullName().toLowerCase().contains(filterText) ||
                    s.getPurok().toLowerCase().contains(filterText) ||
                    s.getSchool().toLowerCase().contains(filterText)) {
                    filtered.add(s);
                }
            }
            list = filtered;
        }

        String previouslySelectedScholarId = (currentSelectedScholar != null) ? currentSelectedScholar.getScholarId() : null;

        dirTableModel.setRowCount(0);
        int idx = 1;
        int reselectRow = -1;
        Scholar reselectScholar = null;

        for (int i = 0; i < list.size(); i++) {
            Scholar s = list.get(i);
            if (previouslySelectedScholarId != null && previouslySelectedScholarId.equals(s.getScholarId())) {
                reselectRow = i;
                reselectScholar = s;
            }
            addScholarRow(idx++, s);
        }

        if (reselectRow >= 0 && reselectScholar != null) {
            dirTable.setRowSelectionInterval(reselectRow, reselectRow);
            displayScholarDetails(reselectScholar);
        }
    }
}
