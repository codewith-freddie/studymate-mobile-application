package com.example.studymate.OfflineModules;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.barteksc.pdfviewer.PDFView;
import com.example.studymate.R;

import java.io.File;

public class PdfDisplayActivity extends AppCompatActivity {

    private PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_display);

        pdfView = findViewById(R.id.pdfView);

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("fileName");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the filename as the title of the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(fileName != null ? fileName : "PDF Viewer");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Handle back button click
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();  // or finish() to close the activity
            }
        });

        if (fileName != null) {
            File file = new File(getFilesDir(), fileName);
            if (file.exists()) {
                pdfView.fromFile(file)
                        .load();
            } else {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
}
