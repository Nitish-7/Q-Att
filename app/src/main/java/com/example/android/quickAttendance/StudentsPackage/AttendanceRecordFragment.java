package com.example.android.quickAttendance.StudentsPackage;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.StrictMode;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.android.quickAttendance.BuildConfig;
import com.example.android.quickAttendance.FirebaseDao;
import com.example.android.quickAttendance.StudentsPackage.DataModel.Attendance;
import com.example.android.quickAttendance.StudentsPackage.DataModel.Student;
import com.example.android.quickAttendance.databinding.FragmentAttendanceRecordBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AttendanceRecordFragment#//newInstance} factory method to
 * create an instance of this fragment.
 */

public class AttendanceRecordFragment extends Fragment {

    FragmentAttendanceRecordBinding binding;
    ArrayList<Pair<String, ArrayList<Attendance>>> allRollNosAttendanceWithDate = new ArrayList<>();
    GenericTypeIndicator<ArrayList<Attendance>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<Attendance>>() {
    };
    public static final String ATTENDANCE_FOLDER_NAME = "Attendance Record";
    String fileName = null;
    String classFolderName = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
//        builder.detectFileUriExposure();
        // Inflate the layout for this fragment
        binding = binding = FragmentAttendanceRecordBinding.inflate(inflater, container, false);

        binding.downloadBt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                downloadExcelfile();
            }
        });

        binding.downloadedAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = getFile(fileName);

                // Get URI and MIME type of file
                Uri uri = Uri.fromFile(file);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                } else {
                    uri = Uri.fromFile(file);
                }
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.toString()));


                // Open file with user selected app
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setType(mime);
                intent.setData(uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "PLease Install Ms-Excel", Toast.LENGTH_SHORT).show();
                }
                startActivity(intent);
            }
        });

        binding.shareBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareAttendance();
            }
        });

        FirebaseDao.getClassAttedanceDbReference().
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot year) {
                        for (DataSnapshot month : year.getChildren()) {
                            for (DataSnapshot date : month.getChildren()) {
                                for (DataSnapshot time : date.getChildren()) {
                                    DataSnapshot attendance = time.getChildren().iterator().next();
                                    String dateForExcel = (month.getKey() + "/" + date.getKey() + "/" + time.getKey());
                                    allRollNosAttendanceWithDate.add(new Pair<String, ArrayList<Attendance>>(dateForExcel, attendance.getValue(genericTypeIndicator)));
                                }
                            }
                        }
                        if(allRollNosAttendanceWithDate.size()!=0)
                        {
                            binding.emptyAttendanceRecord.setVisibility(View.GONE);
                            binding.attendanceRecord.setVisibility(View.VISIBLE);
                        }
                        else {
                            binding.emptyAttendanceRecord.setVisibility(View.VISIBLE);
                            binding.attendanceRecord.setVisibility(View.GONE);

                        }

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showAttendance() {
        Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            downloadExcelfile();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadExcelfile();

            } else {
                Toast.makeText(getContext(), "Please grant permission to show attendance", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void downloadExcelfile() {

        binding.noAttendaceView.setVisibility(View.GONE);
        binding.progressBarView.setVisibility(View.VISIBLE);

        XSSFWorkbook workbook = createNewWorkbook();

        fileName = "attendance.xlsx";
        File file = (getFile(fileName));

        copyWorkbookOnExcelFile(workbook, file);
        binding.progressBarView.setVisibility(View.GONE);
        binding.downloadedAttendaceView.setVisibility(View.VISIBLE);
        binding.tvDownloadedAttendance.setText(fileName);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private XSSFWorkbook createNewWorkbook() {

        XSSFWorkbook workbook = new XSSFWorkbook();
        String previousAttendanceMonth = null;
        int r = 0, c = 2;
        for (Pair<String, ArrayList<Attendance>> eachDateAttendance : allRollNosAttendanceWithDate) {
            String fullDate[] = eachDateAttendance.first.split("/");
            String year = fullDate[0], month = fullDate[1], date = fullDate[2];
            String sheetName=getMonthShortName(Integer.parseInt(month))+"-"+year;
            XSSFSheet sheet;
            XSSFRow row;
            XSSFCell cell;

            if (!month.equals(previousAttendanceMonth)) {
                previousAttendanceMonth = month;
                sheet = workbook.createSheet(getMonthShortName(Integer.parseInt(month)));

                row = sheet.createRow(r);
                cell = row.createCell(0);
                cell.setCellValue(new XSSFRichTextString("Roll Number"));

                cell = row.createCell(1);
                cell.setCellValue(new XSSFRichTextString("Student Name"));
                ArrayList<Student> students = FirebaseDao.getCurrentClassStudents();
                for (Student student : students) {
                    r++;
                    row = sheet.createRow(r);
                    cell = sheet.getRow(r).createCell(0);
                    cell.setCellValue(new XSSFRichTextString(student.getStudentRollNo()));
                    cell = sheet.getRow(r).createCell(1);
                    cell.setCellValue(new XSSFRichTextString(student.getStudentName()));

                }
            }
            r = 0;
            cell = workbook.getSheet(sheetName).getRow(r).createCell(c);
            cell.setCellValue(new XSSFRichTextString(eachDateAttendance.first));
            for (Attendance attendance : eachDateAttendance.second) {
                r++;
                cell = workbook.getSheet(sheetName).getRow(r).createCell(c);
                cell.setCellValue(new XSSFRichTextString(attendance.getpOrA()));
            }
            c++;

        }
        return workbook;
    }


    private void copyWorkbookOnExcelFile(XSSFWorkbook workbook, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true);
            workbook.write(fos);

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public File getFile(String fileName) {
        classFolderName = FirebaseDao.getCurrentClass().getmClassName() + " " + FirebaseDao.getCurrentClass().getmSection() + " " + FirebaseDao.getCurrentClass().getmYear() + " " + FirebaseDao.getCurrentClass().getmSubjectName();
        ContextWrapper contextWrapper = new ContextWrapper(getActivity().getApplicationContext());
        File docsDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(docsDirectory, ATTENDANCE_FOLDER_NAME + "/" + classFolderName + "/" + fileName);
        if (!file.exists()) {
            boolean t = file.getParentFile().mkdirs();
        }
        return file;
    }

    private void downloadAttendanceTillNow() {
        //TODO: download attendance till now

    }

    private void shareAttendance() {
        //TODO: share attendance
        Intent intent = new Intent();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getMonthShortName(int monthNumber) {
        String monthName = "";

        if (monthNumber >= 0 && monthNumber < 12)
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MONTH, monthNumber);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM");
                //simpleDateFormat.setCalendar(calendar);
                monthName = simpleDateFormat.format(calendar.getTime());
            } catch (Exception e) {
                if (e != null)
                    e.printStackTrace();
            }
        return monthName;
    }

}