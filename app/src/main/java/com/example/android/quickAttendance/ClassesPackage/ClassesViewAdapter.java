package com.example.android.quickAttendance.ClassesPackage;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.android.quickAttendance.ClassesPackage.ClassesData.Classes;
import com.example.android.quickAttendance.ClassesPackage.ClassesData.ClassesViewModel;
import com.example.android.quickAttendance.R;

public class ClassesViewAdapter extends  RecyclerView.Adapter<ClassesViewAdapter.ClassesViewHolder> {

    Context ctx;
    ClassesViewModel mClassesViewModel;
    List<Classes> mAllClasses = new ArrayList<>();
    //ArrayList<Integer> selectedClasses = new ArrayList<Integer>();
    MyClickListener mMyClickListener;
    boolean setForeground = false;


    public ClassesViewAdapter(Context context, MyClickListener myClickListener) {
        ctx = context;
        mMyClickListener = myClickListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public ClassesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ClassesViewHolder classesViewHolder = new ClassesViewHolder(LayoutInflater.from(ctx).inflate(R.layout.single_class_view, parent, false));

        if (setForeground)
            classesViewHolder.itemView.setForeground(ctx.getResources().getDrawable(R.drawable.classes_unseleted_fg));

        classesViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                mMyClickListener.OnItemClick(mAllClasses.get(classesViewHolder.getAdapterPosition()),classesViewHolder.getAdapterPosition(),classesViewHolder.itemView);
            }
        });

        classesViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onLongClick(View view) {
                mMyClickListener.OnLongClick(classesViewHolder.itemView,classesViewHolder.getAdapterPosition());
                Toast.makeText(ctx,classesViewHolder.getAdapterPosition()+"",Toast.LENGTH_SHORT).show();

                return true;
            }
        });
        return classesViewHolder;
    }

    @Override
    public int getItemCount() {
        return mAllClasses.size();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ClassesViewHolder holder, int position) {
        holder.getClassTv().setText(mAllClasses.get(position).getmClassName());
        holder.getSubjectNameTv().setText(mAllClasses.get(position).getmSubjectName());
        holder.getSubjectCodeTv().setText(mAllClasses.get(position).getmSubjectCode());
        holder.getSectionTv().setText(ctx.getResources().getStringArray(R.array.array_section_options)[(int) mAllClasses.get(position).getmSection()]);
        holder.getYearTv().setText(ctx.getResources().getStringArray(R.array.array_year_options)[(int) mAllClasses.get(position).getmYear()] + " Year");

    }

    public void updateClassesForForeground(boolean itemSelected) {

        setForeground = itemSelected;
        notifyDataSetChanged();
    }

    public void updateClasses(List<Classes> classes) {
        mAllClasses.clear();
        mAllClasses.addAll(classes);

        notifyDataSetChanged();
    }

    class ClassesViewHolder extends RecyclerView.ViewHolder {
        private TextView classTv;
        private TextView subjectNameTv;
        private TextView sectionTv;
        private TextView subjectCodeTv;
        private TextView yearTv;

        public ClassesViewHolder(@NonNull View itemView) {
            super(itemView);

            classTv = itemView.findViewById(R.id.tv_class_name);
            sectionTv = itemView.findViewById(R.id.tv_section);
            subjectNameTv = itemView.findViewById(R.id.tv_subject_name);
            subjectCodeTv = itemView.findViewById(R.id.tv_subject_code);
            yearTv = itemView.findViewById(R.id.tv_class_year);

        }

        public TextView getSubjectNameTv() {
            return subjectNameTv;
        }

        public TextView getClassTv() {
            return classTv;
        }

        public TextView getSectionTv() {
            return sectionTv;
        }

        public TextView getSubjectCodeTv() {
            return subjectCodeTv;
        }

        public TextView getYearTv() {
            return yearTv;
        }
    }
}

interface MyClickListener {
    void OnItemClick(Classes Class,int position,View classesItem);

    void OnLongClick(View classesItem,int position);
}
