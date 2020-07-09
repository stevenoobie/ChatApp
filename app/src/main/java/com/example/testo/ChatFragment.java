package com.example.testo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.testo.Adapter.RecycleViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import Model.User;
import Model.chatText;


public class ChatFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;


    final String TAG="a7aa chat";
    private ArrayList<String>UsersIds;
    FirebaseUser myUser;
    RecyclerView recyclerView;

    ArrayList<String>ChatsIds;
    Boolean FillRecyclerView;
    public ChatFragment() {
    }
    public static StatusFragment newInstance(String param1, String param2) {
        StatusFragment fragment = new StatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        UsersIds=new ArrayList<>();
        ChatsIds=new ArrayList<>();
        myUser = FirebaseAuth.getInstance().getCurrentUser();
        if (myUser == null) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
        Log.d(TAG, "onCreate: chatFragment");
        readUserChats();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView=view.findViewById(R.id.chat_recycler_view);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        //Log.d(TAG, "onCreateView: " + "chat fragment");
        FillRecyclerView=true;
        if(!ChatsIds.isEmpty())
            initRecycleView();

        return view;
    }
    private void readUserChats(){ //To get the chat Ids of the user
        Log.d(TAG, "readUserChats: ");
        final DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("users").child(myUser.getUid()).child("chats").child("conversations");
        reference
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: readUserChats ");
                        if (ChatsIds.size() != dataSnapshot.getChildrenCount()) {
                            ChatsIds.clear();
                            UsersIds.clear();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                ChatsIds.add(snapshot.getValue(String.class));
                                Log.d(TAG, "onDataChange: Adding to the ChatsIds" + snapshot.getValue(String.class));
                            }
                            readChats();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void readChats(){ //To get the users Ids from the chats
        Log.d(TAG, "readChats: ");

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("chats").child("conversations");


                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Log.d(TAG, "onDataChange: readChats");
                        if(UsersIds.size()<ChatsIds.size()) {
                            for (int i = 0; i < ChatsIds.size(); i++) {
                                DatabaseReference reference1 = reference.child(ChatsIds.get(i));
                                reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            if (UsersIds.size() < ChatsIds.size()) {
                                                chatText chatText = snapshot.getValue(chatText.class);
                                                if (!UsersIds.contains(chatText.getSenderId()) || !UsersIds.contains(chatText.getRecieverId())) {
                                                    if (chatText.getSenderId().equals(myUser.getUid())) {

                                                        UsersIds.add(chatText.getRecieverId());
                                                        Log.d(TAG, "onDataChange: Adding to the UsersIds " + chatText.getRecieverId() + " size: " + UsersIds.size());
                                                    } else {
                                                        UsersIds.add(chatText.getSenderId());
                                                        Log.d(TAG, "onDataChange: Adding to the UsersIds " + chatText.getSenderId() + " size: " + UsersIds.size());
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                        initRecycleView();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }else if(UsersIds.size()==0){
                            initRecycleView();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            private void initRecycleView(){
                RecycleViewAdapter adapter=new RecycleViewAdapter(getContext(),UsersIds,ChatsIds,true);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }

        }


