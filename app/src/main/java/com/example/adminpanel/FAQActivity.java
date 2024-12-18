package com.example.adminpanel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FAQActivity extends AppCompatActivity {
    ImageView back;
    TextView herebtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqactivity);
        back=findViewById(R.id.bckBtn);
        herebtn=findViewById(R.id.hereTxt);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        herebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = "ishratbinteahmed@gmail.com";
                String subject = "Your Subject Here"; // You can add a default subject
                String body = "Your email body content"; // You can add a default body

                // Create an Intent to send an email
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");  // MIME type for email

                // Add email, subject, and body
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body);

                // Check if there is an email app to handle the intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);  // Start the email activity
                } else {
                    Toast.makeText(getApplicationContext(), "No email client installed", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}