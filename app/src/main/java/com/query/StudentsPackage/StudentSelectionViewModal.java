package com.query.StudentsPackage;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.query.StudentsPackage.DataModel.Student;

import java.util.ArrayList;

public class StudentSelectionViewModal extends ViewModel {
    MutableLiveData<ArrayList<Student>> selectedStudentsLiveData = new MutableLiveData<>();

    ArrayList<Student> selectedStudents = new ArrayList<>();


    //below all selecting and further operation code for selected students
    public MutableLiveData<ArrayList<Student>> getSelectedStudentsLiveData() {
        setSelectedStudentsLiveData();
        return selectedStudentsLiveData;
    }

    public void selectOneStudents(Student student) {
        if (selectedStudents.contains(student)) {
            selectedStudents.remove(student);
        } else {
            selectedStudents.add(student);
        }
        setSelectedStudentsLiveData();
    }

    public void selectAllStudents(ArrayList<Student> students) {
        selectedStudents.clear();
        selectedStudents.addAll(students);
        setSelectedStudentsLiveData();
    }
    public void setSelectedStudentsLiveData() {
        selectedStudentsLiveData.setValue(selectedStudents);
    }
    public void clearSelectedStudents() {
        selectedStudents.clear();
        selectedStudentsLiveData.setValue(selectedStudents);
    }
}
