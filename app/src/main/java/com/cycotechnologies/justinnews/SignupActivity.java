package com.cycotechnologies.justinnews;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import cn.pedant.SweetAlert.SweetAlertDialog;

import com.cycotechnologies.justinnews.validator;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db; // Correctly initialized
    private EditText usernameInput, emailInput, pwdInput, confirmPwdInput;
    private Button signupBtn;
    private TextView loginNow;

    private static final String TAG = "SignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        pwdInput = findViewById(R.id.pwd_input);
        confirmPwdInput = findViewById(R.id.confirm_pwd_input);
        signupBtn = findViewById(R.id.signupbtn);

        loginNow = findViewById(R.id.loginnow);

        loginNow.setOnClickListener(v->{
            Intent loginIntent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        });
        signupBtn.setOnClickListener(v-> registerNewUser());
    }

    private void registerNewUser(){

        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = pwdInput.getText().toString().trim();
        String confirm_password = confirmPwdInput.getText().toString().trim();

        // Input validation
        if(username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm_password.isEmpty()){
            new SweetAlertDialog(SignupActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Check Again")
                    .setContentText("All fields must be filled")
                    .show();
            return;
        }

        if(!validator.isValidEmail(email)){
            emailInput.setError("Invalid Email");
            emailInput.requestFocus(); // Focus on the problematic input
        }else if(!validator.isValidPassword(password)){
            pwdInput.setError("Password must contain:\n" +
                    "- At least 8 characters\n" +
                    "- One uppercase letter (A–Z)\n" +
                    "- One lowercase letter (a–z)\n" +
                    "- One digit (0–9)\n" +
                    "- One special character (!@#$%^&*)");
            pwdInput.requestFocus();
        }else if(!validator.doPasswordsMatch(password, confirm_password)){
            confirmPwdInput.setError("Password do not match");
            confirmPwdInput.requestFocus();
        }else{
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public  void onComplete(@NonNull Task<AuthResult> task){
                            if(task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                if(user != null){
                                    String uid = user.getUid();

                                    // Prepare user data for Firestore
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("username", username);
                                    userMap.put("email", email);

                                    // Get document reference for the new user in "User" collection
                                    DocumentReference userRef = db.collection("User").document(uid);

                                    // Save user data to Firestore
                                    userRef.set(userMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Log.d(TAG, "Username and email saved to Firestore for UID: " + uid);
                                                        new SweetAlertDialog(SignupActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                                .setTitleText("Great!")
                                                                .setContentText("Username saved Successfully")
                                                                .show();
                                                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                                        finish();
                                                    }else{
                                                        // Firestore save failed
                                                        Log.e(TAG, "Failed to save username to Firestore: " + task.getException().getMessage(), task.getException());
                                                        new SweetAlertDialog(SignupActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                                .setTitleText("Oops!")
                                                                .setContentText("Failed to save username. Please try again.")
                                                                .show();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Firestore operation failed: " + e.getMessage(), e);
                                                    new SweetAlertDialog(SignupActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                            .setTitleText("Oops!")
                                                            .setContentText("Database Error Occurred: " + e.getMessage()) // Show specific error
                                                            .show();
                                                }
                                            });
                                }
                            }else {
                                // Firebase Auth registration failed
                                Log.e(TAG, "Firebase Authentication failed: " + task.getException().getMessage(), task.getException());
                                new SweetAlertDialog(SignupActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Oops!")
                                        .setContentText("Authentication Failed: " + task.getException().getMessage()) // Show specific error
                                        .show();
                            }
                        }
                    });
        }
    }
}
