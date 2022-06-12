package com.example.android.quickAttendance.StudentsPackage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.android.quickAttendance.ClassesPackage.ClassesData.Classes;
import com.example.android.quickAttendance.FirebaseDao;
import com.example.android.quickAttendance.R;

import com.example.android.quickAttendance.StudentsPackage.StudentsActivity;
import com.example.android.quickAttendance.UserDataPackage.User;
import com.example.android.quickAttendance.UserDataPackage.UserEntries;
import com.example.android.quickAttendance.UserDataPackage.UserEntries.UserClassesDetailes;
import com.example.android.quickAttendance.databinding.ActivityStudentsEditorBinding;

public class StudentsEditorActivity extends AppCompatActivity {

    ActivityStudentsEditorBinding binding;
    long startingRollNo;
    long endingRollNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentsEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if(getIntent().getStringExtra("startingRollNo")!=null)
        {
            startingRollNo = Long.parseLong(getIntent().getStringExtra("startingRollNo"));
            binding.addStudentsRollNo.setVisibility(View.GONE);
            binding.addStudentsName.setVisibility(View.VISIBLE);
            binding.currentStudentRollNo.setText(String.valueOf(startingRollNo));
        }
    }

    public void makeStudentList(View view) {

        if(binding.editStartingRollNo.getText().toString().isEmpty()||binding.editEndingRollNo.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Enter Roll No ", Toast.LENGTH_SHORT).show();
        }
        else {
            startingRollNo = Long.parseLong(binding.editStartingRollNo.getText().toString());
            endingRollNo = Long.parseLong(binding.editEndingRollNo.getText().toString());
            long totalStudents = endingRollNo - startingRollNo;
            if (startingRollNo < 0 || endingRollNo < 0) {
                Toast.makeText(this, "Roll No can't be negative", Toast.LENGTH_SHORT).show();
            }
            else if (endingRollNo <= startingRollNo) {
                Toast.makeText(this, "Ending Roll No must be \ngreater than Starting", Toast.LENGTH_SHORT).show();
            }
            else {
                for(long i=startingRollNo;i<=endingRollNo;i++)
                {
                    //FirebaseDao.insertStudent(FirebaseDao.getCurrentClass().getmClassId(),String.valueOf(i),"Add name");
                }
                binding.addStudentsRollNo.setVisibility(View.GONE);
                binding.addStudentsName.setVisibility(View.VISIBLE);
                binding.currentStudentRollNo.setText(String.valueOf(startingRollNo));
            }
        }
    }

    public void addstudentname(View view) {
        if (binding.currentStudentName.getText().toString().isEmpty()) {
            Toast.makeText(this, "enter student name ", Toast.LENGTH_SHORT).show();
            return;
        }
        //binding.currentStudentName.setHint(FirebaseDao.getStudentDbReference(FirebaseDao.getCurrentClass().getmClassId()).child(startingRollNo+"").getKey());
        //FirebaseDao.insertStudent(FirebaseDao.getCurrentClass().getmClassId(),String.valueOf(startingRollNo),binding.currentStudentName.getText().toString());

        startingRollNo++;
        binding.currentStudentRollNo.setText(String.valueOf(startingRollNo));
        binding.currentStudentName.setText("");

        if(startingRollNo>endingRollNo)
            finish();
    }
}