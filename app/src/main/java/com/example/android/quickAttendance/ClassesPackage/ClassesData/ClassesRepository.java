package com.example.android.quickAttendance.ClassesPackage.ClassesData;
import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.android.quickAttendance.FirebaseDao;

import java.util.List;

public class ClassesRepository {

    private ClassesDao mClassesDao;
    private LiveData<List<Classes>> mAllClasses;

    public ClassesRepository(Application application) {
        ClassesRoomDatabase db = ClassesRoomDatabase.getDatabase(application);
        mClassesDao =db.ClassesDao();
        mAllClasses = mClassesDao.getAlphabetizedWords();
    }

    void insert(Classes Class) {
        ClassesRoomDatabase.databaseWriteExecutor.execute(() -> {
            mClassesDao.insert(Class);
            FirebaseDao.insertClassDetailes(Class);
        });
    }
    void delete(Classes Class) {
        ClassesRoomDatabase.databaseWriteExecutor.execute(() -> {
            mClassesDao.delete(Class);
        });
    }
    void deleteAll() {
        ClassesRoomDatabase.databaseWriteExecutor.execute(() -> {
            mClassesDao.deleteAll();
        });
    }
    LiveData<List<Classes>> getAllClasses(){
        return mAllClasses;
    }

}
