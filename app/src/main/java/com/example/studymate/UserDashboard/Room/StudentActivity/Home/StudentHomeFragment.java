package com.example.studymate.UserDashboard.Room.StudentActivity.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StudentHomeFragment extends Fragment {
    private String roomId;
    private RecyclerView combinedRecyclerView;
    private StudentHomeAdapter homeAdapter;
    private List<ListItem> itemList = new ArrayList<>();
    private TextView accessCodeTextView, roomTitleTextView, roomDescTextView, userCountTextView;
    private ImageView backButton;

    public static StudentHomeFragment newInstance(String roomId) {
        StudentHomeFragment fragment = new StudentHomeFragment();
        Bundle args = new Bundle();
        args.putString("ROOM_ID", roomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomId = getArguments().getString("ROOM_ID");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);


        accessCodeTextView = view.findViewById(R.id.desAccessCode);
        roomTitleTextView = view.findViewById(R.id.desRoomTitle);
        roomDescTextView = view.findViewById(R.id.desRoomDesc);
        userCountTextView = view.findViewById(R.id.desUserCount);
        combinedRecyclerView = view.findViewById(R.id.combinedRecyclerView);
        backButton = view.findViewById(R.id.backButton);
        combinedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize CombinedAdapter with Context, itemList, and roomId
        homeAdapter = new StudentHomeAdapter(getContext(), itemList, roomId);
        combinedRecyclerView.setAdapter(homeAdapter);

        // Set OnClickListener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back button click
                getActivity().onBackPressed(); // Go back to the previous activity or fragment
            }
        });

        loadRoomData();
        loadData(); // Load both PDFs and announcements together

        return view;
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
                    Integer userCount = dataSnapshot.child("personCount").getValue(Integer.class);

                    accessCodeTextView.setText(accessCode != null ? accessCode : "N/A");
                    roomTitleTextView.setText(roomTitle != null ? roomTitle : "N/A");
                    roomDescTextView.setText(roomDesc != null ? roomDesc : "N/A");
                    userCountTextView.setText(userCount != null ? userCount.toString() : "0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load room data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        DatabaseReference moduleRef = FirebaseDatabase.getInstance().getReference("Home").child(roomId);
        moduleRef.orderByChild("dateTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear(); // Clear the list before adding new data

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String type = snapshot.child("type").getValue(String.class);

                    if ("pdf".equals(type)) {
                        PdfInfo pdfInfo = snapshot.getValue(PdfInfo.class);
                        if (pdfInfo != null) {
                            itemList.add(pdfInfo); // Add PDF to combined list
                        }
                    } else if ("announcement".equals(type)) {
                        Announcement announcement = snapshot.getValue(Announcement.class);
                        if (announcement != null) {
                            itemList.add(announcement); // Add announcement to combined list
                        }
                    } else if ("syllabus".equals(type)) { // Add handling for syllabus
                        StudentSyllabusInfo syllabusInfo = snapshot.getValue(StudentSyllabusInfo.class);
                        if (syllabusInfo != null) {
                            itemList.add(syllabusInfo); // Add syllabus to combined list
                        }
                    } else if ("link".equals(type)) { // Add handling for link
                        LinkInfo linkInfo = snapshot.getValue(LinkInfo.class);
                        if (linkInfo != null) {
                            itemList.add(linkInfo); // Add link to combined list
                        }
                    }
                }

                // Convert dateTime values to timestamps for sorting
                Collections.sort(itemList, new Comparator<ListItem>() {
                    @Override
                    public int compare(ListItem o1, ListItem o2) {
                        long timestamp1 = o1.getTimestamp();
                        long timestamp2 = o2.getTimestamp();
                        return Long.compare(timestamp1, timestamp2); // Ascending order
                    }
                });

                homeAdapter.notifyDataSetChanged(); // Notify adapter of data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ToastUtils.showCustomToast(getContext(), "Failed to load data");
            }
        });
    }
}
