package com.example.studymate.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.Spannable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import com.example.studymate.Network.NetworkUtils;
import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.PdfInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisplayReferencesHandler {

    private Context context;
    private String currentRoomId;
    private String currentModuleId;

    public DisplayReferencesHandler(Context context, String roomId, String moduleId) {
        this.context = context;
        this.currentRoomId = roomId;
        this.currentModuleId = moduleId;
    }

    public void handleShowReferencesClick(ImageView anchor, PdfInfo pdfInfo, int position) {
        if (NetworkUtils.isConnected(context)) {
            showReferencesDialog();
        } else {
            ToastUtils.showCustomToast(context, "No Internet Connection");
        }
    }

    public void handleShowStudentReferencesClick(ImageView anchor, com.example.studymate.UserDashboard.Room.StudentActivity.Home.PdfInfo pdfInfo, int position) {
        if (NetworkUtils.isConnected(context)) {
            showReferencesDialog();
        } else {
            ToastUtils.showCustomToast(context, "No Internet Connection");
        }
    }

    public void showReferencesDialog() {
        final Dialog referencesDialog = new Dialog(context);
        referencesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        referencesDialog.setContentView(R.layout.dialog_display_references);

        LinearLayout referencesContainer = referencesDialog.findViewById(R.id.referencesContainer);
        Button closeButton = referencesDialog.findViewById(R.id.closeButton);

        // Initialize Firebase Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Fetch the references for a specific module
        databaseReference.child("Home")
                .child(currentRoomId)
                .child(currentModuleId)
                .child("references")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot referenceSnapshot : dataSnapshot.getChildren()) {
                                String author = referenceSnapshot.child("author").getValue(String.class);
                                String year = referenceSnapshot.child("year").getValue(String.class);
                                String title = referenceSnapshot.child("title").getValue(String.class);
                                String url = referenceSnapshot.child("url").getValue(String.class);

                                // Format the reference in APA style
                                String formattedReference = String.format("%s. (%s). %s.", author, year, title);
                                SpannableString spannableReference = new SpannableString(formattedReference + "\n" + url);

                                // Set the URL color to sky blue
                                int urlStart = formattedReference.length() + 1; // Start after formatted reference and newline
                                int urlEnd = spannableReference.length(); // End of the spannable string
                                spannableReference.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.sky_blue)),
                                        urlStart, urlEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                // Create a TextView for the formatted reference
                                TextView textView = new TextView(context);
                                textView.setText(spannableReference);
                                textView.setPadding(40, 16, 0, 16); // Add left padding for hanging indent
                                textView.setLineSpacing(1.5f, 1.5f); // Optional: Adjust line spacing

                                // Optional: Make the URL clickable
                                textView.setOnClickListener(v -> {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(url));
                                    context.startActivity(intent);
                                });

                                // Add the TextView to the LinearLayout container
                                referencesContainer.addView(textView);
                            }
                        } else {
                            // No references found for the module
                            ToastUtils.showCustomToast(context, "No references found.");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        ToastUtils.showCustomToast(context, "Failed to load references.");
                    }
                });

        // Close button functionality
        closeButton.setOnClickListener(v -> referencesDialog.dismiss());

        referencesDialog.show();
        referencesDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        referencesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        referencesDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        referencesDialog.getWindow().setGravity(Gravity.CENTER);
    }
}
