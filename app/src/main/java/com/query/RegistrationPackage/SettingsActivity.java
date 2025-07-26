package com.query.RegistrationPackage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.query.InitializerPackage.ConnectivityReceiver;
import com.query.FirebaseDao;
import com.query.R;
import com.query.UserDataPackage.User;
import com.query.UserDataPackage.UserEntries;

public class SettingsActivity extends AppCompatActivity {
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private String attendanceMode;
    private User currentUserDetails;

    public ConnectivityReceiver receiver;

    @Override
    protected void onPause() {
        super.onPause();
        receiver.endInternetReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new ConnectivityReceiver();
        receiver.startInternetReceiver(this);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportActionBar().hide();
        currentUserDetails = UserEntries.getPrefsDataForUserDetails(this);
        updatingSettingView();


        radioGroup = (RadioGroup) findViewById(R.id.radio_group_user_attendance_mode);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_swipe_mode:
                        // do operations specific to this selection
                        attendanceMode = UserEntries.SWIPE_ATTENDANCE_MODE;
                        break;
                    case R.id.rb_click_mode:
                        // do operations specific to this selection
                        attendanceMode = UserEntries.CLICK_ATTENDANCE_MODE;
                        break;
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (attendanceMode != null) {
           currentUserDetails.setUserDefaultAttendanceMode(attendanceMode);
            FirebaseDao.setCurrentUserAttendanceMode(attendanceMode);
            currentUserDetails.setUserDefaultAttendanceMode(attendanceMode);
            UserEntries.savePrefsDataForUserDetails(this,currentUserDetails);
        }
    }

    public void helpFeedbackClick(View view) {
        String recipient = getString(R.string.help_and_feedback_email); // Set the recipient email address
        String subject = getString(R.string.help_and_feedback_subject_for_email); // Set the subject for the email
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + recipient)); // Specify the recipient email address
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject); // Set the subject
        startActivity(emailIntent); // Start the email app
    }

    public void shareAppClick(View view) {
        // Create a share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this awesome app\nQuery: The ultimate attendance & student management app for teachers.\n\nhttps://play.google.com/store/apps/details?id="+getPackageName());

        // Start the share chooser dialog
        startActivity(Intent.createChooser(shareIntent, "Share this app via"));
    }

    public void rateAppClick(View view) {
        String url = "https://play.google.com/store/apps/details?id="+getPackageName();

        // Create an intent to view the URL
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        // Start the intent
        startActivity(intent);
    }

    public void logoutClick(View view) {
        FirebaseAuth.getInstance().signOut();
        FirebaseDao.setCurrentFirebaseUser(null);
        //FirebaseDao.setCurrentUserDetailes(null);
        FirebaseDao.setmCurrentUserId(null);
        finish();
    }


    private void updatingSettingView() {
        TextView userName = findViewById(R.id.tv_user_name);
        TextView userEmail = findViewById(R.id.tv_user_email);
        userName.setText(currentUserDetails.getUserName());
        userEmail.setText(currentUserDetails.getUserEmail());

        if (currentUserDetails.getUserDefaultAttendanceMode().equals(UserEntries.SWIPE_ATTENDANCE_MODE)) {
            radioButton = (RadioButton) findViewById(R.id.rb_swipe_mode);
            radioButton.setChecked(true);
        } else {
            radioButton = (RadioButton) findViewById(R.id.rb_click_mode);
            radioButton.setChecked(true);
        }
    }

}