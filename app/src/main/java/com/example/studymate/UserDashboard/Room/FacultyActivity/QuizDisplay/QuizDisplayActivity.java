package com.example.studymate.UserDashboard.Room.FacultyActivity.QuizDisplay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.util.List;

public class QuizDisplayActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuizQuestionDisplayAdapter adapter;
    private List<QuizQuestionDisplay> quizQuestions;
    private DatabaseReference databaseReference;
    private TextView toolbarTitle; // Added toolbarTitle reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_display);

        // Retrieve the roomId and quizId from the Intent
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");
        String quizId = intent.getStringExtra("QUIZ_ID");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewQuizQuestions);
        quizQuestions = new ArrayList<>();

        // Firebase database reference for quiz questions
        databaseReference = FirebaseDatabase.getInstance().getReference("Quizzes").child(roomId).child(quizId).child("questionList");

        // Set up RecyclerView with the adapter
        adapter = new QuizQuestionDisplayAdapter(quizQuestions, roomId, quizId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize Toolbar and toolbar title TextView
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle); // Initialize toolbarTitle
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Closes the activity when backButton is clicked

        // Fetch quiz title from Firebase and set it as toolbar title
        fetchQuizTitle(roomId, quizId);

        // Fetch quiz questions from Firebase
        fetchQuestions();
    }

    private void fetchQuizTitle(String roomId, String quizId) {
        DatabaseReference titleReference = FirebaseDatabase.getInstance()
                .getReference("Quizzes")
                .child(roomId)
                .child(quizId)
                .child("title"); // Adjust if your title path is different

        titleReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String quizTitle = snapshot.getValue(String.class);
                if (quizTitle != null) {
                    toolbarTitle.setText(quizTitle); // Set toolbar title on TextView
                } else {
                    Log.e("QuizDisplayActivity", "Quiz title is missing");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuizDisplayActivity.this, "Failed to load quiz title", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchQuestions() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quizQuestions.clear();
                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    String questionText = questionSnapshot.child("question").getValue(String.class);
                    String correctAnswer = questionSnapshot.child("correct").getValue(String.class);

                    List<String> options = new ArrayList<>();
                    for (DataSnapshot optionSnapshot : questionSnapshot.child("options").getChildren()) {
                        options.add(optionSnapshot.getValue(String.class));
                    }

                    QuizQuestionDisplay question = new QuizQuestionDisplay(questionText, options, correctAnswer);
                    quizQuestions.add(question);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuizDisplayActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
