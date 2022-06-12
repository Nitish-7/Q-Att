package com.example.android.quickAttendance.StudentsPackage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.example.android.quickAttendance.R;
import com.google.android.material.tabs.TabLayout;

public class StudentsAndAttendanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_and_attendance);
        getSupportActionBar().hide();

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager viewPager = findViewById(R.id.viewPager2);
        tabLayout.setupWithViewPager(viewPager);
        VpAdapter adapter = new VpAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragments(new StudentsFragment(),"Students");
        adapter.addFragments(new AttendanceRecordFragment(),"Attendance");
        viewPager.setAdapter(adapter);

    }
}