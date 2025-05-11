package com.plp.elibrary.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.plp.elibrary.R;
import com.plp.elibrary.models.Document;
import com.plp.elibrary.models.User;
import com.plp.elibrary.utils.Constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DocumentDetailActivity extends AppCompatActivity {

    private ImageView coverImageView;
    private TextView titleTextView, authorTextView, uploadDateTextView, descriptionTextView, downloadCountTextView;
    private ChipGroup categoriesChipGroup, tagsChipGroup;
    private Button downloadButton, readButton;
    private FloatingActionButton bookmarkFab;
    private ProgressBar progressBar;
    private ImageView verifiedBadge;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private Document currentDocument;
    private String documentId;
    private boolean isBookmarked = false;
    private boolean isAdmin = false;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_detail);

        // Get document ID from intent
        documentId = getIntent().getStringExtra("document_id");
        if (documentId == null) {
            Toast.makeText(this, "Document not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize UI elements
        coverImageView = findViewById(R.id.cover_image_view);
        titleTextView = findViewById(R.id.title_text_view);
        authorTextView = findViewById(R.id.author_text_view);
        uploadDateTextView = findViewById(R.id.upload_date_text_view);
        descriptionTextView = findViewById(R.id.description_text_view);
        downloadCountTextView = findViewById(R.id.download_count_text_view);
        categoriesChipGroup = findViewById(R.id.categories_chip_group);
        tagsChipGroup = findViewById(R.id.tags_chip_group);
        downloadButton = findViewById(R.id.download_button);
        readButton = findViewById(R.id.read_button);
        bookmarkFab = findViewById(R.id.bookmark_fab);
        progressBar = findViewById(R.id.progress_bar);
        verifiedBadge = findViewById(R.id.verified_badge);

        // Set click listeners
        downloadButton.setOnClickListener(v -> downloadDocument());
        readButton.setOnClickListener(v -> readDocument());
        bookmarkFab.setOnClickListener(v -> toggleBookmark());

        // Load document and user data
        loadDocument();
        loadUserData();
    }

    private void loadUserData() {
        String userId = firebaseAuth.getCurrentUser().getUid();

        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);

                        // Check if user is admin
                        if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                            isAdmin = true;
                            invalidateOptionsMenu(); // Refresh menu to show admin options
                        }

                        // Check if document is bookmarked
                        checkIfBookmarked();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void loadDocument() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.DOCUMENTS_COLLECTION)
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        currentDocument = documentSnapshot.toObject(Document.class);
                        displayDocumentDetails();
                    } else {
                        Toast.makeText(this, "Document not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayDocumentDetails() {
        // Set title and description
        titleTextView.setText(currentDocument.getTitle());
        descriptionTextView.setText(currentDocument.getDescription());

        // Set author and upload date
        authorTextView.setText("By: " + currentDocument.getAuthorName());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        if (currentDocument.getUploadDate() != null) {
            uploadDateTextView.setText("Uploaded on: " + dateFormat.format(currentDocument.getUploadDate()));
        }

        // Set download count
        downloadCountTextView.setText("Downloads: " + currentDocument.getDownloadCount());

        // Load cover image
        if (currentDocument.getCoverImageUrl() != null && !currentDocument.getCoverImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentDocument.getCoverImageUrl())
                    .placeholder(R.drawable.placeholder_cover)
                    .into(coverImageView);
        }

        // Set verified badge visibility
        if (currentDocument.isVerified()) {
            verifiedBadge.setVisibility(View.VISIBLE);
        } else {
            verifiedBadge.setVisibility(View.GONE);
        }

        // Add categories chips
        categoriesChipGroup.removeAllViews();
        if (currentDocument.getCategories() != null) {
            for (String category : currentDocument.getCategories()) {
                Chip chip = new Chip(this);
                chip.setText(category);
                chip.setClickable(false);
                categoriesChipGroup.addView(chip);
            }
        }

        // Add tags chips
        tagsChipGroup.removeAllViews();
        if (currentDocument.getTags() != null) {
            for (String tag : currentDocument.getTags()) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setClickable(false);
                tagsChipGroup.addView(chip);
            }
        }
    }

    private void checkIfBookmarked() {
        String userId = firebaseAuth.getCurrentUser().getUid();

        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection(Constants.BOOKMARKS_COLLECTION)
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isBookmarked = documentSnapshot.exists();
                    updateBookmarkIcon();
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void updateBookmarkIcon() {
        if (isBookmarked) {
            bookmarkFab.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            bookmarkFab.setImageResource(R.drawable.ic_bookmark_border);
        }
    }

    // Added missing methods that are referenced but not defined
    private void downloadDocument() {
        // Implementation for downloading document
        if (currentDocument != null) {
            // Increment download count
            DocumentReference docRef = db.collection(Constants.DOCUMENTS_COLLECTION).document(documentId);
            db.runTransaction((Transaction.Function<Void>) transaction -> {
                transaction.update(docRef, "downloadCount", currentDocument.getDownloadCount() + 1);
                return null;
            });

            Toast.makeText(this, "Downloading document...", Toast.LENGTH_SHORT).show();
            // Actual download implementation would go here
        }
    }

    private void readDocument() {
        // Implementation for reading document
        if (currentDocument != null && currentDocument.getFileUrl() != null) {
            Toast.makeText(this, "Opening document for reading...", Toast.LENGTH_SHORT).show();
            // Implementation to open PDF viewer would go here
        }
    }

    private void toggleBookmark() {
        // Implementation for toggling bookmark status
        String userId = firebaseAuth.getCurrentUser().getUid();
        if (isBookmarked) {
            // Remove bookmark
            db.collection(Constants.USERS_COLLECTION)
                    .document(userId)
                    .collection(Constants.BOOKMARKS_COLLECTION)
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        isBookmarked = false;
                        updateBookmarkIcon();
                        Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Add bookmark
            Map<String, Object> bookmarkData = new HashMap<>();
            bookmarkData.put("documentId", documentId);
            bookmarkData.put("timestamp", System.currentTimeMillis());

            db.collection(Constants.USERS_COLLECTION)
                    .document(userId)
                    .collection(Constants.BOOKMARKS_COLLECTION)
                    .document(documentId)
                    .set(bookmarkData)
                    .addOnSuccessListener(aVoid -> {
                        isBookmarked = true;
                        updateBookmarkIcon();
                        Toast.makeText(this, "Bookmark added", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}