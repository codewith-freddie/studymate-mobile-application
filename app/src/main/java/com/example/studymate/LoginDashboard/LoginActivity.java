package com.example.studymate.LoginDashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.studymate.AdminDashboard.AdminMainActivity;
import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.example.studymate.SignUpDashboard.SignUpActivity;
import com.example.studymate.UserDashboard.UserMainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    EditText editTextEmail, editTextPassword;
    FirebaseAuth mAuth;
    TextView textView;
    DatabaseReference databaseReference;
    ImageView passwordToggle;
    boolean isPasswordVisible = false;
    RelativeLayout buttonLayout;
    TextView buttonText;
    LottieAnimationView buttonAnimation;
    TextView forgotPassword;
    SignInButton googleBtn;
    GoogleSignInClient mGoogleSignInCLient;
    int RC_SIGN_IN = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // UI elements
        editTextEmail = findViewById(R.id.login_email);
        editTextPassword = findViewById(R.id.login_password);
        textView = findViewById(R.id.txtSignUp);
        passwordToggle = findViewById(R.id.password_toggle);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        buttonLayout = findViewById(R.id.button);
        buttonText = findViewById(R.id.button_text);
        buttonAnimation = findViewById(R.id.button_animation);
        forgotPassword = findViewById(R.id.forgotPassword);
        googleBtn = findViewById(R.id.googleBtn);

        // Click listener to navigate to SignUpActivity
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        // Check if user is already logged in and email is verified
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                // Redirect based on user role
//                checkUserRoleAndRedirect(currentUser.getUid());
            } else {
                // Email is not verified, show a message and log out
                Toast.makeText(this, "Please verify your email address.", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }
        }

        // Toggle password visibility
        passwordToggle.setOnClickListener(v -> togglePasswordVisibility());

        // Login button click listener
        buttonLayout.setOnClickListener(view -> {
            buttonEffectAppear();
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                showToast("Enter email");
                buttonEffectDisappear();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                showToast("Enter password");
                buttonEffectDisappear();
                return;
            }

            if (isOnline()) {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            buttonEffectDisappear();
                            if (task.isSuccessful()) {
                                FirebaseUser newUser = mAuth.getCurrentUser();
                                if (newUser != null) {
                                    if (newUser.isEmailVerified()) {
                                        databaseReference.child(newUser.getUid()).get().addOnCompleteListener(roleTask -> {
                                            if (roleTask.isSuccessful()) {
                                                DataSnapshot dataSnapshot = roleTask.getResult();
                                                if (dataSnapshot.exists()) {
                                                    String role = dataSnapshot.child("role").getValue(String.class);
                                                    if (role != null) {
                                                        cacheUserCredentials(email, newUser.getUid(), newUser.getUid(), role);
                                                        checkUserRoleAndRedirect(newUser.getUid());
                                                        Toast.makeText(LoginActivity.this, "Login Successfully.", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        showToast("User role not found");
                                                    }
                                                } else {
                                                    showToast("User data not found");
                                                }
                                            } else {
                                                showToast("Failed to fetch user role: " + roleTask.getException().getMessage());
                                            }
                                        });

                                    } else {
                                        Toast.makeText(LoginActivity.this, "Please verify your email address.", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut(); // Log out the user
                                    }
                                }
                            } else {
                                showToast("Incorrect Email or Password");
                            }
                        });
            } else {
                String[] cachedCredentials = getCachedUserCredentials();
                if (cachedCredentials[0] != null && cachedCredentials[1] != null) {
                    if (email.equals(cachedCredentials[0]) && password.equals(cachedCredentials[1])) {
                        // Retrieve the cached user role
                        SharedPreferences prefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
                        String cachedRole = prefs.getString("cached_role", null);

                        if (cachedRole != null) {
                            ToastUtils.showCustomToast(LoginActivity.this, "Offline Login Successful");
                            checkUserRoleAndRedirect(cachedRole);
                        } else {
                            showToast("No cached role found");
                        }
                    } else {
                        showToast("No Internet Connection");
                    }
                } else {
                    showToast("No cached credentials found. Please log in online first.");
                }
                buttonEffectDisappear();
            }
        });
        //inside on create this is google function
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail().build();

        mGoogleSignInCLient = GoogleSignIn.getClient(this, gso);

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                googleSignin();
            }
        });
        //forgot Password function here
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_pass, null);
                EditText emailBox = dialogView.findViewById(R.id.emailBox1);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.btnReset1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String userEmail = emailBox.getText().toString();

                        if (TextUtils.isEmpty(userEmail) || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                            Toast.makeText(LoginActivity.this, "Enter your registered email id", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss(); // Dismiss the initial dialog
                                if (task.isSuccessful()) {
                                    // Create and show the custom check email dialog
                                    AlertDialog.Builder checkEmailBuilder = new AlertDialog.Builder(LoginActivity.this);
                                    View checkEmailView = getLayoutInflater().inflate(R.layout.dialog_check_email, null);

                                    // Set up the custom dialog view
                                    checkEmailBuilder.setView(checkEmailView);
                                    AlertDialog checkEmailDialog = checkEmailBuilder.create();

                                    // Set up the Done button in dialog_check_email
                                    checkEmailView.findViewById(R.id.btnDone).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            checkEmailDialog.dismiss();
                                        }
                                    });

                                    if (checkEmailDialog.getWindow() != null) {
                                        checkEmailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                                    }

                                    checkEmailDialog.show(); // Show the custom dialog
                                } else {
                                    Toast.makeText(LoginActivity.this, "Unable to send, failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }

                dialog.show();
            }
        });

    }

    void googleSignin(){

        Intent intent = mGoogleSignInCLient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==RC_SIGN_IN){

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());

            }
            catch (Exception e){

                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Save user data to the database with detailed fields
                            HashMap<String, Object> userData = new HashMap<>();
                            userData.put("email", user.getEmail());
                            userData.put("firstName", user.getDisplayName().split(" ")[0]);  // Assuming first name
                            userData.put("lastName", user.getDisplayName().split(" ").length > 1
                                    ? user.getDisplayName().split(" ")[1] : ""); // Assuming last name or blank
                            userData.put("profileImageUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                            userData.put("role", "user");  // Default role, assuming it's a user

                            databaseReference.child(user.getUid()).setValue(userData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // Check the user's role and redirect accordingly
                                            checkUserRoleAndRedirect(user.getUid());
                                        }
                                    });
                        } else {
                            Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void buttonEffectAppear() {
        buttonAnimation.setVisibility(View.VISIBLE);
        buttonAnimation.playAnimation();
        buttonText.setVisibility(View.GONE);
    }

    private void buttonEffectDisappear() {
        buttonAnimation.pauseAnimation();
        buttonAnimation.setVisibility(View.GONE);
        buttonText.setVisibility(View.VISIBLE);
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24);
        } else {
            editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
            passwordToggle.setImageResource(R.drawable.baseline_visibility_24);
        }
        isPasswordVisible = !isPasswordVisible;
        editTextPassword.setSelection(editTextPassword.length());
    }

    private void checkUserRoleAndRedirect(String uid) {
        databaseReference.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.child("role").getValue(String.class);
                    if (role != null) {
                        Class<?> targetActivity = role.equals("admin") ? AdminMainActivity.class : UserMainActivity.class;
                        Intent intent = new Intent(getApplicationContext(), targetActivity);
                        startActivity(intent);
                        finish();
                    } else {
                        showToast("User role not found");
                    }
                } else {
                    showToast("User data not found");
                }
            } else {
                showToast("Failed to fetch user role: " + task.getException().getMessage());
            }
        });
    }

    private void showToast(String message) {
        View layout = getLayoutInflater().inflate(R.layout.custom_toast_layout, findViewById(R.id.custom_toast_container));
        TextView textView = layout.findViewById(R.id.toast_text);
        textView.setText(message);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void cacheUserCredentials(String email, String token, String userId, String role) {
        SharedPreferences prefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("cached_email", email);
        editor.putString("cached_token", token);
        editor.putString("cached_user_id", userId);
        editor.putString("cached_role", role);
        editor.apply();
    }


    private String[] getCachedUserCredentials() {
        SharedPreferences prefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
        String email = prefs.getString("cached_email", null);
        String token = prefs.getString("cached_token", null);
        return new String[]{email, token};
    }
}
