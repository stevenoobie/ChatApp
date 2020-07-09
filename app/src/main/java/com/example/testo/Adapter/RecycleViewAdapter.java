package com.example.testo.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.testo.MessageActivity;
import com.example.testo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import Model.User;
import Model.chatText;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

    String TAG="a7aa";
    ArrayList<String>mUsersIds;
    Context mContext;
    boolean isChat;
    ArrayList<String>ChatsIds;
    String myId=FirebaseAuth.getInstance().getCurrentUser().getUid();
    String userImage;
    public RecycleViewAdapter(Context context, ArrayList<String> usersIds,ArrayList<String>ChatsIds, final boolean isChat){
        mContext=context;
        this.isChat=isChat;
        mUsersIds=usersIds;
        this.ChatsIds=ChatsIds;

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        SharedPreferences sharedPreferences=mContext.getSharedPreferences("MySharedPref",MODE_PRIVATE);
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUsersIds.get(position)).child("information");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                String name=sharedPreferences.getString(user.getPhoneNumber(),"");
                if(name.equals("")){
                    holder.name.setText(user.getPhoneNumber());
                }else {
                    holder.name.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("users").child(mUsersIds.get(position)).child("information");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                //holder.name.setText(user.getName());
                if(isChat){
                    LastMessage(ChatsIds.get(position),holder.last_message,holder.seen_image,holder.seen_count);
                }else {
                    //set the text to be at the center
                }

                if(user.getUriPath().equals("default")) {
                    Glide.with(mContext.getApplicationContext())
                            .load(R.drawable.profile_avatar)
                            .into(holder.image);
                    userImage="default";
                }else {
                    if(mContext.getApplicationContext()!=null) {
                        Glide.with(mContext.getApplicationContext())
                                .load(user.getUriPath())
                                .into(holder.image);
                        userImage=user.getUriPath();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, MessageActivity.class);
                intent.putExtra("userId",mUsersIds.get(position));
                intent.putExtra("userName",holder.name.getText().toString());
                intent.putExtra("userImage",userImage);
                mContext.startActivity(intent);

            }
        });
       // Log.d(TAG, "onBindViewHolder: "+position);
    }

    @Override
    public int getItemCount() {
        return mUsersIds.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView image;
        TextView name;
        RelativeLayout parentLayout;
        ProgressBar progressBar;
        TextView last_message;
        TextView seen_count;
        ImageView seen_image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.user_itemImage);
            name=itemView.findViewById(R.id.user_itemName);
            parentLayout=itemView.findViewById(R.id.relative_layout);
            progressBar=itemView.findViewById(R.id.progressBar_userItem);
            last_message=itemView.findViewById(R.id.user_item_lastMessageText);
            seen_count=itemView.findViewById(R.id.user_item_seen_count);
            seen_image=itemView.findViewById(R.id.user_item_seenImage);
        }
    }
    private void LastMessage(final String chatId, final TextView last_message, final ImageView seen_image, final TextView seen_count){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("chats").child("conversations").child(chatId);
        Query query=reference.orderByKey().limitToLast(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.getValue(chatText.class).getMessage()==null){
                           return;}
                    else {
                        chatText chat = snapshot.getValue(chatText.class);
                        //Log.d(TAG, "onLastMessage: " + chat.getMessage());

                        if (chat.getRecieverId().equals(myId)) {
                            seen_image.setVisibility(View.GONE);
                        } else {
                            seen_image.setVisibility(View.VISIBLE);
                            if(chat.isSent()&&!chat.isSeen()&&!chat.isDelivered()){
                                Glide.with(mContext.getApplicationContext())
                                        .load(R.drawable.sent)
                                        .into(seen_image);
                            }
                            else if(chat.isDelivered()&&!chat.isSeen()){
                                Glide.with(mContext.getApplicationContext())
                                        .load(R.drawable.deliver)
                                        .into(seen_image);
                            }
                            else if(chat.isSeen()){
                                Glide.with(mContext.getApplicationContext())
                                        .load(R.drawable.seen)
                                        .into(seen_image);
                            }else {
                                Glide.with(mContext.getApplicationContext())
                                        .load(R.drawable.clock)
                                        .into(seen_image);
                            }
                        }

                        last_message.setVisibility(View.VISIBLE);
                        last_message.setText(chat.getMessage());
                    }
                }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Query query1=reference.orderByChild("seen").equalTo(false);
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               // Log.d(TAG, "onQuery1: "+dataSnapshot.getValue());
                int count=0;
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    chatText chat=snapshot.getValue(chatText.class);
                    if(chat.getRecieverId().equals(myId)){
                        count++;
                    }
                }
                if(count>0){
                    seen_count.setVisibility(View.VISIBLE);
                    seen_count.setText(String.valueOf(count));
                }else {
                    seen_count.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
