package com.example.studymate.UserDashboard.Room.FacultyActivity.Quiz;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.Notification.DialogUtil;
import com.example.studymate.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateQuizActivity extends AppCompatActivity {
    private String quizID, quizTitle, roomID, quizDateTime;
    private int examineeCount;
    private QuizAdapter quizAdapter;
    private List<QuizQuestion> quizQuestions;
    private int quizDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        quizQuestions = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.listview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        quizAdapter = new QuizAdapter(quizQuestions, getResources().getColor(R.color.green), recyclerView);
        recyclerView.setAdapter(quizAdapter);

        retrieveIntentData();
        setupUI();
    }

    private void retrieveIntentData() {
        // Retrieve the passed data from the intent
        roomID = getIntent().getStringExtra("RoomID");
        quizID = getIntent().getStringExtra("QuizID");
        quizTitle = getIntent().getStringExtra("QuizTitle");
        quizDuration = getIntent().getIntExtra("QuizDuration", 0);

        // Use the retrieved data as needed
        // For example, set the quiz title in a TextView
        TextView quizTitleView = findViewById(R.id.title);
        quizTitleView.setText(quizTitle);
    }

    private void setupUI() {
        Button submitButton = findViewById(R.id.submitQuiz);
        submitButton.setOnClickListener(v -> submitQuiz());

        ImageButton addCardviewButton = findViewById(R.id.addCardview);
        addCardviewButton.setOnClickListener(v -> addNewQuestion());
    }

    private void addNewQuestion() {
        QuizQuestion newQuestion = new QuizQuestion();
        quizQuestions.add(newQuestion);
        quizAdapter.notifyItemInserted(quizQuestions.size() - 1);
    }

    private void submitQuiz() {
        List<Integer> invalidPositions = quizAdapter.validateFields();
        if (invalidPositions.isEmpty()) {
            saveQuizToFirebase();
        } else {
            highlightInvalidFields(invalidPositions);
            Toast.makeText(this, "Please fill in all required fields correctly.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQuizToFirebase() {
        DatabaseReference quizRef = FirebaseDatabase.getInstance().getReference("Quizzes")
                .child(roomID).child(quizID);

        quizRef.child("id").setValue(quizID);
        quizRef.child("title").setValue(quizTitle);

        // Get the current time and format it as desired
        long timestamp = System.currentTimeMillis(); // Get current time in milliseconds
        String formattedDateTime = new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.getDefault()).format(new Date(timestamp));

        quizRef.child("dateTime").setValue(formattedDateTime); // Save formatted dateTime
        quizRef.child("time").setValue(quizDuration);
        quizRef.child("examineeCount").setValue(0);

        // Save the same timestamp as well
        quizRef.child("timestamp").setValue(timestamp); // Save the timestamp

        saveQuestionsToFirebase(quizRef);
    }

    private void saveQuestionsToFirebase(DatabaseReference quizRef) {
        // Create a variable to keep track of the number of successful saves
        final int[] successfulSaves = {0};

        for (int i = 0; i < quizQuestions.size(); i++) {
            QuizQuestion question = quizQuestions.get(i);
            if (isQuestionValid(question)) {  // Check if the question is valid
                // Map the question to a structure before saving
                int finalI = i;
                quizRef.child("questionList").child(String.valueOf(i)).setValue(mapQuestionToStructure(question))
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                successfulSaves[0]++;
                                // Optionally handle success for each question
                            } else {
                                Toast.makeText(this, "Failed to save question at position: " + finalI + " - " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            // After all questions have been processed, check if all were successful
                            if (successfulSaves[0] == quizQuestions.size()) {
                                Toast.makeText(this, "Quiz saved successfully!", Toast.LENGTH_SHORT).show();
                                finish(); // Finish the activity
                            }
                        });
            } else {
                Toast.makeText(this, "Invalid question at position: " + i, Toast.LENGTH_SHORT).show();
            }
        }

        // Final Toast after attempting to save all questions
        // This toast can be misleading if not all questions are saved successfully.
        // You might want to consider moving this to after checking for all successful saves.
    }

    // Method to check if a QuizQuestion is valid
    private boolean isQuestionValid(QuizQuestion question) {
        return question != null &&
                question.getQuestion() != null && !question.getQuestion().trim().isEmpty() &&
                question.getOptions() != null && question.getOptions().size() >= 2; // Adjust the number of options required
    }

    // Method to map the QuizQuestion to the desired structure
    private Map<String, Object> mapQuestionToStructure(QuizQuestion question) {
        Map<String, Object> questionData = new HashMap<>();
        questionData.put("question", question.getQuestion());
        questionData.put("options", question.getOptions());
        questionData.put("correct", question.getCorrect()); // Ensure this field is set correctly
        return questionData;
    }

    private void highlightInvalidFields(List<Integer> invalidPositions) {
        for (int pos : invalidPositions) {
            quizAdapter.notifyItemChanged(pos);
        }
    }
}
