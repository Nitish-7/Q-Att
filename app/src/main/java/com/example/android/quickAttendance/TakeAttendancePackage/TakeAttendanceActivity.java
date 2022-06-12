package com.example.android.quickAttendance.TakeAttendancePackage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.android.quickAttendance.FirebaseDao;
import com.example.android.quickAttendance.R;
import com.example.android.quickAttendance.StudentsPackage.DataModel.Attendance;

import java.util.ArrayList;
import java.util.HashMap;

public class TakeAttendanceActivity extends AppCompatActivity implements MyListener {

    RecyclerView recyclerView;
    CustomLinearLayoutManager customLayoutManager;
    String dateOfAttendance;
    ArrayList<Attendance> allRollNosAttendance = new ArrayList<>();

    public class CustomLinearLayoutManager extends LinearLayoutManager {

        private static final float MILLISECONDS_PER_INCH = 12f; //default is 25f (bigger = slower)
        private boolean scrollFlag = false;
        //private boolean isViewScrolled = false;

        public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);

        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            //super.smoothScrollToPosition(recyclerView, state, position);
            Log.d("x= ", "smooth ");
            final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

                @Override
                public PointF computeScrollVectorForPosition(int targetPosition) {
                    return super.computeScrollVectorForPosition(targetPosition);
                }

                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                }
            };

            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }

        @Override
        public void onScrollStateChanged(int state) {
            super.onScrollStateChanged(state);
            setScroll(false);
            Log.d("x= ", "scrolled ");
        }


        public void setScroll(boolean flag) {
            scrollFlag = flag;
        }

        // it will always pass false to RecyclerView when calling "canScrollVertically()" method.
        @Override
        public boolean canScrollHorizontally() {
            return scrollFlag;
        }

    }


    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        recyclerView = findViewById(R.id.rv);

        dateOfAttendance = FirebaseDao.getDateTime();

        customLayoutManager = new CustomLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        customLayoutManager.setScroll(true);

        recyclerView.setLayoutManager(customLayoutManager);

        TakeAttendanceViewAdapter adapder = new TakeAttendanceViewAdapter(this, this, FirebaseDao.getCurrentClassStudents());
        recyclerView.setAdapter(adapder);

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                recyclerView.onTouchEvent(motionEvent);
                return true;
            }
        });

        ItemTouchHelper.SimpleCallback swipeUpDown = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN) {
            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeThreshold(viewHolder) / 4;
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return super.getSwipeEscapeVelocity(defaultValue) * 2;
            }


            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                Log.d("c = ", actionState + "");

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                final int DIRECTION_UP = 1;
                final int DIRECTION_DOWN = 0;
                View itemView = viewHolder.itemView;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive) {
                    int direction = dY > 0 ? DIRECTION_DOWN : DIRECTION_UP;

                    if (direction == DIRECTION_UP) {
                        itemView.setForeground(getResources().getDrawable(R.drawable.swipe_up_fg));
                    } else if (direction == DIRECTION_DOWN) {
                        itemView.setForeground(getResources().getDrawable(R.drawable.swipe_down_fg));
                    }
//                    TODO haptic feedback aur last me save attendance card aur isme total students present aur absent

                } else
                    itemView.setForeground(null);
            }


            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //customLayoutManager.setScroll(true);
                Attendance attendance;
                if (direction == ItemTouchHelper.UP) {
                    getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                    allRollNosAttendance.add(new Attendance(FirebaseDao.getCurrentClassStudents().get(viewHolder.getAdapterPosition()).getStudentRollNo(), "1"));
                } else {
                    getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.REJECT, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                    allRollNosAttendance.add(new Attendance(FirebaseDao.getCurrentClassStudents().get(viewHolder.getAdapterPosition()).getStudentRollNo(), "0"));

                }

                int pos = viewHolder.getAdapterPosition() + 1;
                customLayoutManager.scrollToPosition(pos);
                if (adapder.getItemCount() == pos) {

                    FirebaseDao.insertAttendance(FirebaseDao.getCurrentClass().getmClassId(), dateOfAttendance, allRollNosAttendance);
                    Toast.makeText(TakeAttendanceActivity.this, "finished", Toast.LENGTH_SHORT).show();

                }
            }

        };

        new ItemTouchHelper(swipeUpDown).attachToRecyclerView(recyclerView);

    }

    @Override
    public void myScrollListener(int position) {
//        Toast.makeText(this, "scrolled", Toast.LENGTH_SHORT).show();
//        customLayoutManager.setScroll(true);
//        customLayoutManager.smoothScrollToPosition(recyclerView, null, position);
    }

//    TODO on back are you sure u want to dismiss the attendance
}