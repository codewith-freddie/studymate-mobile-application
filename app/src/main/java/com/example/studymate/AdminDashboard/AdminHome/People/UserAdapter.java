package com.example.studymate.AdminDashboard.AdminHome.People;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.studymate.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<AdminUser> userList;
    private DatabaseReference usersRef;

    public UserAdapter(Context context, List<AdminUser> userList) {
        this.context = context;
        this.userList = userList;
        this.usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_info, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        AdminUser user = userList.get(position);
        holder.nameTextView.setText(user.getFullName());

        Glide.with(context)
                .load(user.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.userImageView);

        holder.removeButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirm Removal")
                    .setMessage("Are you sure you want to remove this user?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Remove user from database
                        usersRef.child(user.getId()).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    // Decrease registeredCount by 1
                                    DatabaseReference analyticsRef = FirebaseDatabase.getInstance().getReference().child("Analytics").child("registeredCount");
                                    analyticsRef.runTransaction(new Transaction.Handler() {
                                        @NonNull
                                        @Override
                                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                            Integer currentCount = mutableData.getValue(Integer.class);
                                            if (currentCount == null) {
                                                mutableData.setValue(0);
                                            } else {
                                                mutableData.setValue(currentCount - 1);
                                            }
                                            return Transaction.success(mutableData);
                                        }

                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                                            if (databaseError != null) {
                                                // Handle possible errors
                                            } else {
                                                // Remove user from local list and notify adapter
                                                userList.remove(position);
                                                notifyItemRemoved(position);
                                            }
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userImageView;
        TextView nameTextView;
        Button removeButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userImageView = itemView.findViewById(R.id.userImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}
