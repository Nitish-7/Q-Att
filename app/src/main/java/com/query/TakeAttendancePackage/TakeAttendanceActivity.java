package com.query.TakeAttendancePackage;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.query.ClassesPackage.ClassesData.Classes;
import com.query.InitializerPackage.ConnectivityReceiver;
import com.query.FirebaseDao;
import com.query.OnboardingAndGuidePackage.TargetViewMaker;
import com.query.R;
import com.query.StudentsPackage.DataModel.Attendance;
import com.query.StudentsPackage.DataModel.Student;
import com.query.UserDataPackage.UserEntries;

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
import java.util.ArrayList;

public class TakeAttendanceActivity extends AppCompatActivity implements MyListenerTakeAttendance, MyListenerTakenAttendance, ClickModeMyListenerTakeAttendance {

    Menu menu;
    RecyclerView clickModeAttendanceRv;
    RecyclerView takeAttendanceRv;
    RecyclerView takenAttendanceRv;
    GridLayoutManager gridLayoutManager;
    TakeAttendanceViewAdapter takeAttendanceAdapder;
    TakenAttendanceViewAdapter takenAttendanceAdapter;
    ClickModeTakeAttendanceAdapter clickModeTakeAttendanceAdapter;
    int attendanceMode = 0; //attendanceMode=0(swipe),attendanceMode=1(click)
    CustomLinearLayoutManager customLayoutManager;
    LinearLayoutManager takenAttendanceLinearLayoutManager;
    LinearLayoutManager clickModeTakeAttendanceLinearLayoutManager;
    String dateOfAttendance;
    ArrayList<Attendance> allRollNosAttendance;
    TextView prensentStrength;
    TextView absentStrength;
    public Classes currentClass;
    private String classFolderName = null;
    public static final String ATTENDANCE_FOLDER_NAME = "Attendance Record" + "/" + "Users" + "/" + FirebaseDao.getmCurrentUserId() + "/" + "Classes";

    public ConnectivityReceiver receiver;

    @Override
    protected void onPause() {
        super.onPause();
        receiver.endInternetReceiver();
    }

    @Override
    protected void onResume() {

        try {
            //classFolderName = FirebaseDao.getCurrentClass().getmClassId();
            classFolderName = currentClass.getmClassId();
        } catch (Exception e) {
            classFolderName = null;
        }
        super.onResume();
        receiver = new ConnectivityReceiver();
        receiver.startInternetReceiver(this);
    }

    public class CustomLinearLayoutManager extends LinearLayoutManager {

        private static final float MILLISECONDS_PER_INCH = 12f; //default is 25f (bigger = slower)
        private boolean scrollFlag = false;
        //private boolean isViewScrolled = false;

        public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);

        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            //super.smoothScrollToPosition(recyclerView, state, position);
            Log.d("x= ", "smooth ");
            final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

                @Override
                public PointF computeScrollVectorForPosition(int targetPosition) {
                    return super.computeScrollVectorForPosition(targetPosition);
                }

                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                }
            };

            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }

        @Override
        public void onScrollStateChanged(int state) {
            super.onScrollStateChanged(state);
            setScroll(false);
            Log.d("x= ", "scrolled ");
        }


        public void setScroll(boolean flag) {
            scrollFlag = flag;
        }

        // it will always pass false to RecyclerView when calling "canScrollVertically()" method.
        @Override
        public boolean canScrollHorizontally() {
            return scrollFlag;
        }

    }


    @SuppressLint({"NewApi", "ClickableViewAccessibility"})
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_attendance);

        currentClass = FirebaseDao.getClassThroughIntent(getIntent());
