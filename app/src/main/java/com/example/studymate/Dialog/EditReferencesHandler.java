package com.example.studymate.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.studymate.Network.NetworkUtils;
import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.PdfInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditReferencesHandler {

    private Context context;
    private String roomId;
    private String moduleId;
    private List<String> referencesToDelete = new ArrayList<>();

    public EditReferencesHandler(Context context, String roomId, String moduleId) {
        this.context = context;
        this.roomId = roomId;
        this.moduleId = moduleId;
    }

    public void handleEditReferencesClick(ImageView anchor, PdfInfo pdfInfo, int position) {
        if (NetworkUtils.isConnected(context)) {
            showEditReferencesDialog();
        } else {
            ToastUtils.showCustomToast(context, "No Internet Connection");
        }
    }

    private void showEditReferencesDialog() {
        final Dialog referencesDialog = new Dialog(context);
        referencesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        referencesDialog.setContentView(R.layout.dialog_edit_references);

        TextView cancelButton = referencesDialog.findViewById(R.id.cancelReferencesButton);
        Button updateButton = referencesDialog.findViewById(R.id.updateReferencesButton);
        LinearLayout referencesLayout = referencesDialog.findViewById(R.id.referencesLayout);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        loadReferencesForDisplay(databaseReference, referencesLayout);

        cancelButton.setOnClickListener(v -> referencesDialog.dismiss());

        updateButton.setOnClickListener(v -> {
            updateReferences(databaseReference, referencesLayout, referencesDialog);
        });

        referencesDialog.show();
        referencesDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        referencesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        referencesDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        referencesDialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void loadReferencesForDisplay(DatabaseReference databaseReference, LinearLayout referencesLayout) {
        databaseReference.child("Home")
                .child(roomId)
                .child(moduleId)
                .child("references")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    referencesLayout.removeAllViews();
                    for (DataSnapshot referenceSnapshot : dataSnapshot.getChildren()) {
                        String referenceId = referenceSnapshot.getKey();
                        String author = referenceSnapshot.child("author").getValue(String.class);
                        String year = referenceSnapshot.child("year").getValue(String.class);
                        String title = referenceSnapshot.child("title").getValue(String.class);
                        String url = referenceSnapshot.child("url").getValue(String.class);
                        displayReferenceItem(referencesLayout, referenceId, author, year, title, url);
                    }
                });
    }

    private void displayReferenceItem(LinearLayout referencesLayout, String referenceId, String author, String year, String title, String url) {
        View referenceItem = LayoutInflater.from(context).inflate(R.layout.item_references, null);
        referenceItem.setTag(referenceId);
        EditText referenceEditText = referenceItem.findViewById(R.id.referenceEditText);
        ImageView deleteButton = referenceItem.findViewById(R.id.deleteIcon);

        String fullText = author + ", " + year + ", " + title + ", " + url;
        referenceEditText.setText(fullText);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        referenceItem.setLayoutParams(params);

        deleteButton.setOnClickListener(v -> {
            referencesToDelete.add(referenceId);
            referencesLayout.removeView(referenceItem);
        });

        referencesLayout.addView(referenceItem);

        referenceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = referenceEditText.getText().toString();
                String[] details = text.split(", ");

                if (details.length >= 4) {
                    referenceItem.setBackground(ContextCompat.getDrawable(context, R.drawable.border_rectangle));
                    referenceEditText.setTextColor(ContextCompat.getColor(context, R.color.dark_grey)); // Set text color to dark gray
                    deleteButton.setColorFilter(ContextCompat.getColor(context, R.color.green)); // Change icon tint to green
                } else {
                    referenceItem.setBackground(ContextCompat.getDrawable(context, R.drawable.border_error_rectangle_filled));
                    referenceEditText.setTextColor(ContextCompat.getColor(context, R.color.red));
                    deleteButton.setColorFilter(ContextCompat.getColor(context, R.color.red)); // Change icon tint to red
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateReferences(DatabaseReference databaseReference, LinearLayout referencesLayout, Dialog referencesDialog) {
        boolean allValid = true;

        for (int i = 0; i < referencesLayout.getChildCount(); i++) {
            View referenceItem = referencesLayout.getChildAt(i);
            EditText referenceEditText = referenceItem.findViewById(R.id.referenceEditText);
            ImageView deleteButton = referenceItem.findViewById(R.id.deleteIcon);
            String referenceId = (String) referenceItem.getTag();

            String[] details = referenceEditText.getText().toString().split(", ");
            if (details.length < 4) {
                ToastUtils.showCustomToast(context, "Invalid reference format. Please use: Author, Year, Title, URL");
                referenceItem.setBackground(ContextCompat.getDrawable(context, R.drawable.border_error_rectangle_filled));
                referenceEditText.setTextColor(Color.RED);
                deleteButton.setColorFilter(ContextCompat.getColor(context, R.color.red));
                allValid = false;
            } else {
                referenceItem.setBackground(ContextCompat.getDrawable(context, R.drawable.border_rectangle));
                referenceEditText.setTextColor(ContextCompat.getColor(context, R.color.dark_grey)); // Reset text color
                deleteButton.setColorFilter(ContextCompat.getColor(context, R.color.green)); // Change icon tint to green

                String author = details[0];
                String year = details[1];
                String title = details[2];
                String url = details[3];

                Map<String, Object> updatedReference = new HashMap<>();
                updatedReference.put("author", author);
                updatedReference.put("year", year);
                updatedReference.put("title", title);
                updatedReference.put("url", url);

                databaseReference.child("Home")
                        .child(roomId)
                        .child(moduleId)
                        .child("references")
                        .child(referenceId)
                        .setValue(updatedReference);
            }
        }

        if (allValid) {
            for (String referenceId : referencesToDelete) {
                deleteReference(databaseReference, referenceId);
            }
            ToastUtils.showCustomToast(context, "References updated successfully");
            referencesDialog.dismiss(); // Close dialog on success
        }
    }

    private void deleteReference(DatabaseReference databaseReference, String referenceId) {
        databaseReference.child("Home")
                .child(roomId)
                .child(moduleId)
                .child("references")
                .child(referenceId)
                .removeValue();
        ToastUtils.showCustomToast(context, "Reference deleted successfully");
    }
}
