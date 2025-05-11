package com.example.bms_plpelibrary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.bms_plpelibrary.R;
import com.example.bms_plpelibrary.adapters.DocumentAdapter;
import com.plp.elibrary.models.Document;
import com.plp.elibrary.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recentlyAddedRecyclerView, popularRecyclerView, verifiedEbooksRecyclerView;
    private DocumentAdapter recentlyAddedAdapter, popularAdapter, verifiedEbooksAdapter;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    private FirebaseFirestore db;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize UI elements
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateTextView = view.findViewById(R.id.empty_state_text_view);

        // Setup Recently Added RecyclerView
        recentlyAddedRecyclerView = view.findViewById(R.id.recently_added_recycler_view);
        recentlyAddedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recentlyAddedAdapter = new DocumentAdapter(getContext(), new ArrayList<>());
        recentlyAddedRecyclerView.setAdapter(recentlyAddedAdapter);

        // Setup Popular RecyclerView
        popularRecyclerView = view.findViewById(R.id.popular_recycler_view);
        popularRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularAdapter = new DocumentAdapter(getContext(), new ArrayList<>());
        popularRecyclerView.setAdapter(popularAdapter);

        // Setup Verified eBooks RecyclerView
        verifiedEbooksRecyclerView = view.findViewById(R.id.verified_ebooks_recycler_view);
        verifiedEbooksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        verifiedEbooksAdapter = new DocumentAdapter(getContext(), new ArrayList<>());
        verifiedEbooksRecyclerView.setAdapter(verifiedEbooksAdapter);

        // Load data
        loadRecentlyAdded();
        loadPopular();
        loadVerifiedEbooks();

        return view;
    }

    private void loadRecentlyAdded() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.DOCUMENTS_COLLECTION)
                .whereEqualTo("accessLevel", "PUBLIC")
                .orderBy("uploadDate", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);

                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Document> documents = queryDocumentSnapshots.toObjects(Document.class);
                        recentlyAddedAdapter.updateData(documents);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    // Handle error
                });
    }

    private void loadPopular() {
        db.collection(Constants.DOCUMENTS_COLLECTION)
                .whereEqualTo("accessLevel", "PUBLIC")
                .orderBy("downloadCount", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Document> documents = queryDocumentSnapshots.toObjects(Document.class);
                        popularAdapter.updateData(documents);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void loadVerifiedEbooks() {
        db.collection(Constants.DOCUMENTS_COLLECTION)
                .whereEqualTo("documentType", "VERIFIED_EBOOK")
                .whereEqualTo("isVerified", true)
                .orderBy("uploadDate", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Document> documents = queryDocumentSnapshots.toObjects(Document.class);
                        verifiedEbooksAdapter.updateData(documents);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }
}