//        if (FirebaseDao.getCurrentClass() != null) {
//            currentClass = FirebaseDao.getCurrentClass();
//            // do something with section
//        } else {
//            currentClass=null;
//            // handle the case where getCurrentClass() returns null
//        }
        setAttendanceDefaultMode();

        allRollNosAttendance = new ArrayList<Attendance>();
        takeAttendanceRv = findViewById(R.id.take_attendance_rv_swipe_mode);
        takenAttendanceRv = findViewById(R.id.taken_attendance_rv_swipe_mode);
        clickModeAttendanceRv = findViewById(R.id.take_attendance_rv_click_mode);

        dateOfAttendance = FirebaseDao.getDateTime();

        customLayoutManager = new CustomLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        takenAttendanceLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        clickModeTakeAttendanceLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        customLayoutManager.setScroll(true);

        clickModeAttendanceRv.setLayoutManager(clickModeTakeAttendanceLinearLayoutManager);
        takeAttendanceRv.setLayoutManager(customLayoutManager);
        takenAttendanceRv.setLayoutManager(takenAttendanceLinearLayoutManager);

        clickModeTakeAttendanceAdapter = new ClickModeTakeAttendanceAdapter(this, this, FirebaseDao.getCurrentClassStudents(), attendanceMode);
        takeAttendanceAdapder = new TakeAttendanceViewAdapter(this, this, FirebaseDao.getCurrentClassStudents(), attendanceMode);
        takenAttendanceAdapter = new TakenAttendanceViewAdapter(this, this, allRollNosAttendance);

        takeAttendanceRv.setAdapter(takeAttendanceAdapder);
        takenAttendanceRv.setAdapter(takenAttendanceAdapter);
        clickModeAttendanceRv.setAdapter(clickModeTakeAttendanceAdapter);

        takeAttendanceRv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                takeAttendanceRv.onTouchEvent(motionEvent);
                return true;
            }
        });

        ItemTouchHelper.SimpleCallback swipeUpDown = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN) {
            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeThreshold(viewHolder) / 4;
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return super.getSwipeEscapeVelocity(defaultValue) * 2;
            }


            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                Log.d("c = ", actionState + "");

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                final int DIRECTION_UP = 1;
                final int DIRECTION_DOWN = 0;
                View itemView = viewHolder.itemView;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive) {
                    int direction = dY > 0 ? DIRECTION_DOWN : DIRECTION_UP;

                    if (direction == DIRECTION_UP) {
                        itemView.setForeground(getResources().getDrawable(R.drawable.swipe_up_fg));
                    } else if (direction == DIRECTION_DOWN) {
                        itemView.setForeground(getResources().getDrawable(R.drawable.swipe_down_fg));
                    }

                } else
                    itemView.setForeground(null);
            }


            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (allRollNosAttendance.size() == FirebaseDao.getCurrentClassStudents().size()) {
                    doAfterTakingAttendance();

                } else {
                    if (direction == ItemTouchHelper.UP) {
                        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                        if (allRollNosAttendance.size() == 0) {
                            allRollNosAttendance.add(new Attendance(FirebaseDao.getCurrentClassStudents().get(viewHolder.getAdapterPosition()).getStudentRollNo(), "1"));
                        } else if (!allRollNosAttendance.get(allRollNosAttendance.size() - 1).equals(new Attendance(FirebaseDao.getCurrentClassStudents().get(viewHolder.getAdapterPosition()).getStudentRollNo(), "1"))
                                && !allRollNosAttendance.get(allRollNosAttendance.size() - 1).getRollNo().equals(FirebaseDao.getCurrentClassStudents().get(viewHolder.getAdapterPosition()).getStudentRollNo())) {
                            allRollNosAttendance.add(new Attendance(FirebaseDao.getCurrentClassStudents().get(viewHolder.getAdapterPosition()).getStudentRollNo(), "1"));
                        }

                        clickModeTakeAttendanceAdapter.presentStudents.add(takenAttendanceAdapter.getItemCount());
                        clickModeTakeAttendanceAdapter.notifyItemChanged(takenAttendanceAdapter.getItemCount());
                    } else {
                        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                        if (allRollNosAttendance.size() == 0) {
                            allRollNosAttendance.add(new Attendance(FirebaseDao.getCurrentClassStudents().get(viewHolder.getAdapterPosition()).getStudentRollNo(), "0"));
                        } else if (!allRollNosAttendance.get(allRollNosAttendance.size() - 1).equals(new Attendance(FirebaseDao.getCurrentClassStudents().get(viewHolder.getAdapterPosition()).getStudentRollNo(), "0"))
                                && !allRollNosAttendance.get(allRollNosAttendance.size() - 1).getRollNo().equals(FirebaseDao.getCurrentClassStudents().get(viewHolder.getAdapterPosition()).getStudentRollNo())) {
                            allRollNosAttendance.add(new Attendance(FirebaseDao.getCurrentClassStudents().get(viewHolder.getAdapterPosition()).getStudentRollNo(), "0"));
                        }
                    }

                    int pos = viewHolder.getAdapterPosition() + 1;
                    customLayoutManager.scrollToPosition(pos);
                    clickModeTakeAttendanceLinearLayoutManager.scrollToPosition(takenAttendanceAdapter.getItemCount());
                    takenAttendanceAdapter.updateTakenAttendanceOneItem(allRollNosAttendance);
                    takenAttendanceLinearLayoutManager.scrollToPosition(takenAttendanceAdapter.getItemCount() - 1);

                    if (takeAttendanceAdapder.getItemCount() == pos) {
                        doAfterTakingAttendance();
                    }

                    //userguide
                    if (pos == 4) {
                        if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceAfterThreeSwipes", getApplicationContext())) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startOnboardGuideSequenceAfterThreeSwipes();
                                }
                            }, 100);
                        }
                    }
                }
            }
        };

        new ItemTouchHelper(swipeUpDown).attachToRecyclerView(takeAttendanceRv);

        if (attendanceMode == 0) {
            if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceOnStudentCard", getApplicationContext())) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startOnboardGuideSequenceOnStudentCard();
                    }
                }, 200);
            }
        } else {
            if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceOnClickModeAttendance", getApplicationContext())) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startOnboardGuideSequenceOnClickModeAttendance();
                    }
                }, 3000);
            }
        }

    }

    private void setAttendanceDefaultMode() {
        if (UserEntries.getPrefsDataForUserDetails(this).getUserDefaultAttendanceMode().equals(UserEntries.SWIPE_ATTENDANCE_MODE)) {
            attendanceMode = 0;
            findViewById(R.id.click_mode_take_attendance_view).setVisibility(View.GONE);
            findViewById(R.id.swipe_mode_take_attendance_view).setVisibility(View.VISIBLE);
        } else {
            attendanceMode = 1;
            findViewById(R.id.swipe_mode_take_attendance_view).setVisibility(View.GONE);
            findViewById(R.id.click_mode_take_attendance_view).setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void doAfterTakingAttendance() {

        //if last some attendance not filled with absent
        ArrayList<Student> currentClassStudents=FirebaseDao.getCurrentClassStudents();
        if (allRollNosAttendance.size() != currentClassStudents.size()) {
            for (int i = allRollNosAttendance.size(); i < FirebaseDao.getCurrentClassStudents().size(); i++) {
                allRollNosAttendance.add(new Attendance(FirebaseDao.getCurrentClassStudents().get(i).getStudentRollNo(), "0"));
            }
        }
        for(int i=0;i<currentClassStudents.size();i++){
            if(!allRollNosAttendance.get(i).getRollNo().equals(currentClassStudents.get(i).getStudentRollNo())){
                allRollNosAttendance.get(i).setRollNo(currentClassStudents.get(i).getStudentRollNo());
            }
        }

        ArrayList<Attendance> presentStudentsList = new ArrayList<>();
        ArrayList<Attendance> absentStudentsList = new ArrayList<>();

        for (Attendance attendance : allRollNosAttendance) {
            if (attendance.getpOrA().equals("1")) {
                presentStudentsList.add(attendance);
            } else absentStudentsList.add(attendance);
        }
        takeAttendanceRv.setVisibility(View.GONE);
        setContentView(R.layout.before_save_attendance_layout);
        RecyclerView confirmationAttendanceRv = findViewById(R.id.rv_students_before_save_attendance_layout);
        gridLayoutManager = new GridLayoutManager(TakeAttendanceActivity.this, 5, RecyclerView.VERTICAL, false);
        confirmationAttendanceRv.setLayoutManager(gridLayoutManager);
        takenAttendanceAdapter.updateAllItems(allRollNosAttendance);
        confirmationAttendanceRv.setAdapter(takenAttendanceAdapter);
        takenAttendanceAdapter.getFilter().filter("0");
        disableMenu();

        TextView presentStudents = (TextView) findViewById(R.id.bt_present_students_before_save_attendance_layout);
        TextView absentStudents = (TextView) findViewById(R.id.bt_absent_students_before_save_attendance_layout);
        TextView saveAttendance = (TextView) findViewById(R.id.bt_save_before_save_attendance_layout);
        TextView cancleAttendance = (TextView) findViewById(R.id.bt_cancle_before_save_attendance_layout);


        TextView totalStrength = (TextView) findViewById(R.id.tv_total_students_value_before_save_attendance_layout);
        absentStrength = (TextView) findViewById(R.id.tv_absent_students_value_before_save_attendance_layout);
        prensentStrength = (TextView) findViewById(R.id.tv_present_students_value_before_save_attendance_layout);

        prensentStrength.setText(String.valueOf(clickModeTakeAttendanceAdapter.presentStudents.size()));
        absentStrength.setText(String.valueOf(allRollNosAttendance.size() - clickModeTakeAttendanceAdapter.presentStudents.size()));
        totalStrength.setText(String.valueOf(allRollNosAttendance.size()));

        presentStudents.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {

//                takenAttendanceAdapter.updateAllItems(presentStudentsList);
                takenAttendanceAdapter.getFilter().filter("1");
                presentStudents.setTextColor(getResources().getColor(R.color.white));
                presentStudents.getBackground().setTint(getResources().getColor(R.color.red_theme_color));

                absentStudents.setTextColor(getResources().getColor(R.color.red_theme_color));
                absentStudents.getBackground().setTint(getResources().getColor(R.color.white));

            }
        });

        absentStudents.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
//                takenAttendanceAdapter.updateAllItems(absentStudentsList);
                takenAttendanceAdapter.getFilter().filter("0");
                absentStudents.setTextColor(getResources().getColor(R.color.white));
                absentStudents.getBackground().setTint(getResources().getColor(R.color.red_theme_color));
                presentStudents.setTextColor(getResources().getColor(R.color.red_theme_color));
                presentStudents.getBackground().setTint(getResources().getColor(R.color.white));

            }
        });

        saveAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAttendanceInBackground saveAttendanceInBackground = new SaveAttendanceInBackground();
                saveAttendanceInBackground.execute();
            }

        });

        cancleAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBackConfirmationDialog();
            }
        });

        //user guide
        if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceAfterTakingAttendance", getApplicationContext())) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startOnboardGuideSequenceAfterTakingAttendance();
                }
            }, 1000);
        }

    }

    private void pickCustomDateBeforeSavingAttendance() {
        int pickedStartDate[] = {0, 0, 0};
        Long minDatePick, maxDatePick = System.currentTimeMillis();
        DatePickerDialog datePicker = new DatePickerDialog(TakeAttendanceActivity.this, R.style.StartDatePickerTheme, new DatePickerDialog.OnDateSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                pickedStartDate[2] = year;
                pickedStartDate[1] = monthOfYear;
                pickedStartDate[0] = dayOfMonth;
                monthOfYear++;
                String date = String.valueOf(dayOfMonth) + "-" + FirebaseDao.getMonthShortName(monthOfYear)
                        + "-" + String.valueOf(year);
                //dateOfAttendance = String.valueOf(year) + "/" + String.valueOf(monthOfYear) + "/" + String.valueOf(dayOfMonth) + "/" + FirebaseDao.getOnlyTime();

                SaveAttendanceInBackground saveAttendanceInBackground = new SaveAttendanceInBackground();
                saveAttendanceInBackground.execute();
            }
        }, pickedStartDate[2], pickedStartDate[1], pickedStartDate[0]);
        datePicker.getDatePicker().setMinDate(FirebaseDao.getFirstAttendanceDate());
        datePicker.getDatePicker().setMaxDate(maxDatePick);
        datePicker.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void insetAttendanceInExcelFile() {
        String fullDate[] = dateOfAttendance.split("/");
        String year = fullDate[0], month = fullDate[1], date = fullDate[2];
        dateOfAttendance = year + "/" + month + "/" + date;
        String fileName = FirebaseDao.getMonthShortName(Integer.parseInt(month)) + "-" + year;
        File file = getFile(fileName);
        try {
            //if this attendance is created for new month and its excel file is not yet created in attendance record
            //return back in attendance record fragment it will be generated
            if (!file.exists()) {
                return;
            }
            InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(file));
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            cellStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            cellStyle.setWrapText(true);
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 11);
            font.setFontName("Arial");
            font.setColor(IndexedColors.BLACK.getIndex());
            font.setBold(true);
            font.setItalic(false);
            cellStyle.setFont(font);

            XSSFCellStyle valCellStyle = workbook.createCellStyle();
            valCellStyle.setShrinkToFit(true);
            valCellStyle.setAlignment(CellStyle.ALIGN_CENTER);

            XSSFSheet sheet = workbook.getSheetAt(0);
            //if a new sudent is added but excel sheets are not updated with new student then return
            if ((sheet.getPhysicalNumberOfRows() - 1) != FirebaseDao.getCurrentClassStudents().size()) {
                return;
            }
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            XSSFRow row = sheet.getRow(0);
            int lastCellNum = row.getLastCellNum();

            XSSFCell cell = row.createCell(lastCellNum);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(new XSSFRichTextString(dateOfAttendance));

            //adding each studets one day attendance
            int r = 1, attendanceIterator = 0;
            for (Student student : FirebaseDao.getCurrentClassStudents()) {
                if (student.getStudentRollNo().equals(allRollNosAttendance.get(attendanceIterator).getRollNo())) {
                    row = sheet.getRow(r);
                    cell = row.createCell(lastCellNum);
                    cell.setCellStyle(valCellStyle);
                    cell.setCellValue(new XSSFRichTextString(allRollNosAttendance.get(attendanceIterator).getpOrA()));
                    if (attendanceIterator < allRollNosAttendance.size() - 1) {
                        attendanceIterator++;
                    }
                }
                r++;
            }
            //adding each students todays + previous attendance as total attendance
            r = 1;
            attendanceIterator = 0;
            for (Student student : FirebaseDao.getCurrentClassStudents()) {
                if (student.getStudentRollNo().equals(allRollNosAttendance.get(attendanceIterator).getRollNo())) {
                    row = sheet.getRow(r);
                    cell = row.getCell(2);

                    String eachRollNosAttendanceCount = cell.getStringCellValue();
                    String[] AttendanceAndTotal = eachRollNosAttendanceCount.split("/");
                    int eachStudentAttendanceCount = Integer.parseInt(AttendanceAndTotal[0]) + Integer.parseInt(allRollNosAttendance.get(attendanceIterator).getpOrA());
                    int totalClassesDeliverd = Integer.parseInt(AttendanceAndTotal[1]) + 1;
                    cell.setCellValue(new XSSFRichTextString(eachStudentAttendanceCount + "/" + totalClassesDeliverd));

                    cell = row.getCell(3);
                    cell.setCellValue(new XSSFRichTextString(String.valueOf((int) (eachStudentAttendanceCount * 100) / totalClassesDeliverd) + "%"));
                    if (attendanceIterator < allRollNosAttendance.size() - 1) {
                        attendanceIterator++;
                    }
                }
                r++;
            }

            inputStream.close();
            getFile(fileName).delete();
            copyWorkbookOnExcelFile(workbook, getFile(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File docsDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (classFolderName == null) {
            classFolderName = currentClass.getmClassId();
        }
        File file = new File(docsDirectory, ATTENDANCE_FOLDER_NAME + "/" + classFolderName + "/" + fileName + ".xlsx");
        return file;

    }

    @Override
    public void myScrollListener(int position) {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //Back buttons was pressed, do whatever logic you want
            // create the dialog window
            createBackConfirmationDialog();

        }

        return false;
    }

    private void createBackConfirmationDialog() {
        Dialog dialog = new Dialog(this, R.style.StartDatePickerTheme);
        dialog.setContentView(R.layout.dialog_confirm_exit_take_attendace);
        ImageView dialogIv = dialog.findViewById(R.id.dialog_iv);
        dialogIv.setImageResource(R.drawable.ic_exit);
        dialog.findViewById(R.id.dialog_yes_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dialog.findViewById(R.id.dialog_no_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClickTakenAttendanceRvItem(View anchor, String rollNo, int filteredPosition) {
        int pos = 0;
        for (Attendance attendance : allRollNosAttendance) {
            if (attendance.getRollNo().equals(rollNo)) {
                break;
            }
            pos++;
        }
        final int position = pos;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_attendance_change, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        TextView changeAttendanceTv = (TextView) popupView.findViewById(R.id.popup_change_btn);
        TextView nameTv = (TextView) popupView.findViewById(R.id.popup_name_tv);
        nameTv.setText(FirebaseDao.getCurrentClassStudents().get(position).getStudentName());

        if (allRollNosAttendance.get(position).getpOrA().equals("1")) {
            changeAttendanceTv.setText("Mark Absent");
            changeAttendanceTv.getBackground().setTint(getResources().getColor(R.color.transparent_red));
        } else {
            changeAttendanceTv.setText("Mark Present");
            changeAttendanceTv.getBackground().setTint(getResources().getColor(R.color.transparent_green));
        }

        changeAttendanceTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allRollNosAttendance.get(position).getpOrA().equals("1")) {
                    allRollNosAttendance.get(position).setpOrA("0");
                    clickModeTakeAttendanceAdapter.presentStudents.remove((Integer) position);
                    if (prensentStrength != null && absentStrength != null) {
                        int p = Integer.parseInt(prensentStrength.getText().toString());
                        p--;
                        int a = Integer.parseInt(absentStrength.getText().toString());
                        a++;
                        prensentStrength.setText(String.valueOf(p));
                        absentStrength.setText(String.valueOf(a));
                    }
                } else {
                    allRollNosAttendance.get(position).setpOrA("1");
                    clickModeTakeAttendanceAdapter.presentStudents.add(position);
                    if (prensentStrength != null && absentStrength != null) {
                        int p = Integer.parseInt(prensentStrength.getText().toString());
                        p++;
                        int a = Integer.parseInt(absentStrength.getText().toString());
                        a--;
                        prensentStrength.setText(String.valueOf(p));
                        absentStrength.setText(String.valueOf(a));
                    }
                }
                clickModeTakeAttendanceAdapter.notifyItemChanged(position);
                takenAttendanceAdapter.notifyItemChanged(filteredPosition);

                popupWindow.dismiss();
            }
        });

        //popupWindow.setElevation(5);
        popupWindow.showAsDropDown(anchor);
    }

    @Override
    public void onClickClickModeAttendanceRvItem(int position) {
        if (takenAttendanceAdapter.getItemCount() - 1 < position) {

            for (int i = takenAttendanceAdapter.getItemCount(); i <= position; i++) {
                if (i == position) {
                    allRollNosAttendance.add(new Attendance(FirebaseDao.getCurrentClassStudents().get(i).getStudentRollNo(), "1"));
                } else {
                    allRollNosAttendance.add(new Attendance(FirebaseDao.getCurrentClassStudents().get(i).getStudentRollNo(), "0"));
                }
                //takenAttendanceAdapter.notifyItemInserted(i);
            }
            takenAttendanceAdapter.updateTakenAttendanceOneItem(allRollNosAttendance);
            takenAttendanceLinearLayoutManager.scrollToPosition(position);
            if (position + 1 > FirebaseDao.getCurrentClassStudents().size() - 1) {
                customLayoutManager.scrollToPosition(position);
            } else {
                customLayoutManager.scrollToPosition(position + 1);
            }
        } else {
            if (allRollNosAttendance.get(position).getpOrA().equals("0")) {
                allRollNosAttendance.get(position).setpOrA("1");
            } else {
                allRollNosAttendance.get(position).setpOrA("0");
            }
            takenAttendanceAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.take_attendance_menu, menu);
        this.menu = menu;

        if (UserEntries.getPrefsDataForUserDetails(this).getUserDefaultAttendanceMode().equals(UserEntries.SWIPE_ATTENDANCE_MODE)) {
            attendanceMode = 0;
            menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_click_mode));
            menu.getItem(0).setVisible(false);
        } else {
            attendanceMode = 1;
            menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_swipe_mode));
            menu.getItem(0).setVisible(true);
        }

        menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (attendanceMode == 0) {
                    attendanceMode = 1;

                    findViewById(R.id.swipe_mode_take_attendance_view).setVisibility(View.GONE);
                    findViewById(R.id.click_mode_take_attendance_view).setVisibility(View.VISIBLE);
                    menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_swipe_mode));
                    menu.getItem(0).setVisible(true);

                    //user guide
                    if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceOnClickModeAttendance", getApplicationContext())) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startOnboardGuideSequenceOnClickModeAttendance();
                            }
                        }, 2000);
                    }
                } else {
                    attendanceMode = 0;
                    findViewById(R.id.click_mode_take_attendance_view).setVisibility(View.GONE);
                    findViewById(R.id.swipe_mode_take_attendance_view).setVisibility(View.VISIBLE);
                    menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_click_mode));
                    menu.getItem(0).setVisible(false);

                    //user guide
                    if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceOnStudentCard", getApplicationContext())) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startOnboardGuideSequenceOnStudentCard();
                            }
                        }, 1000);
                    }
                }
                return true;
            }
        });
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Dialog dialog = new Dialog(TakeAttendanceActivity.this, R.style.StartDatePickerTheme);
                dialog.setContentView(R.layout.dialog_confirm_exit_take_attendace);

                TextView dialogDescription = (TextView) dialog.findViewById(R.id.dialog_description);
                dialogDescription.setText(R.string.text_dialog_confirm_taken_attendance);

                dialog.findViewById(R.id.dialog_yes_button).setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View v) {
                        doAfterTakingAttendance();
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.dialog_no_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void disableMenu() {
        menu.clear();
    }

    class SaveAttendanceInBackground extends AsyncTask<Void, Void, Void> {

        Dialog dialog = new Dialog(TakeAttendanceActivity.this, R.style.Theme_AppCompat_DayNight_NoActionBar_ClassesEditorTheme);

        @Override
        protected void onPreExecute() {
            dialog.setContentView(R.layout.dialog_confirm_exit_take_attendace);
            dialog.findViewById(R.id.dialog_no_button).setVisibility(View.GONE);
            TextView dialogBt = (TextView) dialog.findViewById(R.id.dialog_yes_button);
            TextView dialogDescription = (TextView) dialog.findViewById(R.id.dialog_description);
            ImageView dialogIv = (ImageView) dialog.findViewById(R.id.dialog_iv);
            ProgressBar dialogPb = (ProgressBar) dialog.findViewById(R.id.dialog_progress_bar);

            dialogBt.setText("OK");
            dialogDescription.setText(R.string.text_dialog_attendance_saving);
            dialogPb.setVisibility(View.VISIBLE);
            dialogIv.setVisibility(View.GONE);
            dialogBt.getBackground().setTint(getResources().getColor(R.color.offblack3));
            dialog.show();

        }

        @Override
        protected void onPostExecute(Void unused) {
            dialog.setContentView(R.layout.dialog_confirm_exit_take_attendace);
            dialog.findViewById(R.id.dialog_no_button).setVisibility(View.GONE);
            TextView dialogBt = (TextView) dialog.findViewById(R.id.dialog_yes_button);
            TextView dialogDescription = (TextView) dialog.findViewById(R.id.dialog_description);
            ImageView dialogIv = (ImageView) dialog.findViewById(R.id.dialog_iv);
            ProgressBar dialogPb = (ProgressBar) dialog.findViewById(R.id.dialog_progress_bar);

            dialogBt.getBackground().setTint(getResources().getColor(R.color.red_theme_color));
            dialogBt.setText("OK");
            dialogDescription.setText(R.string.text_dialog_attendance_saved);
            dialogPb.setVisibility(View.GONE);
            dialogIv.setVisibility(View.VISIBLE);
            dialogIv.setImageResource(R.drawable.ic_success);

            dialogBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... voids) {
            FirebaseDao.insertAttendance(currentClass.getmClassId(), dateOfAttendance, allRollNosAttendance);
            insetAttendanceInExcelFile();
            return null;
        }
    }

    //user guide taptarget functions

    public void startOnboardGuideSequenceOnStudentCard() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(findViewById(R.id.change_attendance_mode_menu_item));

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTransparentTarget(customLayoutManager.findViewByPosition(customLayoutManager.findFirstCompletelyVisibleItemPosition()).findViewById(R.id.tv_card_student_roll_no), getString(R.string.tap_title_card_student_roll_no), getString(R.string.tap_title_card_student_roll_no_desc), 80));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(customLayoutManager.findViewByPosition(customLayoutManager.findFirstCompletelyVisibleItemPosition()).findViewById(R.id.tv_card_student_attendance), getString(R.string.tap_title_card_student_attendance), getString(R.string.tap_title_card_student_attendance_desc), 100));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTransparentTarget(customLayoutManager.findViewByPosition(customLayoutManager.findFirstCompletelyVisibleItemPosition()).findViewById(R.id.tv_card_student_attendance), getString(R.string.tap_title_swipe_explain), getString(R.string.tap_title_swipe_explain_desc), 170));

            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequence(TakeAttendanceActivity.this);
        } catch (Exception exception) {

        }
    }

    public void startOnboardGuideSequenceAfterThreeSwipes() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(findViewById(R.id.change_attendance_mode_menu_item));

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTransparentTarget(takenAttendanceLinearLayoutManager.findViewByPosition(takenAttendanceLinearLayoutManager.findFirstCompletelyVisibleItemPosition()), getString(R.string.tap_title_taken_attendance_circle_on_swipe), getString(R.string.tap_title_taken_attendance_circle_on_swipe_desc), 60));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(findViewById(R.id.change_attendance_mode_menu_item), getString(R.string.tap_title_change_to_click_mode), getString(R.string.tap_title_change_to_click_mode_desc), 40));

            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequence(TakeAttendanceActivity.this);
        } catch (Exception exception) {

        }
    }

    public void startOnboardGuideSequenceOnClickModeAttendance() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(findViewById(R.id.change_attendance_mode_menu_item));

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();

            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(clickModeTakeAttendanceLinearLayoutManager.findViewByPosition(clickModeTakeAttendanceLinearLayoutManager.findFirstCompletelyVisibleItemPosition()).findViewById(R.id.tv_strip_student_roll_no), getString(R.string.tap_title_strip_student_roll_no), getString(R.string.tap_title_strip_student_roll_no_desc), 60));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(clickModeTakeAttendanceLinearLayoutManager.findViewByPosition(clickModeTakeAttendanceLinearLayoutManager.findFirstCompletelyVisibleItemPosition()), getString(R.string.tap_title_click_attendance_explain), getString(R.string.tap_title_click_attendance_explain_desc), 150));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(findViewById(R.id.change_attendance_mode_menu_item), getString(R.string.tap_title_change_to_swipe_mode), getString(R.string.tap_title_change_to_swipe_mode_desc), 40));

            //TODO:Attempt to invoke virtual method 'android.view.View android.view.View.findViewById(int)' on a null object reference
            //targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(findViewById(R.id.save_attendance_menu_item), getString(R.string.tap_title_save_attendance_menu_item), getString(R.string.tap_title_save_attendance_menu_item_desc), 40));

            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequence(TakeAttendanceActivity.this);
        } catch (Exception exception) {

        }

    }

    public void startOnboardGuideSequenceAfterTakingAttendance() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(findViewById(R.id.tv_absent_students_value_before_save_attendance_layout));

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTransparentTarget(findViewById(R.id.bt_absent_students_before_save_attendance_layout), getString(R.string.tap_title_absent_students_value_before_save_attendance), getString(R.string.tap_title_absent_students_value_before_save_attendance_desc), 90));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTransparentTarget(findViewById(R.id.bt_present_students_before_save_attendance_layout), getString(R.string.tap_title_present_students_value_before_save_attendance), getString(R.string.tap_title_present_students_value_before_save_attendance_desc), 90));
            if (gridLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                targetArrayList.add(TargetViewMaker.makeTapTargetWithTransparentTarget(gridLayoutManager.findViewByPosition(0), getString(R.string.tap_title_taken_attendance_circle_after_taking), getString(R.string.tap_title_taken_attendance_circle_after_taking_desc), 40));
            }
            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequence(TakeAttendanceActivity.this);
        } catch (Exception exception) {

        }
    }

}