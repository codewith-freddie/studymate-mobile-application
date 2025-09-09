package com.example.studymate.UserDashboard.Room.FacultyActivity.Home;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;

import java.util.List;

import android.app.AlertDialog;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import android.view.MenuInflater;
import android.widget.PopupMenu;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.PdfViewHolder> {
    private Context context;
    private List<PdfInfo> pdfList;
    private String roomId; // Assume you need roomId for deleting the item

    public PdfAdapter(Context context, List<PdfInfo> pdfList, String roomId) {
        this.context = context;
        this.pdfList = pdfList;
        this.roomId = roomId;
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        PdfInfo pdfInfo = pdfList.get(position);
        holder.titleTextView.setText(pdfInfo.getTitle());
        holder.dateTimeTextView.setText(pdfInfo.getDateTime());

        // Log the values of pdfId and roomId for debugging
        Log.d("PdfAdapter", "pdfId: " + pdfInfo.getModuleId() + ", roomId: " + roomId);

        // Handle item click to view PDF
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PdfViewerActivity.class);
            intent.putExtra("fileName", pdfInfo.getTitle());
            intent.putExtra("downloadUrl", pdfInfo.getUrl());
            context.startActivity(intent);
        });

        // Handle menu button click to delete PDF
        holder.menuButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.menuButton);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.delete_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete PDF")
                            .setMessage("Are you sure you want to delete this PDF?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Log.d("PdfAdapter", "Attempting to delete PDF with id: " + pdfInfo.getModuleId());
                                deletePdf(pdfInfo.getModuleId(), position);
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return pdfList.size();
    }

    private void deletePdf(String pdfId, int position) {
        if (pdfId == null || roomId == null) {
            Log.e("PdfAdapter", "pdfId or roomId is null");
            return;
        }

        // Remove the item from Firebase
        FirebaseDatabase.getInstance().getReference("Modules")
                .child(roomId)
                .child(pdfId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Check if position is still valid before removing from the list
                    if (position >= 0 && position < pdfList.size()) {
                        pdfList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "PDF deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("PdfAdapter", "Invalid position after deletion: " + position);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete PDF", Toast.LENGTH_SHORT).show();
                });
    }


    static class PdfViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView dateTimeTextView;
        ImageView menuButton;

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.moduleTitle);
            dateTimeTextView = itemView.findViewById(R.id.dateTime);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }
}
