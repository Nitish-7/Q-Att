package com.example.android.quickAttendance.ClassesPackage.ClassesData;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ClassesContracts {
    private ClassesContracts() {
    }

    public static final class ClassEntry implements BaseColumns {

        public static final String PET_PATH = "pets";
        public static final String PET_CONTENT_AUTHORITY = "com.example.android.pets";
        public static final Uri BASE_URI = Uri.parse("content://com.example.android.pets/" + PET_PATH);

        public static final String TABLE_NAME = "pets";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_CLASS = "mClassName";
        public static final String COLUMN_SUBJECT = "mSubjectName";
        public static final String COLUMN_SECTION = "mSection";
        public static final String COLUMN_SUBJECT_CODE = "mSubjectCode";
        public static final String COLUMN_YEAR = "mYear";

        public static final int SEC_SEC = 0;
        public static final int SEC_A = 1;
        public static final int SEC_B = 2;
        public static final int SEC_C = 3;
        public static final int SEC_D = 4;
        public static final int SEC_E = 5;
        public static final int SEC_F = 6;
        public static final int SEC_G = 7;
        public static final int SEC_H = 8;

        public static final int YEAR = 0;
        public static final int FIST = 1;
        public static final int SECOND = 2;
        public static final int THIRD = 3;
        public static final int FOURTH = 4;
        public static final int FIFTH = 5;


    }

}
