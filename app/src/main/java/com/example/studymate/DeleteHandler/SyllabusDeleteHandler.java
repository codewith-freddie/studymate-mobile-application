package com.example.studymate.DeleteHandler;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.HomeAdapter;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.SyllabusInfo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.studymate.R;

public class SyllabusDeleteHandler {
    private Context context;
    private String roomId;
    private HomeAdapter adapter;

    public SyllabusDeleteHandler(Context context, String roomId, HomeAdapter adapter) {
        this.context = context;
        this.roomId = roomId;
        this.adapter = adapter;
    }

    public void showDeletePopupMenu(View anchor, SyllabusInfo syllabusInfo, int position) {
        PopupMenu popup = new PopupMenu(context, anchor);
        popup.getMenuInflater().inflate(R.menu.delete_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog(syllabusInfo.getSyllabusId(), syllabusInfo.getUrl(), position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showDeleteConfirmationDialog(String syllabusId, String syllabusUrl, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Course Syllabus")
                .setMessage("Are you sure you want to delete this Course Syllabus?")
                .setPositiveButton("Yes", (dialog, which) -> deleteSyllabus(syllabusId, syllabusUrl, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteSyllabus(String syllabusId, String syllabusUrl, int position) {
        if (syllabusId == null) {
            Log.e("SyllabusDeleteHandler", "syllabusId is null");
            return;
        }

        if (roomId == null) {
            Log.e("SyllabusDeleteHandler", "roomId is null");
            return;
        }

        if (syllabusUrl == null) {
            Log.e("SyllabusDeleteHandler", "syllabusUrl is null");
            return;
        }


        // Create a reference to the PDF file in Firebase Storage using the download URL
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(syllabusUrl);

        Log.d("SyllabusDeleteHandler", "Attempting to delete file at: " + storageReference.getPath());

        // Delete the PDF file from Firebase Storage
        storageReference.delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove the item from Firebase Realtime Database
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Home")
                            .child(roomId)
                            .child(syllabusId);

                    databaseReference.removeValue()
                            .addOnSuccessListener(aVoid1 -> {
                                // Check if position is still valid before removing from the list
                                if (position >= 0 && position < adapter.getItemList().size()) {
                                    adapter.getItemList().remove(position);
                                    adapter.notifyItemRemoved(position);
                                    // Notify that the remaining items have changed in position
                                    adapter.notifyItemRangeChanged(position, adapter.getItemList().size() - position);
                                    Toast.makeText(context, "Course Syllabus Deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("SyllabusDeleteHandler", "Invalid position after deletion: " + position);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("SyllabusDeleteHandler", "Failed to delete PDF from database", e);
                                Toast.makeText(context, "Failed to delete PDF from database", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("SyllabusDeleteHandler", "Failed to delete Course Syllabus from storage: ", e);
                    Toast.makeText(context, "Failed to delete PDF from storage", Toast.LENGTH_SHORT).show();
                });
    }
}
