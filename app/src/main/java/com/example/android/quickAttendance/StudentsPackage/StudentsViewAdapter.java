package com.example.android.quickAttendance.StudentsPackage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.quickAttendance.R;
import com.example.android.quickAttendance.StudentsPackage.DataModel.Student;

import java.util.ArrayList;


public class StudentsViewAdapter extends RecyclerView.Adapter<StudentsViewAdapter.StudentsViewHolder> {

    Context ctx;
    ArrayList<Student> mstudents;
    MyListener mListener;

    public StudentsViewAdapter(Context context, ArrayList<Student> students, MyListener myListener) {
        ctx = context;
        mstudents = students;
        mListener = myListener;
    }

    @NonNull
    @Override
    public StudentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        StudentsViewHolder studentsViewHolder = new StudentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.students_list_item, parent, false));
        return studentsViewHolder;
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull StudentsViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.getTvStudentRollNo().setText(mstudents.get(position).getStudentRollNo());
        holder.getTvStudentName().setText(mstudents.get(position).getStudentName());

        if (true) {
            holder.itemView.setBackground(ctx.getResources().getDrawable(R.drawable.greeb_stu));
            holder.tvStudentAttenPer.setBackground(ctx.getResources().getDrawable(R.drawable.green_per));
            holder.tvStudentName.setTextColor(R.color.green_student_text_light_color);
            holder.tvStudentRollNo.setTextColor(R.color.green_student_text_color);
            holder.tvStudentAttenPer.setTextColor(R.color.green_student_text_color);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.myOnClickStudentsList(mstudents.get(position).getStudentRollNo());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mstudents.size();
    }

    public void notifyStudentsChanged(ArrayList<Student> students) {
        mstudents = students;
        Log.d("size ", getItemCount() + "");
        notifyDataSetChanged();
    }

    public class StudentsViewHolder extends RecyclerView.ViewHolder {

        public TextView getTvStudentRollNo() {
            return tvStudentRollNo;
        }

        public TextView getTvStudentName() {
            return tvStudentName;
        }

        TextView tvStudentRollNo;
        TextView tvStudentName;
        TextView tvStudentAttenPer;

        public StudentsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentRollNo = itemView.findViewById(R.id.tv_student_roll_no);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvStudentAttenPer = itemView.findViewById(R.id.tv_student_attendance_percent);
        }
    }
}

interface MyListener {
    void myOnClickStudentsList(String rollNo);

    void myOnLongClickStudentsList();
}
