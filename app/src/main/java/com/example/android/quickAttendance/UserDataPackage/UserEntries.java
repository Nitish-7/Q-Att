package com.example.android.quickAttendance.UserDataPackage;

public class UserEntries {
    private UserEntries() {
    }

    public static final String USER_ID = "userId";
    public static final String USER_NAME = "username";
    public static final String USER_EMAIL = "userEmail";
    public static final String USER_PASSWORD = "userPassword";
    public static final String USER_CLASSES = "userClasses";

    public static final class UserClassesDetailes {
        private UserClassesDetailes() {
        }

        public static final String CLASS_ID = "classId";
        public static final String CLASS_NAME = "className";
        public static final String CLASS_SECTION = "classSection";
        public static final String CLASS_SUBJECT = "classSubject";
        public static final String CLASS_SUBJECT_CODE = "classSubjectCode";
        public static final String CLASS_YEAR = "classYear";
        public static final String CLASS_STUDENTS = "classStudents";
        public static final String CLASS_ATTENDANCE = "classAttendance";
        public static final String ATTENDANCE_DATE = "attendanceDate";

    }

    public static final class UserClassesStudentsDetailes {
        private UserClassesStudentsDetailes() {
        }

        public static final String STUDENT_ROLL_NO = "studentRollNo";
        public static final String STUDENT_NAME = "studentName";
    }
}
