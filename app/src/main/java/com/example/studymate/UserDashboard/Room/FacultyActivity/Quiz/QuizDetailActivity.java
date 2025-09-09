package com.example.studymate.UserDashboard.Room.FacultyActivity.Quiz;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuestionAdapter questionAdapter;
    private List<Question> questionList;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_detail);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionList = new ArrayList<>();
        questionAdapter = new QuestionAdapter(questionList);
        recyclerView.setAdapter(questionAdapter);

        // Set up the toolbar
        setSupportActionBar(toolbar);

        // Get roomId, quizId, and quizTitle from the intent
        String roomId = getIntent().getStringExtra("roomId");
        String quizId = getIntent().getStringExtra("quizId");
        String quizTitle = getIntent().getStringExtra("quizTitle");

        // Check if roomId or quizId is null
        if (roomId == null || quizId == null) {
            Toast.makeText(this, "Room ID or Quiz ID is missing!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity since the necessary data is missing
            return;
        }

        // Set the toolbar title to the quiz title
        if (quizTitle != null) {
            getSupportActionBar().setTitle(quizTitle);  // Set title instead of app name
        }

        // Enable the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Fetch data from Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Quizzes")
                .child(roomId)
                .child(quizId)
                .child("questions");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                questionList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Question question = snapshot.getValue(Question.class);
                    questionList.add(question);
                }

                // Shuffle the question list to randomize the order
                Collections.shuffle(questionList);

                // Notify the adapter that the data has changed
                questionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
                Toast.makeText(QuizDetailActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Handle the back button click
        return true;
    }
}
