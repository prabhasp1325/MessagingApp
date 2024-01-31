package com.example.messagingapp;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfilePage extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    androidx.appcompat.widget.Toolbar toolbarProfile;
    ImageButton back;
    CardView profPicHolder2;
    ImageView editProfPic;
    EditText editName;
    TextView saveChanges;

    GoogleApiClient gac;
    GoogleSignInOptions gsio;

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(gac);
        if(opr.isDone()){
            GoogleSignInResult result = opr.get();
            updateProfile(result);
        }
        else{
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    updateProfile(googleSignInResult);
                }
            });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inner_profile_page);

        toolbarProfile = findViewById(R.id.toolBarProfile);
        back = findViewById(R.id.back);
        profPicHolder2 = findViewById(R.id.profPicHolder2);
        editProfPic = findViewById(R.id.editProfPic);
        editName = findViewById(R.id.editName);
        saveChanges = findViewById(R.id.editSave);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        setSupportActionBar(toolbarProfile);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        gsio = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gac = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gsio).build();

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void updateProfile(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();

            editName.setText(account.getDisplayName());
            Picasso.get().load(account.getPhotoUrl()).into(editProfPic);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference(auth.getUid());
            UserProfile userProfile = new UserProfile(editName.getText().toString(), auth.getUid());
            databaseReference.setValue(userProfile);
            Toast.makeText(getApplicationContext(), "Successfully Added User", Toast.LENGTH_SHORT).show();
            DocumentReference documentReference = firestore.collection("Users").document(auth.getUid());
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", editName.getText().toString());
            userData.put("image", account.getPhotoUrl());
            userData.put("uid", auth.getUid());
            userData.put("status", "Online");
            documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(getApplicationContext(), "Succesfully Saved Data", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed To Save Data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}