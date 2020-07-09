package com.example.testo.onAppDestroyed;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class onClearTaskFromRecentList extends Service {
    final String TAG="a7aaaa";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Date currentTime = Calendar.getInstance().getTime();
        Status(currentTime.toString());
        Log.d(TAG, "onTaskRemoved: "+currentTime.toString());
        stopSelf();
        //stopForeground(false);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Date currentTime = Calendar.getInstance().getTime();
        Status(currentTime.toString());
        Log.d(TAG, "onDestroy: ");
    }


    private void Status(String status){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        reference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("information").child("activeStatus").setValue(status);
    }
}
