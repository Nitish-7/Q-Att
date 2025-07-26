package com.query.StudentsPackage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.query.ClassesPackage.ClassesData.Classes;
import com.query.FirebaseDao;
import com.query.OnboardingAndGuidePackage.TargetViewMaker;
import com.query.R;
import com.query.StudentsPackage.DataModel.Attendance;
import com.query.StudentsPackage.DataModel.Student;
import com.query.TakeAttendancePackage.TakeAttendanceActivity;
import com.query.databinding.FragmentStudentsBinding;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StudentsFragment#//newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudentsFragment extends Fragment implements MyListener {

    FragmentStudentsBinding binding;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    StudentsViewAdapter adapter;
    ArrayList<Student> students = new ArrayList<>();
    StudentSelectionViewModal studentSelectionViewModal;
    public ActionMode actionMode = null;
    boolean isOneStudentSelected = false;
    boolean isMultipleStudentSelected = false;
    SearchView searchView;
    Activity thisActivity;
    Dialog studentImportDialog;
    public Classes currentClass;
    GenericTypeIndicator<ArrayList<Attendance>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<Attendance>>() {
    };

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        thisActivity = activity;
        if (getArguments() != null) {
            currentClass = FirebaseDao.getClassThroughBundleArgument(getArguments());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        searchView.clearFocus();
        InputMethodManager imm = (InputMethodManager) thisActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

    }

    @Override
    public void onResume() {
        super.onResume();

        //taptarget userguide
        if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceAfterAddingStudents", thisActivity.getApplicationContext())) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (adapter.getItemCount() != 0) {
                        startOnboardGuideSequenceAfterAddingStudents();
                    }
                }
            }, 200);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        binding = FragmentStudentsBinding.inflate(inflater, container, false);
        studentSelectionViewModal = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(StudentSelectionViewModal.class);
        searchView = binding.studentSearchView;
        searchView.clearFocus();

        //Log.d("**student///Fetching**", "started++++++++++++++++++++++++ at time = " + FirebaseDao.getOnlyTime());
        displayStudentsList();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.btTakeAttendance.setVisibility(View.GONE);
                } else {
                    binding.btTakeAttendance.setVisibility(View.VISIBLE);
                    adapter.getFilter().filter("");
                    searchView.setQuery("", true);
                }
            }
        });

        binding.btTakeAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeAttendance();
            }
        });

        binding.ivAddStudentsEmptyStudentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createStudentAddingTypeChooser();
            }
        });
        return binding.getRoot();

    }

    private void createStudentAddingTypeChooser() {
        studentImportDialog = new Dialog(getContext(), R.style.StartDatePickerTheme);
        studentImportDialog.setContentView(R.layout.dialog_confirm_exit_take_attendace);
        TextView dialogDescription = (TextView) studentImportDialog.findViewById(R.id.dialog_description);
        dialogDescription.setText(R.string.text_dialog_adding_students);
        //dialogDescription.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        TextView tvAuto = studentImportDialog.findViewById(R.id.dialog_yes_button);
        TextView tvManual = studentImportDialog.findViewById(R.id.dialog_no_button);
        ImageView dialogIv = studentImportDialog.findViewById(R.id.dialog_iv);
        dialogIv.setImageResource(R.drawable.ic_import);

        tvAuto.setText("AUTO");
        tvManual.setText("MANUAL");

        tvAuto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                studentImportDialog.dismiss();
                createNoteForExcelSheetDialog();
            }
        });
        tvManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), StudentsEditorActivity.class);
                intent = FirebaseDao.passClassThroughIntent(currentClass, intent);
                startActivity(intent);
                studentImportDialog.dismiss();
            }
        });
        studentImportDialog.show();

        //user guide
        if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceBeforeAddingStudent", thisActivity.getApplicationContext())) {
            startOnboardGuideSequenceBeforeAddingStudent();
        }
    }

    private void createNoteForExcelSheetDialog() {
        studentImportDialog = new Dialog(getContext(), R.style.StartDatePickerTheme);
        studentImportDialog.setContentView(R.layout.error_screen);
        TextView dialogTitle = (TextView) studentImportDialog.findViewById(R.id.error_title);
        TextView dialogDescription = (TextView) studentImportDialog.findViewById(R.id.error_description);
        ImageView dialogIv = studentImportDialog.findViewById(R.id.iv_error_dialog);

        dialogDescription.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        Button btnOk = studentImportDialog.findViewById(R.id.btn_error_try_again);
        dialogIv.setImageResource(R.drawable.ic_excel);

        dialogTitle.setText(R.string.excel_import_note_title);
        dialogDescription.setText(R.string.excel_import_note_title_desc);
        btnOk.setText("OK");

        btnOk.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                addStudents();
                studentImportDialog.dismiss();
            }
        });
        studentImportDialog.show();
    }

    private void displayStudentsList() {

        adapter = new StudentsViewAdapter(getContext(), students, this, studentSelectionViewModal);
        recyclerView = binding.rvStudents;
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        try {
            FirebaseDao.
                    getStudentDbReference(currentClass.getmClassId())
                    .addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            students.clear();

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                students.add(dataSnapshot.getValue(Student.class));
                            }

                            FirebaseDao.setCurrentClassStudents(students);
                            adapter.notifyStudentsChanged(students);
                            //Log.d("**student///Fetching**", "endeed-----------------------at time = " + FirebaseDao.getOnlyTime());

                            if (adapter.getItemCount() == 0) {
                                binding.loadingStudentsView.setVisibility(View.GONE);
                                binding.emptyStudentsView.setVisibility(View.VISIBLE);
                                binding.btTakeAttendance.setVisibility(View.GONE);
                                binding.rvStudents.setVisibility(View.GONE);
                                if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceInEmptyStudents", thisActivity.getApplicationContext())) {
                                    startOnboardGuideSequenceInEmptyStudents();
                                }
                            } else {
                                binding.loadingStudentsView.setVisibility(View.GONE);
                                binding.emptyStudentsView.setVisibility(View.GONE);
                                binding.btTakeAttendance.setVisibility(View.VISIBLE);
                                binding.rvStudents.setVisibility(View.VISIBLE);

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        recyclerView.setAdapter(adapter);

    }

    public void addStudents() {

        if ((ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)) {
            selectExcelfile();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        }
    }

    private void selectExcelfile() {

        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent();
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "select an excel file"),
                    102);

        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            startActivityForResult(intent, 102);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Uri fileUri = data.getData();
                    FetchStudentsFromExcelInBackground fetchStudentsFromExcelInBackground = new FetchStudentsFromExcelInBackground();
                    fetchStudentsFromExcelInBackground.execute(fileUri);

                } catch (Exception e) {
                    e.printStackTrace();
                    //Log.e("filePath", e.getMessage());
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String readExcelFile(Uri fileUri) {
        try {
            InputStream inputStream = thisActivity.getContentResolver().openInputStream(fileUri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            ArrayList<Student> studentsListFromExcelSheet = new ArrayList<>();

            int rowCount = sheet.getPhysicalNumberOfRows();
            if (rowCount > 0) {
                Pair<Integer, Integer> rollNoHeadCordinates = findRollNoHeadingRowCol(sheet);
                Pair<Integer, Integer> nameHeadCordinates = findNameHeadingRowCol(sheet);
                if (rollNoHeadCordinates == null) {
                    //Toast.makeText(thisActivity, "Roll No heading Not found", Toast.LENGTH_SHORT).show();
                    return getString(R.string.excel_import_error_rollno_heading);
                }
                if (nameHeadCordinates == null) {
                    //Toast.makeText(thisActivity, "Students Name heading Not found", Toast.LENGTH_SHORT).show();
                    return getString(R.string.excel_import_error_name_heading);
                }

                for (int r = rollNoHeadCordinates.first + 1; r < rowCount; r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) {
                        continue;
                    }
                    try {
                        int rollNoCellType = row.getCell(rollNoHeadCordinates.second).getCellType();
                        int nameCellType = row.getCell(nameHeadCordinates.second).getCellType();
                        String studentName;
                        String studentRollNo;
                        if (rollNoCellType == Cell.CELL_TYPE_NUMERIC) {
                            studentRollNo = String.valueOf((long) (row.getCell(rollNoHeadCordinates.second).getNumericCellValue()));
                        } else if (rollNoCellType == Cell.CELL_TYPE_STRING) {
                            studentRollNo = row.getCell(rollNoHeadCordinates.second).getStringCellValue();
                        } else {
                            continue;
                        }
                        if (nameCellType == Cell.CELL_TYPE_STRING) {
                            studentName = row.getCell(nameHeadCordinates.second).getStringCellValue();
                        } else {
                            studentName = "Add name";
                        }
                        String studentAttendedClasses = "0";
                        String studentAttendancePercent = "100";
                        Student student = new Student(String.valueOf(studentRollNo),
                                studentName, studentAttendedClasses, studentAttendancePercent);
                        studentsListFromExcelSheet.add(student);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
            if (studentsListFromExcelSheet.isEmpty()) {
                return getString(R.string.excel_import_error_empty_wrong);
            } else {
                String studentsListErrorAfterFetching = checkStudentsList(studentsListFromExcelSheet);
                if (studentsListErrorAfterFetching == null) {
                    FirebaseDao.setCurrentClassDeliveredClasses(0);
                    FirebaseDao.insertStudents(currentClass.getmClassId(), studentsListFromExcelSheet);
                    return null;
                } else {
                    return studentsListErrorAfterFetching;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return getString(R.string.excel_import_error_file_invalid);
        } catch (IOException e) {
            e.printStackTrace();
            return getString(R.string.excel_import_error_io_exception);
        }
    }

    private String checkStudentsList(ArrayList<Student> students) {
        int iterator = 0;
        for (Student student : students) {
            if (!isNumeric(student.getStudentRollNo())) {
                return getString(R.string.excel_import_error_alphanumeric);
            }
            if (iterator > 150) {
                break;
            }
            iterator++;
        }
        if (students.size() > Integer.parseInt(getString(R.string.max_students_in_class))) {
            return getString(R.string.excel_import_error_150_students);
        }
        return null;
    }

    private static boolean isAlphanumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (!Character.isLetterOrDigit(ch)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (!Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }


    private Pair<Integer, Integer> findNameHeadingRowCol(XSSFSheet sheet) {
        int rows = sheet.getPhysicalNumberOfRows();
        int coloumns;
        int rowOffSet = 0, colOffSet = 0;
        for (int i = 0; i < rows + rowOffSet && i < 200; i++) {
            XSSFRow row = sheet.getRow(i);
            if (row == null) {
                rowOffSet++;
                continue;
            }
            coloumns = row.getPhysicalNumberOfCells();
            for (int j = 0; j < coloumns + colOffSet && j < 200; j++) {
                XSSFCell cell = row.getCell(j);
                if (cell == null) {
                    colOffSet++;
                    continue;
                }
                int cellType = cell.getCellType();
                String cellValue = "";
                if (cellType == Cell.CELL_TYPE_STRING) {
                    cellValue = row.getCell(j).getStringCellValue();
                    cellValue = removeSpecialCharacter(cellValue);
                    if ((cellValue.toLowerCase().contains("studentname") ||
                            cellValue.toLowerCase().contains("studentsname") ||
                            cellValue.toLowerCase().contains("name")) &&
                            !cellValue.toLowerCase().contains("subject")) {
                        return new Pair<>(i, j);
                    }
                } else {
                    continue;
                }
            }
        }
        return null;
    }

    private Pair<Integer, Integer> findRollNoHeadingRowCol(XSSFSheet sheet) {
        int rows = sheet.getPhysicalNumberOfRows();
        int coloumns;
        int rowOffSet = 0, colOffSet = 0;
        for (int i = 0; i < rows + rowOffSet && i < 200; i++) {
            XSSFRow row = sheet.getRow(i);
            if (row == null) {
                rowOffSet++;
                continue;
            }
            coloumns = row.getPhysicalNumberOfCells();
            for (int j = 0; j < coloumns + colOffSet && j < 200; j++) {
                XSSFCell cell = row.getCell(j);
                if (cell == null) {
                    colOffSet++;
                    continue;
                }
                int cellType = cell.getCellType();
                String cellValue = "";
                if (cellType == Cell.CELL_TYPE_STRING) {
                    cellValue = row.getCell(j).getStringCellValue();
                    cellValue = removeSpecialCharacter(cellValue);
                    if ((cellValue.toLowerCase().contains("rollno") ||
                            cellValue.toLowerCase().contains("rollnumber") ||
                            cellValue.toLowerCase().contains("enrollmentno") ||
                            cellValue.toLowerCase().contains("enrollmentnumber"))) {
                        return new Pair<>(i, j);
                    }
                } else {
                    continue;
                }
            }
        }
        return null;
    }

    String removeSpecialCharacter(String str) {
        char[] s = str.toCharArray();
        int j = 0;
        for (int i = 0; i < s.length; i++) {

            // Store only valid characters
            if ((s[i] >= 'A' && s[i] <= 'Z')
                    || (s[i] >= 'a' && s[i] <= 'z')) {
                s[j] = s[i];
                j++;
            }
        }
        return String.valueOf(s);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
        startActivitySafely(currentClass, TakeAttendanceActivity.class);
        //startActivity(new Intent(getContext(), TakeAttendanceActivity.class));
    }

    private void createDeleteConfirmationDialog(ActionMode mode) {
        Dialog dialog = new Dialog(getContext(), R.style.StartDatePickerTheme);
        dialog.setContentView(R.layout.dialog_confirm_exit_take_attendace);
        TextView dialogTextDescription = dialog.findViewById(R.id.dialog_description);
        dialogTextDescription.setText("Are you sure you want to delete");
        ImageView dialogIv = dialog.findViewById(R.id.dialog_iv);
        dialogIv.setImageResource(R.drawable.ic_delete);
        dialogIv.setColorFilter(getContext().getResources().getColor(R.color.red_theme_color));
        dialog.findViewById(R.id.dialog_yes_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDao.deleteStudents(currentClass.getmClassId(), studentSelectionViewModal.selectedStudents);
                ArrayList<Student> selectedStudents = new ArrayList<>();
                selectedStudents.addAll(studentSelectionViewModal.selectedStudents);
                UpdateAttendanceDataBaseInBackground updateAttendanceDataBaseInBackground = new UpdateAttendanceDataBaseInBackground();
                updateAttendanceDataBaseInBackground.execute(selectedStudents);
                studentSelectionViewModal.selectedStudents.clear();
                studentSelectionViewModal.setSelectedStudentsLiveData();
                isMultipleStudentSelected = false;
                isOneStudentSelected = false;
                dialog.dismiss();
                mode.finish();

            }
        });
        dialog.findViewById(R.id.dialog_no_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mode.finish();
            }
        });
        dialog.show();
    }

    private void checkStudentSlection() {
        ActionMode.Callback callback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.item_selection_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.action_edit:
                        FirebaseDao.setCurrentStudent(studentSelectionViewModal.selectedStudents.get(0));
                        Intent intent = new Intent(getContext(), StudentsEditorActivity.class);
                        intent = FirebaseDao.passClassThroughIntent(currentClass, intent);
                        startActivity(intent.putExtra("startingRollNo", studentSelectionViewModal.selectedStudents.get(0).getStudentRollNo()));
                        mode.finish();
                        break;
                    case R.id.action_delete:
                        createDeleteConfirmationDialog(mode);
                        break;
                    case R.id.action_select_all:
                        studentSelectionViewModal.selectAllStudents(FirebaseDao.getCurrentClassStudents());
                        adapter.notifyDataSetChanged();
                        break;

                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                studentSelectionViewModal.clearSelectedStudents();
                adapter.notifyDataSetChanged();
                isMultipleStudentSelected = false;
                isOneStudentSelected = false;
                actionMode = null;
            }
        };
        actionMode = ((AppCompatActivity) getContext()).startActionMode(callback);

        studentSelectionViewModal.getSelectedStudentsLiveData().observe(requireActivity(), new Observer<ArrayList<Student>>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onChanged(@NonNull ArrayList<Student> students) {

                if (students.size() == 0) {
                    isOneStudentSelected = false;
                    isMultipleStudentSelected = false;
                } else {
                    if (students.size() == 1) {
                        isOneStudentSelected = true;
                        isMultipleStudentSelected = false;
                    } else {
                        isMultipleStudentSelected = true;
                        isOneStudentSelected = false;
                    }

                    actionMode.setTitle(students.size() + " Selected");
                    Menu menu = actionMode.getMenu();
                    if (isOneStudentSelected) {
                        //edit menu item diasble
                        menu.getItem(0).setVisible(true);
                        adapter.notifyDataSetChanged();
                    } else {
                        menu.getItem(0).setVisible(false);
                    }
                    if (students.size() == FirebaseDao.getCurrentClassStudents().size()) {
                        menu.getItem(2).setVisible(false);
                    } else {
                        menu.getItem(2).setVisible(true);
                    }
                }
            }
        });

    }

    class UpdateAttendanceDataBaseInBackground extends AsyncTask<ArrayList<Student>, Void, Void> {

        @Override
        protected Void doInBackground(ArrayList<Student>... selectedStudents) {
            ArrayList<Pair<String, ArrayList<Attendance>>> allRollNosAttendanceWithDate = new ArrayList<>();
            FirebaseDao.getClassAttedanceDbReference(currentClass.getmClassId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    DataSnapshot attendanceDateDb = task.getResult();
                    for (DataSnapshot year : attendanceDateDb.getChildren()) {
                        for (DataSnapshot month : year.getChildren()) {
                            for (DataSnapshot date : month.getChildren()) {
                                for (DataSnapshot time : date.getChildren()) {
                                    String attendanceDate = (year.getKey() + "/" + month.getKey() + "/" + date.getKey() + "/" + time.getKey());
                                    allRollNosAttendanceWithDate.add(new Pair<String, ArrayList<Attendance>>(attendanceDate, time.getValue(genericTypeIndicator)));
                                }
                            }
                        }
                    }
                    for (Pair<String, ArrayList<Attendance>> eachDateAttendance : allRollNosAttendanceWithDate) {
                        for (Student student : selectedStudents[0]) {
                            for (Attendance attendance : eachDateAttendance.second) {
                                if (student.getStudentRollNo().equals(attendance.getRollNo())) {
                                    eachDateAttendance.second.remove(attendance);
                                    break;
                                }
                            }
                        }
                    }

                    Map<String, ArrayList<Attendance>> TT = new HashMap<>();
                    Map<String, Map<String, ArrayList<Attendance>>> DD = new HashMap<>();
                    Map<String, Map<String, Map<String, ArrayList<Attendance>>>> MM = new HashMap<>();
                    Map<String, Map<String, Map<String, Map<String, ArrayList<Attendance>>>>> YYYY = new HashMap<>();

                    String time = "";
                    String date = "";
                    String month = "";
                    String year = "";

                    for (Pair<String, ArrayList<Attendance>> eachDateAttendance : allRollNosAttendanceWithDate) {
                        String[] YYYYMMDDTT = eachDateAttendance.first.split("/");
                        time = YYYYMMDDTT[3];

                        if(!date.equals(YYYYMMDDTT[2])){
                            date = YYYYMMDDTT[2];
                            TT=new HashMap<>();
                        }
                        if(!month.equals(YYYYMMDDTT[1])){
                            month = YYYYMMDDTT[1];
                            DD=new HashMap<>();
                        }
                        if(!year.equals(YYYYMMDDTT[0])){
                            year = YYYYMMDDTT[0];
                            MM=new HashMap<>();
                        }


                        TT.put(time, eachDateAttendance.second);
                        DD.put(date, TT);
                        MM.put(month, DD);
                        YYYY.put(year, MM);
                    }
                    FirebaseDao.updateAttendance(currentClass.getmClassId(), YYYY);
                }
            });

            return null;
        }
    }

    @Override
    public void myOnClickStudentsList(Student student, ImageView imageView) {

        if (isOneStudentSelected || isMultipleStudentSelected) {
            studentSelectionViewModal.selectOneStudents(student);
            if (actionMode != null && !isMultipleStudentSelected && !isOneStudentSelected) {
                actionMode.finish();
            }
            if (imageView.getVisibility() == View.VISIBLE) {
                imageView.setVisibility(View.INVISIBLE);
            } else {
                imageView.setVisibility(View.VISIBLE);
                //haptic feedback
                thisActivity.getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);

            }
        } else {
            if (student.getStudentName().equals("Add Name")) {
                FirebaseDao.setCurrentStudent(student);
                Intent intent = new Intent(getContext(), StudentsEditorActivity.class);
                intent = FirebaseDao.passClassThroughIntent(currentClass, intent);
                startActivity(intent.putExtra("startingRollNo", student.getStudentRollNo()));
            }
        }
    }

    @Override
    public void myOnLongClickStudentsList(Student student, ImageView imageView) {
        if (actionMode == null) {
            checkStudentSlection();
        }
        if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceStudentMenuItem", thisActivity.getApplicationContext())) {
            startOnboardGuideSequenceStudentMenuItem();
        }
        studentSelectionViewModal.selectOneStudents(student);
        if (actionMode != null && !isMultipleStudentSelected && !isOneStudentSelected) {
            actionMode.finish();
        }
        if (imageView.getVisibility() == View.VISIBLE) {
            imageView.setVisibility(View.INVISIBLE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            //haptic feedback
            thisActivity.getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void myOnClickStudentsOverview(Student student, ImageView ivStudentSelected) {

        Dialog studentOverviewDialog = new Dialog(getContext());
        studentOverviewDialog.setContentView(R.layout.student_card);

        Window window = studentOverviewDialog.getWindow();
        window.setBackgroundDrawableResource(R.drawable.flag_transparent);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        View view = studentOverviewDialog.findViewById(R.id.student_card_rv_item);
        view.setElevation(0);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                studentOverviewDialog.dismiss();
            }
        });

        // Get the current layout parameters of the view
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

        // Set the new margins
        int newMargin = 100;

        // Change this value to your desired margin in pixels
        layoutParams.setMargins(newMargin + 60, newMargin + 300, newMargin + 60, newMargin + 200);

        // Apply the updated layout parameters to the view
        view.setLayoutParams(layoutParams);


        TextView tvRollNo;
        TextView tvName;
        TextView tvAttendance;  //format "18/20\n90%"

        tvRollNo = studentOverviewDialog.findViewById(R.id.tv_card_student_roll_no);
        tvName = studentOverviewDialog.findViewById(R.id.tv_card_student_name);
        tvAttendance = studentOverviewDialog.findViewById(R.id.tv_card_student_attendance);

        String rollNo = String.valueOf(Long.parseLong(student.getStudentRollNo()) % 1000);
        String name = student.getStudentName();
        String attendance = student.getStudentAttendedClasses() + "/" + FirebaseDao.getCurrentClassDeliveredClasses() + "\n" + student.getStudentAttendancePercent() + "%";

        tvRollNo.setText(rollNo);
        tvName.setText(name);
        tvAttendance.setText(attendance);

        studentOverviewDialog.show();

        //popupWindow.showAtLocation(getView(), Gravity.CENTER,0,0);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.close();
        inflater.inflate(R.menu.add_students_menu, menu);
        menu.getItem(0).getIcon().setTint(thisActivity.getResources().getColor(R.color.white));
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (FirebaseDao.getCurrentClassStudents().size() != 0) {
                    Intent intent = new Intent(getActivity(), StudentsEditorActivity.class);
                    intent = FirebaseDao.passClassThroughIntent(currentClass, intent);
                    startActivity(intent.putExtra(getString(R.string.intent_extra_add_new_student), true));
                } else {
                    createStudentAddingTypeChooser();
                }
                return true;
            }
        });
    }

    class FetchStudentsFromExcelInBackground extends AsyncTask<Uri, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoadingScreen("Fetching Students List\nPlease wait");
        }

        @Override
        protected void onPostExecute(String status) {
            hideLoadingScreen();
            if (status != null) {
                createExcelImportErrorDialog(status);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected String doInBackground(Uri... fileUri) {
            return readExcelFile(fileUri[0]);
        }

    }

    private void createExcelImportErrorDialog(String error) {
        studentImportDialog = new Dialog(getContext(), R.style.StartDatePickerTheme);
        studentImportDialog.setContentView(R.layout.error_screen);
        TextView dialogTitle = (TextView) studentImportDialog.findViewById(R.id.error_title);
        TextView dialogDescription = (TextView) studentImportDialog.findViewById(R.id.error_description);
        ImageView dialogIv = studentImportDialog.findViewById(R.id.iv_error_dialog);

        dialogDescription.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Button btnOk = studentImportDialog.findViewById(R.id.btn_error_try_again);
        dialogIv.setImageResource(R.drawable.ic_error);

        dialogTitle.setText("Excel Import Error");
        dialogDescription.setText(error);// + "\n\nTip : \n");
        btnOk.setText("OK");

        btnOk.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                studentImportDialog.dismiss();
            }
        });
        studentImportDialog.show();
    }

    private void showLoadingScreen(String message) {
        binding.fetchingStudentsScreen.setVisibility(View.VISIBLE);
        binding.tvFetchingStudentsScreen.setText(message);
    }

    private void hideLoadingScreen() {
        binding.fetchingStudentsScreen.setVisibility(View.GONE);
    }


    void startActivitySafely(Classes currentClass, Class<TakeAttendanceActivity> toActivity) {
        try {
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
                    //FirebaseDao.setCurrentClass(currentClass);

                    Intent intent = new Intent(getContext(), toActivity);
                    intent = FirebaseDao.passClassThroughIntent(currentClass, intent);
                    // Post result to main/UI thread using the handler
                    Intent finalIntent = intent;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Update UI or perform other tasks on main/UI thread
                            // similar to onPostExecute() in AsyncTask
                            startActivity(finalIntent);
                        }
                    });
                }
            });

            // Update UI or perform other tasks on main/UI thread before background task
            // similar to onPreExecute() in AsyncTask

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //onboarding taptarget userguide functions

    public void startOnboardGuideSequenceInEmptyStudents() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(binding.ivAddStudentsEmptyStudentView);

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();
            targetArrayList.add(TargetViewMaker.makeTapTargetWithoutTintTarget(binding.ivAddStudentsEmptyStudentView, getString(R.string.tap_title_import_students), getString(R.string.tap_title_import_students_des), 90));

            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequence(thisActivity);
        } catch (Exception exception) {

        }
    }

    public void startOnboardGuideSequenceBeforeAddingStudent() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(binding.ivAddStudentsEmptyStudentView);

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();
            targetArrayList.add(TargetViewMaker.makeTapTargetWithoutTintTarget(studentImportDialog.findViewById(R.id.dialog_yes_button), getString(R.string.tap_title_auto_add_students), getString(R.string.tap_title_auto_add_students_des), 70));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithoutTintTarget(studentImportDialog.findViewById(R.id.dialog_no_button), getString(R.string.tap_title_manual_add_students), getString(R.string.tap_title_manual_add_students_des), 70));

            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequenceForDialog(studentImportDialog, thisActivity);
        } catch (Exception exception) {

        }
    }

    @SuppressLint("RestrictedApi")
    public void startOnboardGuideSequenceAfterAddingStudents() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(binding.btTakeAttendance);

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();
            targetArrayList.add(TargetViewMaker.makeTapTargetWithoutTintTarget(linearLayoutManager.findViewByPosition(linearLayoutManager.findFirstCompletelyVisibleItemPosition()).findViewById(R.id.tv_student_attendance_percent), getString(R.string.tap_title_student_attendance_in_list), getString(R.string.tap_title_student_attendance_in_list_des), 60));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithoutTintTarget(linearLayoutManager.findViewByPosition(linearLayoutManager.findFirstCompletelyVisibleItemPosition()), getString(R.string.tap_title_select_student), getString(R.string.tap_title_select_student_des), 150));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(thisActivity.findViewById(R.id.add_students_menu_item), getString(R.string.tap_title_add_single_student), getString(R.string.tap_title_add_single_student_desc), 50));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithoutTintTarget(thisActivity.findViewById(R.id.bt_take_attendance), getString(R.string.tap_title_take_attendance), getString(R.string.tap_title_take_attendance_desc), 150));

            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequence(thisActivity);
        } catch (Exception exception) {

        }
    }

    public void startOnboardGuideSequenceStudentMenuItem() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(binding.btTakeAttendance);

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(thisActivity.findViewById(R.id.action_edit), getString(R.string.tap_title_edit_student), getString(R.string.tap_title_edit_student_desc), 60));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(thisActivity.findViewById(R.id.action_delete), getString(R.string.tap_title_delete_student), getString(R.string.tap_title_delete_student_desc), 60));

            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequence(thisActivity);
        } catch (Exception exception) {

        }
    }

}


class FileUtil {
    /*
     * Gets the file path of the given Uri.
     */
    @SuppressLint("NewApi")
    public static String getPath(Uri uri, Context context) {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;

        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final String[] split = id.split(":");
                final String type = split[0];
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };

                uri = Uri.parse(MediaStore.Downloads.EXTERNAL_CONTENT_URI + "/" + split[1]);
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:", "");
                }
                if (id.startsWith("msf:")) {
                    id.replaceFirst("image:", "primary:");
                }
                //uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
                switch (type) {
                    case "image":
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "document":
                        uri = MediaStore.getMediaUri(context, uri);
                        break;
                    case "msf":
                        uri = Uri.parse(MediaStore.Downloads.EXTERNAL_CONTENT_URI + "/" + split[1]);
                        break;
                    case "video":
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "audio":
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        break;
                }
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    return cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("on getPath", "Exception " + e.getMessage());
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
