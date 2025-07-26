package com.query.StudentsPackage;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.query.ClassesPackage.ClassesData.Classes;
import com.query.InitializerPackage.ConnectivityReceiver;
import com.query.FirebaseDao;
import com.query.R;
import com.google.android.material.tabs.TabLayout;

public class StudentsAndAttendanceActivity extends AppCompatActivity {

    public ConnectivityReceiver receiver;
    public Classes currentClass;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_and_attendance);
        getSupportActionBar().setElevation(0);

        currentClass=FirebaseDao.getClassThroughIntent(getIntent());

        String[] years = getResources().getStringArray(R.array.array_year_options);
        String[] sections = getResources().getStringArray(R.array.array_section_options);

        String sectionTitle = sections[currentClass.getmSection()];
        String yearTitle = "";
        if (sectionTitle.equals("Sec")) {
            sectionTitle = "";
        }
        //setting yr or sesseion
        //for sessions assign
        if (currentClass.getmYear() >= 0 && currentClass.getmYear() < getResources().getStringArray(R.array.array_year_options).length) {
            yearTitle = years[(int) currentClass.getmYear()];
        }
        //for sem assign
        else {
            yearTitle=FirebaseDao.getStringSessionFromIntSession(currentClass.getmYear());
        }

        FirebaseDao.setCurrentClassDetailes(currentClass.getmClassName() + " " + sectionTitle + " " + yearTitle + " " + currentClass.getmSubjectName());
        getSupportActionBar().setTitle(FirebaseDao.getCurrentClassDetailes());
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager viewPager = findViewById(R.id.viewPager2);
        tabLayout.setupWithViewPager(viewPager);
        VpAdapter adapter = new VpAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragments(new StudentsFragment(), "Students",currentClass);
        adapter.addFragments(new AttendanceRecordFragment(), "Attendance",currentClass);
        viewPager.setAdapter(adapter);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //FirebaseDao.setCurrentClass(null);
    }
}
