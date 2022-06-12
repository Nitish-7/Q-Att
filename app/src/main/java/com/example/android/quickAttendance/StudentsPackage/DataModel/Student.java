package com.example.android.quickAttendance.StudentsPackage.DataModel;

public class Student {
    public String studentName;
    public String studentRollNo;
    public String studentAttendance;
    public String studentAttendancePercent;

    public String getStudentAttendancePercent() {
        return studentAttendancePercent;
    }

    public void setStudentAttendancePercent(String studentAttendancePercent) {
        this.studentAttendancePercent = studentAttendancePercent;
    }



    public String getStudentAttendance() {
        return studentAttendance;
    }

    public void setStudentAttendance(String studentAttendance) {
        this.studentAttendance = studentAttendance;
    }

    public Student(String studentName, String studentRollNo, String studentAttendance) {
        this.studentName = studentName;
        this.studentRollNo = studentRollNo;
        this.studentAttendance = studentAttendance;
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
