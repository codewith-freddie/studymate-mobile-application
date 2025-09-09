package com.example.studymate.UserDashboard.Room.StudentActivity.Home;

public class PdfDisplay {
    private String fileName;
    private String fileUrl;

    public PdfDisplay() {
        // Default constructor required for calls to DataSnapshot.getValue(Pdf.class)
    }

    public PdfDisplay(String fileName, String fileUrl) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}

