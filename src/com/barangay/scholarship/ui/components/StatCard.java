package com.barangay.scholarship.ui.components;

import com.barangay.scholarship.ui.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * Modern KPI / Metric card with color accent, title, value, and subtitle.
 */
public class StatCard extends JPanel {
    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;

    public StatCard(String title, String initialValue, String subtitle, Color accentColor) {
        setLayout(new BorderLayout(8, 6));
        setBackground(Theme.CARD_BG);

        // Card Border with left accent line
        setBorder(new CompoundBorder(
                new MatteBorder(0, 4, 0, 0, accentColor),
                new CompoundBorder(
                        BorderFactory.createLineBorder(Theme.BORDER, 1),
                        new EmptyBorder(12, 16, 12, 16)
                )
        ));

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(Theme.FONT_SMALL_BOLD);
        lblTitle.setForeground(Theme.TEXT_MUTED);

        lblValue = new JLabel(initialValue);
        lblValue.setFont(Theme.FONT_STAT_VAL);
        lblValue.setForeground(Theme.TEXT_TITLE);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(Theme.FONT_SMALL);
        lblSubtitle.setForeground(Theme.TEXT_MUTED);

        add(lblTitle, BorderLayout.NORTH);
        add(lblValue, BorderLayout.CENTER);
        add(lblSubtitle, BorderLayout.SOUTH);
    }

    public void setValue(String val) {
        lblValue.setText(val);
    }

    public void setSubtitle(String sub) {
        lblSubtitle.setText(sub);
    }
}
