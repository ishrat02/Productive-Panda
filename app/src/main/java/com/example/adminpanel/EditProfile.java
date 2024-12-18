package com.example.adminpanel;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImage;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.yalantis.ucrop.view.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class EditProfile extends AppCompatActivity {
    private boolean flag=false;
    private EditText etName,etUsername,etGender;
    private TextView etEmail;
    private ProgressBar progressBar;
    private String name,username,email,gender;
    private FirebaseAuth authProfile;
    private Button editProfileBtn;
    private DatabaseReference referenceProfile;
    private FirebaseUser firebaseUser;
    private ActivityResultLauncher<Intent>imagePickLauncher;
    private Uri selectedImageUri;
    private ImageView profileImageView,backButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    //String myUri="";
    //StorageTask uploadTask;
    //StorageReference storageProfilePicsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        swipeRefreshLayout=findViewById(R.id.swipe_refresh_layout);
        referenceProfile=FirebaseDatabase.getInstance().getReference("Registered Users");
        etName=findViewById(R.id.profile_name);
        etUsername=findViewById(R.id.profile_username);
        etEmail=findViewById(R.id.profile_email);
        etGender=findViewById(R.id.profile_gender);
        editProfileBtn=findViewById(R.id.profile_update_btn);
        progressBar=findViewById(R.id.profile_progress_bar);
        authProfile=FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();
        backButton=findViewById(R.id.back_button);
        //for image
        //storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("Profile Pic").child(firebaseUser.getUid());//not used yet
        profileImageView = findViewById(R.id.profile_image_view);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);  // Reload user profile data on swipe refresh
        });
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result->{
                    if(result.getResultCode()== AppCompatActivity.RESULT_OK){
                        Intent data = result.getData();
                        if(data!=null & data.getData()!=null){
                            selectedImageUri=data.getData();
                            AndroidUtil.setProfilePic(this,selectedImageUri,profileImageView);
                        }
                    }
                }
                );
        if(firebaseUser==null)
        {
            Toast.makeText(EditProfile.this,"Something went wrong!User's details are not available at the moment",Toast.LENGTH_LONG).show();
        }
        else
        {
            progressBar.setVisibility(View.VISIBLE);
            //Toast.makeText(EditProfile.this,"Please wait...",Toast.LENGTH_LONG).show();
            showUserProfile(firebaseUser);
        }
        showData();
        //for profile picture upload
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(EditProfile.this).cropSquare().compress(512).maxResultSize(512,512)
                        .createIntent(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent intent) {
                                imagePickLauncher.launch(intent);
                                return null;
                            }
                        });
            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                if(selectedImageUri!=null){
                    uploadProfileImage();
                }
                else{
                    updateUserProfileData(null);
                }
            }
        });
        //swipeRefreshLayout.setRefreshing(false);
    }

    private void updateUserProfileData(String imageUrl) {
        if(isNameChanged())
        {
            flag=true;
        }
        if(isGenderChanged())
        {
            flag=true;
        }
        if(isUsernameChanged())
        {
            flag=true;
        }
        if(imageUrl!=null){
            referenceProfile.child(firebaseUser.getUid()).child("profileImageUrl").setValue(imageUrl);
            flag=true;
        }
        if(flag)
        {
            Toast.makeText(EditProfile.this,"Saved",Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
        else
        {
            Toast.makeText(EditProfile.this,"No Changes Found",Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void uploadProfileImage() {
        StorageReference storageReference=FirebaseStorage.getInstance().getReference("Profile Pics")
                .child(firebaseUser.getUid());
        storageReference.putFile(selectedImageUri).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateUserProfileData(uri.toString());
                });
            }else{
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditProfile.this,"Image upload failed",Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public boolean isNameChanged()
    {
        if(!name.equals(etName.getText().toString()))
        {
            referenceProfile.child(firebaseUser.getUid()).child("name").setValue(etName.getText().toString());
            name=etName.getText().toString();
            return true;
        }
        else
        {
            return false;
        }
    }
    public boolean isGenderChanged()
    {
        if(!gender.equals(etGender.getText().toString()))
        {
            String temp=etGender.getText().toString();
            if(!(temp.equals("Female")|| temp.equals("Male")))
            {
                //Toast.makeText(EditProfile.this,"Enter Male or Female",Toast.LENGTH_SHORT).show();
                etGender.setError("Should be \"Male\" or \"Female\"");
                return false;
            }
            referenceProfile.child(firebaseUser.getUid()).child("gender").setValue(etGender.getText().toString());
            gender=etGender.getText().toString();
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isUsernameChanged()
    {
        if(!username.equals(etUsername.getText().toString()))
        {
            referenceProfile.child(firebaseUser.getUid()).child("username").setValue(etUsername.getText().toString());
            username=etUsername.getText().toString();
            return true;
        }
        else
        {
            return false;
        }
    }
    public void showData()
    {
        Intent intent = getIntent();
        name = intent.getStringExtra("name") != null ? intent.getStringExtra("name") : "";
        username = intent.getStringExtra("username") != null ? intent.getStringExtra("username") : "";
        gender = intent.getStringExtra("gender") != null ? intent.getStringExtra("gender") : "";
        etName.setText(name);
        etGender.setText(gender);
        etUsername.setText(username);

    }
    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID=firebaseUser.getUid();
        //Extracting User reference from Database for "Registered Users"
        referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetails!=null)
                {
                    email=firebaseUser.getEmail();
                    username=readUserDetails.username;
                    name=readUserDetails.name;
                    gender=readUserDetails.gender;
                    etEmail.setText(email);
                    etUsername.setText(username);
                    etName.setText(name);
                    etGender.setText(gender);
                    if (snapshot.hasChild("profileImageUrl")) {
                        String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                        if (profileImageUrl != null) {
                            // Load the image using Glide
                            Glide.with(EditProfile.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.user_default_profile_pic) // Optional: Placeholder image
                                    .error(R.drawable.user_default_profile_pic)      // Optional: Error image
                                    .override(512, 512)
                                    .circleCrop()
                                    .into(profileImageView);
                        }
                    }
                }
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfile.this,"Something went wrong!",Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}