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

public class LinkHandler {

    private Context context;
    private String currentRoomId;

    public LinkHandler(Context context, String roomId) {
        this.context = context;
        this.currentRoomId = roomId;
    }

    public void handleLinkClick(Dialog parentDialog) {
        if (NetworkUtils.isConnected(context)) {
            showUploadLinkDialog();
        } else {
            ToastUtils.showCustomToast(context, "No Internet Connection");
        }
        parentDialog.dismiss();
    }

    private void showUploadLinkDialog() {
        final Dialog uploadLinkDialog = new Dialog(context);
        uploadLinkDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        uploadLinkDialog.setContentView(R.layout.dialog_upload_link);

        // Cancel Button
        TextView cancelButton = uploadLinkDialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> uploadLinkDialog.dismiss());

        // Initialize Firebase Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Find views
        EditText linkTitleEditText = uploadLinkDialog.findViewById(R.id.linkTitle);
        EditText linkUrlEditText = uploadLinkDialog.findViewById(R.id.linkUrl);
        EditText instructionsEditText = uploadLinkDialog.findViewById(R.id.createInstructions);
        Button uploadLinkButton = uploadLinkDialog.findViewById(R.id.uploadLinkButton);

        uploadLinkButton.setOnClickListener(v -> {
            String instructionsText = instructionsEditText.getText().toString().trim();

            if (!instructionsText.isEmpty()) {
                // Generate a unique announcement ID using UUID
                String linkId = UUID.randomUUID().toString();

                String linkTitle = linkTitleEditText.getText().toString().trim();
                String linkUrl = linkUrlEditText.getText().toString().trim();

                // Get current date and time in desired format
                String currentDateTime = new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.getDefault()).format(new Date());

                // Get the current timestamp
                long timestamp = System.currentTimeMillis();

                // Prepare data to save
                Map<String, Object> linkData = new HashMap<>();
                linkData.put("linkId", linkId);
                linkData.put("linkTitle", linkTitle);
                linkData.put("link", linkUrl);
                linkData.put("instructions", instructionsText);
                linkData.put("dateTime", currentDateTime);
                linkData.put("timestamp", timestamp); // Add timestamp
                linkData.put("type", "link");

                // Save data to Firebase
                databaseReference.child("Home").child(currentRoomId).child(linkId).updateChildren(linkData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                ToastUtils.showCustomToast(context, "Link created successfully");
                            } else {
                                ToastUtils.showCustomToast(context, "Failed to create link");
                            }
                            // Dismiss dialog
                            uploadLinkDialog.dismiss();
                        });
            } else {
                ToastUtils.showCustomToast(context, "link text cannot be empty");
            }
        });

        uploadLinkDialog.show();
        uploadLinkDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        uploadLinkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        uploadLinkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        uploadLinkDialog.getWindow().setGravity(Gravity.CENTER);
    }
}
