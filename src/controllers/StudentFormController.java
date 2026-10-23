package controllers;

import database.StudentRepository;
import database.ScheduleRepository;
import ui.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import models.Session;
import models.Student;

import java.sql.Date;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StudentFormController {

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtYear;

    @FXML
    private TextField txtPhone;

    @FXML
    private DatePicker dpEnrollmentDate;

    @FXML
    private TextArea txtNotes;

    @FXML private DatePicker dpScheduleStart;
    @FXML private Label lblWeeklyAttendanceDays;
    @FXML private Label lblScheduleEffectiveFrom;
    @FXML private Label lblWeekdayHelp;
    @FXML private Button btnCancel;
    @FXML private Button btnSaveStudent;
    @FXML private CheckBox chkSunday;
    @FXML private CheckBox chkMonday;
    @FXML private CheckBox chkTuesday;
    @FXML private CheckBox chkWednesday;
    @FXML private CheckBox chkThursday;
    @FXML private CheckBox chkFriday;
    @FXML private CheckBox chkSaturday;

    private Student student;

    private HomeController homeController;
    private final StudentRepository studentRepository = new StudentRepository();
    private final ScheduleRepository scheduleRepository = new ScheduleRepository();

    @FXML
    public void initialize() {
        I18n.setText(lblWeeklyAttendanceDays, "Weekly attendance days");
        I18n.setText(lblScheduleEffectiveFrom, "Schedule effective from");
        I18n.setText(lblWeekdayHelp, "Select one or more days; attendance is only recorded on selected days.");
        I18n.setText(btnCancel, "Cancel");
        I18n.setText(btnSaveStudent, "Save student");
        weekdayChecks().forEach((day, checkBox) -> {
            String label = switch (day) {
                case 1 -> "Sunday";
                case 2 -> "Monday";
                case 3 -> "Tuesday";
                case 4 -> "Wednesday";
                case 5 -> "Thursday";
                case 6 -> "Friday";
                case 7 -> "Saturday";
                default -> throw new IllegalStateException("Unexpected weekday: " + day);
            };
            I18n.setText(checkBox, label);
        });
        dpEnrollmentDate.valueProperty().addListener((observable, oldDate, newDate) -> {
            if (newDate != null && dpScheduleStart.getValue() != null
                    && dpScheduleStart.getValue().isBefore(newDate)) {
                dpScheduleStart.setValue(newDate);
            }
        });
    }

    public void setStudent(Student student) {

        this.student = student;

        if (student != null) {

            txtName.setText(student.getName());
            txtYear.setText(String.valueOf(student.getYearOfBirth()));
            txtPhone.setText(student.getPhone());

            if (student.getEnrollmentDate() != null) {
                dpEnrollmentDate.setValue(student.getEnrollmentDate().toLocalDate());
            }

            txtNotes.setText(student.getNotes());

            try {
                Set<Integer> days = scheduleRepository.findCurrentDays(student.getId(), Session.getUserId());
                weekdayChecks().forEach((day, checkBox) -> checkBox.setSelected(days.contains(day)));
                LocalDate currentStart = scheduleRepository.findCurrentEffectiveFrom(student.getId(), Session.getUserId());
                dpScheduleStart.setValue(currentStart == null ? LocalDate.now()
                        : (currentStart.isBefore(LocalDate.now()) ? LocalDate.now() : currentStart.plusDays(1)));
            } catch (Exception exception) {
                exception.printStackTrace();
                showAlert("Database Error", "Could not load the student's attendance schedule.");
            }

        }

        if (dpScheduleStart.getValue() == null) dpScheduleStart.setValue(LocalDate.now());

    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    private void handleSave() {

        String name = txtName.getText().trim();
        String yearText = txtYear.getText().trim();
        String phone = txtPhone.getText().trim();
        String notes = txtNotes.getText().trim();
        Set<Integer> days = selectedDays();

        if (name.isEmpty()
                || yearText.isEmpty()
                || dpEnrollmentDate.getValue() == null) {

            showAlert("Error", "Please fill all required fields.");
            return;

        }

        if (days.isEmpty()) {
            showAlert("Error", "Select at least one attendance day.");
            return;
        }

        if (dpScheduleStart.getValue() == null || dpScheduleStart.getValue().isBefore(dpEnrollmentDate.getValue())) {
            showAlert("Error", "The schedule start date cannot be before the student's registration date.");
            return;
        }

        try {

            int year = Integer.parseInt(yearText);

            if (year < 1900 || year > 2100) {

                showAlert("Error", "Please enter a valid year.");
                return;

            }

            Date enrollmentDate = Date.valueOf(dpEnrollmentDate.getValue());

            Student studentToSave = new Student(student == null ? 0 : student.getId(),
                    name, year, phone, enrollmentDate, notes);
            int savedStudentId = studentRepository.save(studentToSave, Session.getUserId());
            scheduleRepository.saveSchedule(savedStudentId, Session.getUserId(), Session.getUserId(),
                    dpScheduleStart.getValue(), days);

            if (homeController != null) {
                homeController.loadStudents();
            }

            closeWindow();

        } catch (NumberFormatException e) {

            showAlert("Error", "Year of Birth must be numeric.");

        } catch (Exception e) {

            e.printStackTrace();
            showAlert("Database Error", e.getMessage());

        }

    }

    private Map<Integer, CheckBox> weekdayChecks() {
        Map<Integer, CheckBox> checks = new LinkedHashMap<>();
        checks.put(DayOfWeek.SUNDAY.getValue() % 7 + 1, chkSunday);
        checks.put(DayOfWeek.MONDAY.getValue() % 7 + 1, chkMonday);
        checks.put(DayOfWeek.TUESDAY.getValue() % 7 + 1, chkTuesday);
        checks.put(DayOfWeek.WEDNESDAY.getValue() % 7 + 1, chkWednesday);
        checks.put(DayOfWeek.THURSDAY.getValue() % 7 + 1, chkThursday);
        checks.put(DayOfWeek.FRIDAY.getValue() % 7 + 1, chkFriday);
        checks.put(DayOfWeek.SATURDAY.getValue() % 7 + 1, chkSaturday);
        return checks;
    }

    private Set<Integer> selectedDays() {
        Set<Integer> selected = new HashSet<>();
        weekdayChecks().forEach((day, checkBox) -> {
            if (checkBox.isSelected()) selected.add(day);
        });
        return selected;
    }

    @FXML
    private void handleCancel() {

        closeWindow();

    }

    private void closeWindow() {

        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();

    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        I18n.localize(alert);

        alert.showAndWait();

    }

}
