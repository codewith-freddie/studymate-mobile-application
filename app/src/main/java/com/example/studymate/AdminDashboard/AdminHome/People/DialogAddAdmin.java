package com.example.studymate.AdminDashboard.AdminHome.People;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.studymate.R;
import android.text.Editable;
import android.text.TextWatcher;

public class DialogAddAdmin extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create the dialog
        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.dialog_add_admin);

        // Initialize dialog views
        EditText firstNameEditText = dialog.findViewById(R.id.firstName);
        EditText lastNameEditText = dialog.findViewById(R.id.lastName);
        EditText emailEditText = dialog.findViewById(R.id.email);
        Button nextButton = dialog.findViewById(R.id.nextButton);

        // Save the original hints
        String firstNameOriginalHint = firstNameEditText.getHint().toString();
        String lastNameOriginalHint = lastNameEditText.getHint().toString();
        String emailOriginalHint = emailEditText.getHint().toString();

        // Set up text watchers to reset the hint color and text when the user starts typing
        TextWatcher resetHintWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Reset the hint and hint color when the user starts typing
                if (!s.toString().isEmpty()) {
                    if (firstNameEditText.hasFocus()) {
                        firstNameEditText.setHint(firstNameOriginalHint);
                        firstNameEditText.setHintTextColor(Color.GRAY); // Default hint color
                    } else if (lastNameEditText.hasFocus()) {
                        lastNameEditText.setHint(lastNameOriginalHint);
                        lastNameEditText.setHintTextColor(Color.GRAY); // Default hint color
                    } else if (emailEditText.hasFocus()) {
                        emailEditText.setHint(emailOriginalHint);
                        emailEditText.setHintTextColor(Color.GRAY); // Default hint color
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        firstNameEditText.addTextChangedListener(resetHintWatcher);
        lastNameEditText.addTextChangedListener(resetHintWatcher);
        emailEditText.addTextChangedListener(resetHintWatcher);

        // Set up the next button click listener
        nextButton.setOnClickListener(v -> {
            // Handle the click event for the next button
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();

            boolean isValid = true;

            // Check if firstName is empty
            if (firstName.isEmpty()) {
                firstNameEditText.setHint("This field is required");
                firstNameEditText.setHintTextColor(Color.RED);
                isValid = false;
            }

            // Check if lastName is empty
            if (lastName.isEmpty()) {
                lastNameEditText.setHint("This field is required");
                lastNameEditText.setHintTextColor(Color.RED);
                isValid = false;
            }

            // Check if email is empty or not in a valid format
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setHint(email.isEmpty() ? "This field is required" : "Invalid email format");
                emailEditText.setHintTextColor(Color.RED);
                isValid = false;
            }

            if (isValid) {
                // Create a Bundle to pass data to DialogChangePassword
                Bundle args = new Bundle();
                args.putString("firstName", firstName);
                args.putString("lastName", lastName);
                args.putString("email", email);

                // Create an instance of the new dialog and set arguments
                DialogAdminPassword dialogAdminPassword = new DialogAdminPassword();
                dialogAdminPassword.setArguments(args);
                dialogAdminPassword.show(getParentFragmentManager(), "DialogAdminPassword");

                // Dismiss the current dialog
                dismiss();
            }
        });

        // Set dialog window properties
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        return dialog;
    }
}
