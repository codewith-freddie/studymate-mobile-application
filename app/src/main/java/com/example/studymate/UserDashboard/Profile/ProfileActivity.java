package com.example.studymate.UserDashboard.Profile;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.studymate.AdminDashboard.AdminHome.AdminHomeActivity;
import com.example.studymate.AdminDashboard.AdminMainActivity;
import com.example.studymate.Network.NetworkUtils;
import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.UserMainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.text.BreakIterator;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    public static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView profileImageView;
    private TextView fullNameTextView;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        LinearLayout editProfileLayout = findViewById(R.id.editProfileLayout);
        LinearLayout changePasswordLayout = findViewById(R.id.changePasswordLayout);


        // Initialize Firebase references
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures");
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        profileImageView = findViewById(R.id.profile_image);
        fullNameTextView = findViewById(R.id.profile_name);  // Reference to the TextView

        // Load the current user's profile picture
        loadProfileImage();

        // Load the current user's full name
        loadUserName();

        if (NetworkUtils.isConnected(this)) {
            // Set up click listener to change profile picture
            profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFileChooser();
                }
            });

            // Set OnClickListener for the Edit Profile option
            editProfileLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditProfileDialog();
                }
            });

            // Set OnClickListener for the Change Password option
            changePasswordLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangePasswordDialog();
                }
            });
        } else {
            // Handle no internet connection
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
        }


        ImageButton backButton = findViewById(R.id.back_button);

        // Set an OnClickListener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fetch user role
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                    userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String role = dataSnapshot.getValue(String.class);

                            Intent intent;
                            if ("admin".equals(role)) {
                                // User is an admin
                                intent = new Intent(ProfileActivity.this, AdminMainActivity.class);
                                intent.putExtra("openFragment", "AdminAnalyticsFragment");
                            } else {
                                // User is not an admin
                                intent = new Intent(ProfileActivity.this, UserMainActivity.class);
                                intent.putExtra("openFragment", "RoomFragment");
                            }
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle potential errors here
                        }
                    });
                }
            }
        });


    }

    private void loadProfileImage() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef.child(userID).child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String imageUrl = snapshot.getValue(String.class);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Use Glide to load the image with CircleCrop transformation
                    Glide.with(ProfileActivity.this)
                            .load(imageUrl)
                            .apply(new RequestOptions().circleCrop())
                            .into(profileImageView);
                } else {
                    // Set a default image or placeholder if no profile image is available
                    profileImageView.setImageResource(R.drawable.placeholder); // Replace with your default image
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
                Toast.makeText(ProfileActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserName() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                if (firstName != null && lastName != null) {
                    String fullName = firstName + " " + lastName;
                    fullNameTextView.setText(fullName);
                } else {
                    fullNameTextView.setText("User Name");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
                Toast.makeText(ProfileActivity.this, "Failed to load user name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        if (NetworkUtils.isConnected(this)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);

            // Display the selected image in the ImageView with CircleCrop transformation
            Glide.with(this)
                    .load(imageUri)
                    .apply(new RequestOptions().circleCrop())
                    .into(profileImageView);

            // Check internet connection before uploading
            if (NetworkUtils.isConnected(this)) {
                // Save to Firebase Storage and Database
                uploadImageToFirebase();
            } else {
                Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null && NetworkUtils.isConnected(this)) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference fileReference = storageRef.child(userID + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Update the profileImageUrl field in the database with the new image URL
                        databaseRef.child(userID).child("profileImageUrl").setValue(uri.toString());
                        Toast.makeText(ProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> {
                        // Handle upload error
                        Toast.makeText(ProfileActivity.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showEditProfileDialog() {
        if (NetworkUtils.isConnected(this)){
            // Create an AlertDialog builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // Inflate the dialog_edit_profile layout
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
            builder.setView(dialogView);

            // Find the EditTexts and Button in the dialog
            EditText editFirstName = dialogView.findViewById(R.id.editFirstname);
            EditText editLastName = dialogView.findViewById(R.id.editLastname);
            Button updateProfileButton = dialogView.findViewById(R.id.updateProfileButton);

            // Create and show the dialog
            AlertDialog dialog = builder.create();
            dialog.show();

            // Set the window properties for the dialog
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Set a click listener for the Update button
            updateProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle the update profile action
                    String firstName = editFirstName.getText().toString().trim();
                    String lastName = editLastName.getText().toString().trim();

                    if (!firstName.isEmpty() && !lastName.isEmpty()) {
                        // Update in the database
                        updateUserProfile(firstName, lastName);
                        dialog.dismiss(); // Dismiss the dialog after updating
                    } else {
                        Toast.makeText(ProfileActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
        }

    }


    private void updateUserProfile(String firstName, String lastName) {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a map to hold the new values
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);

        // Update the user's profile in the database
        databaseRef.child(userID).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                // Update the displayed name in the profile
                fullNameTextView.setText(firstName + " " + lastName);
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the dialog_change_password layout
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        // Find the EditTexts, ImageViews, TextView, and Button in the dialog
        EditText changePassword = dialogView.findViewById(R.id.newPassword);
        EditText confirmPassword = dialogView.findViewById(R.id.confrimPassword);
        EditText currentPassword = dialogView.findViewById(R.id.currentPassword);
        ImageView passwordToggle = dialogView.findViewById(R.id.password_toggle);
        ImageView confirmPasswordToggle = dialogView.findViewById(R.id.confirm_password_toggle);
        ImageView currentPasswordToggle = dialogView.findViewById(R.id.current_password_toggle);
        TextView verifyNotif = dialogView.findViewById(R.id.verifyNotif);
        LinearLayout currentPasswordLayout = dialogView.findViewById(R.id.currentPasswordLayout);
        Button updatePasswordButton = dialogView.findViewById(R.id.updatePasswordButton);
        TextView errorNotif = dialogView.findViewById(R.id.errorNotif);
        TextView currentPassNotif = dialogView.findViewById(R.id.currentPassNotif);
        TextView passwordCharactersCount = dialogView.findViewById(R.id.passwordCharactersCount);

        // Create and show the dialog first
        AlertDialog dialog = builder.create();
        dialog.show();

        // Toggle password visibility logic
        togglePasswordVisibility(passwordToggle, changePassword);
        togglePasswordVisibility(confirmPasswordToggle, confirmPassword);
        togglePasswordVisibility(currentPasswordToggle, currentPassword);

        // Add TextWatcher to change the color back to default as the user types
        changePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changePassword.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.dark_grey));
                confirmPassword.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.dark_grey));
                errorNotif.setVisibility(View.GONE); // Hide the error notification when typing
                passwordCharactersCount.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

                String newPass = changePassword.getText().toString().trim();
                if (changePassword.length() >= 6) {
                    passwordCharactersCount.setVisibility(View.GONE);
                } else {
                    passwordCharactersCount.setVisibility(View.VISIBLE);
                }
            }
        });

        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmPassword.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.dark_grey));
                changePassword.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.dark_grey));
                errorNotif.setVisibility(View.GONE); // Hide the error notification when typing
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        currentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentPassword.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.dark_grey));
                currentPassNotif.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        // Set a click listener for the Update button
        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = changePassword.getText().toString().trim();
                String confirmNewPassword = confirmPassword.getText().toString().trim();
                String currentPasswordInput = currentPassword.getText().toString().trim();

                boolean isValid = true;

                // Reset text color to default
                changePassword.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.dark_grey));
                confirmPassword.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.dark_grey));
                currentPassword.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.dark_grey));

                // Check if passwords match
                if (!newPassword.equals(confirmNewPassword)) {
                    changePassword.setTextColor(Color.RED);
                    confirmPassword.setTextColor(Color.RED);
                    errorNotif.setVisibility(View.VISIBLE); // Show the error notification
                    isValid = false;
                } else {
                    errorNotif.setVisibility(View.GONE); // Hide the error notification if they match
                }

                // Check password length
                if (newPassword.length() < 6) {
                    changePassword.setTextColor(Color.RED);
                    isValid = false;
                }

                if (confirmNewPassword.length() < 6) {
                    confirmPassword.setTextColor(Color.RED);
                    isValid = false;
                }

                if (isValid) {
                    // Show verification fields
                    verifyNotif.setVisibility(View.VISIBLE);
                    currentPasswordLayout.setVisibility(View.VISIBLE);

                    // Handle password update
                    if (!currentPasswordInput.isEmpty()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPasswordInput);
                            user.reauthenticate(credential).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(ProfileActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        } else {
                                            Toast.makeText(ProfileActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    currentPassword.setTextColor(Color.RED);
                                    currentPassNotif.setVisibility(View.VISIBLE);
                                    Toast.makeText(ProfileActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    passwordCharactersCount.setVisibility(View.VISIBLE);
                }
            }
        });

        // Set dialog window properties
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }



    // Helper function to toggle password visibility
    private void togglePasswordVisibility(ImageView toggleButton, EditText passwordField) {
        toggleButton.setOnClickListener(v -> {
            if (passwordField.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleButton.setImageResource(R.drawable.baseline_visibility_24); // Change icon to 'visibility'
            } else {
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleButton.setImageResource(R.drawable.baseline_visibility_off_24); // Change icon to 'visibility_off'
            }
            passwordField.setSelection(passwordField.getText().length()); // Set cursor at the end of text
        });
    }


}
