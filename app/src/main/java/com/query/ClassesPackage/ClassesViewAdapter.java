package com.query.ClassesPackage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.query.ClassesPackage.ClassesData.Classes;
import com.query.ClassesPackage.ClassesData.ClassesViewModel;
import com.query.FirebaseDao;
import com.query.R;

import java.util.ArrayList;
import java.util.List;

public class ClassesViewAdapter extends RecyclerView.Adapter<ClassesViewAdapter.ClassesViewHolder> implements Filterable {

    Context ctx;
    List<Classes> mAllClasses;
    List<Classes> mAllClassesAll;
    MyClickListener mMyClickListener;
    ClassesViewModel classesViewModel;

    public ClassesViewAdapter(Context context, MyClickListener myClickListener, ClassesViewModel classesViewModel, ArrayList<Classes> classes) {
        ctx = context;
        mMyClickListener = myClickListener;
        mAllClasses = new ArrayList<>();
        mAllClassesAll = new ArrayList<>();
        this.classesViewModel = classesViewModel;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public ClassesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ClassesViewHolder classesViewHolder = new ClassesViewHolder(LayoutInflater.from(ctx).inflate(R.layout.single_class_view, parent, false));
        return classesViewHolder;
    }

    @Override
    public int getItemCount() {
        return mAllClasses.size();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ClassesViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.getClassTv().setText(mAllClasses.get(position).getmClassName());
        holder.getSubjectNameTv().setText(mAllClasses.get(position).getmSubjectName());
        holder.getSubjectCodeTv().setText(mAllClasses.get(position).getmSubjectCode());

        //setting section
        if (mAllClasses.get(position).getmSection() == 0) {
            holder.getSectionTv().setVisibility(View.GONE);
        } else {
            holder.getSectionTv().setVisibility(View.VISIBLE);
            holder.getSectionTv().setText(ctx.getResources().getStringArray(R.array.array_section_options)[(int) mAllClasses.get(position).getmSection()]);
        }

        //setting sem or session
        if (isSessionNotSem(mAllClasses.get(position).getmYear())) {
            holder.getYearTv().setText(FirebaseDao.getStringSessionFromIntSession(mAllClasses.get(position).getmYear()));
        } else {
            holder.getYearTv().setText(ctx.getResources().getStringArray(R.array.array_year_options)[(int) mAllClasses.get(position).getmYear()]);
        }

        //setting if class selected
        if (classesViewModel.selectedClasses.contains(mAllClasses.get(position))) {
            holder.getIvClassSelected().setVisibility(View.VISIBLE);
        } else {
            holder.getIvClassSelected().setVisibility(View.INVISIBLE);
        }


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mMyClickListener.OnLongClick(mAllClasses.get(position), holder.getIvClassSelected());
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMyClickListener.OnItemClick(mAllClasses.get(position), holder.getIvClassSelected());
            }
        });

    }

    private boolean isSessionNotSem(int getmYear) {
        if (getmYear < ctx.getResources().getStringArray(R.array.array_year_options).length && getmYear >= 0) {
            return false;
        } else {
            return true;
        }
    }

    public void updateClassesForForeground(boolean itemSelected) {
        notifyDataSetChanged();
    }

    public void updateClasses(List<Classes> classes) {
        mAllClasses.clear();
        mAllClasses.addAll(classes);
        mAllClassesAll.clear();
        mAllClassesAll.addAll(classes);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Classes> filteredClasses = new ArrayList<>();
            String searchKey = constraint.toString().toLowerCase();
            if (searchKey.trim().isEmpty()) {
                filteredClasses.addAll(mAllClassesAll);
            } else {
                String[] items = ctx.getResources().getStringArray(R.array.array_year_options);

                //setting yr or session array
                ArrayList<String> yrSessionItemArrayList = new ArrayList<>();
                //getting 2 session according to date
                Pair<Integer, Integer> sessions = FirebaseDao.getSession().first;
                yrSessionItemArrayList.add(items[0]);

                for (int i = 1; i < items.length; i++) {

                    yrSessionItemArrayList.add(items[i]);
                }

                //filtering classes from all
                for (Classes Class : mAllClassesAll) {
                    if (isSessionNotSem(Class.getmYear())) {
                        if (Class.getmSection() == 0) {
                            if (Class.getmClassName().toLowerCase().startsWith(searchKey) || Class.getmSubjectName().toLowerCase().startsWith(searchKey) || Class.getmSubjectCode().toLowerCase().startsWith(searchKey) || FirebaseDao.getStringSessionFromIntSession(Class.getmYear()).startsWith(searchKey)) {
                                filteredClasses.add(Class);
                            }
                        } else {
                            if (Class.getmClassName().toLowerCase().startsWith(searchKey) || Class.getmSubjectName().toLowerCase().startsWith(searchKey) || Class.getmSubjectCode().toLowerCase().startsWith(searchKey) || ctx.getResources().getStringArray(R.array.array_section_options)[Class.getmSection()].toLowerCase().startsWith(searchKey) ||  FirebaseDao.getStringSessionFromIntSession(Class.getmYear()).startsWith(searchKey)) {
                                filteredClasses.add(Class);
                            }
                        }
                    }else {
                        if (Class.getmSection() == 0) {
                            if (Class.getmClassName().toLowerCase().startsWith(searchKey) || Class.getmSubjectName().toLowerCase().startsWith(searchKey) || Class.getmSubjectCode().toLowerCase().startsWith(searchKey) || yrSessionItemArrayList.get(Class.getmYear()).toLowerCase().startsWith(searchKey)) {
                                filteredClasses.add(Class);
                            }
                        } else {
                            if (Class.getmClassName().toLowerCase().startsWith(searchKey) || Class.getmSubjectName().toLowerCase().startsWith(searchKey) || Class.getmSubjectCode().toLowerCase().startsWith(searchKey) || ctx.getResources().getStringArray(R.array.array_section_options)[Class.getmSection()].toLowerCase().startsWith(searchKey) || yrSessionItemArrayList.get(Class.getmYear()).toLowerCase().startsWith(searchKey)) {
                                filteredClasses.add(Class);
                            }
                        }
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredClasses;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mAllClasses.clear();
            mAllClasses.addAll((ArrayList<Classes>) results.values);
            notifyDataSetChanged();
        }
    };


    class ClassesViewHolder extends RecyclerView.ViewHolder {
        private TextView classTv;
        private TextView subjectNameTv;
        private TextView sectionTv;
        private TextView subjectCodeTv;
        private TextView yearTv;
        private ImageView IvClassSelected;
        private LinearLayout classCardLl;

        public LinearLayout getClassCardLl() {
            return classCardLl;
        }

        public ClassesViewHolder(@NonNull View itemView) {
            super(itemView);

            IvClassSelected = itemView.findViewById(R.id.iv_select_class);
            classTv = itemView.findViewById(R.id.tv_class_name);
            sectionTv = itemView.findViewById(R.id.tv_section);
            subjectNameTv = itemView.findViewById(R.id.tv_subject_name);
            subjectCodeTv = itemView.findViewById(R.id.tv_subject_code);
            yearTv = itemView.findViewById(R.id.tv_class_year);
            classCardLl = itemView.findViewById(R.id.class_card);
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

        public ImageView getIvClassSelected() {
            return IvClassSelected;
        }
    }
}

interface MyClickListener {
    void OnItemClick(Classes Class, ImageView imageView);

    void OnLongClick(Classes Class, ImageView imageView);
}

