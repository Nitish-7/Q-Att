package com.example.android.quickAttendance.ClassesPackage;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.android.quickAttendance.ClassesPackage.ClassesData.Classes;
import com.example.android.quickAttendance.FirebaseDao;
import com.example.android.quickAttendance.SettingsActivity;
import com.example.android.quickAttendance.R;
import com.example.android.quickAttendance.SignInActivity;
import com.example.android.quickAttendance.StudentsPackage.StudentsAndAttendanceActivity;
import com.example.android.quickAttendance.databinding.ActivityClassesBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import com.example.android.quickAttendance.ClassesPackage.ClassesData.ClassesViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ClassesActivity extends AppCompatActivity implements MyClickListener {

    ClassesViewModel classesViewModel;
    private ActivityClassesBinding binding;
    RecyclerView recyclerView;
    ClassesViewAdapter adapter;
    boolean itemSelected = false;
    ArrayList<Integer> selectedClasses = new ArrayList<>();
    ArrayList<Classes> classes = new ArrayList<Classes>();
    int selectOrNot = 1;

    public static int getTotalClasses() {
        return totalClasses;
    }

    public static int totalClasses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClassesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        selectedClasses.ensureCapacity(10);
        displayUsersClasses();


        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassesActivity.this, ClassesEditorActivity.class);
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        displayUsersClasses();

    }

    private void displayUsersClasses() {


        classesViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(ClassesViewModel.class);

        recyclerView = findViewById(R.id.petsRv);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        FirebaseDao.getUsersClassesDbReference().
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        classes.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            Classes dbClass= dataSnapshot.getValue(Classes.class);
                            Log.d("classes  : ", dbClass.getmClassName()+" "+dbClass.getmSubjectName()+" "+dbClass.getmSubjectCode()+" "+ dbClass.getmSection()+" "+dbClass.getmYear()+"");
                            classes.add(dbClass);
                        }
                        //adapter.notifyStudentsChanged(students);
                        adapter.updateClasses(classes);

                        if (adapter.getItemCount() == 0)
                            findViewById(R.id.empty_pet_view).setVisibility(View.VISIBLE);
                        else {
                            totalClasses = adapter.getItemCount();
                            Log.d("toatal class ", totalClasses+"");
                            findViewById(R.id.empty_pet_view).setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
//        classesViewModel.getAllClasses().observe(this, new Observer<List<Classes>>() {
//            @Override
//            public void onChanged(List<Classes> classes) {
//                adapter.updateClasses(classes);
//                if (adapter.getItemCount() == 0)
//                    findViewById(R.id.empty_pet_view).setVisibility(View.VISIBLE);
//                else {
//                    totalClasses = adapter.getItemCount();
//                    Toast.makeText(ClassesActivity.this, "total class " + totalClasses, Toast.LENGTH_SHORT).show();
//                    findViewById(R.id.empty_pet_view).setVisibility(View.GONE);
//                }
//            }
//        });

        adapter = new ClassesViewAdapter(this, this);
        recyclerView.setAdapter(adapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        //TODO 3 dot ki jgh profile picture, iske andar setting ko hatake profile uske andar dekhloonga ,tips ,rate,feedback,defaul attendace mode,guiding mode
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.sign_out:
//                TODO sign out pe clear cache
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                // Do nothing for now
                //petViewModel.insert(new Pets("Dodo","Pomoranian","40",1));
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_settings:
                // Do nothing for now
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void OnItemClick(Classes Class, int position, View classesItem) {
        if (itemSelected == false) {
            FirebaseDao.setCurrentClass(Class);
            Intent intent = new Intent(ClassesActivity.this, StudentsAndAttendanceActivity.class);
//            intent.putExtra(UserClassesDetailes.CLASS_ID,Class.getmClassId()+"");
//            intent.putExtra(UserClassesDetailes.CLASS_NAME,Class.getmClassName()+"");
//            intent.putExtra(UserClassesDetailes.CLASS_SUBJECT,Class.getmSubjectName()+"");
//            intent.putExtra(UserClassesDetailes.CLASS_SUBJECT_CODE,Class.getmSubjectCode()+"");
//            intent.putExtra(UserClassesDetailes.CLASS_YEAR,Class.getmYear()+"");
//            intent.putExtra(UserClassesDetailes.CLASS_YEAR,Class.getmSection()+"");
            startActivity(intent);
        } else {
            //select
            if (selectOrNot == 1) {
                selectedClasses.add(position);
                selectOrNot = 0;
                classesItem.setForeground(getResources().getDrawable(R.drawable.classes_seleted_fg));
            }
            //deselect
            else {
                selectOrNot = 1;
                selectedClasses.add(position);
                classesItem.setForeground(getResources().getDrawable(R.drawable.classes_unseleted_fg));
            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void OnLongClick(View classesItem, int position) {
        //TODO long click shi kro+ edit,delete pe option are you sure, back to unselect ,select all
        if (itemSelected == false) {
            itemSelected = true;
            adapter.updateClassesForForeground(itemSelected);
        }
        selectedClasses.add(position);
        classesItem.setForeground(getResources().getDrawable(R.drawable.classes_seleted_fg));

    }
}