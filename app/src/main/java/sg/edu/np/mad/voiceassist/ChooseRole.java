package sg.edu.np.mad.voiceassist;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChooseRole extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private TextToSpeech mTTS;
    private Button helpee;
    private Button helper;
    private EditText name;
    private EditText phoneNum;
    private EditText EmeNum;
    private Button uploadImage;
    public String GLOBAL_PREFS = "MyPrefs";
    public Uri imageUri;
    FirebaseStorage storage;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        name = new EditText(this);
        phoneNum = new EditText(this);
        EmeNum = new EditText(this);
        uploadImage = new Button(this);

        sharedPreferences = getSharedPreferences(GLOBAL_PREFS, MODE_PRIVATE);
        String role = sharedPreferences.getString("role","");
        String UID = sharedPreferences.getString("UID","");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Navigate to the pages based on what the user chose at the start
        if (role.equals("helpee") && !UID.isEmpty()){
            startActivity(new Intent(ChooseRole.this, MainActivity.class));
            finish();
        }
        if(role.equals("helper")){
            startActivity(new Intent(ChooseRole.this, helper.class));
            finish();
        }
        setContentView(R.layout.activity_choose_role);

        //Initialise TTS
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

        helpee = findViewById(R.id.helpee);
        helper = findViewById(R.id.helper);

        helpee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak("Please enter your name, phone number, emergency contact and upload an image of yourself. If you are unable to do so yourself, please request help from your caretaker.");
                sharedPreferences = getSharedPreferences(GLOBAL_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("role", "helpee");
                editor.apply();
                showAlert();


            }
        });

        helper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences = getSharedPreferences(GLOBAL_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("role", "helper");
                editor.apply();
                startActivity(new Intent(ChooseRole.this, helper.class));

            }
        });


    }
    private void speak(String string){
        mTTS.speak(string, TextToSpeech.QUEUE_FLUSH,null);
    }


    //Alert to ask for name, num, emergency num, and image
    private void showAlert(){
//        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(ChooseRole.this,R.style.ThemeOverlay_App_MaterialAlertDialog);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ChooseRole.this);
        mBuilder.setTitle("Please enter your phone number");
        Context context = ChooseRole.this;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.removeAllViews();


        if (name != null) {
            if(name.getParent() != null) {
                ((ViewGroup)name.getParent()).removeView(name);
                ((ViewGroup)phoneNum.getParent()).removeView(phoneNum);
                ((ViewGroup)EmeNum.getParent()).removeView(EmeNum);
                ((ViewGroup)uploadImage.getParent()).removeView(uploadImage);
            }
            //This is so when user input wrong input the alertdialog still retain the inputs that were correct
            name.setText(name.getText().toString());
            name.setHint("Name");
            layout.addView(name);
        }
        else{
            name.setHint("Name");
            layout.addView(name);
        }



        if (phoneNum != null) {
            if(phoneNum.getParent() != null) {
                ((ViewGroup)name.getParent()).removeView(name);
                ((ViewGroup)phoneNum.getParent()).removeView(phoneNum);
                ((ViewGroup)EmeNum.getParent()).removeView(EmeNum);
                ((ViewGroup)uploadImage.getParent()).removeView(uploadImage);
            }
            phoneNum.setText(phoneNum.getText().toString());
            phoneNum.setHint("Phone number");
            phoneNum.addTextChangedListener(new TextWatcher()  {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //phone number validation
                    if (phoneNum.getText().toString().length() != 8 || (!phoneNum.getText().toString().matches("[0-9]+"))) {
                        phoneNum.setError("Please enter a 8 digit number");
                    } else {
                        phoneNum.setError(null);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s)  {

                }
            });
            layout.addView(phoneNum);
        }
        else{
            phoneNum.setHint("Phone number");
            phoneNum.addTextChangedListener(new TextWatcher()  {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (phoneNum.getText().toString().length() != 8 || (!phoneNum.getText().toString().matches("[0-9]+"))) {
                        phoneNum.setError("Please enter a 8 digit number");
                    } else {
                        phoneNum.setError(null);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s)  {

                }
            });
            layout.addView(phoneNum);
        }



        if (EmeNum != null) {
            if(EmeNum.getParent() != null) {
                ((ViewGroup)name.getParent()).removeView(name);
                ((ViewGroup)phoneNum.getParent()).removeView(phoneNum);
                ((ViewGroup)EmeNum.getParent()).removeView(EmeNum);
                ((ViewGroup)uploadImage.getParent()).removeView(uploadImage);
            }
            EmeNum.setText(EmeNum.getText().toString());
            EmeNum.setHint("Emergency number");
            EmeNum.addTextChangedListener(new TextWatcher()  {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (EmeNum.getText().toString().length() != 8 || (!EmeNum.getText().toString().matches("[0-9]+"))) {
                        EmeNum.setError("Please enter a 8 digit number");
                    } else {
                        EmeNum.setError(null);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s)  {

                }
            });
            layout.addView(EmeNum);
        }
        else{
            EmeNum.setHint("Emergency number");
            EmeNum.addTextChangedListener(new TextWatcher()  {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (EmeNum.getText().toString().length() != 8 || (!EmeNum.getText().toString().matches("[0-9]+"))) {
                        EmeNum.setError("Please enter a 8 digit number");
                    } else {
                        EmeNum.setError(null);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s)  {

                }
            });
            layout.addView(EmeNum);
        }


        uploadImage.setText("Upload an Image");
        layout.addView(uploadImage);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        mBuilder.setView(layout);
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (imageUri == null){
                    speak("Please upload an image of yourself to allow helpers to identify you.");
                    showAlert();
                }
                else if (name.getText().toString().equals("")){
                    speak("Please enter your name to allow helpers to identify you.");
                    showAlert();
                }
                else if (phoneNum.getText().toString().equals("")){
                    speak("Please enter your phone number to allow helpers to identify you.");
                    showAlert();
                }
                else if (EmeNum.getText().toString().equals("")){
                    speak("Please enter your emergency contact in case of an emergency");
                    showAlert();
                }
                else{

                    //put into firebase
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> user = new HashMap<>();
                    //new user
                    user.put("Name", name.getText().toString());
                    user.put("PhoneNumber", Integer.parseInt(phoneNum.getText().toString()));
                    user.put("Location","");


                    // Add a new document with a generated ID
                    db.collection("Users")
                            .add(user)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("db", "DocumentSnapshot added with ID: " + documentReference.getId());
                                    sharedPreferences = getSharedPreferences(GLOBAL_PREFS, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("UID",documentReference.getId());
                                    editor.putString("Emergency", EmeNum.getText().toString());
                                    editor.apply();
                                    uploadPicture(documentReference.getId());
                                    startActivity(new Intent(ChooseRole.this, MainActivity.class));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("db", "Error adding document", e);
                                }
                            });





                    }
            }


        });
        mBuilder.show();
    }
    private void choosePicture(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        uploadActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> uploadActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        imageUri = data.getData();
                    }
                }
            });

    private void uploadPicture(String UID){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading Image...");
        pd.show();

        StorageReference profileRef = storageReference.child("images/" + UID);

        profileRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Snackbar.make(findViewById(android.R.id.content),"Image Uploaded.", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(),"Failed to upload",Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progressPercent = (100.00* snapshot.getBytesTransferred()/ snapshot.getTotalByteCount());
                        pd.setMessage("Percentage: " + (int) progressPercent + "%");
                    }
                });


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