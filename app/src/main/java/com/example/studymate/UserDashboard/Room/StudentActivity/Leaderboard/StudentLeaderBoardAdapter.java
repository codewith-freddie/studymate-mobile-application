package com.example.studymate.UserDashboard.Room.StudentActivity.Leaderboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studymate.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class StudentLeaderBoardAdapter extends RecyclerView.Adapter<StudentLeaderBoardAdapter.ViewHolder> {

    private List<UserScore> userScoreList;

    public StudentLeaderBoardAdapter(List<UserScore> userScoreList) {
        this.userScoreList = userScoreList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leader_board_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserScore userScore = userScoreList.get(position);

        // Bind data to views
        holder.tvLeaderboardRank.setText(String.valueOf(position + 4));
        holder.tvLeaderboardName.setText(userScore.getFirstName() + " " + userScore.getLastName());
        holder.tvLeaderboardScore.setText(String.valueOf(userScore.getScore()));

        // Load profile image using Glide
        Glide.with(holder.ivLeaderBoardProfilePic.getContext())
                .load(userScore.getProfileImageUrl()) // Get the URL from UserScore
                .placeholder(R.drawable.profile) // Placeholder image
                .into(holder.ivLeaderBoardProfilePic);
    }

    @Override
    public int getItemCount() {
        return userScoreList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLeaderboardRank, tvLeaderboardName, tvLeaderboardScore;
        ShapeableImageView ivLeaderBoardProfilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLeaderboardRank = itemView.findViewById(R.id.tvLeaderboardRank);
            tvLeaderboardName = itemView.findViewById(R.id.tvLeaderboardName);
            tvLeaderboardScore = itemView.findViewById(R.id.tvLeaderboardScore);
            ivLeaderBoardProfilePic = itemView.findViewById(R.id.ivLeaderBoardProfilePic);
        }
    }
}