package com.transport.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * UITheme.java - Central Design System for MOT Logistics
 *
 * Provides a modern, cohesive enterprise palette and utility styling
 * for consistent typography, borders, table styling, and buttons.
 */
public class UITheme {

    // --- Enterprise Color Palette ---
    public static final Color BRAND_NAVY       = new Color(15, 23, 42);     // #0F172A - Header / Slate Midnight
    public static final Color BRAND_BLUE       = new Color(30, 58, 138);    // #1E3A8A - Primary Brand Navy
    public static final Color ACCENT_BLUE      = new Color(37, 99, 235);    // #2563EB - Action Blue (Login/Search)
    public static final Color SUCCESS_GREEN    = new Color(5, 150, 105);    // #059669 - Emerald Green (Submit / Add)
    public static final Color UPDATE_BLUE      = new Color(2, 132, 199);    // #0284C7 - Sky Blue (Update)
    public static final Color DANGER_RED       = new Color(220, 38, 38);    // #DC2626 - Crimson (Delete / Logout)
    public static final Color SECONDARY_GRAY   = new Color(71, 85, 105);    // #475569 - Slate (Clear / Filter)
    public static final Color AMBER_ORANGE     = new Color(217, 119, 6);    // #D97706 - Amber (Export PDF)

    public static final Color BG_LIGHT         = new Color(248, 250, 252);  // #F8FAFC - Main background
    public static final Color CARD_BG          = Color.WHITE;               // #FFFFFF - Card / panel surface
    public static final Color BORDER_COLOR     = new Color(226, 232, 240);  // #E2E8F0 - Soft border
    public static final Color TEXT_DARK        = new Color(15, 23, 42);     // #0F172A - Primary text
    public static final Color TEXT_MUTED       = new Color(100, 116, 139);  // #64748B - Secondary text

    // --- Fonts ---
    public static final Font FONT_TITLE        = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER       = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY         = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL        = new Font("Segoe UI", Font.PLAIN, 11);

    // Private constructor
    private UITheme() {}

    /** Style JTable with clean modern headers, row height, and zebra striping */
    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(FONT_BODY);
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(new Color(219, 234, 254)); // Soft light blue selection
        table.setSelectionForeground(BRAND_NAVY);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 32));
        header.setBackground(BRAND_BLUE);
        header.setForeground(Color.WHITE);
        header.setFont(FONT_HEADER);
        header.setOpaque(true);

        // Center-align default headers
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    /** Create a styled card border with subtle outline */
    public static Border createCardBorder(String title) {
        Border line = new LineBorder(BORDER_COLOR, 1, true);
        Border titled = BorderFactory.createTitledBorder(line, " " + title + " ",
                0, 0, FONT_BODY_BOLD, BRAND_NAVY);
        return new CompoundBorder(titled, new EmptyBorder(10, 12, 10, 12));
    }

    /** Create standardized text input border */
    public static Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        );
    }
}
