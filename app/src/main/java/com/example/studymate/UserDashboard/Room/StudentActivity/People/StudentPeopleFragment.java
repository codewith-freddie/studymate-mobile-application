package com.example.studymate.UserDashboard.Room.StudentActivity.People;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.FacultyActivity.People.UserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StudentPeopleFragment extends Fragment implements StudentPeopleAdapter.OnRemoveUserClickListener {

    private RecyclerView recyclerView;
    private StudentPeopleAdapter adapter;
    private List<UserInfo> userInfoList;
    private List<UserInfo> filteredUserList;
    private DatabaseReference databaseReference, databaseref;
    private String roomId;
    private TextView accessCodeTextView, roomTitleTextView, roomDescTextView, userCountTextView, nameTextView;
    private TextView info1, info2, info3, info4;
    private LinearLayout linearContainer;
    private CardView cardviewContainer;
    private ImageView userImageView;
    private boolean isLinearContainerVisible = false;

    public StudentPeopleFragment() {
        // Default constructor
    }

    public static StudentPeopleFragment newInstance(String roomId) {
        StudentPeopleFragment fragment = new StudentPeopleFragment();
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

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.student_people_fragment, container, false);

        accessCodeTextView = view.findViewById(R.id.desAccessCode);
        roomTitleTextView = view.findViewById(R.id.desRoomTitle);
        roomDescTextView = view.findViewById(R.id.desRoomDesc);
        userCountTextView = view.findViewById(R.id.desUserCount);

        info1 = view.findViewById(R.id.info1);
        info2 = view.findViewById(R.id.info2);
        info3 = view.findViewById(R.id.info3);
        info4 = view.findViewById(R.id.info4);

        nameTextView = view.findViewById(R.id.nameTextView);
        userImageView = view.findViewById(R.id.userImageView);

        linearContainer = view.findViewById(R.id.linearContainer);
        cardviewContainer = view.findViewById(R.id.cardviewContainer);

        cardviewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLinearContainer();
            }
        });

        // Use view instead of android.R.id.content
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isLinearContainerVisible) {
                    hideLinearContainer();
                    return true; // Indicate that we have handled this touch event
                }
                return false;
            }
        });

        // Rest of your code...
        if (getArguments() != null) {
            roomId = getArguments().getString("ROOM_ID");
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        ImageView backButton = view.findViewById(R.id.backButton);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

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
        adapter = new StudentPeopleAdapter(filteredUserList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Rooms");
        databaseref = FirebaseDatabase.getInstance().getReference("Rooms").child(roomId);

        if (roomId != null) {
            fetchUsers(roomId);
        }

        // Back button functionality
        if (backButton != null) {
            backButton.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        loadRoomData();

        return view;
    }


    private void toggleLinearContainer() {
        if (isLinearContainerVisible) {
            hideLinearContainer();
        } else {
            showLinearContainer();
        }
    }

    private void showLinearContainer() {
        linearContainer.setVisibility(View.VISIBLE);
        linearContainer.setTranslationY(-linearContainer.getHeight());
        linearContainer.animate()
                .translationY(0)
                .setDuration(300)
                .start();
        isLinearContainerVisible = true;
    }

    private void hideLinearContainer() {
        linearContainer.animate()
                .translationY(-linearContainer.getHeight())
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        linearContainer.setVisibility(View.GONE);
                    }
                })
                .start();
        isLinearContainerVisible = false;
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

    private void copyAccessCode() {
        if (roomId != null) {
            // Retrieve the access code from the database
            databaseref.child("accessCode").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String accessCode = task.getResult().getValue(String.class);
                    if (accessCode != null) {
                        // Copy to clipboard
                        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Access Code", accessCode);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getContext(), "Access code copied to clipboard", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Access code not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to retrieve access code", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Room ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareAccessCode() {
        if (roomId != null) {
            // Retrieve the access code from the database
            databaseref.child("accessCode").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String accessCode = task.getResult().getValue(String.class);
                    if (accessCode != null) {
                        // Share the access code
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Here is the access code: " + accessCode);
                        shareIntent.setType("text/plain");
                        startActivity(Intent.createChooser(shareIntent, "Share Access Code"));
                    } else {
                        Toast.makeText(getContext(), "Access code not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to retrieve access code", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Room ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void leaveRoom() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user's ID
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("Rooms").child(roomId);
        DatabaseReference participantsRef = roomRef.child("participants");
        DatabaseReference personCountRef = roomRef.child("personCount"); // Reference to personCount

        // Remove the user from participants
        participantsRef.child(currentUserId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // First, retrieve the current value of personCount
                personCountRef.get().addOnCompleteListener(countTask -> {
                    if (countTask.isSuccessful()) {
                        Long currentCount = countTask.getResult().getValue(Long.class);
                        if (currentCount != null && currentCount > 0) {
                            // Decrement personCount by 1 and update it in the database
                            personCountRef.setValue(currentCount - 1).addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Toast.makeText(getContext(), "You have left the room.", Toast.LENGTH_SHORT).show();
                                    // Optional: Redirect the user to another screen after leaving
                                    getActivity().finish(); // Close the current activity or fragment
                                } else {
                                    Toast.makeText(getContext(), "Failed to update person count.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Person count is invalid.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to retrieve person count.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Failed to leave the room.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmLeaveRoom() {
        new AlertDialog.Builder(getContext())
                .setTitle("Leave Room")
                .setMessage("Are you sure you want to leave this room?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    leaveRoom();
                })
                .setNegativeButton("No", null)
                .show();
    }


    private void loadRoomData() {
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("Rooms").child(roomId);
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String accessCode = dataSnapshot.child("accessCode").getValue(String.class);
                    String roomTitle = dataSnapshot.child("dataTitle").getValue(String.class);
                    String roomDesc = dataSnapshot.child("dataDesc").getValue(String.class);
                    String creator = dataSnapshot.child("createdBy").getValue(String.class);
                    Integer userCount = dataSnapshot.child("personCount").getValue(Integer.class);

                    accessCodeTextView.setText(accessCode != null ? accessCode : "N/A");
                    roomTitleTextView.setText(roomTitle != null ? roomTitle : "N/A");
                    roomDescTextView.setText(roomDesc != null ? roomDesc : "N/A");
                    userCountTextView.setText(userCount != null ? userCount.toString() : "0");

                    // Fetch the creator's name asynchronously
                    if (creator != null) {
                        fetchCreatorName(creator); // Call fetchCreatorName with the creator's ID
                    } else {
                        info4.setText("Admin: N/A"); // If creator ID is null
                    }

                    info1.setText("Room Name: " + roomTitle);
                    info2.setText("Description: " + roomDesc);
                    info3.setText("Access Code: " + accessCode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load room data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCreatorName(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    String profileUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                    // Handle null or empty first and last names
                    String creatorName = (firstName != null && !firstName.isEmpty() ? firstName : "") +
                            (lastName != null && !lastName.isEmpty() ? " " + lastName : "");

                    // Update UI
                    info4.setText("Admin: " + creatorName.trim()); // Set the admin name in the appropriate TextView
                    nameTextView.setText(creatorName.trim()); // Update nameTextView

                    // Use an image loading library like Glide to load the profile image
                    if (getActivity() != null && profileUrl != null && !profileUrl.isEmpty()) {
                        Glide.with(getActivity()) // Ensure fragment is attached
                                .load(profileUrl)
                                .placeholder(R.drawable.baseline_person_24) // Add a placeholder image
                                .error(R.drawable.error) // Add an error image
                                .into(userImageView); // Set the loaded image into userImageView
                    } else {
                        userImageView.setImageResource(R.drawable.placeholder); // Set a default image if URL is not available
                    }
                } else {
                    info4.setText("Admin: N/A"); // If user data does not exist
                    nameTextView.setText("N/A");
                    userImageView.setImageResource(R.drawable.placeholder); // Set a default image if no data
                    Toast.makeText(getContext(), "Failed to load creator name.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load creator data.", Toast.LENGTH_SHORT).show();
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Inflate the menu with search and action items
        inflater.inflate(R.menu.settings_menu, menu);

        // Get the search view and set up the listener
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        int color = ContextCompat.getColor(getContext(), R.color.light_gray);
        int red = ContextCompat.getColor(getContext(), R.color.red);
        MenuItem copyCodeItem = menu.findItem(R.id.action_copy_access_code);
        SpannableString copyTitle = new SpannableString(copyCodeItem.getTitle());
        copyTitle.setSpan(new ForegroundColorSpan(color), 0, copyTitle.length(), 0);
        copyCodeItem.setTitle(copyTitle);

        MenuItem shareCodeItem = menu.findItem(R.id.action_share_access_code);
        SpannableString shareTitle = new SpannableString(shareCodeItem.getTitle());
        shareTitle.setSpan(new ForegroundColorSpan(color), 0, shareTitle.length(), 0);
        shareCodeItem.setTitle(shareTitle);

        MenuItem leave = menu.findItem(R.id.action_leave);
        SpannableString leaveTitle = new SpannableString(leave.getTitle());
        leaveTitle.setSpan(new ForegroundColorSpan(red), 0, leaveTitle.length(), 0);
        leave.setTitle(leaveTitle);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Implement your filter logic when search is submitted
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Implement your filter logic as the text changes
                filter(newText);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_copy_access_code) {
            // Call method to copy the access code
            copyAccessCode();
            return true;
        } else if (itemId == R.id.action_share_access_code) {
            // Call method to share the access code
            shareAccessCode();
            return true;
        } else if (itemId == R.id.action_leave) {
            // Call the leave room method
            confirmLeaveRoom();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRemoveUserClick(UserInfo user) {

    }
}
