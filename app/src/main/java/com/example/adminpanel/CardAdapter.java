package com.example.adminpanel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.TaskViewHolder> {
    private List<Card> cardList;
    private FlashCardListActivity flashCardListActivity;
    private OnItemClickListener onItemClickListener;
    //private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public CardAdapter(List<Card> topicList, FlashCardListActivity flashCardListActivity) {
        this.cardList = topicList;
        this.flashCardListActivity = flashCardListActivity;
    }
    public interface OnItemClickListener {
        void onItemClick(Card topic);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flash_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Card topic = cardList.get(position);

        holder.question.setText(topic.getQuestion());
        holder.itemView.setOnClickListener(view -> {
            if(onItemClickListener!=null){
                onItemClickListener.onItemClick(topic);
            }
        });
        holder.options.setOnClickListener(view -> {
            showPopUpMenu(view, position);
        });
    }

    private void showPopUpMenu(View view, int position) {
        final Card topic = cardList.get(position);
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_flash_topic, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.deleteTask) {
                flashCardListActivity.deleteTopic(topic, false);
            } else if (menuItem.getItemId() == R.id.editTask) {
                flashCardListActivity.showEditTopicDialog(topic, position);
            }
            return false;
        });

        popupMenu.show();
    }
    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView question,answer;
        ImageView options;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            question = itemView.findViewById(R.id.item_question);
            options = itemView.findViewById(R.id.item_options);
            //answer=itemView.findViewById(R.id.answerBox);
        }
    }
}
//before editing