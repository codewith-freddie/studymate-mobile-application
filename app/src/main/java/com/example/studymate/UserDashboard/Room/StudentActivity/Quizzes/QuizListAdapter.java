package com.example.studymate.UserDashboard.Room.StudentActivity.Quizzes;

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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.StudentActivity.Leaderboard.StudentLeaderBoardActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;

public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.MyViewHolder> {

    private List<QuizModel> quizModelList;
    private Context context;
    private String roomId;
    private String userId;

    // Constructor that accepts context, quiz list, roomId, and userId
    public QuizListAdapter(Context context, List<QuizModel> quizModelList, String roomId, String userId) {
        this.context = context;
        this.quizModelList = quizModelList;
        this.roomId = roomId;
        this.userId = userId;
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
        QuizModel quizModel = quizModelList.get(position);

        holder.quizTitleText.setText(quizModel.getTitle());
        holder.quizDateTime.setText(quizModel.getDateTime());
        holder.quizTimeText.setText(quizModel.getTime() + " min");

        // Check quiz completion status
        checkQuizCompletionStatus(holder, quizModel);

        // Bind the quiz model to the holder
        holder.bind(quizModel);
    }

    @Override
    public int getItemCount() {
        return quizModelList.size();
    }

    private void checkQuizCompletionStatus(final MyViewHolder holder, QuizModel quizModel) {
        DatabaseReference quizStatusRef = FirebaseDatabase.getInstance()
                .getReference("Leaderboard")
                .child(roomId)
                .child(quizModel.getId())
                .child(userId);

        quizStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User has taken the quiz
                    holder.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
                    holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_check_circle_24));
                    holder.quizTimeText.setText("Done");
                } else {
                    // User has not taken the quiz
                    holder.cardView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Error checking quiz status. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView quizTitleText;
        private TextView quizDateTime;
        private TextView quizTimeText;
        private RelativeLayout cardView;
        private ImageView imageView;
        private View leaderboardView; // Define leaderboardView here

        public MyViewHolder(View itemView) {
            super(itemView);
            quizTitleText = itemView.findViewById(R.id.quizTitleText);
            quizDateTime = itemView.findViewById(R.id.quizDateTimeText);
            quizTimeText = itemView.findViewById(R.id.quizTimeText);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            leaderboardView = itemView.findViewById(R.id.leaderboardView); // Initialize leaderboardView

            // Default background for each quiz item
            cardView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }

        public void bind(final QuizModel model) {
            itemView.setOnClickListener(v -> {
                DatabaseReference quizStatusRef = FirebaseDatabase.getInstance()
                        .getReference("Leaderboard")
                        .child(roomId)
                        .child(model.getId())
                        .child(userId);

                quizStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(context, "You have already taken this quiz.", Toast.LENGTH_SHORT).show();
                        } else {
                            openQuizActivity(model);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, "Error checking quiz status. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            // Leaderboard click listener
            leaderboardView.setOnClickListener(v -> {
                Intent leaderboardIntent = new Intent(context, StudentLeaderBoardActivity.class);
                leaderboardIntent.putExtra("ROOM_ID", roomId);
                leaderboardIntent.putExtra("QUIZ_ID", model.getId());
                context.startActivity(leaderboardIntent);
            });
        }

        private void openQuizActivity(QuizModel model) {
            Intent intent = new Intent(context, QuizActivity.class);

            // Shuffle the quiz questions before passing
            List<QuestionModel> shuffledQuestions = model.getQuestionList();
            Collections.shuffle(shuffledQuestions);

            // Pass the shuffled questions and quiz time to QuizActivity
            QuizActivity.questionModelList = shuffledQuestions;
            QuizActivity.time = String.valueOf(model.getTime());

            // Pass additional data
            intent.putExtra("ROOM_ID", roomId);
            intent.putExtra("QUIZ_ID", model.getId());

            context.startActivity(intent);
        }
    }
}
