package com.example.adminpanel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StopwatchFragment extends Fragment {
    Chronometer chronometer;
    ImageView btStart, btStop,backButton;
    private boolean isResume;
    Handler handler;
    long tMilliSec, tStart, tBuff, tUpdate = 0L;
    int hour, sec, min, milliSec;
    private List<Session> sessionList;
    private FirebaseUser firebaseUser;
    private FirebaseAuth authProfile;
    public StopwatchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        try {
            view = inflater.inflate(R.layout.fragment_stopwatch, container, false);

            // Initialize views
            try {
                backButton=view.findViewById(R.id.backButton);
                chronometer = view.findViewById(R.id.chronometer);
                btStart = view.findViewById(R.id.bt_start);
                btStop = view.findViewById(R.id.bt_stop);
                authProfile = FirebaseAuth.getInstance();
                firebaseUser = authProfile.getCurrentUser();
                handler = new Handler();
            } catch (Exception e) {
                e.printStackTrace();
                // Log or handle view initialization errors
            }
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
            // Start/Pause button logic
            btStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (!isResume) {
                            // Start the stopwatch
                            tStart = SystemClock.uptimeMillis();
                            handler.postDelayed(runnable, 0);
                            chronometer.start();
                            isResume = true;

                            // Change UI for "pause" state
                            btStop.setVisibility(View.GONE);
                            btStart.setImageDrawable(getResources().getDrawable(
                                    R.drawable.ic_pause
                            ));
                        } else {
                            // Pause the stopwatch
                            tBuff += tMilliSec;
                            handler.removeCallbacks(runnable);
                            chronometer.stop();
                            isResume = false;

                            // Change UI for "play" state
                            btStop.setVisibility(View.VISIBLE);
                            btStart.setImageDrawable(getResources().getDrawable(
                                    R.drawable.ic_play
                            ));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Log or handle button click logic errors
                    }
                }
            });

            // Stop/Reset button logic
            btStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (!isResume) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                            // Get the current date
                            String currentDate = sdf.format(new Date());
                            String formattedDuration = String.format("Date:"+currentDate+", "+"%02dhr:%02dmin:%02dsec", hour, min, sec);
                            showSaveSessionDialog(formattedDuration);
                            handler.removeCallbacks(runnable);
                            tMilliSec = tStart = tBuff = tUpdate = 0L;
                            chronometer.setText("00:00:00:00");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Log or handle button click logic errors
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            // Log or handle fragment view creation errors
        }

        return view;
    }

    private void showSaveSessionDialog(String duration) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.time_add_dialog, null);
            builder.setView(dialogView);

            EditText timerNameBox = dialogView.findViewById(R.id.timerNameBox);
            EditText timerDescBox = dialogView.findViewById(R.id.timerDescBox);
            AppCompatButton btnSave = dialogView.findViewById(R.id.btnReset);
            AppCompatButton btnCancel = dialogView.findViewById(R.id.btnCancel);

            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            btnSave.setOnClickListener(v -> {
                try {
                    String title = timerNameBox.getText().toString().trim();
                    String description = timerDescBox.getText().toString().trim();
                    if (!title.isEmpty() && !description.isEmpty()) {
                        //database part
                        FirebaseDatabase database=FirebaseDatabase.getInstance();
                        DatabaseReference focusRef=database.getReference("Focus Sessions").child(firebaseUser.getUid());
                        DatabaseReference newFocusRef=focusRef.push();
                        String focusId=newFocusRef.getKey();//ekhane e id pacche -_-
                        Session session=new Session(focusId,title,description,duration);
                        newFocusRef.setValue(session).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                // Save session to FocusHistory
                                Toast.makeText(StopwatchFragment.this.getContext(), "Uploading...", Toast.LENGTH_SHORT).show();
                                //FocusHistoryFragment.addSession(session);//ekhane ar add korlam na
                                Toast.makeText(StopwatchFragment.this.getContext(), "Session Added Successfully!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                            else{
                                task.getException().printStackTrace();
                            }
                        });
                    }else{
                        timerNameBox.setError("Title is required");
                        timerDescBox.setError("Description is required");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Log or handle save session errors
                }
            });

            btnCancel.setOnClickListener(v -> {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    // Log or handle dialog dismissal errors
                }
            });

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            // Log or handle dialog display errors
        }
    }

    // Runnable for updating the stopwatch
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                tMilliSec = SystemClock.uptimeMillis() - tStart; // Calculate elapsed time in milliseconds
                tUpdate = tBuff + tMilliSec;                    // Total elapsed time including paused state
                sec = (int) (tUpdate / 1000);                   // Convert to seconds
                min = sec / 60;                                 // Convert seconds to minutes
                hour = min / 60;                                // Convert minutes to hours
                min = min % 60;                                 // Remaining minutes after extracting hours
                sec = sec % 60;                                 // Remaining seconds after extracting minutes
                milliSec = (int) ((tUpdate % 1000) / 10);       // Limit to 2-digit precision (0-99)

                // Update the chronometer text with hours, minutes, seconds, and milliseconds
                chronometer.setText(
                        String.format("%02d:%02d:%02d:%02d", hour, min, sec, milliSec)
                );

                // Re-run the runnable every 60ms for smooth updates
                handler.postDelayed(this, 60);
            } catch (Exception e) {
                e.printStackTrace();
                // Log or handle runnable logic errors
            }
        }
    };
}
