package com.query.StudentsPackage.DataModel;

public class Student {
    public String studentName;
    public String studentRollNo;
    public String studentAttendedClasses;
    public String studentAttendancePercent;

    public String getStudentAttendancePercent() {
        return studentAttendancePercent;
    }

    public void setStudentAttendancePercent(String studentAttendancePercent) {
        this.studentAttendancePercent = studentAttendancePercent;
    }

    public String getStudentAttendedClasses() {
        return studentAttendedClasses;
    }

    public void setStudentAttendedClasses(String studentAttendedClasses) {
        this.studentAttendedClasses = studentAttendedClasses;
    }

    public Student(String studentRollNo, String studentName, String studentAttendedClasses,String studentAttendancePercent) {
        this.studentName = studentName;
        this.studentRollNo = studentRollNo;
        this.studentAttendedClasses = studentAttendedClasses;
        this.studentAttendancePercent = studentAttendancePercent;
    }


    public String getStudentName() {
        return this.studentName;
    }

    public String getStudentRollNo() {
        return this.studentRollNo;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public Student(String studentRollNo, String studentName) {
        this.studentName = studentName;
        this.studentRollNo = studentRollNo;
    }

    public Student() {
    }
}
