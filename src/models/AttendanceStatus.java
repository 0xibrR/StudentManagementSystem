package models;

public enum AttendanceStatus {
    PRESENT("Present", "PRESENT"),
    ABSENT("Absent", "ABSENT"),
    LATE("Late", "LATE"),
    EXCUSED("Excused absence", "EXCUSED");

    private final String label;
    private final String code;

    AttendanceStatus(String label, String code) {
        this.label = label;
        this.code = code;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return ui.I18n.text(label); }
}
