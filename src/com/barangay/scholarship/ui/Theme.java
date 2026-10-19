package com.barangay.scholarship.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern design system tokens and helper methods for the Barangay Scholarship GUI.
 */
public class Theme {
    // Brand Color Palette
    public static final Color PRIMARY = new Color(27, 54, 93);         // Deep Barangay Navy
    public static final Color PRIMARY_HOVER = new Color(38, 72, 119);
    public static final Color PRIMARY_LIGHT = new Color(238, 242, 255);
    public static final Color ACCENT = new Color(5, 150, 105);          // Emerald Green
    public static final Color ACCENT_HOVER = new Color(4, 120, 87);
    public static final Color ACCENT_BG = new Color(236, 253, 245);
    public static final Color WARNING = new Color(217, 119, 6);         // Warm Amber
    public static final Color WARNING_BG = new Color(254, 243, 199);
    public static final Color DANGER = new Color(220, 38, 38);          // Crimson
    public static final Color DANGER_BG = new Color(254, 226, 226);
    public static final Color INFO = new Color(37, 99, 235);            // Royal Blue
    public static final Color INFO_BG = new Color(239, 246, 255);

    // Neutrals
    public static final Color BG_APP = new Color(241, 245, 249);        // Slate 100
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_TITLE = new Color(15, 23, 42);       // Slate 900
    public static final Color TEXT_MAIN = TEXT_TITLE;
    public static final Color TEXT_BODY = new Color(51, 65, 85);        // Slate 700
    public static final Color TEXT_MUTED = new Color(100, 116, 139);    // Slate 500
    public static final Color BORDER = new Color(226, 232, 240);        // Slate 200

    // Typography
    private static final String FONT_NAME = "Segoe UI";
    public static final Font FONT_HEADER_LG = new Font(FONT_NAME, Font.BOLD, 22);
    public static final Font FONT_HEADER_MD = new Font(FONT_NAME, Font.BOLD, 17);
    public static final Font FONT_HEADER_SM = new Font(FONT_NAME, Font.BOLD, 14);
    public static final Font FONT_BODY = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FONT_NAME, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(FONT_NAME, Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font(FONT_NAME, Font.BOLD, 11);
    public static final Font FONT_STAT_VAL = new Font(FONT_NAME, Font.BOLD, 24);

    public static Border createCardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        );
    }

    public static Border createPanelBorder() {
        return new EmptyBorder(16, 20, 20, 20);
    }

    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(createCardBorder());
        panel.setLayout(new BorderLayout());
        return panel;
    }
}
