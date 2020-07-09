package com.example.testo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.testo.Adapter.RecycleViewAdapter;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Model.User;


public class StatusFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "a7aa status";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StatusFragment() {
        // Required empty public constructor
    }
    

    ArrayList<Integer> Images;
    RecyclerView recyclerView;
    RecycleViewAdapter adapter;
    TextView searchText;
    ArrayList<String>userIds;


    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("users");

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
        userIds=new ArrayList<>();
       // Log.d(TAG, "onCreate: StatusFragment");
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
        if(!userIds.isEmpty()){
            initRecycleView();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_status, container, false);
        recyclerView=view.findViewById(R.id.users_recycleview);
        searchText=view.findViewById(R.id.Search_text);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        getfriends();
        return view;
    }

    private void searchUsers(String s) {
        Query query=FirebaseDatabase.getInstance().getReference().child("users").orderByChild("information/name")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!searchText.getText().toString().equals("")) {
                    ArrayList<String>tempUsersIds=new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        if(userIds.contains(snapshot.getKey())){
                            tempUsersIds.add(snapshot.getKey());
                        }

                        //.d(TAG, "onDataChange: " + snapshot.getKey());
                    }
                    RecycleViewAdapter recycleViewAdapter1=new RecycleViewAdapter(getContext(),tempUsersIds,null,false);
                    recyclerView.setAdapter(recycleViewAdapter1);

                }else {

                    if(adapter!=null)
                    recyclerView.setAdapter(adapter);
                    else {
                        RecycleViewAdapter recycleViewAdapter=new RecycleViewAdapter(getContext(),userIds,null,false);
                        recyclerView.setAdapter(recycleViewAdapter);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initRecycleView(){
        //Log.d(TAG, "initRecycleView: ");
        adapter=new RecycleViewAdapter(getContext(),userIds,null,false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    private void getfriends(){
        //Log.d(TAG, "getfriends: ");
       DatabaseReference reference=databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends");

       reference.addValueEventListener(new ValueEventListener() {

           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (searchText.getText().toString().equals("")) {
                   if (userIds.size() != dataSnapshot.getChildrenCount()) {
                       userIds.clear();

                       for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                           //Log.d(TAG, "onDataChange: "+snapshot.getKey());
                           userIds.add(snapshot.getKey());
                       }
                       initRecycleView();
                   }
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
    }

}
