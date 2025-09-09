package com.example.studymate.UserDashboard.Room.FacultyActivity.Quizzes;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.studymate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FacultyQuizActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView questionTextview, questionIndicatorTextview, timerIndicatorTextview;
    private ProgressBar questionProgressIndicator;
    private Button btn0, btn1, btn2, btn3, nextBtn;

    private int currentQuestionIndex = 0;
    private String selectedAnswer = "";
    private int score = 0;

    public static List<FacultyQuestionModel> questionModelList;
    public static String time;

    private String userId; // User ID of the currently signed-in user
    private String roomId; // Room ID passed from the calling activity
    private String quizId; // Quiz ID passed from the calling activity

    private String firstName; // User's first name
    private String lastName;  // User's last name
    private String profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid(); // Get the user ID
            fetchUserData(userId); // Fetch the user's first and last name
        } else {
            Toast.makeText(this, "User not signed in. Please log in.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return;
        }

        // Initialize views
        questionTextview = findViewById(R.id.questionTextview);
        questionIndicatorTextview = findViewById(R.id.questionIndicatorTextview);
        timerIndicatorTextview = findViewById(R.id.timerIndicatorTextview);
        questionProgressIndicator = findViewById(R.id.questionProgressIndicator);
        btn0 = findViewById(R.id.btn0);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        nextBtn = findViewById(R.id.next_btn);

        // Set onClick listeners
        btn0.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        nextBtn.setOnClickListener(this);

        // Get the room ID, quiz ID from intent extras
        roomId = getIntent().getStringExtra("ROOM_ID");
        quizId = getIntent().getStringExtra("QUIZ_ID");

        loadQuestions(); // Load the quiz questions
        startTimer(); // Start the quiz timer
    }

    private void fetchUserData(String userId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    firstName = dataSnapshot.child("firstName").getValue(String.class);
                    lastName = dataSnapshot.child("lastName").getValue(String.class);
                    profile = dataSnapshot.child("profileImageUrl").getValue(String.class);

                    Log.d("UserData", "First Name: " + firstName + ", Last Name: " + lastName + ", Profile: " + profile);
                } else {
                    Log.d("UserData", "User data does not exist.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("UserData", "Failed to read user data: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Create a confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit Quiz");
        builder.setMessage("Are you sure you want to quit? Your score will be saved.");

        // If the user confirms quitting, save the score and show the leaderboard
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Save the current score
            saveToLeaderboard(userId, roomId, quizId, score);
            finishQuiz();
        });

        // If the user cancels, dismiss the dialog and stay in the quiz
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        AlertDialog quitDialog = builder.create();
        quitDialog.show();
    }

    private void startTimer() {
        long totalTimeInMillis = Integer.parseInt(time) * 60 * 1000L; // Convert time to milliseconds
        new CountDownTimer(totalTimeInMillis, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                long remainingSeconds = seconds % 60;
                timerIndicatorTextview.setText(String.format("%02d:%02d", minutes, remainingSeconds));
            }

            @Override
            public void onFinish() {
                finishQuiz();
            }
        }.start();
    }

    private void loadQuestions() {
        selectedAnswer = "";
        if (currentQuestionIndex == questionModelList.size()) {
            finishQuiz();
            return;
        }

        // Get the current question
        FacultyQuestionModel currentQuestion = questionModelList.get(currentQuestionIndex);

        // Get the correct answer
        String correctAnswer = currentQuestion.getCorrect(); // Assuming this is the correct answer text

        // Shuffle the options
        List<String> options = currentQuestion.getOptions();
        Collections.shuffle(options); // Shuffle the options list

        // Ensure the correct answer matches the new shuffled options
        if (!options.contains(correctAnswer)) {
            Log.e("QuizActivity", "Correct answer not found in shuffled options.");
            return; // Handle this scenario
        }

        // Update UI
        questionIndicatorTextview.setText("Question " + (currentQuestionIndex + 1) + "/" + questionModelList.size());
        questionProgressIndicator.setProgress((int) ((currentQuestionIndex / (float) questionModelList.size()) * 100));
        questionTextview.setText(currentQuestion.getQuestion());

        // Set the buttons with shuffled options
        btn0.setText(options.get(0));
        btn1.setText(options.get(1));
        btn2.setText(options.get(2));
        btn3.setText(options.get(3));

        // Set the correct answer to the first option to keep track
        if (options.get(0).equals(correctAnswer)) {
            selectedAnswer = correctAnswer; // Track the correct answer for comparison later
        }
    }

    @Override
    public void onClick(View view) {
        resetButtonColors();

        Button clickedBtn = (Button) view;
        if (clickedBtn.getId() == R.id.next_btn) {
            if (selectedAnswer.isEmpty()) {
                Toast.makeText(this, "Please select an answer to continue", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedAnswer.equals(questionModelList.get(currentQuestionIndex).getCorrect())) {
                score++;
                Log.i("Quiz Score", String.valueOf(score));
            }
            currentQuestionIndex++;
            loadQuestions();
        } else {
            selectedAnswer = clickedBtn.getText().toString();
            clickedBtn.setBackgroundColor(getResources().getColor(R.color.green));
        }
    }

    private void resetButtonColors() {
        btn0.setBackgroundColor(getResources().getColor(R.color.gray));
        btn1.setBackgroundColor(getResources().getColor(R.color.gray));
        btn2.setBackgroundColor(getResources().getColor(R.color.gray));
        btn3.setBackgroundColor(getResources().getColor(R.color.gray));
    }

    private void finishQuiz() {
        int totalQuestions = questionModelList.size();
        int percentage = (int) ((score / (float) totalQuestions) * 100);

        // Save score to leaderboard
        saveToLeaderboard(userId, roomId, quizId, score);

        // Create a custom AlertDialog for the score dialog
        View dialogView = getLayoutInflater().inflate(R.layout.score_dialog, null);
        TextView scoreTitle = dialogView.findViewById(R.id.scoreTitle);
        TextView scoreSubtitle = dialogView.findViewById(R.id.scoreSubtitle);
        ProgressBar scoreProgressIndicator = dialogView.findViewById(R.id.scoreProgressIndicator);
        TextView scoreProgressText = dialogView.findViewById(R.id.scoreProgressText);
        Button finishBtn = dialogView.findViewById(R.id.finishBtn);

        scoreProgressIndicator.setProgress(percentage);
        scoreProgressText.setText(percentage + " %");

        if (percentage > 60) {
            scoreTitle.setText("Congrats! You have passed");
            scoreTitle.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            scoreTitle.setText("Oops! You have failed");
            scoreTitle.setTextColor(ContextCompat.getColor(this, R.color.red));
        }

        scoreSubtitle.setText(score + " out of " + totalQuestions + " are correct");

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setCancelable(false)
                .show();
    }

    private void saveToLeaderboard(String userId, String roomId, String quizId, int score) {
        // Log values to check for null
        Log.d("Leaderboard", "UserId: " + userId + ", RoomId: " + roomId + ", QuizId: " + quizId + ", Score: " + score);

        // Check for null values individually
        if (userId == null) {
            Log.e("Leaderboard", "UserId is null");
            Toast.makeText(this, "Error saving score: User ID is null", Toast.LENGTH_SHORT).show();
            return; // Exit early to avoid null reference issues
        }

        if (roomId == null) {
            Log.e("Leaderboard", "RoomId is null");
            Toast.makeText(this, "Error saving score: Room ID is null", Toast.LENGTH_SHORT).show();
            return; // Exit early to avoid null reference issues
        }

        if (quizId == null) {
            Log.e("Leaderboard", "QuizId is null");
            Toast.makeText(this, "Error saving score: Quiz ID is null", Toast.LENGTH_SHORT).show();
            return; // Exit early to avoid null reference issues
        }

        // Save score to Firebase under the leaderboard node
        DatabaseReference leaderboardRef = FirebaseDatabase.getInstance().getReference("Leaderboard")
                .child(roomId)
                .child(quizId)
                .child(userId);

        HashMap<String, Object> scoreData = new HashMap<>();
        scoreData.put("firstName", firstName != null ? firstName : "Unknown"); // Use "Unknown" if firstName is null
        scoreData.put("lastName", lastName != null ? lastName : "Unknown"); // Use "Unknown" if lastName is null
        scoreData.put("profileImageUrl", profile != null ? profile : "Unknown");
        scoreData.put("score", score);

        leaderboardRef.setValue(scoreData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(FacultyQuizActivity.this, "Score saved to leaderboard!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FacultyQuizActivity.this, "Error saving score to leaderboard.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
