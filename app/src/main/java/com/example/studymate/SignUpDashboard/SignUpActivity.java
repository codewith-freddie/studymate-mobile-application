package com.example.studymate.SignUpDashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studymate.LoginDashboard.LoginActivity;
import com.example.studymate.LoginDashboard.UserRole;
import com.example.studymate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class SignUpActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, editTextFirstName, editTextLastName;
    RelativeLayout buttonReg;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    ImageView passwordToggle;
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.signup_email);
        editTextPassword = findViewById(R.id.signup_password);
        editTextFirstName = findViewById(R.id.signup_first_name);
        editTextLastName = findViewById(R.id.signup_last_name);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        passwordToggle = findViewById(R.id.password_toggle);
        buttonReg = findViewById(R.id.button);

        findViewById(R.id.loginNow).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
        togglePasswordVisibility();

        buttonReg.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String firstName = editTextFirstName.getText().toString();
            String lastName = editTextLastName.getText().toString();
            String role = "user";

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(SignUpActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(SignUpActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(SignUpActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(firstName)) {
                Toast.makeText(SignUpActivity.this, "Enter first name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(lastName)) {
                Toast.makeText(SignUpActivity.this, "Enter last name", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(email, password, firstName, lastName, role);
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24);
            editTextPassword.setInputType(129); // Password hidden
        } else {
            passwordToggle.setImageResource(R.drawable.baseline_visibility_24);
            editTextPassword.setInputType(1); // Password visible
        }
        isPasswordVisible = !isPasswordVisible;
    }

    private void registerUser(String email, String password, String firstName, String lastName, String role) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Registration was successful
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Send verification email
                    user.sendEmailVerification().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            String userId = user.getUid();
                            UserRole userRole = new UserRole(firstName, lastName, email, role);
                            databaseReference.child(userId).setValue(userRole).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this, "User registered successfully. Verification email sent.", Toast.LENGTH_SHORT).show();
                                    updateRegisteredCountInDatabase();
                                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(SignUpActivity.this, "Failed to register user details. Try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Failed to send verification email. Try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // Handle registration failure
                String errorMessage = "Authentication failed.";
                if (task.getException() != null) {
                    if (task.getException().getMessage().contains("email address is already in use")) {
                        errorMessage = "Email is already in use. Please use a different email.";
                    }
                }
                Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRegisteredCountInDatabase() {
        DatabaseReference analyticsRef = FirebaseDatabase.getInstance().getReference("Analytics").child("registeredCount");
        analyticsRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentCount = currentData.getValue(Integer.class);
                if (currentCount == null) {
                    currentData.setValue(1);  // Initialize with 1 if modulesCount doesn't exist
                } else {
                    currentData.setValue(currentCount + 1);  // Increment by 1
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e("FirebaseDatabase", "Failed to update modulesCount: " + error.getMessage());
                } else {
                    Log.d("FirebaseDatabase", "modulesCount updated successfully.");
                }
            }
        });
    }


}
