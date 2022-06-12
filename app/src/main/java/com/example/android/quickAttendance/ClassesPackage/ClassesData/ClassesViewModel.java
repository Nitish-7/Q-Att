package com.example.android.quickAttendance.ClassesPackage.ClassesData;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ClassesViewModel extends AndroidViewModel {

    ClassesRepository classesRepository;
    LiveData<List<Classes>> mAllClasses;


    public ClassesViewModel(@NonNull Application application) {
        super(application);
        classesRepository =new ClassesRepository(application);
        mAllClasses = classesRepository.getAllClasses();
    }
    public void insert(Classes Class)
    {
        classesRepository.insert(Class);
    }
    public void delete(Classes Class)
    {
        classesRepository.delete(Class);
    }
    public void deleteAllClasses()
    {
        classesRepository.deleteAll();
    }
    public void update(Classes before, Classes after)
    {
        classesRepository.delete(before);
        classesRepository.insert(after);
    }

    public LiveData<List<Classes>> getAllClasses() {
        return mAllClasses;
    }
}
