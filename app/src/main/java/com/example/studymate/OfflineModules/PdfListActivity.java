package com.example.studymate.OfflineModules;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PdfListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OfflinePdfAdapter pdfAdapter;
    private ProgressBar progressBar;
    private List<File> pdfFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_list);

        recyclerView = findViewById(R.id.recyclerView);

        pdfFiles = getPdfFiles();
        pdfAdapter = new OfflinePdfAdapter(this, pdfFiles, this::openPdf);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(pdfAdapter);

        if (pdfFiles.isEmpty()) {
            Toast.makeText(this, "No PDFs available", Toast.LENGTH_SHORT).show();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the title to "Offline Modules"
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Offline Modules");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            // Set custom drawable as the back button
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24);
        }

        // Handle back button click
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();  // or finish() to close the activity
            }
        });
    }

    private List<File> getPdfFiles() {
        File directory = getFilesDir();
        File[] files = directory.listFiles((dir, name) -> name.endsWith(""));
        List<File> pdfFiles = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                pdfFiles.add(file);
            }
        }
        return pdfFiles;
    }

    private void openPdf(File pdfFile) {
        Intent intent = new Intent(this, PdfDisplayActivity.class);
        intent.putExtra("fileName", pdfFile.getName());
        startActivity(intent);
    }
}
