package com.example.adminpanel;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FocusHistoryFragment extends Fragment {
    private static ArrayList<Session> sessionList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SessionAdapter adapter;
    private DatabaseReference focusRef;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ImageView noDataImage;

    public FocusHistoryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = null;
        try {
            view = inflater.inflate(R.layout.fragment_focus_history, container, false);

            // Initialize UI components
            recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            noDataImage = view.findViewById(R.id.noDataImage);
            Glide.with(getContext()).load(R.drawable.nosession).into(noDataImage);

            adapter = new SessionAdapter(sessionList);
            recyclerView.setAdapter(adapter);

            authProfile = FirebaseAuth.getInstance();
            firebaseUser = authProfile.getCurrentUser();

            //Toast.makeText(getContext(), "Loading sessions...", Toast.LENGTH_SHORT).show();

            // Load sessions from the database
            fetchSessionsFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading Focus History!", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    private void fetchSessionsFromDatabase() {
        if (firebaseUser != null) {
            focusRef = FirebaseDatabase.getInstance()
                    .getReference("Focus Sessions")
                    .child(firebaseUser.getUid());

            focusRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        sessionList.clear(); // Clear the list to prevent duplicates
                        for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                            try {
                                Session session = sessionSnapshot.getValue(Session.class);
                                Log.d("FocusHistory", "Session fetched: " + session);
                                if (session != null && !containsSession(session)) {
                                    sessionList.add(session);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Log.e("FocusHistory", "Error mapping session: " + sessionSnapshot.toString(), ex);
                            }
                        }
                        Log.d("FocusHistory", "Session list size: " + sessionList.size());
                        adapter.notifyDataSetChanged(); // Refresh RecyclerView
                        checkEmptyState(); // Check if there are no sessions
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error processing session data!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                    Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "User not authenticated!", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkEmptyState() {
        if (sessionList.isEmpty()) {
            noDataImage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noDataImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public static void addSession(Session session) {
        try {
            if (session != null) {
                sessionList.add(session);
            } else {
                throw new IllegalArgumentException("Session cannot be null");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to check for duplicates
    private boolean containsSession(Session session) {
        for (Session existingSession : sessionList) {
            if (existingSession.getSessionId().equals(session.getSessionId())) {
                return true;
            }
        }
        return false;
    }
}
