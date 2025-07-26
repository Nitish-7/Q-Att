package com.query.RegistrationPackage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.query.ClassesPackage.ClassesActivity;
import com.query.InitializerPackage.ConnectivityReceiver;
import com.query.FirebaseDao;
import com.query.R;
import com.query.UserDataPackage.User;
import com.query.UserDataPackage.UserEntries;
import com.query.databinding.ActivityPhoneSignInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class PhoneSignInActivity extends AppCompatActivity {

    // variable for FirebaseAuth class
    private FirebaseAuth mAuth;

    // variable for our text input
    // field for phone and OTP.
    private EditText edtPhone, edtOTP;

    // buttons for generating OTP and verifying OTP
    private Button verifyOTPBtn, generateOTPBtn;

    // string for storing our verification ID
    private String verificationId;
    private ProgressDialog progressDialog;
    ActivityPhoneSignInBinding binding;
    CountryCodePicker ccp;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneSignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        progressDialog = new ProgressDialog(this);
        // below line is for getting instance
        // of our FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();

        // initializing variables for button and Edittext.
        edtPhone = findViewById(R.id.idEdtPhoneNumber);
        edtOTP = findViewById(R.id.idEdtOtp);
        verifyOTPBtn = findViewById(R.id.idBtnVerify);
        generateOTPBtn = findViewById(R.id.idBtnGetOtp);
        ccp = findViewById(R.id.ccp);

        // setting onclick listener for generate OTP button.
        generateOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // below line is for checking weather the user
                // has entered his mobile number or not.
                if (TextUtils.isEmpty(edtPhone.getText().toString()) || !Patterns.PHONE.matcher(edtPhone.getText().toString()).matches()) {
                    // when mobile number text field is empty
                    // displaying a toast message.
                    edtPhone.setError("enter phone number");
                    edtPhone.requestFocus();
                } else if ((edtPhone.getText().toString().trim().length() != 10) || !Patterns.PHONE.matcher(edtPhone.getText().toString()).matches()) {
                    // when mobile number text field is empty
                    // displaying a toast message.
                    edtPhone.setError("Invalid phone number");
                    edtPhone.requestFocus();
                } else {
                    // if the text field is not empty we are calling our
                    // send OTP method for getting OTP from Firebase.
                    String phone = ccp.getSelectedCountryCodeWithPlus() + edtPhone.getText().toString();
                    progressDialog.setTitle("Sending OTP");
                    progressDialog.setMessage("Please wait");
                    progressDialog.show();
                    sendVerificationCode(phone);
                }
            }
        });

        // initializing on click listener
        // for verify otp button
        verifyOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // validating if the OTP text field is empty or not.
                if (TextUtils.isEmpty(edtOTP.getText().toString())) {
                    binding.idEdtOtp.setError("enter otp");
                    binding.idEdtOtp.requestFocus();
                } else {
                    binding.phoneEditScreen.setVisibility(View.GONE);
                    binding.otpEditScreen.setVisibility(View.GONE);
                    binding.loadingScreenPhoneLogin.setVisibility(View.VISIBLE);
                    verifyCode(edtOTP.getText().toString());
                }
            }
        });

        binding.tvChangePhoneNoPhoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.phoneEditScreen.setVisibility(View.VISIBLE);
                binding.otpEditScreen.setVisibility(View.GONE);
                binding.idEdtPhoneNumber.requestFocus();
            }
        });

        binding.idBtnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(binding.etvUserNamePhoneLogin.getText().toString().trim())) {
                    binding.etvUserNamePhoneLogin.setError("enter user name");
                    binding.etvUserNamePhoneLogin.requestFocus();
                } else {
                    FirebaseUser fuser = mAuth.getCurrentUser();
                    FirebaseDao.setCurrentFirebaseUser(fuser);

                    if (fuser != null) {
                        FirebaseDao.setmCurrentUserId(fuser.getUid());
                    }
                    User user = new User(FirebaseDao.getmCurrentUserId(), binding.etvUserNamePhoneLogin.getText().toString().trim(), "+91" + binding.idEdtPhoneNumber.getText().toString(), "null", UserEntries.SWIPE_ATTENDANCE_MODE);

                    FirebaseDao.insertUserDetailsOnDatabase(user,PhoneSignInActivity.this);

                    Intent i = new Intent(PhoneSignInActivity.this, ClassesActivity.class);
                    startActivity(i);
                    finish();

                }
            }
        });

    }


    private void signInWithCredential(PhoneAuthCredential credential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // if the code is correct and the task is successful
                            // we are sending our user to new activity.
                            FirebaseUser fuser = mAuth.getCurrentUser();
                            if (fuser != null) {
                                FirebaseDao.setmCurrentUserId(fuser.getUid());
                                FirebaseDao.getUserDetailsDbReference().child(UserEntries.USER_NAME).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        binding.etvUserNamePhoneLogin.setText(snapshot.getValue(String.class));

                                        binding.loadingScreenPhoneLogin.setVisibility(View.GONE);
                                        binding.userNameEditScreen.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(PhoneSignInActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                        } else {
                            // if the code is not correct then we are
                            // displaying an error message to the user.
                            binding.idEdtOtp.setError("wrong OTP");
                            binding.loadingScreenPhoneLogin.setVisibility(View.GONE);
                            binding.otpEditScreen.setVisibility(View.VISIBLE);
                            binding.idEdtOtp.requestFocus();
                        }

                    }
                });
    }


    private void sendVerificationCode(String number) {
        // this method is used for getting
        // OTP on user phone number.
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)            // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)           // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // callback method is called on Phone auth provider.
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            // initializing our callbacks for on
            // verification callback method.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // below method is used when
        // OTP is sent from Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            // when we receive the OTP it
            // contains a unique id which
            // we are storing in our string
            // which we have already created.
            progressDialog.dismiss();
            binding.phoneEditScreen.setVisibility(View.GONE);
            binding.otpEditScreen.setVisibility(View.VISIBLE);
            binding.tvPhoneNoPhoneLogin.setText("+91" + edtPhone.getText().toString());

            verificationId = s;
        }

        // this method is called when user
        // receive OTP from Firebase.
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // below line is used for getting OTP code
            // which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();

            // checking if the code
            // is null or not.
            if (code != null) {
                // if the code is not null then
                // we are setting that code to
                // our OTP edittext field.
                edtOTP.setText(code);

                // after setting this code
                // to OTP edittext field we
                // are calling our verifycode method.
                verifyCode(code);
            }
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // displaying error message with firebase exception.
            Toast.makeText(PhoneSignInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    // below method is use to verify code from Firebase.
    private void verifyCode(String code) {
        // below line is used for getting
        // credentials from our verification id and code.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //Back buttons was pressed, do whatever logic you want
            // create the dialog window
            if (binding.otpEditScreen.getVisibility() == View.VISIBLE) {
                binding.otpEditScreen.setVisibility(View.GONE);
                binding.phoneEditScreen.setVisibility(View.VISIBLE);
            } else {
                finish();
            }

        }

        return false;
    }


}