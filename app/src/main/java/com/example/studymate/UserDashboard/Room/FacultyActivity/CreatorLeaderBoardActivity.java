package com.example.studymate.UserDashboard.Room.FacultyActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.StudentActivity.Leaderboard.UserScore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CreatorLeaderBoardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CreatorLeaderboardAdapter creatorleaderboardAdapter;
    private List<UserScore> userScoreList;
    private DatabaseReference leaderboardRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_creator_leaderboard);

        // Initialize RecyclerView and Adapter for leaderboard
        recyclerView = findViewById(R.id.leaderBoard_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userScoreList = new ArrayList<>();
        creatorleaderboardAdapter = new CreatorLeaderboardAdapter(userScoreList);
        recyclerView.setAdapter(creatorleaderboardAdapter);

        // Initialize Firebase Database reference
        leaderboardRef = FirebaseDatabase.getInstance().getReference("Leaderboard");

        // Fetch leaderboard data for a specific room and quiz
        String roomId = "uZ6atHAY"; // Replace with the actual room ID you want to display
        String quizId = "67c23d10-37d8-494f-80c0-096cc7612c13"; // Replace with the actual quiz ID you want to display

        fetchLeaderboardData(roomId, quizId);
    }

    private void fetchLeaderboardData(String roomId, String quizId) {
        leaderboardRef.child(roomId).child(quizId).addListenerForSingleValueEvent(new ValueEventListener() {
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


                // Update RecyclerView with remaining users only (starting from index 3)
                if (userScoreList.size() > 0) {
                    List<UserScore> remainingUsers = userScoreList.subList(0, userScoreList.size());
                    creatorleaderboardAdapter = new CreatorLeaderboardAdapter(remainingUsers);
                    recyclerView.setAdapter(creatorleaderboardAdapter);
                } else {
                    recyclerView.setAdapter(null);
                }


                // Update RecyclerView with remaining users
                creatorleaderboardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreatorLeaderBoardActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e("CreatorLeaderBoardActivity", "onCancelled", error.toException());
            }
        });
    }
}