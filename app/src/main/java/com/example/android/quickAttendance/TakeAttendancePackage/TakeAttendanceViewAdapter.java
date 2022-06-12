package com.example.android.quickAttendance.TakeAttendancePackage;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.quickAttendance.R;
import com.example.android.quickAttendance.StudentsPackage.DataModel.Student;

import java.util.ArrayList;

public class TakeAttendanceViewAdapter extends RecyclerView.Adapter<TakeAttendanceViewAdapter.CardView> {


    MyListener myListener;
    private Context ctx;
    ArrayList<Student> mStudents;
    RecyclerView mRecyclerViewAttached;
    public TakeAttendanceViewAdapter(Context context, MyListener myListener1, ArrayList<Student> students) {
        myListener = myListener1;
        ctx = context;
        mStudents=students;
        //mRecyclerViewAttached=recyclerViewAttached;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public CardView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CardView(LayoutInflater.from(ctx).inflate(R.layout.student_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CardView holder, int position) {
//        TODO original students data bind kro

    }
    public void update()
    {
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mStudents.size();
    }

    public class CardView extends RecyclerView.ViewHolder {

        @RequiresApi(api = Build.VERSION_CODES.M)
        public CardView(@NonNull View itemView) {
            super(itemView);
        }

    }
}

interface MyListener {
    void myScrollListener(int position);
}
