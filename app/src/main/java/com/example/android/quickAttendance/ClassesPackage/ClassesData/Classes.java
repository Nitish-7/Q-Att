package com.example.android.quickAttendance.ClassesPackage.ClassesData;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.android.quickAttendance.ClassesPackage.ClassesActivity;
import com.example.android.quickAttendance.ClassesPackage.ClassesData.ClassesContracts.ClassEntry;
import com.example.android.quickAttendance.FirebaseDao;
import com.google.firebase.database.FirebaseDatabase;


@Entity(tableName = ClassEntry.TABLE_NAME)
public class Classes {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "classId")
    int mClassId;

    @NonNull
    @ColumnInfo(name = ClassEntry.COLUMN_CLASS)
    String mClassName;

    @ColumnInfo(name = ClassEntry.COLUMN_SUBJECT)
    String mSubjectName;

    @ColumnInfo(name = ClassEntry.COLUMN_SUBJECT_CODE)
    String mSubjectCode;

    @ColumnInfo(name = ClassEntry.COLUMN_SECTION)
    int mSection;


    @ColumnInfo(name = ClassEntry.COLUMN_YEAR)
    int mYear;

    public Classes(int classId, @NonNull String className, String subjectName, String subjectCode, int section, int year) {
        this.mClassName = className;
        this.mSubjectName = subjectName;
        this.mSubjectCode = subjectCode;
        this.mSection = section;
        this.mYear = year;
        if (classId == -1)
            this.mClassId = ClassesActivity.getTotalClasses();
        else
            this.mClassId = classId;
    }
    public Classes(){}

    @NonNull
    public int getmClassId() {
        return this.mClassId;
    }

    @NonNull
    public String getmClassName() {
        return this.mClassName;
    }

    public String getmSubjectName() {
        return this.mSubjectName;
    }

    public String getmSubjectCode() {
        return this.mSubjectCode;
    }

    public int getmSection() {
        return this.mSection;
    }

    public int getmYear() {
        return this.mYear;
    }
}
