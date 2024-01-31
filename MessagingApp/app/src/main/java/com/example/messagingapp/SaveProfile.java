package com.example.messagingapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SaveProfile extends AppCompatActivity{

    CardView profPicHolder;
    ImageView profPic;
    static int pickImage;
    Uri profPicPath;
    EditText userName;
    Button save;
    FirebaseAuth auth;
    String name, imageUriAccessToken;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseFirestore firestore;
    ProgressBar progressBar;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firestore = FirebaseFirestore.getInstance();

        userName = findViewById(R.id.enterName);
        profPicHolder = findViewById(R.id.profPicHolder);
        profPic = findViewById(R.id.profPic);
        save = findViewById(R.id.save);
        progressBar = findViewById(R.id.progressBar);

        profPicHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), pickImage);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = userName.getText().toString().trim();
                if(name.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please Enter Your Name", Toast.LENGTH_SHORT).show();
                }
                else if(profPicPath == null){
                    Toast.makeText(getApplicationContext(), "Please Select A Profile Picture", Toast.LENGTH_SHORT).show();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    //Firebase storing user's name and profile picture
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = database.getReference(auth.getUid());
                    UserProfile userProfile = new UserProfile(name, auth.getUid());
                    databaseReference.setValue(userProfile);
                    Toast.makeText(getApplicationContext(), "Successfully Added User", Toast.LENGTH_SHORT).show();
                    StorageReference imageRef = storageReference.child("Images").child(auth.getUid()).child("Profile Picture");
                    Bitmap bitmap  = null;
                    try{
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profPicPath);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
                    byte [] data = byteArrayOutputStream.toByteArray();
                    UploadTask uploadTask = imageRef.putBytes(data);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUriAccessToken = uri.toString();
                                    Toast.makeText(getApplicationContext(), "Successful URI Access", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Failed URI Access", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Toast.makeText(getApplicationContext(), "Successful Image Upload", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed Image Upload", Toast.LENGTH_SHORT).show();
                        }
                    });
                    DocumentReference documentReference = firestore.collection("Users").document(auth.getUid());
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("name", name);
                    userData.put("image", imageUriAccessToken);
                    userData.put("uid", auth.getUid());
                    userData.put("status", "Online");
                    documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(), "Succesfully Saved Data", Toast.LENGTH_SHORT).show();
                        }
                    });
                    progressBar.setVisibility(View.INVISIBLE);
                    startActivity(new Intent(SaveProfile.this, Chats.class));
                    finish();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == pickImage && resultCode == RESULT_OK){
            profPicPath = data.getData();
            profPic.setImageURI(profPicPath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
