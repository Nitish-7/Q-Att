package com.query.ClassesPackage.ClassesData;

import androidx.annotation.NonNull;

import com.query.ClassesPackage.ClassesActivity;


//@Entity(tableName = ClassEntry.TABLE_NAME)
public class Classes {

//    @PrimaryKey
//    @NonNull
//    @ColumnInfo(name = ClassEntry.COLUMN_CLASS_ID)
    String mClassId;

//    @NonNull
//    @ColumnInfo(name = ClassEntry.COLUMN_CLASS_NAME)
    String mClassName;

    //@ColumnInfo(name = ClassEntry.COLUMN_SUBJECT)
    String mSubjectName;

    //@ColumnInfo(name = ClassEntry.COLUMN_SUBJECT_CODE)
    String mSubjectCode;

    //@ColumnInfo(name = ClassEntry.COLUMN_SECTION)
    int mSection;


    //@ColumnInfo(name = ClassEntry.COLUMN_YEAR)
    int mYear;

    public Classes(String classId, @NonNull String className, String subjectName, String subjectCode, int section, int year) {
        this.mClassName = className;
        this.mSubjectName = subjectName;
        this.mSubjectCode = subjectCode;
        this.mSection = section;
        this.mYear = year;
        if (classId == "-1")
            this.mClassId = String.valueOf(ClassesActivity.getTotalClasses());
        else
            this.mClassId = classId;
    }
    public Classes(){}

    @NonNull
    public String getmClassId() {
        return this.mClassId;
    }
    public void setmClassId(String classId) {
        this.mClassId=classId;
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
