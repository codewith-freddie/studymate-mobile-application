package com.example.studymate.UserDashboard.Room;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.studymate.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateActivity extends AppCompatActivity {

    Button updateButton;
    EditText updateDesc, updateTitle;
    String title, desc, createdBy, accessCode, backgroundImageUrl;;
    String key;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        updateButton = findViewById(R.id.updateButton);
        updateDesc = findViewById(R.id.updateRoomDesc);
        updateTitle = findViewById(R.id.updateRoomTitle);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            updateTitle.setText(bundle.getString("Title"));
            updateDesc.setText(bundle.getString("Description"));
            key = bundle.getString("Key");
            createdBy = bundle.getString("CreatedBy");
            accessCode = bundle.getString("AccessCode");
        }

        if (key != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Rooms").child(key);
        } else {
            Toast.makeText(this, "Error: Key is null", Toast.LENGTH_SHORT).show();
            finish();
        }

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateData();
            }
        });
    }

    public void updateData() {
        title = updateTitle.getText().toString().trim();
        desc = updateDesc.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create DataClass instance with personCount set to 1
        DataClass dataClass = new DataClass(title, desc, createdBy, accessCode, 1, backgroundImageUrl);

        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.saving_progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        databaseReference.setValue(dataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(UpdateActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(UpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
