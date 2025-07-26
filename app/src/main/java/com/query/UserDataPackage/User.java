package com.query.UserDataPackage;

public class User {

    public String userId;
    public String userName;
    public String userEmail;
    public String userPassword;
    public String userDefaultAttendanceMode;

    public User() {
        this.userId = "";
        this.userName = "";
        this.userEmail = "";
        this.userPassword = "";
        this.userDefaultAttendanceMode = "";
    }

    public User(String userId, String userName, String userEmail, String userPassword, String userDefaultAttendanceMode) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userDefaultAttendanceMode = userDefaultAttendanceMode;

    }

    public String getUserDefaultAttendanceMode() {
        return userDefaultAttendanceMode;
    }

    public void setUserDefaultAttendanceMode(String userDefaultAttendanceMode) {
        this.userDefaultAttendanceMode = userDefaultAttendanceMode;
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
