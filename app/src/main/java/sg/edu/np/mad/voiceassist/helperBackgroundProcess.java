package sg.edu.np.mad.voiceassist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.ContentValues.TAG;

public class helperBackgroundProcess extends Service {
    private MessageListener mMessageListener;
    private com.google.android.gms.nearby.messages.Message mMessage;
    private String name;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();


        Intent notificationIntent = new Intent(this, helper.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,0);

        Notification notification = new NotificationCompat.Builder(this,"pingNoti")
                .setContentTitle("CaringFocus service is running...")
                .setContentText("This service runs in the background to constantly check for pings.")
                .setSmallIcon(R.mipmap.appicon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1,notification);

        subscribe();
        return START_NOT_STICKY;

    }

    // Subscribe to receive messages.
    private void subscribe() {
        Nearby.getMessagesClient(this).subscribe(mMessageListener).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Message", "Subscribing!!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Message", "NOT Subscribing.");
            }
        });
    }

    private void unsubscribe() {
        Log.i("Message", "Unsubscribing.");
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
    }

    public void sendNotification(String message, int notificationId,String UID) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(this, showHelpee.class);
        intent.putExtra("UID",UID);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
               0);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Users").document(UID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        name = document.get("Name").toString();
                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(helperBackgroundProcess.this, "pingNoti")
                                .setSmallIcon(R.mipmap.appicon)
                                .setPriority(Notification.PRIORITY_MAX)
                                .setVibrate(new long[] { 1000, 1000})
                                .setDefaults(Notification.DEFAULT_VIBRATE)
                                .setContentTitle(name + " Pinged You!")
                                .setAutoCancel(true)
                                .setContentText(message)
                                .setSound(defaultSoundUri)
                                .setContentIntent(pendingIntent);



                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "VoiceAssist";
            String description = "Help people in need";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("pingNoti", name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[] { 1000, 1000});
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
//        mMessage = new com.google.android.gms.nearby.messages.Message("Hello World".getBytes());
        // start receiving the pings from helpee's
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(com.google.android.gms.nearby.messages.Message message) {
                super.onFound(message);
                String check = new String(message.getContent());
                Log.i("MessageFound-helper", "Found message: " + check);
                if (check.startsWith("helper")){
                // empty cause helper shouldnt recieve pings from other helpers.
                }
                else{
                    String UID = new String(message.getContent());
                    sendNotification("Click on notification to view who pinged you.",11,UID);
                }



            }
            @Override
            public void onLost(com.google.android.gms.nearby.messages.Message message) {
                super.onLost(message);
                Log.i("MessageLost", "Lost sight of message: " + message.toString());
            }
        };

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }




}
