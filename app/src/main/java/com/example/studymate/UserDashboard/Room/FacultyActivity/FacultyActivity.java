package com.example.studymate.UserDashboard.Room.FacultyActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.example.studymate.Dialog.AnnouncementHandler;
import com.example.studymate.Dialog.LinkHandler;
import com.example.studymate.LoginDashboard.LoginActivity;
import com.example.studymate.Network.NetworkUtils;
import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.HomeAdapter;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.PdfInfo;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.SyllabusInfo;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Quiz.CreateQuizActivity;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Quiz.Quiz;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Quiz.QuizQuestion;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Quizzes.FacultyQuizActivity;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Quizzes.FacultyQuizzesFragment;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Settings.CreatorSettingsFragment;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.HomeFragment;
import com.example.studymate.UserDashboard.Room.FacultyActivity.People.PeopleFragment;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Quiz.QuizFragment;
import com.example.studymate.UserDashboard.UserMainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FacultyActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DrawerLayout drawerLayout;
    private Context context;
    private ImageView imageViewUploadStatus;
    private Dialog uploadDialog;
    private String moduleId;
    private String roomId;
    private DatabaseReference syllabusRef;
    private HomeAdapter homeAdapter;
    FloatingActionButton fab;
    BottomNavigationView bottomNavigationView;

    private static final int PICK_PDF_REQUEST = 1;
    private Uri pdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faculty_activity);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
        drawerLayout = findViewById(R.id.drawer_layout);
        syllabusRef = FirebaseDatabase.getInstance().getReference("Syllabus");


        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        roomId = getIntent().getStringExtra("ROOM_ID");


        if (savedInstanceState == null) {
            String roomId = getIntent().getStringExtra("ROOM_ID");
            if (roomId != null) {
                replaceFragment(HomeFragment.newInstance(roomId)); // Pass roomID to ModuleFragment
            } else {
                ToastUtils.showCustomToast(FacultyActivity.this, "No room ID provided");
                Intent intent = new Intent(FacultyActivity.this, UserMainActivity.class);
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
                    replaceFragment(HomeFragment.newInstance(roomId));
                }
            } else if (itemId == R.id.settings) {
                String roomId = getIntent().getStringExtra("ROOM_ID");
                if (roomId != null) {
                    replaceFragment(CreatorSettingsFragment.newInstance(roomId));
                }
            } else if (itemId == R.id.quiz) {
                String roomId = getIntent().getStringExtra("ROOM_ID");
                if (roomId != null) {
                    replaceFragment(FacultyQuizzesFragment.newInstance(roomId));
                }
            } else if (itemId == R.id.people) {
                String roomId = getIntent().getStringExtra("ROOM_ID");
                if (roomId != null) {
                    replaceFragment(PeopleFragment.newInstance(roomId));
                }

            }

            return true;
        });

        fab.setOnClickListener(view -> showBottomDialog());
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

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout_home);

        LinearLayout layoutAnnouncement = dialog.findViewById(R.id.layoutAnnouncement);
        LinearLayout layoutSyllabus = dialog.findViewById(R.id.layoutSyllabus);
        LinearLayout layoutUpload = dialog.findViewById(R.id.layoutUpload);
        LinearLayout layoutLink = dialog.findViewById(R.id.layoutLink);
        LinearLayout layoutCreate = dialog.findViewById(R.id.layoutCreate);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        layoutAnnouncement.setOnClickListener(v -> {
            if (roomId != null) { // Check if roomId is available
                AnnouncementHandler handler = new AnnouncementHandler(this, roomId); // Use roomId here
                handler.handleAnnouncementClick(dialog);
            } else {
                ToastUtils.showCustomToast(this, "Room ID is missing");
            }
        });

        layoutSyllabus.setOnClickListener(v -> {
            if (NetworkUtils.isConnected(this)) {
                showUploadDialogForSyllabus();  // Open upload dialog for syllabus
            } else {
                ToastUtils.showCustomToast(this, "No Internet Connection");
            }
            dialog.dismiss();
        });

        layoutUpload.setOnClickListener(v -> {
            if (NetworkUtils.isConnected(this)) {
                showUploadDialog();
            } else {
                ToastUtils.showCustomToast(this, "No Internet Connection");
            }
            dialog.dismiss();
        });

        layoutLink.setOnClickListener(v -> {
            if (roomId != null) { // Check if roomId is available
                LinkHandler handler = new LinkHandler(this, roomId); // Use roomId here
                handler.handleLinkClick(dialog);
            } else {
                ToastUtils.showCustomToast(this, "Room ID is missing");
            }
        });

        layoutCreate.setOnClickListener(v -> {
            if (NetworkUtils.isConnected(this)) {
                showCreateQuizDialog();
            } else {
                ToastUtils.showCustomToast(this, "No Internet Connection");
            }
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    private void showUploadDialog() {
        uploadDialog = new Dialog(this);
        uploadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        uploadDialog.setContentView(R.layout.dialog_upload);

        EditText editTextModuleTitle = uploadDialog.findViewById(R.id.uploadModuleTitle);
        imageViewUploadStatus = uploadDialog.findViewById(R.id.imageViewUpload);
        Button buttonUpload = uploadDialog.findViewById(R.id.uploadModuleButton);

        // Cancel Button
        TextView cancelButton = uploadDialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> uploadDialog.dismiss());

        final String currentDateTime = String.valueOf(java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));

        imageViewUploadStatus.setOnClickListener(v -> choosePdf());

        buttonUpload.setOnClickListener(v -> {
            String moduleTitle = editTextModuleTitle.getText().toString().trim();
            if (moduleTitle.isEmpty() || pdfUri == null) {
                Toast.makeText(FacultyActivity.this, "Please enter module title and select a PDF", Toast.LENGTH_SHORT).show();
                return;
            }
            // Generate a unique ID for syllabus
            String moduleId = UUID.randomUUID().toString();

            uploadPdfToFirebase(moduleId, moduleTitle, currentDateTime);
            uploadDialog.dismiss();
        });

        uploadDialog.show();
        uploadDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        uploadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void showCreateQuizDialog() {
        final Dialog createQuizDialog = new Dialog(this);
        createQuizDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        createQuizDialog.setContentView(R.layout.dialog_create_quiz);

        final String currentDateTime = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

        EditText quizTitle = createQuizDialog.findViewById(R.id.quiz_title);
        EditText quizDuration = createQuizDialog.findViewById(R.id.quiz_duration); // EditText for duration
        Button createQuiz = createQuizDialog.findViewById(R.id.createQuizButton);

//        LottieAnimationView animationView = findViewById(R.id.lottie);
//        animationView.setAnimation(R.raw.create_quiz);  // Use a valid animation file
//        animationView.playAnimation();

        // Cancel Button
        TextView cancelButton = createQuizDialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> createQuizDialog.dismiss());

        createQuiz.setOnClickListener(v -> {
            String quizTitleStr = quizTitle.getText().toString();
            String quizDurationStr = quizDuration.getText().toString();

            // Validate quiz title
            if (quizTitleStr.isEmpty()) {
                quizTitle.setError("Quiz title cannot be empty");
                Toast.makeText(this, "Quiz Title is Empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate quiz duration
            if (quizDurationStr.isEmpty()) {
                quizDuration.setError("Duration cannot be empty");
                Toast.makeText(this, "Quiz Duration is Empty", Toast.LENGTH_SHORT).show();
                return;
            }

            int duration;
            try {
                duration = Integer.parseInt(quizDurationStr);
                if (duration <= 0) {
                    quizDuration.setError("Duration must be a positive number");
                    Toast.makeText(this, "Invalid Duration", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                quizDuration.setError("Invalid duration format");
                Toast.makeText(this, "Invalid Duration", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate a unique quiz ID
            String quizID = UUID.randomUUID().toString();

            // Clear input fields and dismiss dialog
            quizTitle.setText("");
            quizDuration.setText("");
            createQuizDialog.dismiss();

            // Prepare to pass the room ID and quiz data to CreateQuizActivity
            Intent intent = new Intent(this, CreateQuizActivity.class);
            intent.putExtra("RoomID", roomId); // Pass the room ID
            intent.putExtra("QuizID", quizID); // Pass the quiz ID
            intent.putExtra("QuizTitle", quizTitleStr); // Pass the quiz title
            intent.putExtra("QuizDuration", duration); // Pass the quiz duration
            intent.putExtra("QuizDateTime", currentDateTime); // Pass the date and time

            startActivity(intent);
            Toast.makeText(this, "Quiz created successfully", Toast.LENGTH_SHORT).show();
        });

        createQuizDialog.show();
        createQuizDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        createQuizDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
                // Remove the .pdf extension if present
                String displayName = removePdfExtension(fileName);

                EditText editTextModuleTitle = uploadDialog.findViewById(R.id.uploadModuleTitle);
                editTextModuleTitle.setText(displayName);  // Set file name without extension to EditText

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
                String[] projection = {android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME};
                Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME);
                        if (nameIndex != -1) {
                            fileName = cursor.getString(nameIndex);
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    // Helper method to remove the .pdf extension
    private String removePdfExtension(String fileName) {
        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            // Remove the last 4 characters if they are ".pdf"
            return fileName.substring(0, fileName.length() - 4);
        }
        return fileName;
    }

    private void uploadPdfToFirebase(String moduleId, String moduleTitle, String currentDateTime) {
        if (pdfUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Generate a unique file name using the current timestamp
            String fileName = System.currentTimeMillis() + ".pdf";
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Home/" + fileName);

            storageReference.putFile(pdfUri)
                    .addOnSuccessListener(taskSnapshot ->
                            // Get the download URL after successful upload
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();
                                // Save PDF info to the database
                                savePdfInfoToDatabase(moduleId, moduleTitle, downloadUrl, currentDateTime);
                                progressDialog.dismiss();
                                Toast.makeText(FacultyActivity.this, "PDF uploaded successfully", Toast.LENGTH_SHORT).show();
                                // Optionally update module count or other related info
                                updateModulesCountInDatabase();
                            })
                    )
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(FacultyActivity.this, "Failed to upload PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploaded " + (int) progress + "%...");
                    });
        } else {
            Toast.makeText(FacultyActivity.this, "No PDF selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateModulesCountInDatabase() {
        DatabaseReference analyticsRef = FirebaseDatabase.getInstance().getReference("Analytics").child("moduleCount");
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

    private void savePdfInfoToDatabase(String moduleId, String moduleTitle, String downloadUrl, String currentDateTime) {
        String roomID = getIntent().getStringExtra("ROOM_ID");

        if (roomID == null || roomID.isEmpty()) {
            Toast.makeText(FacultyActivity.this, "Room ID is missing. Cannot save PDF info.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (moduleId == null || moduleId.isEmpty()) {
            Toast.makeText(FacultyActivity.this, "Module ID is missing. Cannot save PDF info.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Home").child(roomID);

        // Get current timestamp
        long timestamp = System.currentTimeMillis();

        // Create a map to hold the PDF information
        Map<String, Object> pdfInfoMap = new HashMap<>();
        pdfInfoMap.put("moduleId", moduleId);
        pdfInfoMap.put("title", moduleTitle);
        pdfInfoMap.put("url", downloadUrl);
        pdfInfoMap.put("dateTime", currentDateTime);
        pdfInfoMap.put("countView", 0);
        pdfInfoMap.put("downloadCount", 0);
        pdfInfoMap.put("type", "pdf"); // Adjust type if needed
        pdfInfoMap.put("timestamp", timestamp);

        // Create the PdfInfo object from the map
        PdfInfo pdfInfo = new PdfInfo(moduleId, moduleTitle, downloadUrl, currentDateTime, 0, 0, "pdf", timestamp);

        // Update or set the PDF info in the database
        databaseReference.child(moduleId).updateChildren(pdfInfoMap)
                .addOnSuccessListener(aVoid -> {;
                    Toast.makeText(FacultyActivity.this, "PDF info saved successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FacultyActivity.this, "Failed to save PDF info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void showUploadDialogForSyllabus() {
        uploadDialog = new Dialog(this);
        uploadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        uploadDialog.setContentView(R.layout.dialog_upload_syllabus);  // Reuse the same layout

        // Change the title of the dialog
        TextView textViewDialogTitle = uploadDialog.findViewById(R.id.uploadTitle);
        textViewDialogTitle.setText("Upload Syllabus");

        // Cancel Button
        TextView cancelButton = uploadDialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> uploadDialog.dismiss());

        EditText editTextSyllabusTitle = uploadDialog.findViewById(R.id.uploadModuleTitle);
        imageViewUploadStatus = uploadDialog.findViewById(R.id.imageViewUpload);
        Button buttonUpload = uploadDialog.findViewById(R.id.uploadModuleButton);

        final String currentDateTime = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

        imageViewUploadStatus.setOnClickListener(v -> {
            choosePdf();
            if (pdfUri != null) {
                String fileName = getFileNameFromUri(pdfUri);
                editTextSyllabusTitle.setText(fileName);  // Optionally set file name in title
            }
        });

        buttonUpload.setOnClickListener(v -> {
            String syllabusTitle = editTextSyllabusTitle.getText().toString().trim();
            if (syllabusTitle.isEmpty() || pdfUri == null) {
                Toast.makeText(FacultyActivity.this, "Please enter syllabus title and select a PDF", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate a unique ID for syllabus
            String syllabusId = UUID.randomUUID().toString();

            uploadSyllabusToFirebase(syllabusId, syllabusTitle, currentDateTime);  // Call the syllabus upload method
            uploadDialog.dismiss();
        });

        uploadDialog.show();
        uploadDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        uploadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


    private void uploadSyllabusToFirebase(String syllabusId, String title, String dateTime) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Syllabus...");
        progressDialog.show();

        // Get the room ID passed through the intent
        String roomID = getIntent().getStringExtra("ROOM_ID");

        // Ensure roomID is not null
        if (roomID == null || roomID.isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(FacultyActivity.this, "Invalid room ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get a reference to store syllabus PDFs in Firebase Storage, ensuring there's a slash between roomID and syllabusId
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Home/" + roomID + "/" + syllabusId + ".pdf");

        // Upload the file to Firebase Storage
        storageReference.putFile(pdfUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploaded " + (int) progress + "%...");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Create a map for syllabus info with a fixed type
                        Map<String, Object> syllabusInfo = new HashMap<>();
                        syllabusInfo.put("syllabusId", syllabusId);
                        syllabusInfo.put("title", title);
                        syllabusInfo.put("url", uri.toString());
                        syllabusInfo.put("dateTime", dateTime);
                        syllabusInfo.put("downloadCount", 0); // Assuming initial download count is 0
                        syllabusInfo.put("timestamp", System.currentTimeMillis());
                        syllabusInfo.put("type", "syllabus");

                        // Save syllabus info in Firebase Realtime Database
                        DatabaseReference syllabusRef = FirebaseDatabase.getInstance().getReference("Home");
                        syllabusRef.child(roomID).child(syllabusId).setValue(syllabusInfo)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(FacultyActivity.this, "Syllabus uploaded successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(FacultyActivity.this, "Failed to upload syllabus", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(FacultyActivity.this, "Failed to upload file", Toast.LENGTH_SHORT).show();
                });
    }
}