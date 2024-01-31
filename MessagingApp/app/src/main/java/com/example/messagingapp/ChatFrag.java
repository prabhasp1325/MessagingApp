package com.example.messagingapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class ChatFrag extends Fragment {

    RecyclerView chatList;
    LinearLayoutManager linearLayoutManager;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    public ImageView userPic;
    FirestoreRecyclerAdapter<ChatModel, NoteViewHolder> chatListAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats_frag, container, false);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        chatList = view.findViewById(R.id.chat_list);
        Query query = firestore.collection("Users").whereNotEqualTo("uid",auth.getUid());
        FirestoreRecyclerOptions<ChatModel> allUserName = new FirestoreRecyclerOptions.Builder<ChatModel>().setQuery(query, ChatModel.class).build();
        chatListAdapter = new FirestoreRecyclerAdapter<ChatModel, NoteViewHolder>(allUserName){
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull ChatModel chatModel) {
                noteViewHolder.userName.setText(chatModel.getName());
                String uri=chatModel.getImage();

                Picasso.get().load(uri).into(userPic);
                if(chatModel.getStatus().equals("Online"))
                {
                    noteViewHolder.userStatus.setText(chatModel.getStatus());
                    noteViewHolder.userStatus.setTextColor(Color.GREEN);
                }
                else
                {
                    noteViewHolder.userStatus.setText(chatModel.getStatus());
                }

                noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(getActivity(),IndividualMsgs.class);
                        intent.putExtra("name",chatModel.getName());
                        intent.putExtra("uid",chatModel.getUid());
                        intent.putExtra("img",chatModel.getImage());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatview, parent, false);
                return new NoteViewHolder(view);
            }
        };

        chatList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        chatList.setLayoutManager(linearLayoutManager);
        chatList.setAdapter(chatListAdapter);

        return view;

    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView userName;
        TextView userStatus;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.displayName);
            userStatus = itemView.findViewById(R.id.displayStatus);
            userPic = itemView.findViewById(R.id.pic);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        chatListAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(chatListAdapter!=null)
        {
            chatListAdapter.stopListening();
        }
    }
}