package com.example.android.quickAttendance;


import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.android.quickAttendance.ClassesPackage.ClassesData.Classes;
import com.example.android.quickAttendance.StudentsPackage.DataModel.Attendance;
import com.example.android.quickAttendance.StudentsPackage.DataModel.Student;
import com.example.android.quickAttendance.UserDataPackage.User;
import com.example.android.quickAttendance.UserDataPackage.UserEntries;
import com.example.android.quickAttendance.UserDataPackage.UserEntries.UserClassesDetailes;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FirebaseDao {

    private static final DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("users");
    private static FirebaseUser mCurrentUser;
    private static String mCurrentUserId;
    private static Classes currentClass;
    private static ArrayList<Student> currentClassStudents;

    public static void insertClassDetailes(Classes Class) {

        DatabaseReference classIdDbReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_CLASSES).child(UserClassesDetailes.CLASS_ID).child(String.valueOf(Class.getmClassId()));
        Log.d("class id", Class.getmClassId() + "");
        classIdDbReference.setValue(Class);
    }

    public static void deleteClassDetailes(Classes Class) {

    }

    public static void updateClassDetailes(Classes Class) {

    }

    public static void insertUserDetailes(User user) {
        DatabaseReference userIdReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId());
        userIdReference.child(UserEntries.USER_EMAIL).setValue(user.getUserEmail());
        userIdReference.child(UserEntries.USER_NAME).setValue(user.getUserName());
        userIdReference.child(UserEntries.USER_PASSWORD).setValue(user.getUserPassword());
        userIdReference.child(UserEntries.USER_CLASSES);
    }

    public static void deleteUserDetailes(User user) {

    }

    public static void updateUserDetailes(User user) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void insertAttendance(int classId, String date, ArrayList<Attendance> allRollNosAttendance) {

        DatabaseReference classAttedanceReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_CLASSES).child(UserClassesDetailes.CLASS_ID).child(String.valueOf(classId)).child(UserClassesDetailes.CLASS_ATTENDANCE).child(UserClassesDetailes.ATTENDANCE_DATE);
        classAttedanceReference.child(date).setValue(allRollNosAttendance);
    }


    public static void insertStudent(int classId, ArrayList<Student> students) {

        DatabaseReference studentRollNoDbReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_CLASSES).child(UserClassesDetailes.CLASS_ID).child(String.valueOf(classId)).child(UserClassesDetailes.CLASS_STUDENTS);
        studentRollNoDbReference.setValue(students);
    }

    public static DatabaseReference getStudentDbReference(int classId) {
        DatabaseReference studentRollNoDbReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_CLASSES).child(UserClassesDetailes.CLASS_ID).child(String.valueOf(classId)).child(UserClassesDetailes.CLASS_STUDENTS);
        return studentRollNoDbReference;
    }

    public static DatabaseReference getClassAttedanceDbReference() {
        DatabaseReference classAttedanceDbReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_CLASSES).child(UserClassesDetailes.CLASS_ID).child(String.valueOf(getCurrentClass().getmClassId())).child(UserClassesDetailes.CLASS_ATTENDANCE).child(UserClassesDetailes.ATTENDANCE_DATE);
        return classAttedanceDbReference;
    }

    public static DatabaseReference getUsersClassesDbReference() {
        DatabaseReference usersClassesDbReference = firebaseDatabase.child(UserEntries.USER_ID).child(getmCurrentUserId()).child(UserEntries.USER_CLASSES).child(UserClassesDetailes.CLASS_ID);
        return usersClassesDbReference;
    }

    public static FirebaseUser getmCurrentUser() {
        return mCurrentUser;
    }

    public static void setmCurrentUser(FirebaseUser currentUser) {
        mCurrentUser = currentUser;
    }

    public static Classes getCurrentClass() {
        return currentClass;
    }

    public static void setCurrentClass(Classes currentClass) {
        FirebaseDao.currentClass = currentClass;
    }

    public static void setCurrentClassStudents(ArrayList<Student> currentClassStudents) {
        FirebaseDao.currentClassStudents = currentClassStudents;
    }

    public static ArrayList<Student> getCurrentClassStudents() {
        return currentClassStudents;
    }

    public static String getmCurrentUserId() {
        return mCurrentUserId;
    }

    public static void setmCurrentUserId(String mCurrentUserId) {
        FirebaseDao.mCurrentUserId = mCurrentUserId;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getDateTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String hour = dateFormat.format(date.getTime());
        String d = String.valueOf(date.getYear() + 1900) + "/" + String.valueOf(date.getMonth()) + "/" + String.valueOf(date.getDate()) + "/" + hour.substring(0, 2);
        return d;
    }
}