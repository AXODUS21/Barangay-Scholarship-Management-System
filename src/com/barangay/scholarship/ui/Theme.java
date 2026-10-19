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

    /**
     * Card panel that spans 100% of the parent container's width in a BoxLayout(Y_AXIS)
     * while preserving its natural preferred height.
     */
    public static JPanel createFullWidthCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        panel.setBackground(CARD_BG);
        panel.setBorder(createCardBorder());
        panel.setLayout(new BorderLayout());
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    public static JPanel createFullWidthCardPanel(LayoutManager layout) {
        JPanel panel = createFullWidthCardPanel();
        panel.setLayout(layout);
        return panel;
    }

    /**
     * Creates a vertical strut with Component.LEFT_ALIGNMENT so it does not cause
     * BoxLayout horizontal alignment shifts when mixed with left-aligned components.
     */
    public static Component createVerticalStrut(int height) {
        JComponent strut = (JComponent) Box.createVerticalStrut(height);
        strut.setAlignmentX(Component.LEFT_ALIGNMENT);
        return strut;
    }

    /**
     * A panel that implements Scrollable and forces width tracking in a JViewport,
     * ensuring child components span 100% of the horizontal scroll viewport width.
     */
    public static class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel() {
            super();
        }

        public ScrollablePanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    public static JScrollPane createBorderlessScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_APP);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        return sp;
    }

    public static JSplitPane createSplitPane(int orientation, Component left, Component right, double resizeWeight) {
        JSplitPane split = new JSplitPane(orientation, left, right);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setOpaque(false);
        split.setContinuousLayout(true);
        split.setResizeWeight(resizeWeight);
        return split;
    }
}
