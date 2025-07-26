package com.query.StudentsPackage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.query.R;
import com.query.StudentsPackage.DataModel.MonthWiseAttendance;

import java.util.ArrayList;

public class AttendanceViewAdapter extends RecyclerView.Adapter<AttendanceViewAdapter.AttendanceViewHolder> {
    Context ctx;
    ArrayList<MonthWiseAttendance> mMonthWiseAttendances=new ArrayList<>();
    MyAttendanceListener mListener;
    public AttendanceViewAdapter(Context context,MyAttendanceListener myAttendanceListener,ArrayList<MonthWiseAttendance> monthWiseAttendance) {
        ctx=context;
        mListener=myAttendanceListener;
        mMonthWiseAttendances.clear();
        mMonthWiseAttendances.addAll(monthWiseAttendance);
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AttendanceViewHolder(LayoutInflater.from(ctx).inflate(R.layout.single_month_attendance_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.attendanceName.setText(mMonthWiseAttendances.get(position).getAttendaceFileName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnAttendanceRecordItemClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMonthWiseAttendances.size();
    }

    public void notifyAttendanceRecordChanged(ArrayList<MonthWiseAttendance> monthWiseAttendances) {
        mMonthWiseAttendances.clear();
        mMonthWiseAttendances.addAll(monthWiseAttendances);
        notifyDataSetChanged();
    }

    public class AttendanceViewHolder extends RecyclerView.ViewHolder{
        TextView attendanceName;
        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            attendanceName=itemView.findViewById(R.id.tv_attendance_name);
        }
    }

}
interface MyAttendanceListener{
    void OnAttendanceRecordItemClicked(int pos);
}
