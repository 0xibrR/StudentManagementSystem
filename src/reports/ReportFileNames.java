package reports;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Builds consistent, filesystem-safe default names for exported reports. */
final class ReportFileNames {
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private ReportFileNames() {}

    static String create(String reportType, String subjectName, String extension) {
        StringBuilder name = new StringBuilder(clean(reportType));
        if (subjectName != null && !subjectName.isBlank()) {
            name.append('_').append(clean(subjectName));
        }
        name.append('_').append(LocalDateTime.now().format(TIMESTAMP));

        String safeExtension = extension == null ? "" : extension.replaceFirst("^\\.+", "");
        if (!safeExtension.isBlank()) name.append('.').append(safeExtension);
        return name.toString();
    }

    private static String clean(String value) {
        if (value == null || value.isBlank()) return "Report";
        String cleaned = value.trim()
                .replaceAll("[<>:\"/\\\\|?*\\p{Cntrl}]", "")
                .replaceAll("\\s+", "_")
                .replaceAll("[. ]+$", "");
        return cleaned.isBlank() ? "Report" : cleaned;
    }
}
