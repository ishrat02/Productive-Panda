package com.example.adminpanel;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

import io.reactivex.rxjava3.annotations.NonNull;

public class SettingsActivity extends AppCompatActivity {
    Switch bellSwitch;
    DatabaseReference databaseReference;
    FirebaseAuth authProfile;
    FirebaseUser firebaseUser;
    ImageView back;
    LinearLayout LogOut,about,deleteAccount;
    boolean previousBellState =false;
    private static final String CHANNEL_ID = "motivation_channel";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //requestNotificationPermission();
        authProfile=FirebaseAuth.getInstance();
        firebaseUser=authProfile.getCurrentUser();
        back=findViewById(R.id.bckBtn);
        LogOut=findViewById(R.id.logOut);
        deleteAccount=findViewById(R.id.deleteAccount);
        bellSwitch=findViewById(R.id.bellOption);
        about=findViewById(R.id.findAbout);
        databaseReference= FirebaseDatabase.getInstance()
                .getReference("DailyMotivation")
                .child(firebaseUser.getUid());
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this,AboutAcitvity.class));
            }
        });
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmAndDeleteUser();
            }
        });
        LogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authProfile.signOut();
                Intent intent = new Intent(SettingsActivity.this,SignUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Toast.makeText(SettingsActivity.this,"Logout Successful",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        //createNotificationChannel();
        fetchSwitchState();
        bellSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the switch state in the database
            databaseReference.child("bellEnabled").setValue(isChecked);

            if (isChecked) {
                // If the switch is ON, show the time picker dialog
                if(!previousBellState)
                {
                    showTimePickerDialog();
                }
            } else {
                // Optionally, clear the stored time if switch is OFF
                databaseReference.child("time").setValue(null);
                cancelNotification();
            }
            previousBellState=isChecked;
        });
    }
    private void confirmAndDeleteUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser != null ? firebaseUser.getUid() : null;
        String userEmail = firebaseUser != null ? firebaseUser.getEmail() : null;

        if (userId == null || userEmail == null) {
            Log.e("DeleteUser", "User is not logged in or email is null.");
            Toast.makeText(this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Confirmation Dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Prompt user to re-enter their password
                    promptForPassword(userId, firebaseUser, userEmail);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void promptForPassword(String userId, FirebaseUser firebaseUser, String userEmail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Re-enter Password");

        // Input field for password
        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordInput);

        // Confirm Button
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String userPassword = passwordInput.getText().toString().trim();
            if (!userPassword.isEmpty()) {
                // Show progress dialog
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Deleting Account");
                progressDialog.setMessage("Please wait while we delete your account...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Perform account deletion
                reauthenticateAndDeleteUser(userId, progressDialog, firebaseUser, userEmail, userPassword);
            } else {
                Toast.makeText(this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel Button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void reauthenticateAndDeleteUser(String userId, ProgressDialog progressDialog, FirebaseUser firebaseUser, String userEmail, String userPassword) {
        // Re-authenticate user
        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, userPassword);
        firebaseUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Reauthentication", "User re-authenticated successfully.");
                    deleteUser(userId, progressDialog, firebaseUser);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e("Reauthentication", "Re-authentication failed", e);
                    Toast.makeText(this, "Authentication failed. Please check your password and try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteUser(String userId, ProgressDialog progressDialog, FirebaseUser firebaseUser) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // List of parent nodes in Realtime Database to delete
        String[] parentNodes = {
                "Completed tasks",
                "DailyMotivation",
                "FlashCard Topics",
                "Focus Sessions",
                "Registered Users",
                "tasks"
        };

        // Delete from Realtime Database
        for (String node : parentNodes) {
            database.child(node).child(userId).removeValue()
                    .addOnSuccessListener(aVoid -> Log.d("DeleteUserData", "Data deleted successfully from " + node))
                    .addOnFailureListener(e -> Log.e("DeleteUserData", "Failed to delete data from " + node, e));
        }

        // Delete from Firestore
        firestore.collection("notes").document(userId).delete()
                .addOnSuccessListener(aVoid -> Log.d("DeleteFirestore", "User data deleted from Firestore"))
                .addOnFailureListener(e -> Log.e("DeleteFirestore", "Failed to delete user data from Firestore", e));

        // Delete the user's profile picture from Firebase Storage
        StorageReference profilePicRef = storage.getReference("Profile Pics").child(userId);

        profilePicRef.delete()
                .addOnSuccessListener(aVoid -> Log.d("DeleteStorage", "Profile picture deleted successfully"))
                .addOnFailureListener(e -> Log.e("DeleteStorage", "Failed to delete profile picture", e));

        // Delete user from Firebase Authentication
        firebaseUser.delete()
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Log.d("DeleteUserAuth", "User account deleted successfully");
                    Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();

                    // Navigate to the SignUpActivity
                    Intent intent = new Intent(SettingsActivity.this, SignUpActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    Toast.makeText(SettingsActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e("DeleteUserAuth", "Failed to delete user account", e);
                    Toast.makeText(this, "Failed to delete account. Please try again later.", Toast.LENGTH_SHORT).show();
                });
    }

//    private void requestNotificationPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
//            }
//        }
//    }
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == 1) { // The request code used when requesting the permission
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, you can now show notifications
//                Log.d("SettingsActivity", "Permission granted for notifications");
//            } else {
//                // Permission denied, handle appropriately
//                Log.d("SettingsActivity", "Permission denied for notifications");
//            }
//        }
//    }

//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "Motivation Channel";
//            String description = "Channel for daily motivational notifications";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel("MotivationChannel", name, importance);
//            channel.setDescription(description);
//
//            // Register the channel with the system
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            if (notificationManager != null) {
//                Toast.makeText(SettingsActivity.this,"Here",Toast.LENGTH_SHORT).show();
//                notificationManager.createNotificationChannel(channel);
//            }
//        }
//    }

    //time set korata thik ase
    private void scheduleDailyNotification(String time) {
        String[] timeParts = time.split(" ");  // Split the time and AM/PM part
        String[] hourMinute = timeParts[0].split(":");  // Split hours and minutes
        int hour = Integer.parseInt(hourMinute[0]);
        int minute = Integer.parseInt(hourMinute[1]);
        String period = timeParts[1];  // Get AM/PM part

        // Convert the time to 24-hour format
        if (period.equals("PM") && hour != 12) {
            hour += 12;
        } else if (period.equals("AM") && hour == 12) {
            hour = 0;  // Handle 12 AM case
        }

        // Set the calendar object to the specified time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        // Get the AlarmManager system service
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Prepare the intent to trigger the notification
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("action", "show_quote");
        // Use FLAG_IMMUTABLE if you don't need to modify the PendingIntent later
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE  // Ensure PendingIntent is immutable
        );

        // Schedule the daily notification using AlarmManager
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,  // Use RTC_WAKEUP to wake up the device if it's asleep
                    calendar.getTimeInMillis(),  // Start time in milliseconds
                    AlarmManager.INTERVAL_DAY,  // Repeat every 24 hours
                    pendingIntent  // The PendingIntent to trigger the notification
            );
            //createNotificationChannel();
            Toast.makeText(this, "Notification scheduled for " + time, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to schedule notification", Toast.LENGTH_SHORT).show();
        }
    }


    private void cancelNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);

        // Use FLAG_IMMUTABLE as you're not modifying the PendingIntent later
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);  // Cancel any scheduled notification
            Log.d("Notification", "Notification cancelled");
        } else {
            Toast.makeText(this, "Failed to cancel notification", Toast.LENGTH_SHORT).show();
        }
    }


    // Show the time picker dialog with 12-hour format
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR); // Use HOUR for 12-hour format
        int minute = calendar.get(Calendar.MINUTE);

        // Create TimePickerDialog with 12-hour format
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                SettingsActivity.this,
                (view, hourOfDay, minute1) -> {
                    String period = (hourOfDay < 12) ? "AM" : "PM"; // Determine AM/PM
                    int hour12 = (hourOfDay == 0) ? 12 : (hourOfDay > 12 ? hourOfDay - 12 : hourOfDay); // Convert to 12-hour format
                    String time = String.format("%02d:%02d %s", hour12, minute1, period); // Format time as 12-hour
                    databaseReference.child("time").setValue(time);
                    //Toast.makeText(SettingsActivity.this, "Time Set: " + time, Toast.LENGTH_SHORT).show();
                    scheduleDailyNotification(time);
                },
                hour,
                minute,
                false // Set to false for 12-hour format
        );
        timePickerDialog.show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        fetchSwitchState();
    }
    private void fetchSwitchState() {
        // Fetch the switch state from the database
        databaseReference.child("bellEnabled").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Boolean bellEnabled = task.getResult().getValue(Boolean.class);

                // If the value is null, we default to false
                if (bellEnabled != null) {
                    bellSwitch.setChecked(bellEnabled);
                    previousBellState = bellEnabled;
                } else {
                    bellSwitch.setChecked(false);
                    previousBellState = false;
                    // Optionally, you can set this value in the database if it's not set yet.
                    databaseReference.child("bellEnabled").setValue(false);
                }

                // If the switch is ON, fetch the saved time
                if (bellEnabled != null && bellEnabled) {
                    fetchTime();
                }
            } else {
                // If there was an error fetching data, handle it
                Toast.makeText(SettingsActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchTime() {
        databaseReference.child("time").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String savedTime = task.getResult().getValue(String.class);
                if (savedTime != null) {
                    //Toast.makeText(SettingsActivity.this, "Saved Time: " + savedTime, Toast.LENGTH_SHORT).show();
                    scheduleDailyNotification(savedTime);
                }
            }
        });
    }
}