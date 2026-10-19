package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.algorithms.AlgorithmUtils;
import com.barangay.scholarship.datastructures.ScholarBST;
import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.Scholar;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.Theme;
import com.barangay.scholarship.ui.components.ModernTable;
import com.barangay.scholarship.ui.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // BST Metrics
        JPanel statsGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(2000, 90));

        cardTreeSize = new StatCard("BST Active Nodes", "0 Scholars", "Indexed in Binary Search Tree", Theme.PRIMARY);
        cardTreeRoot = new StatCard("BST Root Node", "None", "Balanced Key Anchor", Theme.ACCENT);

        statsGrid.add(cardTreeSize);
        statsGrid.add(cardTreeRoot);
        centerPanel.add(statsGrid);
        centerPanel.add(Box.createVerticalStrut(16));

        // Toolbar Card: Search + Traversal Selector + Drop Scholar Button
        JPanel toolbarCard = Theme.createCardPanel();
        toolbarCard.setMaximumSize(new Dimension(2000, 75));
        JPanel toolRow = new JPanel(new BorderLayout(12, 0));
        toolRow.setOpaque(false);

        // Left: Binary Search Input
        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBox.setOpaque(false);

        JLabel lblSearch = new JLabel("Binary Search (ID / Name):");
        lblSearch.setFont(Theme.FONT_BODY_BOLD);
        txtSearchId = new JTextField(12);
        txtSearchId.setPreferredSize(new Dimension(140, 32));

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> executeBinarySearch());

        JLabel lblTrav = new JLabel("Tree Traversal:");
        lblTrav.setFont(Theme.FONT_BODY_BOLD);
        String[] traversals = {"In-Order (Sorted by ID)", "Pre-Order (Root First)", "Post-Order (Leaves First)"};
        cmbTraversal = new JComboBox<>(traversals);
        cmbTraversal.setPreferredSize(new Dimension(190, 32));
        cmbTraversal.addActionListener(e -> refreshDirectory());

        leftBox.add(lblSearch);
        leftBox.add(txtSearchId);
        leftBox.add(btnSearch);
        leftBox.add(Box.createHorizontalStrut(10));
        leftBox.add(lblTrav);
        leftBox.add(cmbTraversal);
        toolRow.add(leftBox, BorderLayout.WEST);

        // Right: Drop & Auto-Promote Button
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        JButton btnDrop = Theme.createStyledButton("Drop & Promote Waitlist", Theme.DANGER, Color.WHITE);
        btnDrop.addActionListener(e -> dropSelectedScholar());

        JButton btnReset = new JButton("Reset View");
        btnReset.addActionListener(e -> {
            txtSearchId.setText("");
            refreshDirectory();
        });

        rightBox.add(btnDrop);
        rightBox.add(btnReset);
        toolRow.add(rightBox, BorderLayout.EAST);

        toolbarCard.add(toolRow, BorderLayout.CENTER);
        centerPanel.add(toolbarCard);
        centerPanel.add(Box.createVerticalStrut(16));

        // Directory Table Card
        JPanel tableCard = Theme.createCardPanel();
        JLabel tblTitle = new JLabel("BST Scholar Directory & Traversal Output");
        tblTitle.setFont(Theme.FONT_HEADER_SM);
        tblTitle.setForeground(Theme.TEXT_TITLE);

        String[] cols = {"Index", "Scholar ID", "Applicant ID", "Full Name", "Purok/Zone", "School", "GWA", "Grant", "Stipend Status"};
        dirTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        dirTable = new ModernTable(dirTableModel);
        JScrollPane tblScroll = ModernTable.createScrollPane(dirTable);

        tableCard.add(tblTitle, BorderLayout.NORTH);
        tableCard.add(tblScroll, BorderLayout.CENTER);
        centerPanel.add(tableCard);

        add(centerPanel, BorderLayout.CENTER);
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

        int waitlistSize = manager.getWaitlistQueue().size();
        Applicant nextWaitlist = manager.getWaitlistQueue().peek();

        String prompt = String.format("Confirm dropping scholar:\n\nScholar: %s (%s)\nPurok: %s\n\nWaitlist Queue Status: %d students in queue.\n%s\n\nProceed with removal?",
                scholar.getFullName(), scholar.getScholarId(), scholar.getPurok(), waitlistSize,
                (nextWaitlist != null ? "Next in line to be auto-promoted: " + nextWaitlist.getFullName() + " (" + nextWaitlist.getPurok() + ")" : "Waitlist is empty; slot will remain vacant."));

        int confirm = JOptionPane.showConfirmDialog(this, prompt, "Confirm Drop & Promotion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Scholar promoted = manager.dropScholarAndPromoteWaitlist(schId, "Dropped / Disqualified");
            if (promoted != null) {
                JOptionPane.showMessageDialog(this,
                        String.format("Scholar %s dropped from BST.\n\nAUTOMATIC PROMOTION TRIGGERED:\n%s was dequeued from Waitlist Queue and promoted to Slot %s!",
                                scholar.getFullName(), promoted.getFullName(), promoted.getScholarId()),
                        "Waitlist Auto-Promotion Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Scholar " + scholar.getFullName() + " dropped from directory.", "Scholar Dropped", JOptionPane.INFORMATION_MESSAGE);
            }
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

        dirTableModel.setRowCount(0);
        int idx = 1;
        for (Scholar s : list) {
            addScholarRow(idx++, s);
        }
    }
}
