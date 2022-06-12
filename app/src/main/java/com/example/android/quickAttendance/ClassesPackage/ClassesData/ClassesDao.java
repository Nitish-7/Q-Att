package com.example.android.quickAttendance.ClassesPackage.ClassesData;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.android.quickAttendance.ClassesPackage.ClassesData.ClassesContracts.ClassEntry;

import java.util.List;

@Dao
public interface ClassesDao {

    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Classes Class);

    @Delete
    void delete(Classes Class);

    @Query("DELETE FROM " + ClassEntry.TABLE_NAME)
    void deleteAll();

    @Query("SELECT * FROM " + ClassEntry.TABLE_NAME + " ORDER BY " + ClassEntry.COLUMN_CLASS + " ASC")
    LiveData<List<Classes>> getAlphabetizedWords();
}
