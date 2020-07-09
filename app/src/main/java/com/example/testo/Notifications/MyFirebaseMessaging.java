package com.example.testo.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.testo.ImageFilePath;
import com.example.testo.MessageActivity;
import com.example.testo.R;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Model.chatText;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    final String TAG="a7oo";
    NotificationManager notificationManager=null;
    int count=0;
    String CHANNEL_ID="SDASDSA";
    ArrayList<Integer>chatNames=new ArrayList<>();
    ArrayList<InboxStyleObject>inboxStyleArrayList=new ArrayList<>();
    NotificationManager manager;


    @Override
    public void onCreate() {
        super.onCreate();
        manager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: "+remoteMessage.getData().get("body"));
        SharedPreferences sharedPreferences=getSharedPreferences("PREFS",MODE_PRIVATE);
        String currentUser=sharedPreferences.getString("currentUser","none");
        String user=remoteMessage.getData().get("user");
        String sent_to=remoteMessage.getData().get("sent_to");
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null&&sent_to.equals(firebaseUser.getUid())) {
            if(!currentUser.equals(user)) {
                count++;
                showNotification(remoteMessage);

            }
        }
    }

    private void showNotification(RemoteMessage remoteMessage) {
        String user=remoteMessage.getData().get("user");
        String body=remoteMessage.getData().get("body");
        String title=remoteMessage.getData().get("title");
        Log.d(TAG, "showNotification: Title: "+title);
        String icon= remoteMessage.getData().get("icon");
        String conversationId=remoteMessage.getData().get("conversationId");
        String messageId=remoteMessage.getData().get("messageId");
        Map<String,Object>map=new HashMap<>();
        map.put("delivered",true);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        reference.child("chats").child("conversations").child(conversationId).child(messageId).updateChildren(map);
        int j=Integer.parseInt(user.replaceAll("[\\D]",""));




            reference.child("chats").child("conversations").child(conversationId).child(messageId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    chatText chatText=dataSnapshot.getValue(Model.chatText.class);
                    if(chatText.isSeen()){
                        if(chatNames.contains(j)){
                            int temp=chatNames.indexOf(j);
                            manager.cancel(j);
                            count-=inboxStyleArrayList.get(temp).getCount();
                            inboxStyleArrayList.remove(temp);
                            chatNames.remove(temp);
                            Log.d(TAG, "OnDataChange: Count "+count);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            Intent intent=new Intent(this, MessageActivity.class);
            intent.putExtra("userId",user);
            intent.putExtra("userImage",icon);
            intent.putExtra("userName",title);
            intent.putExtra("frombackground","true");
            PendingIntent pendingIntent=PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_CANCEL_CURRENT);
            Uri default_sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


            int i=0;
            int arrayindex=0;
            if(j>0){
                i=j;
                if(!chatNames.contains(i))
                {
                    chatNames.add(i);
                    InboxStyleObject styleObject =new InboxStyleObject();
                    styleObject.setInboxStyle(new NotificationCompat.InboxStyle());
                    inboxStyleArrayList.add(styleObject);
                }
                arrayindex=chatNames.indexOf(i);
            }

             inboxStyleArrayList.get(arrayindex).getInboxStyle().addLine(body);
             inboxStyleArrayList.get(arrayindex).incrementCount();

            NotificationCompat.Builder  mBuilder=new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSound(default_sound)
                    .setSmallIcon(R.drawable.chat_icon)
                    .setAutoCancel(true)
                    .setStyle(inboxStyleArrayList.get(arrayindex).getInboxStyle())
                     .setGroup("Messages Group")
                     .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            ;
            if(!icon.equals("default")) {
                Bitmap bitmap = ImageFilePath.getBitmapFromURL(icon);
                bitmap = ImageFilePath.getCircleBitmap(bitmap);
                mBuilder.setLargeIcon(bitmap);
            }else {
                Bitmap bitmap= BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.profile_avatar);
                mBuilder.setLargeIcon(bitmap);
            }
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Receive messages", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.GREEN);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            channel.setShowBadge(true);


            manager.createNotificationChannel(channel);
            manager.notify(i,mBuilder.build());


            if(chatNames.size()>1){
                NotificationCompat.InboxStyle inboxStyle=new NotificationCompat.InboxStyle();
                String exo=count+" messages from "+chatNames.size()+" chats.";
                inboxStyle.setSummaryText(exo);
                NotificationCompat.Builder  Builder=new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setContentIntent(pendingIntent)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSound(default_sound)
                        .setSmallIcon(R.drawable.chat_icon)
                        .setAutoCancel(true)
                        .setStyle(inboxStyle
                        )
                        .setGroup("Messages Group")
                        .setGroupSummary(true)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                        ;
                manager.notify(015,Builder.build());
            }

        }
        else {


            Intent intent=new Intent(this, MessageActivity.class);
            intent.putExtra("userId",user);
            intent.putExtra("userImage",icon);
            intent.putExtra("userName",title);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent=PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_CANCEL_CURRENT);

            Uri default_sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            int arrayindex=0;
            int i=0;
            if(j>0){
                i=j;
                if(!chatNames.contains(i))
                {
                    chatNames.add(i);
                    InboxStyleObject styleObject =new InboxStyleObject();
                    styleObject.setInboxStyle(new NotificationCompat.InboxStyle());
                    inboxStyleArrayList.add(styleObject);
                }
                arrayindex=chatNames.indexOf(i);
            }
            inboxStyleArrayList.get(arrayindex).getInboxStyle().addLine(body);
            inboxStyleArrayList.get(arrayindex).incrementCount();


            NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.chat_icon)
                    .setContentText(body)
                    .setContentTitle(title)
                    .setAutoCancel(true)
                    .setSound(default_sound)
                    .setStyle(inboxStyleArrayList.get(arrayindex).getInboxStyle())
                    .setGroup("Messages Group")
                    .setContentIntent(pendingIntent)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                    ;
            if(!icon.equals("default")) {
                Bitmap bitmap = ImageFilePath.getBitmapFromURL(icon);
                bitmap = ImageFilePath.getCircleBitmap(bitmap);
                builder.setLargeIcon(bitmap);
            }else {
                Bitmap bitmap= BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.profile_avatar);
                builder.setLargeIcon(bitmap);
            }
            getNotificationManager().notify(i,builder.build());

            if(chatNames.size()>1){
                NotificationCompat.InboxStyle inboxStyle=new NotificationCompat.InboxStyle();
                String exo=count+" messages from "+chatNames.size()+" chats.";
                inboxStyle.setSummaryText(exo);
                NotificationCompat.Builder  Builder=new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setContentIntent(pendingIntent)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSound(default_sound)
                        .setSmallIcon(R.drawable.chat_icon)
                        .setAutoCancel(true)
                        .setStyle(inboxStyle
                        )
                        .setGroup("Messages Group")
                        .setGroupSummary(true)
                        ;
                manager.notify(015,Builder.build());
            }

        }


    }
    private NotificationManager getNotificationManager(){
        if(notificationManager==null) {
            notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }
}
