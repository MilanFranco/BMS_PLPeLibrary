package com.example.bms_plpelibrary;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.plp.elibrary.R;
import com.plp.elibrary.adapters.DocumentAdapter;
import com.plp.elibrary.models.Document;
import com.plp.elibrary.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private EditText searchEditText;
    private ChipGroup categoriesChipGroup, documentTypesChipGroup;
    private RecyclerView documentsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    private DocumentAdapter documentAdapter;
    private FirebaseFirestore db;

    private List<String> selectedCategories = new ArrayList<>();
    private List<String> selectedDocumentTypes = new ArrayList<>();
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        searchEditText = view.findViewById(R.id.search_edit_text);
        categoriesChipGroup = view.findViewById(R.id.categories_chip_group);
        documentTypesChipGroup = view.findViewById(R.id.document_types_chip_group);
        documentsRecyclerView = view.findViewById(R.id.documents_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateTextView = view.findViewById(R.id.empty_state_text_view);

        // Setup RecyclerView
        documentsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        documentAdapter = new DocumentAdapter(getContext(), new ArrayList<>());
        documentsRecyclerView.setAdapter(documentAdapter);

        // Load categories
        loadCategories();

        // Setup document types
        setupDocumentTypes();

        // Setup search functionality
        setupSearch();

        // Load initial documents
        loadDocuments();

        return view;
    }

    private void setupDocumentTypes() {
        String[] documentTypes = {"THESIS", "MODULE", "RESEARCH", "VERIFIED_EBOOK"};
        String[] displayNames = {"Thesis", "Module", "Research", "Verified eBook"};

        for (int i = 0; i < documentTypes.length; i++) {
            final String type = documentTypes[i];
            final String displayName = displayNames[i];

            Chip chip = new Chip(getContext());
            chip.setText(displayName);
            chip.setCheckable(true);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedDocumentTypes.add(type);
                } else {
                    selectedDocumentTypes.remove(type);
                }

                loadDocuments();
            });

            documentTypesChipGroup.addView(chip);
        }
    }

    private void loadCategories() {
        db.collection(Constants.CATEGORIES_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String categoryName = document.getString("name");
                        if (categoryName != null) {
                            Chip chip = new Chip(getContext());
                            chip.setText(categoryName);
                            chip.setCheckable(true);

                            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                if (isChecked) {
                                    selectedCategories.add(categoryName);
                                } else {
                                    selectedCategories.remove(categoryName);
                                }

                                loadDocuments();
                            });

                            categoriesChipGroup.addView(chip);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().trim().toLowerCase();
                loadDocuments();
            }
        });
    }

    private void loadDocuments() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);

        Query query = db.collection(Constants.DOCUMENTS_COLLECTION)
                .whereEqualTo("accessLevel", "PUBLIC");

        if (!selectedDocumentTypes.isEmpty()) {
            query = query.whereIn("documentType", selectedDocumentTypes);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            progressBar.setVisibility(View.GONE);

            List<Document> filteredDocuments = new ArrayList<>();

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Document doc = document.toObject(Document.class);

                // Filter by search query
                boolean matchesSearch = currentSearchQuery.isEmpty() ||
                        doc.getTitle().toLowerCase().contains(currentSearchQuery) ||
                        doc.getDescription().toLowerCase().contains(currentSearchQuery);

                // Filter by categories
                boolean matchesCategory = selectedCategories.isEmpty() ||
                        (doc.getCategories() != null && doc.getCategories().stream()
                                .anyMatch(category -> selectedCategories.contains(category)));

                if (matchesSearch && matchesCategory) {
                    filteredDocuments.add(doc);
                }
            }

            documentAdapter.updateData(filteredDocuments);

            if (filteredDocuments.isEmpty()) {
                emptyStateTextView.setVisibility(View.VISIBLE);
            } else {
                emptyStateTextView.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.VISIBLE);
        });
    }
}}