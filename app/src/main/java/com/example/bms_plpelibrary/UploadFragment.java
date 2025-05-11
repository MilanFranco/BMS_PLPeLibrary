package com.example.bms_plpelibrary;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.plp.elibrary.R;
import com.plp.elibrary.models.Document;
import com.plp.elibrary.models.User;
import com.plp.elibrary.utils.Constants;
import com.plp.elibrary.utils.FileUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UploadFragment extends Fragment {

    private static final int PICK_PDF_REQUEST = 1;
    private static final int PICK_COVER_REQUEST = 2;

    private EditText titleEditText, descriptionEditText, tagEditText;
    private Spinner documentTypeSpinner, accessLevelSpinner;
    private Button selectPdfButton, selectCoverButton, uploadButton, addTagButton;
    private TextView selectedFileNameTextView;
    private ImageView coverImageView;
    private ChipGroup tagsChipGroup, categoriesChipGroup, coursesChipGroup;
    private ProgressBar progressBar;

    private Uri pdfUri, coverImageUri;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private User currentUser;
    private List<String> tagsList = new ArrayList<>();
    private List<String> categoriesList = new ArrayList<>();
    private List<String> coursesList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize UI elements
        titleEditText = view.findViewById(R.id.title_edit_text);
        descriptionEditText = view.findViewById(R.id.description_edit_text);
        documentTypeSpinner = view.findViewById(R.id.document_type_spinner);
        accessLevelSpinner = view.findViewById(R.id.access_level_spinner);
        selectPdfButton = view.findViewById(R.id.select_pdf_button);
        selectCoverButton = view.findViewById(R.id.select_cover_button);
        uploadButton = view.findViewById(R.id.upload_button);
        selectedFileNameTextView = view.findViewById(R.id.selected_file_name_text_view);
        coverImageView = view.findViewById(R.id.cover_image_view);
        tagEditText = view.findViewById(R.id.tag_edit_text);
        addTagButton = view.findViewById(R.id.add_tag_button);
        tagsChipGroup = view.findViewById(R.id.tags_chip_group);
        categoriesChipGroup = view.findViewById(R.id.categories_chip_group);
        coursesChipGroup = view.findViewById(R.id.courses_chip_group);
        progressBar = view.findViewById(R.id.progress_bar);

        // Setup document type spinner
        ArrayAdapter<CharSequence> documentTypeAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.document_types,
                android.R.layout.simple_spinner_item
        );
        documentTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        documentTypeSpinner.setAdapter(documentTypeAdapter);

        // Setup access level spinner
        ArrayAdapter<CharSequence> accessLevelAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.access_levels,
                android.R.layout.simple_spinner_item
        );
        accessLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accessLevelSpinner.setAdapter(accessLevelAdapter);

        // Set click listeners
        selectPdfButton.setOnClickListener(v -> selectPdf());
        selectCoverButton.setOnClickListener(v -> selectCoverImage());
        uploadButton.setOnClickListener(v -> uploadDocument());
        addTagButton.setOnClickListener(v -> addTag());

        // Load user data
        loadUserData();

        // Load categories
        loadCategories();

        // Load courses
        loadCourses();

        return view;
    }

    private void loadUserData() {
        String userId = firebaseAuth.getCurrentUser().getUid();

        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(getContext(), "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCategories() {
        db.collection(Constants.CATEGORIES_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            String category = doc.getString("name");
                            if (category != null) {
                                addCategoryChip(category);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(getContext(), "Failed to load categories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCourses() {
        db.collection(Constants.COURSES_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            String course = doc.getString("name");
                            if (course != null) {
                                addCourseChip(course);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(getContext(), "Failed to load courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void selectPdf() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    private void selectCoverImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Cover Image"), PICK_COVER_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_PDF_REQUEST) {
                pdfUri = data.getData();
                selectedFileNameTextView.setText(FileUtils.getFileName(getContext(), pdfUri));
            } else if (requestCode == PICK_COVER_REQUEST) {
                coverImageUri = data.getData();
                Glide.with(this)
                        .load(coverImageUri)
                        .placeholder(R.drawable.placeholder_cover)
                        .into(coverImageView);
            }
        }
    }

    private void addTag() {
        String tag = tagEditText.getText().toString().trim();
        if (!tag.isEmpty() && !tagsList.contains(tag)) {
            tagsList.add(tag);
            addTagChip(tag);
            tagEditText.setText("");
        }
    }

    private void addTagChip(String tag) {
        Chip chip = new Chip(getContext());
        chip.setText(tag);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setClickable(false);

        chip.setOnCloseIconClickListener(v -> {
            tagsChipGroup.removeView(chip);
            tagsList.remove(tag);
        });

        tagsChipGroup.addView(chip);
    }

    private void addCategoryChip(final String category) {
        Chip chip = new Chip(getContext());
        chip.setText(category);
        chip.setCheckable(true);
        chip.setClickable(true);

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                categoriesList.add(category);
            } else {
                categoriesList.remove(category);
            }
        });

        categoriesChipGroup.addView(chip);
    }

    private void addCourseChip(final String course) {
        Chip chip = new Chip(getContext());
        chip.setText(course);
        chip.setCheckable(true);
        chip.setClickable(true);

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                coursesList.add(course);
            } else {
                coursesList.remove(course);
            }
        });

        coursesChipGroup.addView(chip);
    }

    private void uploadDocument() {
        // Validate inputs
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String documentType = documentTypeSpinner.getSelectedItem().toString();
        String accessLevel = accessLevelSpinner.getSelectedItem().toString();

        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        if (description.isEmpty()) {
            descriptionEditText.setError("Description is required");
            return;
        }

        if (pdfUri == null) {
            Toast.makeText(getContext(), "Please select a PDF file", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        uploadButton.setEnabled(false);

        // Create a new document
        Document document = new Document();
        document.setDocumentId(UUID.randomUUID().toString());
        document.setTitle(title);
        document.setDescription(description);
        document.setAuthorId(firebaseAuth.getCurrentUser().getUid());
        document.setAuthorName(currentUser != null ? currentUser.getName() : "Unknown");
        document.setUploadDate(new Date());
        document.setDocumentType(documentType);
        document.setCategories(categoriesList);
        document.setAccessLevel(accessLevel);
        document.setTags(tagsList);
        document.setDownloadCount(0);
        document.setVerified(false);

        if (accessLevel.equals("COURSE_RESTRICTED")) {
            document.setAllowedCourses(coursesList);
        }

        // Upload PDF file
        final StorageReference pdfRef = storageRef.child("pdfs/" + document.getDocumentId() + ".pdf");
        pdfRef.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    pdfRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        document.setFileUrl(uri.toString());

                        // Upload cover image if selected
                        if (coverImageUri != null) {
                            uploadCoverImage(document);
                        } else {
                            // Save document to Firestore
                            saveDocumentToFirestore(document);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    uploadButton.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to upload PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    // Update progress if needed
                    progressBar.setProgress((int) progress);
                });
    }

    private void uploadCoverImage(final Document document) {
        final StorageReference coverRef = storageRef.child("covers/" + document.getDocumentId() + ".jpg");
        coverRef.putFile(coverImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    coverRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        document.setCoverImageUrl(uri.toString());

                        // Save document to Firestore
                        saveDocumentToFirestore(document);
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    uploadButton.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to upload cover image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveDocumentToFirestore(Document document) {
        db.collection(Constants.DOCUMENTS_COLLECTION)
                .document(document.getDocumentId())
                .set(document)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    uploadButton.setEnabled(true);

                    // Clear form
                    clearForm();

                    Toast.makeText(getContext(), "Document uploaded successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    uploadButton.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to save document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        titleEditText.setText("");
        descriptionEditText.setText("");
        tagEditText.setText("");
        documentTypeSpinner.setSelection(0);
        accessLevelSpinner.setSelection(0);
        selectedFileNameTextView.setText("");
        coverImageView.setImageResource(R.drawable.placeholder_cover);
        tagsChipGroup.removeAllViews();
        tagsList.clear();
        pdfUri = null;
        coverImageUri = null;

        // Uncheck all categories and courses
        for (int i = 0; i < categoriesChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) categoriesChipGroup.getChildAt(i);
            chip.setChecked(false);
        }

        for (int i = 0; i < coursesChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) coursesChipGroup.getChildAt(i);
            chip.setChecked(false);
        }

        categoriesList.clear();
        coursesList.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up resources if needed
    }
}