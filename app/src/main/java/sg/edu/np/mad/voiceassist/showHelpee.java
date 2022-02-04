package sg.edu.np.mad.voiceassist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.content.ContentValues.TAG;

public class showHelpee extends AppCompatActivity {
    Intent intent;
    String UID;
    TextView TVname, TVphonenum, TVlocation;
    private com.google.android.gms.nearby.messages.Message mMessage;

    FirebaseStorage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_helpee);
        intent = getIntent();
        UID = intent.getStringExtra("UID");
        retrieveHelpeeImage();
        retrieveHelpeeData();
        TVname = findViewById(R.id.helpeeName);
        TVphonenum = findViewById(R.id.helpeePN);
        TVlocation = findViewById(R.id.helpeeLoc);




    }
    private void retrieveHelpeeImage(){
        // Reference to an image file in Cloud Storage
        storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference ref = storageReference.child("images/"+UID);
        // ImageView in your Activity
        ImageView imageView = findViewById(R.id.helpeeImage);

        // Download directly from StorageReference using Glide
        // (See MyAppGlideModule for Loader registration)
        GlideApp.with(this /* context */)
                .load(ref)
                .into(imageView);
    }
    private void retrieveHelpeeData(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Users").document(UID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("retrievedData", "DocumentSnapshot data: " + document.getData());
                        TVname.setText(document.get("Name").toString());
                        TVphonenum.setText(document.get("PhoneNumber").toString());
                        TVlocation.setText(document.get("Location").toString());
                        String helper = new String("helper");
                        //this publish is to publish a specific message so the helpee who pinged can recieve it and confirm that help is on the way
                        publish(helper+UID);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void publish(String message) {
        mMessage = new Message(message.getBytes());
        Nearby.getMessagesClient(this).publish(mMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Message", "Publishing message: " + message);
            }
        });
    }
}