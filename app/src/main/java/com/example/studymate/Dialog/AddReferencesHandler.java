package com.example.studymate.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.studymate.Network.NetworkUtils;
import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.PdfInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AddReferencesHandler {

    private Context context;
    private String currentRoomId;
    private String currentModuleId;

    public AddReferencesHandler(Context context, String roomId, String moduleId) {
        this.context = context;
        this.currentRoomId = roomId;
        this.currentModuleId = moduleId;
    }

    public void handleAddReferencesClick(ImageView anchor, PdfInfo pdfInfo, int position) {
        if (NetworkUtils.isConnected(context)) {
            showAddReferencesDialog();
        } else {
            ToastUtils.showCustomToast(context, "No Internet Connection");
        }
    }

    private void showAddReferencesDialog() {
        final Dialog addReferencesDialog = new Dialog(context);
        addReferencesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addReferencesDialog.setContentView(R.layout.dialog_add_references);

        TextView cancelButton = addReferencesDialog.findViewById(R.id.cancelButton);
        EditText urlEditText = addReferencesDialog.findViewById(R.id.linkUrl);
        Button addReferenceButton = addReferencesDialog.findViewById(R.id.addReferencesButton);
        ImageView checkIcon = addReferencesDialog.findViewById(R.id.checkIcon);
        TextView textChecker = addReferencesDialog.findViewById(R.id.textChecker);

        // Initialize Firebase Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Set a TextWatcher on the EditText
        urlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hide the check icon and text checker when user types
                checkIcon.setVisibility(View.GONE);
                textChecker.setVisibility(View.GONE);
                // Set the drawable as the background
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.border_rectangle_filled);
                urlEditText.setBackground(drawable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed here
            }
        });

        addReferenceButton.setOnClickListener(v -> {
            String urlsInput = urlEditText.getText().toString().trim();

            if (!urlsInput.isEmpty()) {
                String[] references = urlsInput.split("\\r?\\n");

                boolean allValid = true; // To track overall validity

                // Reset the visibility of the check icon and text checker at the start
                checkIcon.setVisibility(View.GONE);
                textChecker.setVisibility(View.GONE);

                for (String referenceText : references) {
                    referenceText = referenceText.trim();
                    if (!referenceText.isEmpty()) {
                        String[] parts = referenceText.split(",", 4);

                        if (parts.length == 4) {
                            String author = parts[0].trim();
                            String year = parts[1].trim();
                            String title = parts[2].trim();
                            String urlText = parts[3].trim();

                            boolean isValid = validateReference(author, year, title, urlText);
                            if (isValid) {
                                // Prepare to save the reference
                                saveReference(databaseReference, author, year, title, urlText);
                                addReferencesDialog.dismiss();
                            } else {
                                allValid = false; // At least one reference is invalid
                                checkIcon.setVisibility(View.VISIBLE); // Show error icon
                                textChecker.setVisibility(View.VISIBLE); // Show error message
                                textChecker.setText("Please ensure each entry following the given format below.");
                                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.border_error_rectangle_filled);
                                urlEditText.setBackground(drawable);
                            }
                        } else {
                            allValid = false; // Invalid format
                            checkIcon.setVisibility(View.VISIBLE); // Show error icon
                            textChecker.setVisibility(View.VISIBLE); // Show error message
                            textChecker.setText("Invalid format. Ensure each entry is separated by comma (,).");
                            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.border_error_rectangle_filled);
                            urlEditText.setBackground(drawable);
                        }
                    }
                }

                if (allValid) {
                    ToastUtils.showCustomToast(context, "References added successfully");
                }

            } else {
                checkIcon.setVisibility(View.VISIBLE); // Show error icon
                textChecker.setVisibility(View.VISIBLE); // Show error message
                textChecker.setText("Please enter at least one reference.");
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.border_error_rectangle_filled);
                urlEditText.setBackground(drawable);
            }
        });

        cancelButton.setOnClickListener(v -> addReferencesDialog.dismiss());

        addReferencesDialog.show();
        addReferencesDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addReferencesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        addReferencesDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        addReferencesDialog.getWindow().setGravity(Gravity.CENTER);
    }

    private boolean validateReference(String author, String year, String title, String url) {
        // Basic validation checks
        return !author.isEmpty() && !year.isEmpty() && !title.isEmpty() && isValidUrl(url);
    }

    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private void saveReference(DatabaseReference databaseReference, String author, String year, String title, String urlText) {
        String refId = UUID.randomUUID().toString();
        String currentDateTime = new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.getDefault()).format(new Date());
        long timestamp = System.currentTimeMillis();

        Map<String, Object> linkData = new HashMap<>();
        linkData.put("referencesId", refId);
        linkData.put("author", author);
        linkData.put("year", year);
        linkData.put("title", title);
        linkData.put("url", urlText);
        linkData.put("dateTime", currentDateTime);
        linkData.put("timestamp", timestamp);

        databaseReference.child("Home")
                .child(currentRoomId)
                .child(currentModuleId)
                .child("references")
                .child(refId)
                .updateChildren(linkData);
    }
}
