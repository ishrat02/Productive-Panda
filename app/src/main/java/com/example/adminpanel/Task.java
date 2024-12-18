package com.example.adminpanel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task {
    String taskId;
    String title;
    String description;
    String event;
    String date;
    String time;
    private String reminderTime;
    boolean isComplete;

    // Default constructor required for calls to DataSnapshot.getValue(Task.class)
    public Task() {
    }
    public Task(String date) {
        this.date = date;
    }
    public Task(String taskId,String title, String description, String event, String date, String time,boolean isComplete)//shob thik thak korar por eita delete mere dibo
    {
        this.taskId=taskId;
        this.title = title;
        this.description = description;
        this.event = event;
        this.date = date;
        this.time = time;
        this.isComplete=isComplete;
    }
    public Task(String taskId,String title, String description, String event, String date, String time,boolean isComplete,String reminderTime) {
        this.taskId=taskId;
        this.title = title;
        this.description = description;
        this.event = event;
        this.date = date;
        this.time = time;
        this.isComplete=isComplete;
        this.reminderTime=reminderTime;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public Task(String taskTitle, String taskDate) {
        this.title=taskTitle;
        this.date=taskDate;
    }

    public boolean isComplete() {
        return isComplete;
    }
    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getTaskId(){
        return taskId;
    }
    public void setTaskId(String taskId)
    {
        this.taskId=taskId;
    }
    public String getMonthOfWeek(String dateString) {
        try{
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            SimpleDateFormat outputFormat=new SimpleDateFormat("MMM",Locale.getDefault());
            return outputFormat.format(date);
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public String getDateOfWeek(String dateString) {
        try{
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            SimpleDateFormat outputFormat=new SimpleDateFormat("dd",Locale.getDefault());
            return outputFormat.format(date);
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public String getDayOfWeek(String dateString) {
        try {
            // Define the input date format
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            // Parse the input date
            Date date = inputFormat.parse(dateString);

            // Define the output format for the day of the week (e.g., "Sunday")
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

            // Format and return the day of the week
            return dayFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
