package com.example.studymate.UserDashboard.Room;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.studymate.Network.NetworkUtils;
import com.example.studymate.Notification.ToastUtils;
import com.example.studymate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RoomFragment extends Fragment {
    private FloatingActionButton fab;
    private DatabaseReference databaseReference;
    private ValueEventListener eventListener;
    private RecyclerView recyclerView;
    private List<DataClass> dataList;
    private MyAdapter adapter;
    private SearchView searchView;
    private Context context;
    private TextView emptyStateTextView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // Enable Firebase offline persistence
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment_room, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        fab = view.findViewById(R.id.fab);
        searchView = view.findViewById(R.id.search);
        searchView.clearFocus();

        emptyStateTextView = view.findViewById(R.id.empty_message);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(R.layout.loading_progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();
        adapter = new MyAdapter(context, dataList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Rooms");
        databaseReference.keepSynced(true); // Keeps the data synced for offline use

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null) {
                    String currentUserId = currentUser.getUid();

                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        DataClass dataClass = itemSnapshot.getValue(DataClass.class);

                        if (dataClass != null) {
                            dataClass.setKey(itemSnapshot.getKey());

                            String createdBy = itemSnapshot.child("createdBy").getValue(String.class);
                            boolean isParticipant = itemSnapshot.child("participants").child(currentUserId).exists();

                            if (currentUserId.equals(createdBy) || isParticipant) {
                                dataList.add(dataClass);
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                dialog.dismiss();

                // Check if the list is empty and update the UI
                if (dataList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyStateTextView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyStateTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });

        return view;
    }

    private void searchList(String text) {
        ArrayList<DataClass> searchList = new ArrayList<>();
        for (DataClass dataClass : dataList) {
            if (dataClass.getDataTitle().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(dataClass);
            }
        }
        adapter.searchDataList(searchList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseReference != null && eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout_room);

        LinearLayout createRoomLayout = dialog.findViewById(R.id.layoutRoomCreate);
        LinearLayout joinRoomLayout = dialog.findViewById(R.id.layoutRoomJoin);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        createRoomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (NetworkUtils.isConnected(requireContext())) {
                    showCreateRoomDialog();
                } else {
                    ToastUtils.showCustomToast(requireContext(), "No Internet Connection");
                }
            }
        });

        joinRoomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (NetworkUtils.isConnected(requireContext())) {
                    showJoinRoomDialog();
                } else {
                    ToastUtils.showCustomToast(requireContext(), "No Internet Connection");
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void showCreateRoomDialog() {
        final Dialog createRoomDialog = new Dialog(requireContext());
        createRoomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        createRoomDialog.setContentView(R.layout.dialog_create_room);
        createRoomDialog.setCanceledOnTouchOutside(true);

        EditText roomTitle = createRoomDialog.findViewById(R.id.roomTitle);
        EditText roomDesc = createRoomDialog.findViewById(R.id.roomDesc);
        Button createRoomButton = createRoomDialog.findViewById(R.id.createRoomButton);

        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = roomTitle.getText().toString();
                String description = roomDesc.getText().toString();
                String accessCode = generateFormattedCode();

                if (title.isEmpty() || description.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setCancelable(false);
                builder.setView(R.layout.saving_progress_layout);
                AlertDialog dialog = builder.create();
                dialog.show();

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String userId = currentUser != null ? currentUser.getUid() : "Unknown User";

                String backgroundImage = "";
                DataClass dataClass = new DataClass(title, description, userId, accessCode, 0, backgroundImage);  // Initialize personCount to 1

                DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("Rooms").child(accessCode);
                roomsRef.setValue(dataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Room Created", Toast.LENGTH_SHORT).show();
                            createRoomDialog.dismiss();
                            updateRoomCountsInDatabase();  // Call the function to update roomCounts in Realtime Database
                        } else {
                            Toast.makeText(requireContext(), "Failed to create room: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        createRoomDialog.show();
        createRoomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        createRoomDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void updateRoomCountsInDatabase() {
        DatabaseReference analyticsRef = FirebaseDatabase.getInstance().getReference("Analytics").child("roomCount");
        analyticsRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentCount = currentData.getValue(Integer.class);
                if (currentCount == null) {
                    currentData.setValue(1);  // Initialize with 1 if roomCounts doesn't exist
                } else {
                    currentData.setValue(currentCount + 1);  // Increment by 1
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e("FirebaseDatabase", "Failed to update roomCounts: " + error.getMessage());
                } else {
                    Log.d("FirebaseDatabase", "roomCounts updated successfully.");
                }
            }
        });
    }


    private void showJoinRoomDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_join_room);
        dialog.setCanceledOnTouchOutside(true);

        EditText joinRoomEditText = dialog.findViewById(R.id.joinRoom);
        Button joinRoomButton = dialog.findViewById(R.id.joinRoomButton);

        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accessCode = joinRoomEditText.getText().toString();
                if (!accessCode.isEmpty()) {
                    joinRoom(accessCode, dialog);
                } else {
                    Toast.makeText(getContext(), "Please enter an access code", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void joinRoom(String accessCode, Dialog dialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false);
        builder.setView(R.layout.joining_progress_layout);
        AlertDialog progressDialog = builder.create();
        progressDialog.show();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "Unknown User";

        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("Rooms").child(accessCode);
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataClass roomData = snapshot.getValue(DataClass.class);
                    if (roomData != null) {
                        // Check if the user is already a participant
                        if (snapshot.child("participants").hasChild(userId)) {
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(), "You are already in this room", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            // Add the user to the room and update personCount
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("participants/" + userId, true);
                            updates.put("personCount", roomData.getPersonCount() + 1);

                            roomRef.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(requireContext(), "Joined Room", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(requireContext(), "Failed to join room: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Room does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String generateFormattedCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        for (int i = 0; i < 8; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }

        return code.toString();
    }

}
