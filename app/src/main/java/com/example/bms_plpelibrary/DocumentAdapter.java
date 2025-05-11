package com.example.bms_plpelibrary;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bms_plpelibrary.models.Documents;
import com.plp.elibrary.R;
import com.plp.elibrary.activities.DocumentDetailActivity;
import com.plp.elibrary.models.Document;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private Context context;
    private List<Documents> documents;
    private SimpleDateFormat dateFormat;

    public DocumentAdapter(Context context, List<Documents> documents) {
        this.context = context;
        this.documents = documents;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Documents document = documents.get(position);

        holder.titleTextView.setText(document.getTitle());
        holder.authorTextView.setText(document.getAuthorName());

        if (document.getUploadDate() != null) {
            holder.dateTextView.setText(dateFormat.format(document.getUploadDate()));
        } else {
            holder.dateTextView.setText("Unknown date");
        }

        if (document.isVerified()) {
            holder.verifiedBadge.setVisibility(View.VISIBLE);
        } else {
            holder.verifiedBadge.setVisibility(View.GONE);
        }

        // Load cover image
        if (document.getCoverImageUrl() != null && !document.getCoverImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(document.getCoverImageUrl())
                    .placeholder(R.drawable.placeholder_cover)
                    .into(holder.coverImageView);
        } else {
            holder.coverImageView.setImageResource(R.drawable.placeholder_cover);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DocumentDetailActivity.class);
            intent.putExtra("document_id", document.getDocumentId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    public void updateData(List<Documents> newDocuments) {
        this.documents = newDocuments;
        notifyDataSetChanged();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImageView;
        TextView titleTextView, authorTextView, dateTextView;
        ImageView verifiedBadge;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.cover_image_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            authorTextView = itemView.findViewById(R.id.author_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            verifiedBadge = itemView.findViewById(R.id.verified_badge);
        }
    }
}
