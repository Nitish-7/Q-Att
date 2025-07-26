package com.query.ClassesPackage;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.ActionMode;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.query.ClassesPackage.ClassesData.Classes;
import com.query.ClassesPackage.ClassesData.ClassesViewModel;
import com.query.InitializerPackage.ConnectivityReceiver;
import com.query.FirebaseDao;
import com.query.OnboardingAndGuidePackage.TargetViewMaker;
import com.query.R;
import com.query.RegistrationPackage.SettingsActivity;
import com.query.RegistrationPackage.SignInActivity;
import com.query.StudentsPackage.StudentsAndAttendanceActivity;
import com.query.databinding.ActivityClassesBinding;

import java.io.File;
import java.util.ArrayList;

public class ClassesActivity extends AppCompatActivity implements com.query.ClassesPackage.MyClickListener {

    private static final String ATTENDANCE_FOLDER_NAME = "Attendance Record" + "/" + "Users" + "/" + FirebaseDao.getmCurrentUserId() + "/" + "Classes";
    ClassesViewModel classesViewModel;
    private ActivityClassesBinding binding;
    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;
    com.query.ClassesPackage.ClassesViewAdapter adapter;
    ArrayList<Classes> classes = new ArrayList<Classes>();
    public ActionMode actionMode = null;
    boolean isOneStudentSelected = false;
    boolean isMultipleStudentSelected = false;
    private MenuItem userAccountMenuItem;

    public static int getTotalClasses() {
        return totalClasses;
    }

    public static int totalClasses;

    public ConnectivityReceiver receiver;

