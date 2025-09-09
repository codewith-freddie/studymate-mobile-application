package com.example.studymate.AdminDashboard.AdminHome.Analytics;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.studymate.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class AdminAnalyticsFragment extends Fragment {

    private TextView roomCountTextView, moduleCountTextView, registeredCountTextView, quizzesCountTextView;
    private DatabaseReference analyticsRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_analytics, container, false);

        roomCountTextView = view.findViewById(R.id.roomCountTextView);
        moduleCountTextView = view.findViewById(R.id.moduleCountTextView);
        registeredCountTextView = view.findViewById(R.id.registeredCountTextView);
        quizzesCountTextView = view.findViewById(R.id.quizzesCountTextView);

        // Reference to the Analytics node in Firebase
        analyticsRef = FirebaseDatabase.getInstance().getReference("Analytics");

        // Fetch and display the counts
        displayAnalyticsCounts();

        return view;
    }

    private void displayAnalyticsCounts() {
        analyticsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get roomCount
                Integer roomCount = snapshot.child("roomCount").getValue(Integer.class);
                roomCountTextView.setText(String.valueOf(roomCount != null ? roomCount : 0));

                // Get moduleCount
                Integer moduleCount = snapshot.child("moduleCount").getValue(Integer.class);
                moduleCountTextView.setText(String.valueOf(moduleCount != null ? moduleCount : 0));

                // Get registeredCount
                Integer registeredCount = snapshot.child("registeredCount").getValue(Integer.class);
                registeredCountTextView.setText(String.valueOf(registeredCount != null ? registeredCount : 0));

                // Get registeredCount
                Integer quizzesCount = snapshot.child("quizzesCount").getValue(Integer.class);
                quizzesCountTextView.setText(String.valueOf(quizzesCount != null ? quizzesCount : 0));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminAnalyticsFragment", "Failed to load analytics counts", error.toException());
            }
        });
    }
}
