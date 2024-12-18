package com.example.adminpanel;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteViewHolder> {

    private List<Note> currentList = new ArrayList<>();
    private final FirebaseFirestore firebaseFirestore;
    private final Context context;

    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, Context context, FirebaseFirestore firebaseFirestore) {
        super(options);
        this.context = context;
        this.firebaseFirestore = firebaseFirestore;
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note model) {
        String docId = getSnapshots().getSnapshot(position).getId();

        holder.notetitle.setText(model.getTitle());
        holder.notecontent.setText(model.getContent());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditNoteActivity.class);
            intent.putExtra("title", model.getTitle());
            intent.putExtra("content", model.getContent());
            intent.putExtra("noteId", docId);
            context.startActivity(intent);
        });

        holder.popbutton.setOnClickListener(v -> {
            DocumentReference documentReference = firebaseFirestore.collection("notes")
                    .document(docId);
            documentReference.delete()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Note deleted successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to delete note", Toast.LENGTH_SHORT).show());
        });
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_note_item, parent, false);
        return new NoteViewHolder(view);
    }

    public void setFilteredList(List<Note> filteredList) {
        currentList.clear();
        currentList.addAll(filteredList);
        notifyDataSetChanged();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView notetitle;
        private final TextView notecontent;
        private final LinearLayout mnote;
        private final ImageView popbutton;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle = itemView.findViewById(R.id.noteTitle);
            notecontent = itemView.findViewById(R.id.notecontent);
            mnote = itemView.findViewById(R.id.note);
            popbutton = itemView.findViewById(R.id.menupopbutton);
        }
    }
}
