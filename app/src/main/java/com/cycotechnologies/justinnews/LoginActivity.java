package com.cycotechnologies.justinnews;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast; // Added Toast for direct error feedback during development

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot; // Import DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore; // Import FirebaseFirestore

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn, GLoginBtn;
    private EditText emailInput, pwdInput;
    private TextView signupNow;
    private FirebaseAuth auth;
    private FirebaseFirestore db; // Declare Firestore instance

    // Make sure these constants match what HomeFragment expects
    public static final String SHARED_PREFS = "UserPrefs";
    public static final String EMAIL_KEY = "email_key"; // Keeping this as per your request
    public static final String USERNAME_KEY = "USERNAME"; // This is for the greeting
    public static final String PASSWORD_KEY = "password_key"; // Still generally not recommended to store plain password

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        emailInput = findViewById(R.id.email_input);
        pwdInput = findViewById(R.id.pwd_input);
        loginBtn = findViewById(R.id.loginbtn);

        // Ensure this name matches what you use in HomeFragment
        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        signupNow = findViewById(R.id.signupnow);

        signupNow.setOnClickListener(v->{
            Intent signupIntent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(signupIntent);
        });

        loginBtn.setOnClickListener(v->loginUserAccount());
    }

    private void loginUserAccount(){
        String email = emailInput.getText().toString().trim();
        String password = pwdInput.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()){
            new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops")
                    .setContentText("All fields must be filled")
                    .show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                String userEmail = user.getEmail(); // Get email from FirebaseUser

                                // --- Step 1: Save email to SharedPreferences as before ---
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(EMAIL_KEY, userEmail); // Save the email
                                // editor.putString(PASSWORD_KEY, password); // Still recommend not saving plain password
                                editor.apply(); // Apply these changes now

                                // --- Step 2: Fetch username from Firestore ---
                                db.collection("User").document(uid).get()
                                        .addOnCompleteListener(userDocTask -> {
                                            String fetchedUsername = "Guest"; // Default username if not found

                                            if (userDocTask.isSuccessful()) {
                                                DocumentSnapshot document = userDocTask.getResult();
                                                if (document.exists()) {
                                                    // Assuming your username field in Firestore is named "username"
                                                    String usernameFromDb = document.getString("username");
                                                    if (usernameFromDb != null && !usernameFromDb.isEmpty()) {
                                                        fetchedUsername = usernameFromDb;
                                                    } else {
                                                        // Fallback if "username" field is missing or empty in document
                                                        Toast.makeText(LoginActivity.this, "Username field missing in Firestore profile.", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    // Fallback if user document does not exist
                                                    Toast.makeText(LoginActivity.this, "User profile not found in Firestore.", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                // Fallback if fetching document fails
                                                Toast.makeText(LoginActivity.this, "Failed to fetch user profile: " + userDocTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                            // --- Step 3: Save the fetched username to SharedPreferences ---
                                            SharedPreferences.Editor editorForUsername = sharedPreferences.edit();
                                            editorForUsername.putString(USERNAME_KEY, fetchedUsername);
                                            editorForUsername.apply(); // Apply these changes

                                            // --- Finally, proceed to MainActivity ---
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            finish(); // Finish LoginActivity
                                        });

                            } else {
                                // This case should ideally not happen after a successful signInWithEmailAndPassword
                                new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Oops")
                                        .setContentText("User not found after successful login. Please try again.")
                                        .show();
                            }

                        } else {
                            new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Oops")
                                    .setContentText("Login Failed. Please check your credentials.")
                                    .show();
                        }
                    }
                });
    }
}