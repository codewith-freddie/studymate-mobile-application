package com.example.studymate.UserDashboard.Room;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.FacultyActivity.FacultyActivity;
import com.example.studymate.UserDashboard.Room.FacultyActivity.MyViewHolder;
import com.example.studymate.UserDashboard.Room.StudentActivity.ParticipantHomeActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private Context context;
    private List<DataClass> dataList;

    public MyAdapter(Context context, List<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DataClass currentItem = dataList.get(position);
        holder.getRecTitle().setText(currentItem.getDataTitle());
        holder.getRecDesc().setText(currentItem.getDataDesc());
        holder.getPersonCount().setText(String.valueOf(currentItem.getPersonCount()));

        // Load and set the background image using Glide to the RelativeLayout
        String backgroundImageUrl = currentItem.getRoomBackgroundImage();
        if (backgroundImageUrl != null && !backgroundImageUrl.isEmpty()) {
            Glide.with(context)
                    .load(backgroundImageUrl)
                    .centerCrop() // Crop the image
                    .override(300, 200) // Adjust the size of the image as necessary
                    .into(new CustomViewTarget<RelativeLayout, Drawable>(holder.backgroundLayout) {
                        @Override
                        protected void onResourceCleared(@Nullable Drawable placeholder) {
                            // Handle resource clearing
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            // Handle load failure
                        }

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            holder.backgroundLayout.setBackground(resource); // Set background to RelativeLayout
                        }
                    });
        }

        holder.getRecCard().setOnClickListener(view -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Intent intent;
            if (currentItem.getCreatedBy().equals(currentUserId)) {
                intent = new Intent(context, FacultyActivity.class);
            } else {
                intent = new Intent(context, ParticipantHomeActivity.class);
            }
            intent.putExtra("ROOM_ID", currentItem.getAccessCode());
            context.startActivity(intent);
        });

        ImageView menuButton = holder.itemView.findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, menuButton);
            popupMenu.getMenu().add("Access Code");

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Access Code")) {
                    showAccessCodeDialog(currentItem.getAccessCode());
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    private void showAccessCodeDialog(String accessCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_access_code, null);
        builder.setView(dialogView);

        TextView accessCodeTextView = dialogView.findViewById(R.id.accessCode);
        Button copyButton = dialogView.findViewById(R.id.copyButton);

        accessCodeTextView.setText(accessCode);

        copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Access Code", accessCode);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Access Code copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        // Customize the dialog appearance and behavior
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; // Ensure this style exists
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void searchDataList(ArrayList<DataClass> searchList) {
        dataList = new ArrayList<>(searchList); // Ensure the list is updated
        notifyDataSetChanged();
    }

}
