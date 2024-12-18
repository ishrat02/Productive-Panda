package com.example.adminpanel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> completedTaskList = new ArrayList<>();
    private FirebaseUser firebaseUser;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.taskRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        loadCompletedTasks();

        return view;
    }

    private void loadCompletedTasks() {
        if (firebaseUser != null) {
            FirebaseDatabase.getInstance().getReference("Completed tasks")
                    .child(firebaseUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            completedTaskList.clear();
                            for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                                Task task = taskSnapshot.getValue(Task.class);
                                if (task != null && task.isComplete()) {
                                    completedTaskList.add(task);
                                }
                            }
                            taskAdapter = new TaskAdapter(completedTaskList, HistoryFragment.this);
                            recyclerView.setAdapter(taskAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error
                        }
                    });
        }
    }

    public void addCompletedTask(Task task) {
        completedTaskList.add(task);
        if (taskAdapter != null) {
            taskAdapter.notifyDataSetChanged();
        }
    }
}
