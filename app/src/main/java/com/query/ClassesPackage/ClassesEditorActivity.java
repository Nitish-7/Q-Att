package com.query.ClassesPackage;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.query.ClassesPackage.ClassesData.Classes;
import com.query.ClassesPackage.ClassesData.ClassesContracts.ClassEntry;
import com.query.ClassesPackage.ClassesData.ClassesViewModel;
import com.query.InitializerPackage.ConnectivityReceiver;
import com.query.FirebaseDao;
import com.query.R;
import com.query.databinding.ActivityClassesEditerBinding;

import java.util.ArrayList;

public class ClassesEditorActivity extends AppCompatActivity {

    /**
     * EditText field to enter the pet's name
     */
    private EditText mClassNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mSubjectNameEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mSubjectCodeEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mClassSectionSpinner;

    private Spinner mClassYearSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mClassSection = 0;
    private int mClassYear = 0;

    //private ContentValues petRowValues = new ContentValues();

    //PetDbHelper mDbHelper=new PetDbHelper(this);

    ClassesViewModel classesViewModel;
    Classes classToEdit;

    private boolean isEnteredAll = false;

    ActivityClassesEditerBinding binding;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //FirebaseDao.setCurrentClass(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().hasExtra(ClassEntry.COLUMN_CLASS_ID)){
            classToEdit=FirebaseDao.getClassThroughIntent(getIntent());
        }else {
            classToEdit=null;
        }
        //classToEdit = FirebaseDao.getCurrentClass();
        binding = ActivityClassesEditerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        classesViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(ClassesViewModel.class);

        // Find all relevant views that we will need to read user input from
        mClassNameEditText = (EditText) findViewById(R.id.edit_class_name);

        mSubjectNameEditText = (EditText) findViewById(R.id.edit_subject_name);

        mSubjectCodeEditText = (EditText) findViewById(R.id.edit_subject_code);

        mClassSectionSpinner = (Spinner) findViewById(R.id.spinner_class_section);
        setupClassSectionSpinner();

        mClassYearSpinner = (Spinner) findViewById(R.id.spinner_class_year);
        setupClassYearSpinner();

        if (classToEdit != null) {
            //setTitle("Edit Class " + intent.getStringExtra("petName"));
            editClass();
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void editClass() {

        mClassNameEditText.setText((classToEdit).getmClassName());
        mSubjectNameEditText.setText((classToEdit).getmSubjectName());
        mSubjectCodeEditText.setText((classToEdit).getmSubjectCode());
        if (classToEdit.getmYear() < getResources().getStringArray(R.array.array_year_options).length && classToEdit.getmYear() >= 0) {
            mClassYearSpinner.setSelection(classToEdit.getmYear() + 2);
        } else {
            Pair<Integer, Integer> session = FirebaseDao.getSession().first;
            if (session.first == classToEdit.getmYear()) {
                mClassYearSpinner.setSelection(1);
            } else if (session.second == classToEdit.getmYear()) {
                mClassYearSpinner.setSelection(2);
            }else {
                mClassYearSpinner.setSelection(0);
        }
    }
        mClassSectionSpinner.setSelection(classToEdit.getmSection());
//
}

    public void insertClass() {

        String className = mClassNameEditText.getText().toString().trim();
        String subjectName = mSubjectNameEditText.getText().toString().trim();
        String subjectCode = mSubjectCodeEditText.getText().toString().trim();
        int classSection = mClassSection;
        int classYear = mClassYear;

        if (className.isEmpty()) {
            mClassNameEditText.setError("*");
            mClassNameEditText.requestFocus();
            isEnteredAll = false;
        } else if (subjectName.isEmpty()) {
            mSubjectNameEditText.setError("*");
            mSubjectNameEditText.requestFocus();
            isEnteredAll = false;
        } else if (classYear == 0) {
            Toast.makeText(this, "Select Session or Semester", Toast.LENGTH_SHORT).show();
            mSubjectNameEditText.requestFocus();
            isEnteredAll = false;
        } else
            isEnteredAll = true;

        if (classToEdit != null && isEnteredAll) {
            //classesViewModel.delete((clickedClass));
            FirebaseDao.updateClassDetailes(new Classes(classToEdit.getmClassId(), className, subjectName, subjectCode, classSection, classYear));
            Toast.makeText(getApplicationContext(), "class Updated", Toast.LENGTH_SHORT).show();

        } else if (classToEdit == null && isEnteredAll) {
            //classesViewModel.insert(new Classes("-1", className, subjectName, subjectCode, classSection, classYear));
            FirebaseDao.insertClassDetailes(new Classes("-1", className, subjectName, subjectCode, classSection, classYear));
            Toast.makeText(getApplicationContext(), "class added", Toast.LENGTH_SHORT).show();
        }


    }


    private void setupClassSectionSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter classSectionSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_section_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        classSectionSpinnerAdapter.setDropDownViewResource(R.layout.spinner_custom_dropdown);

        // Apply the adapter to the spinner
        mClassSectionSpinner.setAdapter(classSectionSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mClassSectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.sec_a))) {
                        mClassSection = ClassEntry.SEC_A;
                    } else if (selection.equals(getString(R.string.sec_b))) {
                        mClassSection = ClassEntry.SEC_B;
                    } else if (selection.equals(getString(R.string.sec_c))) {
                        mClassSection = ClassEntry.SEC_C;
                    } else if (selection.equals(getString(R.string.sec_d))) {
                        mClassSection = ClassEntry.SEC_D;
                    } else if (selection.equals(getString(R.string.sec_e))) {
                        mClassSection = ClassEntry.SEC_E;
                    } else if (selection.equals(getString(R.string.sec_e))) {
                        mClassSection = ClassEntry.SEC_E;
                    } else if (selection.equals(getString(R.string.sec_f))) {
                        mClassSection = ClassEntry.SEC_F;
                    } else if (selection.equals(getString(R.string.sec_g))) {
                        mClassSection = ClassEntry.SEC_G;
                    } else if (selection.equals(getString(R.string.sec_h))) {
                        mClassSection = ClassEntry.SEC_H;
                    } else {
                        mClassSection = ClassEntry.SEC_SEC;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (classToEdit != null) {
                    mClassSection = classToEdit.getmSection();
                } else
                    mClassSection = 0;// sec
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupClassYearSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout

        String[] items = getResources().getStringArray(R.array.array_year_options);

        ArrayList<String> yrSessionItemArrayList = new ArrayList<>();
        //getting 2 session according to date
        Pair<String, String> stringSessions = FirebaseDao.getSession().second;
        Pair<Integer, Integer> intSessions = FirebaseDao.getSession().first;
        yrSessionItemArrayList.add(items[0]);

        yrSessionItemArrayList.add(stringSessions.first);
        yrSessionItemArrayList.add(stringSessions.second);

        for (int i = 1; i < items.length; i++) {
            yrSessionItemArrayList.add(items[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yrSessionItemArrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        mClassYearSpinner.setAdapter(adapter);

        // Set the integer mSelected to the constant values
        mClassYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.year_1st))) {
                        mClassYear = ClassEntry.FIST;
                    } else if (selection.equals(getString(R.string.year_2nd))) {
                        mClassYear = ClassEntry.SECOND;
                    } else if (selection.equals(getString(R.string.year_3rd))) {
                        mClassYear = ClassEntry.THIRD;
                    } else if (selection.equals(getString(R.string.year_4th))) {
                        mClassYear = ClassEntry.FOURTH;
                    } else if (selection.equals(getString(R.string.year_5th))) {
                        mClassYear = ClassEntry.FIFTH;
                    } else if (selection.equals(getString(R.string.year_6th))) {
                        mClassYear = ClassEntry.SIXTH;
                    } else if (selection.equals(getString(R.string.year_7th))) {
                        mClassYear = ClassEntry.SEVENTH;
                    } else if (selection.equals(getString(R.string.year_8th))) {
                        mClassYear = ClassEntry.EIGHTH;
                    } else if (selection.equals(getString(R.string.year_9th))) {
                        mClassYear = ClassEntry.NINTH;
                    } else if (selection.equals(getString(R.string.year_10th))) {
                        mClassYear = ClassEntry.TENTH;
                    } else if (selection.equals(getString(R.string.year_11th))) {
                        mClassYear = ClassEntry.ELEVENTH;
                    } else if (selection.equals(getString(R.string.year_12th))) {
                        mClassYear = ClassEntry.TWELFTH;
                    } else if (selection.equals(stringSessions.first)) {
                        mClassYear = intSessions.first;
                    } else if (selection.equals(stringSessions.second)) {
                        mClassYear = intSessions.second;
                    } else {
                        mClassYear = ClassEntry.YEAR;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (classToEdit != null) {
                    mClassYear = classToEdit.getmYear();
                } else
                    mClassYear = 0;// sec
            }
        });

    }

    public void saveNewClass(View view) {
        insertClass();
        if (isEnteredAll) {
            finish();
        }
    }
}