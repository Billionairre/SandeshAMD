package edu.sandesh.mealmate.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.sandesh.mealmate.R;
import edu.sandesh.mealmate.WeeklyPlanActivity;

public class MealReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "MealReminderChannel";
    private static final String TAG = "MealReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String mealType = intent.getStringExtra("MEAL_TYPE");
        String date = intent.getStringExtra("DATE");
        
        if (mealType == null || date == null) {
            Log.e(TAG, "Missing required extras: mealType or date");
            return;
        }
        
        // Create notification channel for Android 8.0+
        createNotificationChannel(context);
        
        // Fetch meal details from Firestore to include in notification
        fetchMealDetails(context, mealType, date);
    }
    
    private void fetchMealDetails(Context context, String mealType, String date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("meals").document(date)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    List<Long> mealTimestamps = (List<Long>) documentSnapshot.get(mealType);
                    if (mealTimestamps != null && !mealTimestamps.isEmpty()) {
                        fetchMealNames(context, mealType, date, mealTimestamps);
                    } else {
                        // No meals found for this type, show generic notification
                        showNotification(context, mealType, date, "Time for " + mealType);
                    }
                } else {
                    // No meals found for this day
                    showNotification(context, mealType, date, "Time for " + mealType);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching meal details", e);
                // Show generic notification on error
                showNotification(context, mealType, date, "Time for " + mealType);
            });
    }
    
    private void fetchMealNames(Context context, String mealType, String date, List<Long> timestamps) {
        final List<String> mealNames = new ArrayList<>();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final int[] completedRequests = {0};
        
        for (Long timestamp : timestamps) {
            db.collection("recipes").document(String.valueOf(timestamp))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String recipeName = documentSnapshot.getString("recipeName");
                        if (recipeName != null) {
                            mealNames.add(recipeName);
                        }
                    }
                    
                    completedRequests[0]++;
                    if (completedRequests[0] >= timestamps.size()) {
                        // All requests completed, show notification
                        String notificationText;
                        if (mealNames.isEmpty()) {
                            notificationText = "Time for " + mealType;
                        } else if (mealNames.size() == 1) {
                            notificationText = "Time for " + mealType + ": " + mealNames.get(0);
                        } else {
                            StringBuilder sb = new StringBuilder("Time for " + mealType + ": ");
                            for (int i = 0; i < mealNames.size(); i++) {
                                sb.append(mealNames.get(i));
                                if (i < mealNames.size() - 1) {
                                    sb.append(", ");
                                }
                            }
                            notificationText = sb.toString();
                        }
                        
                        showNotification(context, mealType, date, notificationText);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching recipe details", e);
                    completedRequests[0]++;
                    if (completedRequests[0] >= timestamps.size()) {
                        // Show generic notification on error
                        showNotification(context, mealType, date, "Time for " + mealType);
                    }
                });
        }
    }
    
    private void showNotification(Context context, String mealType, String date, String content) {
        // Create intent to open the app when notification is tapped
        Intent intent = new Intent(context, WeeklyPlanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Get emoji for meal type
        String emoji = getEmojiForMealType(mealType);
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(emoji + " " + mealType + " Reminder")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.olive_green))
                .setCategory(NotificationCompat.CATEGORY_REMINDER);
        
        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = new Random().nextInt(1000); // Use random ID to prevent overwriting
        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted", e);
        }
    }
    
    private String getEmojiForMealType(String mealType) {
        switch (mealType) {
            case "Breakfast": return "🍳";
            case "Lunch": return "🥗";
            case "Dinner": return "🍛";
            default: return "🍽️";
        }
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Meal Reminders";
            String description = "Notifications for meal reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
} 