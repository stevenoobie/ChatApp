package com.example.testo.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.List;

import Model.User;
import Model.chatText;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class MessageViewAdapter extends RecyclerView.Adapter<MessageViewAdapter.ViewHolder> {

    String TAG = "one";
    final int MESSAGE_RIGHT = 0;
    final int MESSAGE_LEFT = 1;

    Context mContext;
    String conversationId;
   public List<chatText> mchatText;
    chatText chat;

    public MessageViewAdapter(Context context,String conversationId,ArrayList<chatText>chatTexts) {
        mContext = context;
        this.conversationId=conversationId;
        this.mchatText=chatTexts;

    }

    @NonNull
    @Override
    public MessageViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == MESSAGE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);

        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);

        }
        MessageViewAdapter.ViewHolder holder = new MessageViewAdapter.ViewHolder(view);
        if(viewType==MESSAGE_RIGHT){
            holder.message=view.findViewById(R.id.chat_item_right_text);
            holder.seenImage=view.findViewById(R.id.chat_item_right_seenText);
            holder.seenImage.setVisibility(View.VISIBLE);
        }else {
            holder.message=view.findViewById(R.id.chat_item_left_text);
            holder.seenImage=view.findViewById(R.id.chat_item_left_seenText);
            holder.seenImage.setVisibility(GONE);
        }

        return holder;

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewAdapter.ViewHolder holder, final int position) {
        chatText chat=mchatText.get(position);
        if(chat.isSent()&&!chat.isSeen()&&!chat.isDelivered()){
            Glide.with(mContext.getApplicationContext())
                    .load(R.drawable.sent)
                    .into(holder.seenImage);
        }
        else if(chat.isDelivered()&&!chat.isSeen()){
            Glide.with(mContext.getApplicationContext())
                    .load(R.drawable.deliver)
                    .into(holder.seenImage);
        }
        else if(chat.isSeen()){
            Glide.with(mContext.getApplicationContext())
                    .load(R.drawable.seen)
                    .into(holder.seenImage);
        }else {
            Glide.with(mContext.getApplicationContext())
                    .load(R.drawable.clock)
                    .into(holder.seenImage);
        }

        holder.message.setText(mchatText.get(position).getMessage());
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        reference=reference.child("chats").child("conversations").child(conversationId);
        reference.child(mchatText.get(position).getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    chatText chat=dataSnapshot.getValue(chatText.class);
                    Log.d(TAG, "onDataChange: "+chat.getMessage());
                    if(chat.isSent()&&!chat.isSeen()&&!chat.isDelivered()){
                        Glide.with(mContext.getApplicationContext())
                                .load(R.drawable.sent)
                                .into(holder.seenImage);
                    }
                    else if(chat.isDelivered()&&!chat.isSeen()){
                        Glide.with(mContext.getApplicationContext())
                                .load(R.drawable.deliver)
                                .into(holder.seenImage);
                    }
                    else if(chat.isSeen()){
                        Glide.with(mContext.getApplicationContext())
                                .load(R.drawable.seen)
                                .into(holder.seenImage);
                    }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public int getItemCount() {

            return mchatText.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView message;
        ImageView seenImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String x=user.getUid();
        if(mchatText!=null){
        if(mchatText.get(position).getRecieverId().equals(user.getUid())){
            return MESSAGE_LEFT;
        }else
            return MESSAGE_RIGHT;
    }
        return -1;
    }
}


