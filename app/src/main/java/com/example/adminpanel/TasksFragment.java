package com.example.adminpanel;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TasksFragment extends Fragment {
    private EditText etTitle, etDescription, etDate, etTime, etEvent;
    private Button btAddTask,btBack;
    private ImageView noDataImage,backBtn;
    private RecyclerView recyclerView;
    private FirebaseUser firebaseUser;
    private List<Task> taskList;
    private TaskAdapter taskAdapter;
    private int mYear, mMonth, mDay;
    private int mHour, mMinute;
    //notification adding
    private Spinner reminderSpinner;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    public TasksFragment() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        // Initialize views
        TextView addTask = view.findViewById(R.id.addTask);
        noDataImage = view.findViewById(R.id.noDataImage);
        backBtn=view.findViewById(R.id.backButton);
        recyclerView = view.findViewById(R.id.taskRecycler);
        Glide.with(getContext()).load(R.drawable.arektagif).into(noDataImage);
        // Initialize Firebase Auth
        FirebaseAuth authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();
        ImageView calendar = view.findViewById(R.id.calendar);
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(taskList,this);
        recyclerView.setAdapter(taskAdapter);
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);
        progressBar=view.findViewById(R.id.progressBar);
        // Load tasks from Firebase
        swipeRefreshLayout.setOnRefreshListener(() -> {
            progressBar.setVisibility(View.VISIBLE);
            loadTasksFromFirebase();  // Reload user profile data on swipe refresh
        });
        loadTasksFromFirebase();
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        calendar.setOnClickListener(view1 -> {
            CalendarViewFragment calendarViewFragment = new CalendarViewFragment();
            calendarViewFragment.show(getParentFragmentManager(), "CalendarViewFragment");
            //Toast.makeText(getContext(),"Entered the calendar",Toast.LENGTH_SHORT).show();
        });
        // Handle add task button click
        addTask.setOnClickListener(v -> showAddTaskDialog());

        return view;
    }

    // Method to load tasks from Firebase
    private void loadTasksFromFirebase() {
        if (firebaseUser == null) return;

        DatabaseReference tasksRef = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(firebaseUser.getUid());

        // Listen for changes to the tasks
        tasksRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!isAdded()){
                    return;
                }
                taskList.clear(); // Clear existing list to avoid duplicates
                //taskDates.clear();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        taskList.add(task);
                        //taskDates.add(task.getDate());
                    }
                }
                sortTaskListByDateTime();
                taskAdapter.notifyDataSetChanged();
                checkEmptyState(); // Check if the list is empty to update UI
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error loading tasks: ", error.toException());
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
            }
        });
        //Toast.makeText(getContext(),"The list has- "+taskList.size(),Toast.LENGTH_SHORT).show();
    }

    private void sortTaskListByDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        taskList.sort((task1, task2) -> {
            try {
                // Combine the date and time to create a full timestamp
                String dateTime1 = task1.getDate() + " " + task1.getTime();
                String dateTime2 = task2.getDate() + " " + task2.getTime();

                Date date1 = dateFormat.parse(dateTime1);
                Date date2 = dateFormat.parse(dateTime2);

                return date1.compareTo(date2);
            } catch (Exception e) {
                e.printStackTrace();
                return 0; // In case of parsing error, return 0 (no change in order)
            }
        });
    }

    // Method to show the Add Task dialog
    @SuppressLint("ClickableViewAccessibility")
    private void showAddTaskDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.fragment_create_task);
        dialog.setCanceledOnTouchOutside(false);
        reminderSpinner=dialog.findViewById(R.id.reminderSpinner);
        // Initialize dialog views
        etTitle = dialog.findViewById(R.id.addTaskTitle);
        etDescription = dialog.findViewById(R.id.addTaskDescription);
        etDate = dialog.findViewById(R.id.taskDate);
        etTime = dialog.findViewById(R.id.taskTime);
        etEvent = dialog.findViewById(R.id.taskEvent);
        btAddTask = dialog.findViewById(R.id.addTask);
        btBack=dialog.findViewById(R.id.backBtn);
        // Date picker for date field
        btBack.setOnClickListener(view -> dialog.dismiss());
        etDate.setOnTouchListener((v, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(),
                        (view1, year, monthOfYear, dayOfMonth) ->
                                etDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year),
                        mYear, mMonth, mDay);

                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
            return true;
        });

        // Time picker for time field
        etTime.setOnTouchListener((v, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                @SuppressLint("SetTextI18n") TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                        (view12, hourOfDay, minute) ->
                                etTime.setText(hourOfDay + ":" + minute),
                        mHour, mMinute, false);

                timePickerDialog.show();
            }
            return true;
        });

        // Add task button logic
        btAddTask.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String event = etEvent.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();

            if (!validateFields()) {
                if(getContext()!=null)
                {
                    //Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            } else {
                addTaskToFirebase(title, description, event, date, time);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // Method to add a task to Firebase
    private void addTaskToFirebase(String title, String description, String event, String date, String time) {
        String selectedReminder=reminderSpinner.getSelectedItem().toString();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tasksRef = database.getReference("tasks").child(firebaseUser.getUid());
        DatabaseReference newTaskRef = tasksRef.push();
        String taskId = newTaskRef.getKey();

        Task task = new Task(taskId, title, description, event, date, time, false,selectedReminder);

        newTaskRef.setValue(task)
                .addOnSuccessListener(aVoid -> {
                    if(getContext()!=null)
                    {
                        Toast.makeText(TasksFragment.this.getContext(), "Task Added Successfully!", Toast.LENGTH_SHORT).show();
                    }
                    boolean taskExists = false;
                    for (Task existingTask : taskList) {
                        if (existingTask.getTaskId().equals(task.getTaskId())) {
                            taskExists = true;
                            break;
                        }
                    }
                    // Update the RecyclerView
                    if (!taskExists) {
                        taskList.add(task);
                        taskAdapter.notifyItemInserted(taskList.size() - 1);
                        recyclerView.scrollToPosition(taskList.size() - 1);
                    }
                    TasksFragment.this.checkEmptyState();
                })
                .addOnFailureListener(e -> {
                    if(getContext()!=null)
                    {
                        Toast.makeText(getContext(), "Failed to Add Task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Log.e("FirebaseError", "Error adding task: ", e);
                });
        if (!selectedReminder.equals("Do not remind me")) {
            long reminderOffset = getReminderTime(selectedReminder);
            if (reminderOffset >= 0) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    Date taskDate = sdf.parse(date + " " + time);
                    if (taskDate != null) {
                        Calendar reminderTime = Calendar.getInstance();
                        reminderTime.setTime(taskDate);
                        reminderTime.add(Calendar.MILLISECOND, -(int) reminderOffset);

                        scheduleReminder(title, description, reminderTime);
                    }
                } catch (Exception e) {
                    Log.e("ReminderError", "Error parsing date and time for reminder: " + e.getMessage());
                }
            } else {
                Log.d("Reminder", "Reminder not scheduled for 'Do not remind me' option.");
            }
        }

    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleReminder(String title, String description, Calendar reminderTime) {
        Intent intent = new Intent(getContext(), ReminderBroadcast.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);

        int uniqueId = (int) System.currentTimeMillis(); // Use current time for unique ID
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(), uniqueId, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
            Log.d("Reminder", "Reminder scheduled for: " + reminderTime.getTime());
        }
    }
    private long getReminderTime(String selectedReminder) {
        switch (selectedReminder) {
            case "At time of event":
                return 0;
            case "5 minutes before":
                return 5 * 60 * 1000;
            case "15 minutes before":
                return 15 * 60 * 1000;
            case "30 minutes before":
                return 30 * 60 * 1000;
            case "1 hour before":
                return 60 * 60 * 1000;
            case "2 hours before":
                return 2 * 60 * 60 * 1000;
            case "12 hours before":
                return 12 * 60 * 60 * 1000;
            case "1 day before":
                return 24 * 60 * 60 * 1000;
            case "1 week before":
                return 7 * 24 * 60 * 60 * 1000;
            default:
                return -1;
        }
    }

    // Method to validate input fields
    private boolean validateFields() {
        if (etTitle.getText().toString().isEmpty()) {
            if(getContext()!=null)
            {
                etTitle.setError("Please enter a valid title");
                //Toast.makeText(getContext(), , Toast.LENGTH_SHORT).show();
            }
            return false;
        } else if (etDescription.getText().toString().isEmpty()) {
            if(getContext()!=null)
            {
                etDescription.setError("Please enter a valid description");
                //Toast.makeText(getContext(), , Toast.LENGTH_SHORT).show();
            }
            return false;
        } else if (etDate.getText().toString().isEmpty()) {
            if(getContext()!=null)
            {
                etDate.setError("Please enter a date");
                //Toast.makeText(getContext(), , Toast.LENGTH_SHORT).show();
            }
            return false;
        } else if (etTime.getText().toString().isEmpty()) {
            if(getContext()!=null)
            {
                etTitle.setError("Please enter a time");
                //Toast.makeText(getContext(), , Toast.LENGTH_SHORT).show();
            }
            return false;
        } else if (etEvent.getText().toString().isEmpty()) {
            if(getContext()!=null)
            {
                etEvent.setError("Please enter an event");
                //Toast.makeText(getContext(), , Toast.LENGTH_SHORT).show();
            }
            return false;
        } else {
            return true;
        }
    }

    // Method to check if the task list is empty
    private void checkEmptyState() {
        if (taskList.isEmpty()) {
            noDataImage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noDataImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    public void showEditTaskDialog(Task task, int position) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.fragment_create_task); // Reuse the same layout
        dialog.setCanceledOnTouchOutside(false);
        reminderSpinner=dialog.findViewById(R.id.reminderSpinner);
        //bomb starts
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.reminder_times, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reminderSpinner.setAdapter(adapter);
        if(task.getReminderTime()!=null)
        {
            int spinnerPosition = adapter.getPosition(task.getReminderTime());
            reminderSpinner.setSelection(spinnerPosition);
        }
        //bomb ends
        // Initialize dialog views
        etTitle = dialog.findViewById(R.id.addTaskTitle);
        etDescription = dialog.findViewById(R.id.addTaskDescription);
        etDate = dialog.findViewById(R.id.taskDate);
        etTime = dialog.findViewById(R.id.taskTime);
        etEvent = dialog.findViewById(R.id.taskEvent);
        btAddTask = dialog.findViewById(R.id.addTask);
        btBack=dialog.findViewById(R.id.backBtn);
        TextView addTaskTextView=dialog.findViewById(R.id.addTaskTextView);
        addTaskTextView.setText("Edit Task");
        btAddTask.setText("Update Task"); // Change button text for editing
        etDate.setOnTouchListener((v, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(),
                        (view1, year, monthOfYear, dayOfMonth) ->
                                etDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year),
                        mYear, mMonth, mDay);

                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
            return true;
        });

        // Time picker for time field
        etTime.setOnTouchListener((v, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                        (view12, hourOfDay, minute) ->
                                etTime.setText(hourOfDay + ":" + minute),
                        mHour, mMinute, false);

                timePickerDialog.show();
            }
            return true;
        });
        // Populate fields with current task data
        etTitle.setText(task.getTitle());
        etDescription.setText(task.getDescription());
        etDate.setText(task.getDate());
        etTime.setText(task.getTime());
        etEvent.setText(task.getEvent());

        // Handle update task logic
        btAddTask.setOnClickListener(v -> {
            String updatedTitle = etTitle.getText().toString().trim();
            String updatedDescription = etDescription.getText().toString().trim();
            String updatedEvent = etEvent.getText().toString().trim();
            String updatedDate = etDate.getText().toString().trim();
            String updatedTime = etTime.getText().toString().trim();
            String selectedReminder=reminderSpinner.getSelectedItem().toString();
            if (!validateFields()) {
                if(getContext()!=null)
                {
                    //Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Update task in Firebase
                updateTaskInFirebase(task.getTaskId(), updatedTitle, updatedDescription, updatedEvent, updatedDate, updatedTime, selectedReminder,position);
                dialog.dismiss();
            }
        });
        btBack.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }
    private void updateTaskInFirebase(String taskId, String title, String description, String event, String date, String time,String reminderOption, int position) {
        if (firebaseUser == null) return;

        DatabaseReference taskRef = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(firebaseUser.getUid())
                .child(taskId);

        Task updatedTask = new Task(taskId, title, description, event, date, time, false,reminderOption);

        taskRef.setValue(updatedTask)
                .addOnSuccessListener(aVoid -> {
                    if(getContext()!=null)
                    {
                        Toast.makeText(TasksFragment.this.getContext(), "Task Updated Successfully!", Toast.LENGTH_SHORT).show();
                    }

                    // Update task in RecyclerView
                    taskList.set(position, updatedTask);
                    taskAdapter.notifyItemChanged(position);
                    if (!reminderOption.equals("Do not remind me")) {
                        long reminderOffset = getReminderTime(reminderOption);
                        if (reminderOffset >= 0) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                                Date taskDate = sdf.parse(date + " " + time);
                                if (taskDate != null) {
                                    Calendar reminderTime = Calendar.getInstance();
                                    reminderTime.setTime(taskDate);
                                    reminderTime.add(Calendar.MILLISECOND, -(int) reminderOffset);

                                    scheduleReminder(title, description, reminderTime);
                                }
                            } catch (Exception e) {
                                Log.e("ReminderError", "Error parsing date and time for reminder: " + e.getMessage());
                            }
                        } else {
                            Log.d("Reminder", "Reminder not scheduled for 'Do not remind me' option.");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if(getContext()!=null)
                    {
                        Toast.makeText(getContext(), "Failed to Update Task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Log.e("FirebaseError", "Error updating task: ", e);
                });
    }
    public void deleteTask(Task task, boolean isComplete) {
        if (firebaseUser == null) return;

        try {
            DatabaseReference taskRef = FirebaseDatabase.getInstance()
                    .getReference("tasks")
                    .child(firebaseUser.getUid())
                    .child(task.getTaskId());

            // Remove the task from Firebase
            taskRef.removeValue()
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful() && !isComplete) {
                            // The task was successfully deleted
                            if(getContext()!=null)
                            {
                                Toast.makeText(getContext(), "Task successfully deleted.", Toast.LENGTH_SHORT).show();
                            }
                            Log.d("Firebase", "Task successfully deleted.");
                        } else {
                            // Handle the failure
                            if (!isComplete) {
                                if(getContext()!=null)
                                {
                                    Toast.makeText(getContext(), "Failed to delete task.", Toast.LENGTH_SHORT).show();
                                }
                                Log.e("Firebase", "Failed to delete task.", task1.getException());
                            }
                        }
                    });
        } catch (Exception e) {
            // Handle any unexpected errors in deleting the task
            Log.e("Error", "Error in deleteTask", e);
            if(getContext()!=null)
            {
                Toast.makeText(getContext(), "An error occurred while deleting the task.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //delete Task
//    public void deleteTask(Task task,boolean isComplete) {
//        if (firebaseUser == null) return;
//
//        DatabaseReference taskRef = FirebaseDatabase.getInstance()
//                .getReference("tasks")
//                .child(firebaseUser.getUid())
//                .child(task.getTaskId());
//
//        // Remove the task from Firebase
//        taskRef.removeValue()
//                .addOnCompleteListener(task1 -> {
//                    if (task1.isSuccessful() && !isComplete) {
//                        // The task was successfully deleted
//                        Toast.makeText(getContext(), "Task successfully deleted.", Toast.LENGTH_SHORT).show();
//                        Log.d("Firebase", "Task successfully deleted.");
//                    } else {
//                        // Handle the failure
//                        if(!isComplete)
//                        {
//                            Toast.makeText(getContext(), "Failed to delete task.", Toast.LENGTH_SHORT).show();
//                            Log.e("Firebase", "Failed to delete task.", task1.getException());
//                        }
//                    }
//                });
//    }

    public void showDetails(Task task) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.activity_show_task_details);
        dialog.setCanceledOnTouchOutside(false);
        TextView det_title,det_description,det_date,det_time,det_event;
        Button det_back;
        det_title=dialog.findViewById(R.id.details_title);
        det_description=dialog.findViewById(R.id.details_description);
        det_date=dialog.findViewById(R.id.details_date);
        det_time=dialog.findViewById(R.id.details_time);
        det_event=dialog.findViewById(R.id.details_event);
        det_back=dialog.findViewById(R.id.details_back);
        det_title.setText(task.getTitle());
        det_description.setText(task.getDescription());
        det_date.setText(task.getDate());
        det_time.setText(task.getTime());
        det_event.setText(task.getEvent());
        det_back.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

//    public void completeTaskPopUp() {
//        //Toast.makeText(getContext(),"got here",Toast.LENGTH_LONG).show();
//        Dialog dialog=new Dialog(requireContext());
//        dialog.setContentView(R.layout.dialog_completed_theme);
//        dialog.setCanceledOnTouchOutside(false);
//        Button btnClose=dialog.findViewById(R.id.closeButton);
//        btnClose.setOnClickListener(view -> dialog.dismiss());
//        dialog.show();
//    }
public void completeTaskPopUp() {
    try {
        // Create a new dialog for the completed task
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_completed_theme);
        dialog.setCanceledOnTouchOutside(false);

        // Set up the close button in the dialog
        Button btnClose = dialog.findViewById(R.id.closeButton);
        btnClose.setOnClickListener(view -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    } catch (Exception e) {
        // Log the error and show a toast message to the user
        Log.e("Error", "Error in completeTaskPopUp", e);
        if(getContext()!=null)
        {
            Toast.makeText(getContext(), "An error occurred while displaying the popup.", Toast.LENGTH_SHORT).show();
        }
    }
}

}
//2nd time fixed