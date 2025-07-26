package com.query.RegistrationPackage;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.query.InitializerPackage.ConnectivityReceiver;
import com.query.databinding.ActivityForgetPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etvEmail;
    private String email;
    ActivityForgetPasswordBinding binding;

    public  ConnectivityReceiver receiver;
    @Override
    protected void onPause() {
        super.onPause();
        receiver.endInternetReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver=new ConnectivityReceiver();
        receiver.startInternetReceiver(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
        etvEmail = binding.etvEmailForget;

        binding.btnSendEmailForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etvEmail.getText().toString();
                if (validateEmail()) {
                    binding.etvEmailForget.clearFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.etvEmailForget.getWindowToken(), 0);
                    binding.tvEmailInForgetPassword.setText(email);
                    binding.emailScreenForgetPassword.setVisibility(View.GONE);
                    binding.loadingScreenSendingResetEmail.setVisibility(View.VISIBLE);
                    mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            binding.emailSentScreenForgetPassword.setVisibility(View.VISIBLE);
                            binding.loadingScreenSendingResetEmail.setVisibility(View.GONE);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            binding.emailScreenForgetPassword.setVisibility(View.VISIBLE);
                            binding.loadingScreenSendingResetEmail.setVisibility(View.GONE);
                            Toast.makeText(ForgetPasswordActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        binding.btnLoginAfterChangingPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.icQuestionMarkInForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.tvQuestionMarkInForgetPassword.getVisibility() == View.INVISIBLE)
                    binding.tvQuestionMarkInForgetPassword.setVisibility(View.VISIBLE);
                else
                    binding.tvQuestionMarkInForgetPassword.setVisibility(View.INVISIBLE);
            }
        });

        binding.tvChangeEmailInForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.emailScreenForgetPassword.setVisibility(View.VISIBLE);
                binding.emailSentScreenForgetPassword.setVisibility(View.GONE);
                binding.etvEmailForget.requestFocus();
            }
        });
    }

    private boolean validateEmail() {
        if (email.isEmpty()) {
            etvEmail.setError("enter email");
            etvEmail.requestFocus();
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etvEmail.setError("enter a valid email");
            etvEmail.requestFocus();
            return false;
        }

        return true;
    }
}