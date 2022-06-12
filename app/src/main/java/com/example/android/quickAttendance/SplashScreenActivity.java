package com.example.android.quickAttendance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Thread(){
            public void run()
            {
                try {

                }
                catch (Exception exception)
                {

                }
                finally {
                    startActivity(new Intent(SplashScreenActivity.this,SignInActivity.class));

                }
            }
        }.start();
//        TODO guiding of app
    }
}