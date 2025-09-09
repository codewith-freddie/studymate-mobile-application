package com.example.studymate.UserDashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.studymate.AdminDashboard.AdminMainActivity;
import com.example.studymate.LoginDashboard.LoginActivity;
import com.example.studymate.OfflineModules.PdfListActivity;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.About.AboutFragment;
import com.example.studymate.UserDashboard.Profile.ProfileActivity;
import com.example.studymate.UserDashboard.Room.RoomFragment;
import com.example.studymate.UserDashboard.Settings.SettingsFragment;
import com.example.studymate.UserDashboard.Share.ShareFragment;
import com.example.studymate.UserDashboard.Subscription.SubscriptionFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DrawerLayout drawerLayout;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            // Redirect to login if user is not authenticated
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            initializeUI();

            if (savedInstanceState == null) {
                // Set default fragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RoomFragment())
                        .commit();
            }
        }

        context = this;
    }

    private void initializeUI() {
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up drawer layout and navigation view
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set the default checked item
        navigationView.setCheckedItem(R.id.nav_home);

        // Load user data
        loadCachedUserData();
        loadUserProfile();
    }

    private void loadUserProfile() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    // Update UI with fetched data
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    View headerView = navigationView.getHeaderView(0);

                    TextView userNameTextView = headerView.findViewById(R.id.currentUser_name);
                    if (firstName != null && lastName != null) {
                        userNameTextView.setText(firstName + " " + lastName);
                    } else {
                        userNameTextView.setText(email);
                    }

                    TextView userEmail = headerView.findViewById(R.id.currentUser_email);
                    userEmail.setText(email);

                    ImageView profileImageView = headerView.findViewById(R.id.profile);
                    if (profileImageUrl != null) {
                        Glide.with(UserMainActivity.this)
                                .load(profileImageUrl)
                                .apply(new RequestOptions().circleCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true))
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.profile)
                                .into(profileImageView);

                    } else {
                        profileImageView.setImageResource(R.drawable.placeholder);
                    }

                    // Set click listener for the profile image
                    profileImageView.setOnClickListener(v -> openProfileActivity());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void openProfileActivity() {
        Intent intent = new Intent(UserMainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RoomFragment()).commit();
        } else if (id == R.id.nav_offlineModules) {
            Intent intent = new Intent(this, PdfListActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(UserMainActivity.this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_about) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
        } else if (id == R.id.nav_logout) {
            showToast("Logout Successfully");
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // Replace the URL with your app's Play Store link
        String appLink = "https://play.google.com/store/apps/details?id=" + getPackageName();
        String shareText = "Check out this amazing app: " + appLink;

        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share App via"));
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (!(currentFragment instanceof RoomFragment)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RoomFragment())
                        .commit();
            } else {
                super.onBackPressed();
            }
        }
    }

    private void logout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Sign out from Google
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // After sign-out, navigate to LoginActivity
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
    }

    private void showToast(String message) {
        View layout = getLayoutInflater().inflate(R.layout.custom_toast_layout, findViewById(R.id.custom_toast_container));

        ImageView imageView = layout.findViewById(R.id.toast_icon);
        imageView.setImageResource(R.drawable.baseline_info_24);

        TextView textView = layout.findViewById(R.id.toast_text);
        textView.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 50);
        toast.show();
    }


    private void loadCachedUserData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String cachedFirstName = prefs.getString("cached_first_name", null);
        String cachedLastName = prefs.getString("cached_last_name", null);
        String cachedEmail = prefs.getString("cached_email", null);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView userNameTextView = headerView.findViewById(R.id.currentUser_name);
        TextView userEmailTextView = headerView.findViewById(R.id.currentUser_email);

        if (cachedFirstName != null && cachedLastName != null) {
            userNameTextView.setText(cachedFirstName + " " + cachedLastName);
        }
        if (cachedEmail != null) {
            userEmailTextView.setText(cachedEmail);
        }
    }
}
