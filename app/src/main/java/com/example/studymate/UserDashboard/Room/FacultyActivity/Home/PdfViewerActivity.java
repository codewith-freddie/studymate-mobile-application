package com.example.studymate.UserDashboard.Room.FacultyActivity.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studymate.R;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PdfViewerActivity extends AppCompatActivity {

    private PDFView pdfView;
    private ProgressBar progressBar;
    private String fileName;
    private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        // Inflate the toolbar layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Enable the back button in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set PDF title
        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        downloadUrl = intent.getStringExtra("downloadUrl");

        if (fileName != null && downloadUrl != null) {

            pdfView = findViewById(R.id.pdfView);
            progressBar = findViewById(R.id.progressBar);

            File pdfFile = getLocalFile(fileName);
            if (pdfFile.exists()) {
                // Load PDF from local storage
                loadPdfFromFile(pdfFile);
            } else {
                // Show progress bar and download PDF
                progressBar.setVisibility(View.VISIBLE);
                downloadPdfFromUrl(downloadUrl, fileName);
            }
        } else {
            // Handle error
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error: Unable to load PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private File getLocalFile(String fileName) {
        // Define the file location in internal storage
        File directory = getFilesDir();
        return new File(directory, fileName);
    }

    private void downloadPdfFromUrl(String downloadUrl, String fileName) {
        new Thread(() -> {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                inputStream = connection.getInputStream();

                File file = getLocalFile(fileName);
                outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    loadPdfFromFile(file);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // Handle error
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error downloading PDF", Toast.LENGTH_SHORT).show();
                });
            } finally {
                // Ensure streams are closed
                try {
                    if (inputStream != null) inputStream.close();
                    if (outputStream != null) outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loadPdfFromFile(File file) {
        runOnUiThread(() -> {
            pdfView.fromFile(file)
                    .onRender((pages, pageWidth, pageHeight) -> {
                        // Hide progress bar when PDF is rendered
                        progressBar.setVisibility(View.GONE);
                    })
                    .load();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu for the toolbar
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the back button and other menu item clicks
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_download) {
            // Handle download action here
            Toast.makeText(this, "PDF has been saved", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
