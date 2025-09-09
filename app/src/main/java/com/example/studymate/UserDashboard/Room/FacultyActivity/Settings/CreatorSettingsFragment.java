package com.example.studymate.UserDashboard.Room.FacultyActivity.Settings;

import static android.app.Activity.RESULT_OK;

import static com.example.studymate.UserDashboard.Profile.ProfileActivity.PICK_IMAGE_REQUEST;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.studymate.Network.NetworkUtils;
import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class CreatorSettingsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private String roomId;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private TextView accessCodeTextView, roomTitleTextView, roomDescTextView, userCountTextView;
    private RelativeLayout backgroundLayout;

    public static CreatorSettingsFragment newInstance(String roomId) {
        CreatorSettingsFragment fragment = new CreatorSettingsFragment();
        Bundle args = new Bundle();
        args.putString("ROOM_ID", roomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomId = getArguments().getString("ROOM_ID");
            // Initialize Firebase Database reference
            databaseReference = FirebaseDatabase.getInstance().getReference("Rooms").child(roomId);
            storageReference = FirebaseStorage.getInstance().getReference("RoomImages");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        accessCodeTextView = view.findViewById(R.id.desAccessCode);
        roomTitleTextView = view.findViewById(R.id.desRoomTitle);
        roomDescTextView = view.findViewById(R.id.desRoomDesc);
        userCountTextView = view.findViewById(R.id.desUserCount);
        //dagdag
        backgroundLayout = view.findViewById(R.id.backgroundLayout);

        // Get references to the layouts
        LinearLayout editRoomTitleLayout = view.findViewById(R.id.editRoomTitleLayout);
        LinearLayout editDescLayout = view.findViewById(R.id.editDescLayout);
        LinearLayout shareAccessCodeLayout = view.findViewById(R.id.shareAccessCodeLayout);
        LinearLayout deleteRoomLayout = view.findViewById(R.id.deleteRoomLayout);
        LinearLayout editImageBackground = view.findViewById(R.id.editImageBackground);


        loadRoomData();

        // Handle background image change dagdag ini
        editImageBackground.setOnClickListener(v -> {
            if (NetworkUtils.isConnected(requireContext())) {
                openImagePicker();
            } else {
                ToastUtils.showCustomToast(requireContext(), "No Internet Connection");
            }
        });

        editRoomTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isConnected(requireContext())) {
                    showEditRoomTitleDialog();
                } else {
                    ToastUtils.showCustomToast(requireContext(), "No Internet Connection");
                }
            }
        });

        editDescLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isConnected(requireContext())) {
                    showEditRoomDescDialog();
                } else {
                    ToastUtils.showCustomToast(requireContext(), "No Internet Connection");
                }
            }
        });

        deleteRoomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isConnected(requireContext())) {
                    showDeleteRoomConfirmationDialog();
                } else {
                    ToastUtils.showCustomToast(requireContext(), "No Internet Connection");
                }
            }
        });

        shareAccessCodeLayout.setOnClickListener(v -> {
            if (roomId != null) {
                // Retrieve the access code from the database
                databaseReference.child("accessCode").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String accessCode = task.getResult().getValue(String.class);

                        if (accessCode != null) {
                            // Copy the access code to the clipboard
                            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Access Code", accessCode);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(getContext(), "Access code copied to clipboard", Toast.LENGTH_SHORT).show();

                            // Share the access code using an intent
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, "Here is the access code: " + accessCode);
                            shareIntent.setType("text/plain");
                            startActivity(Intent.createChooser(shareIntent, "Share Access Code"));
                        } else {
                            Toast.makeText(getContext(), "Access code not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to retrieve access code", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Room ID is null", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
    //dagdag ini
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Room Background"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }
    //dagdagini
    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {

            String imageId = UUID.randomUUID().toString();  // Generate unique ID for the image
            StorageReference fileRef = storageReference.child(imageId);

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Get the download URL and save it to the database
                        String imageUrl = uri.toString();
                        databaseReference.child("roomBackgroundImage").setValue(imageUrl)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Room background updated successfully", Toast.LENGTH_SHORT).show();
                                        // Update the layout background with the new image
                                        Glide.with(getContext())
                                                .load(imageUrl)
                                                .into(new CustomViewTarget<RelativeLayout, Drawable>(backgroundLayout) {
                                                    @Override
                                                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                                        backgroundLayout.setBackgroundResource(R.color.cream_white); // Set default background if failed
                                                    }

                                                    @Override
                                                    protected void onResourceCleared(@Nullable Drawable placeholder) {
                                                        // Handle resource clearing
                                                    }

                                                    @Override
                                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                        // Set the loaded image as the background of the layout
                                                        backgroundLayout.setBackground(resource);
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(getContext(), "Failed to update room background", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show());
        }
    }//end


    private void loadRoomData() {
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("Rooms").child(roomId);
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String accessCode = dataSnapshot.child("accessCode").getValue(String.class);
                    String roomTitle = dataSnapshot.child("dataTitle").getValue(String.class);
                    String roomDesc = dataSnapshot.child("dataDesc").getValue(String.class);
                    Integer userCount = dataSnapshot.child("personCount").getValue(Integer.class);
                    //dagdag ini
                    String roomBackgroundImage = dataSnapshot.child("roomBackgroundImage").getValue(String.class);


                    accessCodeTextView.setText(accessCode != null ? accessCode : "N/A");
                    roomTitleTextView.setText(roomTitle != null ? roomTitle : "N/A");
                    roomDescTextView.setText(roomDesc != null ? roomDesc : "N/A");
                    userCountTextView.setText(userCount != null ? userCount.toString() : "0");

                    // Load the room background image
                    //dagdag ini
                    if (roomBackgroundImage != null) {
                        Glide.with(getContext())
                                .load(roomBackgroundImage)
                                .into(new CustomViewTarget<RelativeLayout, Drawable>(backgroundLayout) {
                                    @Override
                                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                        backgroundLayout.setBackgroundResource(R.color.cream_white); // Set default background if loading failed
                                    }

                                    @Override
                                    protected void onResourceCleared(@Nullable Drawable placeholder) {
                                        // Handle resource clearing
                                    }

                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        backgroundLayout.setBackground(resource);

                                        // Toast for successful background update
                                        Toast.makeText(getContext(), "Background image updated successfully!", Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                }
            }//end


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load room data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditRoomTitleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_room_title, null);
        builder.setView(dialogView);

        EditText editRoomTitle = dialogView.findViewById(R.id.editRoomTitle);
        Button saveRoomTitleBtn = dialogView.findViewById(R.id.updateRoomTitleButton);

        AlertDialog dialog = builder.create();

        saveRoomTitleBtn.setOnClickListener(v -> {
            String newTitle = editRoomTitle.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                // Update the room title in the database
                databaseReference.child("dataTitle").setValue(newTitle)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Room title updated to: " + newTitle, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to update room title", Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter a valid title", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void showEditRoomDescDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_room_desc, null);
        builder.setView(dialogView);

        EditText editRoomDesc = dialogView.findViewById(R.id.editRoomDesc);
        Button saveRoomDescBtn = dialogView.findViewById(R.id.updateRoomDescButton);

        AlertDialog dialog = builder.create();

        saveRoomDescBtn.setOnClickListener(v -> {
            String newDesc = editRoomDesc.getText().toString().trim();
            if (!newDesc.isEmpty()) {
                // Update the room description in the database
                databaseReference.child("dataDesc").setValue(newDesc)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Room description updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to update room description", Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter a valid description", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void showDeleteRoomConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Room")
                .setMessage("Are you sure you want to delete this room? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteRoom())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteRoom() {
        if (roomId != null) {
            databaseReference.removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Room deleted successfully", Toast.LENGTH_SHORT).show();
                            updateRoomCountsInDatabase();
                            // Navigate back to CreatorHomeActivity
                            if (getActivity() != null) {
                                getActivity().finish(); // Close the current activity and return to the previous one
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to delete room", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Room ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRoomCountsInDatabase() {
        DatabaseReference analyticsRef = FirebaseDatabase.getInstance().getReference("Analytics").child("roomCount");
        analyticsRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentCount = currentData.getValue(Integer.class);
                if (currentCount == null) {
                    currentData.setValue(1);  // Initialize with 1 if roomCount doesn't exist
                } else if (currentCount > 0) {
                    currentData.setValue(currentCount - 1);  // Decrement by 1 if count is greater than 0
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e("FirebaseDatabase", "Failed to update roomCount: " + error.getMessage());
                } else {
                    Log.d("FirebaseDatabase", "roomCount updated successfully.");
                }
            }
        });
    }
}
