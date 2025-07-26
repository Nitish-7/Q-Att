package com.query.InitializerPackage;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.query.R;

public class NoInternetDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.error_screen, container, false);
        Button okButton = view.findViewById(R.id.btn_error_try_again);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the dialog
                //view.findViewById(R.id.progress_bar_error_try_again).setVisibility(View.VISIBLE);
                okButton.setVisibility(View.INVISIBLE);
                view.findViewById(R.id.progress_bar_error_try_again).setVisibility(View.VISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.findViewById(R.id.progress_bar_error_try_again).setVisibility(View.INVISIBLE);
                        okButton.setVisibility(View.VISIBLE);
                    }
                },1500);
            }
        });

        return view;
    }

}