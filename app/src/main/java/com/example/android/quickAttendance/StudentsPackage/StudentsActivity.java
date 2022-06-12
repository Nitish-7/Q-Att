package com.example.android.quickAttendance.StudentsPackage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.android.quickAttendance.FirebaseDao;
import com.example.android.quickAttendance.StudentsPackage.DataModel.Student;
import com.example.android.quickAttendance.databinding.ActivityStudentsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class StudentsActivity extends AppCompatActivity implements MyListener {

    ActivityStudentsBinding binding;
    RecyclerView recyclerView;
    StudentsViewAdapter adapter;
    ArrayList<Student> students = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        displayStudentsList();

    }

    @Override
    protected void onStart() {
        super.onStart();
        displayStudentsList();
    }

    private void displayStudentsList() {

        adapter = new StudentsViewAdapter(this, students, this);
        recyclerView = binding.rvStudents;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseDao.
                getStudentDbReference(FirebaseDao.getCurrentClass().getmClassId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        students.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            students.add(dataSnapshot.getValue(Student.class));
                        }

                        adapter.notifyStudentsChanged(students);


                        if (adapter.getItemCount() == 0) {
                            binding.emptyStudentsView.setVisibility(View.VISIBLE);
                            binding.btTakeAttendance.setVisibility(View.GONE);
                            binding.rvStudents.setVisibility(View.GONE);
                        } else {
                            binding.emptyStudentsView.setVisibility(View.GONE);
                            binding.btTakeAttendance.setVisibility(View.VISIBLE);
                            binding.rvStudents.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        recyclerView.setAdapter(adapter);

    }

    public void addStudents(View view) {

        if (ActivityCompat.checkSelfPermission(StudentsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            selectExcelfile();
        } else {
            ActivityCompat.requestPermissions(StudentsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        }
//        Intent intent = new Intent(StudentsActivity.this, StudentsEditorActivity.class);
//        startActivity(intent);
    }

    private void selectExcelfile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 102);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102) {
            if (resultCode == RESULT_OK) {
                String filePath = data.getData().getPath();

                if (filePath.endsWith(".xlsx")) {
                    Toast.makeText(this, "file selected", Toast.LENGTH_SHORT).show();
                    readExcelFile(data.getData());
                } else {
                    Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void readExcelFile(Uri fileUri) {

        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int rowCount = sheet.getPhysicalNumberOfRows();
            if (rowCount > 0) {
                for (int r = 0; r < rowCount; r++) {
                    Row row = sheet.getRow(r);

                    if (row.getPhysicalNumberOfCells() != 2) {
                        Toast.makeText(this, "Please select excel file with only roll-no. and names", Toast.LENGTH_SHORT).show();
                        students.clear();
                        break;
                    } else {
                        long rollNo = (long) row.getCell(0).getNumericCellValue();
                        String name = row.getCell(1).getStringCellValue();
                        Student student = new Student(String.valueOf(rollNo),
                                name);
                        students.add(student);
//                        TODO differnt percent pe alg color text colr each ka shi krna hai
                    }
                }
            }
            if (students.isEmpty()) {
                Toast.makeText(this, "No student found", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseDao.insertStudent(FirebaseDao.getCurrentClass().getmClassId(), students);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectExcelfile();
            } else {
                Toast.makeText(this, "Please grant permission to add student", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    public void takeAttendance(View view) {
//        FirebaseDao.setCurrentClassStudents(students);
//        startActivity(new Intent(StudentsActivity.this, TakeAttendanceActivity.class));
//    }

    @Override
    public void myOnClickStudentsList(String rollNo) {
        startActivity(new Intent(this, StudentsEditorActivity.class).putExtra("startingRollNo", rollNo));
    }

    @Override
    public void myOnLongClickStudentsList() {
//        TODO long click pe multiple select +selcet all+delete+ actionbar pe deleteview
    }
//    TODO left to right swipe pe edit delete function + edit pe current wale ko change krna h

}