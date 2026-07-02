package models;

import java.sql.Date;

public class Student {

    private int id;
    private String name;
    private int yearOfBirth;
    private String phone;
    private Date enrollmentDate;
    private String notes;

    public Student(int id,
                   String name,
                   int yearOfBirth,
                   String phone,
                   Date enrollmentDate,
                   String notes) {

        this.id = id;
        this.name = name;
        this.yearOfBirth = yearOfBirth;
        this.phone = phone;
        this.enrollmentDate = enrollmentDate;
        this.notes = notes;
    }

    // Getters

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public String getNotes() {
        return notes;
    }

    // Setters

    public void setName(String name) {
        this.name = name;
    }

    public void setYearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}