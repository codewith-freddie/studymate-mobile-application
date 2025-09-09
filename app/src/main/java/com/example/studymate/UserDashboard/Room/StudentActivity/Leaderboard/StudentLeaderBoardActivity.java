package com.example.studymate.UserDashboard.Room.StudentActivity.Leaderboard;

import static android.app.PendingIntent.getActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studymate.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StudentLeaderBoardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentLeaderBoardAdapter studentleaderboardAdapter;
    private List<UserScore> userScoreList;
    private DatabaseReference leaderboardRef;

    private TextView tvRank1Name, tvRank1Score, tvRank2Name, tvRank2Score, tvRank3Name, tvRank3Score;
    private ShapeableImageView ivRank1, ivRank2, ivRank3;

    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_leaderboard);

        // Initialize views for top 3 leaderboard display
        tvRank1Name = findViewById(R.id.tvRank1Name);
        tvRank1Score = findViewById(R.id.tvRank1score);
        tvRank2Name = findViewById(R.id.tvRank2Name);
        tvRank2Score = findViewById(R.id.tvRank2score);
        tvRank3Name = findViewById(R.id.tvRank3Name);
        tvRank3Score = findViewById(R.id.tvRank3score);
        ivRank1 = findViewById(R.id.ivRank1);
        ivRank2 = findViewById(R.id.ivRank2);
        ivRank3 = findViewById(R.id.ivRank3);

        // Initialize RecyclerView and Adapter for leaderboard
        recyclerView = findViewById(R.id.leaderBoard_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userScoreList = new ArrayList<>();
        studentleaderboardAdapter = new StudentLeaderBoardAdapter(userScoreList);
        recyclerView.setAdapter(studentleaderboardAdapter);

        backButton = this.findViewById(R.id.backButton);

        // Initialize Firebase Database reference
        String roomId = getIntent().getStringExtra("ROOM_ID");
        String quizId = getIntent().getStringExtra("QUIZ_ID");

        if (roomId == null) {
            Log.e("Leaderboard", "RoomId is null");
            Toast.makeText(this, "Error saving score: Room ID is null", Toast.LENGTH_SHORT).show();
            return; // Exit early to avoid null reference issues
        } else if (quizId == null) {
            Log.e("Leaderboard", "QuizId is null");
            Toast.makeText(this, "Error saving score: Quiz ID is null", Toast.LENGTH_SHORT).show();
            return; // Exit early to avoid null reference issues
        } else {
            leaderboardRef = FirebaseDatabase.getInstance().getReference("Leaderboard").child(roomId).child(quizId);
        }

        // Set OnClickListener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Go back to the previous activity
            }
        });

        fetchLeaderboardData();
    }

    private void fetchLeaderboardData() {
        leaderboardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userScoreList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String firstName = userSnapshot.child("firstName").getValue(String.class);
                    String lastName = userSnapshot.child("lastName").getValue(String.class);
                    Long score = userSnapshot.child("score").getValue(Long.class);
                    String profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String.class);

                    if (firstName != null && lastName != null && score != null) {
                        userScoreList.add(new UserScore(firstName, lastName, score, profileImageUrl));
                    }
                }

                // Sort users by score in descending order
                Collections.sort(userScoreList, new Comparator<UserScore>() {
                    @Override
                    public int compare(UserScore u1, UserScore u2) {
                        return Long.compare(u2.getScore(), u1.getScore());
                    }
                });

                // Display the top 3 users if available
                if (userScoreList.size() > 0) {
                    UserScore top1 = userScoreList.get(0);
                    tvRank1Name.setText(top1.getFirstName() + " " + top1.getLastName());
                    tvRank1Score.setText(String.valueOf(top1.getScore()));
                    loadProfileImage(top1.getProfileImageUrl(), ivRank1);
                }
                if (userScoreList.size() > 1) {
                    UserScore top2 = userScoreList.get(1);
                    tvRank2Name.setText(top2.getFirstName() + " " + top2.getLastName());
                    tvRank2Score.setText(String.valueOf(top2.getScore()));
                    loadProfileImage(top2.getProfileImageUrl(), ivRank2);
                }
                if (userScoreList.size() > 2) {
                    UserScore top3 = userScoreList.get(2);
                    tvRank3Name.setText(top3.getFirstName() + " " + top3.getLastName());
                    tvRank3Score.setText(String.valueOf(top3.getScore()));
                    loadProfileImage(top3.getProfileImageUrl(), ivRank3);
                }

                // Update RecyclerView with remaining users only (starting from index 3)
                if (userScoreList.size() > 3) {
                    List<UserScore> remainingUsers = userScoreList.subList(3, userScoreList.size());
                    studentleaderboardAdapter = new StudentLeaderBoardAdapter(remainingUsers);
                    recyclerView.setAdapter(studentleaderboardAdapter);
                } else {
                    recyclerView.setAdapter(null); // Clear RecyclerView if less than 3 users
                }


                // Update RecyclerView with remaining users
                studentleaderboardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentLeaderBoardActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e("StudentLeaderBoardActivity", "onCancelled", error.toException());
            }
        });
    }

    private void loadProfileImage(String url, ShapeableImageView imageView) {
        if (url != null && !url.isEmpty()) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.profile)  // Default image in case of a missing image
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.profile); // Set default image if URL is empty
        }
    }
}