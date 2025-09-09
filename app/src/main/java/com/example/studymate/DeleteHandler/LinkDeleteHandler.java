package com.example.studymate.DeleteHandler;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.HomeAdapter;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.LinkInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class LinkDeleteHandler {
    private Context context;
    private String roomId;
    private HomeAdapter adapter;

    public LinkDeleteHandler(Context context, String roomId, HomeAdapter adapter) {
        this.context = context;
        this.roomId = roomId;
        this.adapter = adapter;
    }

    public void showDeletePopupMenu(View anchor, LinkInfo linkInfo, int position) {
        PopupMenu popup = new PopupMenu(context, anchor);
        popup.getMenuInflater().inflate(R.menu.delete_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog(linkInfo.getLinkId(), position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showDeleteConfirmationDialog(String linkId, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Link")
                .setMessage("Are you sure you want to delete this Link?")
                .setPositiveButton("Yes", (dialog, which) -> deleteLink(linkId, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteLink(String linkId, int position) {
        if (linkId == null || roomId == null) {
            Log.e("LinkDeleteHandler", "linkId or roomId is null");
            return;
        }

        // Remove from Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Home")
                .child(roomId)
                .child(linkId);

        databaseReference.removeValue()
                .addOnSuccessListener(aVoid -> {

                    // Check if position is still valid before removing from the list
                    if (position >= 0 && position < adapter.getItemList().size()) {
                        adapter.getItemList().remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(context, "Link deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("LinkDeleteHandler", "Invalid position after deletion: " + position);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LinkDeleteHandler", "Failed to delete link from database", e);
                    Toast.makeText(context, "Failed to delete link from database", Toast.LENGTH_SHORT).show();
                });
    }
}
