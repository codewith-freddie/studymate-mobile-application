package com.example.studymate.UserDashboard.Room;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.studymate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UploadActivity extends AppCompatActivity {

    Button saveButton;
    EditText uploadTopic, uploadDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadDesc = findViewById(R.id.uploadRoomDesc);
        uploadTopic = findViewById(R.id.uploadRoomTitle);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    public static String generateRandomCode(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    public static String generateFormattedCode() {
        String part1 = generateRandomCode(4);
        String part2 = generateRandomCode(4);
        String part3 = generateRandomCode(4);
        return part1 + "-" + part2 + "-" + part3;
    }

    public void saveData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.saving_progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        uploadData(dialog);
    }

    public void uploadData(AlertDialog dialog) {
        String title = uploadTopic.getText().toString();
        String desc = uploadDesc.getText().toString();
        String accessCode = generateFormattedCode();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Get current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "Unknown User";

        // Create DataClass instance with additional fields
        String backgroundImage = "";  // Placeholder, or provide the actual image path or URL
        DataClass dataClass = new DataClass(title, desc, userId, accessCode, 1, backgroundImage);

        // Get reference for Rooms
        DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("Rooms").child(accessCode);

        // Create a map for the update
        Map<String, Object> updates = new HashMap<>();
        updates.put("Rooms/" + accessCode, dataClass);

        // Update database path
        FirebaseDatabase.getInstance().getReference().updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(UploadActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(UploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
