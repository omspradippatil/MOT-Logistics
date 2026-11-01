package com.transport.ui;

import javax.swing.*;
import java.awt.*;

/**
 * ModernButton.java - Custom High-Contrast Styled Button
 *
 * Solves the issue where Java Swing / macOS native buttons ignore
 * setBackground() and render white-on-white invisible button text.
 *
 * Features:
 * - Anti-aliased smooth rounded corners (radius 8px)
 * - Guaranteed solid visible background colors across all operating systems
 * - High-contrast readable typography
 * - Interactive hover and click visual feedback
 * - Hand cursor
 */
public class ModernButton extends JButton {

    private Color normalBg;
    private Color hoverBg;
    private Color pressedBg;
    private int   radius = 8;

    public ModernButton(String text, Color bgColor) {
        this(text, bgColor, Color.WHITE);
    }

    public ModernButton(String text, Color bgColor, Color textColor) {
        super(text);
        this.normalBg  = bgColor;
        this.hoverBg   = calculateHoverColor(bgColor);
        this.pressedBg = bgColor.darker();

        setForeground(textColor);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    }

    /** Helper to brighten the hover state subtly */
    private Color calculateHoverColor(Color color) {
        int r = Math.min(255, (int)(color.getRed() * 1.15));
        int g = Math.min(255, (int)(color.getGreen() * 1.15));
        int b = Math.min(255, (int)(color.getBlue() * 1.15));
        return new Color(r, g, b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Pick color based on button state
        if (!isEnabled()) {
            g2.setColor(new Color(200, 205, 215));
        } else if (getModel().isPressed()) {
            g2.setColor(pressedBg);
        } else if (getModel().isRollover()) {
            g2.setColor(hoverBg);
        } else {
            g2.setColor(normalBg);
        }

        // Draw rounded rectangle
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        // Paint label text centered
        FontMetrics fm = g2.getFontMetrics(getFont());
        int textWidth  = fm.stringWidth(getText());
        int textHeight = fm.getAscent();
        int textX = (getWidth() - textWidth) / 2;
        int textY = (getHeight() + textHeight) / 2 - 2;

        g2.setFont(getFont());
        g2.setColor(isEnabled() ? getForeground() : new Color(130, 135, 145));
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }

    public void setColors(Color normalColor) {
        this.normalBg  = normalColor;
        this.hoverBg   = calculateHoverColor(normalColor);
        this.pressedBg = normalColor.darker();
        repaint();
    }
}