    @Override
    protected void onPause() {
        super.onPause();
        receiver.endInternetReceiver();
        clearSearchFocusIfThere();
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
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityClassesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Log.d("**class==Fetching**", "started+++++++++++++++++++++++ at time = " + FirebaseDao.getOnlyTime());
        displayUsersClasses();

        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.getMenu().getItem(1).setEnabled(false);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassesActivity.this, ClassesEditorActivity.class);
                startActivity(intent);
            }
        });

        binding.classesSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    adapter.getFilter().filter(newText);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
        binding.classesSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    adapter.getFilter().filter("");
                    binding.classesSearchView.setQuery("", true);
                }
            }
        });
        binding.bottomNavigationView.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (binding.premiumClassesView.getVisibility() == View.VISIBLE) {
                    if (adapter.getItemCount() == 0) {
                        binding.emptyClassesView.setVisibility(View.VISIBLE);
                        binding.premiumClassesView.setVisibility(View.GONE);
                        setTitle(R.string.title_activity_classes);
                        return false;
                    } else {
                        binding.classesView.setVisibility(View.VISIBLE);
                        binding.premiumClassesView.setVisibility(View.GONE);
                        setTitle(R.string.title_activity_classes);
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });
        binding.bottomNavigationView.getMenu().getItem(2).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (binding.classesView.getVisibility() == View.VISIBLE && binding.premiumClassesView.getVisibility() == View.GONE) {
                    binding.classesView.setVisibility(View.GONE);
                    binding.premiumClassesView.setVisibility(View.VISIBLE);
                    setTitle(R.string.title_activity_classes_time_table);
                    return false;
                } else if (binding.emptyClassesView.getVisibility() == View.VISIBLE && binding.premiumClassesView.getVisibility() == View.GONE) {
                    binding.emptyClassesView.setVisibility(View.GONE);
                    binding.premiumClassesView.setVisibility(View.VISIBLE);
                    setTitle(R.string.title_activity_classes_time_table);
                    return false;
                } else {
                    return false;
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseDao.getCurrentFirebaseUser() == null) {
            startActivity(new Intent(ClassesActivity.this, SignInActivity.class));
            finish();
        }
    }


    private void clearSearchFocusIfThere() {
        binding.classesSearchView.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.classesSearchView.getWindowToken(), 0);

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void displayUsersClasses() {
        classesViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(ClassesViewModel.class);
        recyclerView = findViewById(R.id.classesRv);
        gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);


        try {
            FirebaseDao.getUsersClassesDbReference().
                    addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            classes.clear();

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Classes dbClass = dataSnapshot.getValue(Classes.class);
                                classes.add(dbClass);
                            }
                            FirebaseDao.setCurrentUserClasses(classes);
                            adapter.updateClasses(classes);
                            //Log.d("**class==Fetching**", "endeed-----------------------at time = " + FirebaseDao.getOnlyTime());
                            if (adapter.getItemCount() == 0) {
                                findViewById(R.id.loading_classes_view).setVisibility(View.GONE);
                                findViewById(R.id.classes_view).setVisibility(View.GONE);
                                binding.premiumClassesView.setVisibility(View.GONE);
                                findViewById(R.id.empty_classes_view).setVisibility(View.VISIBLE);
                                binding.bottomAppBar.setVisibility(View.VISIBLE);
                                binding.fab.setVisibility(View.VISIBLE);
                                //taptarget userguide
                                if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceInEmptyClasses", ClassesActivity.this)) {
                                    startOnboardGuideSequenceInEmptyClasses();
                                }
                            } else {
                                totalClasses = adapter.getItemCount();
                                findViewById(R.id.classes_view).setVisibility(View.VISIBLE);
                                binding.premiumClassesView.setVisibility(View.GONE);
                                findViewById(R.id.loading_classes_view).setVisibility(View.GONE);
                                findViewById(R.id.empty_classes_view).setVisibility(View.GONE);
                                binding.bottomAppBar.setVisibility(View.VISIBLE);
                                binding.fab.setVisibility(View.VISIBLE);

                                //taptarget userguide
                                if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceAfterAddingClass", ClassesActivity.this)) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startOnboardGuideSequenceAfterAddingClass();
                                        }
                                    }, 2000);
                                }
                            }
                            binding.bottomNavigationView.getMenu().getItem(0).setChecked(true);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

            adapter = new com.query.ClassesPackage.ClassesViewAdapter(this, this, classesViewModel, classes);
            recyclerView.setAdapter(adapter);

            } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.

        getMenuInflater().inflate(R.menu.profile_menu, menu);
        userAccountMenuItem = menu.findItem(R.id.user_account_menu_item);

        //userAccountMenuItem.setVisible(false);
        userAccountMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(ClassesActivity.this, SettingsActivity.class));
                return true;
            }
        });
        return true;
    }


    private void createDeleteConfirmationDialog(ActionMode mode) {
        Dialog dialog = new Dialog(this, R.style.StartDatePickerTheme);
        dialog.setContentView(R.layout.dialog_confirm_exit_take_attendace);
        TextView dialogTextDescription = dialog.findViewById(R.id.dialog_description);
        dialogTextDescription.setText("Are you sure you want to delete");
        ImageView dialogIv = dialog.findViewById(R.id.dialog_iv);
        dialogIv.setImageResource(R.drawable.ic_delete);
        dialogIv.setColorFilter(getResources().getColor(R.color.red_theme_color));
        dialog.findViewById(R.id.dialog_yes_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Classes classes : classesViewModel.selectedClasses) {
                    deleteClassAttendanceFolder(classes.getmClassId());
                }
                FirebaseDao.deleteClassDetailes(classesViewModel.selectedClasses);
                classesViewModel.selectedClasses.clear();
                classesViewModel.setSelectedClassesLiveData();
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
                        startActivitySafely(classesViewModel.selectedClasses.get(0), ClassesEditorActivity.class);
                        mode.finish();
                        break;
                    case R.id.action_delete:
                        createDeleteConfirmationDialog(mode);
                        break;
                    case R.id.action_select_all:
                        classesViewModel.selectAllClasses(FirebaseDao.getCurrentUserClasses());
                        adapter.notifyDataSetChanged();
                        break;

                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                classesViewModel.clearSelectedClasses();
                adapter.notifyDataSetChanged();
                isMultipleStudentSelected = false;
                isOneStudentSelected = false;
                actionMode = null;
            }
        };
        actionMode = ((AppCompatActivity) this).startActionMode(callback);

        classesViewModel.getSelectedClassesLiveData().observe(this, new Observer<ArrayList<Classes>>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onChanged(@NonNull ArrayList<Classes> Classes) {

                if (Classes.size() == 0) {
                    isOneStudentSelected = false;
                    isMultipleStudentSelected = false;
                } else {
                    if (Classes.size() == 1) {
                        isOneStudentSelected = true;
                        isMultipleStudentSelected = false;
                    } else {
                        isMultipleStudentSelected = true;
                        isOneStudentSelected = false;
                    }

                    actionMode.setTitle(Classes.size() + " Selected");
                    Menu menu = actionMode.getMenu();
                    if (isOneStudentSelected) {
                        //edit menu item diasble
                        menu.getItem(0).setVisible(true);
                    } else {
                        menu.getItem(0).setVisible(false);
                    }
                    if (Classes.size() == FirebaseDao.getCurrentUserClasses().size()) {
                        menu.getItem(2).setVisible(false);
                    } else {
                        menu.getItem(2).setVisible(true);
                    }
                }
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void OnItemClick(Classes Class, ImageView imageView) {
        if (isOneStudentSelected || isMultipleStudentSelected) {
            classesViewModel.selectOneClass(Class);
            if (actionMode != null && !isMultipleStudentSelected && !isOneStudentSelected) {
                actionMode.finish();
            }
            if (imageView.getVisibility() == View.VISIBLE) {
                imageView.setVisibility(View.INVISIBLE);
            } else {
                imageView.setVisibility(View.VISIBLE);
                //haptic feedback
                getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
        } else {
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
                        //FirebaseDao.setCurrentClass(Class);

                        Intent intent = new Intent(ClassesActivity.this, StudentsAndAttendanceActivity.class);
                        intent = FirebaseDao.passClassThroughIntent(Class, intent);
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

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void OnLongClick(Classes Class, ImageView imageView) {
        if (actionMode == null) {
            checkStudentSlection();
        }
        classesViewModel.selectOneClass(Class);

        //taptarget userguide
        if (!TargetViewMaker.hasUserSeenGuide("startOnboardGuideSequenceClassMenuItem", ClassesActivity.this)) {
            startOnboardGuideSequenceClassMenuItem();
        }

        if (actionMode != null && !isMultipleStudentSelected && !isOneStudentSelected) {
            actionMode.finish();
        }
        if (imageView.getVisibility() == View.VISIBLE) {
            imageView.setVisibility(View.INVISIBLE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            //haptic feedback
            getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);

        }
    }

    public void deleteClassAttendanceFolder(String classFolderName) {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
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

    void startActivitySafely(Classes currentClass, Class<ClassesEditorActivity> toActivity) {
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

                    Intent intent = new Intent(ClassesActivity.this, toActivity);
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

    public void startOnboardGuideSequenceInEmptyClasses() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(binding.fab);

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();
            targetArrayList.add(TargetViewMaker.makeTapTargetWithoutTintTarget(binding.fab, getString(R.string.tap_title_add_class), getString(R.string.tap_title_add_class_desc), 70));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(findViewById(R.id.user_account_menu_item), getString(R.string.tap_title_acount), getString(R.string.tap_title_acount_desc), 50));

            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequence(ClassesActivity.this);
        } catch (Exception exception) {

        }
    }

    public void startOnboardGuideSequenceClassMenuItem() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(binding.fab);

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(findViewById(R.id.action_edit), getString(R.string.tap_title_edit_class), getString(R.string.tap_title_edit_class_desc), 40));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(findViewById(R.id.action_delete), getString(R.string.tap_title_delete_class), getString(R.string.tap_title_delete_class_desc), 40));

            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequence(ClassesActivity.this);
        } catch (Exception exception) {

        }
    }

    public void startOnboardGuideSequenceAfterAddingClass() {
        try {
            TargetViewMaker.makeTapTargetSkipButton(binding.fab);

            ArrayList<TapTarget> targetArrayList = new ArrayList<>();
            targetArrayList.add(TargetViewMaker.makeTapTargetWithTintTarget(binding.classesSearchViewPlaceholder, getString(R.string.tap_title_search_class), getString(R.string.tap_title_search_class_desc), 50));
            targetArrayList.add(TargetViewMaker.makeTapTargetWithoutTintTarget(gridLayoutManager.findViewByPosition(0), getString(R.string.tap_title_click_hold), getString(R.string.tap_title_click_hold_desc), 150));

            TargetViewMaker.setTapTargetArrayList(targetArrayList);
            TargetViewMaker.startSequence(ClassesActivity.this);
        } catch (Exception exception) {

        }

    }
}
