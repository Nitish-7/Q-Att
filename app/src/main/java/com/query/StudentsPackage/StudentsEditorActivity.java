package com.query.StudentsPackage;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.query.ClassesPackage.ClassesData.Classes;
import com.query.InitializerPackage.ConnectivityReceiver;
import com.query.FirebaseDao;
import com.query.R;
import com.query.StudentsPackage.DataModel.Student;
import com.query.databinding.ActivityStudentsEditorBinding;

import java.util.ArrayList;

public class StudentsEditorActivity extends AppCompatActivity {

    ActivityStudentsEditorBinding binding;
    long startingRollNo;
    long currentRollNo;
    long endingRollNo;
    Classes currentClass;
    ArrayList<Student> students = new ArrayList<>();

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


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!students.isEmpty() || getIntent().getBooleanExtra(getString(R.string.intent_extra_add_new_student), false))
            FirebaseDao.insertStudents(currentClass.getmClassId(), FirebaseDao.getCurrentClassStudents());
        FirebaseDao.setCurrentStudent(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentsEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        currentClass=FirebaseDao.getClassThroughIntent(getIntent());

        if (FirebaseDao.getCurrentStudent() != null) {
            startingRollNo = Long.parseLong(FirebaseDao.getCurrentStudent().getStudentRollNo());
            binding.addStudentsRollNo.setVisibility(View.GONE);
            binding.addStudentsName.setVisibility(View.VISIBLE);
            binding.tvCurrentStudentRollNo.setText(FirebaseDao.getCurrentStudent().getStudentRollNo());
            binding.currentStudentName.setHint(FirebaseDao.getCurrentStudent().getStudentName());
        } else if (getIntent().getBooleanExtra(getString(R.string.intent_extra_add_new_student), false)) {
            binding.addStudentsRollNo.setVisibility(View.GONE);
            binding.addStudentsName.setVisibility(View.VISIBLE);
            binding.tvCurrentStudentRollNo.setVisibility(View.GONE);
            binding.etCurrentStudentRollNo.setVisibility(View.VISIBLE);
            binding.etCurrentStudentRollNo.setText(FirebaseDao.getCurrentClassNextAvailableStudentRollno());
            binding.currentStudentName.setHint("Name");
        }
        else {
            binding.addStudentsRollNo.setVisibility(View.VISIBLE);
            binding.addStudentsName.setVisibility(View.GONE);
        }
        binding.btnSaveStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnSaveStudent.setVisibility(View.GONE);
                binding.pbSaveStudent.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void run() {
                        binding.btnSaveStudent.setVisibility(View.VISIBLE);
                        binding.pbSaveStudent.setVisibility(View.GONE);
                        addstudentname();
                    }
                },250);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void makeStudentList(View view) {

        if (binding.editStartingRollNo.getText().toString().isEmpty() || binding.editEndingRollNo.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter Roll No ", Toast.LENGTH_SHORT).show();
        } else {
            startingRollNo = Long.parseLong(binding.editStartingRollNo.getText().toString());
            endingRollNo = Long.parseLong(binding.editEndingRollNo.getText().toString());
            long totalStudents = endingRollNo - startingRollNo;
            if (startingRollNo < 0 || endingRollNo < 0) {
                Toast.makeText(this, "Roll-no can't be negative", Toast.LENGTH_SHORT).show();
            } else if (endingRollNo <= startingRollNo) {
                Toast.makeText(this, "Last Roll-No must" +
                        " be \ngreater than First Roll-No", Toast.LENGTH_SHORT).show();
            } else if (totalStudents > 150) {
                Toast.makeText(this, "can't add more than 150 \nstudents in one class", Toast.LENGTH_SHORT).show();
            } else {

                for (long i = startingRollNo; i <= endingRollNo; i++) {
                    students.add(new Student(String.valueOf(i), "Add Name", "0", "100"));
                }
                FirebaseDao.insertStudents(currentClass.getmClassId(), students);
                binding.addStudentsRollNo.setVisibility(View.GONE);
                //Thread.sleep(500);
                binding.addStudentsName.setVisibility(View.VISIBLE);
                currentRollNo = startingRollNo;
                binding.currentStudentName.setHint("Add Name");
                binding.tvCurrentStudentRollNo.setText(String.valueOf(startingRollNo));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addstudentname() {
        if (binding.currentStudentName.getText().toString().isEmpty()) {
            binding.currentStudentName.requestFocus();
            binding.currentStudentName.setError("Enter name");
            return;
        }
        //for adding new student
        else if (getIntent().getBooleanExtra(getString(R.string.intent_extra_add_new_student), false)) {
            if (binding.etCurrentStudentRollNo.getText().toString().isEmpty()) {
                Toast.makeText(this, "enter Roll-no otherwise default next roll-no will be added", Toast.LENGTH_SHORT).show();
                binding.etCurrentStudentRollNo.requestFocus();
                binding.etCurrentStudentRollNo.setError("*");
                binding.etCurrentStudentRollNo.setText(FirebaseDao.getCurrentClassNextAvailableStudentRollno());
                return;
            } else if(Long.parseLong(binding.etCurrentStudentRollNo.getText().toString())<0){
                binding.etCurrentStudentRollNo.requestFocus();
                binding.etCurrentStudentRollNo.setError("Roll-no can't be negative");
                binding.etCurrentStudentRollNo.setText(FirebaseDao.getCurrentClassNextAvailableStudentRollno());
                return;
            }
            else if (!FirebaseDao.checkCurrentClassNextAvailableStudentRollno(binding.etCurrentStudentRollNo.getText().toString())) {
                binding.etCurrentStudentRollNo.requestFocus();
                binding.etCurrentStudentRollNo.setError("Roll-no already exist");
                binding.etCurrentStudentRollNo.setText(FirebaseDao.getCurrentClassNextAvailableStudentRollno());
                return;
            }
            else {
                String studentAttendancePercent="100";
                try {
                    if(FirebaseDao.getCurrentClassDeliveredClasses()==0){
                        studentAttendancePercent="100";
                    }else{
                        studentAttendancePercent="0";
                    }
                }catch (Exception exception){
                    Log.e("adding new student:",exception.getMessage());
                }

                FirebaseDao.getCurrentClassStudents().add(new Student(binding.etCurrentStudentRollNo.getText().toString(), binding.currentStudentName.getText().toString(), "0", studentAttendancePercent));
                finish();
            }
        }
        //for editing student  name
        else if (FirebaseDao.getCurrentStudent() != null) {
            FirebaseDao.getCurrentStudent().setStudentName(binding.currentStudentName.getText().toString());
            FirebaseDao.getCurrentStudent().setStudentRollNo(binding.tvCurrentStudentRollNo.getText().toString());
            FirebaseDao.updateStudent(currentClass.getmClassId(), FirebaseDao.getCurrentStudent());
            finish();
        } else {
            int studentIndex = (int) currentRollNo - (int) startingRollNo;
            FirebaseDao.getCurrentClassStudents().get(studentIndex).setStudentName(binding.currentStudentName.getText().toString());

            currentRollNo++;
            binding.tvCurrentStudentRollNo.setText(String.valueOf(currentRollNo));
            binding.currentStudentName.setText("");
            binding.currentStudentName.setHint("Add name");

            if (currentRollNo > endingRollNo) {
                finish();
            }
        }
    }
}