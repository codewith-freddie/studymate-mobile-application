package com.example.studymate.AdminDashboard.AdminHome.People;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.studymate.R;

public class DialogAdminPassword extends DialogFragment {

    private EditText adminPassword, confirmPassword, currentPassword;
    private TextView passwordCharactersCount, errorNotif, verifyNotif, currentPassNotif;
    private Button submitPasswordButton;
    private ImageView passwordToggle, confirmPasswordToggle, currentPasswordToggle;
    private LinearLayout currentPasswordLayout;

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Inflate the layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_admin_password, null);

        // Initialize layout elements
        adminPassword = view.findViewById(R.id.adminPassword);
        confirmPassword = view.findViewById(R.id.confrimPassword);
        currentPassword = view.findViewById(R.id.currentPassword);
        passwordCharactersCount = view.findViewById(R.id.passwordCharactersCount);
        errorNotif = view.findViewById(R.id.errorNotif);
        verifyNotif = view.findViewById(R.id.verifyNotif);
        currentPassNotif = view.findViewById(R.id.currentPassNotif);
        submitPasswordButton = view.findViewById(R.id.submitPasswordButton);
        passwordToggle = view.findViewById(R.id.password_toggle);
        confirmPasswordToggle = view.findViewById(R.id.confirm_password_toggle);
        currentPasswordToggle = view.findViewById(R.id.current_password_toggle);
        currentPasswordLayout = view.findViewById(R.id.currentPasswordLayout);

        setupPasswordVisibilityToggles();
        setupUpdateButton();
        setupTextWatchers();

        dialog.setContentView(view);

        // Set dialog window properties
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        return dialog;
    }

    private void setupPasswordVisibilityToggles() {
        // Toggle visibility for adminPassword
        passwordToggle.setOnClickListener(v -> {
            if (adminPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                adminPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.baseline_visibility_24);
            } else {
                adminPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24);
            }
        });

        // Toggle visibility for confirmPassword
        confirmPasswordToggle.setOnClickListener(v -> {
            if (confirmPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                confirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                confirmPasswordToggle.setImageResource(R.drawable.baseline_visibility_24);
            } else {
                confirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                confirmPasswordToggle.setImageResource(R.drawable.baseline_visibility_off_24);
            }
        });

        // Toggle visibility for currentPassword
        currentPasswordToggle.setOnClickListener(v -> {
            if (currentPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                currentPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                currentPasswordToggle.setImageResource(R.drawable.baseline_visibility_24);
            } else {
                currentPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                currentPasswordToggle.setImageResource(R.drawable.baseline_visibility_off_24);
            }
        });
    }

    private void setupUpdateButton() {
        submitPasswordButton.setOnClickListener(v -> {
            String adminPass = adminPassword.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();
            String currentPass = currentPassword.getText().toString().trim();

            // Validate inputs
            if (adminPass.isEmpty() || confirmPass.isEmpty()) {
                passwordCharactersCount.setVisibility(View.VISIBLE);
                return;
            }

            if (adminPass.length() < 6) {
                passwordCharactersCount.setVisibility(View.VISIBLE);
                return;
            }

            // Validate password match
            if (!adminPass.equals(confirmPass)) {
                errorNotif.setVisibility(View.VISIBLE);
                confirmPassword.setTextColor(Color.RED); // Only confirmPassword should be red
                return;
            } else {
                errorNotif.setVisibility(View.GONE);
                confirmPassword.setTextColor(Color.BLACK); // Reset text color
            }

            // Check if current password is provided and valid
            if (currentPasswordLayout.getVisibility() == View.VISIBLE) {
                if (currentPass.isEmpty()) {
                    currentPassNotif.setVisibility(View.VISIBLE);
                    return;
                } else {
                    currentPassNotif.setVisibility(View.GONE);
                    // Validate current password with the server (Firebase, etc.)
                    if (validateCurrentPassword(currentPass)) {
                        // Update password
                        submitPassword(adminPass);
                    } else {
                        currentPassNotif.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                // No current password required
                submitPassword(adminPass);
            }
        });
    }

    private void setupTextWatchers() {
        adminPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswords();
            }

            @Override
            public void afterTextChanged(Editable s) {
                String adminPass = adminPassword.getText().toString().trim();
                if (adminPass.length() >= 6) {
                    passwordCharactersCount.setVisibility(View.GONE);
                } else {
                    passwordCharactersCount.setVisibility(View.VISIBLE);
                }
            }
        });

        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswords();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void validatePasswords() {
        String adminPass = adminPassword.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();

        if (!adminPass.equals(confirmPass)) {
            errorNotif.setVisibility(View.VISIBLE);
            confirmPassword.setTextColor(Color.RED); // Only confirmPassword should be red
        } else {
            errorNotif.setVisibility(View.GONE);
            confirmPassword.setTextColor(Color.BLACK); // Reset text color
        }
    }

    private boolean validateCurrentPassword(String currentPass) {
        // Implement password validation logic (e.g., check with Firebase)
        return true; // Replace with actual validation
    }

    private void submitPassword(String newPassword) {
        // Implement password update logic (e.g., update in Firebase)
        // On success:
        Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
    }
}
