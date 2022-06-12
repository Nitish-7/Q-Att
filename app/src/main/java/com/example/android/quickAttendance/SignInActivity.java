package com.example.android.quickAttendance;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.quickAttendance.ClassesPackage.ClassesActivity;
import com.example.android.quickAttendance.UserDataPackage.User;
import com.example.android.quickAttendance.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import static java.lang.Thread.sleep;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 20;
    private final FirebaseAuth mAuth= FirebaseAuth.getInstance();
    private String email;
    private String password;
    private ActivitySignInBinding binding;
    private ProgressDialog progressDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseDatabase firebaseDatabase;


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
            FirebaseDao.setmCurrentUser(currentUser);
            FirebaseDao.setmCurrentUserId(currentUser.getUid());
            Log.d("**id**", "**idStart** : "+FirebaseDao.getmCurrentUserId());
            Toast.makeText(this, "uIdStar : "+FirebaseDao.getmCurrentUserId(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignInActivity.this, ClassesActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        progressDialog = new ProgressDialog(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        Toast.makeText(this, "uIdCreate : "+FirebaseDao.getmCurrentUserId(), Toast.LENGTH_SHORT).show();
        Log.d("**id**", "**idCreate** : "+FirebaseDao.getmCurrentUserId());

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

    private void signInWithGoogle() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

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
        progressDialog.setTitle("Loging In with google");
        progressDialog.setMessage("Just a second");
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                FirebaseDao.setmCurrentUserId(user.getUid());
                            }
                            FirebaseDao.insertUserDetailes(new User(FirebaseDao.getmCurrentUserId(),user.getDisplayName(),user.getEmail(),null));
                            FirebaseDao.setmCurrentUser(user);
                            progressDialog.dismiss();
                            startActivity(new Intent(SignInActivity.this, ClassesActivity.class));
                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            progressDialog.dismiss();
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

        progressDialog.setTitle("Loging In");
        progressDialog.setMessage("Just a second");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                FirebaseDao.setmCurrentUserId(user.getUid());
                            }
                            //updateUI(user);
                            progressDialog.dismiss();
                            startActivity(new Intent(SignInActivity.this, ClassesActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
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

        progressDialog.setTitle("Creating New User");
        progressDialog.setMessage("Just a second");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            if (user != null) {
                                FirebaseDao.setmCurrentUserId(user.getUid());
                            }
                            FirebaseDao.insertUserDetailes(new User(FirebaseDao.getmCurrentUserId(),userName,email,password));
                            progressDialog.dismiss();
                            startActivity(new Intent(SignInActivity.this, ClassesActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                            progressDialog.dismiss();
                        }
                    }
                });

    }



    //on clickes all functions

    public void gotoRegisterScreen(View view) {
        binding.loginScreen.setVisibility(View.GONE);
        binding.signUpScreen.setVisibility(View.VISIBLE);
    }

    public void gotoLoginScreen(View view) {
        binding.signUpScreen.setVisibility(View.GONE);
        binding.loginScreen.setVisibility(View.VISIBLE);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showPasswordSignup(View view) {
        EditText password = binding.passwordTvSignup;
        if (password.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) {
            binding.showPasswordIcSignup.setImageResource(R.drawable.ic_show_password);
            password.setInputType(129);
            password.setTextAppearance(R.style.empty_view_title_text_style);
            password.setSelection(password.getText().toString().length());
        } else {
            binding.showPasswordIcSignup.setImageResource(R.drawable.ic_hide_password);
            password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
            password.setTextAppearance(R.style.empty_view_title_text_style);
            password.setSelection(password.getText().toString().length());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showConfirmPasswordSignup(View view) {
        EditText password = binding.passwordTvConfirmSignup;
        if (password.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) {
            binding.showPasswordIcConfirmSignup.setImageResource(R.drawable.ic_show_password);
            password.setInputType(129);
            password.setTextAppearance(R.style.empty_view_title_text_style);
            password.setSelection(password.getText().toString().length());
        } else {
            binding.showPasswordIcConfirmSignup.setImageResource(R.drawable.ic_hide_password);
            password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
            password.setTextAppearance(R.style.empty_view_title_text_style);
            password.setSelection(password.getText().toString().length());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showPasswordLogin(View view) {
        EditText password = binding.passwordTvLogin;
        if (password.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) {
            binding.showPasswordIcLogin.setImageResource(R.drawable.ic_show_password);
            password.setInputType(129);
            password.setTextAppearance(R.style.empty_view_title_text_style);
            password.setSelection(password.getText().toString().length());
        } else {
            binding.showPasswordIcLogin.setImageResource(R.drawable.ic_hide_password);
            password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
            password.setTextAppearance(R.style.empty_view_title_text_style);
            password.setSelection(password.getText().toString().length());
        }
    }
}