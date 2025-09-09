package com.example.studymate.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.studymate.Network.NetworkUtils;
import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AnnouncementHandler {

    private Context context;
    private String currentRoomId;

    public AnnouncementHandler(Context context, String roomId) {
        this.context = context;
        this.currentRoomId = roomId;
    }

    public void handleAnnouncementClick(Dialog parentDialog) {
        if (NetworkUtils.isConnected(context)) {
            showCreateAnnouncementDialog();
        } else {
            ToastUtils.showCustomToast(context, "No Internet Connection");
        }
        parentDialog.dismiss();
    }

    private void showCreateAnnouncementDialog() {
        final Dialog createAnnouncementDialog = new Dialog(context);
        createAnnouncementDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        createAnnouncementDialog.setContentView(R.layout.dialog_create_announcement);

        // Initialize Firebase Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Cancel Button
        TextView cancelButton = createAnnouncementDialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> createAnnouncementDialog.dismiss());

        // Find views
        EditText createAnnouncementEditText = createAnnouncementDialog.findViewById(R.id.createAnnouncement);
        Button createAnnouncementButton = createAnnouncementDialog.findViewById(R.id.createAnnouncementButton);

        createAnnouncementButton.setOnClickListener(v -> {
            String announcementText = createAnnouncementEditText.getText().toString().trim();

            if (!announcementText.isEmpty()) {
                // Generate a unique announcement ID using UUID
                String announcementId = UUID.randomUUID().toString();

                // Get current date and time in desired format
                String currentDateTime = new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.getDefault()).format(new Date());

                // Get the current timestamp
                long timestamp = System.currentTimeMillis();

                // Prepare data to save
                Map<String, Object> announcementData = new HashMap<>();
                announcementData.put("announcementId", announcementId);
                announcementData.put("announcement", announcementText);
                announcementData.put("dateTime", currentDateTime);
                announcementData.put("timestamp", timestamp); // Add timestamp
                announcementData.put("type", "announcement");

                // Save data to Firebase
                databaseReference.child("Home").child(currentRoomId).child(announcementId).updateChildren(announcementData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                ToastUtils.showCustomToast(context, "Announcement created successfully");
                            } else {
                                ToastUtils.showCustomToast(context, "Failed to create announcement");
                            }
                            // Dismiss dialog
                            createAnnouncementDialog.dismiss();
                        });
            } else {
                ToastUtils.showCustomToast(context, "Announcement text cannot be empty");
            }
        });

        createAnnouncementDialog.show();
        createAnnouncementDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        createAnnouncementDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        createAnnouncementDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        createAnnouncementDialog.getWindow().setGravity(Gravity.CENTER);
    }
}
