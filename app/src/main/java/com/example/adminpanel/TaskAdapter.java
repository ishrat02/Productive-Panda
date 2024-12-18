package com.example.adminpanel;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Task> taskList;
    private TasksFragment tasksFragment;
    private HistoryFragment historyFragment;

    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public TaskAdapter(List<Task> taskList, TasksFragment tasksFragment) {
        this.taskList = taskList;
        this.tasksFragment = tasksFragment;
    }

    public TaskAdapter(List<Task> taskList, HistoryFragment historyFragment) {
        this.taskList = taskList;
        this.historyFragment = historyFragment;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.title.setText(task.getTitle());
        holder.description.setText(task.getDescription());
        holder.time.setText(task.getTime());
        holder.date.setText(task.getDateOfWeek(task.getDate()));
        holder.month.setText(task.getMonthOfWeek(task.getDate()));
        holder.day.setText(task.getDayOfWeek(task.getDate()));

        if (!task.isComplete()) {
            holder.status.setText("Upcoming");
        } else {
            holder.status.setText("Completed");
            holder.options.setVisibility(View.GONE);
        }

        holder.options.setOnClickListener(view -> showPopUpMenu(view, position));
    }

    private void showPopUpMenu(View view, int position) {
        final Task task = taskList.get(position);
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.deleteTask) {
                tasksFragment.deleteTask(task, false);
            } else if (menuItem.getItemId() == R.id.editTask) {
                tasksFragment.showEditTaskDialog(task, position);
            } else if (menuItem.getItemId() == R.id.completeTask) {
                markTaskAsComplete(task, position);
            } else if (menuItem.getItemId() == R.id.details) {
                tasksFragment.showDetails(task);
            }
            return false;
        });

        popupMenu.show();
    }

    private void markTaskAsComplete(Task task, int position) {
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference taskRef = FirebaseDatabase.getInstance()
                    .getReference("Completed tasks")
                    .child(userId)
                    .child(task.getTaskId()); // Use task.getId() to access the task ID

            // Update the task as completed
            task.setComplete(true);
            taskRef.setValue(task)
                    .addOnSuccessListener(aVoid -> {
                        tasksFragment.deleteTask(task, true); // Remove from active list
                        tasksFragment.completeTaskPopUp();

                        if (historyFragment != null) {
                            historyFragment.addCompletedTask(task); // Add to history
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle the failure case
                        Toast.makeText(tasksFragment.getContext(),
                                "Failed to complete task: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, time, status, date, month, day;
        ImageView options;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.item_title);
            description = itemView.findViewById(R.id.item_description);
            time = itemView.findViewById(R.id.item_time);
            status = itemView.findViewById(R.id.item_status);
            date = itemView.findViewById(R.id.item_date);
            day = itemView.findViewById(R.id.item_day);
            month = itemView.findViewById(R.id.item_month);
            options = itemView.findViewById(R.id.item_options);
        }
    }
}
