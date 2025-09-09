package com.example.studymate.AdminDashboard.AdminHome.People;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.studymate.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminPeopleFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<AdminUser> userList;
    private List<AdminUser> filteredUserList;
    private DatabaseReference usersRef;
    private EditText searchView;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_people, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchView = view.findViewById(R.id.searchView);
        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
        adapter = new UserAdapter(getContext(), filteredUserList);
        recyclerView.setAdapter(adapter);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            // Create and show the dialog
            DialogAddAdmin dialogFragment = new DialogAddAdmin();
            dialogFragment.show(getParentFragmentManager(), "DialogAddAdmin");
        });

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    AdminUser user = new AdminUser(id, firstName, lastName, imageUrl);
                    userList.add(user);
                }
                // Initially show all users
                filteredUserList.clear();
                filteredUserList.addAll(userList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        return view;
    }

    private void filterUsers(String query) {
        filteredUserList.clear();
        if (query.isEmpty()) {
            filteredUserList.addAll(userList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (AdminUser user : userList) {
                String fullName = user.getFirstName() + " " + user.getLastName();
                if (fullName.toLowerCase().contains(lowerCaseQuery)) {
                    filteredUserList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
