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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class FlashCardListActivity extends AppCompatActivity {
    private ImageView backButton;
    private RecyclerView recyclerView;
    FirebaseAuth authProfile;
    FirebaseUser firebaseUser;
    private List<Card> cardList;
    private CardAdapter cardAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flash_card_list);
        backButton=findViewById(R.id.back_button);
        authProfile=FirebaseAuth.getInstance();
        firebaseUser=authProfile.getCurrentUser();
        recyclerView = findViewById(R.id.recycler_view_card);
        cardList = new ArrayList<>();
        cardAdapter = new CardAdapter(cardList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cardAdapter);
        String topicName=getIntent().getStringExtra("TOPIC_NAME");
        TextView pageTitle=findViewById(R.id.page_title);
        pageTitle.setText(topicName);
        FloatingActionButton addCardButton = findViewById(R.id.add_card_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        addCardButton.setOnClickListener(view -> {
            showAddTaskDialog();
        });
        cardAdapter.setOnItemClickListener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Card topic) {
                Intent intent = new Intent(FlashCardListActivity.this, FlipCard.class);
                intent.putExtra("QUESTION", topic.getQuestion());
                intent.putExtra("ANSWER", topic.getAnswer());
                startActivity(intent);
            }
        });
        loadTopicsFromFirebase();
    }

    private void loadTopicsFromFirebase() {
        if(firebaseUser==null) return;
        String topicId = getIntent().getStringExtra("TOPIC_ID");
        DatabaseReference cardRef = FirebaseDatabase.getInstance()
                .getReference("FlashCard Topics")
                .child(firebaseUser.getUid())
                .child(topicId)
                .child("Cards");
        cardRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cardList.clear();
                for(DataSnapshot cardSnapShot:snapshot.getChildren()){
                    Card card=cardSnapShot.getValue(Card.class);
                    if(card!=null)
                    {
                        cardList.add(card);
                    }
                }
                cardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FlashCardListActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error loading cards", error.toException());
            }
        });
    }


    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FlashCardListActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_flashcard, null);
        EditText topicQuestion = dialogView.findViewById(R.id.questionBox);
        EditText topicAnswer=dialogView.findViewById(R.id.answerBox);
        Button createBtn = dialogView.findViewById(R.id.btnCreate); // Corrected
        Button cancelBtn = dialogView.findViewById(R.id.btnCancel); // Corrected

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        cancelBtn.setOnClickListener(view -> {
            dialog.dismiss(); // Dismiss the dialog on cancel
        });

        createBtn.setOnClickListener(view -> {
            String topicQuestionStr = topicQuestion.getText().toString().trim();
            String topicAnswerStr = topicAnswer.getText().toString().trim();
            if (topicQuestionStr.isEmpty() || topicAnswerStr.isEmpty()) {
                Toast.makeText(FlashCardListActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }if(topicQuestionStr.length()>600)
            {
                Toast.makeText(FlashCardListActivity.this, "Question can't be longer than 120 words", Toast.LENGTH_SHORT).show();
            }if(topicAnswerStr.length()>600)
            {
                Toast.makeText(FlashCardListActivity.this, "Answer can't be longer than 120 words", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(FlashCardListActivity.this,"Adding topic...",Toast.LENGTH_LONG).show();
                addCardToFirebase(topicQuestionStr,topicAnswerStr);//fire
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addCardToFirebase(String question, String answer) {
        // Reference to the topic node
        String topicId = getIntent().getStringExtra("TOPIC_ID");
        DatabaseReference topicRef = FirebaseDatabase.getInstance()
                .getReference("FlashCard Topics")
                .child(firebaseUser.getUid())
                .child(topicId)
                .child("Cards"); // Add under "Cards" child

        // Generate a unique key for the new card
        String cardId = topicRef.push().getKey();
        if(cardId==null) return;
        // Create a FlashCard object
        Card newCard = new Card(question, answer, cardId);
        // Add the card to the "Cards" child without overwriting the topic node
        topicRef.child(cardId).setValue(newCard)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Toast.makeText(FlashCardListActivity.this, "Card added successfully!", Toast.LENGTH_SHORT).show();
                        boolean cardExists=false;
                        for(Card existingCard:cardList){
                            if(existingCard.getCardId().equals(newCard.getCardId())){
                                cardExists=true;
                                break;
                            }
                        }
                        if(!cardExists){
                            if (recyclerView.getAdapter() == null) {
                                recyclerView.setAdapter(cardAdapter);
                            }
                            cardList.add(newCard);
                            if (cardList == null || cardList.isEmpty()) {
                                Toast.makeText(FlashCardListActivity.this,"Cardlist empty",Toast.LENGTH_SHORT).show();
                                Log.e("AdapterError", "cardList is null or empty");
                                return;
                            }
                            cardAdapter.notifyItemInserted(cardList.size()-1);
                            recyclerView.scrollToPosition(cardList.size()-1);
                            //Toast.makeText(FlashCardListActivity.this,"Card added successfully",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add card: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseError", "Error adding card: ", e);
                });
    }

    public void deleteTopic(Card topic, boolean b) {
        if(firebaseUser==null) return;
        String topicId = getIntent().getStringExtra("TOPIC_ID");
        DatabaseReference topicRef = FirebaseDatabase.getInstance()
                .getReference("FlashCard Topics")
                .child(firebaseUser.getUid())
                .child(topicId)
                .child("Cards").child(topic.getCardId());
        topicRef.removeValue()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(FlashCardListActivity.this, "Card successfully deleted.", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(FlashCardListActivity.this, "Failed to delete card.", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    public void showEditTopicDialog(Card topic, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FlashCardListActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_flashcard, null);
        EditText topicQuestion = dialogView.findViewById(R.id.questionBox);
        EditText topicAnswer=dialogView.findViewById(R.id.answerBox);
        Button createBtn = dialogView.findViewById(R.id.btnCreate); // Corrected
        Button cancelBtn = dialogView.findViewById(R.id.btnCancel); // Corrected
        TextView editCard=dialogView.findViewById(R.id.forgotTitle);
        editCard.setText("Edit Card");
        createBtn.setText("Update");
        topicQuestion.setText(topic.getQuestion());
        topicAnswer.setText(topic.getAnswer());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        cancelBtn.setOnClickListener(view -> {
            dialog.dismiss(); // Dismiss the dialog on cancel
        });

        createBtn.setOnClickListener(view -> {
            String topicQuestionStr = topicQuestion.getText().toString().trim();
            String topicAnswerStr = topicAnswer.getText().toString().trim();
            if (topicQuestionStr.isEmpty() || topicAnswerStr.isEmpty()) {
                Toast.makeText(FlashCardListActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }if(topicQuestionStr.length()>600)
            {
                Toast.makeText(FlashCardListActivity.this, "Question can't be longer than 120 words", Toast.LENGTH_SHORT).show();
            }if(topicAnswerStr.length()>600)
            {
                Toast.makeText(FlashCardListActivity.this, "Answer can't be longer than 120 words", Toast.LENGTH_SHORT).show();
            } else {
                updateCardToFirebase(topic.getCardId(),topicQuestionStr,topicAnswerStr,position);//fire
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateCardToFirebase(String cardId,String topicQuestionStr, String topicAnswerStr,int position) {
        if(firebaseUser==null) return;
        String topicId = getIntent().getStringExtra("TOPIC_ID");
        DatabaseReference topicRef = FirebaseDatabase.getInstance()
                .getReference("FlashCard Topics")
                .child(firebaseUser.getUid())
                .child(topicId)
                .child("Cards").child(cardId);
        Card updatedCard=new Card(topicQuestionStr,topicAnswerStr,cardId);
        topicRef.setValue(updatedCard)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        cardList.set(position,updatedCard);
                        cardAdapter.notifyItemChanged(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FlashCardListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}