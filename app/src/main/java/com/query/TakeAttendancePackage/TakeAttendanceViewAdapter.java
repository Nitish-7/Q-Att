package com.query.TakeAttendancePackage;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.query.FirebaseDao;
import com.query.R;
import com.query.StudentsPackage.DataModel.Student;

import java.util.ArrayList;

public class TakeAttendanceViewAdapter extends RecyclerView.Adapter<TakeAttendanceViewAdapter.CardView> {


    MyListenerTakeAttendance myListenerTakeAttendance;
    int mAttendanceMode = 0;
    private Context ctx;
    ArrayList<Student> mStudents;
    Drawable drawable;

    public TakeAttendanceViewAdapter(Context context, MyListenerTakeAttendance myListenerTakeAttendance1, ArrayList<Student> students, int attendaceMode) {
        myListenerTakeAttendance = myListenerTakeAttendance1;
        ctx = context;
        mStudents = students;
        mAttendanceMode = attendaceMode;
        // Load the vector drawable resource
//        drawable = ContextCompat.getDrawable(context, R.drawable.student_card_bg_with_elevation);
//
//        // Create a copy of the drawable
//        drawable = drawable.mutate().getConstantState().newDrawable();
//
//        // Apply a color filter to convert the drawable to black and white
//        ColorMatrix matrix = new ColorMatrix();
//        //matrix.setSaturation(0);
//        ColorFilter filter = new ColorMatrixColorFilter(matrix);
//        drawable.setColorFilter(filter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public CardView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CardView(LayoutInflater.from(ctx).inflate(R.layout.student_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CardView holder, int position) {
        //rollno of student with only last 3 digits
        //holder.itemView.setBackground(drawable);
        holder.itemView.setAlpha(0.8f);
        String rollNo = String.valueOf(Long.parseLong(mStudents.get(position).getStudentRollNo()) % 1000);
        String name = mStudents.get(position).getStudentName();
        String attendance = mStudents.get(position).getStudentAttendedClasses() + "/" + FirebaseDao.getCurrentClassDeliveredClasses() + "\n" + mStudents.get(position).getStudentAttendancePercent() + "%";
        holder.getRollNo().setText(rollNo);
        holder.getName().setText(name);
        holder.getAttendance().setText(attendance);

    }

    public void updateAttendaceMode(int attendaceMode) {
        mAttendanceMode = attendaceMode;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mStudents.size();
    }

    public class CardView extends RecyclerView.ViewHolder {
        TextView rollNo;
        TextView name;
        TextView attendance;  //format "18/20\n90%"


        public TextView getRollNo() {
            return rollNo;
        }

        public TextView getName() {
            return name;
        }

        public TextView getAttendance() {
            return attendance;
        }

        public CardView(@NonNull View itemView) {
            super(itemView);
            this.rollNo = itemView.findViewById(R.id.tv_card_student_roll_no);
            this.name = itemView.findViewById(R.id.tv_card_student_name);
            this.attendance = itemView.findViewById(R.id.tv_card_student_attendance);
        }
    }
}

interface MyListenerTakeAttendance {
    void myScrollListener(int position);
}
