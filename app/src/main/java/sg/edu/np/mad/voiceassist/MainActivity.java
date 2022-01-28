package sg.edu.np.mad.voiceassist;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alan.alansdk.AlanCallback;
import com.alan.alansdk.AlanConfig;
import com.alan.alansdk.button.AlanButton;
import com.alan.alansdk.events.EventCommand;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private TextToSpeech mTTS;
    private MessageListener mMessageListener;
    /// Add the alanButton variable
    private AlanButton alanButton;
    DatabaseHandler text_DBHandler = new DatabaseHandler(this, null, null, 1);
    SharedPreferences sharedPreferences;
    private View layoutView;
    private com.google.android.gms.nearby.messages.Message mMessage;
    private FloatingActionButton mButtonPing;
    private BottomNavigationView bottomNavigation;
    private String UID;
    private String EmeNum;
    private Boolean Eme;
    public String GLOBAL_PREFS = "MyPrefs";
    private LocationRequest locationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /// Set up the Alan button
        AlanConfig config = AlanConfig.builder().setProjectId("bd0eb3aaa6e78fe61712ce7b11c00ffb2e956eca572e1d8b807a3e2338fdd0dc/stage").build();
        alanButton = findViewById(R.id.alan_button);
        alanButton.initWithConfig(config);

        AlanCallback alanCallback = new AlanCallback() {
            /// Handle commands from Alan Studio
            @Override
            public void onCommand(final EventCommand eventCommand) {
                try {
                    JSONObject command = eventCommand.getData();
                    String commandName = command.getJSONObject("data").getString("command");
                    Log.d("AlanButton", "onCommand: commandName: " + commandName);
                    if (commandName.equals("navigateSTT")){
                        startActivity(new Intent(MainActivity.this, SpeechToText.class));
                    }
                    if (commandName.equals("navigateTTS")){
                        startActivity(new Intent(MainActivity.this, TextToSpeechAct.class));
                    }
                    if (commandName.equals("navigateTTS")){
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                    }
                    if (commandName.equals("Ping")){
                        publish(UID);
                    }
                    if (commandName.equals("unPing")){
                        unpublish();
                        unsubscribe();
                    }
                } catch (JSONException e) {
                    Log.e("AlanButton", e.getMessage());
                }
            }
        };

        /// Register callbacks
        alanButton.registerCallback(alanCallback);

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result = mTTS.setLanguage(Locale.getDefault());

                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.v("TTS","Lang not supported");
                    }
                    else{

                    }
                }
                else{
                    Log.v("TTS","Initialisation FAiled!!!!!!");
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.RECORD_AUDIO}, 1);
        }
        sharedPreferences = getSharedPreferences(GLOBAL_PREFS, MODE_PRIVATE);
        layoutView = findViewById(R.id.layoutView);
//        mMessage = new com.google.android.gms.nearby.messages.Message("Hello World".getBytes());
        mButtonPing = findViewById(R.id.floating_action_button);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this);
        bottomNavigation.setSelectedItemId(R.id.page_2);

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(com.google.android.gms.nearby.messages.Message message) {
                super.onFound(message);
                String check = new String(message.getContent());
                Log.i("MessageFound-Main", "Found message: " + check);
                if (check.startsWith("helper")) {
                    if (check.equals("helper"+UID)){
                        unsubscribe();
                        unpublish();
                        speak("Help is on the way!");
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Help is on the way!");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    }


                }
            }
        };

        String firstTime = sharedPreferences.getString("first","");
        if (firstTime.isEmpty()){
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // run your code here
                    speak("Please click allow on these permission for the app to fully function...... This is the Pinging Feature Page, Swipe left for the text to speech feature, swipe right for the speech to text feature.Swipe up or down to activate the feature.");
                    sharedPreferences = getSharedPreferences(GLOBAL_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("first", "true");
                    editor.apply();
                }
            }, 800);
        }

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);




        final GestureDetector gdt = new GestureDetector(this, new GestureListener());

        layoutView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                gdt.onTouchEvent(event);
                return true;
            }
        });


        mButtonPing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish(UID);
            }
        });


    }


    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                startActivity(new Intent(MainActivity.this, TextToSpeechAct.class));
                return false; //right to left
            }
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                startActivity(new Intent(MainActivity.this, SpeechToText.class));
                return false; // Left to right
            }

            if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                publish(UID);
                return false; // Bottom to top
            }
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                publish(UID);
                return false; // Top to bottom
            }
            return false;
        }
    }



    private void publish(String message) {
        mMessage = new Message(message.getBytes());
        Nearby.getMessagesClient(this).publish(mMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Message", "Publishing message: " + message);
                Eme = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        if (isGPSEnabled()) {

                            LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                    .requestLocationUpdates(locationRequest, new LocationCallback() {
                                        @Override
                                        public void onLocationResult(@NonNull LocationResult locationResult) {
                                            super.onLocationResult(locationResult);

                                            LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                                    .removeLocationUpdates(this);

                                            if (locationResult != null && locationResult.getLocations().size() >0){

                                                int index = locationResult.getLocations().size() - 1;
                                                double latitude = locationResult.getLocations().get(index).getLatitude();
                                                double longitude = locationResult.getLocations().get(index).getLongitude();

                                                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                                try{
                                                    List<Address> addressList = geocoder.getFromLocation(latitude,longitude,1);
                                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                    DocumentReference userRef = db.collection("Users").document(UID);

                                                    userRef.update("Location",addressList.get(0).getAddressLine(0).replaceAll(",", " ")).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                            subscribe();
                                                            Handler h = new Handler();
                                                            h.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    // run your code here
                                                                    if (Eme == false){
                                                                        callEmergencyNum();
                                                                    }
                                                                }
                                                            }, 60000);
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(TAG, "Error updating document", e);
                                                        }
                                                    });

                                                }catch (IOException e){
                                                    e.printStackTrace();
                                                }

                                            }
                                        }
                                    }, Looper.getMainLooper());

                        } else {
                            turnOnGPS();
                            speak("Please click OK to turn on GPS. Then click the ping button again.");
//                            publish(UID);
                        }

                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        speak("If this is your first time using the pinging feature, you would need to allow this app to use your location, a popup will ask you for permissions, just click allow then click the pinging button again.");

                    }
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Message", "FAILLLLLLLLL" + message);
            }
        });
    }

    private void unpublish() {
        Log.i("Message", "Unpublishing.");

        if (mMessage != null) {
            Nearby.getMessagesClient(this).unpublish(mMessage);
            Eme = true;
            mMessage = null;
        }
    }

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

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private void callEmergencyNum(){
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + EmeNum));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
            }
        }
        startActivity(intent);
    }

    private void speak(String string){
        mTTS.speak(string, TextToSpeech.QUEUE_FLUSH,null);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        UID = sharedPreferences.getString("UID","");
        EmeNum = sharedPreferences.getString("Emergency","");
        if (UID.isEmpty()){
            startActivity(new Intent(MainActivity.this, ChooseRole.class));
        }
        text_DBHandler.deleteAllTTS();
        text_DBHandler.deleteAllSTT();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        unpublish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
        unsubscribe();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.page_3:
                startActivity(new Intent(MainActivity.this, TextToSpeechAct.class));
                return true;
            case R.id.page_1:
                startActivity(new Intent(MainActivity.this, SpeechToText.class));

        }
        return false;
    }
}
