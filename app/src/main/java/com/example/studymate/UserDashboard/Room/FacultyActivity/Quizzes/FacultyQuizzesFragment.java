package com.example.studymate.UserDashboard.Room.FacultyActivity.Quizzes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FacultyQuizzesFragment extends Fragment {
    private static final String ARG_ROOM_ID = "ROOM_ID";
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private List<FacultyQuizModel> quizModelList;
    private FacultyQuizListAdapter adapter;
    private ImageView backButton;

    public static FacultyQuizzesFragment newInstance(String roomId) {
        FacultyQuizzesFragment fragment = new FacultyQuizzesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quizzes_fragment, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recyclerView);
        backButton = view.findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back button click
                getActivity().onBackPressed(); // Go back to the previous activity or fragment
            }
        });
        quizModelList = new ArrayList<>();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dataFromFirebase();
    }

    private void setupRecyclerView() {
        String roomId = getArguments().getString(ARG_ROOM_ID);

        // Get the current user ID from FirebaseAuth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;

        progressBar.setVisibility(View.GONE);
        adapter = new FacultyQuizListAdapter(getContext(), quizModelList, roomId, userId);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void dataFromFirebase() {
        String roomId = getArguments().getString(ARG_ROOM_ID);
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference quizzesRef = FirebaseDatabase.getInstance()
                .getReference("Quizzes")
                .child(roomId);

        quizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quizModelList.clear();
                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    FacultyQuizModel facultyQuizModel = quizSnapshot.getValue(FacultyQuizModel.class);
                    if (facultyQuizModel != null) {
                        quizModelList.add(facultyQuizModel);
                    }
                }

                // Sort the quizModelList by timestamp in ascending order
                quizModelList.sort((quiz1, quiz2) -> Long.compare(quiz1.getTimestamp(), quiz2.getTimestamp())); // Sort in ascending order

                if (quizModelList.isEmpty()) {
                    Toast.makeText(getContext(), "No quizzes available.", Toast.LENGTH_SHORT).show();
                } else {
                    setupRecyclerView();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load quizzes: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
