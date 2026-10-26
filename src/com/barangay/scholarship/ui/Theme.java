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
    private static final String FONT_NAME = resolveAppleSystemFont();
    public static final Font FONT_HEADER_LG = new Font(FONT_NAME, Font.BOLD, 22);
    public static final Font FONT_HEADER_MD = new Font(FONT_NAME, Font.BOLD, 17);
    public static final Font FONT_HEADER_SM = new Font(FONT_NAME, Font.BOLD, 14);
    public static final Font FONT_BODY = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FONT_NAME, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(FONT_NAME, Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font(FONT_NAME, Font.BOLD, 11);
    public static final Font FONT_STAT_VAL = new Font(FONT_NAME, Font.BOLD, 24);

    public static String getFontName() {
        return FONT_NAME;
    }

    private static String resolveAppleSystemFont() {
        String[] candidates = {".AppleSystemUIFont", "SF Pro Text", "SF Pro Display", "Helvetica Neue", "Helvetica", "Segoe UI", "SansSerif"};
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] available = ge.getAvailableFontFamilyNames();
            for (String candidate : candidates) {
                for (String av : available) {
                    if (av.equalsIgnoreCase(candidate)) {
                        return av;
                    }
                }
            }
        } catch (Exception ignored) {}
        return "SansSerif";
    }

    public static Border createCardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        );
    }

    public static Border createPanelBorder() {
        return new EmptyBorder(16, 20, 20, 20);
    }

    public static boolean isDarkColor(Color c) {
        if (c == null) return false;
        double luminance = (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255.0;
        return luminance < 0.65;
    }

    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    Color currentBg = getBackground() != null ? getBackground() : bg;
                    Color fill = (getModel().isPressed() && getModel().isArmed())
                            ? currentBg.darker()
                            : currentBg;

                    g2.setColor(fill);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                    Color currentBorder = currentBg.darker();
                    g2.setColor(currentBorder);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                    Color textFg = getForeground() != null ? getForeground() : fg;
                    if (!isEnabled()) {
                        boolean isDark = isDarkColor(fill);
                        textFg = isDark ? Color.WHITE : new Color(148, 163, 184);
                    }

                    g2.setFont(getFont());
                    g2.setColor(textFg);
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                    int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(getText(), textX, textY);
                } finally {
                    g2.dispose();
                }
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTextField createRoundedTextField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    g2.setColor(getBackground() != null ? getBackground() : Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                    super.paintComponent(g2);

                    if (getText().isEmpty() && placeholder != null && !hasFocus()) {
                        g2.setColor(TEXT_MUTED);
                        g2.setFont(getFont());
                        FontMetrics fm = g2.getFontMetrics();
                        Insets insets = getInsets();
                        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                        g2.drawString(placeholder, insets.left, y);
                    }
                } finally {
                    g2.dispose();
                }
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color borderColor = hasFocus() ? PRIMARY : BORDER;
                    g2.setColor(borderColor);
                    g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1.0f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                } finally {
                    g2.dispose();
                }
            }
        };
        tf.setOpaque(false);
        tf.setBackground(Color.WHITE);
        tf.setForeground(TEXT_MAIN);
        tf.setCaretColor(TEXT_MAIN);
        tf.setFont(FONT_BODY);
        tf.setBorder(new EmptyBorder(8, 16, 8, 16));
        return tf;
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
     * while preserving its natural preferred height. Uses 32767 (Short.MAX_VALUE) to prevent
     * 32-bit integer overflow in Swing SizeRequirements calculations.
     */
    public static JPanel createFullWidthCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(32767, getPreferredSize().height);
            }
        };
        panel.setBackground(CARD_BG);
        panel.setBorder(createCardBorder());
        panel.setLayout(new BorderLayout());
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
        Component wrappedView = view;
        if (view instanceof JComponent && !(view instanceof Scrollable)) {
            ScrollablePanel wrapper = new ScrollablePanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.add(view, BorderLayout.CENTER);
            wrappedView = wrapper;
        }

        JScrollPane sp = new JScrollPane(wrappedView);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getViewport().setBackground(BG_APP);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        enableSmoothMouseWheel(sp);
        return sp;
    }

    /**
     * Recursively enables mouse and trackpad wheel scrolling so that events occurring
     * on child components (such as text fields, checkboxes, labels, and panels)
     * properly scroll the enclosing JScrollPane.
     */
    public static void enableSmoothMouseWheel(JScrollPane scrollPane) {
        if (scrollPane == null) return;
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        java.awt.event.ContainerListener containerListener = new java.awt.event.ContainerListener() {
            @Override
            public void componentAdded(java.awt.event.ContainerEvent e) {
                attachWheelRecursively(e.getChild(), scrollPane, this);
            }
            @Override
            public void componentRemoved(java.awt.event.ContainerEvent e) {}
        };

        if (scrollPane.getViewport() != null) {
            Component view = scrollPane.getViewport().getView();
            if (view != null) {
                attachWheelRecursively(view, scrollPane, containerListener);
            }
            scrollPane.getViewport().addContainerListener(containerListener);
        }
    }

    private static void attachWheelRecursively(Component comp, JScrollPane scrollPane, java.awt.event.ContainerListener listener) {
        if (comp == null || comp == scrollPane) return;

        // If the component is another JScrollPane, do not cross boundary
        if (comp instanceof JScrollPane && comp != scrollPane) {
            return;
        }

        // Add mouse wheel listener forwarding event to the target scrollpane
        comp.addMouseWheelListener(e -> {
            scrollPane.dispatchEvent(SwingUtilities.convertMouseEvent(comp, e, scrollPane));
        });

        if (comp instanceof Container) {
            Container c = (Container) comp;
            c.addContainerListener(listener);
            for (Component child : c.getComponents()) {
                attachWheelRecursively(child, scrollPane, listener);
            }
        }
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
