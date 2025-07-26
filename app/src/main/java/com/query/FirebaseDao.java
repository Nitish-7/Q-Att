package com.query;


import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.query.ClassesPackage.ClassesData.Classes;
import com.query.ClassesPackage.ClassesData.ClassesContracts;
import com.query.ClassesPackage.ClassesData.ClassesContracts.ClassEntry;
import com.query.StudentsPackage.DataModel.Attendance;
import com.query.StudentsPackage.DataModel.Student;
import com.query.UserDataPackage.User;
import com.query.UserDataPackage.UserEntries;
import com.query.UserDataPackage.UserEntries.UserClassesDetailes;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

public class FirebaseDao {

    private static final DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("users");
    private static int currentClassDeliveredClasses;
    private static FirebaseUser currentUser;
    private static String mCurrentUserId;
    private static String currentClassDetailes;
    private static User currentUserDetailes = new User();
    private static String currentUserAttendanceMode;
    private static Classes currentClass;
    private static ArrayList<Classes> CurrentUserClasses;
    private static ArrayList<Student> currentClassStudents;
    private static ArrayList<Student> currentClassStudentsSortedRollnoWise;
    private static Student currentStudent;
    private static long firstAttendanceDate;

    public static void insertClassDetailes(Classes Class) {
        DatabaseReference classIdDbReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_CLASSES).child(UserClassesDetailes.CLASS_ID);
        String classId = classIdDbReference.push().getKey();
        Class.setmClassId(classId);
        classIdDbReference.child(classId).setValue(Class);
    }

    public static void deleteClassDetailes(ArrayList<Classes> Classes) {
        for (Classes Class : Classes) {
            DatabaseReference classIdDbReference = getUsersClassesDbReference().child(Class.getmClassId());
            DatabaseReference classIdStudentsAndAttendanceDbReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_CLASSES_STUDENTS_AND_ATTENDANCE).child(UserClassesDetailes.CLASS_ID).child(Class.getmClassId());
            classIdDbReference.setValue(null);
            classIdStudentsAndAttendanceDbReference.setValue(null);
        }
    }

    public static void updateClassDetailes(Classes Class) {
        DatabaseReference classIdDbReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_CLASSES).child(UserClassesDetailes.CLASS_ID).child(String.valueOf(Class.getmClassId()));
        classIdDbReference.child(ClassesContracts.ClassEntry.COLUMN_CLASS_NAME).setValue(Class.getmClassName());
        classIdDbReference.child(ClassesContracts.ClassEntry.COLUMN_SECTION).setValue(Class.getmSection());
        classIdDbReference.child(ClassesContracts.ClassEntry.COLUMN_YEAR).setValue(Class.getmYear());
        classIdDbReference.child(ClassesContracts.ClassEntry.COLUMN_SUBJECT).setValue(Class.getmSubjectName());
        classIdDbReference.child(ClassesContracts.ClassEntry.COLUMN_SUBJECT_CODE).setValue(Class.getmSubjectCode());
    }

    public static void insertUserDetailsOnDatabase(User user, Context context) {
        DatabaseReference userDetailsReference = getUserDetailsDbReference();
        userDetailsReference.setValue(user);
        UserEntries.savePrefsDataForUserDetails(context,user);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void insertAttendance(String classId, String date, ArrayList<Attendance> allRollNosAttendance) {
        try {
            DatabaseReference classAttedanceReference = getClassAttedanceDbReference(classId);
            classAttedanceReference.child(date).setValue(allRollNosAttendance);
            updateCurrentClassStudentsAttendace(classId,allRollNosAttendance);
        } catch (Exception exception) {

        }
    }

    public static void updateAttendance(String classId, Map<String, Map<String, Map<String, Map<String, ArrayList<Attendance>>>>> allRollNosAttendanceWithDate) {
        DatabaseReference classAttedanceReference = getClassAttedanceDbReference(classId);
        classAttedanceReference.setValue(allRollNosAttendanceWithDate);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void updateCurrentClassStudentsAttendace(String classId,ArrayList<Attendance> allRollNosAttendance) {
        currentClassDeliveredClasses++;
        for (int i = 0; i < currentClassStudents.size(); i++) {
            int classAttended = (Integer.parseInt(currentClassStudents.get(i).getStudentAttendedClasses()) + Integer.parseInt(allRollNosAttendance.get(i).getpOrA()));
            int classAttendedPercent = (int) (classAttended * 100) / currentClassDeliveredClasses;
            currentClassStudents.get(i).setStudentAttendedClasses(String.valueOf(classAttended));
            currentClassStudents.get(i).setStudentAttendancePercent(String.valueOf(classAttendedPercent));
        }
        insertStudents(classId, currentClassStudents);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void insertStudents(String classId, ArrayList<Student> students) {
        DatabaseReference studentRollNoDbReference = getStudentDbReference(classId);
        studentRollNoDbReference.setValue(sortStudentsList(students));
        //setCurrentClassStudents(students);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static ArrayList<Student> sortStudentsList(ArrayList<Student> students) {
        try {
            students.sort(new Comparator<Student>() {
                @Override
                public int compare(Student o1, Student o2) {
                    return getIntegerFromStringRollno(o1.getStudentRollNo()).compareTo(getIntegerFromStringRollno(o2.getStudentRollNo()));
                }
            });
            return students;
        } catch (Exception e) {
            return null;
        }
    }

    private static Long getIntegerFromStringRollno(String studentRollNo) {
        StringBuilder onlyNumericRollnoString = new StringBuilder();
        for (char ch : studentRollNo.toCharArray()) {
            if (Character.isDigit(ch)) {
                onlyNumericRollnoString.append(ch);
            }
        }
        return Long.parseLong(onlyNumericRollnoString.toString());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void updateStudent(String classId, Student student) {
        for (Student mStudent : currentClassStudents) {
            if (mStudent.getStudentRollNo().equals(student.getStudentRollNo())) {
                mStudent.setStudentName(student.getStudentName());
                break;
            }
        }
        insertStudents(classId, currentClassStudents);
    }

    public static void deleteStudents(String classId, ArrayList<Student> students) {

        DatabaseReference studentRollNoDbReference = getStudentDbReference(classId);
        // to delete all
        if (currentClassStudents == students) {
            currentClassStudents.clear();
            studentRollNoDbReference.setValue(currentClassStudents);
        }
        // to delete some students but list remains sorted
        else {
            for (Student student : students) {
                currentClassStudents.remove(student);
            }
            studentRollNoDbReference.setValue(currentClassStudents);
        }
    }

    public static DatabaseReference getStudentDbReference(String classId) {
        DatabaseReference studentRollNoDbReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_CLASSES_STUDENTS_AND_ATTENDANCE).child(UserClassesDetailes.CLASS_ID).child(String.valueOf(classId)).child(UserClassesDetailes.CLASS_STUDENTS);
        studentRollNoDbReference.keepSynced(true);
        return studentRollNoDbReference;
    }

    public static DatabaseReference getClassAttedanceDbReference(String classId) {
        DatabaseReference classAttedanceDbReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_CLASSES_STUDENTS_AND_ATTENDANCE).child(UserClassesDetailes.CLASS_ID).child(classId).child(UserClassesDetailes.CLASS_ATTENDANCE).child(UserClassesDetailes.ATTENDANCE_DATE);
        classAttedanceDbReference.keepSynced(true);
        return classAttedanceDbReference;
    }

    public static DatabaseReference getUsersClassesDbReference() {
        DatabaseReference usersClassesDbReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_CLASSES).child(UserClassesDetailes.CLASS_ID);
        usersClassesDbReference.keepSynced(true);
        return usersClassesDbReference;
    }

    public static DatabaseReference getUserDetailsDbReference() {
        DatabaseReference usersDbReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_DETAILS);
        usersDbReference.keepSynced(true);
        return usersDbReference;
    }

    public static FirebaseUser getCurrentFirebaseUser() {
        return currentUser;
    }

    public static void setCurrentFirebaseUser(FirebaseUser currentUser) {
        FirebaseDao.currentUser = currentUser;
    }

    public static User getCurrentUserDetailes() {
        return currentUserDetailes;
    }

    public static void setCurrentUserDetailes(User currentUserDetailes) {
        FirebaseDao.currentUserDetailes = currentUserDetailes;
    }

    public static void setCurrentUserAttendanceMode(String currentUserAttendanceMode) {
        FirebaseDao.currentUserAttendanceMode = currentUserAttendanceMode;
        getUserDetailsDbReference().child(UserEntries.USER_DEFAULT_ATTENDANCE_MODE).setValue(currentUserAttendanceMode);
    }

    public static ArrayList<Classes> getCurrentUserClasses() {
        return CurrentUserClasses;
    }

    public static void setCurrentUserClasses(ArrayList<Classes> currentUserClasses) {
        CurrentUserClasses = currentUserClasses;
    }

    public static Classes getCurrentClass() {
        return FirebaseDao.currentClass;
    }

    public static void setCurrentClass(Classes currentClass) {
        FirebaseDao.currentClass = currentClass;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setCurrentClassStudents(ArrayList<Student> currentClassStudents) {
        FirebaseDao.currentClassStudents = new ArrayList<>();
        FirebaseDao.currentClassStudents.addAll(currentClassStudents);
    }

    public static ArrayList<Student> getCurrentClassStudentsSortedRollnoWise() {
        return currentClassStudentsSortedRollnoWise;
    }

    public static ArrayList<Student> getCurrentClassStudents() {
        return currentClassStudents;
    }

    public static void setCurrentStudent(Student currentStudent) {
        FirebaseDao.currentStudent = currentStudent;
    }

    public static Student getCurrentStudent() {
        return currentStudent;
    }

    public static String getmCurrentUserId() {
        return mCurrentUserId;
    }

    public static void setmCurrentUserId(String mCurrentUserId) {
        FirebaseDao.mCurrentUserId = mCurrentUserId;
    }

    public static int getCurrentClassDeliveredClasses() {
        return currentClassDeliveredClasses;
    }

    public static void setCurrentClassDeliveredClasses(int currentClassDeliveredClasses) {
        FirebaseDao.currentClassDeliveredClasses = currentClassDeliveredClasses;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getOnlyTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String hour = dateFormat.format(date.getTime());
        String t = hour.substring(0, 8);
        return t;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getDateTime() {
        Date date = new Date();

        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String hour = dateFormat.format(date.getTime());
        String d = (date.getYear() + 1900) + "/" + (date.getMonth() + 1) + "/" + (date.getDate()) + "/" + hour.substring(0, 7);
        return d;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Pair<Pair<Integer, Integer>, Pair<String, String>> getSession() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String hour = dateFormat.format(date.getTime());
        int yr = (date.getYear() + 1900);
        int lastTwoDigitYr = Integer.parseInt(String.valueOf(yr).charAt(2) + "" + String.valueOf(yr).charAt(3));
        String s1 = (yr - 1) + "-" + lastTwoDigitYr;
        String s2 = (yr) + "-" + String.valueOf(lastTwoDigitYr + 1);
        return new Pair<>(new Pair<>(lastTwoDigitYr * (-1), lastTwoDigitYr), new Pair<>(s1, s2));
    }


    public static String getStringSessionFromIntSession(int session) {
        String stringSession = "";
        if (session < 0) {
            session = session * (-1);
            stringSession = "20" + String.valueOf(session - 1) + "-" + String.valueOf(session);
        } else {
            stringSession = "20" + String.valueOf(session) + "-" + String.valueOf(session + 1);
        }
        return stringSession;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getMonthShortName(int monthNumber) {
        monthNumber -= 1;
        String monthName = "";

        if (monthNumber >= 0 && monthNumber < 12)
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MONTH, monthNumber);

                android.icu.text.SimpleDateFormat simpleDateFormat = new android.icu.text.SimpleDateFormat("MMM");
                //simpleDateFormat.setCalendar(calendar);
                monthName = simpleDateFormat.format(calendar.getTime());
            } catch (Exception e) {
                if (e != null)
                    e.printStackTrace();
            }
        return monthName;
    }

    public static long getFirstAttendanceDate() {
        return firstAttendanceDate;
    }

    public static void setFirstAttendanceDate(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date mdate = sdf.parse(date);
        firstAttendanceDate = mdate.getTime();
    }



    public static String getCurrentClassNextAvailableStudentRollno() {
        return String.valueOf(Long.parseLong(getCurrentClassStudents().get(getCurrentClassStudents().size() - 1).getStudentRollNo()) + 1);
    }

    public static boolean checkCurrentClassNextAvailableStudentRollno(String checkRollno) {
        long rollno =Long.parseLong(checkRollno);
        for (int i = 0; i < getCurrentClassStudents().size(); i++) {
            if (rollno == Long.parseLong(getCurrentClassStudents().get(i).getStudentRollNo())) {
                return false;
            }
        }
        return true;
    }

    public static void setCurrentClassDetailes(String s) {
        currentClassDetailes = s;
    }

    public static String getCurrentClassDetailes() {
        return currentClassDetailes;
    }

    public static Intent passClassThroughIntent(Classes currentClass, Intent intent) {
        intent.putExtra(ClassEntry.COLUMN_CLASS_ID, currentClass.getmClassId());
        intent.putExtra(ClassEntry.COLUMN_CLASS_NAME, currentClass.getmClassName());
        intent.putExtra(ClassEntry.COLUMN_SUBJECT, currentClass.getmSubjectName());
        intent.putExtra(ClassEntry.COLUMN_SUBJECT_CODE, currentClass.getmSubjectCode());
        intent.putExtra(ClassEntry.COLUMN_SECTION, currentClass.getmSection());
        intent.putExtra(ClassEntry.COLUMN_YEAR, currentClass.getmYear());
        return intent;
    }

    public static Classes getClassThroughIntent(Intent intent) {
        return new Classes(
                intent.getStringExtra(ClassEntry.COLUMN_CLASS_ID),
                intent.getStringExtra(ClassEntry.COLUMN_CLASS_NAME),
                intent.getStringExtra(ClassEntry.COLUMN_SUBJECT),
                intent.getStringExtra(ClassEntry.COLUMN_SUBJECT_CODE),
                intent.getIntExtra(ClassEntry.COLUMN_SECTION,0),
                intent.getIntExtra(ClassEntry.COLUMN_YEAR,0)
        );
    }

    public static Bundle passClassThroughBundle(Classes currentClass) {
        Bundle bundle = new Bundle();
        bundle.putString(ClassEntry.COLUMN_CLASS_ID, currentClass.getmClassId());
        bundle.putString(ClassEntry.COLUMN_CLASS_NAME, currentClass.getmClassName());
        bundle.putString(ClassEntry.COLUMN_SUBJECT, currentClass.getmSubjectName());
        bundle.putString(ClassEntry.COLUMN_SUBJECT_CODE, currentClass.getmSubjectCode());
        bundle.putInt(ClassEntry.COLUMN_SECTION, currentClass.getmSection());
        bundle.putInt(ClassEntry.COLUMN_YEAR, currentClass.getmYear());
        return bundle;
    }
    public static Classes getClassThroughBundleArgument(Bundle intent) {
        return new Classes(
                intent.getString(ClassEntry.COLUMN_CLASS_ID),
                intent.getString(ClassEntry.COLUMN_CLASS_NAME),
                intent.getString(ClassEntry.COLUMN_SUBJECT),
                intent.getString(ClassEntry.COLUMN_SUBJECT_CODE),
                intent.getInt(ClassEntry.COLUMN_SECTION,0),
                intent.getInt(ClassEntry.COLUMN_YEAR,0)
        );
    }
}