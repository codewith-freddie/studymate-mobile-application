package com.example.studymate.Notification;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.example.studymate.R;

public class DialogUtil {

    public static void showConfirmationDialog(Activity activity, String title, String message, View.OnClickListener onDoneClickListener) {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.dialog_confirmation, null);

        // Create the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setView(dialogView);

        // Initialize views
        TextView titleView = dialogView.findViewById(R.id.title);
        TextView messageView = dialogView.findViewById(R.id.confirmation_message);
        Button doneButton = dialogView.findViewById(R.id.btnDone);

        // Set the title and message
        titleView.setText(title);
        messageView.setText(message);

        // Create and show the dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // Set the button click listener
        doneButton.setOnClickListener(v -> {
            if (onDoneClickListener != null) {
                onDoneClickListener.onClick(v);
            }
            dialog.dismiss(); // Dismiss the dialog after button click
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }
}
