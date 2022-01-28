package sg.edu.np.mad.voiceassist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TextToSpeechAct extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    private TextToSpeech mTTS;
    private EditText mEditText;
    private View layoutView;
    private Button mButtonSpeak;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton mHistory;
    DatabaseHandler text_DBHandler = new DatabaseHandler(this, null, null, 1);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);
        mHistory = findViewById(R.id.floating_action_button);
//        mButtonSpeak = findViewById(R.id.button);

        layoutView = findViewById(R.id.layoutView);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this);
        bottomNavigation.setSelectedItemId(R.id.page_3);

        final GestureDetector gdt = new GestureDetector(this, new TextToSpeechAct.GestureListener());

        layoutView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                gdt.onTouchEvent(event);
                return true;
            }
        });

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

        mEditText = findViewById(R.id.editTextInput);
//        mButtonSpeak.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                speak();
//            }
//        });

        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(TextToSpeechAct.this);
                builder.setTitle("Text History");

                // add a list
                ArrayList<String> textHistory = text_DBHandler.GetTTS_Text();
                String[] text = textHistory.toArray(new String[0]);
                builder.setItems(text , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i = 0; i <= which;i++){
                            mTTS.speak(text[i],TextToSpeech.QUEUE_FLUSH,null);
                        }
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
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                return false; // Right to left
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                startActivity(new Intent(TextToSpeechAct.this, MainActivity.class));
                return false; // Left to right
            }
            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                speak();
                return false; // Bottom to top
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                speak();
                return false; // Top to bottom
            }
            return false;

        }
        public void onLongPress(MotionEvent e) {
            speak();
        }



    }

    private void speak(){
        String text = mEditText.getText().toString();
        DateFormat dateFormat = new SimpleDateFormat("hh.mm aa");
        String dateString = dateFormat.format(new Date()).toString();
        String timeStamp = dateString;
        text_DBHandler.storeTTS(text,timeStamp);
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH,null);
        while (mTTS.isSpeaking()){
            // do nothing
        }
        startActivity(new Intent(TextToSpeechAct.this, SpeechToText.class));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.page_1:
                startActivity(new Intent(TextToSpeechAct.this, SpeechToText.class));
                return true;
            case R.id.page_2:
                startActivity(new Intent(TextToSpeechAct.this, MainActivity.class));

        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTTS != null){
            mTTS.stop();
            mTTS.shutdown();
        }
    }
}