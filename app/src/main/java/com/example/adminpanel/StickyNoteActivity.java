package com.example.adminpanel;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StickyNoteActivity extends AppCompatActivity {
    private ImageView back;
    private RecyclerView recyclerView;
    private FlashTopicAdapter topicAdapter;
    private FirebaseUser firebaseUser;
    private FirebaseAuth authProfile;
    private ImageView addTopic;
    private List<FlashTopic> topicList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sticky_note);

        // Initialize RecyclerView and other components
        back=findViewById(R.id.back_button);
        recyclerView = findViewById(R.id.topicRecycler);
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();
        addTopic = findViewById(R.id.addTopic);

        // Initialize topic list BEFORE passing it to the adapter
        topicList = new ArrayList<>();
        topicAdapter = new FlashTopicAdapter(topicList, this);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(topicAdapter);
        Toast.makeText(StickyNoteActivity.this,"Loading topics...",Toast.LENGTH_LONG).show();
        topicAdapter.setOnItemClickListener(new FlashTopicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FlashTopic topic) {
                Intent intent=new Intent(StickyNoteActivity.this,FlashCardListActivity.class);
                intent.putExtra("TOPIC_NAME",topic.getTopicName());
                intent.putExtra("TOPIC_ID", topic.getTopicId());
                startActivity(intent);
            }
        });
        // Load data if necessary
        loadTopicsFromFirebase();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        addTopic.setOnClickListener(view -> {
            Toast.makeText(StickyNoteActivity.this, "clicked", Toast.LENGTH_SHORT).show();
            showAddTaskDialog();
        });
    }
    //eta incomplete->
    private void loadTopicsFromFirebase() {
        if (firebaseUser == null) return;

        DatabaseReference tasksRef = FirebaseDatabase.getInstance()
                .getReference("FlashCard Topics")
                .child(firebaseUser.getUid());

        // Listen for changes to the tasks
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topicList.clear(); // Clear existing list to avoid duplicates
                //taskDates.clear();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    FlashTopic topic = taskSnapshot.getValue(FlashTopic.class);
                    if (topic != null) {
                        DatabaseReference cardsRef = taskSnapshot.getRef().child("Cards");

                        // Count the number of cards
                        cardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot cardsSnapshot) {
                                int cardCount = (int) cardsSnapshot.getChildrenCount();
                                topic.setNoOfCards(cardCount); // Update the count
                                topicList.add(topic); // Add the topic to the list//just eita chilo tokhon
                                topicAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("FirebaseError", "Failed to load card count: " + error.getMessage());
                            }
                        });
                        //taskDates.add(task.getDate());
                    }
                }
                topicAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StickyNoteActivity.this, "Failed to load tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error loading tasks: ", error.toException());
            }
        });

    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StickyNoteActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.add_topic_dialog, null);
        EditText topicName = dialogView.findViewById(R.id.topicBox);

        Button createBtn = dialogView.findViewById(R.id.btnCreate); // Corrected
        Button cancelBtn = dialogView.findViewById(R.id.btnCancel); // Corrected

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        cancelBtn.setOnClickListener(view -> {
            Toast.makeText(StickyNoteActivity.this, "Cancel clicked", Toast.LENGTH_SHORT).show();
            dialog.dismiss(); // Dismiss the dialog on cancel
        });

        createBtn.setOnClickListener(view -> {
            String topicNameStr = topicName.getText().toString().trim();
            if (topicNameStr.isEmpty()) {
                Toast.makeText(StickyNoteActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(StickyNoteActivity.this,"Adding topic...",Toast.LENGTH_LONG).show();
                addTaskToFirebase(topicNameStr);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addTaskToFirebase(String topicNameStr) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference topicRef = database.getReference("FlashCard Topics").child(firebaseUser.getUid());
        DatabaseReference newTopicRef = topicRef.push();
        String topicId = newTopicRef.getKey();
        FlashTopic flashTopic=new FlashTopic(topicNameStr,topicId,0);
        newTopicRef.setValue(flashTopic)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(StickyNoteActivity.this, "Topic Added Successfully!", Toast.LENGTH_SHORT).show();
                    boolean taskExists = false;
                    for (FlashTopic existingTopic : topicList) {
                        if (existingTopic.getTopicId().equals(flashTopic.getTopicId())) {
                            taskExists = true;
                            break;
                        }
                    }
                    // Update the RecyclerView


                    if (!taskExists) {
                        topicList.add(flashTopic);
                        topicAdapter.notifyItemInserted(topicList.size() - 1);
                        recyclerView.scrollToPosition(topicList.size() - 1);
                    }


                    //TasksFragment.this.checkEmptyState();
                }).addOnFailureListener(e -> {
                    Toast.makeText(StickyNoteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseError", "Error adding task: ", e);
                });
    }

    public void deleteTopic(FlashTopic topic, boolean b) {
        if (firebaseUser == null) return;

        DatabaseReference topicRef = FirebaseDatabase.getInstance()
                .getReference("FlashCard Topics")
                .child(firebaseUser.getUid())
                .child(topic.getTopicId());

        // Remove the task from Firebase
        topicRef.removeValue()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        // The task was successfully deleted
                        Toast.makeText(StickyNoteActivity.this, "Topic successfully deleted.", Toast.LENGTH_SHORT).show();
                        Log.d("Firebase", "Topic successfully deleted.");
                    } else {
                        // Handle the failure
                        Toast.makeText(StickyNoteActivity.this, "Failed to delete task.", Toast.LENGTH_SHORT).show();
                        Log.e("Firebase", "Failed to delete task.", task1.getException());
                    }
                });
    }

    public void showEditTopicDialog(FlashTopic topic, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(StickyNoteActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.add_topic_dialog, null);
        EditText topicName = dialogView.findViewById(R.id.topicBox);

        Button createBtn = dialogView.findViewById(R.id.btnCreate); // Corrected
        Button cancelBtn = dialogView.findViewById(R.id.btnCancel); // Corrected
        TextView createNewTopic=dialogView.findViewById(R.id.forgotTitle);
        createNewTopic.setText("Edit Topic");
        createBtn.setText("Update");
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        topicName.setText(topic.getTopicName());
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String updatedName=topicName.getText().toString().trim();
                if(updatedName.isEmpty()){
                    topicName.setError("This field cannot be empty");
                }
                else
                {
                    updateTaskInFirebase(topic.getTopicId(), updatedName, position);
                    dialog.dismiss();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void updateTaskInFirebase(String topicId, String updatedName, int position) {
        if (firebaseUser == null) return;

        DatabaseReference topicRef = FirebaseDatabase.getInstance()
                .getReference("FlashCard Topics")
                .child(firebaseUser.getUid())
                .child(topicId);

        FlashTopic updatedTopic = new FlashTopic(updatedName,topicId,0);

        topicRef.setValue(updatedTopic)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(StickyNoteActivity.this, "Topic Updated Successfully!", Toast.LENGTH_SHORT).show();

                        // Update task in RecyclerView
                        topicList.set(position, updatedTopic);
                        topicAdapter.notifyItemChanged(position);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(StickyNoteActivity.this, "Failed to Update Task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseError", "Error updating task: ", e);
                });
    }
}