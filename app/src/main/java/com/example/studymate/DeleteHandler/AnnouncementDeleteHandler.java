package com.example.studymate.DeleteHandler;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.Announcement;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.HomeAdapter;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.LinkInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AnnouncementDeleteHandler {
    private Context context;
    private String roomId;
    private HomeAdapter adapter;

    public AnnouncementDeleteHandler(Context context, String roomId, HomeAdapter adapter) {
        this.context = context;
        this.roomId = roomId;
        this.adapter = adapter;
    }

    public void showDeletePopupMenu(View anchor, Announcement announcement, int position) {
        PopupMenu popup = new PopupMenu(context, anchor);
        popup.getMenuInflater().inflate(R.menu.delete_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog(announcement.getAnnouncementId(), position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showDeleteConfirmationDialog(String announcementId, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Announcement")
                .setMessage("Are you sure you want to delete this announcement?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAnnouncement(announcementId, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAnnouncement(String announcementId, int position) {
        if (announcementId == null) {
            Log.e("AnnouncementDeleteHandler", "announcementId is null");
            return;
        } else if (roomId == null) {
            Log.e("AnnouncementDeleteHandler", "roomId is null");
            return;
        }

        // Remove from Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Home")
                .child(roomId)
                .child(announcementId);

        databaseReference.removeValue()
                .addOnSuccessListener(aVoid -> {

                    // Check if position is still valid before removing from the list
                    if (position >= 0 && position < adapter.getItemList().size()) {
                        adapter.getItemList().remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(context, "Announcement deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("AnnouncementDeleteHandler", "Invalid position after deletion: " + position);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AnnouncementDeleteHandler", "Failed to delete announcement from database", e);
                    Toast.makeText(context, "Failed to delete link from database", Toast.LENGTH_SHORT).show();
                });
    }
}
