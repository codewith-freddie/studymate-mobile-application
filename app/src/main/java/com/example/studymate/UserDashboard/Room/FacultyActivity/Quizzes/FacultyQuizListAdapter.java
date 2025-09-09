package com.example.studymate.UserDashboard.Room.FacultyActivity.Quizzes;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.FacultyActivity.QuizDisplay.QuizDisplayActivity;
import com.example.studymate.UserDashboard.Room.StudentActivity.Leaderboard.StudentLeaderBoardActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;

public class FacultyQuizListAdapter extends RecyclerView.Adapter<FacultyQuizListAdapter.MyViewHolder> {

    private List<FacultyQuizModel> quizModelList;
    private Context context;
    private String roomId;
    private String userId;

    // Constructor that accepts context, quiz list, roomId, and userId
    public FacultyQuizListAdapter(Context context, List<FacultyQuizModel> quizModelList, String roomId, String userId) {
        this.context = context;
        this.quizModelList = quizModelList;
        this.roomId = roomId; // Initialize roomId
        this.userId = userId; // Initialize userId
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quiz_item_recycler_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        FacultyQuizModel quizModel = quizModelList.get(position);

        // Set quiz details
        holder.quizTitleText.setText(quizModel.getTitle());
        holder.quizDateTime.setText(quizModel.getDateTime());
        holder.quizTimeText.setText(quizModel.getTime() + " min");

        // Get the quiz ID
        String quizId = quizModel.getId();
        if (quizId == null || quizId.isEmpty()) {
            Log.e("QuizListAdapter", "Quiz ID is null or empty for position: " + position);
            return;
        }

        // Set up quiz completion status handling for cardView
        if (holder.cardView != null) {
            DatabaseReference quizStatusRef = FirebaseDatabase.getInstance()
                    .getReference("Leaderboard")
                    .child(roomId)
                    .child(quizId)
                    .child(userId);

            quizStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        holder.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
                        holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_check_circle_24));
                        holder.quizTimeText.setText("Done");
                    } else {
                        holder.cardView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Error checking quiz status. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Set up click listener for leaderboardView
        holder.leaderboardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent leaderboardIntent = new Intent(context, StudentLeaderBoardActivity.class);
                leaderboardIntent.putExtra("ROOM_ID", roomId);
                leaderboardIntent.putExtra("QUIZ_ID", quizId);
                context.startActivity(leaderboardIntent);
            }
        });

        // Bind the quiz item details
        holder.bind(quizModel);
    }

    @Override
    public int getItemCount() {
        return quizModelList.size();  // Return the total count of items
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView quizTitleText;
        private TextView quizDateTime;
        private TextView quizTimeText;
        private RelativeLayout cardView;
        private ImageView imageView;
        private View leaderboardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            // Initialize the views
            quizTitleText = itemView.findViewById(R.id.quizTitleText);
            quizDateTime = itemView.findViewById(R.id.quizDateTimeText);
            quizTimeText = itemView.findViewById(R.id.quizTimeText);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            leaderboardView = itemView.findViewById(R.id.leaderboardView);

            cardView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }

        public void bind(final FacultyQuizModel model) {
            // Set click listener to open QuizActivity with the quiz details
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Reference the quiz completion data for the user
                    DatabaseReference quizStatusRef = FirebaseDatabase.getInstance()
                            .getReference("Leaderboard")
                            .child(roomId)
                            .child(model.getId()) // quizId
                            .child(userId); // Use userId passed via constructor

                    // Create an intent to open QuizActivity
                    Intent intent = new Intent(context, QuizDisplayActivity.class);
                    intent.putExtra("ROOM_ID", roomId);
                    intent.putExtra("QUIZ_ID", model.getId());
                    // Start the activity
                    context.startActivity(intent);

                }
            });
        }
    }
}
