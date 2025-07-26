package com.query.TakeAttendancePackage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.query.R;
import com.query.StudentsPackage.DataModel.Attendance;

import java.util.ArrayList;

public class TakenAttendanceViewAdapter extends RecyclerView.Adapter<TakenAttendanceViewAdapter.TakenAttendanceViewHolder> implements Filterable {
    Context ctx;
    ArrayList<Attendance> mAllRollNosAttendance=new ArrayList<>();
    ArrayList<Attendance> mAllRollNosAttendanceAll=new ArrayList<>();
    MyListenerTakenAttendance mMyListenerTakenAttendance;

    public TakenAttendanceViewAdapter(Context context, MyListenerTakenAttendance myListenerTakenAttendance, ArrayList<Attendance> allRollNosAttendance) {
        ctx = context;
        mAllRollNosAttendance.addAll(allRollNosAttendance);
        mAllRollNosAttendanceAll.addAll(mAllRollNosAttendanceAll);
        mMyListenerTakenAttendance = myListenerTakenAttendance;
    }

    @NonNull
    @Override
    public TakenAttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TakenAttendanceViewHolder(LayoutInflater.from(ctx).inflate(R.layout.taken_attendance_item_view, parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public void onBindViewHolder(@NonNull TakenAttendanceViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String rollNo = String.valueOf(Long.parseLong(mAllRollNosAttendance.get(position).getRollNo()) % 1000);
        holder.getRollNoTv().setText(rollNo);
        if (mAllRollNosAttendance.get(position).getpOrA().equals("1")) {
            holder.getRollNoTv().setBackground(ctx.getResources().getDrawable(R.drawable.green_per));
            holder.getRollNoTv().setTextColor(ContextCompat.getColor(ctx, R.color.white));
            //holder.getRollNoTv().setForeground(ctx.getResources().getDrawable(R.drawable.swipe_up_fg));
        } else {
            holder.getRollNoTv().setBackground(ctx.getResources().getDrawable(R.drawable.red_per));
            holder.getRollNoTv().setTextColor(ContextCompat.getColor(ctx, R.color.white));
            //holder.getRollNoTv().setForeground(ctx.getResources().getDrawable(R.drawable.swipe_down_fg));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMyListenerTakenAttendance.onClickTakenAttendanceRvItem(holder.itemView, mAllRollNosAttendance.get(position).getRollNo(),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAllRollNosAttendance.size();
    }

    public void updateTakenAttendanceOneItem(ArrayList<Attendance> allRollNosAttendanceUpdated) {
        mAllRollNosAttendance.clear();
        mAllRollNosAttendance.addAll(allRollNosAttendanceUpdated);
        mAllRollNosAttendanceAll.clear();
        mAllRollNosAttendanceAll.addAll(mAllRollNosAttendance);
        notifyItemChanged(mAllRollNosAttendance.size() - 1);
    }

    public void updateAllItems(ArrayList<Attendance> allRollNosAttendanceUpdated) {
        mAllRollNosAttendance.clear();
        mAllRollNosAttendance.addAll(allRollNosAttendanceUpdated);
        mAllRollNosAttendanceAll.clear();
        mAllRollNosAttendanceAll.addAll(mAllRollNosAttendance);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            ArrayList<Attendance> filterRollNos = new ArrayList<>();
            String query = constraint.toString();
            if (query.isEmpty()) {
                filterRollNos.addAll(mAllRollNosAttendanceAll);
            } else {
                for (Attendance attendance : mAllRollNosAttendanceAll) {
                    if(attendance.getpOrA().equals(query)){
                        filterRollNos.add(attendance);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filterRollNos;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mAllRollNosAttendance.clear();
            mAllRollNosAttendance.addAll((ArrayList<Attendance>) results.values);
            notifyDataSetChanged();
        }
    };

    class TakenAttendanceViewHolder extends RecyclerView.ViewHolder {

        TextView rollNoTv;

        public TakenAttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            rollNoTv = itemView.findViewById(R.id.tv_taken_attendane_roll_no);
        }

        public TextView getRollNoTv() {
            return rollNoTv;
        }

        public void setRollNoTv(TextView rollNoTv) {
            this.rollNoTv = rollNoTv;
        }

    }
}

interface MyListenerTakenAttendance {
    void onClickTakenAttendanceRvItem(View anchor, String rollNo,int postion);
}