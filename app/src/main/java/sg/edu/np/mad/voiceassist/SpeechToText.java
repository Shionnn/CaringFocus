package sg.edu.np.mad.voiceassist;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SpeechToText extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    DatabaseHandler text_DBHandler = new DatabaseHandler(this, null, null, 1);
    private TextView txtResult;
    private View layoutView;
    private FloatingActionButton mHistory;
    private boolean mBooleanIsPressed;
    private BottomNavigationView bottomNavigation;
//    private final Handler handler = new Handler();
//    private final Runnable runnable = new Runnable() {
//        public void run() {
//            checkGlobalVariable();
//        }
//    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text2);
        txtResult = (TextView) findViewById(R.id.txtResult);
        mHistory = findViewById(R.id.floating_action_button);

        layoutView = findViewById(R.id.layoutView);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this);
        bottomNavigation.setSelectedItemId(R.id.page_1);

        final GestureDetector gdt = new GestureDetector(this, new SpeechToText.GestureListener());

        layoutView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                gdt.onTouchEvent(event);
                return true;
            }
        });

        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(SpeechToText.this);
                builder.setTitle("Text History");

                // add a list
                ArrayList<String> speechHistory = text_DBHandler.GetSTT_Text();
                String[] text = speechHistory.toArray(new String[0]);
                builder.setItems(text , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        for(int i = 0; i <= which;i++){
//                            mTTS.speak(text[i], TextToSpeech.QUEUE_FLUSH,null);
//                        }
                    }
                });

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                startActivity(new Intent(SpeechToText.this, MainActivity.class));
                return false; // Right to left
            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                return false; // Left to right
            }

            if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                getSpeechInput();
                return false; // Bottom to top
            }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                getSpeechInput();
                return false; // Top to bottom
            }
            return false;
        }
        public void onLongPress(MotionEvent e) {
            getSpeechInput();
        }
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if(event.getAction() == MotionEvent.ACTION_DOWN) {
//            // Execute your Runnable after 1000 milliseconds = 1 second.
//            handler.postDelayed(runnable, 1000);
//            mBooleanIsPressed = true;
//        }
//
//        if(event.getAction() == MotionEvent.ACTION_UP) {
//            if(mBooleanIsPressed) {
//                mBooleanIsPressed = false;
//                handler.removeCallbacks(runnable);
//            }
//        }
//        return false;
//    }

//    public void checkGlobalVariable(){
//        if(mBooleanIsPressed == true){
//            getSpeechInput();
//        }
//    }
    public void getSpeechInput(){

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null){

            activityResultLaunch.launch(intent);

        }
        else{
            Toast.makeText(this,"Your device does not support speech input.",Toast.LENGTH_SHORT).show();
        }

    }

    ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getData() != null){
                        ArrayList<String> txt = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        txtResult.setText(txt.get(0));
                        DateFormat dateFormat = new SimpleDateFormat("hh.mm aa");
                        String dateString = dateFormat.format(new Date()).toString();
                        String timeStamp = dateString;
                        text_DBHandler.storeSTT(txt.get(0),timeStamp);
                        Handler h =new Handler() ;
                        h.postDelayed(new Runnable() {
                            public void run() {
                                startActivity(new Intent(SpeechToText.this, TextToSpeechAct.class));
                            }

                        }, txtResult.length()*175);



                    }
//                    if (result.getResultCode() == 10) {
//                    }
                }
            });

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.page_3:
                startActivity(new Intent(SpeechToText.this, TextToSpeechAct.class));
                return true;
            case R.id.page_2:
                startActivity(new Intent(SpeechToText.this, MainActivity.class));

        }
        return false;
    }
    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
