package model;

import java.sql.Timestamp;

public class FileRecord {
    private String fileName;
    private String uploader;
    private Timestamp timestamp;
    private String filePath; // New field for the file path

    // Constructor
    public FileRecord(String fileName, String uploader, Timestamp timestamp, String filePath) {
        this.fileName = fileName;
        this.uploader = uploader;
        this.timestamp = timestamp;
        this.filePath = filePath;
    }

    // Getters and setters for each field
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
