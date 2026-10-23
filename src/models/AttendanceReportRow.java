package models;

import java.time.LocalDate;

public record AttendanceReportRow(LocalDate date, int studentId, String studentName,
                                  String statusCode, String notes) {}
