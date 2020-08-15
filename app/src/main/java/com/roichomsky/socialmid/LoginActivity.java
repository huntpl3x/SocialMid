package com.roichomsky.socialmid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    Dialog registerDialog;
    TextView mNoAccountTv, mForgotPassTv, mHaveAccountTv;
    EditText mEmailEt, mPasswordEt, mNameEt, mPasswordEt2, mEmailEt2;
    Button mLoginBtn, mRegisterBtN2;
    private FirebaseAuth mAuth, mAuth2;

    //progressBar to display while logging user and registering one
    ProgressDialog progressDialog, progressDialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        // Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mLoginBtn = findViewById(R.id.loginBtn);
        mNoAccountTv = findViewById(R.id.noAccountTv);
        mForgotPassTv = findViewById(R.id.forgotPassTv);
        progressDialog = new ProgressDialog(this);

        //forgot password TextView click
        mForgotPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        //don't have an account TextView click open register dialog
        mNoAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRegisterDialog();
            }
        });

        //login button click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                //validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error and focus to email editText
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else{
                    loginUser(email, password); // login in the user
                }
            }
        });
    }

    private void showRecoverPasswordDialog() {
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        //set layout - LinearLayout
        LinearLayout linearLayout = new LinearLayout(this);
        //views to set in dialog
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        //sets the mid EditText width to fit n amount of 'M' letters
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //buttons recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input email
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });

        //buttons cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();
    }

    private void beginRecovery(String email) {
        //displays the progressBar
        progressDialog.show();
        progressDialog.setMessage("Sending email...");
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Check email for reset link", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get and show proper error message
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        //displays the progressBar
        progressDialog.setMessage("Logging In...");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //dismisses the progressBar
                            progressDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            //if user is signing in first time then get and show user info
                            if (task.getResult().getAdditionalUserInfo().isNewUser()){
                                // Get user email and uid from auth
                                String email = user.getEmail();
                                String uid = user.getUid();
                                // When user is registered store user info in firebase realtime database too
                                // using HashMap
                                HashMap<Object , String> hashMap = new HashMap<>();
                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("name", "");
                                // firebase database instance
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                // Path to store user data named "Users"
                                DatabaseReference reference = database.getReference("Users");
                                // Put data withing hashmap in database
                                reference.child(uid).setValue(hashMap);
                            }
                            //user is logged in, so start DashboardActivity
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            //dismisses the progressBar
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dismisses the progressBar
                progressDialog.dismiss();
                //error, get and show error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public boolean onSupportNavigateUp(){
        onBackPressed(); // go previous activity
        return super.onSupportNavigateUp();
    }

    public void createRegisterDialog()
    {
        // Creates dialog and sets the layout and flags
        registerDialog = new Dialog(this);
        registerDialog.setCancelable(true);
        registerDialog.setContentView(R.layout.register_dialog);

        // Init
        mEmailEt2 = registerDialog.findViewById(R.id.emailEt);
        mPasswordEt2 = registerDialog.findViewById(R.id.passwordEt);
        mNameEt = registerDialog.findViewById(R.id.nameEt);
        mRegisterBtN2 = registerDialog.findViewById(R.id.registerBtn);
        mHaveAccountTv = registerDialog.findViewById(R.id.haveAccountTv);
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerDialog.dismiss();
            }
        });

        // Handle register button click
        mRegisterBtN2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input email, password
                String email = mEmailEt2.getText().toString().trim();
                String password = mPasswordEt2.getText().toString().trim();
                String name = mNameEt.getText().toString().trim();
                //validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error and focus to email editText
                    mEmailEt2.setError("Invalid Email");
                    mEmailEt2.setFocusable(true);
                }
                else if(password.length()<6){
                    //set error and focus to password editText
                    mPasswordEt2.setError("Password length at least 6 characters");
                    mPasswordEt2.setFocusable(true);
                }
                else if(name.length()<2){
                    //set error and focus to password editText
                    mNameEt.setError("Invalid nickname");
                    mNameEt.setFocusable(true);
                }
                else{
                    registerUser(email, password); // Registers the user
                }
            }
        });

        registerDialog.show();
    }

    private void registerUser(String email, String password) {
        //email and password pattern is valid, show progressDialog and start registering user
        progressDialog2.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start regiser activity.
                            progressDialog2.dismiss();

                            FirebaseUser user = mAuth2.getCurrentUser();
                            // Get user email and uid from auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            String name = mNameEt.getText().toString().trim();
                            // When user is registered stroe user info in firebase realtime database too
                            // using HashMap
                            HashMap<Object , String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", name);
                            hashMap.put("image", "");
                            // firebase database instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            // Path to store user data named "Users"
                            DatabaseReference reference = database.getReference("Users");
                            // Put data withing hashmap in database
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(LoginActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog2.dismiss();
                            Toast.makeText(LoginActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog2.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
