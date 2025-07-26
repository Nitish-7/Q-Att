package com.query.StudentsPackage;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.query.BuildConfig;
import com.query.ClassesPackage.ClassesData.Classes;
import com.query.FirebaseDao;
import com.query.OnboardingAndGuidePackage.TargetViewMaker;
import com.query.R;
import com.query.StudentsPackage.DataModel.Attendance;
import com.query.StudentsPackage.DataModel.MonthWiseAttendance;
import com.query.StudentsPackage.DataModel.Student;
import com.query.databinding.FragmentAttendanceRecordBinding;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/******************************************************
 * A simple {@link Fragment} subclass.
 * Use the {@link AttendanceRecordFragment#//newInstance} factory method to
 * create an instance of this fragment.
 *********************************************************/

public class AttendanceRecordFragment extends Fragment implements MyAttendanceListener {

    FragmentAttendanceRecordBinding binding;
    AttendanceViewAdapter adapter;
    RecyclerView attendanceGridView;
    GridLayoutManager gridLayoutManager;
    Activity thisActivity;
    ArrayList<Integer> eachRollNosAttendanceCount;
    int totalCLassesDelivered = 0;
    ArrayList<MonthWiseAttendance> monthWiseAttendances = new ArrayList<>();
    ArrayList<Pair<String, ArrayList<Attendance>>> allRollNosAttendanceMonthWise = new ArrayList<>();
    ArrayList<Pair<String, ArrayList<Attendance>>> allRollNosAttendanceWithDate = new ArrayList<>();
    boolean isFragmentVisible;
    GenericTypeIndicator<ArrayList<Attendance>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<Attendance>>() {
    };
    public Classes currentClass;
    public String ATTENDANCE_FOLDER_NAME = "Attendance Record" + "/" + "Users" + "/" + FirebaseDao.getmCurrentUserId() + "/" + "Classes";
    String downloadedFileName = null;
    String classFolderName = null;
    String startDate = null;
    String endDate = null;
    Long minDatePick, maxDatePick;
    int pickedStartDate[] = {0, 0, 0}; //0-day,1-month,2-year
    int pickedEndDate[] = {0, 0, 0};


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void afterAttendanceDataChanged() {
        int s = adapter.getItemCount();
        adapter.notifyAttendanceRecordChanged(monthWiseAttendances);

        Log.d("**atten###Fetching**", "endeed-----------------------at time = " + FirebaseDao.getOnlyTime());

        FirebaseDao.setCurrentClassDeliveredClasses(totalCLassesDelivered);

        minDatePick = FirebaseDao.getFirstAttendanceDate();
        maxDatePick = System.currentTimeMillis();

        String dateString = new SimpleDateFormat("dd/MM/YYYY").format(new Date(minDatePick));
        String date[] = dateString.split("/");
        pickedStartDate[0] = Integer.parseInt(date[0]);
        pickedStartDate[1] = Integer.parseInt(date[1]);
        pickedStartDate[2] = Integer.parseInt(date[2]);

        String dateString2 = new SimpleDateFormat("dd/MM/YYYY").format(new Date(maxDatePick));
        String date2[] = dateString2.split("/");
        pickedEndDate[0] = Integer.parseInt(date2[0]);
        pickedEndDate[1] = Integer.parseInt(date2[1]);
        pickedEndDate[2] = Integer.parseInt(date2[2]);

        startDate = (pickedStartDate[0] + "-" + pickedStartDate[1] + "-" + pickedStartDate[2]);
        endDate = (pickedEndDate[0] + "-" + pickedEndDate[1] + "-" + pickedEndDate[2]);

        String s1 = pickedStartDate[0] + "-" + FirebaseDao.getMonthShortName(pickedStartDate[1]) + "-" + pickedStartDate[2];
        String s2 = (pickedEndDate[0] + "-" + FirebaseDao.getMonthShortName(pickedEndDate[1]) + "-" + pickedEndDate[2]);

        binding.startingDateEditText.setText(s1);
        binding.endingDateEditText.setText(s2);

        pickedEndDate[1]--;
        pickedStartDate[1]--;

        if (allRollNosAttendanceWithDate.size() != 0) {
            binding.loadingAttendanceRecord.setVisibility(View.GONE);
            binding.emptyAttendanceRecord.setVisibility(View.GONE);
            binding.attendanceRecord.setVisibility(View.VISIBLE);

        } else {
            binding.loadingAttendanceRecord.setVisibility(View.GONE);
            binding.emptyAttendanceRecord.setVisibility(View.VISIBLE);
            binding.attendanceRecord.setVisibility(View.GONE);
        }
        if (adapter.getItemCount() == 0) {
            binding.loadingAttendanceRecord.setVisibility(View.GONE);
            binding.emptyAttendanceRecord.setVisibility(View.VISIBLE);
            binding.attendanceRecord.setVisibility(View.GONE);
            deleteClassAttendanceFolder(classFolderName);

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (downloadedFileName != null) {
            getFile(downloadedFileName).delete();
        }

    }


    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        thisActivity = activity;
        if (getArguments() != null) {
            currentClass = FirebaseDao.getClassThroughBundleArgument(getArguments());
        }
        try {
            classFolderName = currentClass.getmClassId();
        } catch (Exception e) {
            classFolderName = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();

        isFragmentVisible = true;
        //update adapter in backgroung  arraylist monthwiseAttendance or if anny student name changed or is added or deleted
        if (isSheetUpdateNeeded()) {
            sheetUpdateInBackground();
        }

        //taptarget userguide
        if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceBeforeDownloadingAttendance", thisActivity.getApplicationContext())) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (adapter.getItemCount() != 0 && isFragmentVisible) {
                        startOnboardGuideSequenceBeforeDownloadingAttendance();
                    }
                }
            }, 200);
        }
    }

    private boolean isSheetUpdateNeeded() {
        if (monthWiseAttendances.size() == 0) {
            return false;
        }
        File file = (getFile(monthWiseAttendances.get(0).getAttendaceFileName()));
        boolean fileExistOrNot = file.exists();
        if (fileExistOrNot) {
            InputStream inputStream = null;
            try {
                Uri fileUri = Uri.fromFile(file);
                inputStream = thisActivity.getContentResolver().openInputStream(fileUri);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                XSSFSheet sheet = workbook.getSheetAt(0);
                XSSFRow row;
                XSSFCell cell;
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

                if ((sheet.getPhysicalNumberOfRows() - 1) != FirebaseDao.getCurrentClassStudents().size()) {
                    return false;
                } else {
                    //fetching each student rollno and name from sheet
                    ArrayList<Student> students = FirebaseDao.getCurrentClassStudents();
                    int r = 1;
                    boolean isNameChanged = false;
                    for (Student student : students) {
                        row = sheet.getRow(r);
                        cell = row.getCell(0);
                        if (student.getStudentRollNo().equals(cell.getRichStringCellValue().getString())) {
                            cell = row.getCell(1);
                            if (!student.getStudentName().equals(cell.getRichStringCellValue().getString())) {
                                cell.setCellValue(new XSSFRichTextString(student.getStudentName()));
                                isNameChanged = true;
                                break;
                            }
                        }
                        r++;
                    }
                    if (isNameChanged) {
                        inputStream.close();
                        return true;
                    } else {
                        inputStream.close();
                        return false;
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void sheetUpdateInBackground() {
        try {
            binding.loadingAttendanceRecord.setVisibility(View.VISIBLE);
            binding.attendanceRecord.setVisibility(View.GONE);

            // Define a Handler and Looper for the main/UI thread
            Handler handler = new Handler(Looper.getMainLooper());

            // Perform background task in a separate thread
            HandlerThread handlerThread = new HandlerThread("BackgroundThread");
            handlerThread.start();
            Handler backgroundHandler = new Handler(handlerThread.getLooper());
            backgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Perform background task here
                    //FirebaseDao.setCurrentClass(Class);
                    getSheetAndUpdateStudentName();

                    // Post result to main/UI thread using the handler
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Update UI or perform other tasks on main/UI thread
                            // similar to onPostExecute() in AsyncTask
                            binding.loadingAttendanceRecord.setVisibility(View.GONE);
                            binding.attendanceRecord.setVisibility(View.VISIBLE);

                        }
                    });
                }
            });

            // Update UI or perform other tasks on main/UI thread before background task
            // similar to onPreExecute() in AsyncTask

        } catch (Exception e) {
            binding.loadingAttendanceRecord.setVisibility(View.GONE);
            binding.attendanceRecord.setVisibility(View.VISIBLE);
            e.printStackTrace();
        }
    }

    private void getSheetAndUpdateStudentName() {
        if (monthWiseAttendances.size() == 0) {
            return;
        }
        for (MonthWiseAttendance monthWiseAttendance : monthWiseAttendances) {
            File file = (getFile(monthWiseAttendance.getAttendaceFileName()));
            boolean fileExistOrNot = file.exists();
            if (fileExistOrNot) {
                InputStream inputStream = null;
                try {
                    Uri fileUri = Uri.fromFile(file);
                    inputStream = thisActivity.getContentResolver().openInputStream(fileUri);
                    XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                    XSSFSheet sheet = workbook.getSheetAt(0);
                    XSSFRow row;
                    XSSFCell cell;
                    FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

                    //fetching each student rollno and name from sheet
                    ArrayList<Student> students = FirebaseDao.getCurrentClassStudents();

                    int r = 1;
                    boolean isNameChanged = false;
                    for (Student student : students) {
                        row = sheet.getRow(r);
                        cell = row.getCell(0);
                        if (student.getStudentRollNo().equals(cell.getRichStringCellValue().getString())) {
                            cell = row.getCell(1);
                            if (!student.getStudentName().equals(cell.getRichStringCellValue().getString())) {
                                cell.setCellValue(new XSSFRichTextString(student.getStudentName()));
                                isNameChanged = true;
                            }
                        }
                        r++;
                    }
                    if (isNameChanged) {
                        file.delete();
                        copyWorkbookOnExcelFile(workbook, file);
                    }

                    inputStream.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentAttendanceRecordBinding.inflate(inflater, container, false);

        binding.startingDateEditText.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(getActivity(), R.style.StartDatePickerTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        pickedStartDate[2] = year;
                        pickedStartDate[1] = monthOfYear;
                        pickedStartDate[0] = dayOfMonth;
                        monthOfYear++;
                        String date = String.valueOf(dayOfMonth) + "-" + FirebaseDao.getMonthShortName(monthOfYear)
                                + "-" + String.valueOf(year);
                        startDate = dayOfMonth + "-" + monthOfYear + "-" + year;

                        try {
                            minDatePick = stringDateToLongDate(startDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        binding.startingDateEditText.setText(date);
                    }
                }, pickedStartDate[2], pickedStartDate[1], pickedStartDate[0]);
                datePicker.getDatePicker().setMinDate(FirebaseDao.getFirstAttendanceDate());
                datePicker.getDatePicker().setMaxDate(maxDatePick);

                datePicker.show();
            }
        });

        binding.endingDateEditText.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                DatePickerDialog datePicker = new DatePickerDialog(getActivity(), R.style.EndDatePickerTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        pickedEndDate[0] = dayOfMonth;
                        pickedEndDate[1] = monthOfYear;
                        pickedEndDate[2] = year;
                        monthOfYear++;
                        String date = String.valueOf(dayOfMonth) + "-" + FirebaseDao.getMonthShortName(monthOfYear)
                                + "-" + String.valueOf(year);
                        endDate = dayOfMonth + "-" + monthOfYear + "-" + year;
                        try {
                            maxDatePick = stringDateToLongDate(endDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        binding.endingDateEditText.setText(date);
                    }
                }, pickedEndDate[2], pickedEndDate[1], pickedEndDate[0]);
                datePicker.getDatePicker().setMinDate(minDatePick);
                datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePicker.show();
            }
        });

        binding.downloadBt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (downloadedFileName != null) {
                        getFile(downloadedFileName).delete();
                    }
                    WaitForDownloadingAttendance waitForDownloadingAttendance = new WaitForDownloadingAttendance();
                    waitForDownloadingAttendance.execute();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                }

            }
        });

        binding.downloadedAttendanceIv.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                showAttendanceInMsExcel(downloadedFileName);
            }
        });

        binding.shareBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareAttendance();
            }
        });

        attendanceGridView = binding.rvAttendanceRecord;
        gridLayoutManager = new GridLayoutManager(thisActivity.getApplicationContext(), 3);
        attendanceGridView.setLayoutManager(gridLayoutManager);
        adapter = new AttendanceViewAdapter(thisActivity.getApplicationContext(), this, monthWiseAttendances);

        //update adapter in backgroung  arraylist monthwiseAttendance or if anny student is added or deleted
        updateAttendanceChnagesInGridView();
        attendanceGridView.setAdapter(adapter);
        return binding.getRoot();
    }

    private void updateAttendanceChnagesInGridView() {
        try {
            FirebaseDao.getClassAttedanceDbReference(currentClass.getmClassId()).
                    addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot attendanceDate) {
                            ReadAttendanceInBackground readAttendanceInBackground = new ReadAttendanceInBackground();
                            readAttendanceInBackground.execute(attendanceDate);
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showAttendanceInMsExcel(String dFileName) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            File file = getFile(dFileName);
            if (!file.exists()) {
                boolean t = file.getParentFile().mkdirs();
            }

            // Get URI and MIME type of file
            Uri uri = Uri.fromFile(file);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file);
            } else {
                uri = Uri.fromFile(file);
            }
            //String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.toString()));
            String mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            // Open file with user selected app
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setType(mime);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
                createExcelSheetOpeningAppDialog("Kindly install either an Excel reading application or Microsoft Excel.");
            }
        } else {
            ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }

    }

    private void createExcelSheetOpeningAppDialog(String messege) {
        Dialog dialog = new Dialog(getContext(), R.style.StartDatePickerTheme);
        dialog.setContentView(R.layout.error_screen);
        TextView dialogTitle = (TextView) dialog.findViewById(R.id.error_title);
        TextView dialogDescription = (TextView) dialog.findViewById(R.id.error_description);
        ImageView dialogIv = dialog.findViewById(R.id.iv_error_dialog);

        dialogDescription.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Button btnOk = dialog.findViewById(R.id.btn_error_try_again);
        dialogIv.setImageResource(R.drawable.ic_excel);

        dialogTitle.setText("No App Found");
        dialogDescription.setText(messege);
        btnOk.setText("OK");

        btnOk.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showAttendanceInMsExcel(downloadedFileName);
            } else {
                Toast.makeText(getContext(), "Please grant permission to display attendance.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Integer downloadExcelfile() {

        downloadedFileName = binding.startingDateEditText.getText().toString() + " to " + binding.endingDateEditText.getText().toString() + " attendance of " + FirebaseDao.getCurrentClassDetailes();

        XSSFWorkbook workbook = createNewWorkbookForDownloading();

        if (workbook == null) {
            return 0;
        }

        File file = (getFile(downloadedFileName));
        if (!file.exists()) {
            boolean t = file.getParentFile().mkdirs();
        }

        copyWorkbookOnExcelFile(workbook, file);
        return 1;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void createWorkbooksForAttendaceAdapter(String fileName) {

        File file = (getFile(fileName));
        boolean fileExistOrNot = file.exists();
        if (fileExistOrNot) {
            InputStream inputStream = null;
            try {
                Uri fileUri = Uri.fromFile(file);
                inputStream = thisActivity.getContentResolver().openInputStream(fileUri);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                XSSFSheet sheet = workbook.getSheetAt(0);
                XSSFRow row;
                XSSFCell cell;
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

                if ((sheet.getPhysicalNumberOfRows() - 1) != FirebaseDao.getCurrentClassStudents().size()) {
                    inputStream.close();
                    file.delete();
                    fileExistOrNot = false;
                } else {
                    //fetching each student rollno and name from sheet
                    ArrayList<Student> students = FirebaseDao.getCurrentClassStudents();

                    int r = 1;
                    boolean isNameChanged = false;
                    for (Student student : students) {
                        row = sheet.getRow(r);
                        cell = row.getCell(0);
                        if (student.getStudentRollNo().equals(cell.getRichStringCellValue().getString())) {
                            cell = row.getCell(1);
                            if (!student.getStudentName().equals(cell.getRichStringCellValue().getString())) {
                                isNameChanged = true;
                                cell.setCellValue(new XSSFRichTextString(student.getStudentName()));
                            }
                        }
                        r++;
                    }
                    if (isNameChanged) {
                        file.delete();
                        copyWorkbookOnExcelFile(workbook, file);
                    }
                    inputStream.close();
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (!fileExistOrNot) {
            boolean t = file.getParentFile().mkdirs();

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet;
            XSSFRow row;

            int r = 0, c = 4, totalClassesDeliverd = 0;
            sheet = workbook.createSheet(fileName);


            //setting cell style
            XSSFCellStyle headCellStyle = headCellStyle(workbook);
            XSSFCellStyle valCellStyle = valCellStyle(workbook);

            //creating heading row name, rollno, classes attended, attendance
            row = sheet.createRow(r);

            createAndFillCell(row, 0, headCellStyle, (thisActivity.getResources().getString(R.string.excel_sheet_students_roll_no)));
            createAndFillCell(row, 1, headCellStyle, (thisActivity.getResources().getString(R.string.excel_sheet_student_name)));
            createAndFillCell(row, 2, headCellStyle, (thisActivity.getResources().getString(R.string.excel_sheet_student_classes_attended)));
            createAndFillCell(row, 3, headCellStyle, (thisActivity.getResources().getString(R.string.excel_sheet_student_attendance_percentage)));

            int nameColoumnWidth = 12;
            int rollNoColoumnWidth = 11;

            sheet.setColumnWidth(2, (256 * 18));
            sheet.setColumnWidth(3, (256 * 12));


            //creating each student rollno and name
            ArrayList<Student> students = FirebaseDao.getCurrentClassStudents();
            eachRollNosAttendanceCount = new ArrayList<>();

            for (Student student : students) {
                r++;
                row = sheet.createRow(r);
                eachRollNosAttendanceCount.add(0);
                if (student.getStudentRollNo().length() > rollNoColoumnWidth) {
                    rollNoColoumnWidth = student.getStudentRollNo().length();
                }
                if (student.getStudentName().length() > nameColoumnWidth) {
                    nameColoumnWidth = student.getStudentName().length();
                }
                createAndFillCell(row, 0, valCellStyle, student.getStudentRollNo());
                createAndFillCell(row, 1, valCellStyle, student.getStudentName());
            }

            sheet.setColumnWidth(0, (256 * (rollNoColoumnWidth + 2)));
            sheet.setColumnWidth(1, (256 * (nameColoumnWidth + 2)));

            //inserting each rollno attendance date wise
            for (Pair<String, ArrayList<Attendance>> eachDateAttendance : allRollNosAttendanceMonthWise) {
                r = 0;

                createAndFillCell(sheet.getRow(r), c, headCellStyle, eachDateAttendance.first);
                sheet.setColumnWidth(c, (256 * 12));

                r = 1;
                int rollNoIterator = 0, attendanceIterator = 0;
                for (Student student : students) {
                    if (student.getStudentRollNo().equals(eachDateAttendance.second.get(attendanceIterator).getRollNo())) {
                        createAndFillCell(sheet.getRow(r), c, valCellStyle, eachDateAttendance.second.get(attendanceIterator).getpOrA());
                        eachRollNosAttendanceCount.set(rollNoIterator, eachRollNosAttendanceCount.get(rollNoIterator) + Integer.parseInt(eachDateAttendance.second.get(attendanceIterator).getpOrA()));
                        if (attendanceIterator < eachDateAttendance.second.size() - 1) {
                            attendanceIterator++;
                        }
                    }
                    r++;
                    rollNoIterator++;
                }

                c++;
                totalClassesDeliverd++;
            }

            //inserting each rollno attended classes and percent
            r = 1;
            for (Integer attendance : eachRollNosAttendanceCount) {

                createAndFillCell(sheet.getRow(r), 2, valCellStyle, (attendance + "/" + totalClassesDeliverd));
                createAndFillCell(sheet.getRow(r), 3, valCellStyle, ((int) (attendance * 100) / totalClassesDeliverd) + "%");
                r++;
            }

            copyWorkbookOnExcelFile(workbook, file);
        }

        if (!monthWiseAttendances.contains(new MonthWiseAttendance(fileName))) {
            monthWiseAttendances.add(new MonthWiseAttendance(fileName));
        }
    }

    private XSSFCellStyle valCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle valCellStyle = workbook.createCellStyle();
        valCellStyle.setShrinkToFit(true);
        valCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        return valCellStyle;
    }

    private XSSFCellStyle headCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle headCellStyle = workbook.createCellStyle();
        headCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        headCellStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headCellStyle.setWrapText(false);
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBold(true);
        font.setItalic(false);
        headCellStyle.setFont(font);
        return headCellStyle;
    }

    private void createAndFillCell(XSSFRow row, int c, XSSFCellStyle cellStyle, String cellValue) {
        XSSFCell cell = row.createCell(c);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(new XSSFRichTextString(cellValue));
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean checkDates(String d1, String d2) {
        SimpleDateFormat dfDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        boolean b = false;
        try {
            b = dfDate.parse(d1).before(dfDate.parse(d2));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return b;
    }

    public static long stringDateToLongDate(String date) throws ParseException {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");
        Date mdate = sdf.parse(date);
        long d = mdate.getTime();
        return d;
    }

    private void shareAttendance() {

        File file = getFile(downloadedFileName);
        if (!file.exists()) {
            boolean t = file.getParentFile().mkdirs();
        }

        // Get URI and MIME type of file
        Uri uri = Uri.fromFile(file);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        //String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.toString()));
        String mime = "application/vnd.ms-excel";
        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setType(mime);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share Attendance"));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void OnAttendanceRecordItemClicked(int pos) {
        showAttendanceInMsExcel(monthWiseAttendances.get(pos).getAttendaceFileName());
    }

    class ReadAttendanceInBackground extends AsyncTask<DataSnapshot, Void, Void> {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(DataSnapshot... attendanceDate) {
            for (DataSnapshot year : attendanceDate[0].getChildren()) {
                for (DataSnapshot month : year.getChildren()) {
                    allRollNosAttendanceMonthWise.clear();
                    for (DataSnapshot date : month.getChildren()) {
                        for (DataSnapshot time : date.getChildren()) {
                            String dateForExcel = (year.getKey() + "/" + month.getKey() + "/" + date.getKey());
                            //setting first attendance date on data change in attendance
                            if (totalCLassesDelivered == 0) {
                                try {
                                    FirebaseDao.setFirstAttendanceDate(dateForExcel);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            totalCLassesDelivered++;

                            ArrayList<Attendance> sortedAttendanceRollnoWise = time.getValue(genericTypeIndicator);

                            allRollNosAttendanceWithDate.add(new Pair<String, ArrayList<Attendance>>(dateForExcel, sortedAttendanceRollnoWise));
                            allRollNosAttendanceMonthWise.add(new Pair<String, ArrayList<Attendance>>(dateForExcel, sortedAttendanceRollnoWise));
                        }
                    }
                    String fileName = FirebaseDao.getMonthShortName(Integer.parseInt(month.getKey())) + "-" + year.getKey();
                    createWorkbooksForAttendaceAdapter(fileName);
                }
            }

            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Void unused) {
            afterAttendanceDataChanged();
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPreExecute() {
            Log.d("**atten###Fetching**", "started+++++++++++++++++++++ at time = " + FirebaseDao.getOnlyTime());

            totalCLassesDelivered = 0;
            binding.loadingAttendanceRecord.setVisibility(View.VISIBLE);
            monthWiseAttendances.clear();
            allRollNosAttendanceMonthWise.clear();
            allRollNosAttendanceWithDate.clear();
            binding.emptyAttendanceRecord.setVisibility(View.GONE);
            binding.attendanceRecord.setVisibility(View.GONE);
        }
    }

    class WaitForDownloadingAttendance extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            binding.downloadedAttendaceView.setVisibility(View.GONE);
            binding.noAttendaceView.setVisibility(View.GONE);
            binding.progressBarView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Integer isDownloaded) {
            binding.progressBarView.setVisibility(View.GONE);
            if (isDownloaded == 1) {
                binding.downloadedAttendaceView.setVisibility(View.VISIBLE);
                binding.tvDownloadedAttendance.setText(downloadedFileName);

                //taptarget userguide
                if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceAfterDownloadingAttendance", thisActivity.getApplicationContext())) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isFragmentVisible)
                                startOnboardGuideSequenceAfterDownloadingAttendance();
                        }
                    }, 200);
                }
            } else {
                binding.noAttendaceView.setVisibility(View.VISIBLE);
                binding.tvNoAttendaceView.setText("No attendance found !");
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Integer doInBackground(Void... voids) {
            return downloadExcelfile();
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private XSSFWorkbook createNewWorkbookForDownloading() {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet;
        XSSFRow row;
        XSSFCell cell;
        int r = 0, c = 4, totalClassesDeliverd = 0;

        String previousAttendanceMonth = null;

        //setting cell style
        sheet = workbook.createSheet(downloadedFileName);
        XSSFCellStyle headCellStyle = headCellStyle(workbook);
        XSSFCellStyle valCellStyle = valCellStyle(workbook);

        //creating heading row .......... name, rollno, classes attended, attendance
        row = sheet.createRow(r);

        createAndFillCell(row, 0, headCellStyle, (thisActivity.getResources().getString(R.string.excel_sheet_students_roll_no)));
        createAndFillCell(row, 1, headCellStyle, (thisActivity.getResources().getString(R.string.excel_sheet_student_name)));
        createAndFillCell(row, 2, headCellStyle, (thisActivity.getResources().getString(R.string.excel_sheet_student_classes_attended)));
        createAndFillCell(row, 3, headCellStyle, (thisActivity.getResources().getString(R.string.excel_sheet_student_attendance_percentage)));

        int nameColoumnWidth = 12;
        int rollNoColoumnWidth = 11;

        sheet.setColumnWidth(2, (256 * 18));
        sheet.setColumnWidth(3, (256 * 12));

        //creating each student rollno and name
        ArrayList<Student> students = FirebaseDao.getCurrentClassStudents();
        eachRollNosAttendanceCount = new ArrayList<>();

        for (Student student : students) {
            r++;
            row = sheet.createRow(r);
            eachRollNosAttendanceCount.add(0);
            if (student.getStudentRollNo().length() > rollNoColoumnWidth) {
                rollNoColoumnWidth = student.getStudentRollNo().length();
            }
            if (student.getStudentName().length() > nameColoumnWidth) {
                nameColoumnWidth = student.getStudentName().length();
            }
            createAndFillCell(row, 0, valCellStyle, student.getStudentRollNo());
            createAndFillCell(row, 1, valCellStyle, student.getStudentName());
        }

        sheet.setColumnWidth(0, (256 * (rollNoColoumnWidth + 2)));
        sheet.setColumnWidth(1, (256 * (nameColoumnWidth + 2)));

        //inserting each rollno attendance date wise
        for (Pair<String, ArrayList<Attendance>> eachDateAttendance : allRollNosAttendanceWithDate) {
            String fullDate[] = eachDateAttendance.first.split("/");
            String year = fullDate[0], month = fullDate[1], date = fullDate[2];
            String currentAttendanceDate = date + "-" + month + "-" + year;

            // checking condition for date range
            {
                if (!month.equals(previousAttendanceMonth)) {
                    previousAttendanceMonth = month;
                    c++;
                }
                if (checkDates(currentAttendanceDate, startDate)) {
                    continue;
                }
                if (checkDates(endDate, currentAttendanceDate)) {
                    break;
                }
            }

            //inserting date in r=0,c=4 AS first date for any month attendance
            r = 0;
            createAndFillCell(sheet.getRow(r), c, headCellStyle, eachDateAttendance.first);
            sheet.setColumnWidth(c, (256 * 12));

            int rollNoIterator = 0;
            r = 1;
            for (Attendance attendance : eachDateAttendance.second) {
                while (!students.get(rollNoIterator).getStudentRollNo().equals(attendance.getRollNo())) {
                    if (rollNoIterator < students.size() - 1) {
                        rollNoIterator++;
                        r++;
                    }
                }
                createAndFillCell(sheet.getRow(r), c, valCellStyle, attendance.getpOrA());
                eachRollNosAttendanceCount.set(rollNoIterator, eachRollNosAttendanceCount.get(rollNoIterator) + Integer.parseInt(attendance.getpOrA()));
                if (rollNoIterator < students.size() - 1) {
                    rollNoIterator++;
                    r++;
                }
            }

            c++;   // incrementing coloumn for next date
            totalClassesDeliverd++; //adding one date as one class
        }

        if (totalClassesDeliverd == 0) {
            return null;
        }

        //inserting each rollno attended classes and percent
        r = 1;
        for (Integer attendance : eachRollNosAttendanceCount) {
            createAndFillCell(sheet.getRow(r), 2, valCellStyle, (attendance + "/" + totalClassesDeliverd));
            createAndFillCell(sheet.getRow(r), 3, valCellStyle, ((int) (attendance * 100) / totalClassesDeliverd) + "%");
            r++;
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
        ContextWrapper contextWrapper = new ContextWrapper(thisActivity.getApplicationContext());
        File docsDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (classFolderName == null) {
            classFolderName = currentClass.getmClassId();
        }
        File file = new File(docsDirectory, ATTENDANCE_FOLDER_NAME + "/" + classFolderName + "/" + fileName + ".xlsx");
        return file;

    }

    public void deleteClassAttendanceFolder(String classFolderName) {
        ContextWrapper contextWrapper = new ContextWrapper(thisActivity.getApplicationContext());
        File docsDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(docsDirectory, ATTENDANCE_FOLDER_NAME + "/" + classFolderName);
        deleteRecursive(file);
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    //user guide taptarget functions
    public void startOnboardGuideSequenceBeforeDownloadingAttendance() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(binding.downloadBt);

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(gridLayoutManager.findViewByPosition(0), getString(R.string.tap_title_monthly_attendance_record), getString(R.string.tap_title_monthly_attendance_record_desc), 100));

            targetArrayList.add(TargetViewMaker.makeTapTargetWithoutTintTarget(thisActivity.findViewById(R.id.download_bt), getString(R.string.tap_title_download_attendance_button), getString(R.string.tap_title_download_attendance_button_desc), 170));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(thisActivity.findViewById(R.id.startingDateView), getString(R.string.tap_title_start_date_pick_for_attendance), getString(R.string.tap_title_start_date_pick_for_attendance_desc), 80));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(thisActivity.findViewById(R.id.endingDateView), getString(R.string.tap_title_end_date_pick_for_attendance), getString(R.string.tap_title_end_date_pick_for_attendance_desc), 80));

            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequence(thisActivity);
        } catch (Exception exception) {

        }
    }

    public void startOnboardGuideSequenceAfterDownloadingAttendance() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(binding.downloadBt);

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(thisActivity.findViewById(R.id.downloaded_attendance_iv), getString(R.string.tap_title_downloaded_attendance), getString(R.string.tap_title_downloaded_attendance_desc), 130));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(thisActivity.findViewById(R.id.share_bt), getString(R.string.tap_title_share_downloaded_attendance), getString(R.string.tap_title_share_downloaded_attendance_desc), 40));

            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequence(thisActivity);
        } catch (Exception exception) {

        }
    }

}