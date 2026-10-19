package com.barangay.scholarship.ui.components;

import com.barangay.scholarship.model.ApplicationStatus;
import com.barangay.scholarship.ui.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Modern styled JTable with alternating row striping, padded cells, and custom headers.
 */
public class ModernTable extends JTable {

    public ModernTable(TableModel model) {
        super(model);
        initModernStyle();
    }

    private void initModernStyle() {
        setRowHeight(36);
        setFont(Theme.FONT_BODY);
        setForeground(Theme.TEXT_MAIN);
        setBackground(Color.WHITE);
        setSelectionBackground(Theme.PRIMARY_LIGHT);
        setSelectionForeground(Theme.PRIMARY);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(Theme.BORDER);
        setIntercellSpacing(new Dimension(0, 1));
        setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = getTableHeader();
        header.setFont(Theme.FONT_BODY_BOLD);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(Theme.TEXT_TITLE);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        // Cell padding renderer
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                }
                return c;
            }
        });

        // Register custom status badge renderer
        setDefaultRenderer(ApplicationStatus.class, new StatusBadgeRenderer());
    }

    public static JScrollPane createScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }
}
