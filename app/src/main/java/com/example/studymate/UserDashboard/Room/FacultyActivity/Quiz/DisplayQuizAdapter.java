package com.example.studymate.UserDashboard.Room.FacultyActivity.Quiz;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class DisplayQuizAdapter extends RecyclerView.Adapter<DisplayQuizAdapter.QuizViewHolder> {

    private List<Quiz> quizList;
    private String roomId;

    public DisplayQuizAdapter(List<Quiz> quizList, String roomId) {
        this.quizList = quizList;
        this.roomId = roomId;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quizzes, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizList.get(position);
        holder.quizTitle.setText(quiz.getTitle()); // Display quiz title
        holder.quizDateTime.setText(quiz.getDateTime()); // Display the date and time
        //holder.quizDuration.setText(quiz.getTime() + " mins"); // Display quiz duration in minutes

        // Set up the click listener to navigate to QuizDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, QuizDetailActivity.class);
            intent.putExtra("quizTitle", quiz.getTitle()); // Pass the quiz title
            intent.putExtra("quizDuration", quiz.getTime()); // Pass the quiz duration
            intent.putExtra("quizDateTime", quiz.getDateTime()); // Pass the quiz date and time
            intent.putExtra("roomId", roomId); // Pass the roomId
            intent.putExtra("quizId", quiz.getId()); // Pass the quiz ID
            context.startActivity(intent);
        });

        // Set up the menu button click listener for delete action
        holder.menuButton.setOnClickListener(v -> {
            showPopupMenu(holder.menuButton, quiz.getId(), holder.itemView.getContext()); // Pass the quiz ID
        });
    }


    private void showPopupMenu(View view, String quizId, Context context) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.delete_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    // Confirm before deleting
                    new AlertDialog.Builder(context)
                            .setTitle("Delete Quiz")
                            .setMessage("Are you sure you want to delete this quiz?")
                            .setPositiveButton("Delete", (dialog, which) -> deleteQuiz(quizId, roomId, context))
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void deleteQuiz(String quizId, String roomId, Context context) {
        DatabaseReference quizRef = FirebaseDatabase.getInstance().getReference("Quizzes").child(roomId).child(quizId);
        quizRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Quiz deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete quiz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {

        TextView quizTitle;
        TextView quizDateTime; // You might want to set this correctly
        TextView duration;
        ImageView menuButton;  // Add ImageView for menu button

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            quizTitle = itemView.findViewById(R.id.quizTitle);
            quizDateTime = itemView.findViewById(R.id.quizDateTime);
            menuButton = itemView.findViewById(R.id.menuButton); // Initialize menu button
        }
    }
}
