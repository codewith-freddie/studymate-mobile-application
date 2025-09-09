package com.example.studymate.Splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.example.studymate.AdminDashboard.AdminMainActivity;
import com.example.studymate.LoginDashboard.LoginActivity;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.UserMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends AppCompatActivity {

    private LottieAnimationView lottieAnimationView;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize animation views
        LottieAnimationView logoAnimationView = findViewById(R.id.lottieAnimationView);
        logoAnimationView.setAnimation(R.raw.stud_logo_anim);
        logoAnimationView.playAnimation();


        ImageView imageViewLogo = findViewById(R.id.imageViewLogo);

        // Fade in animation for logo after 500ms
        new Handler().postDelayed(() -> {
            imageViewLogo.setVisibility(View.VISIBLE);
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            imageViewLogo.startAnimation(fadeIn);
        }, 500);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Check connectivity and proceed accordingly
        new Handler().postDelayed(() -> {
            if (isOnline()) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null && currentUser.isEmailVerified()) {
                    checkUserRoleAndRedirect(currentUser.getUid());
                } else if (currentUser != null) {
                    mAuth.signOut();
                    showToast("Please verify your email address.");
                    redirectToLogin();
                } else {
                    redirectToLogin();
                }
            } else {
                handleOfflineScenario();
            }
        }, 2090);
    }

    private void handleOfflineScenario() {
        SharedPreferences prefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
        String email = prefs.getString("cached_email", null);
        String token = prefs.getString("cached_token", null);
        String userId = prefs.getString("cached_user_id", null);
        String role = prefs.getString("cached_role", null);

        if (email != null && token != null && userId != null && role != null) {
            showToast("Offline login successful");

            // Redirect based on the cached role
            redirectBasedOnRole(role);
        } else {
            showToast("No cached credentials found. Please log in online first.");
            redirectToLogin(); // Redirect to login activity
        }
    }

    private void redirectBasedOnRole(String role) {
        Class<?> targetActivity = role.equals("admin") ? AdminMainActivity.class : UserMainActivity.class;
        Intent intent = new Intent(SplashActivity.this, targetActivity);
        startActivity(intent);
        finish();
    }

    private void checkUserRoleAndRedirect(String uid) {
        databaseReference.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.child("role").getValue(String.class);
                    if (role != null) {
                        redirectBasedOnRole(role);
                    } else {
                        showToast("User role not found");
                        redirectToLogin();
                    }
                } else {
                    showToast("User data not found");
                    redirectToLogin();
                }
            } else {
                showToast("Failed to fetch user role: " + task.getException().getMessage());
                redirectToLogin();
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(SplashActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
