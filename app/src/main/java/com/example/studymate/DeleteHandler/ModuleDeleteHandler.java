package com.example.studymate.DeleteHandler;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.HomeAdapter;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.PdfInfo;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ModuleDeleteHandler {

    private Context context;
    private String roomId;
    private HomeAdapter adapter;

    public ModuleDeleteHandler(Context context, String roomId, HomeAdapter adapter) {
        this.context = context;
        this.roomId = roomId;
        this.adapter = adapter;
    }

    public void showDeletePopupMenu(ImageView anchor, PdfInfo pdfInfo, int position) {
        PopupMenu popup = new PopupMenu(context, anchor);
        popup.getMenuInflater().inflate(R.menu.delete_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog(pdfInfo.getModuleId(), pdfInfo.getUrl(), position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showDeleteConfirmationDialog(String pdfId, String pdfUrl, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete PDF")
                .setMessage("Are you sure you want to delete this PDF?")
                .setPositiveButton("Yes", (dialog, which) -> deletePdf(pdfId, pdfUrl, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deletePdf(String pdfId, String pdfUrl, int position) {
        if (pdfId == null || roomId == null || pdfUrl == null) {
            Log.e("ModuleDeleteHandler", "pdfId, roomId, or pdfUrl is null");
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(pdfUrl);

        Log.d("ModuleDeleteHandler", "Attempting to delete file at: " + storageReference.getPath());

        storageReference.delete()
                .addOnSuccessListener(aVoid -> {
                    FirebaseDatabase.getInstance().getReference("Home")
                            .child(roomId)
                            .child(pdfId)
                            .removeValue()
                            .addOnSuccessListener(aVoid1 -> {
                                if (position >= 0 && position < adapter.getItemList().size()) {
                                    adapter.getItemList().remove(position);
                                    adapter.notifyItemRemoved(position);
                                    Toast.makeText(context, "PDF deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("ModuleDeleteHandler", "Invalid position after deletion: " + position);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to delete PDF from database", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("ModuleDeleteHandler", "Failed to delete PDF from storage: ", e);
                    Toast.makeText(context, "Failed to delete PDF from storage", Toast.LENGTH_SHORT).show();
                });
    }
}
