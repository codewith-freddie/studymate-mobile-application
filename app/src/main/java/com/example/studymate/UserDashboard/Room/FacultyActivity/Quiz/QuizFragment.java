package com.example.studymate.UserDashboard.Room.FacultyActivity.Quiz;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.studymate.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class QuizFragment extends Fragment {

    private static final String ARG_ROOM_ID = "ROOM_ID";
    private String roomId;
    private RecyclerView quizRecyclerView;
    private DisplayQuizAdapter displayQuizAdapter;
    private List<Quiz> quizList;
    private DatabaseReference quizRef;

    // Factory method to create a new instance of QuizFragment with the roomId
    public static QuizFragment newInstance(String roomId) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        quizRecyclerView = view.findViewById(R.id.quizRecyclerView);
        quizRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        quizList = new ArrayList<>();


        // Check if roomId is provided in arguments
        if (getArguments() != null && getArguments().getString(ARG_ROOM_ID) != null) {
            roomId = getArguments().getString(ARG_ROOM_ID);
        } else {
            // Room ID is missing, show a toast message
            Toast.makeText(getContext(), "Room ID is missing", Toast.LENGTH_SHORT).show();
            return view; // Return early to avoid fetching quizzes without a valid roomId
        }

        // Initialize the adapter with the quiz list and roomId
        displayQuizAdapter = new DisplayQuizAdapter(quizList, roomId);
        quizRecyclerView.setAdapter(displayQuizAdapter);

        // Reference to the quizzes node in Firebase using the roomId
        quizRef = FirebaseDatabase.getInstance().getReference("Quizzes").child(roomId);

        // Fetch quiz data from Firebase
        fetchQuizzes();

        return view;
    }

    private void fetchQuizzes() {
        quizRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quizList.clear(); // Clear the list before adding new data
                for (DataSnapshot quizSnapshot : snapshot.getChildren()) {
                    // Extract quiz ID
                    String quizId = quizSnapshot.getKey();

                    // Extract quiz details
                    String quizTitle = quizSnapshot.child("quizTitle").getValue(String.class);
                    String dateTime = quizSnapshot.child("dateTime").getValue(String.class);

                    // Create a new Quiz object and add it to the list with default values
                    Quiz quiz = new Quiz(quizId, quizTitle, 0, 0, new ArrayList<>(), dateTime);
                    quizList.add(quiz);
                }
                displayQuizAdapter.notifyDataSetChanged(); // Notify adapter about data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }
}
