package com.example.android.quickAttendance.UserDataPackage;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.android.quickAttendance.UserDataPackage.UserEntries;
import com.example.android.quickAttendance.UserDataPackage.UserEntries.UserClassesStudentsDetailes;
import com.example.android.quickAttendance.UserDataPackage.UserEntries.UserClassesDetailes;

public class User {

    public String userId;
    public String userName;
    public String userEmail;
    public String userPassword;

    public User(String userId, String userName, String userEmail, String userPassword) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

}
