package com.example.studymate.UserDashboard.Room.FacultyActivity.People;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.Network.NetworkUtils;
import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment implements PeopleAdapter.OnRemoveUserClickListener {

    private RecyclerView recyclerView;
    private PeopleAdapter adapter;
    private List<UserInfo> userInfoList;
    private List<UserInfo> filteredUserList;
    private DatabaseReference databaseReference;
    private String roomId;

    public PeopleFragment() {
        // Default constructor
    }

    public static PeopleFragment newInstance(String roomId) {
        PeopleFragment fragment = new PeopleFragment();
        Bundle args = new Bundle();
        args.putString("ROOM_ID", roomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.faculty_people_fragment, container, false);

        if (getArguments() != null) {
            roomId = getArguments().getString("ROOM_ID");
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        ImageView backButton = view.findViewById(R.id.backButton);
        Toolbar toolbar = view.findViewById(R.id.toolbar);  // Corrected toolbar reference

        // Set up toolbar
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        userInfoList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
        adapter = new PeopleAdapter(filteredUserList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Rooms");

        if (roomId != null) {
            fetchUsers(roomId);
        }

        // Back button functionality
        if (backButton != null) {
            backButton.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        return view;
    }

    private void fetchUsers(String roomId) {
        DatabaseReference participantsRef = databaseReference.child(roomId).child("participants");

        participantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfoList.clear();
                for (DataSnapshot participantSnapshot : snapshot.getChildren()) {
                    String userId = participantSnapshot.getKey();
                    fetchUserDetails(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load participants.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDetails(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserInfo userInfo = snapshot.getValue(UserInfo.class);
                if (userInfo != null) {
                    userInfo.setUserId(userId);
                    userInfoList.add(userInfo);
                    filter(""); // Reapply filter to include new user
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load user details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filter(String text) {
        filteredUserList.clear();
        if (TextUtils.isEmpty(text)) {
            filteredUserList.addAll(userInfoList);
        } else {
            String lowerCaseText = text.toLowerCase();
            for (UserInfo user : userInfoList) {
                String fullName = user.getFullName().toLowerCase();
                if (fullName.contains(lowerCaseText)) {
                    filteredUserList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRemoveUserClick(UserInfo user) {
        if (NetworkUtils.isConnected(requireContext())) {
            if (user.getUserId() == null) {
                Toast.makeText(getContext(), "User ID is null", Toast.LENGTH_SHORT).show();
                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Remove User")
                    .setMessage("Are you sure you want to remove this user?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (roomId == null) {
                            Toast.makeText(getContext(), "Room ID is null", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("Rooms").child(roomId);
                        DatabaseReference roomParticipantsRef = roomRef.child("participants");

                        roomParticipantsRef.child(user.getUserId()).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    roomRef.child("personCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                long personCount = snapshot.getValue(Long.class);
                                                if (personCount > 0) {
                                                    roomRef.child("personCount").setValue(personCount - 1)
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                Toast.makeText(getContext(), "User removed successfully", Toast.LENGTH_SHORT).show();
                                                                fetchUsers(roomId);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(getContext(), "Failed to update person count.", Toast.LENGTH_SHORT).show();
                                                            });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), "Failed to fetch person count.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to remove user.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            ToastUtils.showCustomToast(requireContext(), "No Internet Connection");
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu); // Correct way to inflate menu

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Handle search query
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
}
