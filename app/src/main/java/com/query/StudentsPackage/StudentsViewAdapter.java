package com.query.StudentsPackage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.query.R;
import com.query.StudentsPackage.DataModel.Student;

import java.util.ArrayList;


public class StudentsViewAdapter extends RecyclerView.Adapter<StudentsViewAdapter.StudentsViewHolder> implements Filterable {

    Context ctx;
    ArrayList<Student> mStudents;
    ArrayList<Student> mStudentsAll;
    MyListener mListener;
    StudentSelectionViewModal studentSelectionViewModal;

    public StudentsViewAdapter(Context context, ArrayList<Student> students, MyListener myListener, StudentSelectionViewModal studentSelectionViewModal) {
        ctx = context;
        mStudents = students;
        mStudentsAll=new ArrayList<>();
        mListener = myListener;
        this.studentSelectionViewModal = studentSelectionViewModal;
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

        holder.getTvStudentRollNo().setText(mStudents.get(position).getStudentRollNo());
        holder.getTvStudentName().setText(mStudents.get(position).getStudentName());
        holder.getTvStudentAttenPer().setText(mStudents.get(position).getStudentAttendancePercent());

        int studentAttendancePercent = Integer.parseInt(mStudents.get(position).studentAttendancePercent);

        if (studentAttendancePercent >= 85 && studentAttendancePercent <= 100) {
            holder.tvStudentAttenPer.setBackground(ctx.getResources().getDrawable(R.drawable.green_per));
        } else if (studentAttendancePercent >= 70 && studentAttendancePercent < 85) {
            holder.tvStudentAttenPer.setBackground(ctx.getResources().getDrawable(R.drawable.blue_per));
        } else if (studentAttendancePercent >= 60 && studentAttendancePercent < 70) {
            holder.tvStudentAttenPer.setBackground(ctx.getResources().getDrawable(R.drawable.yellow_per));
        } else if (studentAttendancePercent < 60) {
            holder.tvStudentAttenPer.setBackground(ctx.getResources().getDrawable(R.drawable.red_per));
        }

        if(!studentSelectionViewModal.selectedStudents.isEmpty()){
            holder.ivStudentOverview.setVisibility(View.GONE);
        }else {
            holder.ivStudentOverview.setVisibility(View.VISIBLE);
        }

        if (studentSelectionViewModal.selectedStudents.contains(mStudents.get(position))) {
            holder.getIvStudentSelected().setVisibility(View.VISIBLE);
        } else {
            holder.getIvStudentSelected().setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.myOnLongClickStudentsList(mStudents.get(position), holder.getIvStudentSelected());
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.myOnClickStudentsList(mStudents.get(position), holder.getIvStudentSelected());
            }
        });
        holder.getIvStudentOverview().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.myOnClickStudentsOverview(mStudents.get(position), holder.getIvStudentSelected());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStudents.size();
    }

    public void notifyStudentsChanged(ArrayList<Student> students) {
        mStudents = students;
        mStudentsAll.clear();
        mStudentsAll.addAll(mStudents);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Student> filteredStudents = new ArrayList<>();
            String searchKey = constraint.toString().toLowerCase();
            if (searchKey.isEmpty()) {
                filteredStudents.addAll(mStudentsAll);
            } else {
                 for (Student student : mStudentsAll) {
                    String[] fullname=student.getStudentName().toLowerCase().split(" ");
                    for(String name :fullname){
                        if (name.startsWith(searchKey)){
                            filteredStudents.add(student);
                            break;
                        }
                    }
                    if (student.getStudentName().toLowerCase().startsWith(searchKey)||student.getStudentRollNo().toLowerCase().contains(searchKey)) {
                        if(!filteredStudents.contains(student))
                        filteredStudents.add(student);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredStudents;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mStudents.clear();
            mStudents.addAll((ArrayList<Student>) results.values);
            notifyDataSetChanged();
        }
    };

    public class StudentsViewHolder extends RecyclerView.ViewHolder {

        public TextView getTvStudentRollNo() {
            return tvStudentRollNo;
        }

        public TextView getTvStudentName() {
            return tvStudentName;
        }

        public TextView getTvStudentAttenPer() {
            return tvStudentAttenPer;
        }

        public ImageView getIvStudentSelected() {
            return ivStudentSelected;
        }

        public ImageView getIvStudentOverview() {
            return ivStudentOverview;
        }
        
        TextView tvStudentRollNo;
        TextView tvStudentName;
        TextView tvStudentAttenPer;
        ImageView ivStudentSelected;
        

        ImageView ivStudentOverview;

        public StudentsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvStudentRollNo = itemView.findViewById(R.id.tv_student_roll_no);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvStudentAttenPer = itemView.findViewById(R.id.tv_student_attendance_percent);
            ivStudentSelected = itemView.findViewById(R.id.student_selected_iv);
            ivStudentOverview = itemView.findViewById(R.id.student_overview_iv);
        }
    }
}

interface MyListener {
    void myOnClickStudentsList(Student student, ImageView imageView);

    void myOnLongClickStudentsList(Student student, ImageView imageView);

    void myOnClickStudentsOverview(Student student, ImageView ivStudentSelected);
}
