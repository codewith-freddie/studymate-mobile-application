package com.example.studymate.UserDashboard.Room.StudentActivity.Home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;

import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    private List<Announcement> announcementList;

    public AnnouncementAdapter(List<Announcement> announcementList) {
        this.announcementList = announcementList;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        Announcement announcement = announcementList.get(position);

        // Set the title and datetime fields
        holder.dateTime.setText(announcement.getDateTime());

        // Set the full text for the announcement body
        holder.announcementTextView.setText(announcement.getAnnouncement());

        // Initially set the text collapsed
        holder.announcementTextView.setMaxLines(3);  // Show only 3 lines by default
        holder.expandTextView.setText("More");

        holder.expandTextView.setOnClickListener(v -> {
            if (holder.announcementTextView.getMaxLines() == 3) {
                // Expand the text
                holder.announcementTextView.setMaxLines(Integer.MAX_VALUE);
                holder.expandTextView.setText("Less");
            } else {
                // Collapse the text
                holder.announcementTextView.setMaxLines(3);
                holder.expandTextView.setText("More");
            }
        });
    }

    @Override
    public int getItemCount() {
        return announcementList.size();
    }

    public void updateAnnouncements(List<Announcement> newAnnouncements) {
        announcementList.clear();
        announcementList.addAll(newAnnouncements);
        notifyDataSetChanged();  // Notify the adapter that data has changed
    }

    static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dateTime, announcementTextView, expandTextView;

        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.announcementTitle);
            dateTime = itemView.findViewById(R.id.dateTime);
            announcementTextView = itemView.findViewById(R.id.announcementText);
            expandTextView = itemView.findViewById(R.id.expandTextView);  // TextView for "More" / "Less" functionality
        }
    }
}
