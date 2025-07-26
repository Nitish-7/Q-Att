package com.query.OnboardingAndGuidePackage;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.query.R;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.util.ArrayList;

public class TargetViewMaker {
    public static PopupWindow popupWindow;
    public static Activity activity;
    public static Context context;
    public static ArrayList<TapTarget> tapTargetArrayList;
    public static TapTargetView currentOnScreenTapTargetView;
    public static String tapTargetSequenceName;
    public static int tapTargetIterator;
    public static boolean isDialogTarget;
    public static Dialog dialog;
    public static TapTargetView.Listener listener;


    public static void setTapTargetArrayList(ArrayList<TapTarget> targetArrayList) {
        tapTargetArrayList = new ArrayList<>();
        tapTargetArrayList.addAll(targetArrayList);
    }

    public static TapTarget makeTapTargetWithTintTarget(View view, String title, String description, int radius) {

        return TapTarget.forView(view, title, description)
                .drawShadow(true)
                .cancelable(true)
                .outerCircleColor(R.color.red_theme_color)
                .targetCircleColor(R.color.white)
                .tintTarget(true)
                .textColor(R.color.white)
                .transparentTarget(false)
                .dimColor(R.color.black)
                .targetRadius(radius);

    }

    public static TapTarget makeTapTargetWithoutTintTarget(View view, String title, String description, int radius) {

        return TapTarget.forView(view, title, description)
                .drawShadow(true)
                .cancelable(true)
                .outerCircleColor(R.color.red_theme_color)
                .targetCircleColor(R.color.white)
                .tintTarget(false)
                .textColor(R.color.white)
                .transparentTarget(false)
                .dimColor(R.color.black)
                .targetRadius(radius);
    }

    public static TapTarget makeTapTargetWithTransparentTarget(View view, String title, String description, int radius) {
        return TapTarget.forView(view, title, description)
                .drawShadow(true)
                .cancelable(true)
                .outerCircleColor(R.color.red_theme_color)
                .targetCircleColor(R.color.white)
                .tintTarget(false)
                .textColor(R.color.white)
                .transparentTarget(true)
                .dimColor(R.color.black)
                .targetRadius(radius);
    }

    public static void makeTapTargetSkipButton(View anchor) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                anchor.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_skip_button_onboarding_guide, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height);

        //popupWindow.showAsDropDown(anchor);
        popupWindow.setFocusable(false);
        popupWindow.showAtLocation(anchor, Gravity.FILL_VERTICAL | Gravity.FILL_HORIZONTAL, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {


            @Override
            public void onDismiss() {
                currentOnScreenTapTargetView.setVisibility(View.GONE);
                if (isTapTargetsAvailable()) {
                    if(isDialogTarget){
                        currentOnScreenTapTargetView = TapTargetView.showFor(dialog, tapTargetArrayList.get(tapTargetIterator), listener);
                    }else {
                        currentOnScreenTapTargetView = TapTargetView.showFor(activity, tapTargetArrayList.get(tapTargetIterator), listener);
                    }
                    popupWindow.showAtLocation(anchor, Gravity.FILL_VERTICAL | Gravity.FILL_HORIZONTAL, 0, 0);
                } else {
                    savePrefsDataForUserGuide();
                }
            }
        });
        popupWindow.getContentView().findViewById(R.id.btn_skip_onboarding_taptarget).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapTargetIterator = tapTargetArrayList.size() - 1;
                savePrefsDataForUserGuide();
                popupWindow.dismiss();
            }
        });
    }

    private static boolean isTapTargetsAvailable() {
        if (tapTargetIterator < tapTargetArrayList.size() - 1) {
            tapTargetIterator++;
            return true;
        } else {
            return false;
        }
    }
    public static void startSequence(Activity ctx) {
        activity = ctx;
        isDialogTarget=false;
        tapTargetIterator = 0;
        listener=new TapTargetView.Listener(){
            @Override
            public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                super.onTargetDismissed(view, userInitiated);
                tapTargetIterator = tapTargetArrayList.size() - 1;
                savePrefsDataForUserGuide();
                popupWindow.dismiss();
            }
        };
        currentOnScreenTapTargetView = TapTargetView.showFor(activity, tapTargetArrayList.get(tapTargetIterator),listener);
    }

    public static void startSequenceForDialog(Dialog dl,Activity ctx) {
        tapTargetIterator = 0;
        isDialogTarget=true;
        activity=ctx;
        dialog=dl;
        listener=new TapTargetView.Listener(){
            @Override
            public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                super.onTargetDismissed(view, userInitiated);
                savePrefsDataForUserGuide();
                popupWindow.dismiss();
            }
        };
        currentOnScreenTapTargetView = TapTargetView.showFor(dialog, tapTargetArrayList.get(tapTargetIterator),listener);
    }

    public static boolean hasUserSeenGuide(String name, Context ctx) {
        tapTargetSequenceName = name;
        context = ctx;
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(context.getResources().getString(R.string.tap_target_shared_pref_db), MODE_PRIVATE);
        boolean isIntroActivityOpenedBefore = preferences.getBoolean(tapTargetSequenceName, false);
        return isIntroActivityOpenedBefore;
    }

    public static void savePrefsDataForUserGuide() {
        SharedPreferences preferences = context.getSharedPreferences(context.getResources().getString(R.string.tap_target_shared_pref_db), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(tapTargetSequenceName, true);
        editor.apply();
    }
}

