package sendfile.client;

import java.awt.Color;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * Message styling with eye-friendly colors and fonts
 * Updated for better readability and reduced eye strain
 */
public class MessageStyle {

    // Eye-friendly color palette
    public static final Color SOFT_BLUE = new Color(70, 130, 180);      // Steel Blue
    public static final Color SOFT_GREEN = new Color(34, 139, 34);      // Forest Green
    public static final Color SOFT_PURPLE = new Color(138, 43, 226);    // Blue Violet
    public static final Color SOFT_ORANGE = new Color(255, 140, 0);     // Dark Orange
    public static final Color SOFT_RED = new Color(220, 20, 60);        // Crimson
    public static final Color SOFT_GRAY = new Color(105, 105, 105);     // Dim Gray
    public static final Color DARK_BLUE = new Color(25, 25, 112);       // Midnight Blue
    public static final Color ENCRYPTION_GREEN = new Color(0, 100, 0);  // Dark Green

    /**
     * Create message content style with improved readability
     */
    public static AttributeSet styleMessageContent(Color color, String fontFamily, int size) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground,
                getSoftColor(color));

        // Use more readable fonts
        String readableFont = getReadableFont(fontFamily);
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, readableFont);
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_LEFT);
        aset = sc.addAttribute(aset, StyleConstants.FontSize, size);

        // Add slight boldness for better readability
        if (size >= 12) {
            aset = sc.addAttribute(aset, StyleConstants.Bold, true);
        }

        return aset;
    }

    /**
     * Convert harsh colors to softer, eye-friendly alternatives
     */
    private static Color getSoftColor(Color originalColor) {
        // Map common harsh colors to softer alternatives
        if (originalColor == Color.MAGENTA || originalColor == Color.PINK) {
            return SOFT_PURPLE;
        } else if (originalColor == Color.BLUE) {
            return SOFT_BLUE;
        } else if (originalColor == Color.GREEN) {
            return SOFT_GREEN;
        } else if (originalColor == Color.ORANGE) {
            return SOFT_ORANGE;
        } else if (originalColor == Color.RED) {
            return SOFT_RED;
        } else if (originalColor == Color.BLACK) {
            return new Color(50, 50, 50); // Dark gray instead of pure black
        } else if (originalColor == Color.YELLOW) {
            return new Color(184, 134, 11); // Dark golden yellow
        } else {
            // For other colors, slightly darken them if they're too bright
            return darkenColor(originalColor, 0.8f);
        }
    }

    /**
     * Choose more readable font alternatives
     */
    private static String getReadableFont(String originalFont) {
        switch (originalFont.toLowerCase()) {
            case "impact":
                return "Arial"; // Impact can be hard to read, use Arial instead
            case "consolas":
                return "Courier New"; // Fallback for monospace
            default:
                return originalFont;
        }
    }

    /**
     * Darken a color by a factor to make it easier on the eyes
     */
    private static Color darkenColor(Color color, float factor) {
        int red = Math.max(0, (int) (color.getRed() * factor));
        int green = Math.max(0, (int) (color.getGreen() * factor));
        int blue = Math.max(0, (int) (color.getBlue() * factor));
        return new Color(red, green, blue);
    }

    /**
     * Predefined styles for common message types
     */
    public static AttributeSet getHeaderStyle() {
        return styleMessageContent(DARK_BLUE, "Arial", 13);
    }

    public static AttributeSet getEncryptedMessageStyle() {
        return styleMessageContent(ENCRYPTION_GREEN, "Arial", 12);
    }

    public static AttributeSet getSystemMessageStyle() {
        return styleMessageContent(SOFT_GRAY, "Arial", 11);
    }

    public static AttributeSet getErrorMessageStyle() {
        return styleMessageContent(SOFT_RED, "Arial", 12);
    }

    public static AttributeSet getWarningMessageStyle() {
        return styleMessageContent(SOFT_ORANGE, "Arial", 12);
    }
}