package com.barangay.scholarship.ui.components;

import com.barangay.scholarship.model.ApplicationStatus;
import com.barangay.scholarship.ui.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom table cell renderer that displays ApplicationStatus as a styled badge pill.
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(Theme.FONT_SMALL_BOLD);

        if (value instanceof ApplicationStatus) {
            ApplicationStatus status = (ApplicationStatus) value;
            applyBadgeStyle(label, status);
        } else if (value instanceof String) {
            String str = (String) value;
            if ("DISBURSED".equalsIgnoreCase(str) || "CLAIMED".equalsIgnoreCase(str)) {
                applyBadgeStyle(label, ApplicationStatus.DISBURSED);
            } else if ("UNCLAIMED".equalsIgnoreCase(str)) {
                applyBadgeStyle(label, ApplicationStatus.APPROVED);
            } else {
                label.setBackground(Theme.BG_APP);
                label.setForeground(Theme.TEXT_BODY);
            }
        }

        label.setOpaque(true);
        return label;
    }

    private void applyBadgeStyle(JLabel label, ApplicationStatus status) {
        switch (status) {
            case APPROVED:
                label.setBackground(Theme.ACCENT_BG);
                label.setForeground(Theme.ACCENT_HOVER);
                label.setText("✔ APPROVED");
                break;
            case DISBURSED:
                label.setBackground(new Color(224, 242, 254)); // Light cyan
                label.setForeground(new Color(3, 105, 161));   // Cyan 700
                label.setText("💰 DISBURSED");
                break;
            case VERIFIED:
                label.setBackground(Theme.INFO_BG);
                label.setForeground(Theme.INFO);
                label.setText("📋 VERIFIED");
                break;
            case WAITLISTED:
                label.setBackground(new Color(243, 232, 255)); // Light purple
                label.setForeground(new Color(126, 34, 206));  // Purple 700
                label.setText("⏳ WAITLISTED");
                break;
            case PENDING_VERIFICATION:
                label.setBackground(Theme.WARNING_BG);
                label.setForeground(Theme.WARNING);
                label.setText("🔍 PENDING");
                break;
            case REJECTED:
                label.setBackground(Theme.DANGER_BG);
                label.setForeground(Theme.DANGER);
                label.setText("✖ REJECTED");
                break;
        }
    }
}
