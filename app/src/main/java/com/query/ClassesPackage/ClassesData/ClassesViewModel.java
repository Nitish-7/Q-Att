package com.query.ClassesPackage.ClassesData;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class ClassesViewModel extends AndroidViewModel {

    //ClassesRepository classesRepository;
    LiveData<List<Classes>> mAllClasses;
    MutableLiveData<ArrayList<Classes>> selectedClassesLiveData = new MutableLiveData<>();
    public  ArrayList<Classes> selectedClasses = new ArrayList<>();

    public ClassesViewModel(@NonNull Application application) {
        super(application);
        //classesRepository =new ClassesRepository(application);
        //mAllClasses = classesRepository.getAllClasses();
    }
//    public void insert(Classes Class)
//    {
//        classesRepository.insert(Class);
//    }
//    public void delete(Classes Class)
//    {
//        classesRepository.delete(Class);
//    }
//    public void deleteAllClasses()
//    {
//        classesRepository.deleteAll();
//    }
//    public void update(Classes before, Classes after)
//    {
//        classesRepository.delete(before);
//        classesRepository.insert(after);
//    }
//
//    public LiveData<List<Classes>> getAllClasses() {
//        return mAllClasses;
//    }



    //below all selecting and further operation code for selected students
    public MutableLiveData<ArrayList<Classes>> getSelectedClassesLiveData() {
        setSelectedClassesLiveData();
        return selectedClassesLiveData;
    }

    public void selectOneClass(Classes clas) {
        if (selectedClasses.contains(clas)) {
            selectedClasses.remove(clas);
        } else {
            selectedClasses.add(clas);
        }
        setSelectedClassesLiveData();
    }

    public void selectAllClasses(ArrayList<Classes> classes) {
        selectedClasses.clear();
        selectedClasses.addAll(classes);
        setSelectedClassesLiveData();
    }
    public void setSelectedClassesLiveData() {
        selectedClassesLiveData.setValue(selectedClasses);
    }
    public void clearSelectedClasses() {
        selectedClasses.clear();
        selectedClassesLiveData.setValue(selectedClasses);
    }
}
