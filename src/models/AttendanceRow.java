package models;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

public class AttendanceRow {
    private final int studentId;
    private final String studentName;
    private final SimpleObjectProperty<AttendanceStatus> status = new SimpleObjectProperty<>();
    private final StringProperty notes = new SimpleStringProperty();

    public AttendanceRow(int studentId, String studentName, AttendanceStatus status, String notes) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.status.set(status);
        this.notes.set(notes);
    }

    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public SimpleObjectProperty<AttendanceStatus> statusProperty() { return status; }
    public AttendanceStatus getStatus() { return status.get(); }
    public void setStatus(AttendanceStatus value) { status.set(value); }
    public StringProperty notesProperty() { return notes; }
    public String getNotes() { return notes.get(); }
    public void setNotes(String value) { notes.set(value); }
}
