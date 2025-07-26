package com.query;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class QueryApplicationClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // Initialize the App Startup library
        //AppInitializer.getInstance(this).initializeComponent(MyLibraryInitializer.class);

        //Log.d("**Application**", "started+++++++++++++++++++++++ at time = " + FirebaseDao.getOnlyTime());
    }
}
