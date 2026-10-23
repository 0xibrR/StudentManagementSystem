package reports;

import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

/** Shared embedded Unicode fonts and RTL cell setup for generated reports. */
final class PdfFonts {
    private static final String WINDOWS_FONTS = System.getenv().getOrDefault("WINDIR", "C:/Windows") + "/Fonts/";

    private PdfFonts() {}

    static Font title(Color color) { return create("arialbd.ttf", 22, Font.BOLD, color); }
    static Font subtitle(Color color) { return create("arialbd.ttf", 16, Font.BOLD, color); }
    static Font header(Color color) { return create("arialbd.ttf", 12, Font.BOLD, color); }
    static Font normal(Color color) { return create("arial.ttf", 11, Font.NORMAL, color); }
    static Font footer(Color color) { return create("ariali.ttf", 10, Font.ITALIC, color); }

    static void enableRtl(PdfPCell cell, String text) {
        if (text != null && text.codePoints().anyMatch(
                codePoint -> Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.ARABIC)) {
            cell.setRunDirection(com.lowagie.text.pdf.PdfWriter.RUN_DIRECTION_RTL);
        }
    }

    private static Font create(String fileName, float size, int style, Color color) {
        File file = new File(WINDOWS_FONTS + fileName);
        if (!file.isFile()) {
            throw new IllegalStateException("Required Unicode font was not found: " + file);
        }
        try {
            BaseFont baseFont = BaseFont.createFont(file.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(baseFont, size, style, color);
        } catch (com.lowagie.text.DocumentException | IOException e) {
            throw new IllegalStateException("Could not load the Unicode report font.", e);
        }
    }
}
