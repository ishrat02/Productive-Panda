package com.example.adminpanel;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JournalActivity extends AppCompatActivity {
    ImageView back;
    FloatingActionButton addNoteBtn;
    List<Note> noteList=new ArrayList<>();
    FirebaseUser firebaseUser;
    FirebaseAuth authProfile;
    RecyclerView mrecyclerView;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    FirebaseFirestore firebaseFirestore;
    FirestoreRecyclerAdapter<Note,NoteViewHolder>noteAdapter;
    //emni e rakhsi->jhamela korle search view related shob remove kore dile e hobe
//    public void setFilteredList(List<Note>filteredList){
//        this.itemList=filteredList;
//        notifyDataSetChanged();
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        addNoteBtn=findViewById(R.id.add_note_btn);
        addNoteBtn.setImageTintList(ContextCompat.getColorStateList(this, R.color.white));
        authProfile=FirebaseAuth.getInstance();
        firebaseUser=authProfile.getCurrentUser();
        firebaseFirestore=FirebaseFirestore.getInstance();
        back=findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        addNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JournalActivity.this,CreateNote.class));
            }
        });
        //bomb
        //firebase e recycler view gula save hocche
        try {
            Query qr = firebaseFirestore.collection("notes")
                    .document(firebaseUser.getUid())
                    .collection("myNotes")
                    .orderBy("title", Query.Direction.ASCENDING);

            qr.get().addOnSuccessListener(queryDocumentSnapshots -> {
                noteList.clear();
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Note note = document.toObject(Note.class);
                        noteList.add(note);
                    }
                    //Toast.makeText(JournalActivity.this, "Fetched notes", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                        Toast.makeText(JournalActivity.this, "Failed to fetch notes", Toast.LENGTH_SHORT).show();
                    }
            );
        } catch (Exception e) {
            Toast.makeText(JournalActivity.this, "An unexpected error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace(); // Log the exception for debugging purposes
        }


        //bomb ends
        //new bomb here
        //before editing
        Query query=firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").orderBy("title", com.google.firebase.firestore.Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Note>allUser=new FirestoreRecyclerOptions.Builder<Note>().setQuery(query,Note.class).build();
        noteAdapter=new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allUser) {
            private List<Note>currentList=new ArrayList<>();
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note model) {
                ImageView popbutton=holder.itemView.findViewById(R.id.menupopbutton);
                String docId=noteAdapter.getSnapshots().getSnapshot(position).getId();
                popbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popupMenu=new PopupMenu(view.getContext(),view);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                                //
                                DocumentReference documentReference=firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").document(docId);
                                documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(JournalActivity.this,"The note is successfully deleted!",Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(JournalActivity.this,"Failed to delete",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });
                int colorCode=getRandomColor();
                holder.mnote.setBackgroundColor(holder.itemView.getResources().getColor(colorCode,null));
                holder.notetitle.setText(model.getTitle());
                holder.notecontent.setText(model.getContent());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(view.getContext(),EditNoteActivity.class);
                        intent.putExtra("title",model.getTitle());
                        intent.putExtra("content",model.getContent());
                        intent.putExtra("noteId",docId);
                        view.getContext().startActivity(intent);
                        //Toast.makeText(getApplicationContext(),"This is clicked",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_note_item,parent,false);
                return new NoteViewHolder(view);
            }
            public void setFilteredList(List<Note> filteredList) {
                currentList.clear();
                currentList.addAll(filteredList);
                notifyDataSetChanged();
            }
        };
        mrecyclerView=findViewById(R.id.recycler_view);
        mrecyclerView.setHasFixedSize(true);
        staggeredGridLayoutManager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mrecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mrecyclerView.setAdapter(noteAdapter);
    }
    private int getRandomColor() {
        List<Integer>colorCode=new ArrayList<>();
        colorCode.add(R.color.lightGray);
        colorCode.add(R.color.deep);
        colorCode.add(R.color.colorAccent);
        colorCode.add(R.color.green1);
        colorCode.add(R.color.purple_200);
        colorCode.add(R.color.pink2);
        colorCode.add(R.color.max_blue);
        colorCode.add(R.color.min_blue);
        colorCode.add(R.color.red2);
        Random random=new Random();
        int number=random.nextInt(colorCode.size());
        return colorCode.get(number);
    }

    //    @NonNull
//    @Override
    public class NoteViewHolder extends RecyclerView.ViewHolder
    {
        private TextView notetitle;
        private TextView notecontent;
        LinearLayout mnote;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle=itemView.findViewById(R.id.noteTitle);
            notecontent=itemView.findViewById(R.id.notecontent);
            mnote=itemView.findViewById(R.id.note);

        }
    }
    @Override
    protected void onStart(){
        super.onStart();
        noteAdapter.startListening();
    }
    @Override
    protected void onStop(){
        super.onStop();
        if(noteAdapter!=null)
        {
            noteAdapter.startListening();
        }
    }
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(JournalActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
//before edit