package com.query.TakeAttendancePackage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.query.R;
import com.query.StudentsPackage.DataModel.Student;

import java.util.ArrayList;

public class ClickModeTakeAttendanceAdapter extends RecyclerView.Adapter<ClickModeTakeAttendanceAdapter.ListStudentView> {


    ClickModeMyListenerTakeAttendance mClickModeMyListenerTakeAttendance;
    int mAttendanceMode;
    Context ctx;
    ArrayList<Student> mStudents;
    public ArrayList<Integer> presentStudents = new ArrayList<Integer>();

    public ClickModeTakeAttendanceAdapter(Context context, ClickModeMyListenerTakeAttendance clickModeMyListenerTakeAttendance, ArrayList<Student> students, int attendaceMode) {
        mClickModeMyListenerTakeAttendance = clickModeMyListenerTakeAttendance;
        ctx = context;
        mStudents = students;
        mAttendanceMode = attendaceMode;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public ClickModeTakeAttendanceAdapter.ListStudentView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ClickModeTakeAttendanceAdapter.ListStudentView(LayoutInflater.from(ctx).inflate(R.layout.student_strip, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull ClickModeTakeAttendanceAdapter.ListStudentView holder, @SuppressLint("RecyclerView") int position) {
        //rollno of student with only last 3 digits
        String rollNo = String.valueOf(Long.parseLong(mStudents.get(position).getStudentRollNo()) % 1000);
        String name = mStudents.get(position).getStudentName();
        String attendance = mStudents.get(position).getStudentAttendancePercent() + "%";
        holder.rollNoTv.setText(rollNo);
        holder.nameTv.setText(name);
        holder.attendanceTv.setText(attendance);
        if (presentStudents.contains(position)) {
            holder.getStripSelectedIv().setVisibility(View.VISIBLE);

            holder.itemView.setBackground(ctx.getResources().getDrawable(R.drawable.green_stu));
            holder.getRollNoTv().setBackground(ctx.getResources().getDrawable(R.drawable.green_per_gradiant));
            holder.getAttendanceTv().setTextColor(ContextCompat.getColor(ctx, R.color.green_student_text_light_color));
            holder.getNameTv().setTextColor(ContextCompat.getColor(ctx, R.color.green_student_text_color));
            holder.getAttendanceTv().setTextColor(ContextCompat.getColor(ctx, R.color.green_student_text_color));
        } else {
            holder.itemView.setBackground(ctx.getResources().getDrawable(R.drawable.grey_stu));
            holder.getRollNoTv().setBackground(ctx.getResources().getDrawable(R.drawable.grey_per));
            holder.getAttendanceTv().setTextColor(ContextCompat.getColor(ctx, R.color.grey_student_text_light_color));
            holder.getNameTv().setTextColor(ContextCompat.getColor(ctx, R.color.grey_student_text_color));
            holder.getAttendanceTv().setTextColor(ContextCompat.getColor(ctx, R.color.grey_student_text_color));
            holder.getStripSelectedIv().setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presentStudents.contains((Integer) position)) {
                    presentStudents.remove((Integer) position);
                    holder.itemView.setBackground(ctx.getResources().getDrawable(R.drawable.grey_stu));
                    holder.getRollNoTv().setBackground(ctx.getResources().getDrawable(R.drawable.grey_per));
                    holder.getAttendanceTv().setTextColor(ContextCompat.getColor(ctx, R.color.grey_student_text_light_color));
                    holder.getNameTv().setTextColor(ContextCompat.getColor(ctx, R.color.grey_student_text_color));
                    holder.getAttendanceTv().setTextColor(ContextCompat.getColor(ctx, R.color.grey_student_text_color));
                    holder.getStripSelectedIv().setVisibility(View.INVISIBLE);
                } else {
                    presentStudents.add(position);
                    holder.getStripSelectedIv().setVisibility(View.VISIBLE);
                    holder.itemView.setBackground(ctx.getResources().getDrawable(R.drawable.green_stu));
                    holder.getRollNoTv().setBackground(ctx.getResources().getDrawable(R.drawable.green_per_gradiant));
                    holder.getAttendanceTv().setTextColor(ContextCompat.getColor(ctx, R.color.green_student_text_light_color));
                    holder.getNameTv().setTextColor(ContextCompat.getColor(ctx, R.color.green_student_text_color));
                    holder.getAttendanceTv().setTextColor(ContextCompat.getColor(ctx, R.color.green_student_text_color));
                }
                mClickModeMyListenerTakeAttendance.onClickClickModeAttendanceRvItem(holder.getAdapterPosition());
            }
        });
    }

    public void updateAttendaceMode(int attendaceMode) {
        mAttendanceMode = attendaceMode;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mStudents.size();
    }

    public class ListStudentView extends RecyclerView.ViewHolder {

        ImageView stripSelectedIv;
        TextView rollNoTv;
        TextView nameTv;
        TextView attendanceTv;//format "18/20\n90%"

        public ListStudentView(@NonNull View itemView) {
            super(itemView);
            rollNoTv = (TextView) itemView.findViewById(R.id.tv_strip_student_roll_no);
            nameTv = (TextView) itemView.findViewById(R.id.tv_strip_student_name);
            stripSelectedIv = itemView.findViewById(R.id.student_strip_selected_for_attendance);
            attendanceTv = (TextView) itemView.findViewById(R.id.tv_strip_student_attendance_percent);
        }

        public TextView getRollNoTv() {
            return rollNoTv;
        }

        public TextView getNameTv() {
            return nameTv;
        }

        public TextView getAttendanceTv() {
            return attendanceTv;
        }

        public ImageView getStripSelectedIv() {
            return stripSelectedIv;
        }
    }
}

interface ClickModeMyListenerTakeAttendance {
    void onClickClickModeAttendanceRvItem(int position);
}
