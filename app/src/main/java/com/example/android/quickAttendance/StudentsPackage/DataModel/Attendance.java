package com.example.android.quickAttendance.StudentsPackage.DataModel;

public class Attendance {

    String rollNo;
    String pOrA;
    public Attendance(){}

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getpOrA() {
        return pOrA;
    }

    public void setpOrA(String pOrA) {
        this.pOrA = pOrA;
    }

    public Attendance(String rollNo, String pOrA) {
        this.rollNo = rollNo;
        this.pOrA = pOrA;
    }
}
