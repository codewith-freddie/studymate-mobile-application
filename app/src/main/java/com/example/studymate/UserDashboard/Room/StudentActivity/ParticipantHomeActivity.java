package com.example.studymate.UserDashboard.Room.StudentActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.studymate.LoginDashboard.LoginActivity;
import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.StudentActivity.Home.StudentHomeFragment;
import com.example.studymate.UserDashboard.Room.StudentActivity.People.StudentPeopleFragment;
import com.example.studymate.UserDashboard.Room.StudentActivity.Quizzes.QuizzesFragment;
import com.example.studymate.UserDashboard.UserMainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ParticipantHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DrawerLayout drawerLayout;
    private Context context;
    private ImageView imageViewUploadStatus;
    private Dialog uploadDialog;
    BottomNavigationView bottomNavigationView;

    private static final int PICK_PDF_REQUEST = 1;
    private Uri pdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.participant_activity_home);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        drawerLayout = findViewById(R.id.drawer_layout);

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (savedInstanceState == null) {
            String roomId = getIntent().getStringExtra("ROOM_ID");
            if (roomId != null) {
                replaceFragment(StudentHomeFragment.newInstance(roomId));
            } else {
                ToastUtils.showCustomToast(ParticipantHomeActivity.this, "No room ID provided");
                Intent intent = new Intent(ParticipantHomeActivity.this, UserMainActivity.class);
                finish();
            }
        }

        context = this;

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                String roomId = getIntent().getStringExtra("ROOM_ID");
                if (roomId != null) {
                    replaceFragment(StudentHomeFragment.newInstance(roomId));
                }
            } else if (itemId == R.id.quiz) {
                String roomId = getIntent().getStringExtra("ROOM_ID");
                if (roomId != null) {
                    replaceFragment(QuizzesFragment.newInstance(roomId));
                }
            } else if (itemId == R.id.settings) {
                String roomId = getIntent().getStringExtra("ROOM_ID");
                if (roomId != null) {
                    replaceFragment(StudentPeopleFragment.newInstance(roomId));
                }
            }

            return true;
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

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



    private void choosePdf() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            pdfUri = data.getData();

            // Extract file name from Uri
            String fileName = getFileNameFromUri(pdfUri);
            if (fileName != null && uploadDialog != null) {
                EditText editTextModuleTitle = uploadDialog.findViewById(R.id.uploadModuleTitle);
                editTextModuleTitle.setText(fileName);  // Set file name to EditText

                if (imageViewUploadStatus != null) {
                    imageViewUploadStatus.setImageResource(R.drawable.baseline_file_download_done_24);  // Update ImageView drawable
                }
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri != null) {
            try {
                String[] projection = { android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME };
                Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(projection[0]);
                        fileName = cursor.getString(nameIndex);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }




}
