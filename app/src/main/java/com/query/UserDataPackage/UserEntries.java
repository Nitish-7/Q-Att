package com.query.UserDataPackage;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.query.FirebaseDao;

public class UserEntries {
    private UserEntries() {
    }

    public static final String USER_CLASSES = "userClasses";
    public static final String USER_CLASSES_STUDENTS_AND_ATTENDANCE = "userClassesStudentsAndAttendances";
    public static final String USER_DETAILS = "userDetails";

    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String USER_EMAIL = "userEmail";
    public static final String USER_PASSWORD = "userPassword";
    public static final String USER_DEFAULT_ATTENDANCE_MODE = "userDefaultAttendanceMode";

    public static final String SWIPE_ATTENDANCE_MODE = "swipeMode";
    public static final String CLICK_ATTENDANCE_MODE = "clickMode";


    //private static final String USER_DETAILS_DATA_ON_SHARED_PREFERENCES = "userDetailsOnSharedPreferences";

    public static boolean isUserDetailsAvailableOnSharedPreferences(Context context, String userId) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(USER_ID + ":" + userId, MODE_PRIVATE);
        return preferences.getBoolean(userId, false);
    }

    public static User getPrefsDataForUserDetails(Context context) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(USER_ID + ":" + FirebaseDao.getmCurrentUserId(), MODE_PRIVATE);
        return new User(
                preferences.getString(USER_ID, ""),
                preferences.getString(USER_NAME, ""),
                preferences.getString(USER_EMAIL, ""),
                preferences.getString(USER_PASSWORD, ""),
                preferences.getString(USER_DEFAULT_ATTENDANCE_MODE, "")
        );
    }

    public static void savePrefsDataForUserDetails(Context context, User user) {
        SharedPreferences preferences = context.getSharedPreferences(USER_ID + ":" + user.getUserId(), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(user.getUserId(), true);
        editor.putString(USER_ID, user.getUserId());
        editor.putString(USER_NAME, user.getUserName());
        editor.putString(USER_EMAIL, user.getUserEmail());
        editor.putString(USER_PASSWORD, user.getUserPassword());
        editor.putString(USER_DEFAULT_ATTENDANCE_MODE, user.getUserDefaultAttendanceMode());
        editor.apply();
    }


    public static void settingCurrentUserByUsingDatabase(Context context) {
        //User user = new User();
        try {
            FirebaseDao.getUserDetailsDbReference().
                    addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
//                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                                switch ((dataSnapshot.getKey())) {
//                                    case UserEntries.USER_NAME:
//                                        user.setUserName(dataSnapshot.getValue(String.class));
//                                        break;
//                                    case UserEntries.USER_ID:
//                                        user.setUserId(dataSnapshot.getValue(String.class));
//                                        break;
//                                    case UserEntries.USER_EMAIL:
//                                        user.setUserEmail(dataSnapshot.getValue(String.class));
//                                        break;
//                                    case UserEntries.USER_PASSWORD:
//                                        user.setUserPassword(dataSnapshot.getValue(String.class));
//                                        break;
//                                    case UserEntries.USER_ATTENDANCE_MODE:
//                                        user.setUserDefaultAttendaceMode(dataSnapshot.getValue(String.class));
//                                        break;
//
//                                }
//                            }
                            if (user.getUserName().isEmpty()) {
                                user.setUserName(FirebaseDao.getCurrentFirebaseUser().getDisplayName());
                                if (user.getUserName().isEmpty()) {
                                    user.setUserName("Add User Name");
                                }
                            }
                            if (user.getUserId().isEmpty()) {
                                user.setUserId(FirebaseDao.getCurrentFirebaseUser().getUid());
                            }
                            if (user.getUserEmail().isEmpty()) {
                                user.setUserEmail(FirebaseDao.getCurrentFirebaseUser().getEmail());
                                if (user.getUserEmail().isEmpty()) {
                                    user.setUserEmail(FirebaseDao.getCurrentFirebaseUser().getPhoneNumber());
                                }
                            }
                            if (user.getUserDefaultAttendanceMode().isEmpty()) {
                                user.setUserDefaultAttendanceMode(UserEntries.SWIPE_ATTENDANCE_MODE);
                            }
                            if (user.getUserPassword().isEmpty()) {
                                user.setUserPassword("");
                            }

                            UserEntries.savePrefsDataForUserDetails(context, user);
                            //FirebaseDao.setCurrentUserDetailes(user);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("user&&&&&&&&&", e.getLocalizedMessage());
        }
    }


    public static final class UserClassesDetailes {
        private UserClassesDetailes() {
        }

        public static final String CLASS_ID = "classId";
        public static final String CLASS_NAME = "className";
        public static final String CLASS_SECTION = "classSection";
        public static final String CLASS_SUBJECT = "classSubject";
        public static final String CLASS_SUBJECT_CODE = "classSubjectCode";
        public static final String CLASS_YEAR = "classYear";
        public static final String ATTENDANCE_DATE = "attendanceDate";
        public static final String CLASSES_DELIVERED = "classesDelivered";


        public static final String CLASS_STUDENTS = "classStudents";
        public static final String CLASS_ATTENDANCE = "classAttendance";

    }

    public static final class UserClassesStudentsDetailes {
        private UserClassesStudentsDetailes() {
        }

        public static final String STUDENT_ROLL_NO = "studentRollNo";
        public static final String STUDENT_NAME = "studentName";
    }
}
