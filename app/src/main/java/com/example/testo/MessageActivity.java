package com.example.testo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.testo.Adapter.MessageViewAdapter;
import com.example.testo.Notifications.APIService;
import com.example.testo.Notifications.Client;
import com.example.testo.Notifications.Data;
import com.example.testo.Notifications.MyResponce;
import com.example.testo.Notifications.Sender;
import com.example.testo.Notifications.Token;
import com.example.testo.onAppDestroyed.AppStatesHandling;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.text.format.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TimerTask;


import Model.User;
import Model.chatText;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MessageActivity extends AppCompatActivity {

    Toolbar toolbar;
    CircleImageView message_image;
    TextView message_title;
    TextView message_status;
    TextView downArrow;
    TextView downArrowCount;
    Intent intent;
    String TAG="a7aa message";
    DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("users");
    DatabaseReference reference_messages=FirebaseDatabase.getInstance().getReference();
    TextView message_text;
    String userId;
    String userName;
    String userImage;
    String myName;
    ArrayList<chatText>mchatTexts;
    String uriImage;

    FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
    RecyclerView recyclerView;
    MessageViewAdapter messageViewAdapter;

    AppStatesHandling appStatesHandling;
    ValueEventListener seenListener;
    Query seen_query;

    APIService apiService;
    boolean notify=false;
    Token token;

    private boolean firstLoadMessages=false;

    User myUser;
    String conversationId;

    Stack<chatText>temp=new Stack<>(); //for receiving messages that comes in a reverse order when the count is more than one "check readtexts()"
    int count;
    int downArrow_count=0;
    boolean canscrollDown=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        appStatesHandling=new AppStatesHandling();

        toolbar=findViewById(R.id.Message_toolbar);
        message_image=findViewById(R.id.Message_image);
        message_title=findViewById(R.id.Message_title);
        message_text=findViewById(R.id.message_text);
        message_status=findViewById(R.id.Message_status);
        downArrow=findViewById(R.id.downArrowBtn);
        downArrowCount=findViewById(R.id.downArrowCount);

        recyclerView=findViewById(R.id.messages_recycleview);
        recyclerView.setHasFixedSize(true);

       // recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTaskRoot()){
                    Intent intent=new Intent(getApplicationContext(),activity_signed.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    Log.d(TAG, "onClick: Wooow Rooot is message!!!!!!!!!");
                }else
                finish();
            }
        });
        intent=getIntent();
        userId=intent.getStringExtra("userId");
        userName=intent.getStringExtra("userName");
        userImage=intent.getStringExtra("userImage");
        getChatId(firebaseUser.getUid(),userId);


        mchatTexts=new ArrayList<>();

        apiService= Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        firstLoadMessages=true;





       recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
           @Override
           public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
               super.onScrolled(recyclerView, dx, dy);
              if(!isRecyclerScrollable()){
                  downArrow.setVisibility(View.GONE);
                  downArrow.setAlpha((float) 0.6);
                  downArrow_count=0;
                  downArrowCount.setVisibility(View.GONE);
                    canscrollDown=true;
              }
              else {
                  downArrow.setVisibility(View.VISIBLE);
              }
           }
       });

    }

    private void seenMessage() {

        reference_messages=FirebaseDatabase.getInstance().getReference().child("chats").child("conversations").child(conversationId);
        seen_query=reference_messages.orderByChild("seen").equalTo(false);
        seenListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String,Object>map=new HashMap<>();
                map.put("seen",true);
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    if(snapshot.getValue(chatText.class).getRecieverId().equals(firebaseUser.getUid()))
                        snapshot.getRef().updateChildren(map);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        seen_query.addValueEventListener(seenListener);
    }


    private void setMessage_info(){
        message_title.setText(userName);
        if(userImage.equals("default")){
            Glide.with(getApplicationContext())
                    .load(R.drawable.profile_avatar)
                    .into(message_image);
        }else {
            Glide.with(getApplicationContext())
                    .load(userImage)
                    .into(message_image);
        }
        reference.child(userId).child("information").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 User user=dataSnapshot.getValue(User.class);
                //phonenumber if user don't have a name
                dataSnapshot.getRef().getParent().child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(firebaseUser.getUid())) {
                            dataSnapshot.getRef().child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    myName = dataSnapshot.getValue(String.class);
                                    Log.d(TAG, "myName: " + myName);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }else {
                            reference.child(firebaseUser.getUid()).child("information").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User user1=dataSnapshot.getValue(User.class);
                                    myName=user1.getPhoneNumber();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                if(!user.getUriPath().equals(userImage)&&!user.getUriPath().equals("default")){
                    Glide.with(getApplicationContext())
                            .load(user.getUriPath())
                            .into(message_image);

                }else if(user.getUriPath().equals("default")){
                    Glide.with(getApplicationContext())
                            .load(R.drawable.profile_avatar)
                            .into(message_image);
                }
                if(user.getActiveStatus().equals("Online"))
                message_status.setText(user.getActiveStatus());
                else{
                    ParsePosition pos = new ParsePosition(0);
                    SimpleDateFormat simpledateformat = new SimpleDateFormat("EEE MMM d HH:mm:ss zz yyyy");
                    Date stringDate = simpledateformat.parse(user.getActiveStatus(), pos);
                    Date currentTime = Calendar.getInstance().getTime();
                    String lastseen="";
                    if(currentTime.after(stringDate)){
                       if(currentTime.getDay()!=stringDate.getDay()){
                           String day= (String)DateFormat.format("dd",stringDate);
                           String month=(String)DateFormat.format("MMM",stringDate);
                           String year=(String)DateFormat.format("yyyy",stringDate);
                            lastseen="Last seen: "+day+"-"+month+"-"+year;
                       }else {
                           String origin="am";
                           String hour="";
                           String minutes="";
                           if(stringDate.getHours()>12){
                               origin="pm";
                               stringDate.setHours(stringDate.getHours()-12);
                           }
                           if(stringDate.getHours()<10)
                               hour="0"+stringDate.getHours();
                           else hour=String.valueOf(stringDate.getHours());
                           if(stringDate.getMinutes()<10){
                               minutes="0"+stringDate.getMinutes();
                           }else minutes=String.valueOf(stringDate.getMinutes());

                           lastseen="Last seen: today "+hour+":"+minutes+" "+origin;
                       }
                    }
                    message_status.setText(lastseen);
                }

                if(firstLoadMessages){
                    readTexts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).child("information");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myUser=dataSnapshot.getValue(User.class);
                uriImage=myUser.getUriPath();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        }

    public void handleSendBtn(View view){
        String text=message_text.getText().toString();
        message_text.setText("");
        downArrow.setVisibility(View.GONE);
        if(!text.isEmpty()){
                notify=true;
                sendText(firebaseUser.getUid(),userId,text);


        }else {
            Toast.makeText(this, "Can't send an empty message", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendText(final String senderId, final String recieverId, final String text){



        final DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("users").child(recieverId).child("information");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("chats").child("conversations");
        String MessageId = databaseReference.child(conversationId).push().getKey();
        ref.child("activeStatus").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                final chatText chatText = new chatText(MessageId, senderId, recieverId, text, false, false, false);

                                mchatTexts.add(chatText);
                                messageViewAdapter.notifyItemInserted(mchatTexts.size() - 1);
                                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount()-1);
                                firstLoadMessages = false;
                                databaseReference.child(conversationId).child(MessageId).setValue(chatText).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("sent", true);
                                        databaseReference.child(conversationId).child(MessageId).updateChildren(map);
                                        if (notify) {
                                            sendNotification(text, recieverId, myUser, conversationId, MessageId);
                                        }
                                    }
                                });

                                    ref.child("checkMessages").setValue(true);
                                    ref.getParent().child("Messages").child(conversationId).setValue(conversationId);

                            }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }});
        DatabaseReference refr=FirebaseDatabase.getInstance().getReference()
                .child("users");
        refr.child(firebaseUser.getUid()).child("chats").child("conversations").child(userId).setValue(conversationId);
        refr.child(userId).child("chats").child("conversations").child(firebaseUser.getUid()).setValue(conversationId);


    }


    private void getToken(String recieverId){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Tokens");
        Query query=reference.orderByKey().equalTo(recieverId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    token = snapshot.getValue(Token.class);
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(String msg,String recieverId,User user,String conversationId,String messageId) {

        Data data = new Data(user.getId(), uriImage, msg, myName, recieverId,conversationId,messageId);
        Sender sender = new Sender(token.getToken(), data);
        if (notify) {
            notify = false;

            apiService.sendNotification(sender)
                    .enqueue(new Callback<MyResponce>() {
                        @Override
                        public void onResponse(Call<MyResponce> call, Response<MyResponce> response) {

                        }

                        @Override
                        public void onFailure(Call<MyResponce> call, Throwable t) {

                        }
                    });


        }
    }

    private void readTexts(){ //also for receiving texts

            mchatTexts.clear();
            DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("chats").child("conversations");
            reference.child(conversationId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (firstLoadMessages) {
                        firstLoadMessages = false;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            chatText chatText = snapshot.getValue(chatText.class);
                            mchatTexts.add(chatText);

                        }
                        messageViewAdapter.notifyDataSetChanged();
                    } else {

                        if (mchatTexts.size() < dataSnapshot.getChildrenCount()) {

                            count = (int) dataSnapshot.getChildrenCount() - mchatTexts.size();
                            downArrow.setAlpha(1);
                            downArrow_count++;
                            //Log.d(TAG, "canScrollDown: "+canscrollDown);
                            if(downArrow_count>0&&downArrow.getVisibility()==View.VISIBLE)
                                downArrowCount.setVisibility(View.VISIBLE);
                            downArrowCount.setText(String.valueOf(downArrow_count));
                            int x = (int) dataSnapshot.getChildrenCount();


                            Query query = reference.child(conversationId).orderByChild("id").limitToLast(count);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (mchatTexts.size() <= x) {
                                        if (temp.size() < count) {

                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                boolean cantadd = false;
                                                chatText chatText = snapshot.getValue(chatText.class);
                                                if (count == 1) {
                                                    mchatTexts.add(chatText);
                                                    messageViewAdapter.notifyItemInserted(mchatTexts.size() - 1);
                                                    break;
                                                }
                                                for (chatText tempo : temp) {
                                                    if (tempo.getId().equals(chatText.getId())) {
                                                        cantadd = true;
                                                    }
                                                }
                                                if (!cantadd) {
                                                    temp.push(chatText);
                                                }
                                            }

                                        } else {

                                            for (int i = 0; i < temp.size(); i++) {
                                                mchatTexts.add(temp.pop());
                                                messageViewAdapter.notifyItemInserted(mchatTexts.size() - 1);
                                                count--;
                                            }


                                        }
                                        if(canscrollDown){

                                            canscrollDown=false;
                                            scrolldownOnce();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }
                    }

                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


    }


    private void getChatId(String first_id,String second_id){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("users");
            reference.child(first_id).child("chats").child("conversations").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(second_id)){
                        conversationId=reference.child("chats").child("conversations").push().getKey();
                        Log.d(TAG, "ConversationID: "+conversationId);
                        StartContents();
                        SharedPreferences sharedPreferences=getSharedPreferences("MySharedPref",MODE_PRIVATE);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString(userId,conversationId);
                        editor.apply();

                    }else {
                        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        SharedPreferences.Editor Editor=sharedPreferences.edit();
                        String s = sharedPreferences.getString(userId, "");
                        if (!s.equals("")) {
                            Log.d(TAG, "Conversation Id(shared) : "+s);
                            conversationId = s;
                            StartContents();
                        } else {

                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("users").child(first_id)
                                    .child("chats")
                                    .child("conversations")
                                    .child(second_id);
                           reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        conversationId = dataSnapshot.getValue(String.class);
                                        Editor.putString(userId,conversationId);
                                        Editor.apply();
                                        Log.d(TAG, "Conversation Id(Database) : "+conversationId);
                                        StartContents();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    private void StartContents(){
        messageViewAdapter=new MessageViewAdapter(getApplicationContext(),conversationId,mchatTexts);
        recyclerView.setAdapter(messageViewAdapter);
        setMessage_info();
        seenMessage();
        getToken(userId);
    }
    private void currentUser(String userId){
        SharedPreferences.Editor editor=getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentUser",userId);
        editor.apply();
    }
    @Override
    protected void onPause() {
        super.onPause();
        appStatesHandling.incrementPaused();
        currentUser("none");

        if(seenListener!=null)
            seen_query.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appStatesHandling.incrementResumed();
        currentUser(userId);

        if(seenListener!=null)
            seen_query.addValueEventListener(seenListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        appStatesHandling.incrementStopped();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onStart() {
        super.onStart();
        appStatesHandling.incrementStarted();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setMessage_info();
        readTexts();
    }

    public void handleDownArrowBtn(View view) {
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount()-1);

    }
    public boolean isRecyclerScrollable() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (layoutManager == null || adapter == null) return false;
        return layoutManager.findLastCompletelyVisibleItemPosition() < adapter.getItemCount() - 2;
    }
    private void scrolldownOnce(){
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (layoutManager != null || adapter != null){
            if(adapter.getItemCount()-1-layoutManager.findLastCompletelyVisibleItemPosition()==0)
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
            else
            recyclerView.scrollToPosition(layoutManager.findLastVisibleItemPosition()+1);

        }



    }
}
