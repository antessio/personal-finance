package antessio.personalfinance.infrastructure.report;

import java.awt.Color;

/**
 * Color palette for professional PDF report design.
 * Provides consistent colors across all report components.
 */
public class PdfColorPalette {

    // Primary colors
    public static final Color PRIMARY_DARK = new Color(44, 62, 80);      // Deep blue #2C3E50
    public static final Color PRIMARY_TEAL = new Color(22, 160, 133);    // Teal #16A085

    // Status colors
    public static final Color SUCCESS_GREEN = new Color(39, 174, 96);    // Green #27AE60
    public static final Color WARNING_AMBER = new Color(243, 156, 18);   // Amber #F39C12
    public static final Color DANGER_RED = new Color(231, 76, 60);       // Red #E74C3C

    // Neutral colors
    public static final Color LIGHT_GRAY = new Color(236, 240, 241);     // #ECF0F1
    public static final Color MEDIUM_GRAY = new Color(149, 165, 166);    // #95A5A6
    public static final Color DARK_GRAY = new Color(52, 73, 94);         // #34495E
    public static final Color WHITE = Color.WHITE;
    public static final Color BLACK = Color.BLACK;

    // Chart colors (for JFreeChart)
    public static final Color[] CHART_COLORS = {
        PRIMARY_TEAL,
        new Color(52, 152, 219),   // Blue
        SUCCESS_GREEN,
        new Color(155, 89, 182),   // Purple
        WARNING_AMBER,
        new Color(230, 126, 34),   // Orange
        DANGER_RED,
        new Color(241, 196, 15),   // Yellow
        new Color(127, 140, 141),  // Gray-blue
        new Color(22, 160, 133)    // Teal variant
    };

    // Expense category colors (darker/warmer tones)
    public static final Color EXPENSE_PRIMARY = new Color(231, 76, 60);   // Red
    public static final Color EXPENSE_SECONDARY = new Color(230, 126, 34); // Orange

    // Income category colors (green tones)
    public static final Color INCOME_PRIMARY = new Color(39, 174, 96);    // Green
    public static final Color INCOME_SECONDARY = new Color(22, 160, 133); // Teal

    // Savings category colors (blue tones)
    public static final Color SAVINGS_PRIMARY = new Color(52, 152, 219);  // Blue
    public static final Color SAVINGS_SECONDARY = new Color(41, 128, 185); // Darker blue

    /**
     * Get color for budget status based on utilization percentage.
     *
     * @param percentage Budget utilization percentage (0-100+)
     * @return Color indicating status (green, amber, or red)
     */
    public static Color getBudgetStatusColor(double percentage) {
        if (percentage < 80.0) {
            return SUCCESS_GREEN;
        } else if (percentage <= 100.0) {
            return WARNING_AMBER;
        } else {
            return DANGER_RED;
        }
    }

    /**
     * Get alternating row color for tables (for zebra striping).
     *
     * @param rowIndex Row index (0-based)
     * @return White for even rows, light gray for odd rows
     */
    public static Color getAlternatingRowColor(int rowIndex) {
        return rowIndex % 2 == 0 ? WHITE : LIGHT_GRAY;
    }

    /**
     * Convert java.awt.Color to PDFBox color array (RGB values 0-1).
     *
     * @param color Java AWT color
     * @return Array of [red, green, blue] with values 0.0-1.0
     */
    public static float[] toPdfColor(Color color) {
        return new float[] {
            color.getRed() / 255f,
            color.getGreen() / 255f,
            color.getBlue() / 255f
        };
    }
}
