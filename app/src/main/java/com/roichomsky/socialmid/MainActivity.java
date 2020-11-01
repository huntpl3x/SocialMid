package com.roichomsky.socialmid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //3310 JAVA CODING LINES

    //views
    EditText mEmailEt, mPasswordEt, mNameEt;
    TextView mHaveAccountTv;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    Dialog registerDialog;
    Button mRegisterBtn, mLoginBtn, mRegisterBtN2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init views

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");
        mAuth = FirebaseAuth.getInstance();
        mRegisterBtn = findViewById(R.id.registerBtn);
        mLoginBtn = findViewById(R.id.loginBtn);

        // Handle register button click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start RegisterActivity
                createRegisterDialog();
            }
        });
        //handle login button click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }

    public void createRegisterDialog()
    {
        registerDialog = new Dialog(this);
        registerDialog.setCancelable(true);
        registerDialog.setContentView(R.layout.register_dialog);

        // Init
        mEmailEt = registerDialog.findViewById(R.id.emailEt);
        mPasswordEt = registerDialog.findViewById(R.id.passwordEt);
        mRegisterBtN2 = registerDialog.findViewById(R.id.registerBtn);
        mHaveAccountTv = registerDialog.findViewById(R.id.haveAccountTv);
        mNameEt = registerDialog.findViewById(R.id.nameEt);

        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        // Handle register button click
        mRegisterBtN2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input email, password
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                String name = mNameEt.getText().toString().trim();
                //validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error and focus to email editText
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else if(password.length()<6){
                    //set error and focus to password editText
                    mPasswordEt.setError("Password length at least 6 characters");
                    mPasswordEt.setFocusable(true);
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
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start register activity.
                            progressDialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();
                            // Get user email and uid from auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            String name = mNameEt.getText().toString().trim();
                            // When user is registered store user info in firebase realtime database too
                            // using HashMap
                            HashMap<Object , String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", name);
                            // firebase database instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            // Path to store user data named "Users"
                            DatabaseReference reference = database.getReference("Users");
                            // Put data withing hashmap in database
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(MainActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
