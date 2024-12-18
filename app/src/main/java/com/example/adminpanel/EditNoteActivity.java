package com.example.adminpanel;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditNoteActivity extends AppCompatActivity {
    Intent data;
    private ImageView back;
    private FloatingActionButton editNoteBtn;
    private EditText etTitle,etContent;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_note);
        back=findViewById(R.id.back_button);
        etTitle=findViewById(R.id.edittitleofnote);
        etContent=findViewById(R.id.editcontentofnote);
        authProfile=FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();
        firebaseFirestore=FirebaseFirestore.getInstance();
        editNoteBtn=findViewById(R.id.savenote);
        editNoteBtn.setImageTintList(ContextCompat.getColorStateList(this, R.color.black));
        data=getIntent();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        editNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newTitle=etTitle.getText().toString();
                String newContent=etContent.getText().toString();
                DocumentReference documentReference=firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));
                Map<String,Object> note=new HashMap<>();
                note.put("title",newTitle);
                note.put("content",newContent);
                documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(),"Note updated successfully!",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Couldn't update note.",Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
            }
        });
        String noteTitle=data.getStringExtra("title");
        String noteContent=data.getStringExtra("content");
        etTitle.setText(noteTitle);
        etContent.setText(noteContent);
    }
}