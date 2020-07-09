package com.example.testo.onAppDestroyed;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class AppStatesHandling  {
    final static String TAG="a7eh";
    public static int resumed;
    public static int paused;
    public static int started;
    public static int stopped;
    public  void incrementResumed(){
        resumed++;
        if(resumed>paused){
            Log.d(TAG, "incrementResumed: ");
        }
    }
    public void incrementPaused(){
        paused++;
        if(resumed>paused){
            Status("Online");
            Log.d(TAG, "incrementPaused: "+" App is open");
        }
    }
    public void incrementStarted(){
        started++;
        if(started>stopped){
            Status("Online");
            Log.d(TAG, "incrementStarted: "+" App is Open");
        }else if(started==stopped){
            Log.d(TAG, "incrementStarted: ");
        }
    }
    public void incrementStopped(){
        stopped++;
        if(started>stopped){

            Log.d(TAG, "incrementStopped: "+" App is Open");
        }else if(started==stopped){
            Status(Calendar.getInstance().getTime().toString());
            Log.d(TAG, "incrementStopped: "+" App is in background apps");
        }

    }
    private void Status(final String status){
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("information").child("activeStatus").setValue(status);
        }
    }
}
