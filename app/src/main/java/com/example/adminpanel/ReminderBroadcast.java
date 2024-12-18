package com.example.adminpanel;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderBroadcast extends BroadcastReceiver {
    @Override
    @SuppressLint("NewApi")
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("title");
        String taskDescription = intent.getStringExtra("description");
        if(taskTitle==null || taskTitle.isEmpty()){
            taskTitle="A Task";
        }
        if(taskDescription==null||taskDescription.isEmpty())
        {
            taskDescription="Task Description";
        }
        NotificationManager notificationManager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId="ReminderChannel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Tasks Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for task reminders");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        Notification notification= new NotificationCompat.Builder(context,channelId)
                .setContentTitle("Task Reminder")
                .setContentText("Reminder : "+taskTitle)
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        if(notificationManager!=null){
            notificationManager.notify((int)System.currentTimeMillis(),notification);
        }
    }
}
