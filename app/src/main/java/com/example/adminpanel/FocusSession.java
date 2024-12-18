package com.example.adminpanel;

import android.content.ActivityNotFoundException;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.adminpanel.databinding.ActivityAddTaskBinding;
import com.example.adminpanel.databinding.ActivityFocusSessionBinding;

public class FocusSession extends AppCompatActivity {
    ActivityFocusSessionBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFocusSessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new StopwatchFragment());
        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.stopwatch)
            {
                replaceFragment(new StopwatchFragment());
            }
            else if(item.getItemId()==R.id.timer)
            {
                replaceFragment(new TimerFragment());
            }
            else if(item.getItemId()==R.id.history)
            {
                replaceFragment(new FocusHistoryFragment());
            }
            return true;
        });
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}