package com.example.adminpanel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private AutoCompleteTextView autoCompleteSearch;
    private ArrayAdapter<String>adapter;
    FirebaseAuth authProfile;
    FirebaseUser firebaseUser;
    ConstraintLayout toDo, timerSession, journal, flashCards;
    LinearLayout showProfile,settings,FAQ;
    ImageView profilePic;
    TextView nameUser;
    SwipeRefreshLayout swipeRefreshLayout;
    HorizontalScrollView horizontalScrollView;
    Button btnRight,btnLeft;
    LinearLayout container;
    private int childWidth=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        autoCompleteSearch = findViewById(R.id.autoCompleteSearch);
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        toDo = findViewById(R.id.toDo);
        timerSession = findViewById(R.id.focusTimer);
        journal = findViewById(R.id.journal);
        flashCards = findViewById(R.id.flashCards);
        showProfile = findViewById(R.id.showProfile);
        settings=findViewById(R.id.settingsLayout);
        FAQ=findViewById(R.id.faqOption);
        nameUser = findViewById(R.id.nameUser);
        profilePic = findViewById(R.id.profilePic);
        horizontalScrollView = findViewById(R.id.horizontalScrollView3);
        container = (LinearLayout) horizontalScrollView.getChildAt(0);
        btnLeft = findViewById(R.id.btnScrollLeft);
        btnRight = findViewById(R.id.btnScrollRight);
        // Set Click Listeners
        showProfile.setOnClickListener(this);
        toDo.setOnClickListener(this);
        timerSession.setOnClickListener(this);
        journal.setOnClickListener(this);
        flashCards.setOnClickListener(this);
        settings.setOnClickListener(this);
        FAQ.setOnClickListener(this);
        final List<String> options = new ArrayList<>();
        options.add("Profile");
        options.add("Settings");
        options.add("FAQ");
        options.add("Todolist");
        options.add("Focus Timer");
        options.add("Journal");
        options.add("FlashCards");
        options.add("About");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, options);
        autoCompleteSearch.setAdapter(adapter);

        // Set threshold for showing dropdown after 1 character
        autoCompleteSearch.setThreshold(1);

        // Handle item click and redirect to respective activities
        autoCompleteSearch.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String selectedOption = parent.getItemAtPosition(position).toString();

            // Redirect based on the selected option
            switch (selectedOption) {
                case "Profile":
                    startActivity(new Intent(MainActivity.this,EditProfile.class));
                    break;
                case "Settings":
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    break;
                case "FAQ":
                    startActivity(new Intent(MainActivity.this, FAQActivity.class));
                    break;
                case "Todolist":
                    startActivity(new Intent(MainActivity.this,AddTask.class));
                    break;
                case "Focus Timer":
                    startActivity(new Intent(MainActivity.this, FocusSession.class));
                    break;
                case "Journal":
                    startActivity(new Intent(MainActivity.this, JournalActivity.class));
                    break;
                case "FlashCards":
                    startActivity(new Intent(MainActivity.this, StickyNoteActivity.class));
                    break;
                    case "About":
                    startActivity(new Intent(MainActivity.this, AboutAcitvity.class));
                    break;
                default:
                    break;
            }
        });
        loadUserProfile();

        // Set SwipeRefreshLayout Listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadUserProfile(); // Refresh profile data
            swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
        });
        horizontalScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (container.getChildCount() > 0) {
                    childWidth = container.getChildAt(0).getWidth();
                }
                horizontalScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // Set click listeners for buttons
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollLeft();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollRight();
            }
        });
    }

    private void loadUserProfile() {
        if (firebaseUser != null) {
            String userID = firebaseUser.getUid();

            // Load Profile Picture
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference("Profile Pics")
                    .child(firebaseUser.getUid());
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(MainActivity.this)
                        .load(uri)
                        .apply(new RequestOptions().override(100, 100).circleCrop())
                        .into(profilePic);
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, "Failed to load profile picture", Toast.LENGTH_SHORT).show();
            });

            // Load User Name
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance()
                    .getReference("Registered Users");
            referenceProfile.child(userID).child("name").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        nameUser.setText(name);
                    } else {
                        Toast.makeText(MainActivity.this, "Name not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load user name", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.toDo) {
            startActivity(new Intent(MainActivity.this, AddTask.class));
        } else if (view.getId() == R.id.focusTimer) {
            startActivity(new Intent(MainActivity.this, FocusSession.class));
        } else if (view.getId() == R.id.journal) {
            startActivity(new Intent(MainActivity.this, JournalActivity.class));
        } else if (view.getId() == R.id.flashCards) {
            startActivity(new Intent(MainActivity.this, StickyNoteActivity.class));
        } else if (view.getId() == R.id.showProfile) {
            startActivity(new Intent(MainActivity.this, EditProfile.class));
        }else if (view.getId() == R.id.settingsLayout) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }else if (view.getId() == R.id.faqOption) {
            startActivity(new Intent(MainActivity.this, FAQActivity.class));
        }
    }
    private void scrollLeft() {
        if (horizontalScrollView != null && childWidth > 0) {
            int scrollX = horizontalScrollView.getScrollX();
            int newScrollX = Math.max(scrollX - childWidth, 0);
            horizontalScrollView.smoothScrollTo(newScrollX, 0);
        }
    }

    private void scrollRight() {
        if (horizontalScrollView != null && childWidth > 0) {
            int scrollX = horizontalScrollView.getScrollX();
            int newScrollX = Math.min(scrollX + childWidth, container.getWidth() - horizontalScrollView.getWidth());
            horizontalScrollView.smoothScrollTo(newScrollX, 0);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialogInterface, i) -> finish())
                .setNegativeButton("No", null)
                .show();
    }
}
