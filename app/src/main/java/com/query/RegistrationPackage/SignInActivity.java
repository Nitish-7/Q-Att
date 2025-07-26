package com.query.RegistrationPackage;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.query.ClassesPackage.ClassesActivity;
import com.query.InitializerPackage.ConnectivityReceiver;
import com.query.FirebaseDao;
import com.query.R;
import com.query.UserDataPackage.User;
import com.query.UserDataPackage.UserEntries;
import com.query.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 20;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String email;
    private String password;
    private ActivitySignInBinding binding;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseDatabase firebaseDatabase;

    public ConnectivityReceiver receiver;

    @Override
    protected void onPause() {
        super.onPause();
        receiver.endInternetReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new ConnectivityReceiver();
        receiver.startInternetReceiver(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        firebaseDatabase = FirebaseDatabase.getInstance();
        //Log.d("**Signing**", "started+++++++++++++++++++++++ at time = " + FirebaseDao.getOnlyTime());

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser fuser = mAuth.getCurrentUser();
        FirebaseDao.setCurrentFirebaseUser(fuser);
        if (fuser != null && (fuser.isEmailVerified() || fuser.getPhoneNumber() != null)) {//|| fuser.getEmail().equals("abc@gmail.com"))) {
            fuser.reload();
            FirebaseDao.setmCurrentUserId(fuser.getUid());

            if(!UserEntries.isUserDetailsAvailableOnSharedPreferences(SignInActivity.this,fuser.getUid())){
                UserEntries.settingCurrentUserByUsingDatabase(this);
            }
            startActivity(new Intent(SignInActivity.this, ClassesActivity.class));
            finish();
        }

        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewUserWithEmail();
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUserWithEmail();
            }
        });

        binding.googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

        binding.googleSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

    }

    //////////
    //google//
    //////////


    private void signInWithGoogle() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        showLoadingScreen("Loging In with google\nJust a second");

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser fuser = mAuth.getCurrentUser();
                            FirebaseDao.setCurrentFirebaseUser(fuser);
                            if (fuser != null) {
                                FirebaseDao.setmCurrentUserId(fuser.getUid());
                            }
                            User user = new User(FirebaseDao.getmCurrentUserId(), fuser.getDisplayName(), fuser.getEmail(), "null", UserEntries.SWIPE_ATTENDANCE_MODE);

                            FirebaseDao.insertUserDetailsOnDatabase(user, SignInActivity.this);

                            hideLoadingScreen();
                            startActivity(new Intent(SignInActivity.this, ClassesActivity.class));
                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            hideLoadingScreen();
                            //updateUI(null);
                        }
                    }
                });
    }

    private void loginUserWithEmail() {

        email = binding.emailTvLogin.getText().toString();
        password = binding.passwordTvLogin.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Enter E-mail",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Enter Password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingScreen("Verifying Please wait");


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithEmail:success");
                            FirebaseUser fuser = mAuth.getCurrentUser();
                            FirebaseDao.setCurrentFirebaseUser(fuser);
                            if (fuser != null) {
                                FirebaseDao.setmCurrentUserId(fuser.getUid());
                            }

                            if (!UserEntries.isUserDetailsAvailableOnSharedPreferences(SignInActivity.this, fuser.getUid())) {
                                UserEntries.settingCurrentUserByUsingDatabase(SignInActivity.this);
                            }

                            hideLoadingScreen();
                            startActivity(new Intent(SignInActivity.this, ClassesActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            hideLoadingScreen();
                            //updateUI(null);
                        }
                    }
                });
    }


    private void createNewUserWithEmail() {
        String userName = binding.usernameTv.getText().toString();
        email = binding.emailTvSignup.getText().toString();
        password = binding.passwordTvSignup.getText().toString();
        if (email.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Enter E-mail",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Enter Password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (userName.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Enter User Name",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String confirmPassword = binding.passwordTvConfirmSignup.getText().toString();


        if (!password.matches(confirmPassword)) {
            Toast.makeText(SignInActivity.this, "confirm password not matching\n",
                    Toast.LENGTH_SHORT).show();
            return;
        }


        showLoadingScreen("Creating New Account\nJust a second");


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "createUserWithEmail:success");
                            FirebaseUser fuser = mAuth.getCurrentUser();

                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    setContentView(R.layout.email_verification_screen);
                                    hideLoadingScreen();
                                    findViewById(R.id.ic_question_mark_in_email_verification).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (findViewById(R.id.tv_question_mark_in_email_verification).getVisibility() == View.INVISIBLE) {
                                                findViewById(R.id.tv_question_mark_in_email_verification).setVisibility(View.VISIBLE);
                                            } else {
                                                findViewById(R.id.tv_question_mark_in_email_verification).setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                    WaitForEmailConfirmation waitForEmailConfirmation = new WaitForEmailConfirmation();
                                    waitForEmailConfirmation.execute(fuser);

                                    findViewById(R.id.btnNextAfterVerifyEmail).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            FirebaseDao.setCurrentFirebaseUser(fuser);

                                            //updateUI(user);
                                            if (fuser != null) {
                                                FirebaseDao.setmCurrentUserId(fuser.getUid());
                                            }

                                            User user = new User(FirebaseDao.getmCurrentUserId(), userName, email, password, UserEntries.SWIPE_ATTENDANCE_MODE);

                                            FirebaseDao.insertUserDetailsOnDatabase(user, SignInActivity.this);

                                            startActivity(new Intent(SignInActivity.this, ClassesActivity.class));
                                            finish();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SignInActivity.this, "Something wrong with given E-mail\n try with different E-mail", Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                            hideLoadingScreen();
                        }
                    }
                });

    }


    //on clickes all functions

    public void gotoForgetPasswordScreen(View view) {
        startActivity(new Intent(this, ForgetPasswordActivity.class));
    }

    public void gotoRegisterScreen(View view) {
        binding.loginScreen.setVisibility(View.GONE);
        binding.signUpScreen.setVisibility(View.VISIBLE);
    }

    public void gotoLoginScreen(View view) {
        binding.signUpScreen.setVisibility(View.GONE);
        binding.loginScreen.setVisibility(View.VISIBLE);
    }

    public void goToPhoneRegistrationScreen(View view) {
        startActivity(new Intent(this, PhoneSignInActivity.class));
        if (FirebaseDao.getCurrentFirebaseUser() != null) {
            finish();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showPasswordSignup(View view) {
        EditText password = binding.passwordTvSignup;
        if (password.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) {
            binding.showPasswordIcSignup.setImageResource(R.drawable.ic_show_password);
            password.setInputType(129);
            password.setTextAppearance(R.style.password_text_style);
            password.setSelection(password.getText().toString().length());
        } else {
            binding.showPasswordIcSignup.setImageResource(R.drawable.ic_hide_password);
            password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
            password.setTextAppearance(R.style.password_text_style);
            password.setSelection(password.getText().toString().length());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showConfirmPasswordSignup(View view) {
        EditText password = binding.passwordTvConfirmSignup;
        if (password.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) {
            binding.showPasswordIcConfirmSignup.setImageResource(R.drawable.ic_show_password);
            password.setInputType(129);
            password.setTextAppearance(R.style.password_text_style);
            password.setSelection(password.getText().toString().length());
        } else {
            binding.showPasswordIcConfirmSignup.setImageResource(R.drawable.ic_hide_password);
            password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
            password.setTextAppearance(R.style.password_text_style);
            password.setSelection(password.getText().toString().length());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showPasswordLogin(View view) {
        EditText password = binding.passwordTvLogin;
        if (password.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) {
            binding.showPasswordIcLogin.setImageResource(R.drawable.ic_show_password);
            password.setInputType(129);
            password.setTextAppearance(R.style.password_text_style);
            password.setSelection(password.getText().toString().length());
        } else {
            binding.showPasswordIcLogin.setImageResource(R.drawable.ic_hide_password);
            password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
            password.setTextAppearance(R.style.password_text_style);
            password.setSelection(password.getText().toString().length());
        }
    }

    class WaitForEmailConfirmation extends AsyncTask<FirebaseUser, Void, FirebaseUser> {
        @Override
        protected void onPreExecute() {
            findViewById(R.id.btnNextAfterVerifyEmail).getBackground().setTint(ContextCompat.getColor(SignInActivity.this, R.color.offblack3));
        }

        @Override
        protected FirebaseUser doInBackground(FirebaseUser... fuser) {
            while (!fuser[0].isEmailVerified()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                fuser[0].reload();
                findViewById(R.id.btnNextAfterVerifyEmail).setClickable(false);
            }
            return fuser[0];
        }

        @Override
        protected void onPostExecute(FirebaseUser fuser) {
            if (fuser.isEmailVerified()) {
                findViewById(R.id.btnNextAfterVerifyEmail).setClickable(true);
                findViewById(R.id.btnNextAfterVerifyEmail).getBackground().setTint(ContextCompat.getColor(SignInActivity.this, R.color.red_theme_color));
                TextView textView = findViewById(R.id.emailVerifyAccountHeading);
                textView.setText("E-mail is verified proceed next");
                findViewById(R.id.ll_question_mark_in_email_verification).setVisibility(View.GONE);
            }
        }

    }

    private void showLoadingScreen(String message) {
        binding.loadingScreenEmailLogin.setVisibility(View.VISIBLE);
        binding.tvLoadingScreenEmailLogin.setText(message);
    }

    private void hideLoadingScreen() {
        binding.loadingScreenEmailLogin.setVisibility(View.GONE);
    }
}