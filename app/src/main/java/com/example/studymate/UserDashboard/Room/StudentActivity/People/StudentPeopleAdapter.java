package com.example.studymate.UserDashboard.Room.StudentActivity.People;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.FacultyActivity.People.UserInfo;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentPeopleAdapter extends RecyclerView.Adapter<StudentPeopleAdapter.ViewHolder> {

    private List<UserInfo> userList;
    private final OnRemoveUserClickListener removeUserClickListener;

    public StudentPeopleAdapter(List<UserInfo> userList, OnRemoveUserClickListener removeUserClickListener) {
        this.userList = userList;
        this.removeUserClickListener = removeUserClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_item_user_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserInfo userInfo = userList.get(position);

        // Bind data to views
        holder.nameTextView.setText(userInfo.getFullName());
        Glide.with(holder.itemView.getContext())
                .load(userInfo.getProfileImageUrl())
                .circleCrop() // Apply circular crop transformation
                .into(holder.userImageView);

        // Set action button click listener
        holder.removeButton.setOnClickListener(v -> {
            // Notify the listener that a remove action has been triggered
            if (removeUserClickListener != null) {
                removeUserClickListener.onRemoveUserClick(userInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView userImageView;
        public TextView nameTextView;
        public Button removeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            userImageView = itemView.findViewById(R.id.userImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }

    public void updateList(List<UserInfo> newList) {
        userList.clear();
        userList.addAll(newList);
        notifyDataSetChanged();
    }

    // Interface to handle remove button clicks
    public interface OnRemoveUserClickListener {
        void onRemoveUserClick(UserInfo user);
    }
}
