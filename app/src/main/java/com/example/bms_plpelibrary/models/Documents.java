package com.example.bms_plpelibrary.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Documents {
    private String documentId;
    private String title;
    private String description;
    private String authorId;
    private String authorName;
    private String fileUrl;
    private String coverImageUrl;
    private long fileSize;
    private Date uploadDate;
    private String documentType;  // "THESIS", "MODULE", "RESEARCH", "VERIFIED_EBOOK"
    private List<String> categories;
    private String accessLevel;   // "PUBLIC", "PRIVATE", "COURSE_RESTRICTED"
    private List<String> allowedCourses;
    private int downloadCount;
    private boolean isVerified;
    private List<String> tags;

    // Constructors, getters, setters
    public Documents() {
        // Required empty constructor for Firebase
        this.categories = new ArrayList<>();
        this.allowedCourses = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    // Getters and setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public Date getUploadDate() { return uploadDate; }
    public void setUploadDate(Date uploadDate) { this.uploadDate = uploadDate; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    public List<String> getAllowedCourses() { return allowedCourses; }
    public void setAllowedCourses(List<String> allowedCourses) { this.allowedCourses = allowedCourses; }

    public int getDownloadCount() { return downloadCount; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    public void addCategory(String category) {
        if (this.categories == null) {
            this.categories = new ArrayList<>();
        }
        this.categories.add(category);
    }

    public void addAllowedCourse(String course) {
        if (this.allowedCourses == null) {
            this.allowedCourses = new ArrayList<>();
        }
        this.allowedCourses.add(course);
    }

    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tag);
    }
}