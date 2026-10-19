package com.barangay.scholarship.ui.views;

import com.barangay.scholarship.datastructures.ActionStack;
import com.barangay.scholarship.model.ActionRecord;
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
 * Audit Log & 1-Click Undo View.
 * Visualizes the custom generic LIFO ActionStack and provides 1-click transaction undo.
 */
public class AuditLogView extends JPanel {
    private final ScholarshipManager manager;

    // Stat Cards
    private StatCard cardStackSize;
    private StatCard cardTopAction;

    // Table
    private DefaultTableModel logTableModel;
    private ModernTable logTable;

    public AuditLogView(ScholarshipManager manager) {
        this.manager = manager;

        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        refreshLog();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Audit Log & 1-Click Undo (ActionStack)");
        title.setFont(Theme.FONT_HEADER_LG);
        title.setForeground(Theme.TEXT_TITLE);

        JLabel subtitle = new JLabel("Custom LIFO Stack: Transactions are pushed onto the stack and can be undone with a single click");
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

        // Top Metrics
        JPanel statsGrid = new JPanel(new GridLayout(1, 2, 16, 12));
        statsGrid.setOpaque(false);

        cardStackSize = new StatCard("Undo Stack Depth", "0 Actions", "Last-In, First-Out (LIFO)", Theme.PRIMARY);
        cardTopAction = new StatCard("Top of Stack (Next to Undo)", "None", "Most recent operation", Theme.WARNING);

        statsGrid.add(cardStackSize);
        statsGrid.add(cardTopAction);
        centerPanel.add(statsGrid);
        centerPanel.add(Box.createVerticalStrut(14));

        // Action Toolbar
        JPanel toolbarCard = Theme.createCardPanel();
        JPanel toolRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        toolRow.setOpaque(false);

        JLabel lblInfo = new JLabel("Staff accident protection: Reverses accidental approvals, status updates, and stipend disbursements.");
        lblInfo.setFont(Theme.FONT_BODY);
        lblInfo.setForeground(Theme.TEXT_MUTED);

        JButton btnUndo = Theme.createStyledButton("1-Click Undo Last Action", Theme.PRIMARY, Color.WHITE);
        btnUndo.setFont(Theme.FONT_HEADER_SM);
        btnUndo.addActionListener(e -> executeUndo());

        toolRow.add(btnUndo);
        toolRow.add(lblInfo);

        toolbarCard.add(toolRow, BorderLayout.CENTER);
        centerPanel.add(toolbarCard);
        centerPanel.add(Box.createVerticalStrut(14));

        // Table Card
        JPanel tableCard = Theme.createCardPanel();
        JLabel tblTitle = new JLabel("Recorded Transactions (LIFO Stack Order: Most Recent at Top)");
        tblTitle.setFont(Theme.FONT_HEADER_SM);
        tblTitle.setForeground(Theme.TEXT_TITLE);

        String[] cols = {"Stack Level", "Timestamp", "Action Type", "Target Entity", "Previous Status", "New Status", "Details"};
        logTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        logTable = new ModernTable(logTableModel);
        JScrollPane tblScroll = ModernTable.createScrollPane(logTable);

        tableCard.add(tblTitle, BorderLayout.NORTH);
        tableCard.add(tblScroll, BorderLayout.CENTER);
        centerPanel.add(tableCard);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void executeUndo() {
        ActionStack<ActionRecord> stack = manager.getActionHistory();
        if (stack.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The ActionStack is currently empty. No transactions to undo.", "Stack Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ActionRecord top = stack.peek();
        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Undo the following action from the top of the stack?\n\nAction: %s\nTarget: %s\nTime: %s\nDetails: %s",
                        top.getType().getLabel(), top.getTargetName(), top.getFormattedTime(), top.getDescription()),
                "Confirm Undo Action", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = manager.undoLastAction();
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Action successfully undone!\nSystem state and budget have been rolled back.",
                        "Undo Successful", JOptionPane.INFORMATION_MESSAGE);
                refreshLog();
            }
        }
    }

    public void refreshLog() {
        ActionStack<ActionRecord> stack = manager.getActionHistory();
        cardStackSize.setValue(stack.size() + " Actions");

        ActionRecord top = stack.peek();
        if (top != null) {
            cardTopAction.setValue(top.getType().getLabel());
            cardTopAction.setSubtitle(top.getTargetName() + " (" + top.getFormattedTime() + ")");
        } else {
            cardTopAction.setValue("Stack Empty");
            cardTopAction.setSubtitle("No actions to undo");
        }

        logTableModel.setRowCount(0);
        List<ActionRecord> list = stack.toList();
        int level = 1;
        for (ActionRecord r : list) {
            String prev = r.getPreviousStatus() != null ? r.getPreviousStatus().name() : "N/A";
            String next = r.getNewStatus() != null ? r.getNewStatus().name() : "N/A";

            logTableModel.addRow(new Object[]{
                    (level == 1 ? "TOP (#1)" : "#" + level),
                    r.getFormattedTime(),
                    r.getType().getLabel(),
                    r.getTargetName(),
                    prev,
                    next,
                    r.getDescription()
            });
            level++;
        }
    }
}
