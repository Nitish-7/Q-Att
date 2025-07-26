package com.query.ClassesPackage.ClassesData;

import android.provider.BaseColumns;

public final class ClassesContracts {
    private ClassesContracts() {
    }

    public static final class ClassEntry implements BaseColumns {


        public static final String TABLE_NAME = "pets";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_CLASS_ID = "classId";
        public static final String COLUMN_CLASS_NAME = "mClassName";
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
        public static final int SIXTH = 6;
        public static final int SEVENTH = 7;
        public static final int EIGHTH = 8;
        public static final int NINTH = 9;
        public static final int TENTH = 10;
        public static final int ELEVENTH = 11;
        public static final int TWELFTH = 12;
    }

}
