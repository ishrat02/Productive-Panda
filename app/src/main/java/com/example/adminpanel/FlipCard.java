package com.example.adminpanel;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class FlipCard extends AppCompatActivity {

    private boolean isFlipped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flip_card);

        String question = getIntent().getStringExtra("QUESTION");
        String answer = getIntent().getStringExtra("ANSWER");

        TextView frontCard = findViewById(R.id.front_card);
        TextView backCard = findViewById(R.id.back_card);

        frontCard.setText(question);
        backCard.setText(answer);

        View cardView = findViewById(R.id.card_view);
        cardView.setOnClickListener(view -> flipCard(frontCard, backCard));
    }

    private void flipCard(View frontCard, View backCard) {
        if (isFlipped) {
            frontCard.setVisibility(View.VISIBLE);
            backCard.setVisibility(View.GONE);
        } else {
            frontCard.setVisibility(View.GONE);
            backCard.setVisibility(View.VISIBLE);
        }
        isFlipped = !isFlipped;
    }
}
