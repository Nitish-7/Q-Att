package com.query.ClassesPackage.ClassesData;
//
//    private ClassesDao mClassesDao;
//    private LiveData<List<Classes>> mAllClasses;
//
//    public ClassesRepository(Application application) {
//        com.query.ClassesPackage.ClassesData.ClassesRoomDatabase db = com.query.ClassesPackage.ClassesData.ClassesRoomDatabase.getDatabase(application);
//        mClassesDao =db.ClassesDao();
//        mAllClasses = mClassesDao.getAlphabetizedWords();
//    }
//
//    void insert(Classes Class) {
//        com.query.ClassesPackage.ClassesData.ClassesRoomDatabase.databaseWriteExecutor.execute(() -> {
//            mClassesDao.insert(Class);
//            FirebaseDao.insertClassDetailes(Class);
//        });
//    }
//    void delete(Classes Class) {
//        com.query.ClassesPackage.ClassesData.ClassesRoomDatabase.databaseWriteExecutor.execute(() -> {
//            mClassesDao.delete(Class);
//        });
//    }
//    void deleteAll() {
//        com.query.ClassesPackage.ClassesData.ClassesRoomDatabase.databaseWriteExecutor.execute(() -> {
//            mClassesDao.deleteAll();
//        });
//    }
//    LiveData<List<Classes>> getAllClasses(){
//        return mAllClasses;
//    }
//
//}
