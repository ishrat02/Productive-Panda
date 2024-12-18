package com.example.adminpanel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getStringExtra("action").equals("show_quote")) {
            fetchMotivationalQuote(context);  // Fetch and show a random quote
        }
    }

    private void fetchMotivationalQuote(Context context) {
        String url = "https://api.myjson.online/v1/records/6c7820d0-72c1-4d3c-8c67-b0d87b6a1030";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.optJSONArray("data") != null) {
                            JSONArray quotesArray = response.getJSONArray("data");
                            List<String> quotes = new ArrayList<>();

                            for (int i = 0; i < quotesArray.length(); i++) {
                                JSONObject quoteObject = quotesArray.getJSONObject(i);
                                String quote = quoteObject.getString("quote");
                                //Toast.makeText(context,quote,Toast.LENGTH_SHORT).show();
                                quotes.add(quote);
                            }

                            // Pick a random quote
                            String randomQuote = quotes.get(new Random().nextInt(quotes.size()));
                            Toast.makeText(context,randomQuote,Toast.LENGTH_SHORT).show();
                            // Display the notification
                            showNotification(context, randomQuote);
                        } else {
                            Log.e("NotificationReceiver", "No quotes found in API response");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("NotificationReceiver", "Error parsing API response: " + e.getMessage());
                    }
                },
                error -> Log.e("NotificationReceiver", "Failed to fetch data: " + error.getMessage())
        );

        // Add the request to the Volley queue
        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }

    private void showNotification(Context context, String quote) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MotivationChannel")
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setContentTitle("Daily Motivation")
                .setContentText(quote)  // Show the fetched quote
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);  // Notification dismisses when tapped

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(new Random().nextInt(), builder.build());
    }
}
