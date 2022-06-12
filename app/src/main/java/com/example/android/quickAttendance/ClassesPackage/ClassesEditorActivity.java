package com.example.android.quickAttendance.ClassesPackage;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.quickAttendance.R;

import com.example.android.quickAttendance.ClassesPackage.ClassesData.ClassesViewModel;
import com.example.android.quickAttendance.ClassesPackage.ClassesData.ClassesContracts.ClassEntry;
import com.example.android.quickAttendance.ClassesPackage.ClassesData.Classes;
import com.example.android.quickAttendance.databinding.ActivityClassesEditerBinding;

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
    Classes clickedClass;

    private String petGenderChecker = "-1";

    private boolean isEnteredAll = false;

    ActivityClassesEditerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityClassesEditerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        classesViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(ClassesViewModel.class);

        Intent intent = getIntent();
        petGenderChecker = intent.getStringExtra("petGender");
//        if (intent.hasExtra("petName")) {
//            clickedPet = new Classes(intent.getStringExtra("petName"),
//                    intent.getStringExtra("petBreed"), intent.getStringExtra("petWeight"), Integer.valueOf(petGenderChecker));
//        }

        // Find all relevant views that we will need to read user input from
        mClassNameEditText = (EditText) findViewById(R.id.edit_class_name);

        mSubjectNameEditText = (EditText) findViewById(R.id.edit_subject_name);

        mSubjectCodeEditText = (EditText) findViewById(R.id.edit_subject_code);

        mClassSectionSpinner = (Spinner) findViewById(R.id.spinner_class_section);
        setupClassSectionSpinner();

        mClassYearSpinner = (Spinner) findViewById(R.id.spinner_class_year);
        setupClassYearSpinner();

        if (clickedClass != null) {
            setTitle("Edit Pet " + intent.getStringExtra("petName"));
            editPetActivity();
        }


    }

    private void editPetActivity() {

        mClassNameEditText.setText((clickedClass).getmClassName());
        mSubjectNameEditText.setText((clickedClass).getmSubjectName());
        mSubjectCodeEditText.setText((clickedClass).getmSubjectCode());
//        int petGender = Integer.valueOf(petGenderChecker);
//        switch (petGender) {
//            case PetEntry.GENDER_MALE:
//                mGenderSpinner.setSelection(petGender);
//                break;
//            case PetEntry.GENDER_FEMALE:
//                mGenderSpinner.setSelection(petGender);
//                break;
//            case PetEntry.GENDER_UNKNOWN:
//                mGenderSpinner.setSelection(petGender);
//                break;
//        }
    }

    public void insertClass() {

        String className = mClassNameEditText.getText().toString().trim();
        String subjectName = mSubjectNameEditText.getText().toString().trim();
        String subjectCode = mSubjectCodeEditText.getText().toString().trim();
        int classSection = mClassSection;
        int classYear =mClassYear;

        if (className.isEmpty() || subjectCode.isEmpty()) {
            isEnteredAll = false;
        } else
            isEnteredAll = true;

        if (clickedClass != null && isEnteredAll) {
            classesViewModel.delete((clickedClass));
            classesViewModel.insert(new Classes(-1,className, subjectName, subjectCode, classSection,classYear));
            Toast.makeText(getApplicationContext(), "class Updated", Toast.LENGTH_SHORT).show();

        } else if (clickedClass == null && isEnteredAll) {
            classesViewModel.insert(new Classes(-1,className, subjectName, subjectCode, classSection,classYear));
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
                Toast.makeText(ClassesEditorActivity.this, selection, Toast.LENGTH_SHORT).show();
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.sec_a))) {
                        mClassSection = ClassEntry.SEC_A;
                    } else if (selection.equals(getString(R.string.sec_b))) {
                        mClassSection = ClassEntry.SEC_B;
                    }  else if (selection.equals(getString(R.string.sec_c))) {
                        mClassSection = ClassEntry.SEC_C;
                    }
                    else if (selection.equals(getString(R.string.sec_d))) {
                        mClassSection = ClassEntry.SEC_D;
                    }
                    else if (selection.equals(getString(R.string.sec_e))) {
                        mClassSection = ClassEntry.SEC_E;
                    }
                    else if (selection.equals(getString(R.string.sec_e))) {
                        mClassSection = ClassEntry.SEC_E;
                    }
                    else if (selection.equals(getString(R.string.sec_f))) {
                        mClassSection = ClassEntry.SEC_F;
                    }
                    else if (selection.equals(getString(R.string.sec_g))) {
                        mClassSection = ClassEntry.SEC_G;
                    }
                    else if (selection.equals(getString(R.string.sec_h))) {
                        mClassSection = ClassEntry.SEC_H;
                    }
                    else {
                        mClassSection = ClassEntry.SEC_SEC;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mClassSection = 0;// sec
            }
        });

    }
    private void setupClassYearSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter classYearSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_year_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        classYearSpinnerAdapter.setDropDownViewResource(R.layout.spinner_custom_dropdown);

        // Apply the adapter to the spinner
        mClassYearSpinner.setAdapter(classYearSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mClassYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                Toast.makeText(ClassesEditorActivity.this, selection, Toast.LENGTH_SHORT).show();
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.year_1st))) {
                        mClassSection = ClassEntry.FIST;
                    } else if (selection.equals(getString(R.string.year_2nd))) {
                        mClassSection = ClassEntry.SECOND;
                    } else if (selection.equals(getString(R.string.year_3rd))) {
                        mClassSection = ClassEntry.THIRD;
                    }else if (selection.equals(getString(R.string.year_4th))) {
                        mClassSection = ClassEntry.FOURTH;
                    }else if (selection.equals(getString(R.string.year_5th))) {
                        mClassSection = ClassEntry.FIFTH;
                    }
                    else {
                        mClassYear = ClassEntry.YEAR;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mClassYear = 0;// sec
            }
        });

    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu options from the res/menu/menu_editor.xml file.
//        // This adds menu items to the app bar.
//        getMenuInflater().inflate(R.menu.menu_editor, menu);
//        if (clickedPet == null)
//            menu.findItem(R.id.action_delete).setVisible(false);
//        return true;
//
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // User clicked on a menu option in the app bar overflow menu
//        switch (item.getItemId()) {
//            // Respond to a click on the "Save" menu option
//            case R.id.action_save:
//                // insert new row for new pet
//                insertPet();
//                if (isEnteredAll)
//                    finish();
//                else
//                    Toast.makeText(getApplicationContext(), "Enter all remaining fields", Toast.LENGTH_SHORT).show();
//                return true;
//
//            // Respond to a click on the "Delete" menu option
//            case R.id.action_delete:
//                if (clickedPet != null)
//                    petViewModel.delete((clickedPet));
//                finish();
//                // Do nothing for now
//                return true;
//            // Respond to a click on the "Up" arrow button in the app bar
//            case android.R.id.home:
//                // Navigate back to parent activity (CatalogActivity)
//                NavUtils.navigateUpFromSameTask(this);
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    public void saveNewClass(View view) {
        insertClass();
        if (isEnteredAll)
            finish();
        else
            Toast.makeText(getApplicationContext(), "Enter all remaining fields", Toast.LENGTH_SHORT).show();
    }
}