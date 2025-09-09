package com.example.studymate.OfflineModules;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;

import java.io.File;
import java.util.List;

public class OfflinePdfAdapter extends RecyclerView.Adapter<OfflinePdfAdapter.PdfViewHolder> {

    private final List<File> pdfFiles;
    private final OnPdfClickListener onPdfClickListener;
    private final Context context;

    public OfflinePdfAdapter(Context context, List<File> pdfFiles, OnPdfClickListener onPdfClickListener) {
        this.context = context;
        this.pdfFiles = pdfFiles;
        this.onPdfClickListener = onPdfClickListener;
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pdf_card, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        File pdfFile = pdfFiles.get(position);
        holder.titleTextView.setText(pdfFile.getName());

        // Set click listener for the PDF item
        holder.cardView.setOnClickListener(v -> onPdfClickListener.onPdfClick(pdfFile));

        // Set click listener for the menu button
        holder.menuButton.setOnClickListener(v -> showPopupMenu(holder, pdfFile, position));
    }

    @Override
    public int getItemCount() {
        return pdfFiles.size();
    }

    // Method to show the popup menu with a delete option
    private void showPopupMenu(PdfViewHolder holder, File pdfFile, int position) {
        PopupMenu popupMenu = new PopupMenu(context, holder.menuButton);
        popupMenu.inflate(R.menu.delete_menu); // Assume you've created this menu resource with a "Delete" item

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog(pdfFile, position);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    // Method to show a confirmation dialog before deleting
    private void showDeleteConfirmationDialog(File pdfFile, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete PDF")
                .setMessage("Are you sure you want to delete this file?")
                .setPositiveButton("Yes", (dialog, which) -> deletePdfFile(pdfFile, position))
                .setNegativeButton("No", null)
                .show();
    }

    // Method to delete the PDF file and update the RecyclerView
    private void deletePdfFile(File pdfFile, int position) {
        if (pdfFile.delete()) {
            pdfFiles.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class PdfViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView titleTextView;
        ImageView menuButton;

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.offlineModuleCard);
            titleTextView = itemView.findViewById(R.id.offlineModuleTitle);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }

    public interface OnPdfClickListener {
        void onPdfClick(File pdfFile);
    }
}
