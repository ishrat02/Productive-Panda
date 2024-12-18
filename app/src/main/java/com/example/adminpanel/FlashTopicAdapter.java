package com.example.adminpanel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FlashTopicAdapter extends RecyclerView.Adapter<FlashTopicAdapter.TaskViewHolder> {
    private List<FlashTopic> topicList;
    private StickyNoteActivity stickyNoteActivity;
    private OnItemClickListener onItemClickListener;
    //private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public FlashTopicAdapter(List<FlashTopic> topicList, StickyNoteActivity stickyNoteActivity) {
        this.topicList = topicList;
        this.stickyNoteActivity = stickyNoteActivity;
    }
    public interface OnItemClickListener {
        void onItemClick(FlashTopic topic);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flash_topic, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        FlashTopic topic = topicList.get(position);

        holder.name.setText(topic.getTopicName());
        holder.noOfCards.setText(topic.getNoOfCards()+" Cards");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(topic);
                }
            }
        });
        holder.options.setOnClickListener(view -> {
            showPopUpMenu(view, position);
        });
    }

    private void showPopUpMenu(View view, int position) {
        final FlashTopic topic = topicList.get(position);
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_flash_topic, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.deleteTask) {
                stickyNoteActivity.deleteTopic(topic, false);
            } else if (menuItem.getItemId() == R.id.editTask) {
                stickyNoteActivity.showEditTopicDialog(topic, position);
            }
            return false;
        });

        popupMenu.show();
    }
    @Override
    public int getItemCount() {
        return topicList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView name,noOfCards;
        ImageView options;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.item_topic);
            options = itemView.findViewById(R.id.item_options);
            noOfCards=itemView.findViewById(R.id.item_number);
        }
    }
}
//before editing