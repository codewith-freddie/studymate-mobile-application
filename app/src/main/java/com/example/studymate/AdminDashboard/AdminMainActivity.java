package com.example.studymate.AdminDashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.studymate.AdminDashboard.AdminHome.AdminHomeActivity;
import com.example.studymate.AdminDashboard.AdminHome.Analytics.AdminAnalyticsFragment;
import com.example.studymate.AdminDashboard.AdminHome.People.AdminPeopleFragment;
import com.example.studymate.AdminDashboard.AdminHome.Rooms.AdminRoomsFragment;
import com.example.studymate.AdminDashboard.AdminHome.Subscription.AdminSubscriptionFragment;
import com.example.studymate.LoginDashboard.LoginActivity;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.About.AboutFragment;
import com.example.studymate.UserDashboard.Profile.ProfileActivity;
import com.example.studymate.UserDashboard.Room.RoomFragment;
import com.example.studymate.UserDashboard.Settings.SettingsFragment;
import com.example.studymate.UserDashboard.Share.ShareFragment;
import com.example.studymate.UserDashboard.Subscription.SubscriptionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

public class AdminMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "AdminMainActivity";
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DrawerLayout drawerLayout;
    private Context context;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        drawerLayout = findViewById(R.id.drawer_layout);

        loadCachedUserData();
        loadUserProfile();

        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            NavigationView navigationView = findViewById(R.id.nav_view);
            View headerView = navigationView.getHeaderView(0);

            TextView userEmailTextView = headerView.findViewById(R.id.currentUser_email);
            userEmailTextView.setText(user.getEmail());

            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                        String lastName = dataSnapshot.child("lastName").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);

                        TextView userNameTextView = headerView.findViewById(R.id.currentUser_name);
                        if (firstName != null && lastName != null) {
                            userNameTextView.setText(firstName + " " + lastName);
                        } else {
                            userNameTextView.setText(email);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle potential errors here
                }
            });

            context = this;
            bottomNavigationView.setBackground(null);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                Log.d(TAG, "BottomNavigationView item selected: " + itemId);

                if (itemId == R.id.analytics) {
                    replaceFragment(new AdminAnalyticsFragment());
                } else if (itemId == R.id.rooms) {
                    replaceFragment(new AdminRoomsFragment());
                } else if (itemId == R.id.people) {
                    replaceFragment(new AdminPeopleFragment());
                }

                return true;
            });

        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdminAnalyticsFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "Navigation item selected: " + id);

        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RoomFragment()).commit();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(AdminMainActivity.this, ProfileActivity.class);
            startActivity(intent); // Start the ProfileActivity
        } else if (id == R.id.nav_share) {
            shareApp();
//            shareApk();
        } else if (id == R.id.nav_about) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
        } else if (id == R.id.nav_logout) {
            showToast("Logout Successfully");
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true; // Ensure this returns true
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


//    // Method to share the APK
//    private void shareApk() {
//        // Specify the APK file path
//        File apkFile = new File(getExternalFilesDir(null), "Studymate.apk"); // Replace with your APK file name
//
//        if (apkFile.exists()) {
//            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//            shareIntent.setType("application/vnd.android.package-archive");
//
//            // Get the URI for the APK file
//            Uri apkUri = FileProvider.getUriForFile(this, "com.example.studymate.fileprovider", apkFile);
//
//            shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
//            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            // Start the share activity
//            startActivity(Intent.createChooser(shareIntent, "Share APK via"));
//        } else {
//            showToast("APK file not found");
//        }
//    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
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

    private void loadUserProfile() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                ImageView profileImageView = headerView.findViewById(R.id.profile);

                if (profileImageView != null) {
                    if (user != null) {
                        String profileImageUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
                        Log.d(TAG, "Profile Image URL: " + profileImageUrl);

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(profileImageUrl)
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.placeholder)  // Replace with your placeholder image
                                            .error(R.drawable.profile_male_no1)  // Replace with your error image
                                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                                    .into(profileImageView);
                        } else {
                            Log.e(TAG, "Profile image URL is null or empty.");
                            // Set a default or placeholder image if URL is not available
                            profileImageView.setImageResource(R.drawable.profile_male_no1);
                        }

                        profileImageView.setOnClickListener(v -> openProfileActivity());
                    } else {
                        Log.e(TAG, "FirebaseUser object is null.");
                    }
                } else {
                    Log.e(TAG, "ImageView with ID profile_image not found in headerView");
                }
            } else {
                Log.e(TAG, "HeaderView is null");
            }
        } else {
            Log.e(TAG, "NavigationView is null");
        }
    }


    private void openProfileActivity() {
        Intent intent = new Intent(AdminMainActivity.this, ProfileActivity.class);
        startActivity(intent);
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
