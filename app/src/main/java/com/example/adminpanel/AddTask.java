package com.example.adminpanel;

import static com.example.adminpanel.R.id.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.adminpanel.HistoryFragment;
import com.example.adminpanel.R;
import com.example.adminpanel.TasksFragment;
import com.example.adminpanel.databinding.ActivityAddTaskBinding;

public class AddTask extends AppCompatActivity {

    ActivityAddTaskBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new TasksFragment());
        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.home)
            {
                replaceFragment(new TasksFragment());
            }
            else if(item.getItemId()==R.id.shorts)
            {
                replaceFragment(new HistoryFragment());
            }
//            switch (item.getItemId()) {
//                case R.id.home:
//                    replaceFragment(new TasksFragment());
//                    break;
//
//                case R.id.shorts:
//                    replaceFragment(new HistoryFragment());
//                    break;
//
//            }

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