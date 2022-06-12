package com.example.android.quickAttendance.StudentsPackage;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.quickAttendance.FirebaseDao;
import com.example.android.quickAttendance.StudentsPackage.DataModel.Student;
import com.example.android.quickAttendance.TakeAttendancePackage.TakeAttendanceActivity;
import com.example.android.quickAttendance.databinding.FragmentStudentsBinding;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StudentsFragment#//newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudentsFragment extends Fragment implements MyListener {

    FragmentStudentsBinding binding;
    RecyclerView recyclerView;
    StudentsViewAdapter adapter;
    ArrayList<Student> students = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStudentsBinding.inflate(inflater, container, false);
        displayStudentsList();

        binding.btTakeAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeAttendance();
            }
        });

        binding.addStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStudents();
            }
        });
        return binding.getRoot();

    }
    private void displayStudentsList() {

        adapter = new StudentsViewAdapter(getContext(), students,this );
        recyclerView = binding.rvStudents;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
        FirebaseDao.setCurrentClassStudents(students);

    }

    public void addStudents() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            selectExcelfile();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                String filePath = data.getData().getPath();

                if (filePath.endsWith(".xlsx")) {
                    Toast.makeText(getContext(), "file selected", Toast.LENGTH_SHORT).show();
                    readExcelFile(data.getData());
                } else {
                    Toast.makeText(getContext(), filePath, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void readExcelFile(Uri fileUri) {

        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(fileUri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int rowCount = sheet.getPhysicalNumberOfRows();
            if (rowCount > 0) {
                for (int r = 0; r < rowCount; r++) {
                    Row row = sheet.getRow(r);

                    if (row.getPhysicalNumberOfCells() != 2) {
                        Toast.makeText(getContext(), "Please select excel file with only roll-no. and names", Toast.LENGTH_SHORT).show();
                        students.clear();
                        break;
                    } else {
                        long rollNo = (long) row.getCell(0).getNumericCellValue();
                        String name = row.getCell(1).getStringCellValue();
                        Student student = new Student(String.valueOf(rollNo),
                                name);
                        students.add(student);
                    }
                }
            }
            if (students.isEmpty()) {
                Toast.makeText(getContext(), "No student found", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Please grant permission to add student", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void takeAttendance() {
        startActivity(new Intent(getContext(), TakeAttendanceActivity.class));
    }

    @Override
    public void myOnClickStudentsList(String rollNo) {
        startActivity(new Intent(getContext(), StudentsEditorActivity.class).putExtra("startingRollNo", rollNo));
    }

    @Override
    public void myOnLongClickStudentsList() {

    }
}