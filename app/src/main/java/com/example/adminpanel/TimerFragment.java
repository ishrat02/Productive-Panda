package com.example.adminpanel;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TimerFragment extends Fragment {

    private TextView tvTimeLeft, tvAddTime;
    private ImageButton ibReset;
    private AppCompatButton btnPlayPause;
    private ImageButton btnAddTimer;
    private ProgressBar pbTimer;

    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private long timeLeftInMillis = 60000; // Default 1 minute
    private long timerMaxMillis = 60000;  // Total time (used to calculate progress)
    private FirebaseUser firebaseUser;
    private FirebaseAuth authProfile;
    private static final String TAG = "TimerFragment";

    public TimerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        try {
            tvTimeLeft = view.findViewById(R.id.tvTimeLeft);
            ibReset = view.findViewById(R.id.ib_reset);
            btnPlayPause = view.findViewById(R.id.btnPlayPause);
            btnAddTimer = view.findViewById(R.id.btnAddTimer);
            pbTimer = view.findViewById(R.id.pbTimer);
            tvAddTime = view.findViewById(R.id.tv_addTime);
            authProfile = FirebaseAuth.getInstance();
            firebaseUser = authProfile.getCurrentUser();
            pbTimer.setMax((int) (timerMaxMillis / 1000)); // Set max to total seconds
            pbTimer.setProgress((int) (timeLeftInMillis / 1000)); // Set initial progress

            updateTimerText();

            btnPlayPause.setOnClickListener(v -> {
                try {
                    if (isRunning) {
                        pauseTimer();
                    } else {
                        startTimer();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in start/pause timer button: ", e);
                }
            });

            ibReset.setOnClickListener(v -> {
                try {
                    resetTimer();
                } catch (Exception e) {
                    Log.e(TAG, "Error in reset button: ", e);
                }
            });

            btnAddTimer.setOnClickListener(v -> {
                try {
                    showAddTimeDialog();
                } catch (Exception e) {
                    Log.e(TAG, "Error in add timer button: ", e);
                }
            });

            tvAddTime.setOnClickListener(v -> {
                try {
                    timeLeftInMillis += 15000;
                    timerMaxMillis += 15000;
                    pbTimer.setMax((int) (timerMaxMillis / 1000));
                    pbTimer.setProgress((int) (timeLeftInMillis / 1000));
                    updateTimerText2(timerMaxMillis);
                    if (isRunning) {
                        startTimer();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in +15s button: ", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views or setting listeners: ", e);
        }

        return view;
    }

    private void startTimer() {
        try {
            cancelExistingTimer();

            countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    try {
                        timeLeftInMillis = millisUntilFinished;
                        updateTimerText();

                        int progress = (int) (timeLeftInMillis / 1000);
                        pbTimer.setProgress(progress);
                    } catch (Exception e) {
                        Log.e(TAG, "Error during timer tick: ", e);
                    }
                }

                @Override
                public void onFinish() {
                    try {
                        isRunning = false;
                        btnPlayPause.setText("Start");

                        pbTimer.setProgress(0);
                        showSessionDialog();
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Time's Up!")
                                .setMessage("You focused for " +(timerMaxMillis / 1000) / 3600 + " hours "+ (((timerMaxMillis / 1000)%3600)/60) + " minutes "+((timerMaxMillis / 1000)%60) + " seconds.")
                                .setPositiveButton("OK", (dialog2, which) -> dialog2.dismiss())
                                .create()
                                .show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error when timer finishes: ", e);
                    }
                }
            }.start();

            isRunning = true;
            btnPlayPause.setText("Pause");
        } catch (Exception e) {
            Log.e(TAG, "Error starting timer: ", e);
        }
    }

    private void pauseTimer() {
        try {
            cancelExistingTimer();
            isRunning = false;
            btnPlayPause.setText("Start");
        } catch (Exception e) {
            Log.e(TAG, "Error pausing timer: ", e);
        }
    }

    private void resetTimer() {
        try {
            cancelExistingTimer();
            timeLeftInMillis = timerMaxMillis;
            updateTimerText();
            pbTimer.setProgress((int) (timeLeftInMillis / 1000));
            isRunning = false;
            btnPlayPause.setText("Start");
        } catch (Exception e) {
            Log.e(TAG, "Error resetting timer: ", e);
        }
    }

    private void showAddTimeDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Set Timer");

            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_timer_dialog, null);
            builder.setView(dialogView);

            EditText etHours = dialogView.findViewById(R.id.etHours);
            EditText etMinutes = dialogView.findViewById(R.id.etMinutes);
            EditText etSeconds = dialogView.findViewById(R.id.etSeconds);

            builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        String hoursText = etHours.getText().toString();
                        String minutesText = etMinutes.getText().toString();
                        String secondsText = etSeconds.getText().toString();

                        int hours = TextUtils.isEmpty(hoursText) ? 0 : Integer.parseInt(hoursText);
                        int minutes = TextUtils.isEmpty(minutesText) ? 0 : Integer.parseInt(minutesText);
                        int seconds = TextUtils.isEmpty(secondsText) ? 0 : Integer.parseInt(secondsText);

                        if (hours > 99999 || minutes > 59 || seconds > 59) {
                            TimerFragment.this.showErrorDialog("Invalid time input. Check hours, minutes, and seconds.");
                            return;
                        }

                        long newTimeInMillis = (hours * 3600L + minutes * 60L + seconds) * 1000L;
                        if (newTimeInMillis > 0) {
                            timeLeftInMillis = newTimeInMillis;
                            timerMaxMillis = newTimeInMillis;

                            TimerFragment.this.updateTimerText();
                            pbTimer.setMax((int) (timerMaxMillis / 1000));
                            pbTimer.setProgress((int) (timeLeftInMillis / 1000));

                            if (isRunning) {
                                TimerFragment.this.startTimer();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting timer in dialog: ", e);
                    }
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing add time dialog: ", e);
        }
    }

    private void showErrorDialog(String message) {
        try {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Invalid Input")
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing error dialog: ", e);
        }
    }

    private void updateTimerText() {
        try {
            int hours = (int) (timeLeftInMillis / 1000) / 3600;
            int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
            int seconds = (int) (timeLeftInMillis / 1000) % 60;

            String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            tvTimeLeft.setText(timeFormatted);
        } catch (Exception e) {
            Log.e(TAG, "Error updating timer text: ", e);
        }
    }

    private void updateTimerText2(long time) {
        try {
            int hours = (int) (time / 1000) / 3600;
            int minutes = (int) ((time / 1000) % 3600) / 60;
            int seconds = (int) (time / 1000) % 60;

            String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            tvTimeLeft.setText(timeFormatted);
        } catch (Exception e) {
            Log.e(TAG, "Error updating max timer text: ", e);
        }
    }

    private void cancelExistingTimer() {
        try {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling existing timer: ", e);
        }
    }

    private void showSessionDialog() {
        try {
            //let's try to save it
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.time_add_dialog, null);
            builder.setView(dialogView);

            EditText timerNameBox = dialogView.findViewById(R.id.timerNameBox);
            EditText timerDescBox = dialogView.findViewById(R.id.timerDescBox);
            AppCompatButton btnSave = dialogView.findViewById(R.id.btnReset);
            AppCompatButton btnCancel = dialogView.findViewById(R.id.btnCancel);
            int hours = (int) (timerMaxMillis / 1000) / 3600;
            int minutes = (int) ((timerMaxMillis / 1000) % 3600) / 60;
            int seconds = (int) (timerMaxMillis / 1000) % 60;
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            // Get the current date
            String currentDate = sdf.format(new Date());
            String duration = String.format("Date:"+currentDate+", "+"%02dhr:%02dmin:%02dsec", hours, minutes, seconds);
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            btnSave.setOnClickListener(v -> {
                try {
                    String title = timerNameBox.getText().toString().trim();
                    String description = timerDescBox.getText().toString().trim();

                    if (!title.isEmpty() && !description.isEmpty()) {
                        //Firebase part
                        FirebaseDatabase database=FirebaseDatabase.getInstance();
                        DatabaseReference focusRef=database.getReference("Focus Sessions").child(firebaseUser.getUid());
                        DatabaseReference newFocusRef=focusRef.push();
                        String focusId=newFocusRef.getKey();
                        Session session=new Session(focusId,title,description,duration);
                        newFocusRef.setValue(session).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                Toast.makeText(TimerFragment.this.getContext(),"Uploading...",Toast.LENGTH_SHORT).show();
                                Toast.makeText(TimerFragment.this.getContext(),"Session Added Successfully",Toast.LENGTH_SHORT).show();
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
            //shob sheshe
        } catch (Exception e) {
            Log.e(TAG, "Error showing session dialog: ", e);
        }
    }
}
