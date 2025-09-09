package com.example.studymate.UserDashboard.Room.StudentActivity.Home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.studymate.Network.NetworkUtils;
import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ModuleViewerActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;

    private PDFView pdfView;
    private ProgressBar progressBar;
    private String fileName;
    private String downloadUrl;
    private ImageView backButton;
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        // Initialize the toolbar and layout components
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Set up the toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Enable the default toolbar back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            // Set the fileName as the toolbar title
            getSupportActionBar().setTitle(fileName);  // Correct way to set the title
        }

        // Get the file name and download URL from the Intent
        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        downloadUrl = intent.getStringExtra("downloadUrl");

        // Initialize PDF view and progress bar
        pdfView = findViewById(R.id.pdfView);
        progressBar = findViewById(R.id.progressBar);

        // Check if the PDF file exists locally
        File pdfFile = getLocalFile(fileName);
        if (pdfFile.exists()) {
            loadPdfFromFile(pdfFile);
        } else {
            // If the file is not saved offline, show a message
            Toast.makeText(this, "Save it offline", Toast.LENGTH_SHORT).show();
            // You can use the download URL if you want to download the file or show a download button.
        }
    }

    private File getLocalFile(String fileName) {
        // Define the file location in internal storage
        File directory = getFilesDir();
        return new File(directory, fileName);
    }

    private void downloadPdfFromUrl(String downloadUrl, String fileName) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
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
                        Toast.makeText(this, "Download completed", Toast.LENGTH_SHORT).show();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
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

    // This method is triggered when the download icon is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_download) {
            Log.d("PdfViewerActivity", "Download icon clicked");
            if (NetworkUtils.isConnected(this )) {
                if (downloadUrl != null) {
                    downloadPdfFromUrl(downloadUrl, fileName); // Start the download ONLY when user clicks
                } else {
                    Toast.makeText(this, "No URL available to download", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else {
                ToastUtils.showCustomToast(this, "No Internet Connection");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, retry the download
                if (downloadUrl != null && fileName != null) {
                    downloadPdfFromUrl(downloadUrl, fileName);
                }
            } else {
                Toast.makeText(this, "Permission denied to write external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
