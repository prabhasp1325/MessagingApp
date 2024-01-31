package com.example.messagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class IndividualMsgs extends AppCompatActivity {

    EditText typedMsg;
    ImageButton sendMsg;
    CardView sendMsgHolder;
    ImageView recieverProfPic;
    TextView recieverName;
    androidx.appcompat.widget.Toolbar toolbar;
    String typedMsgString;
    Intent intent;
    String recieverNameString, senderNameString, recieverUid, senderUid;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String senderRoom, recieverRoom;
    ImageButton back;
    String currentTime;
    Calendar calendar;
    SimpleDateFormat sdf;
    MsgsAdapter msgsAdapter;
    ArrayList<Msgs> msgsList;
    RecyclerView msgsListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.individual_msg_page);

        typedMsg = findViewById(R.id.getmessage);
        sendMsg = findViewById(R.id.imageviewsendmessage);
        sendMsgHolder = findViewById(R.id.carviewofsendmessage);
        recieverProfPic = findViewById(R.id.specificuserimageinimageview);
        recieverName = findViewById(R.id.Nameofspecificuser);
        toolbar = findViewById(R.id.toolbarofspecificchat);
        back = findViewById(R.id.backbuttonofspecificchat);
        msgsListView = findViewById(R.id.recyclerviewofspecific);

        msgsList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        msgsListView.setLayoutManager(linearLayoutManager);
        msgsAdapter = new MsgsAdapter(IndividualMsgs.this, msgsList);
        msgsListView.setAdapter(msgsAdapter);

        intent = getIntent();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("hh:mm a");
        senderUid = auth.getUid();
        recieverUid = intent.getStringExtra("uid");
        recieverNameString = intent.getStringExtra("name");
        senderRoom = senderUid + recieverUid;
        recieverRoom = recieverUid + senderUid;

        DatabaseReference databaseReference = database.getReference().child("Chats").child(senderRoom).child("Msgs");
        msgsAdapter = new MsgsAdapter(IndividualMsgs.this,msgsList);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                msgsList.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren())
                {
                    Msgs messages = snapshot1.getValue(Msgs.class);
                    msgsList.add(messages);
                }
                msgsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recieverName.setText(recieverNameString);
        Picasso.get().load(intent.getStringExtra("img")).into(recieverProfPic);

        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                typedMsgString = typedMsg.getText().toString();
                if(typedMsgString.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter A Message", Toast.LENGTH_SHORT).show();
                }
                else{
                    Date date = new Date();
                    currentTime = sdf.format(calendar.getTime());
                    Msgs msgs = new Msgs(typedMsgString, auth.getUid(), date.getTime(), currentTime);
                    database.getReference().child("Chats").child(senderRoom).child("Msgs").push().setValue(msgs).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            database.getReference().child("Chats").child(recieverRoom).child("Msgs").push().setValue(msgs);
                        }
                    });
                }
                typedMsg.setText(null);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        msgsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(msgsAdapter != null)
            msgsAdapter.notifyDataSetChanged();
    }
}
